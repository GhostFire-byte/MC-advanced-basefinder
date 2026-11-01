package com.basefinder.ui;

/**
 * Mutable, session-scoped UI layout + animation state shared by the overlay
 * views: the global UI scale, user-dragged panel positions / sizes, and the
 * dashboard open/close fade. Null position fields mean "use the default anchor".
 */
public final class UiState {

    /** Global size multiplier for panels and text (1.0 = default). */
    public double uiScale = 1.0;

    // Compact status panel (top-left anchor by default).
    public Integer statusX;
    public Integer statusY;

    // Radar centre (top-right anchor by default).
    public Integer radarCx;
    public Integer radarCy;

    // Dashboard window (centred by default).
    public Integer dashX;
    public Integer dashY;
    public Integer dashW;
    public Integer dashH;

    /** Dashboard open/close fade, 0..1, eased by the overlay each frame. */
    public double dashFade;

    public float textScale() {
        return (float) uiScale;
    }

    public int scaled(int base) {
        return (int) Math.round(base * uiScale);
    }

    /** Clears all user layout overrides (back to default anchors and scale). */
    public void resetLayout() {
        uiScale = 1.0;
        statusX = statusY = null;
        radarCx = radarCy = null;
        dashX = dashY = dashW = dashH = null;
    }

    /** Writes the layout + scale into a flat string map (for host persistence). */
    public void exportTo(java.util.Map<String, String> m) {
        m.put("uiScale", Double.toString(uiScale));
        putInt(m, "statusX", statusX);
        putInt(m, "statusY", statusY);
        putInt(m, "radarCx", radarCx);
        putInt(m, "radarCy", radarCy);
        putInt(m, "dashX", dashX);
        putInt(m, "dashY", dashY);
        putInt(m, "dashW", dashW);
        putInt(m, "dashH", dashH);
    }

    /** Restores the layout + scale from a flat string map. */
    public void importFrom(java.util.Map<String, String> m) {
        if (m == null) {
            return;
        }
        uiScale = parseDouble(m.get("uiScale"), uiScale);
        statusX = parseInt(m.get("statusX"));
        statusY = parseInt(m.get("statusY"));
        radarCx = parseInt(m.get("radarCx"));
        radarCy = parseInt(m.get("radarCy"));
        dashX = parseInt(m.get("dashX"));
        dashY = parseInt(m.get("dashY"));
        dashW = parseInt(m.get("dashW"));
        dashH = parseInt(m.get("dashH"));
    }

    private static void putInt(java.util.Map<String, String> m, String k, Integer v) {
        if (v != null) {
            m.put(k, Integer.toString(v));
        }
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double parseDouble(String s, double fallback) {
        if (s == null || s.isEmpty()) {
            return fallback;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
