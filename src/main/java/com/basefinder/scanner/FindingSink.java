package com.basefinder.scanner;

import com.basefinder.model.Finding;

/** Callback a scanner uses to publish findings. */
@FunctionalInterface
public interface FindingSink {
    void emit(Finding finding);
}
