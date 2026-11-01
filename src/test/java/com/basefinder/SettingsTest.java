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
    void persistsGuiStyleAndPanelLayout() {
        InMemorySettingsBridge bridge = new InMemorySettingsBridge();
        Settings s = new Settings();
        s.setGuiStyle("IMGUI");
        com.basefinder.ui.UiState state = new com.basefinder.ui.UiState();
        state.uiScale = 1.4;
        state.dashX = 123;
        state.dashY = 45;
        state.dashW = 700;
        state.radarCx = 900;
        state.exportTo(s.layout());
        s.save(bridge);

        Settings loaded = new Settings();
        loaded.load(bridge);
        assertEquals("IMGUI", loaded.guiStyle());
        com.basefinder.ui.UiState restored = new com.basefinder.ui.UiState();
        restored.importFrom(loaded.layout());
        assertEquals(1.4, restored.uiScale, 1.0e-9);
        assertEquals(Integer.valueOf(123), restored.dashX);
        assertEquals(Integer.valueOf(45), restored.dashY);
        assertEquals(Integer.valueOf(700), restored.dashW);
        assertEquals(Integer.valueOf(900), restored.radarCx);
        assertEquals(null, restored.dashH); // unset stays unset
    }

    @Test
    void perSubsystemFloorIsStricterOfTwo() {
        Settings s = new Settings();
        s.subsystem("x").setConfidenceOverride(90);
        assertEquals(90, s.subsystem("x").effectiveFloor(75));
        assertEquals(95, s.subsystem("x").effectiveFloor(95));
    }
}
