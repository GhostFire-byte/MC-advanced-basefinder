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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects vertical access shafts (ladders, scaffolding, vines, water/soul-sand
 * elevators) &mdash; a column of access blocks spanning many Y levels is a player
 * elevator/mineshaft that usually leads to a base.
 */
public final class AccessShaftScanner extends AbstractBlockScanner {

    private static final int MIN_SPAN = 8;

    private final Map<Long, int[]> columns = new HashMap<>(); // key -> {minY, maxY, count}
    private long tick;

    public AccessShaftScanner() {
        super("access_shaft", "Access Shaft Scanner",
                "Detects vertical ladder/scaffolding/elevator columns that span many Y levels.",
                FindingCategory.ACCESS_SHAFT);
    }

    @Override
    public void begin(ScanContext ctx) {
        columns.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (!Blocks.isAccessBlock(id)) {
            return;
        }
        long key = ((long) (x & 0x3FFFFFFF) << 30) | (z & 0x3FFFFFFF);
        int[] col = columns.computeIfAbsent(key, k -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0});
        col[0] = Math.min(col[0], y);
        col[1] = Math.max(col[1], y);
        col[2]++;
    }

    @Override
    public void finish(FindingSink sink) {
        for (Map.Entry<Long, int[]> e : columns.entrySet()) {
            int[] col = e.getValue();
            int span = col[1] - col[0];
            if (span < MIN_SPAN || col[2] < MIN_SPAN) {
                continue;
            }
            long key = e.getKey();
            int x = (int) ((key >> 30) & 0x3FFFFFFF);
            int z = (int) (key & 0x3FFFFFFF);
            x = signExtend30(x);
            z = signExtend30(z);
            Vec3i center = new Vec3i(x, (col[0] + col[1]) / 2, z);
            int conf = Math.min(100, 45 + span * 2);
            sink.emit(Finding.builder(id(), FindingCategory.ACCESS_SHAFT, center)
                    .confidence(conf)
                    .signalWeight(0.4)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.LOW)
                    .label("Access shaft (" + span + " tall)")
                    .source("Vertical access")
                    .observedAtTick(tick)
                    .build());
        }
    }

    private static int signExtend30(int v) {
        return (v & 0x20000000) != 0 ? v | ~0x3FFFFFFF : v;
    }
}
