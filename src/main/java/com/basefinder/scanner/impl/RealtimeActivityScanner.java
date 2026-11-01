package com.basefinder.scanner.impl;

import com.basefinder.engine.BlockChangeListener;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.Vec3i;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Real-time activity detector. The client receives block-change packets as
 * players build/mine nearby; this scanner buffers recent changes (time-decayed)
 * and reports clusters as live "someone is active here" findings &mdash; a very
 * strong, current base/player signal.
 */
public final class RealtimeActivityScanner extends AbstractScanner implements BlockChangeListener {

    private static final double WEIGHT = 0.8;
    private static final long WINDOW_TICKS = 600; // ~30s
    private static final int CLUSTER_RADIUS = 12;
    private static final int MIN_CHANGES = 4;
    private static final int MAX_BUFFER = 4096;

    private final Deque<Change> changes = new ArrayDeque<>();

    public RealtimeActivityScanner() {
        super("activity", "Real-time Activity Scanner",
                "Detects players actively building or mining nearby from live block changes.",
                FindingCategory.ACTIVITY);
    }

    @Override
    public void onBlockChange(Vec3i pos, String oldId, String newId, long tick) {
        synchronized (changes) {
            changes.addLast(new Change(pos, tick));
            while (changes.size() > MAX_BUFFER) {
                changes.removeFirst();
            }
        }
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        List<Change> recent = new ArrayList<>();
        synchronized (changes) {
            changes.removeIf(c -> ctx.tick() - c.tick > WINDOW_TICKS);
            recent.addAll(changes);
        }
        boolean[] used = new boolean[recent.size()];
        for (int i = 0; i < recent.size(); i++) {
            if (used[i]) {
                continue;
            }
            List<Vec3i> cluster = new ArrayList<>();
            Vec3i a = recent.get(i).pos;
            for (int j = 0; j < recent.size(); j++) {
                if (a.distanceSq(recent.get(j).pos) <= (long) CLUSTER_RADIUS * CLUSTER_RADIUS) {
                    cluster.add(recent.get(j).pos);
                    used[j] = true;
                }
            }
            if (cluster.size() >= MIN_CHANGES) {
                Vec3i c = ScannerText.centroid(cluster);
                int conf = Math.min(100, 50 + cluster.size() * 5);
                sink.emit(Finding.builder(id(), FindingCategory.ACTIVITY, c)
                        .confidence(conf)
                        .signalWeight(WEIGHT)
                        .verification(VerificationStatus.HEURISTIC)
                        .severity(Severity.HIGH)
                        .label("Live activity (" + cluster.size() + " recent changes)")
                        .source("Block updates")
                        .detail(cluster.size() + " block changes within " + WINDOW_TICKS + " ticks")
                        .observedAtTick(ctx.tick())
                        .build());
            }
        }
    }

    @Override
    public void reset() {
        synchronized (changes) {
            changes.clear();
        }
    }

    private static final class Change {
        final Vec3i pos;
        final long tick;

        Change(Vec3i pos, long tick) {
            this.pos = pos;
            this.tick = tick;
        }
    }
}
