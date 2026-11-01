package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.theme.ThemeManager;

import java.util.List;
import java.util.function.Consumer;

/** A row of selectable tabs. */
public final class TabBar extends Component {

    private final List<String> tabs;
    private int selected;
    private Consumer<Integer> onSelect = i -> {
    };

    public TabBar(List<String> tabs) {
        this.tabs = tabs;
    }

    public TabBar onSelect(Consumer<Integer> c) {
        this.onSelect = c;
        return this;
    }

    public int selected() {
        return selected;
    }

    public void setSelected(int i) {
        this.selected = Math.max(0, Math.min(tabs.size() - 1, i));
    }

    private int tabWidth() {
        return Math.max(1, bounds.w() / Math.max(1, tabs.size()));
    }

    @Override
    public void render(Renderer r, ThemeManager theme) {
        if (!visible) {
            return;
        }
        int tw = tabWidth();
        for (int i = 0; i < tabs.size(); i++) {
            Rect tab = new Rect(bounds.x() + i * tw, bounds.y(), tw, bounds.h());
            boolean active = i == selected;
            int fill = active ? theme.active().primary() : UiColor.withAlpha(theme.active().panelBottom(), 180);
            r.fill(tab, active ? UiColor.withAlpha(fill, 235) : fill);
            if (active) {
                r.fill(new Rect(tab.x(), tab.bottom() - 2, tab.w(), 2), theme.active().accent());
            }
            r.textCentered(tabs.get(i), tab.centerX(), tab.centerY() - r.lineHeight() / 2,
                    active ? UiColor.rgb(18, 18, 24) : theme.active().text(), false);
        }
    }

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (visible && e.type() == InputEvent.Type.MOUSE_DOWN && contains(e.mouseX(), e.mouseY())) {
            int tw = tabWidth();
            int idx = (int) ((e.mouseX() - bounds.x()) / tw);
            if (idx >= 0 && idx < tabs.size()) {
                selected = idx;
                onSelect.accept(idx);
                return true;
            }
        }
        return false;
    }
}
