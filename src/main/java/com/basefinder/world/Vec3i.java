package com.basefinder.world;

/**
 * Immutable integer block position. Minecraft-agnostic so the core never
 * touches a real Minecraft class.
 */
public record Vec3i(int x, int y, int z) {

    public long distanceSq(Vec3i o) {
        long dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Vec3i o) {
        return Math.sqrt(distanceSq(o));
    }

    public Vec3i add(int dx, int dy, int dz) {
        return new Vec3i(x + dx, y + dy, z + dz);
    }

    public int chunkX() {
        return x >> 4;
    }

    public int chunkZ() {
        return z >> 4;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
