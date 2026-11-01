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
 * Detects unnaturally flat platforms: a large single-material horizontal plane at
 * one Y level. Dynamic &mdash; the bigger the plane, the higher the confidence.
 * Natural flat ground is filtered out by requiring either a built material or an
 * off-surface Y (deep underground or high in the sky).
 */
public final class FlatPlatformScanner extends AbstractBlockScanner {

    private static final int MIN_AREA = 24;

    // key = (y, blockId-hash) -> count + bounding box
    private final Map<Long, Plane> planes = new HashMap<>();
    private long tick;

    public FlatPlatformScanner() {
        super("platform", "Flat Platform Scanner",
                "Detects large unnaturally flat single-material planes (build platforms, farm floors, landing pads).",
                FindingCategory.PLATFORM);
    }

    @Override
    public void begin(ScanContext ctx) {
        planes.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        boolean built = Blocks.isRefinedPlayerBlock(id);
        boolean offSurface = y < 55 || y > 100;
        if (!built && !offSurface) {
            return; // ignore natural surface ground
        }
        long key = ((long) y << 32) ^ (id.hashCode() & 0xffffffffL);
        Plane p = planes.computeIfAbsent(key, k -> new Plane(y, id));
        p.add(x, z);
    }

    @Override
    public void finish(FindingSink sink) {
        for (Plane p : planes.values()) {
            if (p.count < MIN_AREA) {
                continue;
            }
            // Require it to actually be plane-like (area fills a good part of its bounding box).
            int bbox = (p.maxX - p.minX + 1) * (p.maxZ - p.minZ + 1);
            if (bbox <= 0 || p.count < bbox * 0.5) {
                continue;
            }
            int conf = Math.min(100, 40 + p.count); // dynamic: bigger = higher
            Vec3i center = new Vec3i((p.minX + p.maxX) / 2, p.y, (p.minZ + p.maxZ) / 2);
            sink.emit(Finding.builder(id(), FindingCategory.PLATFORM, center)
                    .confidence(conf)
                    .signalWeight(0.4)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.LOW)
                    .label("Flat platform (" + p.count + " " + ScannerText.pretty(p.id) + ")")
                    .source("Flat plane")
                    .observedAtTick(tick)
                    .build());
        }
    }

    private static final class Plane {
        final int y;
        final String id;
        int count;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        Plane(int y, String id) {
            this.y = y;
            this.id = id;
        }

        void add(int x, int z) {
            count++;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }
    }
}
