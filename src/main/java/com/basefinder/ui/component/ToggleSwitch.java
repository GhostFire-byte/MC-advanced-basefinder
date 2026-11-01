package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.theme.ThemeManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** An on/off toggle switch bound to a supplier/consumer. */
public final class ToggleSwitch extends Component {

    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    public ToggleSwitch(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        boolean on = Boolean.TRUE.equals(getter.get());
        int track = on ? theme.active().accent() : UiColor.rgb(90, 90, 100);
        r.fill(bounds, UiColor.withAlpha(track, 230));
        int knobSize = bounds.h() - 4;
        int knobX = on ? bounds.right() - knobSize - 2 : bounds.x() + 2;
        r.fill(new Rect(knobX, bounds.y() + 2, knobSize, knobSize), UiColor.rgb(245, 245, 250));
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
