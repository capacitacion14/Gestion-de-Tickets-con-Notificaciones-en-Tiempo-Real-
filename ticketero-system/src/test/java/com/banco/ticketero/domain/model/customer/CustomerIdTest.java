package com.banco.ticketero.domain.model.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerId Value Object Tests")
class CustomerIdTest {

    @Test
    @DisplayName("Should generate new CustomerId with random UUID")
    void shouldGenerateNewCustomerId() {
        // When
        CustomerId customerId = CustomerId.generate();
        
        // Then
        assertNotNull(customerId);
        assertNotNull(customerId.getValue());
    }

    @Test
    @DisplayName("Should create CustomerId from existing UUID")
    void shouldCreateCustomerIdFromUuid() {
        // Given
        UUID uuid = UUID.randomUUID();
        
        // When
        CustomerId customerId = CustomerId.of(uuid);
        
        // Then
        assertNotNull(customerId);
        assertEquals(uuid, customerId.getValue());
    }

    @Test
    @DisplayName("Should create CustomerId from valid UUID string")
    void shouldCreateCustomerIdFromValidString() {
        // Given
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        CustomerId customerId = CustomerId.of(uuidString);
        
        // Then
        assertNotNull(customerId);
        assertEquals(uuidString, customerId.getValue().toString());
    }

    @Test
    @DisplayName("Should throw exception when creating CustomerId with null UUID")
    void shouldThrowExceptionWhenUuidIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CustomerId.of((UUID) null)
        );
        
        assertEquals("CustomerId cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating CustomerId with invalid UUID string")
    void shouldThrowExceptionWhenUuidStringIsInvalid() {
        // Given
        String invalidUuid = "invalid-uuid-string";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CustomerId.of(invalidUuid)
        );
        
        assertTrue(exception.getMessage().contains("Invalid UUID format"));
    }

    @Test
    @DisplayName("Should be equal when UUIDs are the same")
    void shouldBeEqualWhenUuidsAreSame() {
        // Given
        UUID uuid = UUID.randomUUID();
        CustomerId customerId1 = CustomerId.of(uuid);
        CustomerId customerId2 = CustomerId.of(uuid);
        
        // Then
        assertEquals(customerId1, customerId2);
        assertEquals(customerId1.hashCode(), customerId2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when UUIDs are different")
    void shouldNotBeEqualWhenUuidsAreDifferent() {
        // Given
        CustomerId customerId1 = CustomerId.generate();
        CustomerId customerId2 = CustomerId.generate();
        
        // Then
        assertNotEquals(customerId1, customerId2);
    }

    @Test
    @DisplayName("Should return UUID string representation")
    void shouldReturnUuidStringRepresentation() {
        // Given
        UUID uuid = UUID.randomUUID();
        CustomerId customerId = CustomerId.of(uuid);
        
        // When
        String result = customerId.toString();
        
        // Then
        assertEquals(uuid.toString(), result);
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        CustomerId customerId = CustomerId.generate();
        UUID originalValue = customerId.getValue();
        
        // When - Try to get the value multiple times
        UUID value1 = customerId.getValue();
        UUID value2 = customerId.getValue();
        
        // Then - Should always return the same instance
        assertSame(originalValue, value1);
        assertSame(originalValue, value2);
    }
}