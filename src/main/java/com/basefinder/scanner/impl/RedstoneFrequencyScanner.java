package com.basefinder.scanner.impl;

import com.basefinder.engine.BlockChangeListener;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.BlockAccess;
import com.basefinder.world.Blocks;
import com.basefinder.world.Vec3i;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects locations with an abnormally high block-update rate &mdash; redstone
 * clocks, active farms, lag machines. The client receives block-update packets
 * for redstone state changes; this scanner buffers them per location over a
 * rolling window and flags cells whose update count exceeds an ADAPTIVE
 * threshold.
 *
 * <p>The threshold is not a fixed number: it scales with nearby redstone density.
 * A cell surrounded by many redstone components (repeaters, comparators, pistons,
 * observers, wire, ...) needs only a modest repeat rate to flag (it is almost
 * certainly a contraption), while updates in the open require a much higher rate.
 * Confidence rises with both the update rate and the redstone density.
 */
public final class RedstoneFrequencyScanner extends AbstractScanner implements BlockChangeListener {

    private static final long WINDOW_TICKS = 100; // ~5s
    private static final int CELL = 2;             // group updates into 2-block cells
    private static final int BASE_THRESHOLD = 24;  // required rate with no redstone nearby
    private static final int FLOOR_THRESHOLD = 6;  // required rate in dense redstone
    private static final int REDSTONE_RADIUS = 4;
    private static final int MAX_CELLS = 64;       // cap candidate cells per cycle
    private static final int MAX_BUFFER = 8192;

    private final Deque<Update> updates = new ArrayDeque<>();

    public RedstoneFrequencyScanner() {
        super("redstone_freq", "Redstone Frequency Scanner",
                "Flags locations with an abnormally high block-update rate (redstone clocks, active farms). "
                        + "The trigger adapts to nearby redstone density.",
                FindingCategory.REDSTONE_CLOCK);
    }

    @Override
    public void onBlockChange(Vec3i pos, String oldId, String newId, long tick) {
        synchronized (updates) {
            updates.addLast(new Update(cellKey(pos), pos, tick));
            while (updates.size() > MAX_BUFFER) {
                updates.removeFirst();
            }
        }
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        Map<Long, int[]> counts = new HashMap<>();
        Map<Long, Vec3i> samplePos = new HashMap<>();
        synchronized (updates) {
            updates.removeIf(u -> ctx.tick() - u.tick > WINDOW_TICKS);
            for (Update u : updates) {
                counts.computeIfAbsent(u.cell, k -> new int[1])[0]++;
                samplePos.putIfAbsent(u.cell, u.pos);
            }
        }

        BlockAccess blocks = ctx.world().blocks();
        int processed = 0;
        for (Map.Entry<Long, int[]> e : counts.entrySet()) {
            if (processed >= MAX_CELLS) {
                break;
            }
            int count = e.getValue()[0];
            if (count < FLOOR_THRESHOLD) {
                continue; // can't flag below the lowest possible adaptive threshold
            }
            Vec3i pos = samplePos.get(e.getKey());
            if (!ctx.scope().contains(pos)) {
                continue;
            }
            processed++;
            int redstone = countNearbyRedstone(blocks, pos);
            int threshold = adaptiveThreshold(redstone);
            if (count < threshold) {
                continue;
            }
            int conf = Math.min(100, 45 + (count - threshold) * 3 + redstone * 2);
            sink.emit(Finding.builder(id(), FindingCategory.REDSTONE_CLOCK, pos)
                    .confidence(conf)
                    .signalWeight(0.65)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("High update rate (" + count + "/" + WINDOW_TICKS + "t)")
                    .source("Block-update frequency")
                    .detail(count + " updates in " + WINDOW_TICKS + " ticks, " + redstone + " redstone nearby")
                    .observedAtTick(ctx.tick())
                    .meta("rate", String.valueOf(count))
                    .meta("redstone", String.valueOf(redstone))
                    .build());
        }
    }

    /** Required update count to flag: lower when more redstone is nearby. */
    private static int adaptiveThreshold(int redstoneNearby) {
        int reduced = BASE_THRESHOLD - (int) Math.round(redstoneNearby * 1.5);
        return Math.max(FLOOR_THRESHOLD, reduced);
    }

    private static int countNearbyRedstone(BlockAccess blocks, Vec3i c) {
        int count = 0;
        for (int dx = -REDSTONE_RADIUS; dx <= REDSTONE_RADIUS; dx++) {
            for (int dy = -REDSTONE_RADIUS; dy <= REDSTONE_RADIUS; dy++) {
                for (int dz = -REDSTONE_RADIUS; dz <= REDSTONE_RADIUS; dz++) {
                    int x = c.x() + dx, y = c.y() + dy, z = c.z() + dz;
                    if (!blocks.isLoaded(x, y, z)) {
                        continue;
                    }
                    if (Blocks.REDSTONE.contains(blocks.blockId(x, y, z))) {
                        count++;
                        if (count >= 16) {
                            return count; // cap; dense enough
                        }
                    }
                }
            }
        }
        return count;
    }

    @Override
    public void reset() {
        synchronized (updates) {
            updates.clear();
        }
    }

    private static long cellKey(Vec3i p) {
        long cx = Math.floorDiv(p.x(), CELL) & 0x3FFFFFL;
        long cy = Math.floorDiv(p.y(), CELL) & 0x3FFL;
        long cz = Math.floorDiv(p.z(), CELL) & 0xFFFFFFFFL;
        return (cx << 42) | (cy << 32) | cz;
    }

    private static final class Update {
        final long cell;
        final Vec3i pos;
        final long tick;

        Update(long cell, Vec3i pos, long tick) {
            this.cell = cell;
            this.pos = pos;
            this.tick = tick;
        }
    }
}
