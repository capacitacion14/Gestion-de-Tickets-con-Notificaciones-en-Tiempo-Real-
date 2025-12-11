package com.banco.ticketero.domain.model.notification;

/**
 * Enum que representa los estados posibles de una notificación.
 */
public enum NotificationStatus {
    
    /**
     * Notificación pendiente de envío.
     */
    PENDING,
    
    /**
     * Notificación enviada exitosamente.
     */
    SENT,
    
    /**
     * Notificación falló al enviarse.
     */
    FAILED,
    
    /**
     * Notificación cancelada.
     */
    CANCELLED;
    
    /**
     * Verifica si la notificación está en estado final.
     */
    public boolean isFinalState() {
        return this == SENT || this == CANCELLED;
    }
    
    /**
     * Verifica si la notificación puede ser procesada.
     */
    public boolean canBeProcessed() {
        return this == PENDING || this == FAILED;
    }
}