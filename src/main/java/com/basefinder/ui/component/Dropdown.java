package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.theme.ThemeManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A compact dropdown selector: shows the current value; clicking opens an option list. */
public final class Dropdown extends Component {

    private final String label;
    private final List<String> options;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private boolean open;

    public Dropdown(String label, List<String> options, Supplier<String> getter, Consumer<String> setter) {
        this.label = label;
        this.options = options;
        this.getter = getter;
        this.setter = setter;
    }

    private int rowH() {
        return bounds.h();
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        var th = theme.active();
        int rad = theme.imgui() ? 3 : 6;
        r.text(label, bounds.x(), bounds.y() - r.lineHeight() - 1, th.textDim(), true);
        UiDraw.roundedRect(r, bounds, rad, UiColor.withAlpha(th.panelTop(), 220));
        UiDraw.roundedOutline(r, bounds, rad, UiColor.withAlpha(th.border(), 210), 1f);
        String cur = getter.get();
        r.text(cur, bounds.x() + 8, bounds.centerY() - r.lineHeight() / 2, th.text(), true);
        // chevron
        int cx = bounds.right() - 12;
        int cy = bounds.centerY();
        r.line(cx - 4, cy - 2, cx, cy + 2, th.textDim(), 1.5f);
        r.line(cx, cy + 2, cx + 4, cy - 2, th.textDim(), 1.5f);

        if (open) {
            int h = rowH();
            for (int i = 0; i < options.size(); i++) {
                Rect row = new Rect(bounds.x(), bounds.bottom() + 2 + i * h, bounds.w(), h);
                boolean hov = row.contains(lastX, lastY);
                UiDraw.roundedRect(r, row, rad, UiColor.withAlpha(hov ? th.accent() : th.panelTop(), hov ? 230 : 240));
                UiDraw.roundedOutline(r, row, rad, UiColor.withAlpha(th.border(), 180), 1f);
                int ink = hov ? UiColor.rgb(16, 17, 24) : th.text();
                r.text(options.get(i), row.x() + 8, row.centerY() - r.lineHeight() / 2, ink, true);
            }
        }
    }

    private double lastX;
    private double lastY;

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (!visible) {
            return false;
        }
        if (e.type() == InputEvent.Type.MOUSE_MOVE || e.type() == InputEvent.Type.MOUSE_DRAG) {
            lastX = e.mouseX();
            lastY = e.mouseY();
        }
        if (e.type() == InputEvent.Type.MOUSE_DOWN) {
            if (open) {
                int h = rowH();
                for (int i = 0; i < options.size(); i++) {
                    Rect row = new Rect(bounds.x(), bounds.bottom() + 2 + i * h, bounds.w(), h);
                    if (row.contains(e.mouseX(), e.mouseY())) {
                        setter.accept(options.get(i));
                        open = false;
                        return true;
                    }
                }
            }
            if (contains(e.mouseX(), e.mouseY())) {
                open = !open;
                return true;
            }
            if (open) {
                open = false;
            }
        }
        return false;
    }
}
