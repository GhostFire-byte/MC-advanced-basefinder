package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.theme.ThemeManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** WindUI-style slider: label with a value pill, an accent-filled track and a round knob. */
public final class Slider extends Component {

    private final String label;
    private final int min;
    private final int max;
    private final Supplier<Integer> getter;
    private final Consumer<Integer> setter;
    private boolean dragging;

    public Slider(String label, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        this.label = label;
        this.min = min;
        this.max = Math.max(min + 1, max);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        var th = theme.active();
        int value = clamp(getter.get());
        if (theme.imgui()) {
            // ImGui slider: label above, a framed track with a grab and centered value.
            int labelH = r.lineHeight();
            r.text(label, bounds.x(), bounds.y(), th.text(), true);
            int fy = bounds.y() + labelH + 3;
            Rect frame = new Rect(bounds.x(), fy, bounds.w(), Math.max(14, bounds.bottom() - fy));
            UiDraw.roundedRect(r, frame, 2, th.panelTop());
            UiDraw.roundedOutline(r, frame, 2, UiColor.withAlpha(th.border(), 200), 1f);
            double tt = (value - min) / (double) (max - min);
            int grabW = Math.max(8, frame.w() / 12);
            int gx = frame.x() + (int) ((frame.w() - grabW) * tt);
            UiDraw.roundedRect(r, new Rect(gx, frame.y() + 1, grabW, frame.h() - 2), 2,
                    UiColor.withAlpha(th.accent(), 235));
            String vs = String.valueOf(value);
            r.text(vs, frame.centerX() - r.textWidth(vs) / 2, frame.centerY() - r.lineHeight() / 2,
                    th.text(), true);
            return;
        }
        r.text(label, bounds.x(), bounds.y(), th.text(), true);

        // Value pill (rounded) on the right.
        String vs = String.valueOf(value);
        int pw = Math.max(26, r.textWidth(vs) + 16);
        Rect pill = new Rect(bounds.right() - pw, bounds.y() - 2, pw, 18);
        UiDraw.roundedRect(r, pill, 9, UiColor.withAlpha(th.accent(), 40));
        UiDraw.roundedOutline(r, pill, 9, UiColor.withAlpha(th.text(), 36), 1f);
        r.text(vs, pill.centerX() - r.textWidth(vs) / 2, pill.y() + 4, th.text(), true);

        int trackY = bounds.y() + bounds.h() - 8;
        Rect track = new Rect(bounds.x(), trackY, bounds.w(), 5);
        UiDraw.roundedRect(r, track, 2, UiColor.withAlpha(th.text(), 28));
        double t = (value - min) / (double) (max - min);
        int fillW = (int) (bounds.w() * t);
        UiDraw.roundedRect(r, new Rect(bounds.x(), trackY, Math.max(4, fillW), 5), 2, th.accent());
        double knobX = bounds.x() + fillW;
        r.filledCircle(knobX, trackY + 2.5, 6, UiColor.withAlpha(0x000000, 50));
        r.filledCircle(knobX, trackY + 2.5, 5.5, UiColor.rgb(245, 246, 252));
    }

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (!visible) {
            return false;
        }
        switch (e.type()) {
            case MOUSE_DOWN -> {
                if (contains(e.mouseX(), e.mouseY())) {
                    dragging = true;
                    applyFromMouse(e.mouseX());
                    return true;
                }
            }
            case MOUSE_DRAG -> {
                if (dragging) {
                    applyFromMouse(e.mouseX());
                    return true;
                }
            }
            case MOUSE_UP -> dragging = false;
            default -> {
            }
        }
        return false;
    }

    private void applyFromMouse(double mouseX) {
        double t = (mouseX - bounds.x()) / Math.max(1, bounds.w());
        int value = (int) Math.round(min + t * (max - min));
        setter.accept(clamp(value));
    }

    private int clamp(Integer v) {
        int val = v == null ? min : v;
        return Math.max(min, Math.min(max, val));
    }
}
