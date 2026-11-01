package com.basefinder.ui.view;

import com.basefinder.model.BaseCandidate;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.UiState;
import com.basefinder.world.Vec3d;

/**
 * Circular radar centred on the player. Player always faces up; markers rotate
 * with the camera. Draggable and scalable via {@link UiState}; frosted-glass
 * disc with a soft ring.
 */
public final class RadarView {

    private static final int BASE_RADIUS = 58;

    private final UiState state;
    private Rect last = new Rect(0, 0, 0, 0);

    public RadarView(UiState state) {
        this.state = state;
    }

    /** Bounds from the last render, for drag hit-testing. */
    public Rect lastBounds() {
        return last;
    }

    public int radius() {
        return Math.max(28, state.scaled(BASE_RADIUS));
    }

    public int centerX(Renderer r) {
        return state.radarCx != null ? state.radarCx : r.screenWidth() - radius() - 16;
    }

    public int centerY(Renderer r) {
        return state.radarCy != null ? state.radarCy : radius() + 16;
    }

    public Rect bounds(Renderer r) {
        int rad = radius();
        return new Rect(centerX(r) - rad, centerY(r) - rad, rad * 2, rad * 2);
    }

    public void render(Renderer r, UiModel model) {
        if (!model.globals().radarEnabled()) {
            return;
        }
        int rad = radius();
        int cx = centerX(r);
        int cy = centerY(r);
        this.last = new Rect(cx - rad, cy - rad, rad * 2, rad * 2);
        var th = model.theme().active();

        // Frosted disc: soft outer glow, translucent body, bright ring.
        r.filledCircle(cx, cy, rad + 2, UiColor.withAlpha(th.primary(), 40));
        r.filledCircle(cx, cy, rad, UiColor.withAlpha(th.background(), Math.min(235, th.opacity())));
        r.circle(cx, cy, rad, UiColor.withAlpha(th.border(), 230), 1.5f);
        r.circle(cx, cy, rad * 0.66, UiColor.withAlpha(0xFFFFFF, 18), 1f);
        r.line(cx - rad, cy, cx + rad, cy, UiColor.withAlpha(0xFFFFFF, 34), 1f);
        r.line(cx, cy - rad, cx, cy + rad, UiColor.withAlpha(0xFFFFFF, 34), 1f);

        Vec3d p = model.playerPos();
        float yaw = (float) Math.toRadians(model.playerYaw());
        int range = model.globals().radarRange();
        double dotScale = state.uiScale;
        for (BaseCandidate c : model.visibleCandidates()) {
            double dx = c.center().x() + 0.5 - p.x();
            double dz = c.center().z() + 0.5 - p.z();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > range) {
                continue;
            }
            double worldAngle = Math.atan2(dx, dz);
            double rel = worldAngle - yaw;
            double rr = (dist / range) * rad;
            double sx = cx + Math.sin(rel) * rr;
            double sy = cy - Math.cos(rel) * rr;
            int color = c.colorOverride() != null ? c.colorOverride() : UiColor.confidence(c.fusedConfidence());
            r.filledCircle(sx, sy, (c.pinned() ? 3.2 : 2.2) * dotScale, color);
            if (c.pinned()) {
                r.circle(sx, sy, 4.5 * dotScale, th.accent(), 1f);
            }
        }
        // Player marker (always centre, facing up).
        double s = dotScale;
        r.triangle(cx, cy - 5 * s, cx - 3.5 * s, cy + 4 * s, cx + 3.5 * s, cy + 4 * s, th.accent());
    }
}
