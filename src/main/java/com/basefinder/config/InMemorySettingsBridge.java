package com.basefinder.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Default in-memory bridge so the module runs and tests without a host client. */
public final class InMemorySettingsBridge implements SettingsBridge {

    private Map<String, String> store = new LinkedHashMap<>();

    @Override
    public Map<String, String> load() {
        return new LinkedHashMap<>(store);
    }

    @Override
    public void persist(Map<String, String> data) {
        this.store = new LinkedHashMap<>(data);
    }

    public Map<String, String> raw() {
        return new HashMap<>(store);
    }
}
