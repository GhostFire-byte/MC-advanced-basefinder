package com.basefinder.test;

import com.basefinder.world.BlockAccess;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Dimension;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.WorldSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/**
 * Simulated Minecraft world for tests. Backs a sparse block map (default air),
 * plus block entities and entities, so scanners/engine can be exercised without
 * any Minecraft on the classpath.
 */
public final class FakeWorld implements WorldSnapshot {

    private Dimension dimension = Dimension.OVERWORLD;
    private long gameTime;
    private Vec3d playerPos = new Vec3d(0, 70, 0);
    private float playerYaw;
    private int renderDistance = 8;
    private Long seed;

    private final Map<Long, String> blocks = new HashMap<>();
    private final Map<Long, Integer> light = new HashMap<>();
    private final List<BlockRef> blockEntities = new ArrayList<>();
    private final List<EntityRef> entities = new ArrayList<>();
    private int minY = -64;
    private int maxY = 320;

    private final BlockAccess access = new BlockAccess() {
        @Override
        public String blockId(int x, int y, int z) {
            if (y < minY || y > maxY) {
                return null;
            }
            return blocks.getOrDefault(key(x, y, z), "minecraft:air");
        }

        @Override
        public int lightLevel(int x, int y, int z) {
            return light.getOrDefault(key(x, y, z), 0);
        }

        @Override
        public boolean isLoaded(int x, int y, int z) {
            return y >= minY && y <= maxY;
        }

        @Override
        public int minY() {
            return minY;
        }

        @Override
        public int maxY() {
            return maxY;
        }
    };

    public FakeWorld dimension(Dimension d) {
        this.dimension = d;
        return this;
    }

    public FakeWorld player(double x, double y, double z) {
        this.playerPos = new Vec3d(x, y, z);
        return this;
    }

    public FakeWorld yaw(float y) {
        this.playerYaw = y;
        return this;
    }

    public FakeWorld seed(long s) {
        this.seed = s;
        return this;
    }

    public FakeWorld tick(long t) {
        this.gameTime = t;
        return this;
    }

    public FakeWorld bounds(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        return this;
    }

    public FakeWorld setBlock(int x, int y, int z, String id) {
        blocks.put(key(x, y, z), id);
        return this;
    }

    public FakeWorld setLight(int x, int y, int z, int level) {
        light.put(key(x, y, z), level);
        return this;
    }

    public FakeWorld fill(int x0, int y0, int z0, int x1, int y1, int z1, String id) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    blocks.put(key(x, y, z), id);
                }
            }
        }
        return this;
    }

    public FakeWorld addBlockEntity(BlockRef be) {
        blockEntities.add(be);
        blocks.put(key(be.pos().x(), be.pos().y(), be.pos().z()), be.id());
        return this;
    }

    public FakeWorld addEntity(EntityRef e) {
        entities.add(e);
        return this;
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
        return playerYaw;
    }

    @Override
    public int renderDistanceChunks() {
        return renderDistance;
    }

    @Override
    public OptionalLong seed() {
        return seed == null ? OptionalLong.empty() : OptionalLong.of(seed);
    }

    @Override
    public BlockAccess blocks() {
        return access;
    }

    @Override
    public Iterable<BlockRef> blockEntities() {
        return blockEntities;
    }

    @Override
    public Iterable<EntityRef> entities() {
        return entities;
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFF) << 42) | ((long) (y & 0x3FF) << 32) | (z & 0xFFFFFFFFL);
    }
}
