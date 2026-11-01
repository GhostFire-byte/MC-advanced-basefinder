package com.basefinder.preview;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.basefinder.ui.InputEvent;
import com.basefinder.ui.view.OverlayRoot;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Headless end-to-end check of the preview: the real overlay rendered through the
 * Java2D renderer into an off-screen image, plus a few input events. Validates the
 * sample model against the real UI interfaces and that nothing throws.
 */
class PreviewRenderTest {

    @Test
    void rendersSampleOverlayHeadlessly() {
        assertDoesNotThrow(() -> {
            BufferedImage img = new BufferedImage(1180, 680, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Java2dRenderer r = new Java2dRenderer();
            SampleUi ui = new SampleUi();
            OverlayRoot root = new OverlayRoot(ui, ui);

            r.begin(g, 1180, 680);
            root.render(r); // passive HUD with sample candidates

            root.setInteractionMode(true);
            for (int i = 0; i < 5; i++) {
                r.begin(g, 1180, 680);
                root.render(r); // dashboard fading in
            }

            // Drag the dashboard title area and resize corner; render at a larger scale.
            root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_DOWN, 590, 140, 0));
            root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_DRAG, 620, 180, 0));
            root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_UP, 620, 180, 0));
            root.state().uiScale = 1.5;
            r.begin(g, 1180, 680);
            root.render(r);

            // Switch to the ImGui skin + an ImGui theme and render across tabs.
            ui.theme().setGuiStyle(com.basefinder.ui.GuiStyle.IMGUI);
            ui.theme().selectPreset("ImGui Classic");
            for (int tab = 0; tab < 6; tab++) {
                root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_DOWN, 40 + tab * 60, 120, 0));
                r.begin(g, 1180, 680);
                root.render(r);
            }

            g.dispose();
        });
    }
}
