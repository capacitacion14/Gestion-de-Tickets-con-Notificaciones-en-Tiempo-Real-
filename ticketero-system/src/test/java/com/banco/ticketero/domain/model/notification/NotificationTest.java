package com.banco.ticketero.domain.model.notification;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.ticket.TicketId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Aggregate Root Tests")
class NotificationTest {

    private final TicketId validTicketId = TicketId.generate();
    private final CustomerId validCustomerId = CustomerId.generate();
    private final NotificationType validType = NotificationType.TICKET_CREATED;
    private final String validMessage = "Your ticket has been created";
    private final LocalDateTime validScheduledTime = LocalDateTime.now().plusMinutes(5);

    @Test
    @DisplayName("Should create notification with all required fields")
    void shouldCreateNotificationWithAllRequiredFields() {
        // When
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        
        // Then
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals(validTicketId, notification.getTicketId());
        assertEquals(validCustomerId, notification.getCustomerId());
        assertEquals(validType, notification.getType());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(validMessage, notification.getMessage());
        assertEquals(validScheduledTime, notification.getScheduledAt());
        assertEquals(0, notification.getRetryCount());
        assertNotNull(notification.getCreatedAt());
        assertNull(notification.getTelegramMessageId());
        assertNull(notification.getSentAt());
        assertNull(notification.getFailedAt());
        assertNull(notification.getErrorMessage());
    }

    @Test
    @DisplayName("Should create immediate notification")
    void shouldCreateImmediateNotification() {
        // When
        Notification notification = Notification.createImmediate(validTicketId, validCustomerId, 
                                                               validType, validMessage);
        
        // Then
        assertNotNull(notification);
        assertTrue(notification.getScheduledAt().isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    @DisplayName("Should create notification with default message")
    void shouldCreateNotificationWithDefaultMessage() {
        // When
        Notification notification = Notification.createWithDefaultMessage(validTicketId, validCustomerId, 
                                                                         validType, validScheduledTime);
        
        // Then
        assertEquals(validType.getDefaultMessage(), notification.getMessage());
    }

    @Test
    @DisplayName("Should trim message whitespace")
    void shouldTrimMessageWhitespace() {
        // Given
        String messageWithSpaces = "  " + validMessage + "  ";
        
        // When
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, messageWithSpaces, validScheduledTime);
        
        // Then
        assertEquals(validMessage, notification.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when ticket ID is null")
    void shouldThrowExceptionWhenTicketIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Notification.create(null, validCustomerId, validType, validMessage, validScheduledTime)
        );
        
        assertEquals("Ticket ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when customer ID is null")
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Notification.create(validTicketId, null, validType, validMessage, validScheduledTime)
        );
        
        assertEquals("Customer ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when notification type is null")
    void shouldThrowExceptionWhenNotificationTypeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Notification.create(validTicketId, validCustomerId, null, validMessage, validScheduledTime)
        );
        
        assertEquals("Notification type is required", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw exception when message is empty")
    void shouldThrowExceptionWhenMessageIsEmpty(String emptyMessage) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Notification.create(validTicketId, validCustomerId, validType, emptyMessage, validScheduledTime)
        );
        
        assertEquals("Message is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when message exceeds limit")
    void shouldThrowExceptionWhenMessageExceedsLimit() {
        // Given
        String longMessage = "a".repeat(1001);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Notification.create(validTicketId, validCustomerId, validType, longMessage, validScheduledTime)
        );
        
        assertEquals("Message cannot exceed 1000 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark notification as sent")
    void shouldMarkNotificationAsSent() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Long telegramMessageId = 123456789L;
        
        // When
        Notification sentNotification = notification.markAsSent(telegramMessageId);
        
