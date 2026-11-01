package com.basefinder.diag;

/** One diagnostic entry. {@code count} collapses repeated identical messages. */
public final class LogEntry {

    private final LogLevel level;
    private final String category;
    private final String message;
    private final long firstTick;
    private long lastTick;
    private int count;

    public LogEntry(LogLevel level, String category, String message, long tick) {
        this.level = level;
        this.category = category;
        this.message = message;
        this.firstTick = tick;
        this.lastTick = tick;
        this.count = 1;
    }

    public LogLevel level() {
        return level;
    }

    public String category() {
        return category;
    }

    public String message() {
        return message;
    }

    public long firstTick() {
        return firstTick;
    }

    public long lastTick() {
        return lastTick;
    }

    public int count() {
        return count;
    }

    void repeat(long tick) {
        this.lastTick = tick;
        this.count++;
    }

    public String key() {
        return level + "|" + category + "|" + message;
    }

    @Override
    public String toString() {
        String suffix = count > 1 ? " (x" + count + ")" : "";
        return "[" + level + "] " + category + ": " + message + suffix;
    }
}
