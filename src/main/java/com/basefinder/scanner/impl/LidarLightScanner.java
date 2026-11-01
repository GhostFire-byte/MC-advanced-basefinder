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
 * LiDAR (light detection and ranging). Flags player-placed light sources
 * (torches, lanterns, sea lanterns, glowstone, ...) &mdash; especially
 * underground where terrain is naturally dark &mdash; as a human-activity signal.
 * Natural light (lava) is excluded.
 */
public final class LidarLightScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.5;
    private static final int CLUSTER_RADIUS = 8;
    private static final int UNDERGROUND_Y = 58;

    private final List<Vec3i> lights = new ArrayList<>();
    private long tick;

    public LidarLightScanner() {
        super("lidar", "LiDAR Light Scanner",
                "Detects player light sources (torches, lanterns, sea lanterns, glowstone) in unnaturally dark places.",
                FindingCategory.LIGHT);
    }

    @Override
    public void begin(ScanContext ctx) {
        lights.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.LIGHT_SOURCES.contains(id) && !id.equals("minecraft:lava")
                && !id.equals("minecraft:campfire") && !id.equals("minecraft:soul_campfire")) {
            lights.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(lights, CLUSTER_RADIUS)) {
            boolean underground = c.center().y() < UNDERGROUND_Y;
            int conf = Math.min(100, (underground ? 65 : 45) + c.count() * 3);
            sink.emit(Finding.builder(id(), FindingCategory.LIGHT, c.center())
                    .confidence(conf)
                    .signalWeight(underground ? WEIGHT + 0.1 : WEIGHT)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(underground ? Severity.MEDIUM : Severity.LOW)
                    .label("Light cluster (" + c.count() + (underground ? ", underground)" : ")"))
                    .source("Light scan")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
