package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Blocks;

/** Finds beacons (strong indicator of an established base). */
public final class BeaconScanner extends AbstractScanner {

    private static final double WEIGHT = 0.9;

    public BeaconScanner() {
        super("beacon", "Beacon Scanner",
                "Detects beacons, which almost always mark an established base.",
                FindingCategory.BEACON);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        for (BlockRef be : ctx.world().blockEntities()) {
            if (!ctx.scope().containsBlockEntity(be) || !Blocks.BEACON.contains(be.id())) {
                continue;
            }
            sink.emit(Finding.builder(id(), FindingCategory.BEACON, be.pos())
                    .confidence(100)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.HIGH)
                    .label("Beacon")
                    .source("Block entity")
                    .detail("Beacon at " + be.pos())
                    .observedAtTick(ctx.tick())
                    .build());
        }
    }
}
