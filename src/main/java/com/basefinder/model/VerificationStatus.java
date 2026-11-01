package com.basefinder.model;

/**
 * How trustworthy a result is. The spec requires never presenting assumptions
 * as confirmed facts: UNVERIFIED items render separately as "observations".
 */
public enum VerificationStatus {
    /** Directly observed block/entity that definitively exists (e.g. a chest tile entity). */
    VERIFIED,
    /** Heuristic inference with a confidence score (e.g. a terrain anomaly). */
    HEURISTIC,
    /** Weak signal shown only as an unverified observation. */
    UNVERIFIED
}
