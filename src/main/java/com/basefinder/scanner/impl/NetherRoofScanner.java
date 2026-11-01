package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractBlockScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.Blocks;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects construction on or above the Nether bedrock roof (a very common place
 * for hidden bases, portals and gold farms). Any non-bedrock solid block at/above
 * the roof Y is a signal; gold-farm material signatures raise severity and are
 * explicitly flagged. The roof Y is adjustable (portals/gold farms sit on it).
 */
public final class NetherRoofScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 10;

    private final List<Vec3i> roofBlocks = new ArrayList<>();
    private int goldFarmHits;
    private int roofY;
    private boolean active;
    private long tick;

    public NetherRoofScanner() {
        super("nether_roof", "Nether Roof Scanner",
                "Detects construction on/above the Nether bedrock roof: bases, roof portals and gold farms.",
                FindingCategory.NETHER_ROOF);
    }

    @Override
    public void begin(ScanContext ctx) {
        roofBlocks.clear();
        goldFarmHits = 0;
        tick = ctx.tick();
        active = ctx.world().dimension() == Dimension.NETHER;
        roofY = ctx.settings().paramInt("roof.y", 127);
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (!active || y < roofY) {
            return;
        }
        if (id.equals("minecraft:bedrock") || id.equals("minecraft:air")) {
            return;
        }
        roofBlocks.add(new Vec3i(x, y, z));
        if (Blocks.isGoldFarmMaterial(id)) {
            goldFarmHits++;
        }
    }

    @Override
    public void finish(FindingSink sink) {
        if (!active) {
            return;
        }
        for (Clustering.Cluster c : Clustering.cluster(roofBlocks, CLUSTER_RADIUS)) {
            if (c.count() < 3) {
                continue;
            }
            boolean goldFarm = goldFarmHits >= 8 && c.count() >= 20;
            int conf = Math.min(100, 55 + c.count() * 2 + (goldFarm ? 15 : 0));
            sink.emit(Finding.builder(id(), FindingCategory.NETHER_ROOF, c.center())
                    .confidence(conf)
                    .signalWeight(0.8)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(goldFarm ? Severity.HIGH : Severity.MEDIUM)
                    .label(goldFarm ? "Nether-roof gold farm" : "Nether-roof construction (" + c.count() + ")")
                    .source("Roof blocks")
                    .observedAtTick(tick)
                    .meta("goldFarm", String.valueOf(goldFarm))
                    .build());
        }
    }
}
