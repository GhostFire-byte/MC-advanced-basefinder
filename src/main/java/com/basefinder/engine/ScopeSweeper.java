package com.basefinder.engine;

import com.basefinder.config.Settings;
import com.basefinder.diag.DiagnosticsLog;
import com.basefinder.scanner.BlockVisitor;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanBudget;
import com.basefinder.scanner.ScanContext;
import com.basefinder.scanner.Scanner;
import com.basefinder.scanner.ScannerRegistry;
import com.basefinder.scanner.ScannerStatus;
import com.basefinder.world.BlockAccess;
import com.basefinder.world.ScanScope;
import com.basefinder.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Performs a single budgeted pass over the scope and dispatches every loaded
 * block to all enabled block-visitor scanners. One sweep serves all of them, so
 * the world is only read once per cycle. Runs on one worker thread, so visitors
 * can safely keep per-cycle accumulation state.
 */
final class ScopeSweeper {

    private final Settings settings;
    private final ScannerRegistry registry;
    private final DiagnosticsLog log;

    ScopeSweeper(Settings settings, ScannerRegistry registry, DiagnosticsLog log) {
        this.settings = settings;
        this.registry = registry;
        this.log = log;
    }

    void sweep(List<Scanner> visitorScanners, WorldSnapshot snapshot, ScanScope scope,
               ScanBudget budget, long tick, ConcurrentLinkedQueue<com.basefinder.model.Finding> collected,
               VillageContext village) {
        if (visitorScanners.isEmpty()) {
            return;
        }
        List<Scanner> active = new ArrayList<>();
        for (Scanner s : visitorScanners) {
            try {
                ScanContext ctx = new ScanContext(snapshot, scope, settings.global(),
                        settings.subsystem(s.id()), tick, budget, village);
                ((BlockVisitor) s).begin(ctx);
                active.add(s);
            } catch (Throwable t) {
                recordError(s, t);
            }
        }
        if (active.isEmpty()) {
            return;
        }

        BlockAccess blocks = snapshot.blocks();
        List<Scanner> airWanters = new ArrayList<>();
        for (Scanner s : active) {
            if (((BlockVisitor) s).wantsAir()) {
                airWanters.add(s);
            }
        }
        int minY = Math.max(scope.minY(), blocks.minY());
        int maxY = Math.min(scope.maxY(), blocks.maxY());
        int x0 = scope.blockMinX(), x1 = scope.blockMaxX();
        int z0 = scope.blockMinZ(), z1 = scope.blockMaxZ();

        outer:
        for (int y = minY; y <= maxY; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (budget.exhausted()) {
                        break outer;
                    }
                    budget.consume(1);
                    if (!blocks.isLoaded(x, y, z)) {
                        continue;
                    }
                    String id = blocks.blockId(x, y, z);
                    if (id == null || id.endsWith(":air") || id.endsWith("_air")) {
                        dispatchAir(airWanters, x, y, z);
                        continue;
                    }
                    int light = blocks.lightLevel(x, y, z);
                    for (Scanner s : active) {
                        try {
                            ((BlockVisitor) s).visitBlock(x, y, z, id, light);
                        } catch (Throwable t) {
                            recordError(s, t);
                        }
                    }
                }
            }
        }

        for (Scanner s : active) {
            ScannerStatus status = registry.status(s.id());
            int before = collected.size();
            try {
                FindingSink sink = f -> {
                    if (f != null) {
                        collected.add(f);
                    }
                };
                ((BlockVisitor) s).finish(sink);
                if (status != null) {
                    status.recordSuccess(0, collected.size() - before, tick);
                }
            } catch (Throwable t) {
                recordError(s, t);
            }
        }
    }

    // Some visitors (carved-void detection) care about air too; send air with a sentinel id.
    private void dispatchAir(List<Scanner> active, int x, int y, int z) {
        for (Scanner s : active) {
            try {
                ((BlockVisitor) s).visitBlock(x, y, z, "minecraft:air", 0);
            } catch (Throwable t) {
                recordError(s, t);
            }
        }
    }

    private void recordError(Scanner s, Throwable t) {
        ScannerStatus status = registry.status(s.id());
        if (status != null) {
            status.recordError(t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        log.warn("scanner", s.id() + " sweep error: " + t.getMessage());
    }
}
