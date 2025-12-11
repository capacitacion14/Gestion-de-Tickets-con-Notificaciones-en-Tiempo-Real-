package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.ticket.TicketStatus;

/**
 * Excepción lanzada cuando se intenta una transición de estado inválida en un ticket.
 */
public class InvalidTicketStatusException extends DomainException {
    
    public InvalidTicketStatusException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, targetStatus));
    }
    
    public InvalidTicketStatusException(TicketStatus currentStatus, String operation) {
        super(String.format("Cannot perform operation '%s' on ticket with status %s", operation, currentStatus));
    }
    
    public InvalidTicketStatusException(String message) {
        super(message);
    }
}