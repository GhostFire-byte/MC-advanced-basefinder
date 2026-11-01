package com.basefinder.engine;

/**
 * Tracks world data-source availability with automatic retry accounting, so the
 * module can "retry, else continue, else report the limitation" per the spec
 * instead of presenting stale data as current.
 */
public final class DataSourceHealth {

    private volatile boolean available = false;
    private volatile int consecutiveFailures = 0;
    private volatile long lastAvailableTick = -1;
    private volatile String note = "No world loaded";

    public boolean available() {
        return available;
    }

    public int consecutiveFailures() {
        return consecutiveFailures;
    }

    public long lastAvailableTick() {
        return lastAvailableTick;
    }

    public String note() {
        return note;
    }

    public void markAvailable(long tick) {
        this.available = true;
        this.consecutiveFailures = 0;
        this.lastAvailableTick = tick;
        this.note = "World data available";
    }

    public void markUnavailable(String reason) {
        this.available = false;
        this.consecutiveFailures++;
        this.note = reason;
    }
}
