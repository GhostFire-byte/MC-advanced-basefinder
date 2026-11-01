package com.basefinder.engine;

/**
 * Decides how many worker threads the next scan cycle should use. Starts small
 * (derived from CPU count) and adapts within a safe cap based on how the last
 * cycle performed relative to a target frame-budget, so scanning never starves
 * Minecraft. This is the "benchmark and self-tune rather than hard-code" approach.
 */
public final class PerformanceGovernor {

    private final int maxWorkers;
    private final double targetCycleMillis;
    private int current;

    public PerformanceGovernor(double targetCycleMillis) {
        int cores = Runtime.getRuntime().availableProcessors();
        this.maxWorkers = Math.max(1, Math.min(4, cores / 4));
        this.targetCycleMillis = targetCycleMillis;
        this.current = 1;
    }

    public int maxWorkers() {
        return maxWorkers;
    }

    public int current() {
        return current;
    }

    /** Adjust worker count from the last cycle's measured duration. */
    public int adjust(double lastCycleMillis) {
        if (lastCycleMillis > targetCycleMillis * 1.5 && current < maxWorkers) {
            current++;
        } else if (lastCycleMillis < targetCycleMillis * 0.5 && current > 1) {
            current--;
        }
        return current;
    }
}
