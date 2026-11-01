package com.basefinder.mc;

import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Implements the core {@link Renderer} seam with Minecraft 26.2's
 * {@link GuiGraphics}. 2D only &mdash; radar/HUD/panels. World-space 3D ESP is
 * drawn separately (see {@link EspRenderer}) because it needs the camera matrices.
 *
 * <p>NOTE (integration): {@code GuiGraphics} lacks native circle/line/triangle
 * primitives, so these are approximated with filled quads. If the client exposes
 * a Blaze3D immediate buffer, replace the approximations with real primitives.
 */
public final class BlazeRenderer implements Renderer {

    private final GuiGraphics g;
    private final Font font;

    public BlazeRenderer(GuiGraphics graphics) {
        this.g = graphics;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public int screenWidth() {
        return g.guiWidth();
    }

    @Override
    public int screenHeight() {
        return g.guiHeight();
    }

    @Override
    public void fill(Rect r, int argb) {
        g.fill(r.x(), r.y(), r.right(), r.bottom(), argb);
    }

    @Override
    public void gradientVertical(Rect r, int topArgb, int bottomArgb) {
        g.fillGradient(r.x(), r.y(), r.right(), r.bottom(), topArgb, bottomArgb);
    }

    @Override
    public void outline(Rect r, int argb, float thickness) {
        int t = Math.max(1, (int) thickness);
        g.fill(r.x(), r.y(), r.right(), r.y() + t, argb);
        g.fill(r.x(), r.bottom() - t, r.right(), r.bottom(), argb);
        g.fill(r.x(), r.y(), r.x() + t, r.bottom(), argb);
        g.fill(r.right() - t, r.y(), r.right(), r.bottom(), argb);
    }

    @Override
    public void line(double x1, double y1, double x2, double y2, int argb, float thickness) {
        // Approximate with a thin quad along the segment.
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.max(1e-3, Math.sqrt(dx * dx + dy * dy));
        int steps = (int) len;
        int t = Math.max(1, (int) thickness);
        for (int i = 0; i <= steps; i++) {
            int px = (int) (x1 + dx * i / steps);
            int py = (int) (y1 + dy * i / steps);
            g.fill(px, py, px + t, py + t, argb);
        }
    }

    @Override
    public void filledCircle(double cx, double cy, double radius, int argb) {
        int r = (int) Math.ceil(radius);
        for (int dy = -r; dy <= r; dy++) {
            int span = (int) Math.sqrt(Math.max(0, radius * radius - dy * dy));
            g.fill((int) (cx - span), (int) (cy + dy), (int) (cx + span), (int) (cy + dy) + 1, argb);
        }
    }

    @Override
    public void circle(double cx, double cy, double radius, int argb, float thickness) {
        int segments = 48;
        double prevX = cx + radius, prevY = cy;
        for (int i = 1; i <= segments; i++) {
            double a = i / (double) segments * Math.PI * 2;
            double x = cx + Math.cos(a) * radius;
            double y = cy + Math.sin(a) * radius;
            line(prevX, prevY, x, y, argb, thickness);
            prevX = x;
            prevY = y;
        }
    }

    @Override
    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, int argb) {
        // Scanline fill of the triangle (small HUD arrows), good enough without a buffer.
        int minY = (int) Math.floor(Math.min(y1, Math.min(y2, y3)));
        int maxY = (int) Math.ceil(Math.max(y1, Math.max(y2, y3)));
        for (int y = minY; y <= maxY; y++) {
            Double xa = edgeX(x1, y1, x2, y2, y);
            Double xb = edgeX(x2, y2, x3, y3, y);
            Double xc = edgeX(x3, y3, x1, y1, y);
            double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
            for (Double x : new Double[]{xa, xb, xc}) {
                if (x != null) {
                    lo = Math.min(lo, x);
                    hi = Math.max(hi, x);
                }
            }
            if (lo <= hi) {
                g.fill((int) lo, y, (int) hi + 1, y + 1, argb);
            }
        }
    }

    private static Double edgeX(double x1, double y1, double x2, double y2, int y) {
        if ((y < y1 && y < y2) || (y > y1 && y > y2) || y1 == y2) {
            return null;
        }
        double t = (y - y1) / (y2 - y1);
        return x1 + (x2 - x1) * t;
    }

    @Override
    public void text(String s, int x, int y, int argb, boolean shadow) {
        g.drawString(font, s, x, y, argb, shadow);
    }

    @Override
    public void textScaled(String s, int x, int y, int argb, boolean shadow, float scale) {
        if (scale == 1.0f) {
            g.drawString(font, s, x, y, argb, shadow);
            return;
        }
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0f);
        g.drawString(font, s, 0, 0, argb, shadow);
        pose.popPose();
    }

    @Override
    public int textWidthScaled(String s, float scale) {
        return Math.round(font.width(s) * scale);
    }

    @Override
    public int lineHeightScaled(float scale) {
        return Math.round(font.lineHeight * scale);
    }

    @Override
    public int textWidth(String s) {
        return font.width(s);
    }

    @Override
    public int lineHeight() {
        return font.lineHeight;
    }

    @Override
    public void pushClip(Rect r) {
        g.enableScissor(r.x(), r.y(), r.right(), r.bottom());
    }

    @Override
    public void popClip() {
        g.disableScissor();
    }
}
