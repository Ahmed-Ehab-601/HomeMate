package com.homemate.notification.observer.imp;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.observer.NotificationObserver;
import com.homemate.notification.observer.NotificationSubject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationSubjectImp implements NotificationSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();
    @Setter
    @Getter
    private EmailRequest emailRequest;
    @Override
    public void attach(NotificationObserver notificationObserver) {
        observers.add(notificationObserver);
    }

    @Override
    public void detach(NotificationObserver notificationObserver) {
        observers.remove(notificationObserver);

    }


    @Override
    public void notifyAllObservers() {
        for (NotificationObserver notificationObserver : observers){
            notificationObserver.update(this);
        }

    }
}
