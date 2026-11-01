package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiAnim;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.theme.ThemeManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** WindUI-style pill toggle: track fades from a faint tint to the accent; the knob slides (eased). */
public final class ToggleSwitch extends Component {

    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;
    private final UiAnim anim;

    public ToggleSwitch(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.getter = getter;
        this.setter = setter;
        this.anim = new UiAnim(Boolean.TRUE.equals(getter.get()) ? 1.0 : 0.0);
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        boolean on = Boolean.TRUE.equals(getter.get());
        double t = anim.step(on ? 1.0 : 0.0, 18);
        var th = theme.active();
        if (theme.imgui()) {
            // ImGui-style checkbox: a square frame with an accent tick when on.
            int s = Math.min(bounds.h(), bounds.w());
            Rect box = new Rect(bounds.x(), bounds.y(), s, s);
            UiDraw.roundedRect(r, box, 2, th.panelTop());
            UiDraw.roundedOutline(r, box, 2, UiColor.withAlpha(th.border(), 220), 1f);
            if (t > 0.05) {
                int acc = UiColor.withAlpha(th.accent(), (int) (255 * t));
                r.line(box.x() + s * 0.24, box.y() + s * 0.52, box.x() + s * 0.44, box.y() + s * 0.72, acc, 2f);
                r.line(box.x() + s * 0.44, box.y() + s * 0.72, box.x() + s * 0.78, box.y() + s * 0.28, acc, 2f);
            }
            return;
        }
        int rad = bounds.h() / 2;
        int faint = UiColor.withAlpha(th.text(), 20);
        int track = UiColor.lerp(faint, UiColor.withAlpha(th.accent(), 255), t);
        UiDraw.roundedRect(r, bounds, rad, track);
        UiDraw.roundedOutline(r, bounds, rad, UiColor.withAlpha(th.text(), (int) (12 + 22 * t)), 1f);
        int knob = bounds.h() - 8;
        double kx = bounds.x() + 4 + t * (bounds.w() - 8 - knob);
        double ky = bounds.y() + 4 + knob / 2.0;
        r.filledCircle(kx + knob / 2.0, ky + 1, knob / 2.0, UiColor.withAlpha(0x000000, 50));
        r.filledCircle(kx + knob / 2.0, ky, knob / 2.0, UiColor.rgb(255, 255, 255));
    }

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (visible && e.type() == InputEvent.Type.MOUSE_DOWN && contains(e.mouseX(), e.mouseY())) {
            setter.accept(!Boolean.TRUE.equals(getter.get()));
            return true;
        }
        return false;
    }
}
