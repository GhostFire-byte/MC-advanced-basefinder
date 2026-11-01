package com.basefinder;

import com.basefinder.guidance.DirectionalGuidance;
import com.basefinder.guidance.DirectionalGuidance.GuidanceTarget;
import com.basefinder.model.BaseCandidate;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuidanceTest {

    @Test
    void picksHighestConfidenceAboveFloorAndComputesDistance() {
        DirectionalGuidance g = new DirectionalGuidance();
        BaseCandidate low = new BaseCandidate("low", new Vec3i(10, 64, 0), 0);
        low.setFusedConfidence(78);
        BaseCandidate high = new BaseCandidate("high", new Vec3i(0, 64, 50), 0);
        high.setFusedConfidence(95);

        Optional<GuidanceTarget> t = g.compute(List.of(low, high), new Vec3d(0, 64, 0), 0f, 75);
        assertTrue(t.isPresent());
        assertEquals("high", t.get().candidateId());
        assertEquals(50.0, t.get().distance(), 1.0);
    }

    @Test
    void pinnedCandidateWins() {
        DirectionalGuidance g = new DirectionalGuidance();
        BaseCandidate a = new BaseCandidate("a", new Vec3i(0, 64, 10), 0);
        a.setFusedConfidence(99);
        BaseCandidate b = new BaseCandidate("b", new Vec3i(0, 64, 20), 0);
        b.setFusedConfidence(80);
        b.setPinned(true);
        Optional<GuidanceTarget> t = g.compute(List.of(a, b), new Vec3d(0, 64, 0), 0f, 75);
        assertTrue(t.isPresent());
        assertEquals("b", t.get().candidateId());
    }

    @Test
    void emptyWhenNothingAboveFloor() {
        DirectionalGuidance g = new DirectionalGuidance();
        BaseCandidate a = new BaseCandidate("a", new Vec3i(0, 64, 10), 0);
        a.setFusedConfidence(40);
        assertTrue(g.compute(List.of(a), new Vec3d(0, 64, 0), 0f, 75).isEmpty());
    }
}
