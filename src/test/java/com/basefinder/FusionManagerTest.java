package com.basefinder;

import com.basefinder.fusion.FusionManager;
import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FusionManagerTest {

    private Finding at(Vec3i p, FindingCategory cat, int conf, double weight) {
        return Finding.builder("s-" + cat.name(), cat, p).confidence(conf).signalWeight(weight).build();
    }

    private Finding spawner(Vec3i p, boolean trial) {
        return Finding.builder("spawner", FindingCategory.SPAWNER, p)
                .confidence(100).signalWeight(0.6)
                .meta("variant", trial ? "trial" : "mob").build();
    }

    private Finding container(Vec3i p, boolean surface) {
        return Finding.builder("storage", FindingCategory.STORAGE, p)
                .confidence(100).signalWeight(0.45)
                .meta("surface", String.valueOf(surface)).build();
    }

    @Test
    void oneOrTwoSurfaceContainersAloneAreLeftoversNotABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(70, 72, 70);
        BaseCandidate c = fm.update(List.of(
                container(a, true),
                container(a.add(1, 0, 0), true)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.LEFTOVER, c.kind(),
                "two sky-exposed containers alone = explorer leftovers");
    }

    @Test
    void anUndergroundContainerStillCountsAsABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(70, 20, 70);
        BaseCandidate c = fm.update(List.of(container(a, false)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind(),
                "a buried container is not a surface leftover");
    }

    @Test
    void threeSurfaceContainersAreTreatedAsABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(60, 72, 60);
        BaseCandidate c = fm.update(List.of(
                container(a, true),
                container(a.add(1, 0, 0), true),
                container(a.add(2, 0, 0), true)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind(),
                "3+ surface containers together look intentional");
    }

    @Test
    void surfaceContainerWithOtherEvidenceIsABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(70, 72, 70);
        BaseCandidate c = fm.update(List.of(
                container(a, true),
                at(a.add(1, 0, 0), FindingCategory.BED, 100, 0.8)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind());
    }

    @Test
    void spawnerWithOneOrTwoChestsIsADungeonNotABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(50, 32, 50);
        BaseCandidate c = fm.update(List.of(
                spawner(a, false),
                at(a.add(1, 0, 0), FindingCategory.STORAGE, 100, 0.45),
                at(a.add(2, 0, 0), FindingCategory.STORAGE, 100, 0.45)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.DUNGEON, c.kind(), "spawner + 2 chests = dungeon");
        assertEquals("Spawner chamber", c.displayLabel());
    }

    @Test
    void spawnerWithThreeChestsIsTreatedAsABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(50, 32, 50);
        BaseCandidate c = fm.update(List.of(
                spawner(a, false),
                at(a.add(1, 0, 0), FindingCategory.STORAGE, 100, 0.45),
                at(a.add(2, 0, 0), FindingCategory.STORAGE, 100, 0.45),
                at(a.add(3, 0, 0), FindingCategory.STORAGE, 100, 0.45)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind(), "3+ chests by a spawner = base");
    }

    @Test
    void spawnerWithChestsPlusRealBaseEvidenceStaysABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(50, 32, 50);
        BaseCandidate c = fm.update(List.of(
                spawner(a, false),
                at(a.add(1, 0, 0), FindingCategory.STORAGE, 100, 0.45),
                at(a.add(2, 0, 0), FindingCategory.BED, 100, 0.8)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind(),
                "a bed alongside the spawner means someone lives here");
    }

    @Test
    void trialSpawnerIsATrialChamberNotABase() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(50, 32, 50);
        BaseCandidate c = fm.update(List.of(
                spawner(a, true),
                at(a.add(1, 0, 0), FindingCategory.STORAGE, 100, 0.45)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.DUNGEON, c.kind(), "trial spawner = trial chamber");
    }

    @Test
    void twoMobSpawnersAreNotAutoSuppressed() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(50, 32, 50);
        BaseCandidate c = fm.update(List.of(
                spawner(a, false),
                spawner(a.add(1, 0, 0), false),
                at(a.add(2, 0, 0), FindingCategory.STORAGE, 100, 0.45)), 1).get(0);
        assertEquals(BaseCandidate.CandidateKind.BASE, c.kind(),
                "multiple mob spawners are not auto-classified as a dungeon");
    }

    @Test
    void clustersNearbyFindingsIntoOneCandidate() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(100, 64, 100);
        List<BaseCandidate> cands = fm.update(List.of(
                at(a, FindingCategory.STORAGE, 100, 0.9),
                at(a.add(1, 0, 1), FindingCategory.BED, 100, 0.8)), 1);
        assertEquals(1, cands.size());
        assertEquals(2, cands.get(0).findings().size());
    }

    @Test
    void diverseSignalsRaiseFusedConfidence() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(0, 64, 0);
        int single = fm.update(List.of(at(a, FindingCategory.STORAGE, 70, 0.6)), 1).get(0).fusedConfidence();
        FusionManager fm2 = new FusionManager();
        int multi = fm2.update(List.of(
                at(a, FindingCategory.STORAGE, 70, 0.6),
                at(a, FindingCategory.BED, 70, 0.6),
                at(a, FindingCategory.BEACON, 70, 0.6)), 1).get(0).fusedConfidence();
        assertTrue(multi > single, "more agreeing signals should fuse higher");
    }

    @Test
    void persistenceGrowsAcrossCyclesAndDecaysAway() {
        FusionManager fm = new FusionManager();
        Vec3i a = new Vec3i(10, 64, 10);
        Finding f = at(a, FindingCategory.STORAGE, 80, 0.7);
        int c1 = fm.update(List.of(f), 1).get(0).fusedConfidence();
        int c3 = 0;
        for (int t = 2; t <= 4; t++) {
            c3 = fm.update(List.of(f), t).get(0).fusedConfidence();
        }
        assertTrue(c3 >= c1, "persistence should not lower a continuously-seen candidate");

        // Now stop observing: candidate should disappear within a few cycles.
        boolean gone = false;
        for (int t = 5; t <= 10; t++) {
            if (fm.update(List.of(), t).isEmpty()) {
                gone = true;
                break;
            }
        }
        assertTrue(gone, "an unobserved candidate should decay and be removed");
    }

    @Test
    void resetClearsCandidates() {
        FusionManager fm = new FusionManager();
        fm.update(List.of(at(new Vec3i(0, 64, 0), FindingCategory.STORAGE, 90, 0.9)), 1);
        fm.reset();
        assertTrue(fm.candidates().isEmpty());
    }
}
