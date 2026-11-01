package com.basefinder;

import com.basefinder.engine.BlockEventType;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.scanner.impl.AccessShaftScanner;
import com.basefinder.scanner.impl.BranchMineScanner;
import com.basefinder.scanner.impl.DecorationScanner;
import com.basefinder.scanner.impl.FlatPlatformScanner;
import com.basefinder.scanner.impl.FoodFarmScanner;
import com.basefinder.scanner.impl.HiddenEntranceScanner;
import com.basefinder.scanner.impl.HollowShellScanner;
import com.basefinder.scanner.impl.MarkerScanner;
import com.basefinder.scanner.impl.NetherRoofScanner;
import com.basefinder.scanner.impl.SecurityScanner;
import com.basefinder.scanner.impl.SpawnProofScanner;
import com.basefinder.scanner.impl.StashActivityScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.Dimension;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedScannersTest {

    private boolean has(List<Finding> fs, FindingCategory c) {
        return fs.stream().anyMatch(f -> f.category() == c);
    }

    @Test
    void markerScannerFindsLodestone() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).setBlock(3, 70, 3, "minecraft:lodestone");
        assertTrue(has(ScannerHarness.run(new MarkerScanner(), w), FindingCategory.MARKER));
    }

    @Test
    void spawnProofFindsUndergroundSlabs() {
        FakeWorld w = new FakeWorld().player(0, 30, 0).bounds(20, 40);
        for (int i = 0; i < 6; i++) {
            w.setBlock(2 + i, 30, 2, "minecraft:stone_slab");
        }
        assertTrue(has(ScannerHarness.run(new SpawnProofScanner(), w), FindingCategory.SPAWN_PROOF));
    }

    @Test
    void spawnProofIgnoresSurfaceSlabs() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        for (int i = 0; i < 6; i++) {
            w.setBlock(2 + i, 70, 2, "minecraft:stone_slab");
        }
        assertFalse(has(ScannerHarness.run(new SpawnProofScanner(), w), FindingCategory.SPAWN_PROOF));
    }

    @Test
    void netherRoofFindsConstructionAndGoldFarm() {
        FakeWorld w = new FakeWorld().player(0, 130, 0).bounds(120, 140).dimension(Dimension.NETHER);
        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                w.setBlock(x, 130, z, "minecraft:magma_block");
                w.setBlock(x, 131, z, "minecraft:oak_trapdoor");
            }
        }
        List<Finding> fs = ScannerHarness.run(new NetherRoofScanner(), w);
        assertTrue(has(fs, FindingCategory.NETHER_ROOF));
        assertTrue(fs.stream().anyMatch(f -> f.label().toLowerCase().contains("gold farm")));
    }

    @Test
    void netherRoofInactiveInOverworld() {
        FakeWorld w = new FakeWorld().player(0, 130, 0).bounds(120, 140).dimension(Dimension.OVERWORLD);
        w.setBlock(2, 130, 2, "minecraft:cobblestone");
        assertFalse(has(ScannerHarness.run(new NetherRoofScanner(), w), FindingCategory.NETHER_ROOF));
    }

    @Test
    void securityFindsSurfaceSculkSensor() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .setBlock(2, 70, 2, "minecraft:sculk_sensor")
                .setBlock(3, 70, 2, "minecraft:tripwire");
        assertTrue(has(ScannerHarness.run(new SecurityScanner(), w), FindingCategory.SECURITY));
    }

    @Test
    void decorationFindsItemFrameWall() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        for (int i = 0; i < 6; i++) {
            w.setBlock(2 + i, 70, 2, "minecraft:item_frame");
        }
        assertTrue(has(ScannerHarness.run(new DecorationScanner(), w), FindingCategory.DECORATION));
    }

    @Test
    void foodFarmFindsCropField() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 3; z++) {
                w.setBlock(x, 70, z, "minecraft:farmland");
                w.setBlock(x, 71, z, "minecraft:wheat");
            }
        }
        assertTrue(has(ScannerHarness.run(new FoodFarmScanner(), w), FindingCategory.FARM));
    }

    @Test
    void accessShaftFindsLadderColumn() {
        FakeWorld w = new FakeWorld().player(0, 60, 0).bounds(0, 80);
        for (int y = 20; y <= 60; y++) {
            w.setBlock(5, y, 5, "minecraft:ladder");
        }
        assertTrue(has(ScannerHarness.run(new AccessShaftScanner(), w), FindingCategory.ACCESS_SHAFT));
    }

    @Test
    void flatPlatformFindsLargePlane() {
        FakeWorld w = new FakeWorld().player(0, 120, 0).bounds(100, 140);
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                w.setBlock(x, 120, z, "minecraft:smooth_stone");
            }
        }
        assertTrue(has(ScannerHarness.run(new FlatPlatformScanner(), w), FindingCategory.PLATFORM));
    }

    @Test
    void branchMineFindsStraightTunnel() {
        FakeWorld w = new FakeWorld().player(0, 12, 0).bounds(0, 40);
        // Carve a 1-wide, 2-tall tunnel in solid stone from x=2..18 at y=12, z=5.
        w.fill(0, 10, 0, 25, 15, 10, "minecraft:stone");
        for (int x = 2; x <= 18; x++) {
            w.setBlock(x, 12, 5, "minecraft:air");
            w.setBlock(x, 13, 5, "minecraft:air");
        }
        assertTrue(has(ScannerHarness.run(new BranchMineScanner(), w), FindingCategory.MINING_AREA));
    }

    @Test
    void hollowShellFindsCarvedBaseButNotBareCave() {
        // Base: large air pocket lined with player blocks.
        FakeWorld base = new FakeWorld().player(0, 30, 0).bounds(0, 50);
        for (int x = 0; x < 6; x++) {
            for (int y = 28; y < 34; y++) {
                for (int z = 0; z < 6; z++) {
                    base.setBlock(x, y, z, "minecraft:air");
                }
            }
        }
        for (int x = 0; x < 10; x++) {
            base.setBlock(x, 27, 0, "minecraft:stone_bricks");
            base.setBlock(x, 34, 0, "minecraft:oak_planks");
        }
        assertTrue(has(ScannerHarness.run(new HollowShellScanner(), base), FindingCategory.HOLLOW_SHELL));

        // Bare cave: same air, no player blocks -> not flagged.
        FakeWorld cave = new FakeWorld().player(0, 30, 0).bounds(0, 50);
        for (int x = 0; x < 6; x++) {
            for (int y = 28; y < 34; y++) {
                for (int z = 0; z < 6; z++) {
                    cave.setBlock(x, y, z, "minecraft:air");
                }
            }
        }
        assertFalse(has(ScannerHarness.run(new HollowShellScanner(), cave), FindingCategory.HOLLOW_SHELL));
    }

    @Test
    void hiddenEntranceFindsPistonDoor() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80)
                .setBlock(2, 70, 2, "minecraft:sticky_piston")
                .setBlock(3, 70, 2, "minecraft:sticky_piston")
                .setBlock(2, 71, 2, "minecraft:iron_trapdoor");
        assertTrue(has(ScannerHarness.run(new HiddenEntranceScanner(), w), FindingCategory.HIDDEN_ENTRANCE));
    }

    @Test
    void stashActivityFlagsContainerOpenWithNoPlayer() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100);
        StashActivityScanner scanner = new StashActivityScanner();
        scanner.onBlockEvent(BlockEventType.CONTAINER_OPEN, new Vec3i(5, 70, 5), "minecraft:chest", 100);
        assertTrue(has(ScannerHarness.run(scanner, w), FindingCategory.STASH_ACTIVITY));
    }

    @Test
    void stashActivitySuppressedWhenPlayerNearby() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80).tick(100)
                .addEntity(new EntityRef(1, "minecraft:player", new Vec3d(5, 70, 6)));
        StashActivityScanner scanner = new StashActivityScanner();
        scanner.onBlockEvent(BlockEventType.CONTAINER_OPEN, new Vec3i(5, 70, 5), "minecraft:chest", 100);
        assertFalse(has(ScannerHarness.run(scanner, w), FindingCategory.STASH_ACTIVITY));
    }
}
