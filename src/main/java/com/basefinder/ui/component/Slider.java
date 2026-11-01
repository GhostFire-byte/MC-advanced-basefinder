package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.theme.ThemeManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** A horizontal value slider over an integer range. */
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
        int value = clamp(getter.get());
        r.text(label + ": " + value, bounds.x(), bounds.y(), theme.active().text(), true);
        int trackY = bounds.y() + bounds.h() - 6;
        Rect track = new Rect(bounds.x(), trackY, bounds.w(), 4);
        r.fill(track, UiColor.rgb(70, 70, 82));
        double t = (value - min) / (double) (max - min);
        int fillW = (int) (bounds.w() * t);
        r.fill(new Rect(bounds.x(), trackY, fillW, 4), theme.active().accent());
        int knobX = bounds.x() + fillW;
        r.fill(new Rect(knobX - 3, trackY - 4, 6, 12), UiColor.rgb(240, 240, 248));
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
