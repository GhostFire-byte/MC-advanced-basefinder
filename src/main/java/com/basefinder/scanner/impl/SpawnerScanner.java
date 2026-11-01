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

/** Finds mob spawners and trial spawners (verified block entities). */
public final class SpawnerScanner extends AbstractScanner {

    private static final double WEIGHT = 0.6;

    public SpawnerScanner() {
        super("spawner", "Spawner Scanner",
                "Detects mob spawners and trial spawners in loaded chunks.",
                FindingCategory.SPAWNER);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        for (BlockRef be : ctx.world().blockEntities()) {
            if (!ctx.scope().containsBlockEntity(be) || !Blocks.SPAWNERS.contains(be.id())) {
                continue;
            }
            boolean trial = be.id().contains("trial");
            sink.emit(Finding.builder(id(), FindingCategory.SPAWNER, be.pos())
                    .confidence(100)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(trial ? Severity.MEDIUM : Severity.LOW)
                    .label(trial ? "Trial spawner" : "Mob spawner")
                    .source("Block entity")
                    .detail(be.id() + " at " + be.pos())
                    .observedAtTick(ctx.tick())
                    .meta("variant", trial ? "trial" : "mob")
                    .build());
        }
    }
}
