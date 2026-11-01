package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight, honest structure heuristic from loaded entities: clusters of
 * villagers/iron golems suggest a village, piglin clusters a bastion, etc. This
 * is intentionally conservative and reported as a heuristic observation, since
 * reliable structure bounds are not available to a normal client.
 */
public final class StructureScanner extends AbstractScanner {

    private static final double WEIGHT = 0.2;
    private static final int CLUSTER_RADIUS = 24;
    private static final int MIN_VILLAGERS = 3;

    public StructureScanner() {
        super("structure", "Structure Scanner",
                "Infers nearby structures (villages, outposts, bastions) from characteristic entities in loaded chunks.",
                FindingCategory.STRUCTURE);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        List<Vec3i> villagers = new ArrayList<>();
        List<Vec3i> piglins = new ArrayList<>();
        for (EntityRef e : ctx.world().entities()) {
            Vec3i p = e.pos().toBlock();
            if (!ctx.scope().contains(p)) {
                continue;
            }
            String t = e.type();
            if (t.equals("minecraft:villager") || t.equals("minecraft:iron_golem")) {
                villagers.add(p);
            } else if (t.contains("piglin")) {
                piglins.add(p);
            }
        }
        emitCluster(ctx, villagers, MIN_VILLAGERS, "Village", sink);
        emitCluster(ctx, piglins, 3, "Bastion / piglin camp", sink);
    }

    private void emitCluster(ScanContext ctx, List<Vec3i> pts, int min, String label, FindingSink sink) {
        if (pts.size() < min) {
            return;
        }
        Vec3i c = ScannerText.centroid(pts);
        long spread = 0;
        for (Vec3i p : pts) {
            spread = Math.max(spread, c.distanceSq(p));
        }
        if (spread > (long) CLUSTER_RADIUS * CLUSTER_RADIUS * 4) {
            return; // too dispersed to be one structure
        }
        int conf = Math.min(85, 40 + pts.size() * 6);
        sink.emit(Finding.builder(id(), FindingCategory.STRUCTURE, c)
                .confidence(conf)
                .signalWeight(WEIGHT)
                .verification(VerificationStatus.HEURISTIC)
                .severity(Severity.LOW)
                .label(label + " (" + pts.size() + ")")
                .source("Entity cluster")
                .observedAtTick(ctx.tick())
                .build());
    }
}
