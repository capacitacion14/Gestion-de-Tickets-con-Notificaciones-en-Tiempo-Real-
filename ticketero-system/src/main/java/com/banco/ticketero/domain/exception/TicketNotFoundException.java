package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketId;

/**
 * Excepción lanzada cuando no se encuentra un ticket solicitado.
 */
public class TicketNotFoundException extends DomainException {
    
    public TicketNotFoundException(TicketId ticketId) {
        super("Ticket not found with ID: " + ticketId);
    }
    
    public TicketNotFoundException(TicketCode ticketCode) {
        super("Ticket not found with code: " + ticketCode);
    }
    
    public TicketNotFoundException(String message) {
        super(message);
    }
}