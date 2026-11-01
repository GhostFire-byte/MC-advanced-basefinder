package com.basefinder.ui.view;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiCommands;
import com.basefinder.ui.UiModel;
import com.basefinder.ui.UiState;

/**
 * Top-level overlay. Two layers, both rendered inside Minecraft's frame:
 * <ul>
 *   <li>Passive HUD (radar, status panel, toasts, guidance) &mdash; always drawn
 *       when the overlay is enabled, never takes input (click-through).</li>
 *   <li>Interactive dashboard &mdash; drawn and takes input only in interaction
 *       mode, with a smooth open/close fade.</li>
 * </ul>
 * In interaction mode the radar, status panel and dashboard are draggable (and
 * the dashboard resizable); the layout/scale live in a shared {@link UiState}.
 */
public final class OverlayRoot {

    private enum Drag {NONE, STATUS, RADAR, DASH_MOVE, DASH_RESIZE}

    private final UiModel model;
    private final UiState state = new UiState();
    private final RadarView radar = new RadarView(state);
    private final StatusPanelView status = new StatusPanelView(state);
    private final ToastView toasts = new ToastView();
    private final GuidanceView guidance = new GuidanceView();
    private final DashboardView dashboard;

    private boolean interactionMode;
    private long lastNanos;

    private Drag drag = Drag.NONE;
    private double grabX;
    private double grabY;

    public OverlayRoot(UiModel model, UiCommands commands) {
        this.model = model;
        this.dashboard = new DashboardView(model, commands, state);
    }

    public UiState state() {
        return state;
    }

    public boolean interactionMode() {
        return interactionMode;
    }

    public void setInteractionMode(boolean on) {
        this.interactionMode = on;
        if (!on) {
            drag = Drag.NONE;
        }
    }

    public void toggleInteractionMode() {
        setInteractionMode(!interactionMode);
    }

    /** Draw the overlay. Safe to call every frame; cheap when disabled. */
    public void render(Renderer r) {
        animateFade();
        if (!model.overlayEnabled()) {
            return;
        }
        status.render(r, model);
        radar.render(r, model);
        guidance.render(r, model);
        toasts.render(r, model);
        if (state.dashFade > 0.02) {
            dashboard.render(r);
        }
    }

    private void animateFade() {
        long now = System.nanoTime();
        double dt = lastNanos == 0 ? 0.016 : (now - lastNanos) / 1.0e9;
        lastNanos = now;
        dt = Math.max(0.0, Math.min(0.1, dt));
        double target = interactionMode ? 1.0 : 0.0;
        state.dashFade += (target - state.dashFade) * Math.min(1.0, dt * 9.0);
        if (Math.abs(target - state.dashFade) < 0.01) {
            state.dashFade = target;
        }
    }

    /** Route input. Returns true if consumed (only possible in interaction mode). */
    public boolean handle(InputEvent e) {
        if (!interactionMode || !model.overlayEnabled()) {
            return false;
        }
        double x = e.mouseX();
        double y = e.mouseY();
        switch (e.type()) {
            case MOUSE_DOWN -> {
                return onMouseDown(e, x, y);
            }
            case MOUSE_DRAG -> {
                if (applyDrag(x, y)) {
                    return true;
                }
                return dashboard.handle(e);
            }
            case MOUSE_UP -> {
                if (drag != Drag.NONE) {
                    drag = Drag.NONE;
                    return true;
                }
                return dashboard.handle(e);
            }
            default -> {
                return dashboard.handle(e);
            }
        }
    }

    private boolean onMouseDown(InputEvent e, double x, double y) {
        if (dashboard.resizeHandle().contains(x, y)) {
            drag = Drag.DASH_RESIZE;
            return true;
        }
        if (dashboard.titleBar().contains(x, y)) {
            drag = Drag.DASH_MOVE;
            grabX = x - dashboard.window().x();
            grabY = y - dashboard.window().y();
            return true;
        }
        if (dashboard.window().contains(x, y)) {
            return dashboard.handle(e);
        }
        Rect rb = radar.lastBounds();
        if (rb.w() > 0 && rb.contains(x, y)) {
            drag = Drag.RADAR;
            grabX = x - rb.centerX();
            grabY = y - rb.centerY();
            return true;
        }
        Rect sb = status.bounds();
        if (sb.w() > 0 && sb.contains(x, y)) {
            drag = Drag.STATUS;
            grabX = x - sb.x();
            grabY = y - sb.y();
            return true;
        }
        return dashboard.handle(e);
    }

    private boolean applyDrag(double x, double y) {
        switch (drag) {
            case DASH_MOVE -> {
                state.dashX = (int) Math.round(x - grabX);
                state.dashY = (int) Math.round(y - grabY);
                return true;
            }
            case DASH_RESIZE -> {
                Rect w = dashboard.window();
                state.dashW = Math.max(360, (int) Math.round(x - w.x()));
                state.dashH = Math.max(240, (int) Math.round(y - w.y()));
                return true;
            }
            case RADAR -> {
                state.radarCx = (int) Math.round(x - grabX);
                state.radarCy = (int) Math.round(y - grabY);
                return true;
            }
            case STATUS -> {
                state.statusX = (int) Math.round(x - grabX);
                state.statusY = (int) Math.round(y - grabY);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
