package com.basefinder.scanner;

import com.basefinder.model.FindingCategory;

/**
 * A pluggable analysis subsystem. Implementations must be stateless with respect
 * to shared data (they read a snapshot and emit findings), tolerate partial or
 * missing world data, and never throw out of {@link #scan} in a way that could
 * crash the engine (the engine guards this, but scanners should fail soft).
 */
public interface Scanner {

    /** Stable identifier used for settings keys and status, e.g. "storage". */
    String id();

    String displayName();

    String description();

    /** Primary category of findings this scanner produces. */
    FindingCategory category();

    /** Examine the scope and emit findings via the sink. Runs off the render thread. */
    void scan(ScanContext ctx, FindingSink sink);

    /** Called on world change / disconnect so the scanner can drop cached state. */
    default void reset() {
    }
}
