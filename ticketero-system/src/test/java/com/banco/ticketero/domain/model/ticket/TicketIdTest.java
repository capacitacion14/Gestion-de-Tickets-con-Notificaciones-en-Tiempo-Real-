package com.banco.ticketero.domain.model.ticket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketId Value Object Tests")
class TicketIdTest {

    @Test
    @DisplayName("Should generate new TicketId with random UUID")
    void shouldGenerateNewTicketId() {
        // When
        TicketId ticketId = TicketId.generate();
        
        // Then
        assertNotNull(ticketId);
        assertNotNull(ticketId.getValue());
    }

    @Test
    @DisplayName("Should create TicketId from existing UUID")
    void shouldCreateTicketIdFromUuid() {
        // Given
        UUID uuid = UUID.randomUUID();
        
        // When
        TicketId ticketId = TicketId.of(uuid);
        
        // Then
        assertNotNull(ticketId);
        assertEquals(uuid, ticketId.getValue());
    }

    @Test
    @DisplayName("Should create TicketId from valid UUID string")
    void shouldCreateTicketIdFromValidString() {
        // Given
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        TicketId ticketId = TicketId.of(uuidString);
        
        // Then
        assertNotNull(ticketId);
        assertEquals(uuidString, ticketId.getValue().toString());
    }

    @Test
    @DisplayName("Should throw exception when creating TicketId with null UUID")
    void shouldThrowExceptionWhenUuidIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TicketId.of((UUID) null)
        );
        
        assertEquals("TicketId cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating TicketId with invalid UUID string")
    void shouldThrowExceptionWhenUuidStringIsInvalid() {
        // Given
        String invalidUuid = "invalid-uuid-string";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TicketId.of(invalidUuid)
        );
        
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    @DisplayName("Should be equal when UUIDs are the same")
    void shouldBeEqualWhenUuidsAreSame() {
        // Given
        UUID uuid = UUID.randomUUID();
        TicketId ticketId1 = TicketId.of(uuid);
        TicketId ticketId2 = TicketId.of(uuid);
        
        // Then
        assertEquals(ticketId1, ticketId2);
        assertEquals(ticketId1.hashCode(), ticketId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when UUIDs are different")
    void shouldNotBeEqualWhenUuidsAreDifferent() {
        // Given
        TicketId ticketId1 = TicketId.generate();
        TicketId ticketId2 = TicketId.generate();
        
        // Then
        assertNotEquals(ticketId1, ticketId2);
    }

    @Test
    @DisplayName("Should return UUID string representation")
    void shouldReturnUuidStringRepresentation() {
        // Given
        UUID uuid = UUID.randomUUID();
        TicketId ticketId = TicketId.of(uuid);
        
        // When
        String result = ticketId.toString();
        
        // Then
        assertEquals(uuid.toString(), result);
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        TicketId ticketId = TicketId.generate();
        UUID originalValue = ticketId.getValue();
        
        // When - Try to get the value multiple times
        UUID value1 = ticketId.getValue();
        UUID value2 = ticketId.getValue();
        
        // Then - Should always return the same instance
        assertSame(originalValue, value1);
        assertSame(originalValue, value2);
    }
}