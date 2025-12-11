package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.notification.NotificationId;

/**
 * Excepción lanzada cuando ocurre un error en el procesamiento de notificaciones.
 */
public class NotificationException extends DomainException {
    
    public NotificationException(NotificationId notificationId, String reason) {
        super(String.format("Notification error for ID %s: %s", notificationId, reason));
    }
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}