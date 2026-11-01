package com.basefinder.ui;

/**
 * Frame-rate-independent exponential easing animator (the smoothing WindUI uses).
 * Each instance eases its value toward a target using wall-clock time, so UI
 * animations (hover, toggle, expand) are smooth regardless of frame rate.
 */
public final class UiAnim {

    private double value;
    private long lastNanos;

    public UiAnim(double initial) {
        this.value = initial;
    }

    public double value() {
        return value;
    }

    public void set(double v) {
        this.value = v;
    }

    /** Eases toward {@code target} at {@code speed} (higher = snappier) and returns the new value. */
    public double step(double target, double speed) {
        long now = System.nanoTime();
        double dt = lastNanos == 0 ? 0.016 : (now - lastNanos) / 1.0e9;
        lastNanos = now;
        dt = Math.max(0.0, Math.min(0.1, dt));
        value += (target - value) * (1.0 - Math.exp(-dt * speed));
        if (Math.abs(target - value) < 0.001) {
            value = target;
        }
        return value;
    }
}
