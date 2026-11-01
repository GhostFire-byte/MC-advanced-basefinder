package com.basefinder.model;

/** Severity of a finding, driving notification colour and ordering. */
public enum Severity {
    INFO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    Severity(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    public boolean atLeast(Severity other) {
        return weight >= other.weight;
    }
}
