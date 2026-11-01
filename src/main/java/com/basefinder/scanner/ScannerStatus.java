package com.basefinder.scanner;

/** Live runtime status of one scanner, surfaced in the status panel and diagnostics. */
public final class ScannerStatus {

    private final String scannerId;
    private volatile boolean enabled = true;
    private volatile boolean healthy = true;
    private volatile long lastScanDurationNanos;
    private volatile int lastFindingCount;
    private volatile long totalScans;
    private volatile String lastError;
    private volatile long lastScanTick;

    public ScannerStatus(String scannerId) {
        this.scannerId = scannerId;
    }

    public String scannerId() {
        return scannerId;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
    }

    public boolean healthy() {
        return healthy;
    }

    public long lastScanDurationNanos() {
        return lastScanDurationNanos;
    }

    public double lastScanMillis() {
        return lastScanDurationNanos / 1_000_000.0;
    }

    public int lastFindingCount() {
        return lastFindingCount;
    }

    public long totalScans() {
        return totalScans;
    }

    public String lastError() {
        return lastError;
    }

    public long lastScanTick() {
        return lastScanTick;
    }

    public void recordSuccess(long durationNanos, int findingCount, long tick) {
        this.lastScanDurationNanos = durationNanos;
        this.lastFindingCount = findingCount;
        this.lastScanTick = tick;
        this.totalScans++;
        this.healthy = true;
    }

    public void recordError(String message) {
        this.lastError = message;
        this.healthy = false;
    }
}
