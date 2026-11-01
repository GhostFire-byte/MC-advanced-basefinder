package com.basefinder.notify;

/** Host hook for notifications (so the client can route them to its own HUD/sound). */
@FunctionalInterface
public interface NotificationListener {
    void onNotification(Notification notification);
}
