package com.banco.ticketero.domain.model.notification;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.ticket.TicketId;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregate Root que representa una notificación del sistema.
 * Contiene la lógica de negocio relacionada con el envío de notificaciones.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Notification {
    
    private final NotificationId id;
    private final TicketId ticketId;
    private final CustomerId customerId;
    private final NotificationType type;
    private final NotificationStatus status;
    private final String message;
    private final Long telegramMessageId;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime sentAt;
    private final LocalDateTime failedAt;
    private final String errorMessage;
    private final int retryCount;
    private final LocalDateTime createdAt;
    
    /**
     * Crea una nueva notificación programada.
     */
    public static Notification create(TicketId ticketId, CustomerId customerId, 
                                    NotificationType type, String message, 
                                    LocalDateTime scheduledAt) {
        validateRequiredFields(ticketId, customerId, type, message, scheduledAt);
        
        return Notification.builder()
                .id(NotificationId.generate())
                .ticketId(ticketId)
                .customerId(customerId)
                .type(type)
                .status(NotificationStatus.PENDING)
                .message(message.trim())
                .scheduledAt(scheduledAt)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Crea una notificación inmediata.
     */
    public static Notification createImmediate(TicketId ticketId, CustomerId customerId, 
                                             NotificationType type, String message) {
        return create(ticketId, customerId, type, message, LocalDateTime.now());
    }
    
    /**
     * Crea una notificación con mensaje por defecto del tipo.
     */
    public static Notification createWithDefaultMessage(TicketId ticketId, CustomerId customerId, 
                                                       NotificationType type, LocalDateTime scheduledAt) {
        return create(ticketId, customerId, type, type.getDefaultMessage(), scheduledAt);
    }
    
    /**
     * Marca la notificación como enviada exitosamente.
     */
    public Notification markAsSent(Long telegramMessageId) {
        if (status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Can only mark pending notifications as sent");
        }
        
        return Notification.builder()
                .id(this.id)
                .ticketId(this.ticketId)
                .customerId(this.customerId)
                .type(this.type)
                .status(NotificationStatus.SENT)
                .message(this.message)
                .telegramMessageId(telegramMessageId)
                .scheduledAt(this.scheduledAt)
                .sentAt(LocalDateTime.now())
                .failedAt(this.failedAt)
                .errorMessage(this.errorMessage)
                .retryCount(this.retryCount)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Marca la notificación como fallida con mensaje de error.
     */
    public Notification markAsFailed(String errorMessage) {
        if (status == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot mark sent notification as failed");
        }
        
        return Notification.builder()
                .id(this.id)
                .ticketId(this.ticketId)
                .customerId(this.customerId)
                .type(this.type)
                .status(NotificationStatus.FAILED)
                .message(this.message)
                .telegramMessageId(this.telegramMessageId)
                .scheduledAt(this.scheduledAt)
                .sentAt(this.sentAt)
                .failedAt(LocalDateTime.now())
                .errorMessage(errorMessage != null ? errorMessage.trim() : null)
                .retryCount(this.retryCount)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Incrementa el contador de reintentos.
     */
    public Notification incrementRetryCount() {
        if (retryCount >= 5) {
            throw new IllegalStateException("Maximum retry count exceeded");
        }
        if (status == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot retry sent notification");
        }
        
        return Notification.builder()
                .id(this.id)
                .ticketId(this.ticketId)
                .customerId(this.customerId)
                .type(this.type)
                .status(NotificationStatus.PENDING) // Reset to pending for retry
                .message(this.message)
                .telegramMessageId(this.telegramMessageId)
                .scheduledAt(this.scheduledAt)
                .sentAt(this.sentAt)
                .failedAt(this.failedAt)
                .errorMessage(this.errorMessage)
                .retryCount(this.retryCount + 1)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Cancela la notificación.
     */
    public Notification cancel() {
        if (status == NotificationStatus.SENT) {
            throw new IllegalStateException("Cannot cancel sent notification");
        }
        
        return Notification.builder()
                .id(this.id)
                .ticketId(this.ticketId)
                .customerId(this.customerId)
                .type(this.type)
                .status(NotificationStatus.CANCELLED)
                .message(this.message)
                .telegramMessageId(this.telegramMessageId)
                .scheduledAt(this.scheduledAt)
                .sentAt(this.sentAt)
                .failedAt(this.failedAt)
                .errorMessage(this.errorMessage)
                .retryCount(this.retryCount)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Verifica si la notificación está lista para ser enviada.
     */
    public boolean isReadyToSend() {
        return status == NotificationStatus.PENDING && 
               scheduledAt.isBefore(LocalDateTime.now().plusMinutes(1)); // 1 minute tolerance
    }
    
    /**
     * Verifica si la notificación puede ser reintentada.
     */
    public boolean canRetry() {
        return status == NotificationStatus.FAILED && retryCount < 5;
    }
    
    /**
     * Verifica si la notificación es urgente.
     */
    public boolean isUrgent() {
        return type.isUrgent();
    }
    
    /**
     * Verifica si la notificación requiere confirmación de lectura.
     */
    public boolean requiresReadConfirmation() {
        return type.requiresReadConfirmation();
    }
    
    /**
     * Calcula el próximo intento de envío basado en el número de reintentos.
     */
    public LocalDateTime getNextRetryTime() {
        if (!canRetry()) {
            return null;
        }
        
        // Exponential backoff: 1, 2, 4, 8, 16 minutes
        int delayMinutes = (int) Math.pow(2, retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
    
    /**
     * Verifica si la notificación ha expirado (más de 24 horas sin enviar).
     */
    public boolean isExpired() {
        if (status == NotificationStatus.SENT) {
            return false;
        }
        
        return scheduledAt.isBefore(LocalDateTime.now().minusHours(24));
    }
    
    private static void validateRequiredFields(TicketId ticketId, CustomerId customerId, 
                                             NotificationType type, String message, 
                                             LocalDateTime scheduledAt) {
        if (ticketId == null) {
            throw new IllegalArgumentException("Ticket ID is required");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        if (message.trim().length() > 1000) {
            throw new IllegalArgumentException("Message cannot exceed 1000 characters");
        }
    }
}