package com.basefinder.ui;

import com.basefinder.ui.theme.Theme;

/**
 * Premium drawing helpers built on the minimal {@link Renderer} primitives:
 * rounded rectangles, frosted-glass panels with a vertical gradient and a top
 * sheen, soft borders and glows, and fit-to-width text. Everything degrades
 * gracefully on a non-polished renderer.
 */
public final class UiDraw {

    private UiDraw() {
    }

    private static int clampRadius(Rect b, int rad) {
        return Math.max(0, Math.min(rad, Math.min(b.w(), b.h()) / 2));
    }

    /** Solid rounded rectangle. */
    public static void roundedRect(Renderer r, Rect b, int rad, int argb) {
        rad = clampRadius(b, rad);
        if (rad <= 0) {
            r.fill(b, argb);
            return;
        }
        // Non-overlapping body (so a translucent fill never double-blends).
        r.fill(new Rect(b.x() + rad, b.y(), b.w() - 2 * rad, b.h()), argb);
        r.fill(new Rect(b.x(), b.y() + rad, rad, b.h() - 2 * rad), argb);
        r.fill(new Rect(b.right() - rad, b.y() + rad, rad, b.h() - 2 * rad), argb);
        // Corner arcs, each clipped to its own corner square so they don't overlap.
        cornerArc(r, b.x(), b.y(), rad, b.x() + rad, b.y() + rad, argb);
        cornerArc(r, b.right() - rad, b.y(), rad, b.right() - rad, b.y() + rad, argb);
        cornerArc(r, b.x(), b.bottom() - rad, rad, b.x() + rad, b.bottom() - rad, argb);
        cornerArc(r, b.right() - rad, b.bottom() - rad, rad, b.right() - rad, b.bottom() - rad, argb);
    }

    /** Fills one rounded corner: a quarter circle clipped to the rad x rad corner cell. */
    private static void cornerArc(Renderer r, int cellX, int cellY, int rad, double cx, double cy,
                                  int argb) {
        r.pushClip(new Rect(cellX, cellY, rad, rad));
        r.filledCircle(cx, cy, rad, argb);
        r.popClip();
    }

    /** Rounded rectangle with a vertical top-&gt;bottom gradient. */
    public static void roundedGradient(Renderer r, Rect b, int rad, int top, int bottom) {
        rad = clampRadius(b, rad);
        if (rad <= 0) {
            r.gradientVertical(b, top, bottom);
            return;
        }
        r.gradientVertical(new Rect(b.x() + rad, b.y(), b.w() - 2 * rad, b.h()), top, bottom);
        r.gradientVertical(new Rect(b.x(), b.y() + rad, rad, b.h() - 2 * rad), top, bottom);
        r.gradientVertical(new Rect(b.right() - rad, b.y() + rad, rad, b.h() - 2 * rad), top, bottom);
        cornerArc(r, b.x(), b.y(), rad, b.x() + rad, b.y() + rad, top);
        cornerArc(r, b.right() - rad, b.y(), rad, b.right() - rad, b.y() + rad, top);
        cornerArc(r, b.x(), b.bottom() - rad, rad, b.x() + rad, b.bottom() - rad, bottom);
        cornerArc(r, b.right() - rad, b.bottom() - rad, rad, b.right() - rad, b.bottom() - rad, bottom);
    }

    /** Soft rounded border traced along the edges and quarter-arc corners (no full circles). */
    public static void roundedOutline(Renderer r, Rect b, int rad, int argb, float thickness) {
        rad = clampRadius(b, rad);
        r.line(b.x() + rad, b.y(), b.right() - rad, b.y(), argb, thickness);
        r.line(b.x() + rad, b.bottom(), b.right() - rad, b.bottom(), argb, thickness);
        r.line(b.x(), b.y() + rad, b.x(), b.bottom() - rad, argb, thickness);
        r.line(b.right(), b.y() + rad, b.right(), b.bottom() - rad, argb, thickness);
        if (rad > 1) {
            arc(r, b.x() + rad, b.y() + rad, rad, 180, 270, argb, thickness);
            arc(r, b.right() - rad, b.y() + rad, rad, 270, 360, argb, thickness);
            arc(r, b.right() - rad, b.bottom() - rad, rad, 0, 90, argb, thickness);
            arc(r, b.x() + rad, b.bottom() - rad, rad, 90, 180, argb, thickness);
        }
    }

    private static void arc(Renderer r, double cx, double cy, double rad, double startDeg,
                            double endDeg, int argb, float thickness) {
        int seg = Math.max(2, (int) Math.round(rad / 2.0));
        double prevX = cx + rad * Math.cos(Math.toRadians(startDeg));
        double prevY = cy + rad * Math.sin(Math.toRadians(startDeg));
        for (int i = 1; i <= seg; i++) {
            double a = Math.toRadians(startDeg + (endDeg - startDeg) * i / seg);
            double nx = cx + rad * Math.cos(a);
            double ny = cy + rad * Math.sin(a);
            r.line(prevX, prevY, nx, ny, argb, thickness);
            prevX = nx;
            prevY = ny;
        }
    }

