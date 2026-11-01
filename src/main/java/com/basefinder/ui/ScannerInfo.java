package com.basefinder.ui;

import com.basefinder.model.Severity;

/** A scanner's identity + live status, as the UI needs it (decoupled from the registry). */
public record ScannerInfo(String id, String name, String description, boolean enabled,
                          boolean healthy, double lastScanMs, int lastFindingCount,
                          String category, Severity worstSeverity) {
}
