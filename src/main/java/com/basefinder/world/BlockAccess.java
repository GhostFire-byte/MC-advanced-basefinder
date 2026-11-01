package com.basefinder.world;

/**
 * Random-access view over currently-loaded blocks. Implementations must be
 * safe to call from background worker threads for the duration of a single scan
 * (the Minecraft adapter takes a consistent snapshot before handing this out).
 */
public interface BlockAccess {

    /**
     * Registry id of the block at the given position, or {@code null} if that
     * position is not currently loaded. Air is reported as {@code "minecraft:air"}.
     */
    String blockId(int x, int y, int z);

    /** Block-light level 0..15, or 0 if unknown/unloaded. */
    int lightLevel(int x, int y, int z);

    /** Whether the position is within a currently-loaded chunk section. */
    boolean isLoaded(int x, int y, int z);

    /** Lowest world Y (e.g. -64 for the overworld). */
    int minY();

    /** Highest world Y, exclusive upper bound handling is up to the caller. */
    int maxY();

    default boolean isAir(int x, int y, int z) {
        String id = blockId(x, y, z);
        return id == null || id.endsWith(":air") || id.endsWith("_air");
    }
}
