package com.banco.ticketero.application.port.out;

import com.banco.ticketero.domain.model.notification.Notification;

/**
 * Puerto de salida para el envío de notificaciones.
 * Abstrae el mecanismo específico de envío.
 */
public interface NotificationPort {
    
    /**
     * Envía una notificación inmediatamente.
     */
    NotificationResult sendNotification(Notification notification);
    
    /**
     * Programa una notificación para envío posterior.
     */
    void scheduleNotification(Notification notification);
    
    /**
     * Cancela una notificación programada.
     */
    void cancelScheduledNotification(String notificationId);
    
    /**
     * Verifica el estado de una notificación enviada.
     */
    NotificationStatus checkNotificationStatus(String externalId);
    
    /**
     * Record para el resultado del envío de notificación.
     */
    record NotificationResult(
        boolean success,
        String externalId,
        String errorMessage
    ) {}
    
    /**
     * Enum para el estado de notificación externa.
     */
    enum NotificationStatus {
        DELIVERED, FAILED, PENDING, UNKNOWN
    }
}