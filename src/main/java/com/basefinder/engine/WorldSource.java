package com.basefinder.engine;

import com.basefinder.world.WorldSnapshot;

/**
 * Supplies the current world snapshot. The Minecraft adapter implements this;
 * returns {@code null} when no world is available (main menu, loading, disconnect),
 * which the engine treats as a data-source outage and retries.
 */
public interface WorldSource {
    WorldSnapshot current();
}
