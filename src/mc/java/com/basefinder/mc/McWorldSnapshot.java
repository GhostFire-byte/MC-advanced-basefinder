package com.basefinder.mc;

import com.basefinder.world.BlockAccess;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Dimension;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import com.basefinder.world.WorldSnapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 * Captures an immutable {@link WorldSnapshot} from Minecraft 26.2 on the client
 * thread, so background scanners can read it safely off-thread.
 *
 * <p>NOTE (integration): class/field names below use the deobfuscated 26.2
 * mappings. The block-array snapshot strategy (copying ids into a flat map) is a
 * safe default; for performance the host may instead wrap the live palette with
 * a read-only view. Verify the exact {@code LevelChunk}/{@code BlockState}
 * accessors against the client during integration &mdash; do not assume older APIs.
 */
public final class McWorldSnapshot implements WorldSnapshot {

    private final Dimension dimension;
    private final long gameTime;
    private final Vec3d playerPos;
    private final float yaw;
    private final int renderDistance;
    private final OptionalLong seed;
    private final List<BlockRef> blockEntities = new ArrayList<>();
    private final List<EntityRef> entities = new ArrayList<>();
    private final McBlockAccess blocks;

    private McWorldSnapshot(ClientLevel level, LocalPlayer player, int radiusChunks, OptionalLong seed) {
        this.dimension = Dimension.fromId(level.dimension().location().toString());
        this.gameTime = level.getGameTime();
        this.playerPos = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
        this.yaw = player.getYRot();
        this.renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
        this.seed = seed;
        this.blocks = new McBlockAccess(level, level.getMinY(), level.getMaxY());

        int cx = player.blockPosition().getX() >> 4;
        int cz = player.blockPosition().getZ() >> 4;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    blockEntities.add(toBlockRef(level, be));
                }
            }
        }
        for (Entity e : level.entitiesForRendering()) {
            entities.add(new EntityRef(e.getId(),
                    BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(),
                    new Vec3d(e.getX(), e.getY(), e.getZ()),
                    new Vec3d(e.getDeltaMovement().x, e.getDeltaMovement().y, e.getDeltaMovement().z),
                    e.hasCustomName() ? e.getCustomName().getString() : null));
        }
    }

    public static McWorldSnapshot capture(int radiusChunks) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            return null;
        }
        return new McWorldSnapshot(level, player, radiusChunks, OptionalLong.empty());
    }

    private static BlockRef toBlockRef(ClientLevel level, BlockEntity be) {
        BlockPos p = be.getBlockPos();
        String id = BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString();
        String text = null;
        if (be instanceof SignBlockEntity sign) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(sign.getFrontText().getMessage(i, false).getString()).append('\n');
            }
            text = sb.toString();
        }
        return new BlockRef(new Vec3i(p.getX(), p.getY(), p.getZ()), id,
                level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, p), true, text);
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
        return seed;
    }

    @Override
    public BlockAccess blocks() {
        return blocks;
    }

    @Override
    public Iterable<BlockRef> blockEntities() {
        return blockEntities;
    }

    @Override
    public Iterable<EntityRef> entities() {
        return entities;
    }
}
