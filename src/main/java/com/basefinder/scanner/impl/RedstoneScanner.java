package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractBlockScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.Blocks;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/** Finds redstone contraptions/farms by clustering redstone components. */
public final class RedstoneScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.6;
    private static final int CLUSTER_RADIUS = 8;
    private static final int MIN_COMPONENTS = 4;

    private final List<Vec3i> parts = new ArrayList<>();
    private long tick;

    public RedstoneScanner() {
        super("redstone", "Redstone / Farm Scanner",
                "Detects redstone contraptions and farms by clustering hoppers, pistons, observers, comparators and wiring.",
                FindingCategory.REDSTONE);
    }

    @Override
    public void begin(ScanContext ctx) {
        parts.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.REDSTONE.contains(id)) {
            parts.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(parts, CLUSTER_RADIUS)) {
            if (c.count() < MIN_COMPONENTS) {
                continue;
            }
            int conf = Math.min(100, 45 + c.count() * 4);
            sink.emit(Finding.builder(id(), FindingCategory.REDSTONE, c.center())
                    .confidence(conf)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("Redstone contraption (" + c.count() + ")")
                    .source("Block scan")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
