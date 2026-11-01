package com.basefinder;

import com.basefinder.config.InMemorySettingsBridge;
import com.basefinder.config.ScanAggressiveness;
import com.basefinder.config.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SettingsTest {

    @Test
    void roundTripsThroughBridge() {
        InMemorySettingsBridge bridge = new InMemorySettingsBridge();
        Settings s = new Settings();
        s.global().setConfidenceFloor(82);
        s.global().setDetectionRadiusChunks(10);
        s.global().setAggressiveness(ScanAggressiveness.AGGRESSIVE);
        s.global().setEspLabels(false);
        s.subsystem("storage").setEnabled(false);
        s.subsystem("terrain").params().put("det.geometric", "false");
        s.setThemeName("Neon");
        s.ruleData().add("rule-line-1");
        s.save(bridge);

        Settings loaded = new Settings();
        loaded.load(bridge);
        assertEquals(82, loaded.global().confidenceFloor());
        assertEquals(10, loaded.global().detectionRadiusChunks());
        assertEquals(ScanAggressiveness.AGGRESSIVE, loaded.global().aggressiveness());
        assertFalse(loaded.global().espLabels());
        assertFalse(loaded.subsystem("storage").enabled());
        assertEquals("false", loaded.subsystem("terrain").params().get("det.geometric"));
        assertEquals("Neon", loaded.themeName());
        assertEquals(1, loaded.ruleData().size());
    }

    @Test
    void perSubsystemFloorIsStricterOfTwo() {
        Settings s = new Settings();
        s.subsystem("x").setConfidenceOverride(90);
        assertEquals(90, s.subsystem("x").effectiveFloor(75));
        assertEquals(95, s.subsystem("x").effectiveFloor(95));
    }
}
