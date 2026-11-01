package com.basefinder.test;

import com.basefinder.config.GlobalSettings;
import com.basefinder.config.SubsystemSettings;
import com.basefinder.model.Finding;
import com.basefinder.scanner.BlockVisitor;
import com.basefinder.scanner.ScanBudget;
import com.basefinder.scanner.ScanContext;
import com.basefinder.scanner.Scanner;
import com.basefinder.world.BlockAccess;
import com.basefinder.world.ScanScope;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/** Drives a single scanner over a FakeWorld and returns its findings. */
public final class ScannerHarness {

    private ScannerHarness() {
    }

    public static List<Finding> run(Scanner scanner, FakeWorld world) {
        Vec3i p = world.playerPos().toBlock();
        BlockAccess ba = world.blocks();
        ScanScope scope = new ScanScope(p.chunkX(), p.chunkZ(), 3, ba.minY(), ba.maxY());
        return run(scanner, world, scope, new SubsystemSettings(), new GlobalSettings());
    }

    public static List<Finding> run(Scanner scanner, FakeWorld world, ScanScope scope,
                                    SubsystemSettings sub, GlobalSettings global) {
        List<Finding> findings = new ArrayList<>();
        ScanContext ctx = new ScanContext(world, scope, global, sub, world.gameTime(),
                ScanBudget.unlimited(), com.basefinder.engine.VillageContext.from(world));
        if (scanner instanceof BlockVisitor visitor) {
            visitor.begin(ctx);
            BlockAccess ba = world.blocks();
            int minY = Math.max(scope.minY(), ba.minY());
            int maxY = Math.min(scope.maxY(), ba.maxY());
            for (int y = minY; y <= maxY; y++) {
                for (int x = scope.blockMinX(); x <= scope.blockMaxX(); x++) {
                    for (int z = scope.blockMinZ(); z <= scope.blockMaxZ(); z++) {
                        if (!ba.isLoaded(x, y, z)) {
                            continue;
                        }
                        String id = ba.blockId(x, y, z);
                        boolean air = id == null || id.endsWith(":air") || id.endsWith("_air");
                        if (air && !visitor.wantsAir()) {
                            continue;
                        }
                        visitor.visitBlock(x, y, z, air ? "minecraft:air" : id, ba.lightLevel(x, y, z));
                    }
                }
            }
            visitor.finish(findings::add);
        } else {
            scanner.scan(ctx, findings::add);
        }
        return findings;
    }
}