        // Then
        assertEquals(NotificationStatus.SENT, sentNotification.getStatus());
        assertEquals(telegramMessageId, sentNotification.getTelegramMessageId());
        assertNotNull(sentNotification.getSentAt());
        // Verify immutability
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNull(notification.getTelegramMessageId());
    }

    @Test
    @DisplayName("Should throw exception when marking non-pending notification as sent")
    void shouldThrowExceptionWhenMarkingNonPendingNotificationAsSent() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Notification failedNotification = notification.markAsFailed("Error occurred");
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> failedNotification.markAsSent(123456789L)
        );
        
        assertEquals("Can only mark pending notifications as sent", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark notification as failed")
    void shouldMarkNotificationAsFailed() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        String errorMessage = "Network timeout";
        
        // When
        Notification failedNotification = notification.markAsFailed(errorMessage);
        
        // Then
        assertEquals(NotificationStatus.FAILED, failedNotification.getStatus());
        assertEquals(errorMessage, failedNotification.getErrorMessage());
        assertNotNull(failedNotification.getFailedAt());
        // Verify immutability
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNull(notification.getErrorMessage());
    }

    @Test
    @DisplayName("Should throw exception when marking sent notification as failed")
    void shouldThrowExceptionWhenMarkingSentNotificationAsFailed() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Notification sentNotification = notification.markAsSent(123456789L);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentNotification.markAsFailed("Error")
        );
        
        assertEquals("Cannot mark sent notification as failed", exception.getMessage());
    }

    @Test
    @DisplayName("Should increment retry count")
    void shouldIncrementRetryCount() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Notification failedNotification = notification.markAsFailed("Error");
        
        // When
        Notification retriedNotification = failedNotification.incrementRetryCount();
        
        // Then
        assertEquals(1, retriedNotification.getRetryCount());
        assertEquals(NotificationStatus.PENDING, retriedNotification.getStatus()); // Reset to pending
        // Verify immutability
        assertEquals(0, failedNotification.getRetryCount());
    }

    @Test
    @DisplayName("Should throw exception when retry count exceeds limit")
    void shouldThrowExceptionWhenRetryCountExceedsLimit() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        
        // Simulate 5 retries
        Notification currentNotification = notification;
        for (int i = 0; i < 5; i++) {
            currentNotification = currentNotification.markAsFailed("Error").incrementRetryCount();
        }
        
        final Notification maxRetriedNotification = currentNotification;
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> maxRetriedNotification.incrementRetryCount()
        );
        
        assertEquals("Maximum retry count exceeded", exception.getMessage());
    }

    @Test
    @DisplayName("Should cancel notification")
    void shouldCancelNotification() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        
        // When
        Notification cancelledNotification = notification.cancel();
        
        // Then
        assertEquals(NotificationStatus.CANCELLED, cancelledNotification.getStatus());
        // Verify immutability
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling sent notification")
    void shouldThrowExceptionWhenCancellingSentNotification() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Notification sentNotification = notification.markAsSent(123456789L);
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> sentNotification.cancel()
        );
        
        assertEquals("Cannot cancel sent notification", exception.getMessage());
    }

    @Test
    @DisplayName("Should identify when notification is ready to send")
    void shouldIdentifyWhenNotificationIsReadyToSend() {
        // Given
        Notification futureNotification = Notification.create(validTicketId, validCustomerId, 
                                                             validType, validMessage, 
                                                             LocalDateTime.now().plusMinutes(10));
        Notification immediateNotification = Notification.createImmediate(validTicketId, validCustomerId, 
                                                                         validType, validMessage);
        Notification sentNotification = immediateNotification.markAsSent(123456789L);
        
        // Then
        assertFalse(futureNotification.isReadyToSend()); // Scheduled for future
        assertTrue(immediateNotification.isReadyToSend()); // Immediate
        assertFalse(sentNotification.isReadyToSend()); // Already sent
    }

    @Test
    @DisplayName("Should identify when notification can retry")
    void shouldIdentifyWhenNotificationCanRetry() {
        // Given
        Notification pendingNotification = Notification.create(validTicketId, validCustomerId, 
                                                             validType, validMessage, validScheduledTime);
        Notification failedNotification = pendingNotification.markAsFailed("Error");
        Notification sentNotification = pendingNotification.markAsSent(123456789L);
        
        // Then
        assertFalse(pendingNotification.canRetry()); // Not failed
        assertTrue(failedNotification.canRetry()); // Failed and under retry limit
        assertFalse(sentNotification.canRetry()); // Already sent
    }

    @Test
    @DisplayName("Should identify urgent notifications")
    void shouldIdentifyUrgentNotifications() {
        // Given
        Notification urgentNotification = Notification.create(validTicketId, validCustomerId, 
                                                             NotificationType.TICKET_CALLED, 
                                                             validMessage, validScheduledTime);
        Notification normalNotification = Notification.create(validTicketId, validCustomerId, 
                                                             NotificationType.REMINDER, 
                                                             validMessage, validScheduledTime);
        
        // Then
        assertTrue(urgentNotification.isUrgent());
        assertFalse(normalNotification.isUrgent());
    }

    @Test
    @DisplayName("Should calculate next retry time with exponential backoff")
    void shouldCalculateNextRetryTimeWithExponentialBackoff() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        
        // When - First retry (retry count 0)
        Notification firstRetry = notification.markAsFailed("Error").incrementRetryCount();
        LocalDateTime firstRetryTime = firstRetry.getNextRetryTime();
        
        // When - Second retry (retry count 1)  
        Notification secondRetry = firstRetry.markAsFailed("Error").incrementRetryCount();
        LocalDateTime secondRetryTime = secondRetry.getNextRetryTime();
        
        // Then
        assertNotNull(firstRetryTime);
        assertNotNull(secondRetryTime);
        assertTrue(secondRetryTime.isAfter(firstRetryTime)); // Exponential backoff
    }

    @Test
    @DisplayName("Should return null for next retry time when cannot retry")
    void shouldReturnNullForNextRetryTimeWhenCannotRetry() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        Notification sentNotification = notification.markAsSent(123456789L);
        
        // When
        LocalDateTime nextRetryTime = sentNotification.getNextRetryTime();
        
        // Then
        assertNull(nextRetryTime);
    }

    @Test
    @DisplayName("Should not expire sent notifications")
    void shouldNotExpireSentNotifications() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, 
                                                       LocalDateTime.now().minusHours(25));
        Notification sentNotification = notification.markAsSent(123456789L);
        
        // Then
        assertFalse(sentNotification.isExpired()); // Sent notifications don't expire
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, validMessage, validScheduledTime);
        NotificationId originalId = notification.getId();
        NotificationStatus originalStatus = notification.getStatus();
        
        // When - Perform operations that return new instances
        Notification sent = notification.markAsSent(123456789L);
        Notification failed = notification.markAsFailed("Error");
        Notification cancelled = notification.cancel();
        
        // Then - Original notification should remain unchanged
        assertEquals(originalId, notification.getId());
        assertEquals(originalStatus, notification.getStatus());
        assertNull(notification.getTelegramMessageId());
        assertNull(notification.getSentAt());
        assertNull(notification.getFailedAt());
        
        // New instances should have different values
        assertNotSame(notification, sent);
        assertNotSame(notification, failed);
        assertNotSame(notification, cancelled);
    }

    @Test
    @DisplayName("Should handle maximum message length")
    void shouldHandleMaximumMessageLength() {
        // Given
        String maxLengthMessage = "a".repeat(1000);
        
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> 
            Notification.create(validTicketId, validCustomerId, validType, maxLengthMessage, validScheduledTime)
        );
        
        Notification notification = Notification.create(validTicketId, validCustomerId, 
                                                       validType, maxLengthMessage, validScheduledTime);
        assertEquals(1000, notification.getMessage().length());
    }
}