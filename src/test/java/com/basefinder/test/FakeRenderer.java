package com.basefinder.test;

import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;

/** Minimal no-op {@link Renderer} for headless UI smoke tests. */
public final class FakeRenderer implements Renderer {

    private final int w;
    private final int h;
    public int drawCalls;

    public FakeRenderer(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override public int screenWidth() { return w; }
    @Override public int screenHeight() { return h; }
    @Override public void fill(Rect rect, int argb) { drawCalls++; }
    @Override public void gradientVertical(Rect rect, int topArgb, int bottomArgb) { drawCalls++; }
    @Override public void outline(Rect rect, int argb, float thickness) { drawCalls++; }
    @Override public void line(double x1, double y1, double x2, double y2, int argb, float t) { drawCalls++; }
    @Override public void filledCircle(double cx, double cy, double radius, int argb) { drawCalls++; }
    @Override public void circle(double cx, double cy, double radius, int argb, float t) { drawCalls++; }
    @Override public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, int argb) { drawCalls++; }
    @Override public void text(String s, int x, int y, int argb, boolean shadow) { drawCalls++; }
    @Override public int textWidth(String s) { return s == null ? 0 : s.length() * 6; }
    @Override public int lineHeight() { return 9; }
    @Override public void pushClip(Rect rect) { }
    @Override public void popClip() { }
}
