package com.basefinder.world;

import java.util.OptionalLong;

/**
 * Read-only, point-in-time view of everything the client legitimately knows
 * about the world. This is the single seam between the Minecraft adapter and
 * the whole analysis core: scanners never see a real Minecraft class.
 */
public interface WorldSnapshot {

    Dimension dimension();

    /** Monotonic client game time in ticks; used for temporal/persistence logic. */
    long gameTime();

    Vec3d playerPos();

    /** Player/camera yaw in degrees, for radar rotation and guidance. */
    float playerYaw();

    int renderDistanceChunks();

    /** World seed, present only when the client legitimately knows it (singleplayer, /seed, manual entry). */
    OptionalLong seed();

    BlockAccess blocks();

    /** All block entities (chests, spawners, signs, beacons, ...) currently loaded. Cheap to enumerate. */
    Iterable<BlockRef> blockEntities();

    /** All loaded entities (players, mobs, items, vehicles). */
    Iterable<EntityRef> entities();
}
