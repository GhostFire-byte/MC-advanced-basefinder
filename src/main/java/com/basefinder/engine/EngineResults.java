package com.basefinder.engine;

import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;

import java.util.Collections;
import java.util.List;

/** Immutable snapshot of a completed cycle's output, published for the UI to read. */
public record EngineResults(List<BaseCandidate> candidates, List<Finding> rawFindings, long tick) {

    public static EngineResults empty() {
        return new EngineResults(List.of(), List.of(), 0);
    }

    public EngineResults {
        candidates = Collections.unmodifiableList(List.copyOf(candidates));
        rawFindings = Collections.unmodifiableList(List.copyOf(rawFindings));
    }
}
