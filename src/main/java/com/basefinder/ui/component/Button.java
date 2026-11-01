package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiAnim;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiDraw;
import com.basefinder.ui.theme.ThemeManager;

/** A clickable button, WindUI-flat: accent fill for primary, subtle fill for secondary, eased hover. */
public final class Button extends Component {

    private String label;
    private Runnable onClick = () -> {
    };
    private boolean hovered;
    private boolean primary = true;
    private final UiAnim hover = new UiAnim(0);

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
        var th = theme.active();
        double hv = hover.step(hovered ? 1.0 : 0.0, 22);
        if (theme.imgui()) {
            int fill = primary
                    ? UiColor.lerp(th.accent(), UiColor.rgb(255, 255, 255), 0.14 * hv)
                    : UiColor.lerp(th.panelTop(), th.accent(), 0.10 + 0.25 * hv);
            UiDraw.roundedRect(r, bounds, 2, fill);
            UiDraw.roundedOutline(r, bounds, 2, UiColor.withAlpha(th.border(), 200), 1f);
            int ink = primary ? UiColor.rgb(16, 17, 26) : th.text();
            UiDraw.fitText(r, label, bounds, ink, 1.0f, 0.72f);
            return;
        }
        int rad = Math.min(6, bounds.h() / 2);
        int ink;
        if (primary) {
            int fill = UiColor.lerp(th.accent(), UiColor.rgb(255, 255, 255), 0.12 * hv);
            UiDraw.roundedRect(r, bounds, rad, fill);
            ink = UiColor.rgb(16, 17, 26);
        } else {
            UiDraw.roundedRect(r, bounds, rad, UiColor.withAlpha(th.text(), (int) (20 + 24 * hv)));
            ink = th.text();
        }
        UiDraw.roundedOutline(r, bounds, rad, UiColor.withAlpha(th.text(), (int) (34 + 20 * hv)), 1f);
        UiDraw.fitText(r, label, bounds, ink, 1.0f, 0.72f);
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
