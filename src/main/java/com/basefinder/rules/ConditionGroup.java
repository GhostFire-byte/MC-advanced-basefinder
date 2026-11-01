package com.basefinder.rules;

import java.util.ArrayList;
import java.util.List;

/** A group of leaf conditions combined with AND/OR. One level of nesting: rules hold groups. */
public final class ConditionGroup {

    private LogicOp logic = LogicOp.AND;
    private final List<Condition> conditions = new ArrayList<>();

    public ConditionGroup() {
    }

    public ConditionGroup(LogicOp logic) {
        this.logic = logic;
    }

    public LogicOp logic() {
        return logic;
    }

    public void setLogic(LogicOp logic) {
        this.logic = logic;
    }

    public List<Condition> conditions() {
        return conditions;
    }

    public ConditionGroup add(Condition c) {
        conditions.add(c);
        return this;
    }

    public boolean evaluate(CandidateView v) {
        if (conditions.isEmpty()) {
            return true;
        }
        if (logic == LogicOp.AND) {
            for (Condition c : conditions) {
                if (!c.evaluate(v)) {
                    return false;
                }
            }
            return true;
        }
        for (Condition c : conditions) {
            if (c.evaluate(v)) {
                return true;
            }
        }
        return false;
    }
}
