package com.basefinder.ui;

/** ARGB colour helpers (0xAARRGGBB). */
public final class UiColor {

    private UiColor() {
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int alpha(int c) {
        return (c >> 24) & 0xFF;
    }

    public static int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public static int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    /** Linear blend between two colours, t in [0,1]. */
    public static int lerp(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int aa = (int) (alpha(a) + (alpha(b) - alpha(a)) * t);
        int ar = (int) (red(a) + (red(b) - red(a)) * t);
        int ag = (int) (green(a) + (green(b) - green(a)) * t);
        int ab = (int) (blue(a) + (blue(b) - blue(a)) * t);
        return argb(aa, ar, ag, ab);
    }

    /** Colour for a confidence 0..100: red -> amber -> green. */
    public static int confidence(int pct) {
        if (pct < 50) {
            return lerp(rgb(220, 60, 60), rgb(230, 180, 50), pct / 50.0);
        }
        return lerp(rgb(230, 180, 50), rgb(70, 210, 110), (pct - 50) / 50.0);
    }
}
