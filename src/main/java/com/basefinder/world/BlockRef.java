package com.basefinder.world;

/**
 * A single observed block. For ordinary blocks {@code lightLevel} carries the
 * block-light value; for block entities (chests, signs, spawners, ...) the
 * optional {@code text} / {@code blockEntityType} fields may be populated by the
 * Minecraft adapter. The {@code id} is the deobfuscated registry name, e.g.
 * {@code "minecraft:chest"}.
 */
public record BlockRef(Vec3i pos, String id, int lightLevel, boolean blockEntity, String text) {

    public BlockRef(Vec3i pos, String id) {
        this(pos, id, 0, false, null);
    }

    public BlockRef(Vec3i pos, String id, int lightLevel) {
        this(pos, id, lightLevel, false, null);
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
