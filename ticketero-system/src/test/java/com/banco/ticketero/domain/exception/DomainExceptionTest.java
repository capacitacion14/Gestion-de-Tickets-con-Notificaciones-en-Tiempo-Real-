package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.model.notification.NotificationId;
import com.banco.ticketero.domain.model.queue.QueueId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Exception Tests")
class DomainExceptionTest {

    @Test
    @DisplayName("TicketNotFoundException should create with ticket ID")
    void ticketNotFoundExceptionShouldCreateWithTicketId() {
        // Given
        TicketId ticketId = TicketId.generate();
        
        // When
        TicketNotFoundException exception = new TicketNotFoundException(ticketId);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(ticketId.toString()));
        assertInstanceOf(DomainException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("TicketNotFoundException should create with ticket code")
    void ticketNotFoundExceptionShouldCreateWithTicketCode() {
        // Given
        TicketCode ticketCode = TicketCode.fromSequence(1001);
        
        // When
        TicketNotFoundException exception = new TicketNotFoundException(ticketCode);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("T1001"));
    }

    @Test
    @DisplayName("TicketNotFoundException should create with custom message")
    void ticketNotFoundExceptionShouldCreateWithCustomMessage() {
        // Given
        String customMessage = "Custom ticket not found message";
        
        // When
        TicketNotFoundException exception = new TicketNotFoundException(customMessage);
        
        // Then
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("InvalidTicketStatusException should create with status transition")
    void invalidTicketStatusExceptionShouldCreateWithStatusTransition() {
        // Given
        TicketStatus currentStatus = TicketStatus.COMPLETED;
        TicketStatus targetStatus = TicketStatus.PENDING;
        
        // When
        InvalidTicketStatusException exception = new InvalidTicketStatusException(currentStatus, targetStatus);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("COMPLETED"));
        assertTrue(exception.getMessage().contains("PENDING"));
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("InvalidTicketStatusException should create with operation")
    void invalidTicketStatusExceptionShouldCreateWithOperation() {
        // Given
        TicketStatus currentStatus = TicketStatus.COMPLETED;
        String operation = "call";
        
        // When
        InvalidTicketStatusException exception = new InvalidTicketStatusException(currentStatus, operation);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("call"));
        assertTrue(exception.getMessage().contains("COMPLETED"));
        assertTrue(exception.getMessage().contains("Cannot perform operation"));
    }

