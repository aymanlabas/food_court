package com.foodcourt.notification.exceptions;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("Notification introuvable : " + id);
    }
}
