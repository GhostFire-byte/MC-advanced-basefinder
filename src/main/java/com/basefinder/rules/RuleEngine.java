package com.basefinder.rules;

import com.basefinder.model.BaseCandidate;
import com.basefinder.world.Vec3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds and applies custom rules. Each cycle, {@link #apply} resets every
 * candidate's transient display state and re-applies all enabled rules in order,
 * so rule effects (colour, visibility, pin, label, notification behaviour) are
 * deterministic and never leak across cycles.
 */
public final class RuleEngine {

    private final List<Rule> rules = new ArrayList<>();

    public List<Rule> rules() {
        return rules;
    }

    public void add(Rule rule) {
        rules.add(rule);
    }

    public void remove(Rule rule) {
        rules.remove(rule);
    }

    public void clear() {
        rules.clear();
    }

    /** Apply all enabled rules to the candidates; returns per-candidate notify overrides. */
    public Map<String, NotifyBehavior> apply(List<BaseCandidate> candidates, Vec3d playerPos, String dimension) {
        Map<String, NotifyBehavior> notifyOverrides = new LinkedHashMap<>();
        for (BaseCandidate c : candidates) {
            c.resetRuleState();
            CandidateView view = new CandidateView(c, playerPos, dimension);
            for (Rule rule : rules) {
                if (!rule.enabled() || !rule.matches(view)) {
                    continue;
                }
                for (RuleAction action : rule.actions()) {
                    NotifyBehavior behavior = action.apply(c);
                    if (behavior != null) {
                        notifyOverrides.put(c.id(), behavior);
                    }
                }
            }
        }
        return notifyOverrides;
    }

    // ---- persistence helpers --------------------------------------------

    public List<String> export() {
        List<String> out = new ArrayList<>();
        for (Rule r : rules) {
            out.add(RuleCodec.encode(r));
        }
        return out;
    }

    public void importFrom(List<String> lines) {
        rules.clear();
        for (String line : lines) {
            Rule r = RuleCodec.decode(line);
            if (r != null) {
                rules.add(r);
            }
        }
    }
}
