package com.basefinder.ui;

import com.basefinder.config.ScanAggressiveness;
import com.basefinder.rules.Rule;

/**
 * Write seam for the interactive overlay. The bundled GUI calls these; a host
 * client can implement it to route changes into its own settings framework.
 * Implementations should persist via the host settings bridge after a change.
 */
public interface UiCommands {

    void setScannerEnabled(String scannerId, boolean enabled);

    void setConfidenceFloor(int percent);

    void setDetectionRadius(int chunks);

    void setRadarRange(int blocks);

    void setAggressiveness(ScanAggressiveness aggressiveness);

    void selectTheme(String themeName);

    void setFlatStyle(boolean flat);

    void setEsp(boolean outlines, boolean boxes, boolean labels, boolean icons);

    void setStatusPanelItem(String item, boolean enabled);

    void setOverlayEnabled(boolean enabled);

    void addRule(Rule rule);

    void removeRule(Rule rule);

    void addPresetRules();

    void save();
}
