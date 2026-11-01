package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks loaded entities (players, mobs, items, vehicles) and flags idle/AFK
 * players by watching their position history across scans &mdash; a parked
 * player usually means a nearby base or farm.
 */
public final class EntityScanner extends AbstractScanner {

    private static final double PLAYER_WEIGHT = 0.5;
    private static final double AFK_WEIGHT = 0.7;
    private static final double STILL_EPSILON = 0.25;
    private static final long AFK_TICKS = 200; // ~10s

    private final Map<Integer, Still> stillness = new HashMap<>();

    public EntityScanner() {
        super("entity", "Entity / Player Scanner",
                "Tracks players, mobs, items and vehicles; flags idle/AFK players as a base signal.",
                FindingCategory.ENTITY);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        for (EntityRef e : ctx.world().entities()) {
            if (!ctx.scope().contains(e.pos().toBlock())) {
                continue;
            }
            if (e.isPlayer()) {
                scanPlayer(ctx, e, sink);
            } else {
                sink.emit(Finding.builder(id(), FindingCategory.ENTITY, e.pos().toBlock())
                        .precisePos(e.pos())
                        .confidence(100)
                        .signalWeight(0.1)
                        .verification(VerificationStatus.VERIFIED)
                        .severity(Severity.INFO)
                        .label(ScannerText.pretty(e.type()))
                        .source("Entity")
                        .observedAtTick(ctx.tick())
                        .build());
            }
        }
    }

    private void scanPlayer(ScanContext ctx, EntityRef e, FindingSink sink) {
        Still st = stillness.computeIfAbsent(e.networkId(), k -> new Still(e.pos(), ctx.tick()));
        if (e.pos().distance(st.lastPos) > STILL_EPSILON) {
            st.lastPos = e.pos();
            st.stillSinceTick = ctx.tick();
        }
        boolean afk = ctx.tick() - st.stillSinceTick >= AFK_TICKS;
        sink.emit(Finding.builder(id(), FindingCategory.PLAYER, e.pos().toBlock())
                .precisePos(e.pos())
                .confidence(100)
                .signalWeight(afk ? AFK_WEIGHT : PLAYER_WEIGHT)
                .verification(VerificationStatus.VERIFIED)
                .severity(afk ? Severity.HIGH : Severity.MEDIUM)
                .label(afk ? "AFK player: " + e.displayName() : "Player: " + e.displayName())
                .source(afk ? "Idle player" : "Player")
                .detail(afk ? "Stationary for " + (ctx.tick() - st.stillSinceTick) + " ticks" : "Active player")
                .observedAtTick(ctx.tick())
                .meta("afk", String.valueOf(afk))
                .build());
    }

    @Override
    public void reset() {
        stillness.clear();
    }

    private static final class Still {
        Vec3d lastPos;
        long stillSinceTick;

        Still(Vec3d pos, long tick) {
            this.lastPos = pos;
            this.stillSinceTick = tick;
        }
    }
}
