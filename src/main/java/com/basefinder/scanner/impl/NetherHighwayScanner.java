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

/** Detects nether highways: long straight ice roads (and packed/blue ice lines). */
public final class NetherHighwayScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.6;
    private static final int MIN_RUN = 12;

    private final List<Vec3i> ice = new ArrayList<>();
    private long tick;

    public NetherHighwayScanner() {
        super("highway", "Nether Highway Scanner",
                "Detects long straight ice roads (nether highways) in loaded chunks.",
                FindingCategory.HIGHWAY);
    }

    @Override
    public void begin(ScanContext ctx) {
        ice.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.ICE_ROAD.contains(id)) {
            ice.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        if (ice.size() < MIN_RUN) {
            return;
        }
        int run = LineRuns.longestRun(ice);
        if (run < MIN_RUN) {
            return;
        }
        Vec3i c = ScannerText.centroid(ice);
        int conf = Math.min(100, 50 + run * 2);
        sink.emit(Finding.builder(id(), FindingCategory.HIGHWAY, c)
                .confidence(conf)
                .signalWeight(WEIGHT)
                .verification(VerificationStatus.HEURISTIC)
                .severity(Severity.MEDIUM)
                .label("Ice highway (" + run + " long)")
                .source("Block scan")
                .detail("Straight ice run of " + run + " blocks")
                .observedAtTick(tick)
                .build());
    }
}
