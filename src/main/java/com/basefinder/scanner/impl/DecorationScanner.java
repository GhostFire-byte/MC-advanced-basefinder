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

/**
 * Detects decorated interiors: clusters of item frames (sorting/map walls),
 * paintings, banners, flower pots, armor stands, bookshelves and lecterns.
 * Village-explainable clusters are down-weighted by the fusion stage.
 */
public final class DecorationScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 8;
    private static final int MIN_DECOR = 4;

    private final List<Vec3i> decor = new ArrayList<>();
    private long tick;

    public DecorationScanner() {
        super("decoration", "Decoration Density Scanner",
                "Detects decorated/inhabited interiors: item frames, paintings, banners, flower pots and more.",
                FindingCategory.DECORATION);
    }

    @Override
    public void begin(ScanContext ctx) {
        decor.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isDecoration(id)) {
            decor.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(decor, CLUSTER_RADIUS)) {
            if (c.count() < MIN_DECOR) {
                continue;
            }
            int conf = Math.min(100, 45 + c.count() * 5);
            sink.emit(Finding.builder(id(), FindingCategory.DECORATION, c.center())
                    .confidence(conf)
                    .signalWeight(0.5)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.LOW)
                    .label("Decoration cluster (" + c.count() + ")")
                    .source("Decorative blocks")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
