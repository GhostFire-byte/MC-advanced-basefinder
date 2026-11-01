package com.basefinder.scanner.impl;

import com.basefinder.engine.BlockEventListener;
import com.basefinder.engine.BlockEventType;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Detects a hidden stash being used in real time. The client receives block-action
 * packets when a container's lid animates open; if a container opens with NO player
 * entity visible within ~16 blocks, someone is using a stash you can't see &mdash;
 * a strong live base/stash signal.
 */
public final class StashActivityScanner extends AbstractScanner implements BlockEventListener {

    private static final long WINDOW_TICKS = 100;
    private static final double PLAYER_RADIUS = 16.0;
    private static final int MAX_BUFFER = 2048;

    private final Deque<Event> events = new ArrayDeque<>();

    public StashActivityScanner() {
        super("stash_activity", "Stash Activity Scanner",
                "Detects containers opening with no visible player nearby \u2014 a hidden stash in live use.",
                FindingCategory.STASH_ACTIVITY);
    }

    @Override
    public void onBlockEvent(BlockEventType type, Vec3i pos, String blockId, long tick) {
        if (type != BlockEventType.CONTAINER_OPEN) {
            return;
        }
        synchronized (events) {
            events.addLast(new Event(pos, tick));
            while (events.size() > MAX_BUFFER) {
                events.removeFirst();
            }
        }
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        List<Event> recent = new ArrayList<>();
        synchronized (events) {
            events.removeIf(e -> ctx.tick() - e.tick > WINDOW_TICKS);
            recent.addAll(events);
        }
        List<Vec3d> players = new ArrayList<>();
        for (EntityRef e : ctx.world().entities()) {
            if (e.isPlayer()) {
                players.add(e.pos());
            }
        }
        double r2 = PLAYER_RADIUS * PLAYER_RADIUS;
        for (Event e : recent) {
            if (!ctx.scope().contains(e.pos)) {
                continue;
            }
            Vec3d p = new Vec3d(e.pos.x() + 0.5, e.pos.y() + 0.5, e.pos.z() + 0.5);
            boolean playerNear = false;
            for (Vec3d pl : players) {
                if (pl.distanceSq(p) <= r2) {
                    playerNear = true;
                    break;
                }
            }
            if (playerNear) {
                continue; // visible player explains it
            }
            sink.emit(Finding.builder(id(), FindingCategory.STASH_ACTIVITY, e.pos)
                    .confidence(85)
                    .signalWeight(0.8)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.HIGH)
                    .label("Stash in use (no player visible)")
                    .source("Container open event")
                    .observedAtTick(ctx.tick())
                    .build());
        }
    }

    @Override
    public void reset() {
        synchronized (events) {
            events.clear();
        }
    }

    private static final class Event {
        final Vec3i pos;
        final long tick;

        Event(Vec3i pos, long tick) {
            this.pos = pos;
            this.tick = tick;
        }
    }
}
