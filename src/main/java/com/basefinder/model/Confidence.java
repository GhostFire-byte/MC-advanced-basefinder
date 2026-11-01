package com.basefinder.model;

/** Confidence helpers. Confidence is an integer percentage 0..100 everywhere. */
public final class Confidence {

    public static final int MIN = 0;
    public static final int MAX = 100;

    private Confidence() {
    }

    public static int clamp(int v) {
        return Math.max(MIN, Math.min(MAX, v));
    }

    public static int clamp(double v) {
        return clamp((int) Math.round(v));
    }

    /**
     * Combine independent confidences with a probabilistic OR (noisy-or):
     * multiple weak-but-independent signals reinforce each other without ever
     * exceeding 100%. Used by the fusion manager.
     */
    public static int combine(int a, int b) {
        double pa = a / 100.0, pb = b / 100.0;
        double combined = 1.0 - (1.0 - pa) * (1.0 - pb);
        return clamp(combined * 100.0);
    }

    public static int combineAll(Iterable<Integer> values) {
        double notAny = 1.0;
        for (int v : values) {
            notAny *= (1.0 - clamp(v) / 100.0);
        }
        return clamp((1.0 - notAny) * 100.0);
    }
}
