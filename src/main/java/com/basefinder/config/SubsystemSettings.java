package com.basefinder.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-subsystem settings: an on/off toggle, an optional stricter confidence
 * override (null = use the global floor), and a free-form parameter map each
 * scanner interprets itself.
 */
public final class SubsystemSettings {

    private boolean enabled = true;
    private Integer confidenceOverride; // null => use global floor
    private final Map<String, String> params = new LinkedHashMap<>();

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
    }

    public Integer confidenceOverride() {
        return confidenceOverride;
    }

    public void setConfidenceOverride(Integer v) {
        this.confidenceOverride = v == null ? null : Math.max(0, Math.min(100, v));
    }

    /** Effective floor for this subsystem: the stricter of global and override. */
    public int effectiveFloor(int globalFloor) {
        return confidenceOverride == null ? globalFloor : Math.max(globalFloor, confidenceOverride);
    }

    public Map<String, String> params() {
        return params;
    }

    public String param(String key, String def) {
        return params.getOrDefault(key, def);
    }

    public int paramInt(String key, int def) {
        try {
            String v = params.get(key);
            return v == null ? def : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public boolean paramBool(String key, boolean def) {
        String v = params.get(key);
        return v == null ? def : Boolean.parseBoolean(v);
    }
}
