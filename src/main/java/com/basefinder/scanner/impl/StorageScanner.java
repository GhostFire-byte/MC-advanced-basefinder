package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.BlockAccess;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Blocks;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds storage/utility block entities (chests, barrels, shulkers, hoppers,
 * furnaces, enchanting tables, anvils, ...). These are verified detections. Also
 * applies stash-cluster analysis: a dense pack of containers emits an extra,
 * stronger "stash" signal toward the base-candidate fusion.
 */
public final class StorageScanner extends AbstractScanner {

    private static final double CONTAINER_WEIGHT = 0.45;
    private static final double STASH_WEIGHT = 0.9;
    private static final int STASH_RADIUS = 6;
    private static final int STASH_MIN_COUNT = 5;

    public StorageScanner() {
        super("storage", "Storage Scanner",
                "Detects chests, barrels, shulker boxes, hoppers, furnaces, enchanting tables and more. "
                        + "Flags dense container clusters as likely stashes.",
                FindingCategory.STORAGE);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        List<Vec3i> containers = new ArrayList<>();
        for (BlockRef be : ctx.world().blockEntities()) {
            if (!ctx.scope().containsBlockEntity(be)) {
                continue;
            }
            if (Blocks.isStorage(be.id())) {
                containers.add(be.pos());
                boolean surface = surfaceExposed(ctx.world().blocks(), be.pos());
                sink.emit(Finding.builder(id(), FindingCategory.STORAGE, be.pos())
                        .confidence(100)
                        .signalWeight(CONTAINER_WEIGHT)
                        .verification(VerificationStatus.VERIFIED)
                        .severity(Severity.LOW)
                        .label(ScannerText.pretty(be.id()))
                        .source("Block entity")
                        .detail(be.id() + " at " + be.pos())
                        .observedAtTick(ctx.tick())
                        .meta("block", be.id())
                        .meta("surface", String.valueOf(surface))
                        .build());
            }
        }
        emitStashClusters(ctx, containers, sink);
    }

    private void emitStashClusters(ScanContext ctx, List<Vec3i> containers, FindingSink sink) {
        boolean[] used = new boolean[containers.size()];
        for (int i = 0; i < containers.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<Vec3i> cluster = new ArrayList<>();
            Vec3i a = containers.get(i);
            for (int j = 0; j < containers.size(); j++) {
                if (a.distanceSq(containers.get(j)) <= (long) STASH_RADIUS * STASH_RADIUS) {
                    cluster.add(containers.get(j));
                    used[j] = true;
                }
            }
            if (cluster.size() >= STASH_MIN_COUNT) {
                Vec3i c = ScannerText.centroid(cluster);
                int conf = Math.min(100, 60 + cluster.size() * 4);
                sink.emit(Finding.builder(id(), FindingCategory.STORAGE, c)
                        .confidence(conf)
                        .signalWeight(STASH_WEIGHT)
                        .verification(VerificationStatus.HEURISTIC)
                        .severity(Severity.HIGH)
                        .label("Stash (" + cluster.size() + " containers)")
                        .source("Container density")
                        .detail(cluster.size() + " containers within " + STASH_RADIUS + " blocks")
                        .observedAtTick(ctx.tick())
                        .meta("count", String.valueOf(cluster.size()))
                        .build());
            }
        }
    }

    /**
     * True if the container is open to the sky (only air above it up to the top
     * of the loaded world). Used to tell transient surface drops left by
     * explorers from buried/hidden storage. Unloaded space above reads as air.
     */
    private static boolean surfaceExposed(BlockAccess blocks, Vec3i p) {
        int top = blocks.maxY();
        for (int y = p.y() + 1; y < top; y++) {
            if (!blocks.isAir(p.x(), y, p.z())) {
                return false;
            }
        }
        return true;
    }
}
