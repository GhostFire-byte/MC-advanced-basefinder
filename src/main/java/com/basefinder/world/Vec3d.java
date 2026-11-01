package com.basefinder.world;

/** Immutable double-precision position, used for entities and the camera. */
public record Vec3d(double x, double y, double z) {

    public double distanceSq(Vec3d o) {
        double dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distance(Vec3d o) {
        return Math.sqrt(distanceSq(o));
    }

    public double horizontalDistance(Vec3d o) {
        double dx = x - o.x, dz = z - o.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3i toBlock() {
        return new Vec3i((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
}
