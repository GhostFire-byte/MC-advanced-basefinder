package com.basefinder.rules;

import java.util.ArrayList;
import java.util.List;

/** Validates rules before they are saved/applied, so the editor can show errors. */
public final class RuleValidator {

    private RuleValidator() {
    }

    public static List<String> validate(Rule rule) {
        List<String> errors = new ArrayList<>();
        if (rule.name() == null || rule.name().isBlank()) {
            errors.add("Rule name is required");
        }
        if (rule.groups().isEmpty()) {
            errors.add("Rule needs at least one condition group");
        }
        boolean anyCondition = false;
        for (ConditionGroup g : rule.groups()) {
            for (Condition c : g.conditions()) {
                anyCondition = true;
                if (c.value() == null || c.value().isBlank()) {
                    errors.add("Condition on " + c.field() + " has an empty value");
                } else if (c.operator().isNumeric() && !isNumber(c.value())) {
                    errors.add("Condition on " + c.field() + " " + c.operator().symbol()
                            + " needs a number, got '" + c.value() + "'");
                }
            }
        }
        if (!anyCondition) {
            errors.add("Rule has no conditions");
        }
        if (rule.actions().isEmpty()) {
            errors.add("Rule has no actions");
        }
        return errors;
    }

    public static boolean isValid(Rule rule) {
        return validate(rule).isEmpty();
    }

    private static boolean isNumber(String s) {
        try {
            Double.parseDouble(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
