package com.basefinder.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Central settings holder. Serialises to / from the flat key-value map that the
 * host {@link SettingsBridge} persists. Only interface settings live here;
 * analysis findings are never stored.
 */
public final class Settings {

    private final GlobalSettings global = new GlobalSettings();
    private final Map<String, SubsystemSettings> subsystems = new LinkedHashMap<>();

    private String themeName = "Premium";
    private String styleMode = "POLISHED"; // POLISHED or FLAT
    private final Map<String, String> layout = new LinkedHashMap<>();
    private final List<String> ruleData = new ArrayList<>();

    public GlobalSettings global() {
        return global;
    }

    public SubsystemSettings subsystem(String scannerId) {
        return subsystems.computeIfAbsent(scannerId, k -> new SubsystemSettings());
    }

    public Map<String, SubsystemSettings> subsystems() {
        return subsystems;
    }

    public String themeName() {
        return themeName;
    }

    public void setThemeName(String v) {
        this.themeName = v;
    }

    public String styleMode() {
        return styleMode;
    }

    public void setStyleMode(String v) {
        this.styleMode = v;
    }

    public Map<String, String> layout() {
        return layout;
    }

    public List<String> ruleData() {
        return ruleData;
    }

    // ---- persistence -----------------------------------------------------

    public void load(SettingsBridge bridge) {
        Map<String, String> m = bridge.load();
        if (m == null || m.isEmpty()) {
            return;
        }
        global.setDetectionRadiusChunks(getInt(m, "global.radiusChunks", global.detectionRadiusChunks()));
        global.setBindRadiusToRenderDistance(getBool(m, "global.bindRadius", global.bindRadiusToRenderDistance()));
        global.setMinY(getInt(m, "global.minY", global.minY()));
        global.setMaxY(getInt(m, "global.maxY", global.maxY()));
        global.setConfidenceFloor(getInt(m, "global.confidenceFloor", global.confidenceFloor()));
        global.setRadarRange(getInt(m, "global.radarRange", global.radarRange()));
        global.setBindRadarToRenderDistance(getBool(m, "global.bindRadar", global.bindRadarToRenderDistance()));
        global.setNotifyOnlyNewCandidates(getBool(m, "global.notifyNewOnly", global.notifyOnlyNewCandidates()));
        global.setNotifyOncePerCandidate(getBool(m, "global.notifyOnce", global.notifyOncePerCandidate()));
        global.setNotifyCooldownTicks(getInt(m, "global.notifyCooldown", global.notifyCooldownTicks()));
        global.setOverlayEnabled(getBool(m, "global.overlay", global.overlayEnabled()));
        global.setWdaExcludeFromCapture(getBool(m, "global.wda", global.wdaExcludeFromCapture()));
        global.setEspOutlines(getBool(m, "esp.outlines", global.espOutlines()));
        global.setEspBoxes(getBool(m, "esp.boxes", global.espBoxes()));
        global.setEspLabels(getBool(m, "esp.labels", global.espLabels()));
        global.setEspIcons(getBool(m, "esp.icons", global.espIcons()));
        global.setRadarEnabled(getBool(m, "esp.radar", global.radarEnabled()));
        global.setSpEngine(getBool(m, "sp.engine", global.spEngine()));
        global.setSpSubsystems(getBool(m, "sp.subsystems", global.spSubsystems()));
        global.setSpDataHealth(getBool(m, "sp.dataHealth", global.spDataHealth()));
        global.setSpPerformance(getBool(m, "sp.performance", global.spPerformance()));
        global.setSpActivity(getBool(m, "sp.activity", global.spActivity()));
        global.setSpResultCounts(getBool(m, "sp.resultCounts", global.spResultCounts()));
        global.setSpErrors(getBool(m, "sp.errors", global.spErrors()));
        try {
            global.setAggressiveness(ScanAggressiveness.valueOf(
                    m.getOrDefault("global.aggressiveness", global.aggressiveness().name())));
        } catch (IllegalArgumentException ignored) {
            // keep default
        }

        themeName = m.getOrDefault("theme.name", themeName);
        styleMode = m.getOrDefault("theme.style", styleMode);

        for (Map.Entry<String, String> e : m.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("sub.")) {
                parseSubsystemEntry(k.substring(4), e.getValue());
            } else if (k.startsWith("layout.")) {
                layout.put(k.substring(7), e.getValue());
            } else if (k.startsWith("rule.")) {
                ruleData.add(e.getValue());
            }
        }
    }

    private void parseSubsystemEntry(String rest, String value) {
        // rest = "<scannerId>.<field>" where field is enabled | floor | p.<name>
        int firstDot = rest.indexOf('.');
        if (firstDot < 0) {
            return;
        }
        String scannerId = rest.substring(0, firstDot);
        String field = rest.substring(firstDot + 1);
        SubsystemSettings s = subsystem(scannerId);
        if (field.equals("enabled")) {
            s.setEnabled(Boolean.parseBoolean(value));
        } else if (field.equals("floor")) {
            s.setConfidenceOverride(value.isEmpty() ? null : safeInt(value, null));
        } else if (field.startsWith("p.")) {
            s.params().put(field.substring(2), value);
        }
    }

    public void save(SettingsBridge bridge) {
        bridge.persist(toMap());
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new TreeMap<>();
        m.put("global.radiusChunks", String.valueOf(global.detectionRadiusChunks()));
        m.put("global.bindRadius", String.valueOf(global.bindRadiusToRenderDistance()));
        m.put("global.minY", String.valueOf(global.minY()));
        m.put("global.maxY", String.valueOf(global.maxY()));
        m.put("global.confidenceFloor", String.valueOf(global.confidenceFloor()));
        m.put("global.radarRange", String.valueOf(global.radarRange()));
        m.put("global.bindRadar", String.valueOf(global.bindRadarToRenderDistance()));
        m.put("global.notifyNewOnly", String.valueOf(global.notifyOnlyNewCandidates()));
        m.put("global.notifyOnce", String.valueOf(global.notifyOncePerCandidate()));
        m.put("global.notifyCooldown", String.valueOf(global.notifyCooldownTicks()));
        m.put("global.overlay", String.valueOf(global.overlayEnabled()));
        m.put("global.wda", String.valueOf(global.wdaExcludeFromCapture()));
        m.put("esp.outlines", String.valueOf(global.espOutlines()));
        m.put("esp.boxes", String.valueOf(global.espBoxes()));
        m.put("esp.labels", String.valueOf(global.espLabels()));
        m.put("esp.icons", String.valueOf(global.espIcons()));
        m.put("esp.radar", String.valueOf(global.radarEnabled()));
        m.put("sp.engine", String.valueOf(global.spEngine()));
        m.put("sp.subsystems", String.valueOf(global.spSubsystems()));
        m.put("sp.dataHealth", String.valueOf(global.spDataHealth()));
        m.put("sp.performance", String.valueOf(global.spPerformance()));
        m.put("sp.activity", String.valueOf(global.spActivity()));
        m.put("sp.resultCounts", String.valueOf(global.spResultCounts()));
        m.put("sp.errors", String.valueOf(global.spErrors()));
        m.put("global.aggressiveness", global.aggressiveness().name());
        m.put("theme.name", themeName);
        m.put("theme.style", styleMode);

        for (Map.Entry<String, SubsystemSettings> e : subsystems.entrySet()) {
            String id = e.getKey();
            SubsystemSettings s = e.getValue();
            m.put("sub." + id + ".enabled", String.valueOf(s.enabled()));
            m.put("sub." + id + ".floor", s.confidenceOverride() == null ? "" : String.valueOf(s.confidenceOverride()));
            for (Map.Entry<String, String> p : s.params().entrySet()) {
                m.put("sub." + id + ".p." + p.getKey(), p.getValue());
            }
        }
        for (Map.Entry<String, String> e : layout.entrySet()) {
            m.put("layout." + e.getKey(), e.getValue());
        }
        for (int i = 0; i < ruleData.size(); i++) {
            m.put("rule." + String.format("%04d", i), ruleData.get(i));
        }
        return m;
    }

    private static int getInt(Map<String, String> m, String k, int def) {
        return safeInt(m.get(k), def);
    }

    private static int safeInt(String v, Integer def) {
        try {
            return v == null ? (def == null ? 0 : def) : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def == null ? 0 : def;
        }
    }

    private static boolean getBool(Map<String, String> m, String k, boolean def) {
        String v = m.get(k);
        return v == null ? def : Boolean.parseBoolean(v);
    }
}
