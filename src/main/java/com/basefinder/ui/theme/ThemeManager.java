package com.basefinder.ui.theme;

import com.basefinder.ui.GuiStyle;

import java.util.List;

/** Holds the active theme and style mode, and resolves presets by name. */
public final class ThemeManager {

    private Theme active = ThemePresets.night();
    private StyleMode styleMode = StyleMode.POLISHED;
    private GuiStyle guiStyle = GuiStyle.WINDUI;

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

    public GuiStyle guiStyle() {
        return guiStyle;
    }

    public void setGuiStyle(GuiStyle style) {
        this.guiStyle = style;
    }

    public boolean imgui() {
        return guiStyle == GuiStyle.IMGUI;
    }

    public List<Theme> presets() {
        return ThemePresets.all();
    }
}
