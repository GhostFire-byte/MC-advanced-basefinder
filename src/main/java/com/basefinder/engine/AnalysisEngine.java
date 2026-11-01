package com.basefinder.engine;

import com.basefinder.config.GlobalSettings;
import com.basefinder.config.Settings;
import com.basefinder.config.SubsystemSettings;
import com.basefinder.diag.DiagnosticsLog;
import com.basefinder.fusion.FusionManager;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanBudget;
import com.basefinder.scanner.ScanContext;
import com.basefinder.scanner.Scanner;
import com.basefinder.scanner.ScannerRegistry;
import com.basefinder.scanner.ScannerStatus;
import com.basefinder.world.ScanScope;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import com.basefinder.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The central analysis engine. Drives timed, budgeted, off-thread scan cycles:
 * capture snapshot (on the client thread) &rarr; fan scanners out to a bounded
 * worker pool &rarr; fuse findings into base candidates &rarr; run the cycle
 * callback (rules + notifications) &rarr; publish results for the UI.
 *
 * <p>Every scanner call is guarded so one failing subsystem cannot crash the
 * engine, and the whole cycle is guarded so a bad snapshot cannot stall the game.
 */
public final class AnalysisEngine {

    private final ScannerRegistry registry;
    private final Settings settings;
    private final WorldSource worldSource;
    private final FusionManager fusion;
    private final DiagnosticsLog log;
    private final PerformanceGovernor governor;
    private final EngineMetrics metrics = new EngineMetrics();
    private final DataSourceHealth health = new DataSourceHealth();
    private final ScopeSweeper sweeper;

    private ExecutorService coordinator;
    private ExecutorService workers;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean cycleInProgress = new AtomicBoolean(false);

    private volatile long lastCycleTick = Long.MIN_VALUE;
    private volatile EngineResults results = EngineResults.empty();
    private volatile CycleCallback callback;
    private volatile VillageContext lastVillage = VillageContext.empty();

    public AnalysisEngine(ScannerRegistry registry, Settings settings, WorldSource worldSource,
                          FusionManager fusion, DiagnosticsLog log) {
        this.registry = registry;
        this.settings = settings;
        this.worldSource = worldSource;
        this.fusion = fusion;
        this.log = log;
        this.governor = new PerformanceGovernor(6.0);
        this.sweeper = new ScopeSweeper(settings, registry, log);
    }

    public void setCallback(CycleCallback callback) {
        this.callback = callback;
    }

    public EngineResults results() {
        return results;
    }

    public EngineMetrics metrics() {
        return metrics;
    }

    public DataSourceHealth health() {
        return health;
    }

    public ScannerRegistry registry() {
        return registry;
    }

    public boolean running() {
        return running.get();
    }

    public synchronized void start() {
        if (running.get()) {
            return;
        }
        coordinator = Executors.newSingleThreadExecutor(named("basefinder-cycle"));
        workers = Executors.newFixedThreadPool(governor.maxWorkers(), named("basefinder-worker"));
        running.set(true);
        log.info("engine", "Analysis engine started (max " + governor.maxWorkers() + " workers)");
    }

    public synchronized void stop() {
        if (!running.get()) {
            return;
        }
        running.set(false);
        shutdown(coordinator);
        shutdown(workers);
        coordinator = null;
        workers = null;
        log.info("engine", "Analysis engine stopped");
    }

    /** Called by the host once per client tick (on the client thread). Cheap and non-blocking. */
    public void onClientTick(long tick) {
        if (!running.get()) {
            return;
        }
        log.setCurrentTick(tick);
        int interval = settings.global().aggressiveness().scanIntervalTicks();
        if (cycleInProgress.get()) {
            return;
        }
        if (lastCycleTick != Long.MIN_VALUE && tick - lastCycleTick < interval) {
            return;
        }

        WorldSnapshot snapshot;
        try {
            snapshot = worldSource.current();
        } catch (Throwable t) {
            health.markUnavailable("World source error: " + t.getMessage());
            return;
        }
        if (snapshot == null) {
            health.markUnavailable("No world loaded");
            return;
        }
        health.markAvailable(tick);

        ScanScope scope = buildScope(snapshot);
        ScanBudget budget = new ScanBudget(settings.global().aggressiveness().blockBudgetPerCycle());
        lastCycleTick = tick;
        cycleInProgress.set(true);
        coordinator.execute(() -> runCycle(snapshot, scope, budget, tick));
    }

