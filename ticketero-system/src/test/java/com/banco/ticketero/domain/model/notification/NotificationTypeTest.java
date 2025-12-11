package com.banco.ticketero.domain.model.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationType Enum Tests")
class NotificationTypeTest {

    @Test
    @DisplayName("Should have correct default messages")
    void shouldHaveCorrectDefaultMessages() {
        // Then
        assertEquals("Su ticket ha sido creado", NotificationType.TICKET_CREATED.getDefaultMessage());
        assertEquals("Su ticket está siendo llamado", NotificationType.TICKET_CALLED.getDefaultMessage());
        assertEquals("Actualización de cola", NotificationType.QUEUE_UPDATE.getDefaultMessage());
        assertEquals("Recordatorio de su ticket", NotificationType.REMINDER.getDefaultMessage());
    }

    @Test
    @DisplayName("Should identify urgent notifications correctly")
    void shouldIdentifyUrgentNotificationsCorrectly() {
        // Urgent notifications
        assertTrue(NotificationType.TICKET_CREATED.isUrgent());
        assertTrue(NotificationType.TICKET_CALLED.isUrgent());
        
        // Non-urgent notifications
        assertFalse(NotificationType.QUEUE_UPDATE.isUrgent());
        assertFalse(NotificationType.REMINDER.isUrgent());
    }

    @Test
    @DisplayName("Should require read confirmation for urgent notifications")
    void shouldRequireReadConfirmationForUrgentNotifications() {
        // Urgent notifications require confirmation
        assertTrue(NotificationType.TICKET_CREATED.requiresReadConfirmation());
        assertTrue(NotificationType.TICKET_CALLED.requiresReadConfirmation());
        
        // Non-urgent notifications don't require confirmation
        assertFalse(NotificationType.QUEUE_UPDATE.requiresReadConfirmation());
        assertFalse(NotificationType.REMINDER.requiresReadConfirmation());
    }

    @Test
    @DisplayName("Should have correct priority levels")
    void shouldHaveCorrectPriorityLevels() {
        // Then
        assertEquals(1, NotificationType.TICKET_CALLED.getPriority()); // Highest priority
        assertEquals(2, NotificationType.TICKET_CREATED.getPriority()); // High priority
        assertEquals(3, NotificationType.QUEUE_UPDATE.getPriority());   // Normal priority
        assertEquals(3, NotificationType.REMINDER.getPriority());       // Normal priority
    }

    @Test
    @DisplayName("Should correctly identify higher priority")
    void shouldCorrectlyIdentifyHigherPriority() {
        // TICKET_CALLED has highest priority
        assertTrue(NotificationType.TICKET_CALLED.hasHigherPriorityThan(NotificationType.TICKET_CREATED));
        assertTrue(NotificationType.TICKET_CALLED.hasHigherPriorityThan(NotificationType.QUEUE_UPDATE));
        assertTrue(NotificationType.TICKET_CALLED.hasHigherPriorityThan(NotificationType.REMINDER));
        
        // TICKET_CREATED has higher priority than normal notifications
        assertTrue(NotificationType.TICKET_CREATED.hasHigherPriorityThan(NotificationType.QUEUE_UPDATE));
        assertTrue(NotificationType.TICKET_CREATED.hasHigherPriorityThan(NotificationType.REMINDER));
        
        // QUEUE_UPDATE and REMINDER have same priority
        assertFalse(NotificationType.QUEUE_UPDATE.hasHigherPriorityThan(NotificationType.REMINDER));
        assertFalse(NotificationType.REMINDER.hasHigherPriorityThan(NotificationType.QUEUE_UPDATE));
        
        // Reverse should be false
        assertFalse(NotificationType.TICKET_CREATED.hasHigherPriorityThan(NotificationType.TICKET_CALLED));
        assertFalse(NotificationType.QUEUE_UPDATE.hasHigherPriorityThan(NotificationType.TICKET_CREATED));
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        // Given
        NotificationType[] expectedValues = {
            NotificationType.TICKET_CREATED,
            NotificationType.TICKET_CALLED,
            NotificationType.QUEUE_UPDATE,
            NotificationType.REMINDER
        };
        
        // When
        NotificationType[] actualValues = NotificationType.values();
        
        // Then
        assertEquals(4, actualValues.length);
        assertArrayEquals(expectedValues, actualValues);
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    @DisplayName("Should have non-null default message for all notification types")
    void shouldHaveNonNullDefaultMessageForAllNotificationTypes(NotificationType notificationType) {
        // Then
        assertNotNull(notificationType.getDefaultMessage(), 
            "Default message should not be null for " + notificationType);
        assertFalse(notificationType.getDefaultMessage().trim().isEmpty(), 
            "Default message should not be empty for " + notificationType);
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    @DisplayName("Should have valid priority for all notification types")
    void shouldHaveValidPriorityForAllNotificationTypes(NotificationType notificationType) {
        // Then
        int priority = notificationType.getPriority();
        assertTrue(priority >= 1 && priority <= 3, 
            "Priority should be between 1 and 3 for " + notificationType);
    }

    @Test
    @DisplayName("Should handle enum valueOf correctly")
    void shouldHandleEnumValueOfCorrectly() {
        // Given
        String notificationTypeName = "TICKET_CREATED";
        
        // When
        NotificationType notificationType = NotificationType.valueOf(notificationTypeName);
        
        // Then
        assertEquals(NotificationType.TICKET_CREATED, notificationType);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        String invalidNotificationTypeName = "INVALID_NOTIFICATION";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> NotificationType.valueOf(invalidNotificationTypeName));
    }

    @Test
    @DisplayName("Should not have higher priority than itself")
    void shouldNotHaveHigherPriorityThanItself() {
        // Given all notification types
        NotificationType[] allTypes = NotificationType.values();
        
        // Then - No notification type should have higher priority than itself
        for (NotificationType notificationType : allTypes) {
            assertFalse(notificationType.hasHigherPriorityThan(notificationType),
                String.format("%s should not have higher priority than itself", notificationType));
        }
    }

    @Test
    @DisplayName("Should maintain consistency between urgency and read confirmation")
    void shouldMaintainConsistencyBetweenUrgencyAndReadConfirmation() {
        // Given all notification types
        NotificationType[] allTypes = NotificationType.values();
        
        // Then - Urgent notifications should require read confirmation
        for (NotificationType notificationType : allTypes) {
            assertEquals(notificationType.isUrgent(), notificationType.requiresReadConfirmation(),
                String.format("Urgency and read confirmation should be consistent for %s", notificationType));
        }
    }

    @Test
    @DisplayName("Should have meaningful default messages")
    void shouldHaveMeaningfulDefaultMessages() {
        // All default messages should contain relevant keywords
        assertTrue(NotificationType.TICKET_CREATED.getDefaultMessage().toLowerCase().contains("ticket"));
        assertTrue(NotificationType.TICKET_CALLED.getDefaultMessage().toLowerCase().contains("ticket"));
        assertTrue(NotificationType.QUEUE_UPDATE.getDefaultMessage().toLowerCase().contains("cola"));
        assertTrue(NotificationType.REMINDER.getDefaultMessage().toLowerCase().contains("ticket"));
    }
}