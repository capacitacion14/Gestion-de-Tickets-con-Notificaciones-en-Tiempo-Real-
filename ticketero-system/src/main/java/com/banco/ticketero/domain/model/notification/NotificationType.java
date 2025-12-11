package com.banco.ticketero.domain.model.notification;

/**
 * Enum que representa los tipos de notificación disponibles en el sistema.
 */
public enum NotificationType {
    
    /**
     * Notificación enviada cuando se crea un nuevo ticket.
     */
    TICKET_CREATED("Su ticket ha sido creado", true),
    
    /**
     * Notificación enviada cuando el ticket es llamado para atención.
     */
    TICKET_CALLED("Su ticket está siendo llamado", true),
    
    /**
     * Notificación de actualización del estado de la cola.
     */
    QUEUE_UPDATE("Actualización de cola", false),
    
    /**
     * Recordatorio enviado al cliente.
     */
    REMINDER("Recordatorio de su ticket", false);
    
    private final String defaultMessage;
    private final boolean isUrgent;
    
    NotificationType(String defaultMessage, boolean isUrgent) {
        this.defaultMessage = defaultMessage;
        this.isUrgent = isUrgent;
    }
    
    /**
     * Retorna el mensaje por defecto para este tipo de notificación.
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    /**
     * Verifica si este tipo de notificación es urgente.
     */
    public boolean isUrgent() {
        return isUrgent;
    }
    
    /**
     * Verifica si este tipo de notificación requiere confirmación de lectura.
     */
    public boolean requiresReadConfirmation() {
        return isUrgent;
    }
    
    /**
     * Retorna la prioridad de la notificación (1 = más alta, 3 = más baja).
     */
    public int getPriority() {
        return switch (this) {
            case TICKET_CALLED -> 1;  // Máxima prioridad
            case TICKET_CREATED -> 2; // Alta prioridad
            case QUEUE_UPDATE, REMINDER -> 3; // Prioridad normal
        };
    }
    
    /**
     * Verifica si esta notificación tiene mayor prioridad que otra.
     */
    public boolean hasHigherPriorityThan(NotificationType other) {
        return this.getPriority() < other.getPriority();
    }
}