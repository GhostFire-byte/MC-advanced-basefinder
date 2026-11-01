package com.basefinder;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.engine.BlockEventType;
import com.basefinder.fusion.FusionManager;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.test.FakeWorld;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Deeper debug: concurrency, event flooding, dimension churn, and fusion edge cases. */
class DeepDebugTest {

    private FakeWorld richBase() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        w.addBlockEntity(new BlockRef(new Vec3i(5, 70, 5), "minecraft:beacon"));
        for (int i = 0; i < 6; i++) {
            w.addBlockEntity(new BlockRef(new Vec3i(4 + i, 70, 6), "minecraft:chest"));
        }
        w.setBlock(5, 70, 7, "minecraft:red_bed");
        w.setBlock(6, 70, 7, "minecraft:lodestone");
        return w;
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void moduleSurvivesConcurrentEventFloodingDuringCycles() throws InterruptedException {
        FakeWorld w = richBase();
        BaseFinderModule module = new BaseFinderModule(() -> w, new InMemorySettingsBridge());
        module.start();
        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);

        // Two writer threads: block changes + block events, hammering while cycles run.
        Thread changes = new Thread(() -> {
            long t = 0;
            while (!stop.get()) {
                try {
                    module.onBlockChange(new Vec3i((int) (t % 40), 70, (int) (t % 40)),
                            "minecraft:repeater", "minecraft:repeater", t++);
                } catch (Throwable e) {
                    failed.set(true);
                }
            }
        });
        Thread events = new Thread(() -> {
            long t = 0;
            while (!stop.get()) {
                try {
                    module.onBlockEvent(BlockEventType.CONTAINER_OPEN,
                            new Vec3i(5, 70, 5), "minecraft:chest", t++);
                } catch (Throwable e) {
                    failed.set(true);
                }
            }
        });
        changes.start();
        events.start();
        try {
            for (int i = 0; i < 200; i++) {
                w.tick(i * 25L);
                module.onClientTick(i * 25L, w);
                Thread.sleep(10);
            }
        } finally {
            stop.set(true);
            changes.join(2000);
            events.join(2000);
            module.stop();
        }
        assertFalse(failed.get(), "concurrent event delivery must never throw");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void dimensionChurnAndNullWorldInterleaved() throws InterruptedException {
        FakeWorld overworld = richBase();
        FakeWorld nether = new FakeWorld().player(0, 130, 0).bounds(120, 140).dimension(Dimension.NETHER);
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                nether.setBlock(x, 130, z, "minecraft:magma_block");
            }
        }
        FakeWorld[] cur = {overworld};
        BaseFinderModule module = new BaseFinderModule(() -> cur[0], new InMemorySettingsBridge());
        module.start();
        try {
            for (int i = 0; i < 120; i++) {
                if (i == 40) {
                    module.onWorldChanged("dimension");
                    cur[0] = nether;
                } else if (i == 80) {
                    module.onWorldChanged("disconnect");
                    cur[0] = null;
                }
                long t = i * 25L;
                if (cur[0] != null) {
                    cur[0].tick(t);
                }
                final int fi = i;
                assertDoesNotThrow(() -> module.onClientTick(fi * 25L, cur[0]));
                Thread.sleep(8);
            }
        } finally {
            module.stop();
        }
    }

    @Test
    void fusionHandlesEmptyAndSingleWeakSignal() {
        FusionManager fm = new FusionManager();
        assertTrue(fm.update(List.of(), 1).isEmpty());
        BaseCandidate weak = fm.update(List.of(
                Finding.builder("s", FindingCategory.TERRAIN_ANOMALY, new Vec3i(0, 64, 0))
                        .confidence(20).signalWeight(0.3).build()), 2).get(0);
        // Ambient-only, low confidence -> not a base, capped.
        assertFalse(weak.kind() == BaseCandidate.CandidateKind.BASE);
    }

    @Test
    void fusionManyClustersStayIndependent() {
        FusionManager fm = new FusionManager();
        List<Finding> findings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Vec3i p = new Vec3i(i * 64, 64, i * 64); // far apart -> separate cells
            findings.add(Finding.builder("s", FindingCategory.STORAGE, p)
                    .confidence(100).signalWeight(0.5).build());
        }
        List<BaseCandidate> cands = fm.update(findings, 1);
        assertTrue(cands.size() >= 40, "distant findings should not all merge");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void repeatedWorldChangesDoNotLeakCandidates() {
        FusionManager fm = new FusionManager();
        for (int cycle = 0; cycle < 500; cycle++) {
            fm.update(List.of(Finding.builder("s", FindingCategory.STORAGE, new Vec3i(cycle, 64, 0))
                    .confidence(90).signalWeight(0.5).build()), cycle);
            if (cycle % 10 == 0) {
                fm.reset();
            }
        }
        // After the loop, nothing should have accumulated unbounded (reset + decay keep it small).
        assertTrue(fm.candidates().size() < 50);
    }
}