    private void runCycle(WorldSnapshot snapshot, ScanScope scope, ScanBudget budget, long tick) {
        long startNanos = System.nanoTime();
        List<Finding> all = new ArrayList<>();
        int workerCount = governor.current();
        try {
            VillageContext village;
            try {
                village = VillageContext.from(snapshot);
            } catch (Throwable t) {
                village = VillageContext.empty();
            }
            lastVillage = village;
            final VillageContext villageRef = village;
            ConcurrentLinkedQueue<Finding> collected = new ConcurrentLinkedQueue<>();
            List<Runnable> tasks = new ArrayList<>();
            List<Scanner> visitorScanners = new ArrayList<>();
            for (Scanner scanner : registry.enabled()) {
                if (scanner instanceof com.basefinder.scanner.BlockVisitor) {
                    visitorScanners.add(scanner);
                } else {
                    tasks.add(() -> runScanner(scanner, snapshot, scope, budget, tick, collected, villageRef));
                }
            }
            if (!visitorScanners.isEmpty()) {
                tasks.add(() -> sweeper.sweep(visitorScanners, snapshot, scope, budget, tick, collected, villageRef));
            }
            ParallelRunner.runAll(workers, tasks, workerCount);
            all.addAll(collected);

            List<BaseCandidate> candidates = fusion.update(all, tick, villageRef);
            if (callback != null) {
                try {
                    callback.onCycleComplete(candidates, all, tick);
                } catch (Throwable t) {
                    log.error("engine", "Cycle callback failed: " + t.getMessage());
                }
            }
            results = new EngineResults(candidates, all, tick);

            double cycleMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
            metrics.recordCycle(cycleMillis, workerCount, candidates.size(), all.size(), budget.used());
            governor.adjust(cycleMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            // Fail-safe: a cycle must never crash the module.
            log.error("engine", "Scan cycle failed: " + t.getMessage());
        } finally {
            cycleInProgress.set(false);
        }
    }

    private void runScanner(Scanner scanner, WorldSnapshot snapshot, ScanScope scope, ScanBudget budget,
                            long tick, ConcurrentLinkedQueue<Finding> collected, VillageContext village) {
        ScannerStatus status = registry.status(scanner.id());
        SubsystemSettings sub = settings.subsystem(scanner.id());
        GlobalSettings global = settings.global();
        long t0 = System.nanoTime();
        int before = collected.size();
        FindingSink sink = f -> {
            if (f != null) {
                collected.add(f);
            }
        };
        try {
            ScanContext ctx = new ScanContext(snapshot, scope, global, sub, tick, budget, village);
            scanner.scan(ctx, sink);
            if (status != null) {
                status.recordSuccess(System.nanoTime() - t0, collected.size() - before, tick);
            }
        } catch (Throwable t) {
            if (status != null) {
                status.recordError(t.getClass().getSimpleName() + ": " + t.getMessage());
            }
            log.warn("scanner", scanner.id() + " failed: " + t.getMessage());
        }
    }

    private ScanScope buildScope(WorldSnapshot snapshot) {
        GlobalSettings g = settings.global();
        int radius = g.detectionRadiusChunks();
        if (g.bindRadiusToRenderDistance()) {
            radius = Math.max(1, snapshot.renderDistanceChunks());
        }
        Vec3d p = snapshot.playerPos();
        Vec3i bp = p.toBlock();
        return new ScanScope(bp.chunkX(), bp.chunkZ(), radius,
                Math.min(g.minY(), g.maxY()), Math.max(g.minY(), g.maxY()));
    }

    /** Forward a live block change to any scanner that listens for activity. */
    public void onBlockChange(Vec3i pos, String oldId, String newId, long tick) {
        if (!running.get()) {
            return;
        }
        for (Scanner s : registry.all()) {
            if (s instanceof BlockChangeListener listener) {
                try {
                    listener.onBlockChange(pos, oldId, newId, tick);
                } catch (Throwable t) {
                    log.warn("scanner", s.id() + " block-change handler failed: " + t.getMessage());
                }
            }
        }
    }

    /** Forward a live block-action event (container lid, door, note, bell) to listening scanners. */
    public void onBlockEvent(BlockEventType type, Vec3i pos, String blockId, long tick) {
        if (!running.get()) {
            return;
        }
        for (Scanner s : registry.all()) {
            if (s instanceof BlockEventListener listener) {
                try {
                    listener.onBlockEvent(type, pos, blockId, tick);
                } catch (Throwable t) {
                    log.warn("scanner", s.id() + " block-event handler failed: " + t.getMessage());
                }
            }
        }
    }

    public VillageContext lastVillage() {
        return lastVillage;
    }

    /** Drop all transient state on world change / disconnect. Findings are never persisted. */
    public void onWorldChanged(String reason) {
        fusion.reset();
        registry.resetAll();
        results = EngineResults.empty();
        lastVillage = VillageContext.empty();
        health.markUnavailable(reason);
        log.info("engine", "World changed (" + reason + "); cleared transient analysis data");
    }

    public List<ScannerStatus> scannerStatuses() {
        return Collections.unmodifiableList(new ArrayList<>(registry.statuses()));
    }

    private static void shutdown(ExecutorService es) {
        if (es != null) {
            es.shutdownNow();
        }
    }

    private static ThreadFactory named(String prefix) {
        return r -> {
            Thread t = new Thread(r, prefix);
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        };
    }
}
