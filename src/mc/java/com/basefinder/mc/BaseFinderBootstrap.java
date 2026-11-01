package com.basefinder.mc;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.config.SettingsBridge;
import com.basefinder.engine.WorldSource;
import com.basefinder.ui.InputEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Glue between the host Minecraft 26.2 client and {@link BaseFinderModule}.
 * The host calls these hooks from its own lifecycle/render/input events; this
 * class invents no client internals of its own (per the integration contract).
 *
 * <p>Wire-up checklist (see docs/INTEGRATION.md):
 * <ol>
 *   <li>{@link #init} once at client start, passing the host's settings bridge.</li>
 *   <li>{@link #onClientTick} every client tick.</li>
 *   <li>{@link #onRenderHud} from the HUD render event.</li>
 *   <li>{@link #onRenderWorld} from the world/level render event (for 3D ESP).</li>
 *   <li>{@link #onBlockChange} from the block-update packet handler.</li>
 *   <li>{@link #onWorldChanged} on join/leave/dimension change.</li>
 *   <li>Route mouse/keyboard to {@link #onInput} while interaction mode is on.</li>
 *   <li>Bind keys to {@link #toggleOverlay}/{@link #toggleInteraction}.</li>
 * </ol>
 */
public final class BaseFinderBootstrap {

    private static BaseFinderModule module;

    private BaseFinderBootstrap() {
    }

    public static synchronized void init(SettingsBridge bridge) {
        if (module != null) {
            return;
        }
        WorldSource worldSource = () -> McWorldSnapshot.capture(
                module.settings().global().detectionRadiusChunks());
        module = new BaseFinderModule(worldSource, bridge != null ? bridge : new InMemorySettingsBridge());
        module.start();
        if (module.settings().global().wdaExcludeFromCapture()) {
            CaptureExclusion.apply(true);
        }
    }

    public static BaseFinderModule module() {
        return module;
    }

    public static void onClientTick(long gameTime) {
        if (module != null) {
            module.onClientTick(gameTime, McPlayerView.capture());
        }
    }

    public static void onRenderHud(GuiGraphics graphics) {
        if (module != null) {
            module.renderOverlay(new BlazeRenderer(graphics));
        }
    }

    public static void onRenderWorld(Object poseStack, MultiBufferSource buffers, Camera camera) {
        if (module == null) {
            return;
        }
        var g = module.settings().global();
        EspRenderer.render(poseStack, buffers, camera, module.espMarkers(),
                g.espBoxes(), g.espOutlines(), g.espLabels(), g.espIcons());
    }

    public static void onBlockChange(int x, int y, int z, String oldId, String newId, long gameTime) {
        if (module != null) {
            module.onBlockChange(new com.basefinder.world.Vec3i(x, y, z), oldId, newId, gameTime);
        }
    }

    /** Forward a block-action/sound event (container lid, door, note, bell) for live stash detection. */
    public static void onBlockEvent(com.basefinder.engine.BlockEventType type, int x, int y, int z,
                                    String blockId, long gameTime) {
        if (module != null) {
            module.onBlockEvent(type, new com.basefinder.world.Vec3i(x, y, z), blockId, gameTime);
        }
    }

    public static void onWorldChanged(String reason) {
        if (module != null) {
            module.onWorldChanged(reason);
        }
    }

    /** @return true if the overlay consumed the input (host should then cancel it). */
    public static boolean onInput(InputEvent event) {
        return module != null && module.handleInput(event);
    }

    public static void toggleOverlay() {
        if (module != null) {
            module.toggleOverlay();
        }
    }

    public static void toggleInteraction() {
        if (module != null) {
            module.toggleInteractionMode();
        }
    }

    public static boolean interactionMode() {
        return module != null && module.interactionMode();
    }

    public static void shutdown() {
        if (module != null) {
            module.stop();
            module = null;
        }
    }
}
