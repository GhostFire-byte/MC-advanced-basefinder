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
import java.util.List;

/**
 * Breadcrumb / path tracer. Rails, torch lines and ladder shafts form trails
 * that usually lead to a base. Detects long straight runs of such markers and
 * reports them as path findings pointing the way.
 */
public final class BreadcrumbScanner extends AbstractBlockScanner {

    private static final double WEIGHT = 0.4;
    private static final int MIN_RUN = 8;

    private final List<Vec3i> rails = new ArrayList<>();
    private final List<Vec3i> trailMarkers = new ArrayList<>();
    private long tick;

    public BreadcrumbScanner() {
        super("breadcrumb", "Breadcrumb / Path Scanner",
                "Detects rails, torch lines and ladder shafts that trail toward a base.",
                FindingCategory.PATH);
    }

    @Override
    public void begin(ScanContext ctx) {
        rails.clear();
        trailMarkers.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isRail(id)) {
            rails.add(new Vec3i(x, y, z));
        } else if (id.equals("minecraft:torch") || id.equals("minecraft:wall_torch")
                || id.equals("minecraft:ladder")) {
            trailMarkers.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        emitRun(rails, "Rail line", sink);
        emitRun(trailMarkers, "Marker trail", sink);
    }

    private void emitRun(List<Vec3i> pts, String label, FindingSink sink) {
        if (pts.size() < MIN_RUN) {
            return;
        }
        int run = LineRuns.longestRun(pts);
        if (run < MIN_RUN) {
            return;
        }
        Vec3i c = ScannerText.centroid(pts);
        int conf = Math.min(90, 40 + run * 3);
        sink.emit(Finding.builder(id(), FindingCategory.PATH, c)
                .confidence(conf)
                .signalWeight(WEIGHT)
                .verification(VerificationStatus.HEURISTIC)
                .severity(Severity.LOW)
                .label(label + " (" + run + ")")
                .source("Block scan")
                .observedAtTick(tick)
                .build());
    }
}
