package com.basefinder.preview;

import com.basefinder.ui.GuiStyle;
import com.basefinder.ui.view.OverlayRoot;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Headless screenshot tool: renders the overlay skins to PNG files for inspection. */
public final class GuiShot {

    public static void main(String[] args) throws Exception {
        String outDir = args.length > 0 ? args[0] : ".";
        new File(outDir).mkdirs();
        String[] tabs = {"overview", "scanners", "findings", "rules", "appearance", "diagnostics"};
        for (int t = 0; t < tabs.length; t++) {
            shot(outDir + "/windui-" + t + "-" + tabs[t] + ".png", GuiStyle.WINDUI, "Night", t);
            shot(outDir + "/imgui-" + t + "-" + tabs[t] + ".png", GuiStyle.IMGUI, "ImGui Classic", t);
        }
        System.out.println("wrote screenshots to " + new File(outDir).getAbsolutePath());
    }

    private static void shot(String path, GuiStyle style, String theme, int tab) throws Exception {
        int w = 1180;
        int h = 680;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // backdrop
        g.setPaint(new GradientPaint(0, 0, new Color(26, 32, 54), 0, h, new Color(8, 10, 18)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(40, 56, 48));
        g.fillRect(0, (int) (h * 0.62), w, h);

        SampleUi ui = new SampleUi();
        ui.theme().setGuiStyle(style);
        ui.theme().selectPreset(theme);
        OverlayRoot root = new OverlayRoot(ui, ui);
        root.setInteractionMode(true);
        root.selectDashboardTab(tab);
        root.state().dashFade = 1.0; // skip the fade for a clean still
        Java2dRenderer r = new Java2dRenderer();
        for (int i = 0; i < 4; i++) {
            r.begin(g, w, h);
            root.render(r);
        }
        g.dispose();
        ImageIO.write(img, "png", new File(path));
    }

    private GuiShot() {
    }
}
