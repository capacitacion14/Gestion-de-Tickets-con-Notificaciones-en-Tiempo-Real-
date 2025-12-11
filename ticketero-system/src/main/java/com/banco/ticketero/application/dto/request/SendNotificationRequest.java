package com.banco.ticketero.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para enviar una notificación.
 */
public record SendNotificationRequest(
    
    @NotBlank(message = "Ticket ID is required")
    String ticketId,
    
    @NotBlank(message = "Customer ID is required")
    String customerId,
    
    @NotNull(message = "Notification type is required")
    String notificationType,
    
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    String message,
    
    LocalDateTime scheduledAt
    
) {
    
    /**
     * Constructor compacto para normalización.
     */
    public SendNotificationRequest {
        if (ticketId != null) {
            ticketId = ticketId.trim();
        }
        if (customerId != null) {
            customerId = customerId.trim();
        }
        if (notificationType != null) {
            notificationType = notificationType.trim().toUpperCase();
        }
        if (message != null) {
            message = message.trim();
        }
        if (scheduledAt == null) {
            scheduledAt = LocalDateTime.now();
        }
    }
}