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

/** Finds nether/end portals and end gateways, grouped per portal. */
public final class PortalScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.55;
    private static final int PORTAL_RADIUS = 6;

    private final List<Vec3i> portals = new ArrayList<>();
    private long tick;

    public PortalScanner() {
        super("portal", "Portal Scanner",
                "Detects nether portals, end portals and end gateways in loaded chunks.",
                FindingCategory.PORTAL);
    }

    @Override
    public void begin(ScanContext ctx) {
        portals.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.PORTAL.contains(id)) {
            portals.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(portals, PORTAL_RADIUS)) {
            sink.emit(Finding.builder(id(), FindingCategory.PORTAL, c.center())
                    .confidence(100)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.MEDIUM)
                    .label("Portal")
                    .source("Block scan")
                    .detail("Portal blocks: " + c.count())
                    .observedAtTick(tick)
                    .build());
        }
    }
}
