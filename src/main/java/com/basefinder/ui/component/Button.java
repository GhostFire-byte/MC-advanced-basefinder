package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.theme.ThemeManager;

/** A clickable button. */
public final class Button extends Component {

    private String label;
    private Runnable onClick = () -> {
    };
    private boolean hovered;
    private boolean primary = true;

    public Button(String label) {
        this.label = label;
    }

    public Button label(String l) {
        this.label = l;
        return this;
    }

    public Button onClick(Runnable r) {
        this.onClick = r;
        return this;
    }

    public Button secondary() {
        this.primary = false;
        return this;
    }

    public String label() {
        return label;
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        int base = primary ? theme.active().primary() : theme.active().secondary();
        int fill = hovered ? UiColor.lerp(base, UiColor.rgb(255, 255, 255), 0.22) : base;
        int rad = Math.min(10, bounds.h() / 2);
        if (hovered) {
            UiDraw.glow(r, bounds, rad, UiColor.withAlpha(base, 120), 2);
        }
        if (theme.polished()) {
            UiDraw.roundedGradient(r, bounds, rad,
                    UiColor.withAlpha(fill, 240),
                    UiColor.withAlpha(UiColor.lerp(fill, 0, 0.42), 240));
        } else {
            UiDraw.roundedRect(r, bounds, rad, UiColor.withAlpha(fill, 240));
        }
        UiDraw.roundedOutline(r, bounds, rad, UiColor.withAlpha(theme.active().border(), 210), 1f);
        // Readable label that always fits the button width.
        int ink = primary ? UiColor.rgb(16, 16, 24) : UiColor.rgb(236, 238, 248);
        UiDraw.fitText(r, label, bounds, ink, 1.0f, 0.75f);
    }

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (!visible) {
            return false;
        }
        switch (e.type()) {
            case MOUSE_MOVE, MOUSE_DRAG -> hovered = contains(e.mouseX(), e.mouseY());
            case MOUSE_DOWN -> {
                if (contains(e.mouseX(), e.mouseY())) {
                    onClick.run();
                    return true;
                }
            }
            default -> {
            }
        }
        return false;
    }
}