    @Test
    @DisplayName("QueueFullException should create with capacity details")
    void queueFullExceptionShouldCreateWithCapacityDetails() {
        // Given
        QueueType queueType = QueueType.GENERAL;
        int currentCapacity = 50;
        int maxCapacity = 50;
        
        // When
        QueueFullException exception = new QueueFullException(queueType, currentCapacity, maxCapacity);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("GENERAL"));
        assertTrue(exception.getMessage().contains("50/50"));
        assertTrue(exception.getMessage().contains("full"));
    }

    @Test
    @DisplayName("QueueFullException should create with queue type only")
    void queueFullExceptionShouldCreateWithQueueTypeOnly() {
        // Given
        QueueType queueType = QueueType.VIP;
        
        // When
        QueueFullException exception = new QueueFullException(queueType);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("VIP"));
        assertTrue(exception.getMessage().contains("maximum capacity"));
    }

    @Test
    @DisplayName("CustomerNotFoundException should create with customer ID")
    void customerNotFoundExceptionShouldCreateWithCustomerId() {
        // Given
        CustomerId customerId = CustomerId.generate();
        
        // When
        CustomerNotFoundException exception = new CustomerNotFoundException(customerId);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(customerId.toString()));
        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    @DisplayName("CustomerNotFoundException should create with national ID")
    void customerNotFoundExceptionShouldCreateWithNationalId() {
        // Given
        NationalId nationalId = NationalId.of("12345678");
        
        // When
        CustomerNotFoundException exception = new CustomerNotFoundException(nationalId);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("12345678"));
        assertTrue(exception.getMessage().contains("national ID"));
    }

    @Test
    @DisplayName("CustomerNotFoundException should create with Telegram chat ID")
    void customerNotFoundExceptionShouldCreateWithTelegramChatId() {
        // Given
        Long telegramChatId = 123456789L;
        
        // When
        CustomerNotFoundException exception = new CustomerNotFoundException(telegramChatId);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("123456789"));
        assertTrue(exception.getMessage().contains("Telegram chat ID"));
    }

    @Test
    @DisplayName("NotificationException should create with notification ID and reason")
    void notificationExceptionShouldCreateWithNotificationIdAndReason() {
        // Given
        NotificationId notificationId = NotificationId.generate();
        String reason = "Network timeout";
        
        // When
        NotificationException exception = new NotificationException(notificationId, reason);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(notificationId.toString()));
        assertTrue(exception.getMessage().contains("Network timeout"));
        assertTrue(exception.getMessage().contains("Notification error"));
    }

    @Test
    @DisplayName("NotificationException should create with message and cause")
    void notificationExceptionShouldCreateWithMessageAndCause() {
        // Given
        String message = "Notification processing failed";
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        NotificationException exception = new NotificationException(message, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("QueueNotFoundException should create with queue ID")
    void queueNotFoundExceptionShouldCreateWithQueueId() {
        // Given
        QueueId queueId = QueueId.generate();
        
        // When
        QueueNotFoundException exception = new QueueNotFoundException(queueId);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(queueId.toString()));
        assertTrue(exception.getMessage().contains("Queue not found"));
    }

    @Test
    @DisplayName("QueueNotFoundException should create with queue type")
    void queueNotFoundExceptionShouldCreateWithQueueType() {
        // Given
        QueueType queueType = QueueType.BUSINESS;
        
        // When
        QueueNotFoundException exception = new QueueNotFoundException(queueType);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("BUSINESS"));
        assertTrue(exception.getMessage().contains("Queue not found for type"));
    }

    @Test
    @DisplayName("All domain exceptions should extend DomainException")
    void allDomainExceptionsShouldExtendDomainException() {
        // Given & When
        TicketNotFoundException ticketException = new TicketNotFoundException("test");
        InvalidTicketStatusException statusException = new InvalidTicketStatusException("test");
        QueueFullException queueFullException = new QueueFullException("test");
        CustomerNotFoundException customerException = new CustomerNotFoundException("test");
        NotificationException notificationException = new NotificationException("test");
        QueueNotFoundException queueNotFoundException = new QueueNotFoundException("test");
        
        // Then
        assertInstanceOf(DomainException.class, ticketException);
        assertInstanceOf(DomainException.class, statusException);
        assertInstanceOf(DomainException.class, queueFullException);
        assertInstanceOf(DomainException.class, customerException);
        assertInstanceOf(DomainException.class, notificationException);
        assertInstanceOf(DomainException.class, queueNotFoundException);
    }

    @Test
    @DisplayName("All domain exceptions should be runtime exceptions")
    void allDomainExceptionsShouldBeRuntimeExceptions() {
        // Given & When
        TicketNotFoundException ticketException = new TicketNotFoundException("test");
        InvalidTicketStatusException statusException = new InvalidTicketStatusException("test");
        QueueFullException queueFullException = new QueueFullException("test");
        CustomerNotFoundException customerException = new CustomerNotFoundException("test");
        NotificationException notificationException = new NotificationException("test");
        QueueNotFoundException queueNotFoundException = new QueueNotFoundException("test");
        
        // Then
        assertInstanceOf(RuntimeException.class, ticketException);
        assertInstanceOf(RuntimeException.class, statusException);
        assertInstanceOf(RuntimeException.class, queueFullException);
        assertInstanceOf(RuntimeException.class, customerException);
        assertInstanceOf(RuntimeException.class, notificationException);
        assertInstanceOf(RuntimeException.class, queueNotFoundException);
    }

    @Test
    @DisplayName("Domain exceptions should preserve stack trace")
    void domainExceptionsShouldPreserveStackTrace() {
        // Given
        String message = "Test exception";
        
        // When
        try {
            throw new TicketNotFoundException(message);
        } catch (TicketNotFoundException e) {
            // Then
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    @DisplayName("NotificationException should preserve cause chain")
    void notificationExceptionShouldPreserveCauseChain() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root cause");
        String message = "Notification failed";
        
        // When
        NotificationException exception = new NotificationException(message, rootCause);
        
        // Then
        assertEquals(rootCause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }
}