    /**
     * Frosted-glass panel: translucent rounded body with a vertical gradient, a
     * subtle top sheen highlight and a soft border. {@code alphaScale} 0..1 fades
     * the whole panel (for open/close animations).
     */
    public static void glassPanel(Renderer r, Rect b, Theme th, boolean polished, int rad,
                                  double alphaScale) {
        int a = (int) Math.round(th.opacity() * clamp01(alphaScale));
        if (a <= 0) {
            return;
        }
        if (polished) {
            roundedGradient(r, b, rad, UiColor.withAlpha(th.panelTop(), a),
                    UiColor.withAlpha(th.panelBottom(), a));
        } else {
            roundedRect(r, b, rad, UiColor.withAlpha(th.background(), a));
        }
        // Top sheen: a faint light gradient fading downward gives the glass feel.
        int sheenH = Math.max(3, b.h() / 6);
        int sheenA = (int) Math.round(34 * clamp01(alphaScale));
        r.gradientVertical(new Rect(b.x() + rad, b.y() + 1, Math.max(0, b.w() - 2 * rad), sheenH),
                UiColor.withAlpha(0xFFFFFF, sheenA), UiColor.withAlpha(0xFFFFFF, 0));
        roundedOutline(r, b, rad, UiColor.withAlpha(th.border(),
                (int) Math.round(220 * clamp01(alphaScale))), 1f);
    }

    /** A soft outer glow ring (e.g. hover/focus) drawn just outside {@code b}. */
    public static void glow(Renderer r, Rect b, int rad, int argb, int rings) {
        for (int i = 1; i <= rings; i++) {
            int a = Math.max(0, UiColor.alpha(argb) - i * (UiColor.alpha(argb) / (rings + 1)));
            Rect g = new Rect(b.x() - i, b.y() - i, b.w() + 2 * i, b.h() + 2 * i);
            roundedOutline(r, g, rad + i, UiColor.withAlpha(argb, a), 1f);
        }
    }

    /** Soft drop shadow: concentric translucent-black rounded outlines expanding from {@code b}. */
    public static void shadow(Renderer r, Rect b, int rad, int layers, double alphaScale) {
        for (int i = layers; i >= 1; i--) {
            int a = (int) Math.round(10 * alphaScale);
            Rect s = new Rect(b.x() - i, b.y() - i + 3, b.w() + 2 * i, b.h() + 2 * i);
            roundedRect(r, s, rad + i, UiColor.withAlpha(0x000000, a));
        }
    }

    /** WindUI-style flat window: soft shadow, solid rounded body, hairline border. */
    public static void windowPanel(Renderer r, Rect b, Theme th, int rad, double alphaScale) {
        int a = (int) Math.round(th.opacity() * clamp01(alphaScale));
        if (a <= 0) {
            return;
        }
        shadow(r, b, rad, 7, alphaScale);
        roundedRect(r, b, rad, UiColor.withAlpha(th.background(), a));
        roundedOutline(r, b, rad, scaleAlpha(th.border(), alphaScale), 1f);
    }

    /**
     * WindUI-style flat card: a faint fill (panel colour) over the window, plus a
     * hairline border. {@code hoverBoost} 0..1 lightens it on hover.
     */
    public static void card(Renderer r, Rect b, Theme th, int rad, double hoverBoost,
                            double alphaScale) {
        int baseA = (int) Math.round((120 + 40 * clamp01(hoverBoost)) * clamp01(alphaScale));
        roundedRect(r, b, rad, UiColor.withAlpha(th.panelTop(), baseA));
        roundedOutline(r, b, rad, scaleAlpha(th.border(), alphaScale), 1f);
    }

    private static int scaleAlpha(int argb, double alphaScale) {
        return UiColor.withAlpha(argb, (int) Math.round(UiColor.alpha(argb) * clamp01(alphaScale)));
    }

    /**
     * Draws {@code label} centred in {@code b}, shrinking the scale (down to
     * {@code minScale}) so it always fits the width, and ellipsising only as a
     * last resort. Vertically centred for the given base line height.
     */
    public static void fitText(Renderer r, String label, Rect b, int argb, float baseScale,
                               float minScale) {
        int pad = 6;
        int avail = Math.max(1, b.w() - pad * 2);
        float scale = baseScale;
        while (scale > minScale && r.textWidthScaled(label, scale) > avail) {
            scale -= 0.05f;
        }
        String draw = label;
        if (r.textWidthScaled(draw, scale) > avail) {
            while (draw.length() > 1 && r.textWidthScaled(draw + "\u2026", scale) > avail) {
                draw = draw.substring(0, draw.length() - 1);
            }
            draw = draw + "\u2026";
        }
        int ty = b.centerY() - r.lineHeightScaled(scale) / 2;
        r.textCenteredScaled(draw, b.centerX(), ty, argb, false, scale);
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }
}
