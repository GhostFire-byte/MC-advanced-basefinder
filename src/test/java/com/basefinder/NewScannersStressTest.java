package com.basefinder;

import com.basefinder.model.Finding;
import com.basefinder.scanner.impl.EntityStackingScanner;
import com.basefinder.scanner.impl.LightingAnomalyScanner;
import com.basefinder.scanner.impl.RedstoneFrequencyScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Stress / edge-case / concurrency checks for the three new scanners. */
class NewScannersStressTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void entityStackingHandlesManyEntities() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        // 6000 entities spread across many small stacks + noise.
        for (int i = 0; i < 6000; i++) {
            String type = (i % 3 == 0) ? "minecraft:chest_minecart"
                    : (i % 3 == 1) ? "minecraft:zombie" : "minecraft:item";
            double x = (i % 120) + 0.1 * (i % 7);
            double z = (i / 120) + 0.1 * (i % 5);
            w.addEntity(new EntityRef(i, type, new Vec3d(x, 70, z)));
        }
        List<Finding> fs = assertDoesNotThrow(() -> ScannerHarness.run(new EntityStackingScanner(), w));
        assertTrue(fs.size() >= 0);
    }

    @Test
    void entityStackingEmptyWorldIsSafe() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        assertTrue(ScannerHarness.run(new EntityStackingScanner(), w).isEmpty());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void redstoneFrequencyConcurrentUpdatesWhileScanning() throws InterruptedException {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                w.setBlock(5 + dx, 70, 5 + dz, "minecraft:redstone_wire");
            }
        }
        RedstoneFrequencyScanner scanner = new RedstoneFrequencyScanner();
        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);

        Thread writer = new Thread(() -> {
            long t = 100;
            while (!stop.get()) {
                try {
                    scanner.onBlockChange(new Vec3i(5, 70, 5), "minecraft:repeater", "minecraft:repeater", t);
                } catch (Throwable e) {
                    failed.set(true);
                }
            }
        });
        writer.start();
        try {
            for (int i = 0; i < 200; i++) {
                w.tick(100 + i);
                assertDoesNotThrow(() -> ScannerHarness.run(scanner, w));
            }
        } finally {
            stop.set(true);
            writer.join(2000);
        }
        assertTrue(!failed.get(), "concurrent onBlockChange must not throw");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void redstoneFrequencyBufferStaysBounded() {
        RedstoneFrequencyScanner scanner = new RedstoneFrequencyScanner();
        // Flood far more than the internal buffer cap; must not OOM or throw.
        for (int i = 0; i < 100_000; i++) {
            scanner.onBlockChange(new Vec3i(i % 50, 70, i % 50), "a", "b", i);
        }
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100_000);
        assertDoesNotThrow(() -> ScannerHarness.run(scanner, w));
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void lightingHandlesHugeLightField() {
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        // A 40x40 dense lantern field underground.
        for (int x = 0; x < 40; x++) {
            for (int z = 0; z < 40; z++) {
                w.setBlock(x, 30, z, "minecraft:lantern");
            }
        }
        List<Finding> fs = assertDoesNotThrow(() -> ScannerHarness.run(new LightingAnomalyScanner(), w));
        assertTrue(!fs.isEmpty(), "a huge underground light field should flag");
    }

    @Test
    void lightingResetClearsState() {
        LightingAnomalyScanner scanner = new LightingAnomalyScanner();
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        w.setBlock(3, 30, 3, "minecraft:torch");
        w.setBlock(4, 30, 3, "minecraft:torch");
        ScannerHarness.run(scanner, w);
        scanner.reset();
        // Running on an empty world after reset should yield nothing and not carry old lights.
        FakeWorld empty = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        assertTrue(ScannerHarness.run(scanner, empty).isEmpty());
    }
}
