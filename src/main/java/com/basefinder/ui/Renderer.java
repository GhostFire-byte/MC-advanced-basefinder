package com.basefinder.ui;

/**
 * Minimal 2D drawing surface the host client implements with its own renderer
 * (Blaze3D in Minecraft 26.2). The core only ever calls these primitives, so the
 * UI model is fully testable with a fake renderer and never touches Minecraft.
 */
public interface Renderer {

    int screenWidth();

    int screenHeight();

    void fill(Rect rect, int argb);

    /** Vertical gradient fill (top -> bottom). Implementations may approximate. */
    void gradientVertical(Rect rect, int topArgb, int bottomArgb);

    void outline(Rect rect, int argb, float thickness);

    void line(double x1, double y1, double x2, double y2, int argb, float thickness);

    void filledCircle(double cx, double cy, double radius, int argb);

    void circle(double cx, double cy, double radius, int argb, float thickness);

    void triangle(double x1, double y1, double x2, double y2, double x3, double y3, int argb);

    void text(String s, int x, int y, int argb, boolean shadow);

    int textWidth(String s);

    int lineHeight();

    /**
     * Draws text scaled by {@code scale} about its top-left origin. Hosts should
     * implement true scaling (e.g. a matrix scale); the default falls back to
     * unscaled text so non-scaling renderers still work.
     */
    default void textScaled(String s, int x, int y, int argb, boolean shadow, float scale) {
        text(s, x, y, argb, shadow);
    }

    /** Pixel width of {@code s} at {@code scale}. Default approximates from {@link #textWidth}. */
    default int textWidthScaled(String s, float scale) {
        return Math.round(textWidth(s) * scale);
    }

    /** Line height at {@code scale}. Default approximates from {@link #lineHeight}. */
    default int lineHeightScaled(float scale) {
        return Math.round(lineHeight() * scale);
    }

    void pushClip(Rect rect);

    void popClip();

    default void textCentered(String s, int cx, int y, int argb, boolean shadow) {
        text(s, cx - textWidth(s) / 2, y, argb, shadow);
    }

    default void textCenteredScaled(String s, int cx, int y, int argb, boolean shadow, float scale) {
        textScaled(s, cx - textWidthScaled(s, scale) / 2, y, argb, shadow, scale);
    }
}
