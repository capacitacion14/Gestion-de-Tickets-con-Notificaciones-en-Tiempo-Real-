package com.banco.ticketero.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateTicketRequest DTO Tests")
class CreateTicketRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Should create valid request")
    void shouldCreateValidRequest() {
        // Given
        String nationalId = "12345678";
        String queueType = "GENERAL";
        
        // When
        CreateTicketRequest request = new CreateTicketRequest(nationalId, queueType);
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals("12345678", request.nationalId());
        assertEquals("GENERAL", request.queueType());
    }

    @Test
    @DisplayName("Should normalize national ID by removing non-digits")
    void shouldNormalizeNationalIdByRemovingNonDigits() {
        // Given
        String nationalIdWithFormatting = "12.345.678-9";
        
        // When
        CreateTicketRequest request = new CreateTicketRequest(nationalIdWithFormatting, "GENERAL");
        
        // Then
        assertEquals("123456789", request.nationalId());
    }

    @Test
    @DisplayName("Should normalize queue type to uppercase")
    void shouldNormalizeQueueTypeToUppercase() {
        // Given
        String lowerCaseQueueType = "general";
        
        // When
        CreateTicketRequest request = new CreateTicketRequest("12345678", lowerCaseQueueType);
        
        // Then
        assertEquals("GENERAL", request.queueType());
    }

    @Test
    @DisplayName("Should trim whitespace from inputs")
    void shouldTrimWhitespaceFromInputs() {
        // Given
        String nationalIdWithSpaces = "  12345678  ";
        String queueTypeWithSpaces = "  general  ";
        
        // When
        CreateTicketRequest request = new CreateTicketRequest(nationalIdWithSpaces, queueTypeWithSpaces);
        
        // Then
        assertEquals("12345678", request.nationalId());
        assertEquals("GENERAL", request.queueType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "1234567", "123456789012345678901"})
    @DisplayName("Should fail validation for invalid national ID")
    void shouldFailValidationForInvalidNationalId(String invalidNationalId) {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(invalidNationalId, "GENERAL");
        
        // When
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nationalId")));
    }

    @Test
    @DisplayName("Should fail validation for null national ID")
    void shouldFailValidationForNullNationalId() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest(null, "GENERAL");
        
        // When
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("National ID is required")));
    }

    @Test
    @DisplayName("Should fail validation for null queue type")
    void shouldFailValidationForNullQueueType() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", null);
        
        // When
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Queue type is required")));
    }

    @Test
    @DisplayName("Should accept valid national ID lengths")
    void shouldAcceptValidNationalIdLengths() {
        // Given valid lengths (8-20 digits)
        String minLength = "12345678";        // 8 digits
        String maxLength = "12345678901234567890"; // 20 digits
        
        // When
        CreateTicketRequest minRequest = new CreateTicketRequest(minLength, "GENERAL");
        CreateTicketRequest maxRequest = new CreateTicketRequest(maxLength, "GENERAL");
        
        Set<ConstraintViolation<CreateTicketRequest>> minViolations = validator.validate(minRequest);
        Set<ConstraintViolation<CreateTicketRequest>> maxViolations = validator.validate(maxRequest);
        
        // Then
        assertTrue(minViolations.isEmpty());
        assertTrue(maxViolations.isEmpty());
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "GENERAL");
        
        // When & Then - Records are immutable by design
        assertNotNull(request.nationalId());
        assertNotNull(request.queueType());
        
        // Verify it's a record
        assertTrue(request.getClass().isRecord());
    }

    @Test
    @DisplayName("Should handle null inputs gracefully in constructor")
    void shouldHandleNullInputsGracefullyInConstructor() {
        // When & Then - Should not throw exception, validation will catch nulls
        assertDoesNotThrow(() -> new CreateTicketRequest(null, null));
        
        CreateTicketRequest request = new CreateTicketRequest(null, null);
        assertNull(request.nationalId());
        assertNull(request.queueType());
    }
}