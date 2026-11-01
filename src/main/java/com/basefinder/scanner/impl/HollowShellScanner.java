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

import java.util.HashMap;
import java.util.Map;

/**
 * Detects hollow-shell bases: a large enclosed air pocket dug underground and
 * lined with player blocks (walls/floor/light), as opposed to a natural cave.
 *
 * <p>Per 16-block cell it accumulates air volume and player-block count. A cell
 * with a big air pocket AND meaningful player-block lining is a hollow shell;
 * confidence scales with air volume. Natural caves (lots of air, ~no player
 * blocks) are excluded, which is how big caves are told apart from bases.
 */
public final class HollowShellScanner extends AbstractBlockScanner {

    private static final int CELL = 16;
    private static final int UNDERGROUND_Y = 55;
    private int minAir = 40;
    private static final int MIN_PLAYER_LINING = 8;

    private final Map<Long, Cell> cells = new HashMap<>();
    private long tick;

    public HollowShellScanner() {
        super("hollow_shell", "Hollow Shell Scanner",
                "Detects underground bases carved into terrain: a large air pocket lined with player blocks "
                        + "(distinguished from natural caves).",
                FindingCategory.HOLLOW_SHELL);
    }

    @Override
    public boolean wantsAir() {
        return true;
    }

    @Override
    public void begin(ScanContext ctx) {
        cells.clear();
        tick = ctx.tick();
        minAir = Math.max(16, ctx.settings().paramInt("shell.minAir", 40));
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (y >= UNDERGROUND_Y) {
            return;
        }
        Cell c = cells.computeIfAbsent(key(x, y, z), k -> new Cell(new Vec3i(
                Math.floorDiv(x, CELL) * CELL + CELL / 2,
                Math.floorDiv(y, CELL) * CELL + CELL / 2,
                Math.floorDiv(z, CELL) * CELL + CELL / 2)));
        if (id.endsWith(":air") || id.endsWith("_air")) {
            c.air++;
        } else if (Blocks.isRefinedPlayerBlock(id) || Blocks.isPlayerLight(id, com.basefinder.world.Dimension.OVERWORLD)) {
            c.player++;
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Cell c : cells.values()) {
            if (c.air < minAir || c.player < MIN_PLAYER_LINING) {
                continue;
            }
            // The player-block lining (absolute count) is what separates a carved base
            // from a natural cave (which has ~0 player blocks). Confidence scales with
            // air volume plus the amount of built lining.
            int conf = Math.min(100, 40 + c.air / 4 + c.player);
            sink.emit(Finding.builder(id(), FindingCategory.HOLLOW_SHELL, c.center)
                    .confidence(conf)
                    .signalWeight(0.6)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("Hollow shell (" + c.air + " air, " + c.player + " built)")
                    .source("Enclosed interior")
                    .observedAtTick(tick)
                    .build());
        }
    }

    private static long key(int x, int y, int z) {
        long cx = Math.floorDiv(x, CELL) & 0x3FFFFFL;
        long cy = Math.floorDiv(y, CELL) & 0x3FFL;
        long cz = Math.floorDiv(z, CELL) & 0xFFFFFFFFL;
        return (cx << 42) | (cy << 32) | cz;
    }

    private static final class Cell {
        final Vec3i center;
        int air, player;

        Cell(Vec3i center) {
            this.center = center;
        }
    }
}
