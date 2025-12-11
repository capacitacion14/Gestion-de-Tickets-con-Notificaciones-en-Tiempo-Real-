package com.banco.ticketero.application.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de tickets.
 */
public record TicketResponse(
    String id,
    String ticketCode,
    String customerId,
    String queueType,
    String status,
    Integer positionInQueue,
    Integer estimatedWaitTime,
    LocalDateTime calledAt,
    LocalDateTime completedAt,
    LocalDateTime cancelledAt,
    LocalDateTime createdAt
) {}