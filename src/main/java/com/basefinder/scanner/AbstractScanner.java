package com.basefinder.scanner;

import com.basefinder.model.FindingCategory;

/** Convenience base class carrying the identity metadata. */
public abstract class AbstractScanner implements Scanner {

    private final String id;
    private final String displayName;
    private final String description;
    private final FindingCategory category;

    protected AbstractScanner(String id, String displayName, String description, FindingCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public FindingCategory category() {
        return category;
    }
}
