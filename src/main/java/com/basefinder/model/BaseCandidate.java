package com.basefinder.model;

import com.basefinder.world.Vec3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A spatial cluster of findings fused into a single "something is here" result.
 * Maintained across scan cycles by the fusion manager so temporal persistence
 * can accrue. Display overrides (from rules) live here too and are reapplied
 * each cycle.
 */
public final class BaseCandidate {

    private final String id;
    private Vec3i center;
    private int baseConfidence;
    private int fusedConfidence;
    private double persistence;
    private final long firstSeenTick;
    private long lastSeenTick;
    private int consecutiveCycles;
    private Severity severity = Severity.LOW;

    private final List<Finding> findings = new ArrayList<>();
    private final EnumSet<FindingCategory> categories = EnumSet.noneOf(FindingCategory.class);
    private CandidateKind kind = CandidateKind.BASE;

    /** Whether this cluster reads as an inhabited base or merely an activity/mining area. */
    public enum CandidateKind {BASE, MINING_AREA, ACTIVITY, DUNGEON, LEFTOVER}

    // Rule-driven display state (reset + reapplied every cycle).
    private boolean visible = true;
    private boolean pinned;
    private boolean forcedHidden;
    private boolean forcedShown;
    private Integer colorOverride;
    private String labelOverride;
    private String iconOverride;

    public BaseCandidate(String id, Vec3i center, long nowTick) {
        this.id = id;
        this.center = center;
        this.firstSeenTick = nowTick;
        this.lastSeenTick = nowTick;
    }

    public String id() {
        return id;
    }

    public Vec3i center() {
        return center;
    }

    public void setCenter(Vec3i c) {
        this.center = c;
    }

    public int baseConfidence() {
        return baseConfidence;
    }

    public void setBaseConfidence(int c) {
        this.baseConfidence = Confidence.clamp(c);
    }

    public int fusedConfidence() {
        return fusedConfidence;
    }

    public void setFusedConfidence(int c) {
        this.fusedConfidence = Confidence.clamp(c);
    }

    public double persistence() {
        return persistence;
    }

    public void setPersistence(double p) {
        this.persistence = Math.max(0.0, Math.min(1.0, p));
    }

    public long firstSeenTick() {
        return firstSeenTick;
    }

    public long lastSeenTick() {
        return lastSeenTick;
    }

    public void setLastSeenTick(long t) {
        this.lastSeenTick = t;
    }

    public int consecutiveCycles() {
        return consecutiveCycles;
    }

    public void incrementCycles() {
        this.consecutiveCycles++;
    }

    public Severity severity() {
        return severity;
    }

    public void setSeverity(Severity s) {
        this.severity = s;
    }

    public CandidateKind kind() {
        return kind;
    }

    public void setKind(CandidateKind kind) {
        this.kind = kind;
    }

    public List<Finding> findings() {
        return Collections.unmodifiableList(findings);
    }

    public Set<FindingCategory> categories() {
        return Collections.unmodifiableSet(categories);
    }

    public void replaceFindings(List<Finding> next) {
        findings.clear();
        categories.clear();
        findings.addAll(next);
        for (Finding f : next) {
            categories.add(f.category());
        }
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean v) {
        this.visible = v;
    }

    public boolean pinned() {
        return pinned;
    }

    public void setPinned(boolean p) {
        this.pinned = p;
    }

    public boolean forcedHidden() {
        return forcedHidden;
    }

    public void setForcedHidden(boolean v) {
        this.forcedHidden = v;
    }

    public boolean forcedShown() {
        return forcedShown;
    }

    public void setForcedShown(boolean v) {
        this.forcedShown = v;
    }

    public Integer colorOverride() {
        return colorOverride;
    }

    public void setColorOverride(Integer c) {
        this.colorOverride = c;
    }

    public String labelOverride() {
        return labelOverride;
    }

    public void setLabelOverride(String l) {
        this.labelOverride = l;
    }

    public String iconOverride() {
        return iconOverride;
    }

    public void setIconOverride(String i) {
        this.iconOverride = i;
    }

    /** Reset transient rule state before rules are reapplied for a cycle. */
    public void resetRuleState() {
        pinned = false;
        forcedHidden = false;
        forcedShown = false;
        colorOverride = null;
        labelOverride = null;
        iconOverride = null;
    }

    public String displayLabel() {
        if (labelOverride != null) {
            return labelOverride;
        }
        return switch (kind) {
            case MINING_AREA -> "Mining area";
            case ACTIVITY -> "Activity";
            case DUNGEON -> "Spawner chamber";
            case LEFTOVER -> "Surface storage";
            default -> "Base candidate";
        };
    }
}
