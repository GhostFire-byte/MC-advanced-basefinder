package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractBlockScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.BlockAccess;
import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects player branch mines: long straight 1-wide, 2-tall tunnels at low Y.
 * These feed the "Mining area" classification rather than a base (unless
 * habitation evidence sits nearby), so strip mines don't masquerade as bases.
 */
public final class BranchMineScanner extends AbstractBlockScanner {

    private static final int MAX_Y = 55;
    private int minLength = 12;

    private final List<Vec3i> tunnelFloor = new ArrayList<>();
    private BlockAccess blocks;
    private long tick;

    public BranchMineScanner() {
        super("branch_mine", "Branch Mine Scanner",
                "Detects long straight 1x2 mining tunnels (strip mines) that trail toward a base.",
                FindingCategory.MINING_AREA);
    }

    @Override
    public boolean wantsAir() {
        return true;
    }

    @Override
    public void begin(ScanContext ctx) {
        tunnelFloor.clear();
        blocks = ctx.world().blocks();
        tick = ctx.tick();
        minLength = Math.max(6, ctx.settings().paramInt("tunnel.length", 12));
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (y > MAX_Y || !isAir(id)) {
            return;
        }
        // 2-tall air corridor with solid floor and ceiling.
        if (isAir(blocks.blockId(x, y + 1, z)) && !isAir(blocks.blockId(x, y - 1, z))
                && !isAir(blocks.blockId(x, y + 2, z))) {
            // 1-wide: solid walls on both sides of at least one axis.
            boolean narrowX = !isAir(blocks.blockId(x - 1, y, z)) && !isAir(blocks.blockId(x + 1, y, z));
            boolean narrowZ = !isAir(blocks.blockId(x, y, z - 1)) && !isAir(blocks.blockId(x, y, z + 1));
            if (narrowX || narrowZ) {
                tunnelFloor.add(new Vec3i(x, y, z));
            }
        }
    }

    @Override
    public void finish(FindingSink sink) {
        if (tunnelFloor.size() < minLength) {
            return;
        }
        int run = LineRuns.longestRun(tunnelFloor);
        if (run < minLength) {
            return;
        }
        Vec3i c = ScannerText.centroid(tunnelFloor);
        int conf = Math.min(100, 45 + run * 2);
        sink.emit(Finding.builder(id(), FindingCategory.MINING_AREA, c)
                .confidence(conf)
                .signalWeight(0.4)
                .verification(VerificationStatus.HEURISTIC)
                .severity(Severity.LOW)
                .label("Branch mine (" + run + " long)")
                .source("Tunnel scan")
                .detail("Straight 1x2 tunnel run of " + run + " blocks")
                .observedAtTick(tick)
                .build());
    }

    private static boolean isAir(String id) {
        return id == null || id.endsWith(":air") || id.endsWith("_air");
    }
}
