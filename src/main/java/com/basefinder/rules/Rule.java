package com.basefinder.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom rule: a set of condition groups combined by a top-level AND/OR, plus
 * actions to apply to any candidate it matches. This is exactly one level of
 * nesting (rule &rarr; groups &rarr; conditions), which covers real needs without
 * a confusing editor.
 */
public final class Rule {

    private String name;
    private boolean enabled = true;
    private LogicOp groupLogic = LogicOp.AND;
    private final List<ConditionGroup> groups = new ArrayList<>();
    private final List<RuleAction> actions = new ArrayList<>();

    public Rule(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LogicOp groupLogic() {
        return groupLogic;
    }

    public void setGroupLogic(LogicOp groupLogic) {
        this.groupLogic = groupLogic;
    }

    public List<ConditionGroup> groups() {
        return groups;
    }

    public List<RuleAction> actions() {
        return actions;
    }

    public boolean matches(CandidateView v) {
        if (groups.isEmpty()) {
            return false;
        }
        if (groupLogic == LogicOp.AND) {
            for (ConditionGroup g : groups) {
                if (!g.evaluate(v)) {
                    return false;
                }
            }
            return true;
        }
        for (ConditionGroup g : groups) {
            if (g.evaluate(v)) {
                return true;
            }
        }
        return false;
    }
}
