package com.basefinder.rules;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Serialises a {@link Rule} to/from a single line so the host settings bridge can
 * persist it. Free-text values are Base64-encoded to stay delimiter-safe.
 * Format: {@code name|enabled|groupLogic|group;group|action;action}
 * where group = {@code logic~cond^cond} and cond = {@code field,op,valueB64}.
 */
public final class RuleCodec {

    private RuleCodec() {
    }

    public static String encode(Rule rule) {
        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < rule.groups().size(); i++) {
            if (i > 0) {
                groups.append(';');
            }
            ConditionGroup g = rule.groups().get(i);
            groups.append(g.logic().name()).append('~');
            for (int j = 0; j < g.conditions().size(); j++) {
                if (j > 0) {
                    groups.append('^');
                }
                Condition c = g.conditions().get(j);
                groups.append(c.field().name()).append(',')
                        .append(c.operator().name()).append(',')
                        .append(b64(c.value()));
            }
        }
        StringBuilder actions = new StringBuilder();
        for (int i = 0; i < rule.actions().size(); i++) {
            if (i > 0) {
                actions.append(';');
            }
            RuleAction a = rule.actions().get(i);
            actions.append(a.type().name()).append(',').append(b64(a.value()));
        }
        return String.join("|",
                b64(rule.name()),
                String.valueOf(rule.enabled()),
                rule.groupLogic().name(),
                groups.toString(),
                actions.toString());
    }

    public static Rule decode(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5) {
            return null;
        }
        Rule rule = new Rule(unb64(parts[0]));
        rule.setEnabled(Boolean.parseBoolean(parts[1]));
        rule.setGroupLogic(safeLogic(parts[2]));
        if (!parts[3].isEmpty()) {
            for (String gStr : parts[3].split(";", -1)) {
                int tilde = gStr.indexOf('~');
                if (tilde < 0) {
                    continue;
                }
                ConditionGroup g = new ConditionGroup(safeLogic(gStr.substring(0, tilde)));
                String condStr = gStr.substring(tilde + 1);
                if (!condStr.isEmpty()) {
                    for (String cStr : condStr.split("\\^", -1)) {
                        String[] cf = cStr.split(",", 3);
                        if (cf.length == 3) {
                            g.add(new Condition(safeField(cf[0]), safeOp(cf[1]), unb64(cf[2])));
                        }
                    }
                }
                rule.groups().add(g);
            }
        }
        if (!parts[4].isEmpty()) {
            for (String aStr : parts[4].split(";", -1)) {
                String[] af = aStr.split(",", 2);
                if (af.length == 2) {
                    RuleActionType t = safeActionType(af[0]);
                    if (t != null) {
                        rule.actions().add(new RuleAction(t, unb64(af[1])));
                    }
                }
            }
        }
        return rule;
    }

    private static String b64(String s) {
        return Base64.getEncoder().encodeToString((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
    }

    private static String unb64(String s) {
        try {
            return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private static LogicOp safeLogic(String s) {
        try {
            return LogicOp.valueOf(s);
        } catch (IllegalArgumentException e) {
            return LogicOp.AND;
        }
    }

    private static ConditionField safeField(String s) {
        try {
            return ConditionField.valueOf(s);
        } catch (IllegalArgumentException e) {
            return ConditionField.CONFIDENCE;
        }
    }

    private static Operator safeOp(String s) {
        try {
            return Operator.valueOf(s);
        } catch (IllegalArgumentException e) {
            return Operator.GTE;
        }
    }

    private static RuleActionType safeActionType(String s) {
        try {
            return RuleActionType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
