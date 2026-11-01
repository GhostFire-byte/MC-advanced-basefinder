package com.basefinder.ui;

import com.basefinder.world.Vec3d;

/**
 * World-space marker for a base candidate. The core produces these; the host
 * adapter draws them in 3D (outlines/boxes/labels/icons) using Minecraft's camera.
 * The core cannot project 3D itself without the live camera matrices.
 */
public record EspMarker(String candidateId, Vec3d pos, int color, int confidence,
                        String label, boolean pinned) {
}
