package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.model.notification.Notification;
import com.banco.ticketero.domain.model.notification.NotificationStatus;
import com.banco.ticketero.domain.model.notification.NotificationType;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationDomainService Tests")
class NotificationDomainServiceTest {

    private NotificationDomainService notificationDomainService;

    @BeforeEach
    void setUp() {
        notificationDomainService = new NotificationDomainService();
    }

    @Test
    @DisplayName("Should send urgent notifications immediately")
    void shouldSendUrgentNotificationsImmediately() {
        // Given
        Customer regularCustomer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        
        // When & Then
        assertTrue(notificationDomainService.shouldSendImmediately(NotificationType.TICKET_CALLED, regularCustomer));
        assertTrue(notificationDomainService.shouldSendImmediately(NotificationType.TICKET_CREATED, regularCustomer));
        assertFalse(notificationDomainService.shouldSendImmediately(NotificationType.REMINDER, regularCustomer));
    }

    @Test
    @DisplayName("Should send all notifications immediately for VIP customers")
    void shouldSendAllNotificationsImmediatelyForVipCustomers() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        
        // When & Then
        assertTrue(notificationDomainService.shouldSendImmediately(NotificationType.REMINDER, vipCustomer));
        assertTrue(notificationDomainService.shouldSendImmediately(NotificationType.QUEUE_UPDATE, vipCustomer));
    }

    @Test
    @DisplayName("Should calculate scheduled time for immediate notifications")
    void shouldCalculateScheduledTimeForImmediateNotifications() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        Ticket ticket = Ticket.create(vipCustomer.getId(), QueueType.VIP, TicketCode.fromSequence(1001));
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);
        
        // When
        LocalDateTime scheduledTime = notificationDomainService.calculateScheduledTime(
            NotificationType.TICKET_CREATED, vipCustomer, ticket);
        
        // Then
        assertTrue(scheduledTime.isAfter(before));
        assertTrue(scheduledTime.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    @DisplayName("Should calculate scheduled time for queue updates")
    void shouldCalculateScheduledTimeForQueueUpdates() {
        // Given
        Customer regularCustomer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Ticket ticket = Ticket.create(regularCustomer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1001));
        LocalDateTime before = LocalDateTime.now().plusMinutes(4);
        
        // When
        LocalDateTime scheduledTime = notificationDomainService.calculateScheduledTime(
            NotificationType.QUEUE_UPDATE, regularCustomer, ticket);
        
        // Then
        assertTrue(scheduledTime.isAfter(before));
        assertTrue(scheduledTime.isBefore(LocalDateTime.now().plusMinutes(6)));
    }

    @Test
    @DisplayName("Should generate personalized message for ticket creation")
    void shouldGeneratePersonalizedMessageForTicketCreation() {
        // Given
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Ticket ticket = Ticket.create(customer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(3, 60);
        
        // When
        String message = notificationDomainService.generatePersonalizedMessage(
            NotificationType.TICKET_CREATED, customer, ticket);
        
        // Then
        assertTrue(message.contains("John"));
        assertTrue(message.contains("T1001"));
        assertTrue(message.contains("3"));
        assertTrue(message.contains("60"));
    }

    @Test
    @DisplayName("Should generate personalized message for ticket called")
    void shouldGeneratePersonalizedMessageForTicketCalled() {
        // Given
        Customer customer = Customer.create(NationalId.of("12345678"), "Jane", "Smith");
        Ticket ticket = Ticket.create(customer.getId(), QueueType.VIP, TicketCode.fromSequence(2001));
        
        // When
        String message = notificationDomainService.generatePersonalizedMessage(
            NotificationType.TICKET_CALLED, customer, ticket);
        
        // Then
        assertTrue(message.contains("Jane"));
        assertTrue(message.contains("T2001"));
        assertTrue(message.contains("llamado"));
    }

    @Test
    @DisplayName("Should use default message for null inputs")
    void shouldUseDefaultMessageForNullInputs() {
        // When
        String message = notificationDomainService.generatePersonalizedMessage(
            NotificationType.TICKET_CREATED, null, null);
        
        // Then
        assertEquals(NotificationType.TICKET_CREATED.getDefaultMessage(), message);
    }

    @Test
    @DisplayName("Should retry failed notifications within limits")
    void shouldRetryFailedNotificationsWithinLimits() {
        // Given
        Notification failedNotification = Notification.createImmediate(
            Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                         QueueType.GENERAL, TicketCode.fromSequence(1001)).getId(),
            Customer.create(NationalId.of("12345678"), "John", "Doe").getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        ).markAsFailed("Network error");
        
        // When
        boolean shouldRetry = notificationDomainService.shouldRetryFailedNotification(failedNotification);
        
        // Then
        assertTrue(shouldRetry);
    }

    @Test
    @DisplayName("Should not retry sent notifications")
    void shouldNotRetrySentNotifications() {
        // Given
        Notification sentNotification = Notification.createImmediate(
            Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                         QueueType.GENERAL, TicketCode.fromSequence(1001)).getId(),
            Customer.create(NationalId.of("12345678"), "John", "Doe").getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        ).markAsSent(123456789L);
        
        // When
        boolean shouldRetry = notificationDomainService.shouldRetryFailedNotification(sentNotification);
        
        // Then
        assertFalse(shouldRetry);
    }

    @Test
    @DisplayName("Should calculate notification priority for VIP customers")
    void shouldCalculateNotificationPriorityForVipCustomers() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        Customer regularCustomer = Customer.create(NationalId.of("87654321"), "Jane", "Smith");
        
        Notification vipNotification = Notification.createImmediate(
            Ticket.create(vipCustomer.getId(), QueueType.VIP, TicketCode.fromSequence(1001)).getId(),
            vipCustomer.getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        );
        
        Notification regularNotification = Notification.createImmediate(
            Ticket.create(regularCustomer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1002)).getId(),
            regularCustomer.getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        );
        
        // When
        int vipPriority = notificationDomainService.calculateNotificationPriority(vipNotification, vipCustomer);
        int regularPriority = notificationDomainService.calculateNotificationPriority(regularNotification, regularCustomer);
        
        // Then
        assertTrue(vipPriority < regularPriority); // Lower number = higher priority
    }

    @Test
    @DisplayName("Should verify customer notification capability")
    void shouldVerifyCustomerNotificationCapability() {
        // Given
        Customer customerWithTelegram = Customer.create(NationalId.of("12345678"), "John", "Doe")
                .withTelegramChatId(123456789L);
        Customer customerWithEmail = Customer.create(NationalId.of("87654321"), "Jane", "Smith")
                .updateContactInfo("jane@email.com", null);
        Customer customerWithoutContact = Customer.create(NationalId.of("11111111"), "Bob", "Wilson");
        
        // When & Then
        assertTrue(notificationDomainService.canCustomerReceiveNotifications(customerWithTelegram));
        assertTrue(notificationDomainService.canCustomerReceiveNotifications(customerWithEmail));
        assertFalse(notificationDomainService.canCustomerReceiveNotifications(customerWithoutContact));
        assertFalse(notificationDomainService.canCustomerReceiveNotifications(null));
    }

    @Test
    @DisplayName("Should determine preferred notification channel")
    void shouldDeterminePreferredNotificationChannel() {
        // Given
        Customer telegramCustomer = Customer.create(NationalId.of("12345678"), "John", "Doe")
                .withTelegramChatId(123456789L);
        Customer emailCustomer = Customer.create(NationalId.of("87654321"), "Jane", "Smith")
                .updateContactInfo("jane@email.com", null);
        Customer noContactCustomer = Customer.create(NationalId.of("11111111"), "Bob", "Wilson");
        
        // When & Then
        assertEquals(NotificationDomainService.NotificationChannel.TELEGRAM, 
                    notificationDomainService.determinePreferredChannel(telegramCustomer));
        assertEquals(NotificationDomainService.NotificationChannel.EMAIL, 
                    notificationDomainService.determinePreferredChannel(emailCustomer));
        assertEquals(NotificationDomainService.NotificationChannel.NONE, 
                    notificationDomainService.determinePreferredChannel(noContactCustomer));
        assertEquals(NotificationDomainService.NotificationChannel.NONE, 
                    notificationDomainService.determinePreferredChannel(null));
    }

    @Test
    @DisplayName("Should calculate notification metrics correctly")
    void shouldCalculateNotificationMetricsCorrectly() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(2);
        LocalDateTime endDate = LocalDateTime.now();
        
        Notification sentNotification = Notification.createImmediate(
            Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                         QueueType.GENERAL, TicketCode.fromSequence(1001)).getId(),
            Customer.create(NationalId.of("12345678"), "John", "Doe").getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        ).markAsSent(123456789L);
        
        Notification failedNotification = Notification.createImmediate(
            Ticket.create(Customer.create(NationalId.of("87654321"), "Jane", "Smith").getId(), 
                         QueueType.GENERAL, TicketCode.fromSequence(1002)).getId(),
            Customer.create(NationalId.of("87654321"), "Jane", "Smith").getId(),
            NotificationType.TICKET_CREATED,
            "Test message"
        ).markAsFailed("Error");
        
        List<Notification> notifications = List.of(sentNotification, failedNotification);
        
        // When
        NotificationDomainService.NotificationMetrics metrics = 
            notificationDomainService.calculateNotificationMetrics(notifications, startDate, endDate);
        
        // Then
        assertEquals(2, metrics.totalNotifications());
        assertEquals(1, metrics.sentNotifications());
        assertEquals(1, metrics.failedNotifications());
        assertEquals(0, metrics.pendingNotifications());
        assertEquals(50.0, metrics.successRatePercentage(), 0.01);
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // When & Then
        assertFalse(notificationDomainService.shouldSendImmediately(null, null));
        assertFalse(notificationDomainService.shouldRetryFailedNotification(null));
        assertEquals(Integer.MAX_VALUE, notificationDomainService.calculateNotificationPriority(null, null));
        
        // Metrics with empty list
        NotificationDomainService.NotificationMetrics emptyMetrics = 
            notificationDomainService.calculateNotificationMetrics(
                List.of(), LocalDateTime.now().minusHours(1), LocalDateTime.now());
        assertEquals(0, emptyMetrics.totalNotifications());
    }

    @Test
    @DisplayName("Should calculate reminder time based on ticket wait time")
    void shouldCalculateReminderTimeBasedOnTicketWaitTime() {
        // Given
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Ticket ticketWithWaitTime = Ticket.create(customer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(2, 30); // 30 minutes wait time
        
        // When
        LocalDateTime scheduledTime = notificationDomainService.calculateScheduledTime(
            NotificationType.REMINDER, customer, ticketWithWaitTime);
        
        // Then
        // Should be scheduled for approximately 25 minutes from now (30 - 5 minutes before)
        assertTrue(scheduledTime.isAfter(LocalDateTime.now().plusMinutes(20)));
        assertTrue(scheduledTime.isBefore(LocalDateTime.now().plusMinutes(30)));
    }

    @Test
    @DisplayName("Should prefer Telegram over email for customers with both")
    void shouldPreferTelegramOverEmailForCustomersWithBoth() {
        // Given
        Customer customerWithBoth = Customer.create(NationalId.of("12345678"), "John", "Doe")
                .updateContactInfo("john@email.com", "123456789")
                .withTelegramChatId(123456789L);
        
        // When
        NotificationDomainService.NotificationChannel channel = 
            notificationDomainService.determinePreferredChannel(customerWithBoth);
        
        // Then
        assertEquals(NotificationDomainService.NotificationChannel.TELEGRAM, channel);
    }
}