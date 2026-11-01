package com.basefinder;

import com.basefinder.model.Confidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfidenceTest {

    @Test
    void clampsToRange() {
        assertEquals(0, Confidence.clamp(-5));
        assertEquals(100, Confidence.clamp(150));
        assertEquals(42, Confidence.clamp(42));
    }

    @Test
    void noisyOrReinforcesButNeverExceeds100() {
        int combined = Confidence.combine(60, 60);
        assertTrue(combined > 60, "two independent signals should reinforce");
        assertTrue(combined <= 100);
        assertEquals(100, Confidence.combine(100, 50));
    }

    @Test
    void combineAllFoldsMultipleSignals() {
        int c = Confidence.combineAll(List.of(50, 50, 50));
        assertTrue(c > 50 && c <= 100);
    }
}
