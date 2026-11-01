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
}
