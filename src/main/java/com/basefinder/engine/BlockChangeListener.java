package com.basefinder.engine;

import com.basefinder.world.Vec3i;

/**
 * Implemented by scanners that want live block-change events (the real-time
 * activity detector). The engine forwards client block updates to every scanner
 * implementing this, off the render thread.
 */
public interface BlockChangeListener {
    void onBlockChange(Vec3i pos, String oldId, String newId, long tick);
}
