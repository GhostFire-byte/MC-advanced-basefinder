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
        list.add(imguiClassic());
        list.add(imguiDark());
        list.add(imguiCherry());
        list.add(imguiEmerald());
        list.add(imguiAmber());
        list.add(imguiLight());
        list.add(imguiPhobia());
        list.add(imguiNebula());
        return list;
    }

    /** Theme names that belong to the Dear-ImGui family (so the ImGui skin is auto-selected). */
    public static boolean isImguiFamily(String name) {
        return name != null && (name.startsWith("ImGui") || name.equals("Phobia (8)")
                || name.equals("Nebula (19)"));
    }

    // --- ImGui-style flat themes (gray frames + accent), mirroring the collection ----
    private static Theme imgui(String name, int accent, int bg, int frame, int text, int dim) {
        Theme t = new Theme(name, accent, accent, accent, bg, frame, frame, text, dim,
                UiColor.argb(110, 110, 114, 128));
        t.setOpacity(248);
        return t;
    }

    public static Theme imguiClassic() {
        return imgui("ImGui Classic", UiColor.rgb(66, 150, 250), UiColor.rgb(20, 20, 23),
                UiColor.rgb(41, 42, 48), UiColor.rgb(236, 236, 240), UiColor.rgb(140, 142, 152));
    }

    public static Theme imguiDark() {
        return imgui("ImGui Dark", UiColor.rgb(90, 180, 170), UiColor.rgb(16, 16, 18),
                UiColor.rgb(33, 34, 38), UiColor.rgb(228, 230, 232), UiColor.rgb(128, 130, 138));
    }

    public static Theme imguiCherry() {
        return imgui("ImGui Cherry", UiColor.rgb(233, 69, 96), UiColor.rgb(26, 18, 24),
                UiColor.rgb(48, 34, 44), UiColor.rgb(240, 228, 234), UiColor.rgb(170, 140, 155));
    }

    public static Theme imguiEmerald() {
        return imgui("ImGui Emerald", UiColor.rgb(46, 204, 113), UiColor.rgb(16, 22, 19),
                UiColor.rgb(30, 40, 35), UiColor.rgb(226, 238, 230), UiColor.rgb(130, 150, 138));
    }

    public static Theme imguiAmber() {
        return imgui("ImGui Amber", UiColor.rgb(245, 166, 35), UiColor.rgb(24, 20, 14),
                UiColor.rgb(44, 38, 28), UiColor.rgb(240, 234, 220), UiColor.rgb(166, 156, 132));
    }

    public static Theme imguiLight() {
        return imgui("ImGui Light", UiColor.rgb(66, 150, 250), UiColor.rgb(222, 224, 228),
                UiColor.rgb(244, 245, 248), UiColor.rgb(28, 30, 36), UiColor.rgb(110, 114, 126));
    }

    /** Menu 8 "Phobia": minimalist near-black with a clean white accent. */
    public static Theme imguiPhobia() {
        return imgui("Phobia (8)", UiColor.rgb(236, 236, 240), UiColor.rgb(14, 14, 16),
                UiColor.rgb(26, 26, 30), UiColor.rgb(236, 236, 240), UiColor.rgb(120, 120, 128));
    }

    /** Menu 19: dark navy with a periwinkle-purple accent (142,132,255). */
    public static Theme imguiNebula() {
        return imgui("Nebula (19)", UiColor.rgb(142, 132, 255), UiColor.rgb(18, 18, 25),
                UiColor.rgb(21, 21, 29), UiColor.rgb(235, 236, 245), UiColor.rgb(104, 104, 120));
    }

    public static Theme byName(String name) {
        for (Theme t : all()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return night();
    }

    /** Default: a WindUI-style deep "Night" indigo on near-black, flat with an indigo accent. */
    public static Theme night() {
        Theme t = new Theme("Night",
                UiColor.rgb(99, 102, 241),   // primary  (indigo-500 accent)
                UiColor.rgb(129, 140, 248),  // secondary(indigo-400)
                UiColor.rgb(129, 140, 248),  // accent
                UiColor.rgb(16, 17, 28),     // background (window fill)
                UiColor.rgb(28, 30, 46),     // panelTop  (card fill, flat)
                UiColor.rgb(24, 26, 40),     // panelBottom
                UiColor.rgb(236, 238, 248),  // text
                UiColor.rgb(148, 154, 178),  // textDim (placeholder/icon)
                UiColor.argb(46, 150, 158, 210)); // border (hairline, translucent)
        t.setOpacity(246);
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
