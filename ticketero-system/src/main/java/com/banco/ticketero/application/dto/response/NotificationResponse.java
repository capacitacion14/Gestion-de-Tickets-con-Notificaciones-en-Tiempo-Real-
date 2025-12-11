package com.banco.ticketero.application.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de notificaciones.
 */
public record NotificationResponse(
    String id,
    String ticketId,
    String customerId,
    String type,
    String status,
    String message,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    LocalDateTime failedAt,
    String errorMessage,
    int retryCount,
    LocalDateTime createdAt
) {}