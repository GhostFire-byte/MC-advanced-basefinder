package com.basefinder.ui.view;

import com.basefinder.model.Severity;
import com.basefinder.notify.Notification;
import com.basefinder.ui.Rect;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiModel;

import java.util.List;

/** Draws the most recent notifications as stacked toasts (bottom-right). */
public final class ToastView {

    private static final int MAX = 3;

    public void render(Renderer r, UiModel model) {
        List<Notification> all = model.notifications();
        if (all.isEmpty()) {
            return;
        }
        int shown = Math.min(MAX, all.size());
        int w = 210;
        int h = 26;
        int gap = 6;
        int x = r.screenWidth() - w - 12;
        int y = r.screenHeight() - 12 - shown * (h + gap);
        for (int i = all.size() - shown; i < all.size(); i++) {
            Notification n = all.get(i);
            Rect box = new Rect(x, y, w, h);
            StatusPanelView.drawPanel(r, model, box);
            r.fill(new Rect(x, y, 3, h), severityColor(n.severity()));
            r.text(n.title(), x + 8, y + 4, model.theme().active().text(), true);
            r.text(clip(r, n.message(), w - 16), x + 8, y + 4 + r.lineHeight(),
                    model.theme().active().textDim(), true);
            y += h + gap;
        }
    }

    private static int severityColor(Severity s) {
        return switch (s) {
            case CRITICAL -> UiColor.rgb(230, 60, 60);
            case HIGH -> UiColor.rgb(235, 140, 50);
            case MEDIUM -> UiColor.rgb(235, 200, 70);
            default -> UiColor.rgb(90, 190, 230);
        };
    }

    private static String clip(Renderer r, String s, int maxW) {
        if (r.textWidth(s) <= maxW) {
            return s;
        }
        while (s.length() > 1 && r.textWidth(s + "...") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }
}
