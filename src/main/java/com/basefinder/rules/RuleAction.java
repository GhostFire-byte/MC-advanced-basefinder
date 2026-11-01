package com.basefinder.rules;

import com.basefinder.model.BaseCandidate;
import com.basefinder.model.Severity;

/** A single action applied to a candidate matched by a rule. */
public final class RuleAction {

    private RuleActionType type;
    private String value;

    public RuleAction(RuleActionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public RuleActionType type() {
        return type;
    }

    public void setType(RuleActionType type) {
        this.type = type;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /** Apply to the candidate's transient display state. Returns a notify behavior if set, else null. */
    public NotifyBehavior apply(BaseCandidate c) {
        switch (type) {
            case SET_VISIBLE -> c.setVisible(Boolean.parseBoolean(value));
            case FORCE_HIDE -> {
                c.setForcedHidden(true);
                c.setVisible(false);
            }
            case PIN_RADAR -> c.setPinned(true);
            case SET_COLOR -> c.setColorOverride(parseIntSafe(value));
            case SET_ICON -> c.setIconOverride(value);
            case SET_LABEL -> c.setLabelOverride(value);
            case SET_NOTIFY_SEVERITY -> c.setSeverity(parseSeverity(value, c.severity()));
            case SET_NOTIFY_BEHAVIOR -> {
                return parseBehavior(value);
            }
        }
        return null;
    }

    private static Integer parseIntSafe(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Severity parseSeverity(String v, Severity def) {
        try {
            return Severity.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private static NotifyBehavior parseBehavior(String v) {
        try {
            return NotifyBehavior.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotifyBehavior.DEFAULT;
        }
    }
}
