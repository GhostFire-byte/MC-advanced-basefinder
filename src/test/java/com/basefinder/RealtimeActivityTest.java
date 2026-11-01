package com.basefinder;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.scanner.impl.RealtimeActivityScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RealtimeActivityTest {

    @Test
    void clustersRecentBlockChangesIntoActivity() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).tick(10);
        RealtimeActivityScanner scanner = new RealtimeActivityScanner();
        for (int i = 0; i < 5; i++) {
            scanner.onBlockChange(new Vec3i(5 + i, 70, 5), "minecraft:stone", "minecraft:air", 10);
        }
        List<Finding> fs = ScannerHarness.run(scanner, w);
        assertTrue(fs.stream().anyMatch(f -> f.category() == FindingCategory.ACTIVITY));
    }

    @Test
    void staleChangesAreIgnored() {
        FakeWorld w = new FakeWorld().player(0, 70, 0).tick(5000);
        RealtimeActivityScanner scanner = new RealtimeActivityScanner();
        for (int i = 0; i < 5; i++) {
            scanner.onBlockChange(new Vec3i(5 + i, 70, 5), "minecraft:stone", "minecraft:air", 10);
        }
        assertTrue(ScannerHarness.run(scanner, w).isEmpty(), "changes outside the time window should expire");
    }
}
