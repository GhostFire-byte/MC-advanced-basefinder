package com.basefinder.mc;

import com.basefinder.world.BlockAccess;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Dimension;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.WorldSnapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.OptionalLong;

/**
 * Cheap per-tick snapshot carrying only the player pose/dimension, used to keep
 * the radar and guidance smooth each frame without a full world capture (the
 * engine does the heavy capture on its own cadence).
 */
public final class McPlayerView implements WorldSnapshot {

    private final Dimension dimension;
    private final long gameTime;
    private final Vec3d playerPos;
    private final float yaw;
    private final int renderDistance;

    private McPlayerView(ClientLevel level, LocalPlayer player) {
        this.dimension = Dimension.fromId(level.dimension().location().toString());
        this.gameTime = level.getGameTime();
        this.playerPos = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
        this.yaw = player.getYRot();
        this.renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
    }

    public static McPlayerView capture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return null;
        }
        return new McPlayerView(mc.level, mc.player);
    }

    @Override
    public Dimension dimension() {
        return dimension;
    }

    @Override
    public long gameTime() {
        return gameTime;
    }

    @Override
    public Vec3d playerPos() {
        return playerPos;
    }

    @Override
    public float playerYaw() {
        return yaw;
    }

    @Override
    public int renderDistanceChunks() {
        return renderDistance;
    }

    @Override
    public OptionalLong seed() {
        return OptionalLong.empty();
    }

    @Override
    public BlockAccess blocks() {
        return EMPTY_BLOCKS;
    }

    @Override
    public Iterable<BlockRef> blockEntities() {
        return List.of();
    }

    @Override
    public Iterable<EntityRef> entities() {
        return List.of();
    }

    private static final BlockAccess EMPTY_BLOCKS = new BlockAccess() {
        public String blockId(int x, int y, int z) {
            return null;
        }

        public int lightLevel(int x, int y, int z) {
            return 0;
        }

        public boolean isLoaded(int x, int y, int z) {
            return false;
        }

        public int minY() {
            return 0;
        }

        public int maxY() {
            return 0;
        }
    };
}
