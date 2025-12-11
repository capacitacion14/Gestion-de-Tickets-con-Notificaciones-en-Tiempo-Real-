package com.banco.ticketero.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado de un ticket.
 */
public record UpdateTicketStatusRequest(
    
    @NotNull(message = "New status is required")
    String newStatus
    
) {
    
    /**
     * Constructor compacto para normalización.
     */
    public UpdateTicketStatusRequest {
        if (newStatus != null) {
            newStatus = newStatus.trim().toUpperCase();
        }
    }
}