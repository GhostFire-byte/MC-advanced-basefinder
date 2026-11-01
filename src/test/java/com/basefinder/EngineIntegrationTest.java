package com.basefinder;

import com.basefinder.api.BaseFinderModule;
import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.engine.WorldSource;
import com.basefinder.test.FakeWorld;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineIntegrationTest {

    private FakeWorld baseWorld() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).bounds(60, 80);
        // A clear base cluster: beacon + beds + many chests + player blocks, all together.
        w.addBlockEntity(new BlockRef(new Vec3i(5, 70, 5), "minecraft:beacon"));
        for (int i = 0; i < 6; i++) {
            w.addBlockEntity(new BlockRef(new Vec3i(4 + i, 70, 6), "minecraft:chest"));
        }
        w.setBlock(5, 70, 7, "minecraft:red_bed");
        for (int i = 0; i < 8; i++) {
            w.setBlock(2 + i, 70, 4, "minecraft:oak_planks");
        }
        return w;
    }

    @Test
    void engineProducesBaseCandidateFromSimulatedBase() throws InterruptedException {
        FakeWorld w = baseWorld();
        WorldSource source = () -> w;
        BaseFinderModule module = new BaseFinderModule(source, new InMemorySettingsBridge());
        module.start();
        try {
            boolean found = false;
            for (int i = 0; i < 150 && !found; i++) {
                w.tick(i * 25L);
                module.onClientTick(i * 25L, w);
                Thread.sleep(20);
                found = !module.visibleCandidates().isEmpty();
            }
            assertTrue(found, "engine should surface a base candidate from the simulated base");
            assertTrue(module.visibleCandidates().get(0).fusedConfidence() >= module.globals().confidenceFloor());
            assertFalse(module.espMarkers().isEmpty(), "ESP markers should be produced for the candidate");
        } finally {
            module.stop();
        }
    }

    @Test
    void worldChangeClearsTransientData() throws InterruptedException {
        FakeWorld w = baseWorld();
        BaseFinderModule module = new BaseFinderModule(() -> w, new InMemorySettingsBridge());
        module.start();
        try {
            for (int i = 0; i < 150 && module.visibleCandidates().isEmpty(); i++) {
                w.tick(i * 25L);
                module.onClientTick(i * 25L, w);
                Thread.sleep(20);
            }
            assertFalse(module.visibleCandidates().isEmpty());
            module.onWorldChanged("disconnect");
            assertTrue(module.visibleCandidates().isEmpty(), "findings must be cleared on world change");
            assertTrue(module.espMarkers().isEmpty());
        } finally {
            module.stop();
        }
    }

    @Test
    void nullWorldIsHandledGracefully() {
        BaseFinderModule module = new BaseFinderModule(() -> null, new InMemorySettingsBridge());
        module.start();
        try {
            module.onClientTick(0, null);
            module.onClientTick(100, null);
            assertFalse(module.dataSourceAvailable());
            assertTrue(module.visibleCandidates().isEmpty());
        } finally {
            module.stop();
        }
    }
}
