package com.basefinder;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.scanner.impl.EntityScanner;
import com.basefinder.test.FakeWorld;
import com.basefinder.test.ScannerHarness;
import com.basefinder.world.EntityRef;
import com.basefinder.world.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityScannerTest {

    @Test
    void detectsPlayerThenFlagsAfkAfterStillness() {
        FakeWorld w = new FakeWorld().player(0, 70, 0)
                .addEntity(new EntityRef(7, "minecraft:player", new Vec3d(5, 70, 5), new Vec3d(0, 0, 0), "Steve"));
        EntityScanner scanner = new EntityScanner();

        w.tick(0);
        List<Finding> first = ScannerHarness.run(scanner, w);
        assertTrue(first.stream().anyMatch(f -> f.category() == FindingCategory.PLAYER));
        assertTrue(first.stream().noneMatch(f -> "true".equals(f.metadata().get("afk"))),
                "not AFK on first observation");

        w.tick(300);
        List<Finding> later = ScannerHarness.run(scanner, w);
        assertTrue(later.stream().anyMatch(f -> "true".equals(f.metadata().get("afk"))),
                "stationary player should be flagged AFK after the idle window");
    }
}
