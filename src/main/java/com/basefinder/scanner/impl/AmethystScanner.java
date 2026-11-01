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

/** Finds amethyst geodes (budding amethyst and clusters), grouped per geode. */
public final class AmethystScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.12;
    private static final int GEODE_RADIUS = 10;

    private final List<Vec3i> budding = new ArrayList<>();
    private long tick;

    public AmethystScanner() {
        super("amethyst", "Amethyst / Geode Scanner",
                "Detects budding amethyst and amethyst clusters (geodes) in loaded chunks.",
                FindingCategory.AMETHYST);
    }

    @Override
    public void begin(ScanContext ctx) {
        budding.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.AMETHYST.contains(id)) {
            budding.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(budding, GEODE_RADIUS)) {
            int conf = Math.min(100, 70 + c.count() * 2);
            sink.emit(Finding.builder(id(), FindingCategory.AMETHYST, c.center())
                    .confidence(conf)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.LOW)
                    .label("Amethyst geode (" + c.count() + ")")
                    .source("Block scan")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
