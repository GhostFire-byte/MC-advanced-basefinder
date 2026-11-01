package com.basefinder.diag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Detailed diagnostic log with de-duplication: repeated identical messages
 * increment a counter on the existing entry instead of flooding the log. Keeps a
 * bounded ring buffer of recent entries for the diagnostics panel; all data is
 * in-memory only (no persistence), consistent with the temporary-data rule.
 */
public final class DiagnosticsLog {

    private final int capacity;
    private final Deque<LogEntry> entries = new ArrayDeque<>();
    private LogLevel minLevel = LogLevel.INFO;
    private volatile long currentTick;

    public DiagnosticsLog() {
        this(400);
    }

    public DiagnosticsLog(int capacity) {
        this.capacity = capacity;
    }

    public void setMinLevel(LogLevel level) {
        this.minLevel = level;
    }

    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }

    public synchronized void log(LogLevel level, String category, String message) {
        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }
        // De-dup against the most recent entry with the same key.
        for (LogEntry e : entries) {
            if (e.key().equals(keyOf(level, category, message))) {
                e.repeat(currentTick);
                return;
            }
        }
        entries.addLast(new LogEntry(level, category, message, currentTick));
        while (entries.size() > capacity) {
            entries.removeFirst();
        }
    }

    public void debug(String category, String message) {
        log(LogLevel.DEBUG, category, message);
    }

    public void info(String category, String message) {
        log(LogLevel.INFO, category, message);
    }

    public void warn(String category, String message) {
        log(LogLevel.WARN, category, message);
    }

    public void error(String category, String message) {
        log(LogLevel.ERROR, category, message);
    }

    public synchronized List<LogEntry> recent() {
        return new ArrayList<>(entries);
    }

    public synchronized List<LogEntry> recent(int n) {
        List<LogEntry> all = new ArrayList<>(entries);
        int from = Math.max(0, all.size() - n);
        return new ArrayList<>(all.subList(from, all.size()));
    }

    public synchronized void clear() {
        entries.clear();
    }

    private static String keyOf(LogLevel level, String category, String message) {
        return level + "|" + category + "|" + message;
    }
}
