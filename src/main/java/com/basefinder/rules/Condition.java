package com.basefinder.rules;

/** One leaf condition: field OP value. */
public final class Condition {

    private ConditionField field;
    private Operator operator;
    private String value;

    public Condition(ConditionField field, Operator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public ConditionField field() {
        return field;
    }

    public void setField(ConditionField field) {
        this.field = field;
    }

    public Operator operator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean evaluate(CandidateView v) {
        return switch (field) {
            case SCANNER_ID -> setContains(v.scannerIds(), value);
            case CATEGORY -> setContains(v.categories(), value);
            case DIMENSION -> compareString(v.dimension());
            case CONFIDENCE -> compareNumber(v.maxConfidence());
            case FUSED_SCORE -> compareNumber(v.fusedScore());
            case DISTANCE -> compareNumber(v.distance());
            case Y_LEVEL -> compareNumber(v.yLevel());
            case FINDING_COUNT -> compareNumber(v.findingCount());
            case SEVERITY -> compareNumber(v.severityWeight());
        };
    }

    private boolean setContains(java.util.Set<String> set, String needle) {
        if (operator == Operator.NEQ) {
            return set.stream().noneMatch(s -> s.equalsIgnoreCase(needle));
        }
        // EQ / CONTAINS both mean membership here.
        return set.stream().anyMatch(s -> s.equalsIgnoreCase(needle)
                || (operator == Operator.CONTAINS && s.toLowerCase().contains(needle.toLowerCase())));
    }

    private boolean compareString(String actual) {
        if (actual == null) {
            actual = "";
        }
        return switch (operator) {
            case EQ -> actual.equalsIgnoreCase(value);
            case NEQ -> !actual.equalsIgnoreCase(value);
            case CONTAINS -> actual.toLowerCase().contains(value.toLowerCase());
            default -> false;
        };
    }

    private boolean compareNumber(double actual) {
        double target;
        try {
            target = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        return switch (operator) {
            case EQ -> actual == target;
            case NEQ -> actual != target;
            case GT -> actual > target;
            case GTE -> actual >= target;
            case LT -> actual < target;
            case LTE -> actual <= target;
            case CONTAINS -> false;
        };
    }
}
