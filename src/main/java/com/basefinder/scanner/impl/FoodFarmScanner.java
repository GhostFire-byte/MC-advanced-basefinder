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
 * Detects food farms: tilled farmland, crops and composters clustered together.
 * Village farms are down-weighted by the fusion stage (via VillageContext).
 */
public final class FoodFarmScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 10;
    private static final int MIN_FARM = 8;

    private final List<Vec3i> farm = new ArrayList<>();
    private long tick;

    public FoodFarmScanner() {
        super("food_farm", "Food Farm Scanner",
                "Detects farmland, crops and composters grouped as a food farm (village farms are down-weighted).",
                FindingCategory.FARM);
    }

    @Override
    public void begin(ScanContext ctx) {
        farm.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isFarmBlock(id)) {
            farm.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(farm, CLUSTER_RADIUS)) {
            if (c.count() < MIN_FARM) {
                continue;
            }
            int conf = Math.min(100, 40 + c.count() * 3);
            sink.emit(Finding.builder(id(), FindingCategory.FARM, c.center())
                    .confidence(conf)
                    .signalWeight(0.45)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.LOW)
                    .label("Food farm (" + c.count() + " blocks)")
                    .source("Farm blocks")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
