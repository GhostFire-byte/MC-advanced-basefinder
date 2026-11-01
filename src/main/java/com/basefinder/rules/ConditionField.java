package com.basefinder.rules;

/** Fields a rule condition can test on a base candidate. */
public enum ConditionField {
    SCANNER_ID,     // candidate contains a finding from this scanner id
    CATEGORY,       // candidate contains this finding category
    CONFIDENCE,     // highest raw finding confidence
    FUSED_SCORE,    // fused candidate confidence
    DISTANCE,       // distance from player (blocks)
    Y_LEVEL,        // candidate centre Y
    DIMENSION,      // current dimension id
    FINDING_COUNT,  // number of contributing findings
    SEVERITY        // candidate severity weight (0..4)
}
