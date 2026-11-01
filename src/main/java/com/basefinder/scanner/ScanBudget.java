package com.basefinder.scanner;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A soft budget shared across a scan cycle so no single pass stalls gameplay.
 * Scanners call {@link #consume} as they examine blocks and stop early when
 * {@link #exhausted()} becomes true. This is how the "balanced/adaptive"
 * performance target is enforced without blocking the render thread.
 */
public final class ScanBudget {

    private final AtomicLong remaining;
    private final long total;

    public ScanBudget(long total) {
        this.total = total;
        this.remaining = new AtomicLong(total);
    }

    public static ScanBudget unlimited() {
        return new ScanBudget(Long.MAX_VALUE);
    }

    public boolean exhausted() {
        return remaining.get() <= 0;
    }

    public void consume(long n) {
        remaining.addAndGet(-n);
    }

    public long remaining() {
        return Math.max(0, remaining.get());
    }

    public long used() {
        return total == Long.MAX_VALUE ? 0 : total - remaining();
    }
}
