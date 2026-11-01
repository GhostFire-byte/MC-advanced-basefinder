package com.basefinder.ui.theme;

import com.basefinder.ui.UiColor;

import java.util.ArrayList;
import java.util.List;

/** Five ready-made themes; "Premium" is the default. */
public final class ThemePresets {

    private ThemePresets() {
    }

    public static List<Theme> all() {
        List<Theme> list = new ArrayList<>();
        list.add(night());
        list.add(premium());
        list.add(midnightGlass());
        list.add(highContrast());
        list.add(neon());
        list.add(ocean());
        return list;
    }

    public static Theme byName(String name) {
        for (Theme t : all()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return night();
    }

    /** Default: a deep night-blue frosted glass with a smooth indigo gradient. */
    public static Theme night() {
        Theme t = new Theme("Night",
                UiColor.rgb(129, 140, 248), UiColor.rgb(94, 234, 212), UiColor.rgb(250, 204, 120),
                UiColor.argb(225, 15, 17, 30),
                UiColor.argb(225, 38, 42, 78), UiColor.argb(230, 15, 17, 34),
                UiColor.rgb(235, 238, 250), UiColor.rgb(150, 156, 184), UiColor.argb(150, 120, 132, 240));
        t.setOpacity(225);
        return t;
    }

    public static Theme premium() {
        return new Theme("Premium",
                UiColor.rgb(120, 110, 255), UiColor.rgb(80, 200, 220), UiColor.rgb(255, 180, 90),
                UiColor.argb(235, 18, 18, 28),
                UiColor.argb(235, 40, 36, 70), UiColor.argb(235, 20, 18, 36),
                UiColor.rgb(240, 240, 250), UiColor.rgb(160, 160, 185), UiColor.argb(180, 130, 120, 255));
    }

    public static Theme midnightGlass() {
        return new Theme("Midnight Glass",
                UiColor.rgb(90, 140, 230), UiColor.rgb(120, 180, 255), UiColor.rgb(90, 220, 200),
                UiColor.argb(200, 10, 14, 22),
                UiColor.argb(200, 24, 32, 50), UiColor.argb(200, 12, 16, 28),
                UiColor.rgb(230, 236, 245), UiColor.rgb(150, 160, 180), UiColor.argb(160, 90, 140, 230));
    }

    public static Theme highContrast() {
        return new Theme("High Contrast",
                UiColor.rgb(255, 255, 0), UiColor.rgb(0, 255, 255), UiColor.rgb(255, 80, 80),
                UiColor.argb(255, 0, 0, 0),
                UiColor.argb(255, 20, 20, 20), UiColor.argb(255, 0, 0, 0),
                UiColor.rgb(255, 255, 255), UiColor.rgb(200, 200, 200), UiColor.rgb(255, 255, 255));
    }

    public static Theme neon() {
        return new Theme("Neon",
                UiColor.rgb(255, 60, 200), UiColor.rgb(60, 240, 255), UiColor.rgb(180, 255, 60),
                UiColor.argb(235, 12, 10, 24),
                UiColor.argb(235, 34, 12, 48), UiColor.argb(235, 14, 10, 28),
                UiColor.rgb(245, 235, 255), UiColor.rgb(170, 150, 200), UiColor.argb(200, 255, 60, 200));
    }

    public static Theme ocean() {
        return new Theme("Ocean",
                UiColor.rgb(40, 160, 200), UiColor.rgb(60, 200, 180), UiColor.rgb(250, 200, 100),
                UiColor.argb(230, 10, 24, 32),
                UiColor.argb(230, 16, 44, 58), UiColor.argb(230, 10, 26, 36),
                UiColor.rgb(225, 240, 245), UiColor.rgb(140, 175, 185), UiColor.argb(180, 40, 160, 200));
    }
}
