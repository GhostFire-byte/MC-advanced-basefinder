package com.basefinder.rules;

/** What a rule action changes about a matched candidate. */
public enum RuleActionType {
    SET_VISIBLE,         // value "true"/"false"
    FORCE_HIDE,          // hide regardless of threshold
    PIN_RADAR,           // pin/prioritise on radar
    SET_COLOR,           // value = ARGB int
    SET_ICON,            // value = icon id
    SET_LABEL,           // value = label text
    SET_NOTIFY_SEVERITY, // value = Severity name
    SET_NOTIFY_BEHAVIOR  // value = NotifyBehavior name
}
