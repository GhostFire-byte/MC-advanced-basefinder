package com.basefinder.ui;

/** Immutable rectangle in screen pixels. */
public record Rect(int x, int y, int w, int h) {

    public int right() {
        return x + w;
    }

    public int bottom() {
        return y + h;
    }

    public int centerX() {
        return x + w / 2;
    }

    public int centerY() {
        return y + h / 2;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    public Rect inset(int d) {
        return new Rect(x + d, y + d, Math.max(0, w - 2 * d), Math.max(0, h - 2 * d));
    }

    public Rect at(int nx, int ny) {
        return new Rect(nx, ny, w, h);
    }
}
