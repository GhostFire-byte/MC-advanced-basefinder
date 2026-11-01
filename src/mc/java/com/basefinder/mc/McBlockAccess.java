package com.basefinder.mc;

import com.basefinder.world.BlockAccess;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Read-only block access backed by the live {@link ClientLevel}. Reads are
 * confined to the client thread in practice; the engine only touches this during
 * the on-thread snapshot window unless the host guarantees thread-safe chunk access.
 *
 * <p>NOTE (integration): if off-thread reads are required, copy the needed
 * sections into an immutable array here instead of querying the live level.
 */
public final class McBlockAccess implements BlockAccess {

    private final ClientLevel level;
    private final int minY;
    private final int maxY;

    public McBlockAccess(ClientLevel level, int minY, int maxY) {
        this.level = level;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public String blockId(int x, int y, int z) {
        if (y < minY || y >= maxY) {
            return null;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    @Override
    public int lightLevel(int x, int y, int z) {
        return level.getBrightness(LightLayer.BLOCK, new BlockPos(x, y, z));
    }

    @Override
    public boolean isLoaded(int x, int y, int z) {
        return y >= minY && y < maxY && level.hasChunkAt(new BlockPos(x, y, z));
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int maxY() {
        return maxY;
    }
}
