package com.basefinder;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.scanner.impl.AmethystScanner;
import com.basefinder.scanner.impl.BeaconScanner;
import com.basefinder.scanner.impl.BedScanner;
import com.basefinder.scanner.impl.BreadcrumbScanner;
import com.basefinder.scanner.impl.IllegalBlockScanner;
import com.basefinder.scanner.impl.LidarLightScanner;
import com.basefinder.scanner.impl.NetherHighwayScanner;
import com.basefinder.scanner.impl.PortalScanner;
import com.basefinder.scanner.impl.RedstoneScanner;
import com.basefinder.scanner.impl.SignScanner;
import com.basefinder.scanner.impl.SpawnerScanner;
import com.basefinder.scanner.impl.StorageScanner;
import com.basefinder.scanner.impl.TerrainAnomalyScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockScannersTest {

    private boolean hasCategory(List<Finding> fs, FindingCategory c) {
        return fs.stream().anyMatch(f -> f.category() == c);
    }

    @Test
    void storageScannerFindsContainersAndStash() {
        FakeWorld w = new FakeWorld().player(0, 70, 0);
        for (int i = 0; i < 6; i++) {
            w.addBlockEntity(new BlockRef(new Vec3i(2 + i, 70, 2), "minecraft:chest"));
        }
        List<Finding> fs = ScannerHarness.run(new StorageScanner(), w);
        assertTrue(hasCategory(fs, FindingCategory.STORAGE));
        assertTrue(fs.stream().anyMatch(f -> f.label().startsWith("Stash")), "dense containers should flag a stash");
    }

    @Test
    void spawnerScannerFindsSpawner() {
        FakeWorld w = new FakeWorld().player(0, 70, 0)
                .addBlockEntity(new BlockRef(new Vec3i(3, 68, 3), "minecraft:trial_spawner"));
        assertTrue(hasCategory(ScannerHarness.run(new SpawnerScanner(), w), FindingCategory.SPAWNER));
    }

    @Test
    void signScannerReadsText() {
        FakeWorld w = new FakeWorld().player(0, 70, 0)
                .addBlockEntity(new BlockRef(new Vec3i(1, 70, 1), "minecraft:oak_sign", 0, true, "Base this way"));
        List<Finding> fs = ScannerHarness.run(new SignScanner(), w);
        assertTrue(fs.stream().anyMatch(f -> f.metadata().getOrDefault("text", "").contains("Base")));
    }

    @Test
    void beaconScannerFindsBeacon() {
        FakeWorld w = new FakeWorld().player(0, 70, 0)
                .addBlockEntity(new BlockRef(new Vec3i(2, 70, 2), "minecraft:beacon"));
        assertTrue(hasCategory(ScannerHarness.run(new BeaconScanner(), w), FindingCategory.BEACON));
    }

    @Test
    void terrainAnomalyDetectsPlayerBlocksAndGeometry() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        for (int i = 0; i < 8; i++) {
            w.setBlock(2 + i, 70, 2, "minecraft:oak_planks");
        }
        List<Finding> fs = ScannerHarness.run(new TerrainAnomalyScanner(), w);
        assertTrue(hasCategory(fs, FindingCategory.TERRAIN_ANOMALY));
    }

    @Test
    void illegalScannerFindsCommandBlockAndMisplacedBedrock() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .setBlock(1, 70, 1, "minecraft:command_block")
                .setBlock(3, 72, 3, "minecraft:bedrock");
        List<Finding> fs = ScannerHarness.run(new IllegalBlockScanner(), w);
        assertTrue(fs.size() >= 2, "command block + misplaced bedrock");
    }

    @Test
    void lidarFindsUndergroundLights() {
        FakeWorld w = new FakeWorld().player(0, 50, 0).bounds(40, 60);
        for (int i = 0; i < 4; i++) {
            w.setBlock(2 + i, 50, 2, "minecraft:torch");
        }
        assertTrue(hasCategory(ScannerHarness.run(new LidarLightScanner(), w), FindingCategory.LIGHT));
    }

    @Test
    void amethystScannerFindsGeode() {
        FakeWorld w = new FakeWorld().player(0, 60, 0).bounds(50, 70)
                .setBlock(2, 60, 2, "minecraft:budding_amethyst");
        assertTrue(hasCategory(ScannerHarness.run(new AmethystScanner(), w), FindingCategory.AMETHYST));
    }

    @Test
    void portalScannerFindsNetherPortal() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .fill(2, 68, 2, 2, 71, 2, "minecraft:nether_portal");
        assertTrue(hasCategory(ScannerHarness.run(new PortalScanner(), w), FindingCategory.PORTAL));
    }

    @Test
    void bedScannerFindsRespawnPoint() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .setBlock(2, 70, 2, "minecraft:red_bed");
        assertTrue(hasCategory(ScannerHarness.run(new BedScanner(), w), FindingCategory.BED));
    }

    @Test
    void redstoneScannerFindsContraption() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .setBlock(2, 70, 2, "minecraft:hopper")
                .setBlock(3, 70, 2, "minecraft:piston")
                .setBlock(2, 70, 3, "minecraft:observer")
                .setBlock(3, 70, 3, "minecraft:comparator");
        assertTrue(hasCategory(ScannerHarness.run(new RedstoneScanner(), w), FindingCategory.REDSTONE));
    }

    @Test
    void highwayScannerFindsIceRoad() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).dimension(Dimension.NETHER);
        for (int x = 2; x <= 16; x++) {
            w.setBlock(x, 70, 4, "minecraft:blue_ice");
        }
        assertTrue(hasCategory(ScannerHarness.run(new NetherHighwayScanner(), w), FindingCategory.HIGHWAY));
    }

    @Test
    void breadcrumbScannerFindsRailLine() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        for (int x = 2; x <= 12; x++) {
            w.setBlock(x, 70, 5, "minecraft:rail");
        }
        assertTrue(hasCategory(ScannerHarness.run(new BreadcrumbScanner(), w), FindingCategory.PATH));
    }

    @Test
    void disabledDetectorProducesNothing() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        w.setBlock(2, 70, 2, "minecraft:red_bed");
        List<Finding> fs = ScannerHarness.run(new TerrainAnomalyScanner(), w);
        assertFalse(hasCategory(fs, FindingCategory.TERRAIN_ANOMALY), "a lone bed is not a terrain anomaly");
    }
}
