package com.basefinder.preview;

import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Deque;
import java.util.LinkedList;

/**
 * A {@link Renderer} backed by Java2D, so the real BaseFinder overlay can be
 * previewed in a desktop window with no Minecraft. Only used by the preview app.
 */
final class Java2dRenderer implements Renderer {

    private Graphics2D g;
    private int width;
    private int height;
    private final Font baseFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Deque<Shape> clipStack = new LinkedList<>();

    void begin(Graphics2D graphics, int w, int h) {
        this.g = graphics;
        this.width = w;
        this.height = h;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(baseFont);
    }

    private static Color color(int argb) {
        return new Color(argb, true);
    }

    @Override public int screenWidth() { return width; }

    @Override public int screenHeight() { return height; }

    @Override
    public void fill(Rect r, int argb) {
        g.setColor(color(argb));
        g.fillRect(r.x(), r.y(), r.w(), r.h());
    }

    @Override
    public void gradientVertical(Rect r, int topArgb, int bottomArgb) {
        if (r.h() <= 0 || r.w() <= 0) {
            return;
        }
        g.setPaint(new GradientPaint(r.x(), r.y(), color(topArgb), r.x(), r.bottom(), color(bottomArgb)));
        g.fillRect(r.x(), r.y(), r.w(), r.h());
        g.setPaint(null);
    }

    @Override
    public void outline(Rect r, int argb, float thickness) {
        g.setColor(color(argb));
        g.setStroke(new BasicStroke(Math.max(1f, thickness)));
        g.drawRect(r.x(), r.y(), r.w(), r.h());
    }

    @Override
    public void line(double x1, double y1, double x2, double y2, int argb, float thickness) {
        g.setColor(color(argb));
        g.setStroke(new BasicStroke(Math.max(1f, thickness)));
        g.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
    }

    @Override
    public void filledCircle(double cx, double cy, double radius, int argb) {
        g.setColor(color(argb));
        int d = (int) Math.round(radius * 2);
        g.fillOval((int) Math.round(cx - radius), (int) Math.round(cy - radius), d, d);
    }

    @Override
    public void circle(double cx, double cy, double radius, int argb, float thickness) {
        g.setColor(color(argb));
        g.setStroke(new BasicStroke(Math.max(1f, thickness)));
        int d = (int) Math.round(radius * 2);
        g.drawOval((int) Math.round(cx - radius), (int) Math.round(cy - radius), d, d);
    }

    @Override
    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, int argb) {
        Polygon p = new Polygon();
        p.addPoint((int) Math.round(x1), (int) Math.round(y1));
        p.addPoint((int) Math.round(x2), (int) Math.round(y2));
        p.addPoint((int) Math.round(x3), (int) Math.round(y3));
        g.setColor(color(argb));
        g.fillPolygon(p);
    }

    @Override
    public void text(String s, int x, int y, int argb, boolean shadow) {
        drawText(s, x, y, argb, shadow, baseFont);
    }

    @Override
    public void textScaled(String s, int x, int y, int argb, boolean shadow, float scale) {
        drawText(s, x, y, argb, shadow, baseFont.deriveFont(baseFont.getSize2D() * scale));
    }

    private void drawText(String s, int x, int y, int argb, boolean shadow, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int baseline = y + fm.getAscent();
        if (shadow) {
            g.setColor(new Color(0, 0, 0, 160));
            g.drawString(s, x + 1, baseline + 1);
        }
        g.setColor(color(argb));
        g.drawString(s, x, baseline);
        g.setFont(baseFont);
    }

    @Override
    public int textWidth(String s) {
        return g.getFontMetrics(baseFont).stringWidth(s);
    }

    @Override
    public int textWidthScaled(String s, float scale) {
        return g.getFontMetrics(baseFont.deriveFont(baseFont.getSize2D() * scale)).stringWidth(s);
    }

    @Override
    public int lineHeight() {
        return g.getFontMetrics(baseFont).getHeight();
    }

    @Override
    public int lineHeightScaled(float scale) {
        return g.getFontMetrics(baseFont.deriveFont(baseFont.getSize2D() * scale)).getHeight();
    }

    @Override
    public void pushClip(Rect r) {
        clipStack.push(g.getClip());
        g.clipRect(r.x(), r.y(), r.w(), r.h());
    }

    @Override
    public void popClip() {
        if (!clipStack.isEmpty()) {
            g.setClip(clipStack.pop());
        }
    }
}
