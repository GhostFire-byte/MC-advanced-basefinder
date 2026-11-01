package com.basefinder.config;

import java.util.Map;

/**
 * Persistence seam. The module never writes files itself; the host client loads
 * and saves this flat key/value map however it likes (per the delegated-persistence
 * decision). {@link InMemorySettingsBridge} is the standalone/testing default.
 * Only interface settings are ever stored here — analysis findings are never persisted.
 */
public interface SettingsBridge {

    /** Return previously-persisted settings, or an empty map on first run. */
    Map<String, String> load();

    /** Persist the given settings snapshot. Called after any settings change. */
    void persist(Map<String, String> data);
}
