package com.basefinder.notify;

import com.basefinder.model.Severity;

/** An immutable notification the module raises; the host may also consume these. */
public record Notification(String candidateId, String title, String message, Severity severity, long tick) {
}
