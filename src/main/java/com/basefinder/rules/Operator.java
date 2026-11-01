package com.basefinder.rules;

/** Comparison operators for rule conditions. */
public enum Operator {
    EQ("="), NEQ("!="), GT(">"), GTE(">="), LT("<"), LTE("<="), CONTAINS("contains");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public boolean isNumeric() {
        return this == GT || this == GTE || this == LT || this == LTE;
    }
}
