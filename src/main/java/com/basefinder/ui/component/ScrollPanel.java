package com.basefinder.ui.component;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.theme.ThemeManager;

/** Tracks vertical scroll offset for a scrollable region; views draw rows relative to it. */
public final class ScrollPanel extends Component {

    private int offset;
    private int maxOffset;
    private static final int STEP = 18;

    public int offset() {
        return offset;
    }

    public void setContentHeight(int contentHeight) {
        this.maxOffset = Math.max(0, contentHeight - bounds.h());
        if (offset > maxOffset) {
            offset = maxOffset;
        }
    }

    public int originY() {
        return bounds.y() - offset;
    }

    @Override
    public void render(com.basefinder.ui.Renderer r, ThemeManager theme) {
        // Drawing is performed by the owning view using originY(); nothing to draw here.
    }

    @Override
    public boolean handle(InputEvent e, ThemeManager theme) {
        if (visible && e.type() == InputEvent.Type.SCROLL && contains(e.mouseX(), e.mouseY())) {
            offset -= (int) (e.scroll() * STEP);
            offset = Math.max(0, Math.min(maxOffset, offset));
            return true;
        }
        return false;
    }
}
