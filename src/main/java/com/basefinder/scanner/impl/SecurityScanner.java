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
 * Detects base-security / alarm systems: sculk sensors (outside the deep dark),
 * calibrated sculk sensors, tripwire and target blocks. Perimeter observers are
 * counted as corroboration. Natural deep-dark sculk is not flagged (sculk sensors
 * above y=0 are treated as player-placed, since the deep dark sits below y~0).
 */
public final class SecurityScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 10;

    private final List<Vec3i> security = new ArrayList<>();
    private long tick;

    public SecurityScanner() {
        super("security", "Security Block Scanner",
                "Detects player alarm/trap systems: sculk sensors outside the deep dark, tripwire and target blocks.",
                FindingCategory.SECURITY);
    }

    @Override
    public void begin(ScanContext ctx) {
        security.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        boolean suspicious;
        if (id.equals("minecraft:sculk_sensor") || id.equals("minecraft:calibrated_sculk_sensor")) {
            // Deep dark generates roughly below y=0; above that a sensor is player-placed.
            suspicious = y >= 0;
        } else {
            suspicious = Blocks.SECURITY.contains(id) && !id.equals("minecraft:observer");
        }
        if (suspicious) {
            security.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(security, CLUSTER_RADIUS)) {
            int conf = Math.min(100, 55 + c.count() * 6);
            sink.emit(Finding.builder(id(), FindingCategory.SECURITY, c.center())
                    .confidence(conf)
                    .signalWeight(0.7)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("Security system (" + c.count() + ")")
                    .source("Security blocks")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
