package com.basefinder.config;

/**
 * Global, authoritative settings that apply across every subsystem. Per the
 * design, detection range and the confidence floor are global and override any
 * per-subsystem preference unless that subsystem sets a stricter override.
 */
public final class GlobalSettings {

    // Scanning scope.
    private int detectionRadiusChunks = 8;
    private boolean bindRadiusToRenderDistance = false;
    private int minY = -64;
    private int maxY = 320;
    private ScanAggressiveness aggressiveness = ScanAggressiveness.BALANCED;

    // Confidence (authoritative global floor; default 75%).
    private int confidenceFloor = 75;

    // Radar.
    private int radarRange = 128;
    private boolean bindRadarToRenderDistance = false;

    // Notifications.
    private boolean notifyOnlyNewCandidates = true;
    private boolean notifyOncePerCandidate = true;
    private int notifyCooldownTicks = 300; // ~15s at 20 tps

    // Overlay / capture.
    private boolean overlayEnabled = true;
    private boolean wdaExcludeFromCapture = true;

    // 3D ESP visuals (all on by default).
    private boolean espOutlines = true;
    private boolean espBoxes = true;
    private boolean espLabels = true;
    private boolean espIcons = true;
    private boolean radarEnabled = true;

    // Status-panel content (defaults per spec: engine, subsystems, performance, counts, errors).
    private boolean spEngine = true;
    private boolean spSubsystems = true;
    private boolean spDataHealth = false;
    private boolean spPerformance = true;
    private boolean spActivity = false;
    private boolean spResultCounts = true;
    private boolean spErrors = true;

    public int detectionRadiusChunks() {
        return detectionRadiusChunks;
    }

    public void setDetectionRadiusChunks(int v) {
        this.detectionRadiusChunks = Math.max(1, Math.min(32, v));
    }

    public boolean bindRadiusToRenderDistance() {
        return bindRadiusToRenderDistance;
    }

    public void setBindRadiusToRenderDistance(boolean v) {
        this.bindRadiusToRenderDistance = v;
    }

    public int minY() {
        return minY;
    }

    public void setMinY(int v) {
        this.minY = v;
    }

    public int maxY() {
        return maxY;
    }

    public void setMaxY(int v) {
        this.maxY = v;
    }

    public ScanAggressiveness aggressiveness() {
        return aggressiveness;
    }

    public void setAggressiveness(ScanAggressiveness v) {
        this.aggressiveness = v;
    }

    public int confidenceFloor() {
        return confidenceFloor;
    }

    public void setConfidenceFloor(int v) {
        this.confidenceFloor = Math.max(0, Math.min(100, v));
    }

    public int radarRange() {
        return radarRange;
    }

    public void setRadarRange(int v) {
        this.radarRange = Math.max(16, Math.min(1024, v));
    }

    public boolean bindRadarToRenderDistance() {
        return bindRadarToRenderDistance;
    }

    public void setBindRadarToRenderDistance(boolean v) {
        this.bindRadarToRenderDistance = v;
    }

    public boolean notifyOnlyNewCandidates() {
        return notifyOnlyNewCandidates;
    }

    public void setNotifyOnlyNewCandidates(boolean v) {
        this.notifyOnlyNewCandidates = v;
    }

    public boolean notifyOncePerCandidate() {
        return notifyOncePerCandidate;
    }

    public void setNotifyOncePerCandidate(boolean v) {
        this.notifyOncePerCandidate = v;
    }

    public int notifyCooldownTicks() {
        return notifyCooldownTicks;
    }

    public void setNotifyCooldownTicks(int v) {
        this.notifyCooldownTicks = Math.max(0, v);
    }

    public boolean overlayEnabled() {
        return overlayEnabled;
    }

    public void setOverlayEnabled(boolean v) {
        this.overlayEnabled = v;
    }

    public boolean wdaExcludeFromCapture() {
        return wdaExcludeFromCapture;
    }

    public void setWdaExcludeFromCapture(boolean v) {
        this.wdaExcludeFromCapture = v;
    }

    public boolean espOutlines() {
        return espOutlines;
    }

    public void setEspOutlines(boolean v) {
        this.espOutlines = v;
    }

    public boolean espBoxes() {
        return espBoxes;
    }

    public void setEspBoxes(boolean v) {
        this.espBoxes = v;
    }

    public boolean espLabels() {
        return espLabels;
    }

    public void setEspLabels(boolean v) {
        this.espLabels = v;
    }

    public boolean espIcons() {
        return espIcons;
    }

    public void setEspIcons(boolean v) {
        this.espIcons = v;
    }

    public boolean radarEnabled() {
        return radarEnabled;
    }

    public void setRadarEnabled(boolean v) {
        this.radarEnabled = v;
    }

    public boolean spEngine() {
        return spEngine;
    }

    public void setSpEngine(boolean v) {
        this.spEngine = v;
    }

    public boolean spSubsystems() {
        return spSubsystems;
    }

    public void setSpSubsystems(boolean v) {
        this.spSubsystems = v;
    }

    public boolean spDataHealth() {
        return spDataHealth;
    }

    public void setSpDataHealth(boolean v) {
        this.spDataHealth = v;
    }

    public boolean spPerformance() {
        return spPerformance;
    }

    public void setSpPerformance(boolean v) {
        this.spPerformance = v;
    }

    public boolean spActivity() {
        return spActivity;
    }

    public void setSpActivity(boolean v) {
        this.spActivity = v;
    }

    public boolean spResultCounts() {
        return spResultCounts;
    }

    public void setSpResultCounts(boolean v) {
        this.spResultCounts = v;
    }

    public boolean spErrors() {
        return spErrors;
    }

    public void setSpErrors(boolean v) {
        this.spErrors = v;
    }
}
