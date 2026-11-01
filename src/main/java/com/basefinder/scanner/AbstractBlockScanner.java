package com.basefinder.scanner;

import com.basefinder.model.FindingCategory;

/**
 * Base for raw-block scanners. The shared sweep drives {@link BlockVisitor};
 * {@link #scan} is intentionally a no-op so these scanners never double-iterate
 * the world.
 */
public abstract class AbstractBlockScanner extends AbstractScanner implements BlockVisitor {

    protected AbstractBlockScanner(String id, String displayName, String description, FindingCategory category) {
        super(id, displayName, description, category);
    }

    @Override
    public final void scan(ScanContext ctx, FindingSink sink) {
        // Driven by the shared block sweep instead; see ScopeSweeper.
    }
}
