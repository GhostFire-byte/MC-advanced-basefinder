package com.basefinder.engine;

import com.basefinder.world.BlockRef;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3i;
import com.basefinder.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Cheap per-cycle inference of where villages are, so village-explainable findings
 * (food farms, decoration, beds) aren't mistaken for a player base. A village is
 * inferred from villager/iron-golem clusters and bells. {@link #isVillage} tests
 * whether a position falls within an inferred village radius.
 *
 * <p>This is a heuristic (a normal client has no authoritative village bounds),
 * kept deliberately conservative: it only suppresses, never fabricates findings.
 */
public final class VillageContext {

    private static final int VILLAGER_CLUSTER_RADIUS = 48;
    private static final int MIN_VILLAGERS = 2;
    private static final int VILLAGE_RADIUS = 64;

    private final List<Vec3i> centers = new ArrayList<>();

    private VillageContext() {
    }

    public static VillageContext from(WorldSnapshot world) {
        VillageContext ctx = new VillageContext();
        List<Vec3i> villagers = new ArrayList<>();
        for (EntityRef e : world.entities()) {
            String t = e.type();
            if (t.equals("minecraft:villager") || t.equals("minecraft:iron_golem")
                    || t.equals("minecraft:wandering_trader")) {
                villagers.add(e.pos().toBlock());
            }
        }
        for (BlockRef be : world.blockEntities()) {
            if (be.id().equals("minecraft:bell")) {
                villagers.add(be.pos());
            }
        }
        ctx.buildCenters(villagers);
        return ctx;
    }

    public static VillageContext empty() {
        return new VillageContext();
    }

    private void buildCenters(List<Vec3i> pts) {
        boolean[] used = new boolean[pts.size()];
        long r2 = (long) VILLAGER_CLUSTER_RADIUS * VILLAGER_CLUSTER_RADIUS;
        for (int i = 0; i < pts.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<Vec3i> group = new ArrayList<>();
            Vec3i a = pts.get(i);
            for (int j = 0; j < pts.size(); j++) {
                if (!used[j] && a.distanceSq(pts.get(j)) <= r2) {
                    group.add(pts.get(j));
                    used[j] = true;
                }
            }
            if (group.size() >= MIN_VILLAGERS) {
                long x = 0, y = 0, z = 0;
                for (Vec3i p : group) {
                    x += p.x();
                    y += p.y();
                    z += p.z();
                }
                int n = group.size();
                centers.add(new Vec3i((int) (x / n), (int) (y / n), (int) (z / n)));
            }
        }
    }

    public boolean isVillage(Vec3i pos) {
        long r2 = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;
        for (Vec3i c : centers) {
            long dx = pos.x() - c.x(), dz = pos.z() - c.z();
            if (dx * dx + dz * dz <= r2) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyVillage() {
        return !centers.isEmpty();
    }

    public List<Vec3i> centers() {
        return centers;
    }
}
