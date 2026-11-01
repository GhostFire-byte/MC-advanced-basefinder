package com.basefinder;

import com.basefinder.engine.VillageContext;
import com.basefinder.fusion.FusionManager;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.test.FakeWorld;
import com.basefinder.world.BlockRef;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceFusionTest {

    private Finding f(Vec3i p, FindingCategory cat, int conf, double w) {
        return Finding.builder("sc-" + cat, cat, p).confidence(conf).signalWeight(w).build();
    }

    @Test
    void miningOnlyClusterIsClassifiedMiningAreaNotBase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(100, 12, 100);
        List<BaseCandidate> cands = fm.update(List.of(
                f(a, FindingCategory.MINING_AREA, 80, 0.4),
                f(a.add(1, 0, 0), FindingCategory.PATH, 70, 0.4)), 1);
        assertEquals(1, cands.size());
        BaseCandidate c = cands.get(0);
        assertEquals(BaseCandidate.CandidateKind.MINING_AREA, c.kind());
        assertTrue(c.fusedConfidence() <= 60, "mining-only must be capped below a confident base");
    }

    @Test
    void habitationClusterIsBaseAndCanScoreHigh() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(0, 64, 0);
        List<BaseCandidate> cands = fm.update(List.of(
                f(a, FindingCategory.STORAGE, 100, 0.5),
                f(a, FindingCategory.BED, 100, 0.8),
                f(a, FindingCategory.MARKER, 95, 0.95)), 1);
        BaseCandidate c = cands.get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind());
        assertTrue(c.fusedConfidence() >= 80, "strong habitation+marker should score high");
    }

    @Test
    void strongMarkerAloneCountsAsBase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(0, 64, 0);
        BaseCandidate c = fm.update(List.of(f(a, FindingCategory.MARKER, 95, 0.95)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind());
    }

    @Test
    void corroborationAcrossEvidenceClassesRaisesScore() {
        // One habitation signal vs the same plus security + marker (3 distinct classes).
        FusionManager single = new FusionManager();
        Vec3i a = new Vec3i(0, 64, 0);
        int one = single.update(List.of(f(a, FindingCategory.STORAGE, 70, 0.5)), 1).get(0).fusedConfidence();
        FusionManager multi = new FusionManager();
        int many = multi.update(List.of(
                f(a, FindingCategory.STORAGE, 70, 0.5),
                f(a, FindingCategory.SECURITY, 70, 0.7),
                f(a, FindingCategory.MARKER, 70, 0.9)), 1).get(0).fusedConfidence();
        assertTrue(many > one, "more distinct evidence classes should raise fused confidence");
    }

    @Test
    void villageSuppressesFarmDecorationCluster() {
        // Build a world that reads as a village (villagers + bell) around the farm.
        FakeWorld village = new FakeWorld().player(0, 70, 0);
        for (int i = 0; i < 3; i++) {
            village.addEntity(new EntityRef(i, "minecraft:villager", new Vec3d(100, 70, 100)));
        }
        village.addBlockEntity(new BlockRef(new Vec3i(100, 70, 100), "minecraft:bell"));
        VillageContext vc = VillageContext.from(village);

        Vec3i farmPos = new Vec3i(102, 70, 102);
        FusionManager inVillage = new FusionManager();
        int suppressed = inVillage.update(List.of(
                f(farmPos, FindingCategory.FARM, 80, 0.45),
                f(farmPos, FindingCategory.DECORATION, 70, 0.5)), 1, vc).get(0).fusedConfidence();

        FusionManager noVillage = new FusionManager();
        int normal = noVillage.update(List.of(
                f(farmPos, FindingCategory.FARM, 80, 0.45),
                f(farmPos, FindingCategory.DECORATION, 70, 0.5)), 1, VillageContext.empty()).get(0).fusedConfidence();

        assertTrue(suppressed < normal, "village context should down-weight farm/decoration clusters");
    }
}
