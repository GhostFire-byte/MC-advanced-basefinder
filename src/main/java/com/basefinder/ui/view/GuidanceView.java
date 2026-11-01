package com.basefinder.ui.view;

import com.basefinder.guidance.DirectionalGuidance.GuidanceTarget;
import com.basefinder.ui.Renderer;
import com.basefinder.ui.UiColor;
import com.basefinder.ui.UiModel;

import java.util.Optional;

/**
 * Draws a directional arrow near the screen centre pointing toward the most
 * relevant base candidate, with its distance and confidence.
 */
public final class GuidanceView {

    public void render(Renderer r, UiModel model) {
        Optional<GuidanceTarget> opt = model.guidance();
        if (opt.isEmpty()) {
            return;
        }
        GuidanceTarget t = opt.get();
        int cx = r.screenWidth() / 2;
        int cy = r.screenHeight() / 2 - 40;
        double ang = Math.toRadians(t.relativeBearingDeg());
        double len = 18;
        double tipX = cx + Math.sin(ang) * len;
        double tipY = cy - Math.cos(ang) * len;
        double leftX = cx + Math.sin(ang + 2.5) * 8;
        double leftY = cy - Math.cos(ang + 2.5) * 8;
        double rightX = cx + Math.sin(ang - 2.5) * 8;
        double rightY = cy - Math.cos(ang - 2.5) * 8;
        int color = UiColor.confidence(t.confidence());
        r.triangle(tipX, tipY, leftX, leftY, rightX, rightY, color);
        String label = (int) t.distance() + "m  " + t.confidence() + "%";
        r.textCentered(label, cx, cy + 22, model.theme().active().text(), true);
    }
}
