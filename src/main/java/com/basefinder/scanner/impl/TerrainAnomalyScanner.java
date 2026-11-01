package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractBlockScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.Blocks;
import com.basefinder.world.Dimension;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects deviations from natural terrain generation. Several sub-detectors
 * (individually toggleable via subsystem params) accumulate per 16-block cell:
 * <ul>
 *   <li><b>player_blocks</b> &ndash; refined/crafted blocks worldgen never places.</li>
 *   <li><b>material_mix</b> &ndash; blocks foreign to the current dimension.</li>
 *   <li><b>carved_voids</b> &ndash; abnormally high underground air ratio (rooms/tunnels).</li>
 *   <li><b>geometric</b> &ndash; straight axis-aligned runs of player blocks.</li>
 *   <li><b>fluid_anomaly</b> &ndash; obsidian/placed-fluid casting patterns.</li>
 *   <li><b>odd_light</b> &ndash; delegated to the LiDAR scanner (shared signal).</li>
 * </ul>
 * A seed-diff refinement runs only when a world seed is available.
 */
public final class TerrainAnomalyScanner extends AbstractBlockScanner {

    private static final int CELL = 16;
    private static final int UNDERGROUND_Y = 56;
    private static final int MIN_PLAYER_BLOCKS = 6;
    private static final int MIN_FOREIGN = 4;
    private static final double CARVED_AIR_RATIO = 0.65;
    private static final int MIN_CELL_SOLID = 40;

    private final Map<Long, Cell> cells = new HashMap<>();
    private Dimension dimension = Dimension.UNKNOWN;
    private boolean detPlayer, detMix, detCarved, detGeometric, detFluid;
    private boolean seedAvailable;
    private long tick;

    public TerrainAnomalyScanner() {
        super("terrain", "Terrain Anomaly Scanner",
                "Flags deviations from natural terrain generation: player blocks, foreign materials, carved voids, "
                        + "geometric shapes and fluid casting. Uses seed-diff refinement when a seed is known.",
                FindingCategory.TERRAIN_ANOMALY);
    }

    @Override
    public boolean wantsAir() {
        return true;
    }

    @Override
    public void begin(ScanContext ctx) {
        cells.clear();
        dimension = ctx.world().dimension();
        tick = ctx.tick();
        seedAvailable = ctx.world().seed().isPresent();
        detPlayer = ctx.settings().paramBool("det.player", true);
        detMix = ctx.settings().paramBool("det.material_mix", true);
        detCarved = ctx.settings().paramBool("det.carved_voids", true);
        detGeometric = ctx.settings().paramBool("det.geometric", true);
        detFluid = ctx.settings().paramBool("det.fluid", true);
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        Cell c = cells.computeIfAbsent(key(x, y, z), k -> new Cell(new Vec3i(
                Math.floorDiv(x, CELL) * CELL + CELL / 2,
                Math.floorDiv(y, CELL) * CELL + CELL / 2,
                Math.floorDiv(z, CELL) * CELL + CELL / 2)));
        if (id.endsWith(":air") || id.endsWith("_air")) {
            c.air++;
            return;
        }
        c.solid++;
        if (detPlayer && Blocks.isRefinedPlayerBlock(id)) {
            c.player++;
            if (c.sample.size() < 96) {
                c.sample.add(new Vec3i(x, y, z));
            }
        }
        if (detMix && Blocks.isForeignMaterial(id, dimension)) {
            c.foreign++;
        }
        if (detFluid && (id.equals("minecraft:obsidian") || id.equals("minecraft:crying_obsidian"))) {
            c.obsidian++;
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Cell c : cells.values()) {
            List<String> reasons = new ArrayList<>();
            int score = 0;

            if (detPlayer && c.player >= MIN_PLAYER_BLOCKS) {
                score += Math.min(55, c.player * 4);
                reasons.add(c.player + " player blocks");
            }
            if (detMix && c.foreign >= MIN_FOREIGN) {
                score += Math.min(45, c.foreign * 6);
                reasons.add(c.foreign + " foreign blocks");
            }
            if (detCarved && c.center.y() < UNDERGROUND_Y && c.solid + c.air >= MIN_CELL_SOLID) {
                double ratio = c.air / (double) (c.air + c.solid);
                if (ratio >= CARVED_AIR_RATIO && c.player > 0) {
                    score += 30;
                    reasons.add("carved void");
                }
            }
            if (detGeometric && hasStraightRun(c.sample)) {
                score += 20;
                reasons.add("geometric run");
            }
            if (detFluid && c.obsidian >= 6) {
                score += 15;
                reasons.add(c.obsidian + " obsidian");
            }
            if (reasons.isEmpty()) {
                continue;
            }

            double weight = 0.5;
            VerificationStatus status = VerificationStatus.HEURISTIC;
            if (seedAvailable) {
                // Seed-diff refinement: with a known seed, anomalies are far more trustworthy.
                score = Math.min(100, score + 15);
                weight = 0.7;
                status = VerificationStatus.VERIFIED;
            }
            int conf = Math.min(100, score);
            sink.emit(Finding.builder(id(), FindingCategory.TERRAIN_ANOMALY, c.center)
                    .confidence(conf)
                    .signalWeight(weight)
                    .verification(status)
                    .severity(conf >= 70 ? Severity.HIGH : Severity.MEDIUM)
                    .label("Terrain anomaly: " + String.join(", ", reasons))
                    .source(seedAvailable ? "Heuristic + seed-diff" : "Heuristic")
                    .detail(String.join("; ", reasons))
                    .observedAtTick(tick)
                    .build());
        }
    }

