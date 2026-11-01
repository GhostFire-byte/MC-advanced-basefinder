package com.basefinder.scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Holds all registered scanners and their runtime status. */
public final class ScannerRegistry {

    private final Map<String, Scanner> scanners = new LinkedHashMap<>();
    private final Map<String, ScannerStatus> statuses = new LinkedHashMap<>();

    public void register(Scanner scanner) {
        if (scanners.containsKey(scanner.id())) {
            throw new IllegalArgumentException("Duplicate scanner id: " + scanner.id());
        }
        scanners.put(scanner.id(), scanner);
        statuses.put(scanner.id(), new ScannerStatus(scanner.id()));
    }

    public Scanner get(String id) {
        return scanners.get(id);
    }

    public ScannerStatus status(String id) {
        return statuses.get(id);
    }

    public Collection<Scanner> all() {
        return scanners.values();
    }

    public Collection<ScannerStatus> statuses() {
        return statuses.values();
    }

    public List<Scanner> enabled() {
        List<Scanner> out = new ArrayList<>();
        for (Scanner s : scanners.values()) {
            ScannerStatus st = statuses.get(s.id());
            if (st != null && st.enabled()) {
                out.add(s);
            }
        }
        return out;
    }

    public int size() {
        return scanners.size();
    }

    public void resetAll() {
        for (Scanner s : scanners.values()) {
            try {
                s.reset();
            } catch (Throwable ignored) {
                // a scanner's reset must never break others
            }
        }
    }
}
