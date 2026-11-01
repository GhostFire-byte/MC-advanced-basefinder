package com.basefinder.mc;

import com.basefinder.ui.EspMarker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Draws world-space ESP for base candidates (bounding boxes, outlines, labels,
 * icons) during the client world-render pass, using the live camera so markers
 * stick to blocks. Called from the host's world-render hook with a pose stack
 * and buffer source.
 *
 * <p>NOTE (integration): the exact level-renderer hook and line/box helper
 * (e.g. {@code LevelRenderer.renderLineBox} / {@code DebugRenderer}) must be
 * wired against the client. The data (markers + style flags) is fully provided
 * by the core via {@link com.basefinder.api.BaseFinderModule#espMarkers()}.
 */
public final class EspRenderer {

    private EspRenderer() {
    }

    /**
     * @param poseStack the current render pose (object type left loose to avoid
     *                  pinning a specific 26.2 pose class here; cast at integration)
     */
    public static void render(Object poseStack, MultiBufferSource buffers, Camera camera,
                              List<EspMarker> markers, boolean boxes, boolean outlines,
                              boolean labels, boolean icons) {
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;
        for (EspMarker m : markers) {
            double x = m.pos().x() - camX;
            double y = m.pos().y() - camY;
            double z = m.pos().z() - camZ;
            AABB box = new AABB(x - 0.6, y - 0.6, z - 0.6, x + 0.6, y + 0.6, z + 0.6);
            // Integration: feed `box` + m.color() into the client's line/box renderer.
            // Labels/icons: billboard m.label()/m.confidence() toward the camera.
            // Left as an explicit hook so we never invent an unverified render call.
            drawBox(poseStack, buffers, box, m.color(), boxes, outlines);
            if (labels) {
                drawLabel(poseStack, buffers, x, y + 0.8, z, m.label() + " " + m.confidence() + "%", m.color());
            }
        }
    }

    private static void drawBox(Object poseStack, MultiBufferSource buffers, AABB box, int color,
                                boolean filled, boolean outline) {
        // Hook point: client-specific box drawing. See integration notes above.
    }

    private static void drawLabel(Object poseStack, MultiBufferSource buffers,
                                  double x, double y, double z, String text, int color) {
        // Hook point: billboard text toward the camera.
    }
}