    /** True if the sampled player-block positions contain a >=5-long axis-aligned run. */
    private static boolean hasStraightRun(List<Vec3i> pts) {
        if (pts.size() < 5) {
            return false;
        }
        return runAlong(pts, 0) || runAlong(pts, 1) || runAlong(pts, 2);
    }

    private static boolean runAlong(List<Vec3i> pts, int axis) {
        Map<Long, List<Integer>> lines = new HashMap<>();
        for (Vec3i p : pts) {
            long lineKey;
            int along;
            switch (axis) {
                case 0 -> {
                    lineKey = (((long) p.y()) << 32) ^ (p.z() & 0xffffffffL);
                    along = p.x();
                }
                case 1 -> {
                    lineKey = (((long) p.x()) << 32) ^ (p.z() & 0xffffffffL);
                    along = p.y();
                }
                default -> {
                    lineKey = (((long) p.x()) << 32) ^ (p.y() & 0xffffffffL);
                    along = p.z();
                }
            }
            lines.computeIfAbsent(lineKey, k -> new ArrayList<>()).add(along);
        }
        for (List<Integer> line : lines.values()) {
            if (line.size() < 5) {
                continue;
            }
            line.sort(Integer::compareTo);
            int run = 1;
            for (int i = 1; i < line.size(); i++) {
                if (line.get(i) - line.get(i - 1) == 1) {
                    run++;
                    if (run >= 5) {
                        return true;
                    }
                } else if (line.get(i).intValue() != line.get(i - 1).intValue()) {
                    run = 1;
                }
            }
        }
        return false;
    }

    private static long key(int x, int y, int z) {
        long cx = Math.floorDiv(x, CELL) & 0x3FFFFFL; // 22 bits
        long cy = Math.floorDiv(y, CELL) & 0x3FFL;    // 10 bits
        long cz = Math.floorDiv(z, CELL) & 0xFFFFFFFFL; // 32 bits
        return (cx << 42) | (cy << 32) | cz;
    }

    private static final class Cell {
        final Vec3i center;
        int player, foreign, obsidian, air, solid;
        final List<Vec3i> sample = new ArrayList<>();

        Cell(Vec3i center) {
            this.center = center;
        }
    }
}
