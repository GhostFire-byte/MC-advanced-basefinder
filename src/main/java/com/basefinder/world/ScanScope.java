package com.basefinder.world;

/**
 * The volume a scan pass should examine: a square of chunks around a centre
 * chunk, between {@code minY} and {@code maxY} (inclusive). Block-array scanners
 * iterate this; block-entity/entity scanners filter by it.
 */
public record ScanScope(int centerChunkX, int centerChunkZ, int radiusChunks, int minY, int maxY) {

    public int blockMinX() {
        return (centerChunkX - radiusChunks) << 4;
    }

    public int blockMaxX() {
        return ((centerChunkX + radiusChunks) << 4) + 15;
    }

    public int blockMinZ() {
        return (centerChunkZ - radiusChunks) << 4;
    }

    public int blockMaxZ() {
        return ((centerChunkZ + radiusChunks) << 4) + 15;
    }

    public boolean contains(Vec3i p) {
        return p.x() >= blockMinX() && p.x() <= blockMaxX()
                && p.z() >= blockMinZ() && p.z() <= blockMaxZ()
                && p.y() >= minY && p.y() <= maxY;
    }

    public boolean containsBlockEntity(BlockRef b) {
        return contains(b.pos());
    }

    public long blockCount() {
        long span = ((long) radiusChunks * 2 + 1) * 16;
        return span * span * (Math.max(0, (long) maxY - minY + 1));
    }
}
