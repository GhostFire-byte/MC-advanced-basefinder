package com.basefinder.engine;

/** Rolling performance metrics for one scan cycle, surfaced to the status panel. */
public final class EngineMetrics {

    private volatile double lastCycleMillis;
    private volatile double avgCycleMillis;
    private volatile int workerCount;
    private volatile long totalCycles;
    private volatile int lastCandidateCount;
    private volatile int lastFindingCount;
    private volatile long lastBudgetUsed;

    public void recordCycle(double cycleMillis, int workers, int candidates, int findings, long budgetUsed) {
        this.lastCycleMillis = cycleMillis;
        this.avgCycleMillis = totalCycles == 0 ? cycleMillis : avgCycleMillis * 0.8 + cycleMillis * 0.2;
        this.workerCount = workers;
        this.lastCandidateCount = candidates;
        this.lastFindingCount = findings;
        this.lastBudgetUsed = budgetUsed;
        this.totalCycles++;
    }

    public double lastCycleMillis() {
        return lastCycleMillis;
    }

    public double avgCycleMillis() {
        return avgCycleMillis;
    }

    public int workerCount() {
        return workerCount;
    }

    public long totalCycles() {
        return totalCycles;
    }

    public int lastCandidateCount() {
        return lastCandidateCount;
    }

    public int lastFindingCount() {
        return lastFindingCount;
    }

    public long lastBudgetUsed() {
        return lastBudgetUsed;
    }
}
