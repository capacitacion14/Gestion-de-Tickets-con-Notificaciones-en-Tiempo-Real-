package com.banco.ticketero.application.usecase.notification;

import com.banco.ticketero.application.dto.request.SendNotificationRequest;
import com.banco.ticketero.application.dto.response.NotificationResponse;
import com.banco.ticketero.application.port.out.NotificationPort;
import com.banco.ticketero.domain.exception.CustomerNotFoundException;
import com.banco.ticketero.domain.exception.TicketNotFoundException;
import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.notification.Notification;
import com.banco.ticketero.domain.model.notification.NotificationType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.repository.CustomerRepository;
import com.banco.ticketero.domain.repository.NotificationRepository;
import com.banco.ticketero.domain.repository.TicketRepository;
import com.banco.ticketero.domain.service.NotificationDomainService;

import java.time.LocalDateTime;

/**
 * Caso de uso para enviar notificaciones.
 */
public class SendNotificationUseCase {
    
    private final NotificationRepository notificationRepository;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final NotificationPort notificationPort;
    private final NotificationDomainService notificationDomainService;
    
    public SendNotificationUseCase(NotificationRepository notificationRepository,
                                  TicketRepository ticketRepository,
                                  CustomerRepository customerRepository,
                                  NotificationPort notificationPort,
                                  NotificationDomainService notificationDomainService) {
        this.notificationRepository = notificationRepository;
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.notificationPort = notificationPort;
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * Envía una notificación basada en el request.
     */
    public NotificationResponse execute(SendNotificationRequest request) {
        // 1. Validar entidades relacionadas
        TicketId ticketId = TicketId.of(request.ticketId());
        CustomerId customerId = CustomerId.of(request.customerId());
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        // 2. Parsear tipo de notificación
        NotificationType notificationType = parseNotificationType(request.notificationType());
        
        // 3. Determinar tiempo de programación
        LocalDateTime scheduledAt = request.scheduledAt() != null ? 
                request.scheduledAt() : 
                notificationDomainService.calculateScheduledTime(notificationType, customer, ticket);
        
        // 4. Crear notificación
        Notification notification = Notification.create(
                ticketId, customerId, notificationType, request.message(), scheduledAt);
        
        // 5. Guardar notificación
        Notification savedNotification = notificationRepository.save(notification);
        
        // 6. Enviar si es inmediata
        if (notificationDomainService.shouldSendImmediately(notificationType, customer)) {
            NotificationPort.NotificationResult result = notificationPort.sendNotification(savedNotification);
            if (result.success()) {
                savedNotification = savedNotification.markAsSent(Long.parseLong(result.externalId()));
                savedNotification = notificationRepository.save(savedNotification);
            } else {
                savedNotification = savedNotification.markAsFailed(result.errorMessage());
                savedNotification = notificationRepository.save(savedNotification);
            }
        } else {
            // Programar para envío posterior
            notificationPort.scheduleNotification(savedNotification);
        }
        
        return toResponse(savedNotification);
    }
    
    private NotificationType parseNotificationType(String typeStr) {
        try {
            return NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification type: " + typeStr);
        }
    }
    
    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId().toString(),
                notification.getTicketId().toString(),
                notification.getCustomerId().toString(),
                notification.getType().name(),
                notification.getStatus().name(),
                notification.getMessage(),
                notification.getScheduledAt(),
                notification.getSentAt(),
                notification.getFailedAt(),
                notification.getErrorMessage(),
                notification.getRetryCount(),
                notification.getCreatedAt()
        );
    }
}