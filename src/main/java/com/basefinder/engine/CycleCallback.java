package com.basefinder.engine;

import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;

import java.util.List;

/**
 * Invoked (on a background thread) after fusion but before results are published,
 * so the module can apply custom rules to candidates and raise notifications.
 */
public interface CycleCallback {
    void onCycleComplete(List<BaseCandidate> candidates, List<Finding> rawFindings, long tick);
}
