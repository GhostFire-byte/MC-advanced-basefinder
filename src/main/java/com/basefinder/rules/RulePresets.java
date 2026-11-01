package com.basefinder.rules;

import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;

import java.util.ArrayList;
import java.util.List;

/** A handful of ready-made rules users can add from the editor. */
public final class RulePresets {

    private RulePresets() {
    }

    public static List<Rule> all() {
        List<Rule> list = new ArrayList<>();
        list.add(highlightIllegalRed());
        list.add(pinBeacons());
        list.add(alwaysNotifyHighConfidence());
        list.add(hideDistantLowConfidence());
        return list;
    }

    /** Colour illegal-block candidates bright red. */
    public static Rule highlightIllegalRed() {
        Rule r = new Rule("Highlight illegal blocks red");
        r.groups().add(new ConditionGroup(LogicOp.OR)
                .add(new Condition(ConditionField.CATEGORY, Operator.EQ, FindingCategory.ILLEGAL.name())));
        r.actions().add(new RuleAction(RuleActionType.SET_COLOR, String.valueOf(0xFFFF3030)));
        r.actions().add(new RuleAction(RuleActionType.PIN_RADAR, "true"));
        return r;
    }

    /** Pin beacon candidates on the radar. */
    public static Rule pinBeacons() {
        Rule r = new Rule("Pin beacons");
        r.groups().add(new ConditionGroup(LogicOp.OR)
                .add(new Condition(ConditionField.CATEGORY, Operator.EQ, FindingCategory.BEACON.name())));
        r.actions().add(new RuleAction(RuleActionType.PIN_RADAR, "true"));
        return r;
    }

    /** Always notify for very high confidence candidates. */
    public static Rule alwaysNotifyHighConfidence() {
        Rule r = new Rule("Always notify >= 90%");
        r.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.GTE, "90")));
        r.actions().add(new RuleAction(RuleActionType.SET_NOTIFY_BEHAVIOR, NotifyBehavior.ALWAYS.name()));
        r.actions().add(new RuleAction(RuleActionType.SET_NOTIFY_SEVERITY, Severity.CRITICAL.name()));
        return r;
    }

    /** Hide distant, low-confidence candidates to reduce clutter. */
    public static Rule hideDistantLowConfidence() {
        Rule r = new Rule("Hide distant low-confidence");
        r.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.DISTANCE, Operator.GT, "200"))
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.LT, "80")));
        r.actions().add(new RuleAction(RuleActionType.FORCE_HIDE, "true"));
        return r;
    }
}
