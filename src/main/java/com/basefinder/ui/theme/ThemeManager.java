package com.basefinder.ui.theme;

import java.util.List;

/** Holds the active theme and style mode, and resolves presets by name. */
public final class ThemeManager {

    private Theme active = ThemePresets.night();
    private StyleMode styleMode = StyleMode.POLISHED;

    public Theme active() {
        return active;
    }

    public void setActive(Theme theme) {
        this.active = theme;
    }

    public void selectPreset(String name) {
        this.active = ThemePresets.byName(name);
    }

    public StyleMode styleMode() {
        return styleMode;
    }

    public void setStyleMode(StyleMode mode) {
        this.styleMode = mode;
    }

    public boolean polished() {
        return styleMode == StyleMode.POLISHED;
    }

    public List<Theme> presets() {
        return ThemePresets.all();
    }
}
