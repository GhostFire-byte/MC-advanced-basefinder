package com.basefinder;

import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.rules.Condition;
import com.basefinder.rules.ConditionField;
import com.basefinder.rules.ConditionGroup;
import com.basefinder.rules.LogicOp;
import com.basefinder.rules.NotifyBehavior;
import com.basefinder.rules.Operator;
import com.basefinder.rules.Rule;
import com.basefinder.rules.RuleAction;
import com.basefinder.rules.RuleActionType;
import com.basefinder.rules.RuleCodec;
import com.basefinder.rules.RuleEngine;
import com.basefinder.rules.RuleValidator;
import com.basefinder.world.Vec3d;
import com.basefinder.world.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEngineTest {

    private BaseCandidate candidate(int fused, FindingCategory cat) {
        BaseCandidate c = new BaseCandidate("c1", new Vec3i(0, 64, 0), 0);
        c.replaceFindings(List.of(
                Finding.builder("sc", cat, new Vec3i(0, 64, 0)).confidence(fused).build()));
        c.setFusedConfidence(fused);
        return c;
    }

    @Test
    void forceHideActionHidesCandidate() {
        Rule r = new Rule("hide");
        r.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.LT, "80")));
        r.actions().add(new RuleAction(RuleActionType.FORCE_HIDE, "true"));
        RuleEngine engine = new RuleEngine();
        engine.add(r);

        BaseCandidate c = candidate(70, FindingCategory.STORAGE);
        engine.apply(List.of(c), new Vec3d(0, 64, 0), "minecraft:overworld");
        assertTrue(c.forcedHidden());
        assertFalse(c.visible());
    }

    @Test
    void categoryConditionAndPinAction() {
        Rule r = new Rule("pin beacons");
        r.groups().add(new ConditionGroup(LogicOp.OR)
                .add(new Condition(ConditionField.CATEGORY, Operator.EQ, FindingCategory.BEACON.name())));
        r.actions().add(new RuleAction(RuleActionType.PIN_RADAR, "true"));
        RuleEngine engine = new RuleEngine();
        engine.add(r);

        BaseCandidate beacon = candidate(90, FindingCategory.BEACON);
        BaseCandidate storage = candidate(90, FindingCategory.STORAGE);
        engine.apply(List.of(beacon, storage), new Vec3d(0, 64, 0), "minecraft:overworld");
        assertTrue(beacon.pinned());
        assertFalse(storage.pinned());
    }

    @Test
    void notifyBehaviorOverrideReturned() {
        Rule r = new Rule("always notify");
        r.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.GTE, "90")));
        r.actions().add(new RuleAction(RuleActionType.SET_NOTIFY_BEHAVIOR, NotifyBehavior.ALWAYS.name()));
        RuleEngine engine = new RuleEngine();
        engine.add(r);

        BaseCandidate c = candidate(95, FindingCategory.STORAGE);
        Map<String, NotifyBehavior> overrides = engine.apply(List.of(c), new Vec3d(0, 0, 0), "minecraft:overworld");
        assertEquals(NotifyBehavior.ALWAYS, overrides.get("c1"));
    }

    @Test
    void ruleStateResetsEachApply() {
        Rule r = new Rule("pin high");
        r.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.GTE, "90")));
        r.actions().add(new RuleAction(RuleActionType.PIN_RADAR, "true"));
        RuleEngine engine = new RuleEngine();
        engine.add(r);

        BaseCandidate c = candidate(95, FindingCategory.STORAGE);
        engine.apply(List.of(c), new Vec3d(0, 0, 0), "d");
        assertTrue(c.pinned());
        c.setFusedConfidence(50);
        engine.apply(List.of(c), new Vec3d(0, 0, 0), "d");
        assertFalse(c.pinned(), "rule state must reset so stale pins clear");
    }

    @Test
    void codecRoundTrips() {
        Rule r = new Rule("Custom: label with spaces & |pipes|");
        r.setGroupLogic(LogicOp.OR);
        ConditionGroup g = new ConditionGroup(LogicOp.AND);
        g.add(new Condition(ConditionField.DISTANCE, Operator.GT, "200"));
        g.add(new Condition(ConditionField.CATEGORY, Operator.EQ, "STORAGE"));
        r.groups().add(g);
        r.actions().add(new RuleAction(RuleActionType.SET_LABEL, "Has|weird;chars^here"));

        Rule back = RuleCodec.decode(RuleCodec.encode(r));
        assertEquals(r.name(), back.name());
        assertEquals(1, back.groups().size());
        assertEquals(2, back.groups().get(0).conditions().size());
        assertEquals("Has|weird;chars^here", back.actions().get(0).value());
    }

    @Test
    void validatorCatchesBadRules() {
        Rule empty = new Rule("");
        assertFalse(RuleValidator.isValid(empty));
        Rule numeric = new Rule("num");
        numeric.groups().add(new ConditionGroup(LogicOp.AND)
                .add(new Condition(ConditionField.FUSED_SCORE, Operator.GT, "notanumber")));
        numeric.actions().add(new RuleAction(RuleActionType.PIN_RADAR, "true"));
        assertFalse(RuleValidator.isValid(numeric));
    }
}
