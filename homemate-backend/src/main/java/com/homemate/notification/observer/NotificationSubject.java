package com.homemate.notification.observer;

public interface NotificationSubject {
    void attach(NotificationObserver notificationObserver);
    void detach(NotificationObserver notificationObserver);
    void notifyAllObservers();

}
