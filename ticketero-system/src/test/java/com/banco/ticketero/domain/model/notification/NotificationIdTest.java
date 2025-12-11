package com.banco.ticketero.domain.model.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationId Value Object Tests")
class NotificationIdTest {

    @Test
    @DisplayName("Should generate new NotificationId with random UUID")
    void shouldGenerateNewNotificationId() {
        // When
        NotificationId notificationId = NotificationId.generate();
        
        // Then
        assertNotNull(notificationId);
        assertNotNull(notificationId.getValue());
    }

    @Test
    @DisplayName("Should create NotificationId from existing UUID")
    void shouldCreateNotificationIdFromUuid() {
        // Given
        UUID uuid = UUID.randomUUID();
        
        // When
        NotificationId notificationId = NotificationId.of(uuid);
        
        // Then
        assertNotNull(notificationId);
        assertEquals(uuid, notificationId.getValue());
    }

    @Test
    @DisplayName("Should create NotificationId from valid UUID string")
    void shouldCreateNotificationIdFromValidString() {
        // Given
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        NotificationId notificationId = NotificationId.of(uuidString);
        
        // Then
        assertNotNull(notificationId);
        assertEquals(uuidString, notificationId.getValue().toString());
    }

    @Test
    @DisplayName("Should throw exception when creating NotificationId with null UUID")
    void shouldThrowExceptionWhenUuidIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NotificationId.of((UUID) null)
        );
        
        assertEquals("NotificationId cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating NotificationId with invalid UUID string")
    void shouldThrowExceptionWhenUuidStringIsInvalid() {
        // Given
        String invalidUuid = "invalid-uuid-string";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NotificationId.of(invalidUuid)
        );
        
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    @DisplayName("Should be equal when UUIDs are the same")
    void shouldBeEqualWhenUuidsAreSame() {
        // Given
        UUID uuid = UUID.randomUUID();
        NotificationId notificationId1 = NotificationId.of(uuid);
        NotificationId notificationId2 = NotificationId.of(uuid);
        
        // Then
        assertEquals(notificationId1, notificationId2);
        assertEquals(notificationId1.hashCode(), notificationId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when UUIDs are different")
    void shouldNotBeEqualWhenUuidsAreDifferent() {
        // Given
        NotificationId notificationId1 = NotificationId.generate();
        NotificationId notificationId2 = NotificationId.generate();
        
        // Then
        assertNotEquals(notificationId1, notificationId2);
    }

    @Test
    @DisplayName("Should return UUID string representation")
    void shouldReturnUuidStringRepresentation() {
        // Given
        UUID uuid = UUID.randomUUID();
        NotificationId notificationId = NotificationId.of(uuid);
        
        // When
        String result = notificationId.toString();
        
        // Then
        assertEquals(uuid.toString(), result);
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        NotificationId notificationId = NotificationId.generate();
        UUID originalValue = notificationId.getValue();
        
        // When - Try to get the value multiple times
        UUID value1 = notificationId.getValue();
        UUID value2 = notificationId.getValue();
        
        // Then - Should always return the same instance
        assertSame(originalValue, value1);
        assertSame(originalValue, value2);
    }
}