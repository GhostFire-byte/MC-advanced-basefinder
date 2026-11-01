package com.basefinder;

import com.basefinder.config.GlobalSettings;
import com.basefinder.model.BaseCandidate;
import com.basefinder.notify.NotificationManager;
import com.basefinder.rules.NotifyBehavior;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationManagerTest {

    private BaseCandidate candidate(String id, int fused) {
        BaseCandidate c = new BaseCandidate(id, new Vec3i(0, 64, 0), 0);
        c.setFusedConfidence(fused);
        return c;
    }

    @Test
    void notifiesOncePerCandidate() {
        NotificationManager nm = new NotificationManager();
        GlobalSettings g = new GlobalSettings();
        g.setConfidenceFloor(75);
        g.setNotifyOncePerCandidate(true);
        BaseCandidate c = candidate("a", 80);

        nm.process(List.of(c), Map.of(), g, 10);
        int afterFirst = nm.recent().size();
        nm.process(List.of(c), Map.of(), g, 400);
        assertEquals(afterFirst, nm.recent().size(), "notify-once should suppress repeats");
        assertEquals(1, afterFirst);
    }

    @Test
    void belowFloorDoesNotNotify() {
        NotificationManager nm = new NotificationManager();
        GlobalSettings g = new GlobalSettings();
        g.setConfidenceFloor(75);
        nm.process(List.of(candidate("a", 50)), Map.of(), g, 1);
        assertEquals(0, nm.recent().size());
    }

    @Test
    void silentOverrideSuppresses() {
        NotificationManager nm = new NotificationManager();
        GlobalSettings g = new GlobalSettings();
        g.setConfidenceFloor(75);
        BaseCandidate c = candidate("a", 90);
        nm.process(List.of(c), Map.of("a", NotifyBehavior.SILENT), g, 1);
        assertEquals(0, nm.recent().size());
    }

    @Test
    void alwaysOverrideBypassesOnceLimit() {
        NotificationManager nm = new NotificationManager();
        GlobalSettings g = new GlobalSettings();
        g.setConfidenceFloor(75);
        g.setNotifyOncePerCandidate(true);
        g.setNotifyCooldownTicks(0);
        BaseCandidate c = candidate("a", 90);
        nm.process(List.of(c), Map.of("a", NotifyBehavior.ALWAYS), g, 1);
        nm.process(List.of(c), Map.of("a", NotifyBehavior.ALWAYS), g, 2);
        assertEquals(2, nm.recent().size());
    }
}
