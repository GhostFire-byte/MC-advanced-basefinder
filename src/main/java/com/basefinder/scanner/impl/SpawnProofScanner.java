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
 * Detects spawn-proofing: slabs/carpets/trapdoors placed underground to stop mob
 * spawns. The threshold is dynamic &mdash; any underground spawn-proof cover is a
 * signal, and confidence scales with how much cover is clustered together.
 */
public final class SpawnProofScanner extends AbstractBlockScanner {

    private static final int UNDERGROUND_Y = 52;
    private static final int CLUSTER_RADIUS = 8;
    private static final int MIN_COVER = 4;

    private final List<Vec3i> covers = new ArrayList<>();
    private long tick;

    public SpawnProofScanner() {
        super("spawnproof", "Spawn-proofing Scanner",
                "Detects underground slabs/carpets/trapdoors used to stop mob spawns around a base.",
                FindingCategory.SPAWN_PROOF);
    }

    @Override
    public void begin(ScanContext ctx) {
        covers.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (y < UNDERGROUND_Y && Blocks.isSpawnProofCover(id)) {
            covers.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(covers, CLUSTER_RADIUS)) {
            if (c.count() < MIN_COVER) {
                continue;
            }
            int conf = Math.min(100, 40 + c.count() * 4);
            sink.emit(Finding.builder(id(), FindingCategory.SPAWN_PROOF, c.center())
                    .confidence(conf)
                    .signalWeight(0.55)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("Spawn-proofing (" + c.count() + " covers)")
                    .source("Underground cover")
                    .observedAtTick(tick)
                    .build());
        }
    }
}
