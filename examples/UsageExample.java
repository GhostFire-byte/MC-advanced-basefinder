// Example: minimal integration using the bundled overlay.
// Not part of the build; illustrative only.
package examples;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.engine.WorldSource;
import com.basefinder.world.WorldSnapshot;

public final class UsageExample {

    public static void main(String[] args) {
        // In a real client, build a WorldSnapshot from the loaded world each cycle.
        WorldSource worldSource = () -> currentWorldSnapshot();

        BaseFinderModule module = new BaseFinderModule(worldSource, new InMemorySettingsBridge());
        module.start();

        // Each client tick (pass a lightweight player-view snapshot for smooth radar):
        long gameTime = 0;
        module.onClientTick(gameTime, currentWorldSnapshot());

        // From your block-update packet handler:
        // module.onBlockChange(pos, oldId, newId, gameTime);

        // Render from your HUD/world render events:
        // module.renderOverlay(new BlazeRenderer(guiGraphics));
        // EspRenderer.render(pose, buffers, camera, module.espMarkers(), true, true, true, true);

        // Bind a key to open the interactive dashboard:
        // module.toggleInteractionMode();

        module.stop();
    }

    private static WorldSnapshot currentWorldSnapshot() {
        return null; // supply a real snapshot in-client (see McWorldSnapshot).
    }
}
