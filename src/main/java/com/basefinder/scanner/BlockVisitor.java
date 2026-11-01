package com.basefinder.scanner;

/**
 * A scanner that examines raw blocks. All block visitors share a single sweep
 * over the scope per cycle (the "scan each chunk once" efficiency goal), rather
 * than each iterating the world independently.
 *
 * <p>Lifecycle per cycle: {@link #begin} once, {@link #visitBlock} for every
 * loaded block in scope (in sweep order), then {@link #finish} to emit findings.
 * A visitor instance is only ever driven by one thread per cycle, so it may keep
 * mutable accumulation state between {@code begin} and {@code finish}.
 */
public interface BlockVisitor {

    void begin(ScanContext ctx);

    /** @param id registry id (never null/air here), {@code light} block-light 0..15. */
    void visitBlock(int x, int y, int z, String id, int light);

    void finish(FindingSink sink);

    /** Return true to also receive air blocks (e.g. carved-void detection). Default false. */
    default boolean wantsAir() {
        return false;
    }
}
