package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.theme.ThemeManager;

/** Base of the retained-mode component tree. */
public abstract class Component {

    protected Rect bounds = new Rect(0, 0, 0, 0);
    protected boolean visible = true;

    public Rect bounds() {
        return bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean contains(double x, double y) {
        return visible && bounds.contains(x, y);
    }

    public abstract void render(Renderer r, ThemeManager theme);

    /** @return true if the event was consumed. */
    public boolean handle(InputEvent e, ThemeManager theme) {
        return false;
    }
}
