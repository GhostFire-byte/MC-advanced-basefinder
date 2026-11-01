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
 * Detects concealed entrances: piston doors (clusters of pistons, often sticky)
 * and iron door/iron trapdoor entrances. Several pistons packed together almost
 * always form a hidden door or vault entrance.
 */
public final class HiddenEntranceScanner extends AbstractBlockScanner {

    private static final int CLUSTER_RADIUS = 4;
    private static final int MIN_PISTONS = 2;

    private final List<Vec3i> pistons = new ArrayList<>();
    private final List<Vec3i> doors = new ArrayList<>();
    private long tick;

    public HiddenEntranceScanner() {
        super("hidden_entrance", "Hidden Entrance Scanner",
                "Detects piston doors and iron door/trapdoor entrances that conceal a base.",
                FindingCategory.HIDDEN_ENTRANCE);
    }

    @Override
    public void begin(ScanContext ctx) {
        pistons.clear();
        doors.clear();
        tick = ctx.tick();
    }

    @Override
    public void visitBlock(int x, int y, int z, String id, int light) {
        if (Blocks.isPiston(id)) {
            pistons.add(new Vec3i(x, y, z));
        } else if (id.equals("minecraft:iron_door") || id.equals("minecraft:iron_trapdoor")) {
            doors.add(new Vec3i(x, y, z));
        }
    }

    @Override
    public void finish(FindingSink sink) {
        for (Clustering.Cluster c : Clustering.cluster(pistons, CLUSTER_RADIUS)) {
            if (c.count() < MIN_PISTONS) {
                continue;
            }
            boolean nearDoor = doorNear(c.center());
            int conf = Math.min(100, 45 + c.count() * 8 + (nearDoor ? 15 : 0));
            sink.emit(Finding.builder(id(), FindingCategory.HIDDEN_ENTRANCE, c.center())
                    .confidence(conf)
                    .signalWeight(0.65)
                    .verification(VerificationStatus.HEURISTIC)
                    .severity(Severity.MEDIUM)
                    .label("Hidden entrance (" + c.count() + " pistons" + (nearDoor ? " + door)" : ")"))
                    .source("Piston door")
                    .observedAtTick(tick)
                    .build());
        }
    }

    private boolean doorNear(Vec3i center) {
        for (Vec3i d : doors) {
            if (center.distanceSq(d) <= 36) {
                return true;
            }
        }
        return false;
    }
}
