package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.notification.Notification;
import com.banco.ticketero.domain.model.notification.NotificationStatus;
import com.banco.ticketero.domain.model.notification.NotificationType;
import com.banco.ticketero.domain.model.ticket.Ticket;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de dominio para la lógica de negocio relacionada con notificaciones.
 * Contiene reglas de negocio para el envío y gestión de notificaciones.
 */
public class NotificationDomainService {
    
    /**
     * Determina si una notificación debe ser enviada inmediatamente.
     */
    public boolean shouldSendImmediately(NotificationType type, Customer customer) {
        if (type == null || customer == null) {
            return false;
        }
        
        // Notificaciones urgentes se envían inmediatamente
        if (type.isUrgent()) {
            return true;
        }
        
        // Clientes VIP reciben todas las notificaciones inmediatamente
        return customer.isVip();
    }
    
    /**
     * Calcula el tiempo de programación para una notificación.
     */
    public LocalDateTime calculateScheduledTime(NotificationType type, Customer customer, Ticket ticket) {
        if (shouldSendImmediately(type, customer)) {
            return LocalDateTime.now();
        }
        
        // Para notificaciones no urgentes, programar según el tipo
        return switch (type) {
            case QUEUE_UPDATE -> LocalDateTime.now().plusMinutes(5);
            case REMINDER -> calculateReminderTime(ticket);
            default -> LocalDateTime.now().plusMinutes(1);
        };
    }
    
    /**
     * Calcula el tiempo para enviar un recordatorio.
     */
    private LocalDateTime calculateReminderTime(Ticket ticket) {
        if (ticket == null || ticket.getEstimatedWaitTime() == null) {
            return LocalDateTime.now().plusMinutes(15);
        }
        
        // Enviar recordatorio 5 minutos antes del tiempo estimado
        int reminderMinutes = Math.max(5, ticket.getEstimatedWaitTime() - 5);
        return LocalDateTime.now().plusMinutes(reminderMinutes);
    }
    
    /**
     * Genera el mensaje personalizado para una notificación.
     */
    public String generatePersonalizedMessage(NotificationType type, Customer customer, Ticket ticket) {
        if (type == null || customer == null || ticket == null) {
            return type != null ? type.getDefaultMessage() : "Notificación del sistema";
        }
        
        String customerName = customer.getFirstName();
        String ticketCode = ticket.getTicketCode().getValue();
        
        return switch (type) {
            case TICKET_CREATED -> String.format(
                "Hola %s, tu ticket %s ha sido creado. Posición en cola: %d. Tiempo estimado: %d minutos.",
                customerName, ticketCode, 
                ticket.getPositionInQueue() != null ? ticket.getPositionInQueue() : 0,
                ticket.getEstimatedWaitTime() != null ? ticket.getEstimatedWaitTime() : 0
            );
            case TICKET_CALLED -> String.format(
                "¡%s, tu ticket %s está siendo llamado! Por favor acércate al mostrador.",
                customerName, ticketCode
            );
            case QUEUE_UPDATE -> String.format(
                "Hola %s, actualización de tu ticket %s. Nueva posición: %d.",
                customerName, ticketCode,
                ticket.getPositionInQueue() != null ? ticket.getPositionInQueue() : 0
            );
            case REMINDER -> String.format(
                "Recordatorio %s: Tu ticket %s será llamado pronto. Tiempo estimado: %d minutos.",
                customerName, ticketCode,
                ticket.getEstimatedWaitTime() != null ? ticket.getEstimatedWaitTime() : 5
            );
        };
    }
    
    /**
     * Determina si una notificación fallida debe ser reintentada.
     */
    public boolean shouldRetryFailedNotification(Notification notification) {
        if (notification == null || notification.getStatus() != NotificationStatus.FAILED) {
            return false;
        }
        
        // No reintentar notificaciones expiradas
        if (notification.isExpired()) {
            return false;
        }
        
        // Verificar límite de reintentos
        return notification.canRetry();
    }
    
    /**
     * Calcula la prioridad de envío de una notificación.
     */
    public int calculateNotificationPriority(Notification notification, Customer customer) {
        if (notification == null || customer == null) {
            return Integer.MAX_VALUE; // Menor prioridad
        }
        
        int basePriority = notification.getType().getPriority();
        
        // Ajustar por tipo de cliente
        if (customer.isVip()) {
            basePriority -= 10; // Mayor prioridad para VIP
        }
        
        // Ajustar por número de reintentos (más reintentos = menor prioridad)
        basePriority += notification.getRetryCount() * 2;
        
        // Ajustar por tiempo de espera
        if (notification.getScheduledAt().isBefore(LocalDateTime.now().minusMinutes(30))) {
            basePriority -= 5; // Mayor prioridad para notificaciones atrasadas
        }
        
        return Math.max(1, basePriority);
    }
    
    /**
     * Verifica si un cliente puede recibir notificaciones.
     */
    public boolean canCustomerReceiveNotifications(Customer customer) {
        if (customer == null) {
            return false;
        }
        
        return customer.canReceiveNotifications();
    }
    
    /**
     * Determina el canal de notificación preferido para un cliente.
     */
    public NotificationChannel determinePreferredChannel(Customer customer) {
        if (customer == null) {
            return NotificationChannel.NONE;
        }
        
        if (customer.hasTelegramConfigured()) {
            return NotificationChannel.TELEGRAM;
        }
        
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            return NotificationChannel.EMAIL;
        }
        
        return NotificationChannel.NONE;
    }
    
    /**
     * Calcula métricas de notificaciones para un período.
     */
    public NotificationMetrics calculateNotificationMetrics(List<Notification> notifications, 
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        if (notifications == null || notifications.isEmpty()) {
            return new NotificationMetrics(0, 0, 0, 0, 0.0);
        }
        
        List<Notification> periodNotifications = notifications.stream()
                .filter(n -> n.getCreatedAt().isAfter(startDate) && n.getCreatedAt().isBefore(endDate))
                .toList();
        
        int totalNotifications = periodNotifications.size();
        long sentNotifications = periodNotifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.SENT)
                .count();
        long failedNotifications = periodNotifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.FAILED)
                .count();
        long pendingNotifications = periodNotifications.stream()
                .filter(n -> n.getStatus() == NotificationStatus.PENDING)
                .count();
        
        double successRate = totalNotifications > 0 ? (double) sentNotifications / totalNotifications * 100 : 0.0;
        
        return new NotificationMetrics(totalNotifications, (int) sentNotifications, 
                                     (int) failedNotifications, (int) pendingNotifications, successRate);
    }
    
    /**
     * Enum para canales de notificación.
     */
    public enum NotificationChannel {
        TELEGRAM, EMAIL, SMS, NONE
    }
    
    /**
     * Record para métricas de notificaciones.
     */
    public record NotificationMetrics(
            int totalNotifications,
            int sentNotifications,
            int failedNotifications,
            int pendingNotifications,
            double successRatePercentage
    ) {}
}