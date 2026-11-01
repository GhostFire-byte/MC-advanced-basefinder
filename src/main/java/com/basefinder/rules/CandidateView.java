package com.basefinder.rules;

import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.world.Vec3d;

import java.util.HashSet;
import java.util.Set;

/** Flattened, read-only view of a candidate used to evaluate rule conditions. */
public final class CandidateView {

    private final Set<String> scannerIds = new HashSet<>();
    private final Set<String> categories = new HashSet<>();
    private final int fusedScore;
    private final int maxConfidence;
    private final double distance;
    private final int yLevel;
    private final String dimension;
    private final int findingCount;
    private final int severityWeight;

    public CandidateView(BaseCandidate c, Vec3d playerPos, String dimension) {
        int maxConf = 0;
        for (Finding f : c.findings()) {
            scannerIds.add(f.scannerId());
            categories.add(f.category().name());
            maxConf = Math.max(maxConf, f.confidence());
        }
        this.fusedScore = c.fusedConfidence();
        this.maxConfidence = maxConf;
        this.yLevel = c.center().y();
        this.dimension = dimension;
        this.findingCount = c.findings().size();
        this.severityWeight = c.severity().weight();
        Vec3d center = new Vec3d(c.center().x(), c.center().y(), c.center().z());
        this.distance = playerPos == null ? 0 : playerPos.distance(center);
    }

    public Set<String> scannerIds() {
        return scannerIds;
    }

    public Set<String> categories() {
        return categories;
    }

    public int fusedScore() {
        return fusedScore;
    }

    public int maxConfidence() {
        return maxConfidence;
    }

    public double distance() {
        return distance;
    }

    public int yLevel() {
        return yLevel;
    }

    public String dimension() {
        return dimension;
    }

    public int findingCount() {
        return findingCount;
    }

    public int severityWeight() {
        return severityWeight;
    }
}
