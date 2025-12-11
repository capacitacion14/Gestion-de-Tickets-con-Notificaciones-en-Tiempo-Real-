package com.banco.ticketero.domain.repository;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.notification.Notification;
import com.banco.ticketero.domain.model.notification.NotificationId;
import com.banco.ticketero.domain.model.notification.NotificationStatus;
import com.banco.ticketero.domain.model.notification.NotificationType;
import com.banco.ticketero.domain.model.ticket.TicketId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de notificaciones.
 * Define las operaciones de persistencia sin implementación específica.
 */
public interface NotificationRepository {
    
    /**
     * Guarda una notificación en el repositorio.
     */
    Notification save(Notification notification);
    
    /**
     * Busca una notificación por su ID.
     */
    Optional<Notification> findById(NotificationId notificationId);
    
    /**
     * Busca notificaciones por ticket.
     */
    List<Notification> findByTicketId(TicketId ticketId);
    
    /**
     * Busca notificaciones por cliente.
     */
    List<Notification> findByCustomerId(CustomerId customerId);
    
    /**
     * Busca notificaciones por estado.
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * Busca notificaciones por tipo.
     */
    List<Notification> findByType(NotificationType type);
    
    /**
     * Obtiene notificaciones pendientes listas para enviar.
     */
    List<Notification> findPendingNotificationsReadyToSend();
    
    /**
     * Obtiene notificaciones fallidas que pueden ser reintentadas.
     */
    List<Notification> findFailedNotificationsForRetry();
    
    /**
     * Busca notificaciones programadas antes de una fecha específica.
     */
    List<Notification> findByScheduledAtBefore(LocalDateTime dateTime);
    
    /**
     * Busca notificaciones urgentes pendientes.
     */
    List<Notification> findUrgentPendingNotifications();
    
    /**
     * Obtiene notificaciones por cliente y tipo.
     */
    List<Notification> findByCustomerIdAndType(CustomerId customerId, NotificationType type);
    
    /**
     * Cuenta notificaciones por estado.
     */
    long countByStatus(NotificationStatus status);
    
    /**
     * Cuenta notificaciones fallidas por cliente.
     */
    long countFailedNotificationsByCustomerId(CustomerId customerId);
    
    /**
     * Obtiene notificaciones expiradas (más de 24 horas sin enviar).
     */
    List<Notification> findExpiredNotifications();
    
    /**
     * Busca notificaciones creadas en un rango de fechas.
     */
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Elimina una notificación por su ID.
     */
    void deleteById(NotificationId notificationId);
    
    /**
     * Elimina notificaciones expiradas.
     */
    void deleteExpiredNotifications();
}