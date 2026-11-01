package com.basefinder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.test.FakeRenderer;
import com.basefinder.test.FakeWorld;
import com.basefinder.ui.InputEvent;
import com.basefinder.ui.view.OverlayRoot;
import org.junit.jupiter.api.Test;

/**
 * Headless smoke tests for the overhauled overlay: the premium drawing helpers,
 * scaled text, open/close fade and drag routing must run without exceptions and
 * actually move panels.
 */
class UiSmokeTest {

    private BaseFinderModule module() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        return new BaseFinderModule(() -> w, new InMemorySettingsBridge());
    }

    @Test
    void rendersPassiveAndInteractiveWithoutErrors() throws InterruptedException {
        BaseFinderModule m = module();
        OverlayRoot root = new OverlayRoot(m, m);
        FakeRenderer r = new FakeRenderer(960, 540);

        // Passive HUD.
        for (int i = 0; i < 3; i++) {
            root.render(r);
        }
        // Open the dashboard and let the fade advance over real time.
        root.setInteractionMode(true);
        for (int i = 0; i < 40; i++) {
            root.render(r);
            Thread.sleep(16);
        }
        assertTrue(root.state().dashFade > 0.9, "dashboard should fade in");

        root.state().uiScale = 1.6;
        root.render(r);
        root.state().uiScale = 0.9;
        root.render(r);
        assertTrue(r.drawCalls > 0, "something was drawn");
    }

    @Test
    void draggingTheStatusPanelMovesIt() {
        BaseFinderModule m = module();
        OverlayRoot root = new OverlayRoot(m, m);
        FakeRenderer r = new FakeRenderer(960, 540);
        root.setInteractionMode(true);
        root.render(r); // establishes status-panel bounds at the default anchor (~12,12)

        // Press inside the status panel, drag, release.
        root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_DOWN, 20, 20, 0));
        root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_DRAG, 300, 260, 0));
        root.handle(InputEvent.mouse(InputEvent.Type.MOUSE_UP, 300, 260, 0));

        assertNotEquals(null, root.state().statusX, "status panel should have a custom position");
        assertTrue(root.state().statusX > 200, "status panel moved to where it was dragged");
        root.render(r); // re-render at the new position without error
    }

    @Test
    void resetLayoutClearsOverrides() {
        BaseFinderModule m = module();
        OverlayRoot root = new OverlayRoot(m, m);
        root.state().statusX = 400;
        root.state().uiScale = 1.8;
        root.state().resetLayout();
        assertTrue(root.state().statusX == null && root.state().uiScale == 1.0);
    }
}
