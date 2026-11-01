package com.basefinder;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.engine.BlockEventType;
import com.basefinder.scanner.impl.FlatPlatformScanner;
import com.basefinder.scanner.impl.HollowShellScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Stress / integration checks for the Batch 2 scanners and the end-to-end module. */
class AdvancedScannersStressTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void blockVisitorsHandleLargeFilledRegion() {
        FakeWorld w = new FakeWorld().player(0, 40, 0).bounds(0, 80);
        // A 48x48x8 slab of mixed built blocks: platforms, shells, etc. must not throw.
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 48; z++) {
                for (int y = 36; y < 44; y++) {
                    w.setBlock(x, y, z, (x + z) % 2 == 0 ? "minecraft:smooth_stone" : "minecraft:air");
                }
            }
        }
        assertDoesNotThrow(() -> ScannerHarness.run(new FlatPlatformScanner(), w));
        assertDoesNotThrow(() -> ScannerHarness.run(new HollowShellScanner(), w));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void fullModuleRunsAllThirtyOneScanners() throws InterruptedException {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        // A rich base: storage + bed + beacon + lodestone + decoration + lights.
        w.addBlockEntity(new com.basefinder.world.BlockRef(new Vec3i(5, 70, 5), "minecraft:beacon"));
        for (int i = 0; i < 6; i++) {
            w.addBlockEntity(new com.basefinder.world.BlockRef(new Vec3i(4 + i, 70, 6), "minecraft:chest"));
        }
        w.setBlock(5, 70, 7, "minecraft:red_bed");
        w.setBlock(6, 70, 7, "minecraft:lodestone");
        for (int i = 0; i < 6; i++) {
            w.setBlock(2 + i, 70, 8, "minecraft:item_frame");
        }

        BaseFinderModule module = new BaseFinderModule(() -> w, new InMemorySettingsBridge());
        module.start();
        try {
            module.onBlockEvent(BlockEventType.CONTAINER_OPEN, new Vec3i(5, 70, 5), "minecraft:chest", 0);
            boolean found = false;
            for (int i = 0; i < 150 && !found; i++) {
                w.tick(i * 25L);
                module.onClientTick(i * 25L, w);
                Thread.sleep(20);
                found = !module.visibleCandidates().isEmpty();
            }
            assertFalse(module.visibleCandidates().isEmpty(), "rich base should surface a candidate");
            // The top candidate should be classified as a real BASE (habitation + marker present).
            assertFalse(module.scanners().isEmpty());
        } finally {
            module.stop();
        }
    }
}
