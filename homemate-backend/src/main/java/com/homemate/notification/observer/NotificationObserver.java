package com.homemate.notification.observer;

import com.homemate.notification.observer.imp.NotificationSubjectImp;

public interface NotificationObserver {
    void update(NotificationSubjectImp notificationSubjectImp);

}
