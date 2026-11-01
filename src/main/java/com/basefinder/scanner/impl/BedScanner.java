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

/** Finds beds and respawn anchors &mdash; a strong player-presence signal. */
public final class BedScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.8;

    private final List<Vec3i> beds = new ArrayList<>();
    private long tick;

    public BedScanner() {
        super("beds", "Bed / Respawn Scanner",
                "Detects beds and respawn anchors, which indicate player spawn points.",
                FindingCategory.BED);
    }

    @Override
    public void begin(ScanContext ctx) {
        beds.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isRespawnPoint(id)) {
            beds.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        // A bed is two block halves; cluster tightly to one finding each.
        for (Clustering.Cluster c : Clustering.cluster(beds, 2)) {
            sink.emit(Finding.builder(id(), FindingCategory.BED, c.center())
                    .confidence(100)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.HIGH)
                    .label("Bed / respawn point")
                    .source("Block scan")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
