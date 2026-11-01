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

/** Finds rarely-natural marker blocks (lodestone, conduit, respawn anchor, dragon egg). */
public final class MarkerScanner extends AbstractBlockScanner {

    private final List<Vec3i> markers = new ArrayList<>();
    private final List<String> ids = new ArrayList<>();
    private long tick;

    public MarkerScanner() {
        super("marker", "Marker Block Scanner",
                "Detects lodestones, conduits, respawn anchors and dragon eggs \u2014 strong base markers.",
                FindingCategory.MARKER);
    }

    @Override
    public void begin(ScanContext ctx) {
        markers.clear();
        ids.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.MARKERS.contains(id)) {
            markers.add(new Vec3i(x, y, z));
            ids.add(id);
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (int i = 0; i < markers.size(); i++) {
            sink.emit(Finding.builder(id(), FindingCategory.MARKER, markers.get(i))
                    .confidence(95)
                    .signalWeight(0.95)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.HIGH)
                    .label(ScannerText.pretty(ids.get(i)))
                    .source("Marker block")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
