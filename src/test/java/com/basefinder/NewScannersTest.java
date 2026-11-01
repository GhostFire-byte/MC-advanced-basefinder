package com.basefinder;

import com.basefinder.api.DefaultScanners;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.scanner.ScannerRegistry;
import com.basefinder.scanner.impl.EntityStackingScanner;
import com.basefinder.scanner.impl.LightingAnomalyScanner;
import com.basefinder.scanner.impl.RedstoneFrequencyScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewScannersTest {

    private boolean has(List<Finding> fs, FindingCategory c) {
        return fs.stream().anyMatch(f -> f.category() == c);
    }

    // ---- Scanner A: entity stacking --------------------------------------

    @Test
    void stackingFlagsSixChestMinecartsTogether() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        for (int i = 0; i < 6; i++) {
            w.addEntity(new EntityRef(10 + i, "minecraft:chest_minecart",
                    new Vec3d(5.0 + i * 0.1, 70, 5.0)));
        }
        assertTrue(has(ScannerHarness.run(new EntityStackingScanner(), w), FindingCategory.ENTITY_STACK));
    }

    @Test
    void stackingIgnoresFiveOrFewer() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        for (int i = 0; i < 5; i++) {
            w.addEntity(new EntityRef(10 + i, "minecraft:chest_minecart", new Vec3d(5.0, 70, 5.0)));
        }
        assertFalse(has(ScannerHarness.run(new EntityStackingScanner(), w), FindingCategory.ENTITY_STACK));
    }

    @Test
    void stackingIsPerTypeNotMixed() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        // 3 minecarts + 3 pigs in one spot: per-type, neither reaches 6, so no stack.
        for (int i = 0; i < 3; i++) {
            w.addEntity(new EntityRef(10 + i, "minecraft:minecart", new Vec3d(5.0, 70, 5.0)));
            w.addEntity(new EntityRef(20 + i, "minecraft:pig", new Vec3d(5.0, 70, 5.0)));
        }
        assertFalse(has(ScannerHarness.run(new EntityStackingScanner(), w), FindingCategory.ENTITY_STACK));
    }

    @Test
    void stackingFlagsStackedMobs() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        for (int i = 0; i < 8; i++) {
            w.addEntity(new EntityRef(30 + i, "minecraft:zombie", new Vec3d(4.0, 64, 4.0)));
        }
        assertTrue(has(ScannerHarness.run(new EntityStackingScanner(), w), FindingCategory.ENTITY_STACK));
    }

    // ---- Scanner B: redstone frequency -----------------------------------

    @Test
    void redstoneFrequencyFlagsHighRateNearRedstone() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100);
        // Surround the spot with redstone so the adaptive threshold is low.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                w.setBlock(5 + dx, 70, 5 + dz, "minecraft:redstone_wire");
            }
        }
        RedstoneFrequencyScanner scanner = new RedstoneFrequencyScanner();
        for (int i = 0; i < 12; i++) {
            scanner.onBlockChange(new Vec3i(5, 70, 5), "minecraft:repeater", "minecraft:repeater", 100);
        }
        assertTrue(has(ScannerHarness.run(scanner, w), FindingCategory.REDSTONE_CLOCK));
    }

    @Test
    void redstoneFrequencyIgnoresSparseUpdatesInOpen() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100);
        RedstoneFrequencyScanner scanner = new RedstoneFrequencyScanner();
        // No redstone nearby => high adaptive threshold; 10 updates shouldn't flag.
        for (int i = 0; i < 10; i++) {
            scanner.onBlockChange(new Vec3i(5, 70, 5), "minecraft:stone", "minecraft:air", 100);
        }
        assertFalse(has(ScannerHarness.run(scanner, w), FindingCategory.REDSTONE_CLOCK));
    }

    @Test
    void redstoneFrequencyExpiresOldUpdates() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(5000);
        RedstoneFrequencyScanner scanner = new RedstoneFrequencyScanner();
        for (int i = 0; i < 20; i++) {
            scanner.onBlockChange(new Vec3i(5, 70, 5), "minecraft:repeater", "minecraft:repeater", 10);
        }
        assertFalse(has(ScannerHarness.run(scanner, w), FindingCategory.REDSTONE_CLOCK));
    }

    // ---- Scanner C: lighting anomaly -------------------------------------

    @Test
    void lightingFlagsLightInDarkness() {
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        // Deep underground torches with no lava: anomalous.
        w.setBlock(3, 30, 3, "minecraft:torch");
        w.setBlock(4, 30, 3, "minecraft:torch");
        assertTrue(has(ScannerHarness.run(new LightingAnomalyScanner(), w), FindingCategory.LIGHTING));
    }

    @Test
    void lightingSkipsLightsNextToLava() {
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        w.setBlock(3, 30, 3, "minecraft:torch");
        w.setBlock(3, 30, 4, "minecraft:lava"); // natural emitter nearby => expected light high
        assertFalse(has(ScannerHarness.run(new LightingAnomalyScanner(), w), FindingCategory.LIGHTING));
    }

    @Test
    void lightingFlagsHighDensityCluster() {
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        for (int i = 0; i < 8; i++) {
            w.setBlock(3 + (i % 3), 30, 3 + (i / 3), "minecraft:lantern");
        }
        assertTrue(has(ScannerHarness.run(new LightingAnomalyScanner(), w), FindingCategory.LIGHTING));
    }

    // ---- Registration ----------------------------------------------------

    @Test
    void allScannersRegister() {
        ScannerRegistry reg = new ScannerRegistry();
        DefaultScanners.registerAll(reg);
        assertEquals(31, reg.size());
    }
}
