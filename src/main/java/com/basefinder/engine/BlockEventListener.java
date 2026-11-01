package com.basefinder.engine;

import com.basefinder.world.Vec3i;

/**
 * Implemented by scanners that want block-action events (container lids opening,
 * doors, note blocks, bells). The engine forwards client block-action/sound
 * packets to every scanner implementing this, off the render thread.
 */
public interface BlockEventListener {
    void onBlockEvent(BlockEventType type, Vec3i pos, String blockId, long tick);
}
