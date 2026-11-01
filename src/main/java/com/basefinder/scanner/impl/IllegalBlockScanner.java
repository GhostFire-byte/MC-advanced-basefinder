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
import java.util.List;

/**
 * Finds blocks that cannot be obtained/placed in normal survival (command blocks,
 * barriers, structure blocks, ...) plus contextually-misplaced bedrock &mdash;
 * common markers around griefed/blown-up bases on anarchy servers.
 */
public final class IllegalBlockScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.85;

    private final List<Illegal> hits = new ArrayList<>();
    private Dimension dimension = Dimension.UNKNOWN;
    private long tick;

    public IllegalBlockScanner() {
        super("illegal", "Illegal Block Scanner",
                "Detects non-survival blocks (command blocks, barriers, structure voids) and misplaced bedrock.",
                FindingCategory.ILLEGAL);
    }

    @Override
    public void begin(ScanContext ctx) {
        hits.clear();
        dimension = ctx.world().dimension();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.ILLEGAL.contains(id)) {
            hits.add(new Illegal(new Vec3i(x, y, z), id));
        } else if (id.equals("minecraft:bedrock") && isMisplacedBedrock(y)) {
            hits.add(new Illegal(new Vec3i(x, y, z), "misplaced bedrock"));
        }
    }

    private boolean isMisplacedBedrock(int y) {
        // Overworld bedrock generates only at the world bottom (y below ~-59).
        return dimension == Dimension.OVERWORLD && y > 5;
    }

    @Override
    public void finish(FindingSink sink) {
        for (Illegal h : hits) {
            sink.emit(Finding.builder(id(), FindingCategory.ILLEGAL, h.pos)
                    .confidence(95)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.HIGH)
                    .label("Illegal: " + ScannerText.pretty(h.what))
                    .source("Block scan")
                    .detail(h.what + " at " + h.pos)
                    .observedAtTick(tick)
                    .meta("block", h.what)
                    .build());
        }
    }

    private static final class Illegal {
        final Vec3i pos;
        final String what;

        Illegal(Vec3i pos, String what) {
            this.pos = pos;
            this.what = what;
        }
    }
}
