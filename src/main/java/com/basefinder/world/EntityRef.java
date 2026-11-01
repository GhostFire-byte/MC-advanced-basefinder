package com.basefinder.world;

/**
 * A single observed entity. {@code type} is the deobfuscated registry name
 * (e.g. {@code "minecraft:player"}). {@code velocity} is per-tick motion used
 * for a cheap "moving now" check; longer-term idle detection is done by the
 * entity scanner using a position history keyed by {@link #networkId()}.
 */
public record EntityRef(int networkId, String type, Vec3d pos, Vec3d velocity, String name) {

    public EntityRef(int networkId, String type, Vec3d pos) {
        this(networkId, type, pos, new Vec3d(0, 0, 0), null);
    }

    public boolean isPlayer() {
        return "minecraft:player".equals(type);
    }

    public boolean movingNow() {
        return velocity != null && velocity.length() > 0.003;
    }

    public String displayName() {
        return name != null ? name : type;
    }
}
