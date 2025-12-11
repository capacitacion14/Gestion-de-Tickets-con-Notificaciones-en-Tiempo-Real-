package com.banco.ticketero.domain.model.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueueId Value Object Tests")
class QueueIdTest {

    @Test
    @DisplayName("Should generate new QueueId with random UUID")
    void shouldGenerateNewQueueId() {
        // When
        QueueId queueId = QueueId.generate();
        
        // Then
        assertNotNull(queueId);
        assertNotNull(queueId.getValue());
    }

    @Test
    @DisplayName("Should create QueueId from existing UUID")
    void shouldCreateQueueIdFromUuid() {
        // Given
        UUID uuid = UUID.randomUUID();
        
        // When
        QueueId queueId = QueueId.of(uuid);
        
        // Then
        assertNotNull(queueId);
        assertEquals(uuid, queueId.getValue());
    }

    @Test
    @DisplayName("Should create QueueId from valid UUID string")
    void shouldCreateQueueIdFromValidString() {
        // Given
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        QueueId queueId = QueueId.of(uuidString);
        
        // Then
        assertNotNull(queueId);
        assertEquals(uuidString, queueId.getValue().toString());
    }

    @Test
    @DisplayName("Should throw exception when creating QueueId with null UUID")
    void shouldThrowExceptionWhenUuidIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QueueId.of((UUID) null)
        );
        
        assertEquals("QueueId cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating QueueId with invalid UUID string")
    void shouldThrowExceptionWhenUuidStringIsInvalid() {
        // Given
        String invalidUuid = "invalid-uuid-string";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QueueId.of(invalidUuid)
        );
        
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    @DisplayName("Should be equal when UUIDs are the same")
    void shouldBeEqualWhenUuidsAreSame() {
        // Given
        UUID uuid = UUID.randomUUID();
        QueueId queueId1 = QueueId.of(uuid);
        QueueId queueId2 = QueueId.of(uuid);
        
        // Then
        assertEquals(queueId1, queueId2);
        assertEquals(queueId1.hashCode(), queueId2.hashCode());
    }

    @Test
    @DisplayName("Should return UUID string representation")
    void shouldReturnUuidStringRepresentation() {
        // Given
        UUID uuid = UUID.randomUUID();
        QueueId queueId = QueueId.of(uuid);
        
        // When
        String result = queueId.toString();
        
        // Then
        assertEquals(uuid.toString(), result);
    }
}