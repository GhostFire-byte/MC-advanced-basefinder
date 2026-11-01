package com.basefinder.config;

/** How hard the engine scans; affects scan interval and per-cycle budget. */
public enum ScanAggressiveness {
    LIGHT(40, 1_500_000),
    BALANCED(20, 4_000_000),
    AGGRESSIVE(10, 9_000_000);

    private final int scanIntervalTicks;
    private final long blockBudgetPerCycle;

    ScanAggressiveness(int scanIntervalTicks, long blockBudgetPerCycle) {
        this.scanIntervalTicks = scanIntervalTicks;
        this.blockBudgetPerCycle = blockBudgetPerCycle;
    }

    /** Minimum ticks between full scan passes. */
    public int scanIntervalTicks() {
        return scanIntervalTicks;
    }

    /** Soft cap on blocks examined per cycle, enforced by the scheduler budget. */
    public long blockBudgetPerCycle() {
        return blockBudgetPerCycle;
    }
}
