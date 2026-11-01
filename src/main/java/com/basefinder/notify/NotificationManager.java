package com.basefinder.notify;

import com.basefinder.config.GlobalSettings;
import com.basefinder.model.BaseCandidate;
import com.basefinder.rules.NotifyBehavior;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Decides when a base candidate should raise a HUD notification, honouring the
 * global policy (only-new, notify-once, cooldown) and per-candidate rule
 * overrides (SILENT suppresses; ALWAYS bypasses the once/cooldown limits). Keeps
 * a bounded in-memory toast history for the bundled HUD; nothing is persisted.
 */
public final class NotificationManager {

    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, Long> lastNotifyTick = new HashMap<>();
    private final Deque<Notification> recent = new ArrayDeque<>();
    private static final int RECENT_LIMIT = 40;

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Evaluate candidates for notifications. A candidate must meet the global
     * confidence floor and be visible (not force-hidden) to notify.
     */
    public synchronized void process(List<BaseCandidate> candidates, Map<String, NotifyBehavior> overrides,
                                     GlobalSettings global, long tick) {
        int floor = global.confidenceFloor();
        for (BaseCandidate c : candidates) {
            if (c.forcedHidden() || !c.visible() || c.fusedConfidence() < floor) {
                continue;
            }
            NotifyBehavior behavior = overrides.getOrDefault(c.id(), NotifyBehavior.DEFAULT);
            if (behavior == NotifyBehavior.SILENT) {
                continue;
            }
            boolean always = behavior == NotifyBehavior.ALWAYS;
            boolean firstTime = !lastNotifyTick.containsKey(c.id());

            if (!always) {
                if (global.notifyOnlyNewCandidates() && !firstTime) {
                    continue;
                }
                if (global.notifyOncePerCandidate() && !firstTime) {
                    continue;
                }
                Long last = lastNotifyTick.get(c.id());
                if (last != null && tick - last < global.notifyCooldownTicks()) {
                    continue;
                }
            }
            lastNotifyTick.put(c.id(), tick);
            raise(new Notification(c.id(), "Base candidate " + c.fusedConfidence() + "%",
                    describe(c), c.severity(), tick));
        }
        // Drop bookkeeping for candidates that no longer exist to avoid unbounded growth.
        if (lastNotifyTick.size() > 512) {
            lastNotifyTick.clear();
        }
    }

    private String describe(BaseCandidate c) {
        return c.displayLabel() + " at " + c.center() + " (" + c.findings().size() + " signals)";
    }

    private void raise(Notification n) {
        recent.addLast(n);
        while (recent.size() > RECENT_LIMIT) {
            recent.removeFirst();
        }
        for (NotificationListener l : listeners) {
            try {
                l.onNotification(n);
            } catch (Throwable ignored) {
                // a listener must not break notification dispatch
            }
        }
    }

    public synchronized List<Notification> recent() {
        return new ArrayList<>(recent);
    }

    public synchronized void reset() {
        lastNotifyTick.clear();
        recent.clear();
    }
}
