package com.banco.ticketero.domain.model.ticket;

/**
 * Enum que representa los estados posibles de un ticket en el sistema.
 * Sigue el flujo de negocio: PENDING → CALLED → IN_PROGRESS → COMPLETED/CANCELLED/NO_SHOW
 */
public enum TicketStatus {
    
    /**
     * Ticket creado y en espera de ser llamado.
     */
    PENDING,
    
    /**
     * Ticket llamado por el ejecutivo, cliente debe acercarse.
     */
    CALLED,
    
    /**
     * Ticket en proceso de atención.
     */
    IN_PROGRESS,
    
    /**
     * Ticket completado exitosamente.
     */
    COMPLETED,
    
    /**
     * Ticket cancelado por el cliente o sistema.
     */
    CANCELLED,
    
    /**
     * Cliente no se presentó después de ser llamado.
     */
    NO_SHOW;
    
    /**
     * Verifica si el ticket puede cambiar al estado especificado.
     */
    public boolean canTransitionTo(TicketStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CALLED || newStatus == CANCELLED;
            case CALLED -> newStatus == IN_PROGRESS || newStatus == NO_SHOW || newStatus == CANCELLED;
            case IN_PROGRESS -> newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED, CANCELLED, NO_SHOW -> false; // Estados finales
        };
    }
    
    /**
     * Verifica si el ticket está en un estado final.
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
    }
    
    /**
     * Verifica si el ticket está activo (no en estado final).
     */
    public boolean isActive() {
        return !isFinalState();
    }
}