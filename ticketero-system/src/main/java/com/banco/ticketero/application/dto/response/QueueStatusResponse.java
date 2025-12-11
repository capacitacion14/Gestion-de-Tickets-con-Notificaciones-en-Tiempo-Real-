package com.banco.ticketero.application.dto.response;

import java.util.List;

/**
 * DTO de respuesta para el estado de las colas.
 */
public record QueueStatusResponse(
    String queueType,
    int currentTickets,
    int maxCapacity,
    int estimatedTimeMinutes,
    boolean isActive,
    List<TicketSummary> pendingTickets
) {
    
    /**
     * Resumen de ticket para la cola.
     */
    public record TicketSummary(
        String ticketCode,
        int positionInQueue,
        int estimatedWaitTime
    ) {}
}