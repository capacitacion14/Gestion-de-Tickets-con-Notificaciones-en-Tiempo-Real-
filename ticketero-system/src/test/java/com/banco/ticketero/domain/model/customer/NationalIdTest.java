package com.banco.ticketero.domain.model.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NationalId Value Object Tests")
class NationalIdTest {

    @Test
    @DisplayName("Should create NationalId from valid string")
    void shouldCreateNationalIdFromValidString() {
        // Given
        String validId = "12345678";
        
        // When
        NationalId nationalId = NationalId.of(validId);
        
        // Then
        assertNotNull(nationalId);
        assertEquals("12345678", nationalId.getValue());
    }

    @Test
    @DisplayName("Should clean and normalize input")
    void shouldCleanAndNormalizeInput() {
        // Given
        String idWithFormatting = "12.345.678-9";
        
        // When
        NationalId nationalId = NationalId.of(idWithFormatting);
        
        // Then
        assertEquals("123456789", nationalId.getValue());
    }

    @Test
    @DisplayName("Should trim whitespace")
    void shouldTrimWhitespace() {
        // Given
        String idWithSpaces = "  12345678  ";
        
        // When
        NationalId nationalId = NationalId.of(idWithSpaces);
        
        // Then
        assertEquals("12345678", nationalId.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "123456789", "12345678901234567890"})
    @DisplayName("Should accept valid length national IDs")
    void shouldAcceptValidLengthNationalIds(String validId) {
        // When & Then
        assertDoesNotThrow(() -> NationalId.of(validId));
        
        NationalId nationalId = NationalId.of(validId);
        assertTrue(nationalId.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789012345678901"})
    @DisplayName("Should reject invalid length national IDs")
    void shouldRejectInvalidLengthNationalIds(String invalidId) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NationalId.of(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("must be between 8 and 20 digits"));
    }

    @Test
    @DisplayName("Should throw exception when national ID is null")
    void shouldThrowExceptionWhenNationalIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> NationalId.of(null)
        );
        
        assertEquals("NationalId cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject empty national IDs")
    void shouldRejectEmptyNationalIds() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> NationalId.of(""));
        assertThrows(IllegalArgumentException.class, () -> NationalId.of("   "));
    }
    
    @Test
    @DisplayName("Should reject non-numeric national IDs")
    void shouldRejectNonNumericNationalIds() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> NationalId.of("abc"));
        assertThrows(IllegalArgumentException.class, () -> NationalId.of("!@#$%"));
    }

    @Test
    @DisplayName("Should format national ID for display")
    void shouldFormatNationalIdForDisplay() {
        // Given
        NationalId shortId = NationalId.of("12345678");
        NationalId longId = NationalId.of("123456789012");
        
        // When
        String shortFormatted = shortId.getFormattedValue();
        String longFormatted = longId.getFormattedValue();
        
        // Then
        assertEquals("12345678", shortFormatted); // No formatting for 8 digits
        assertEquals("123.456.789.012", longFormatted); // Formatted for longer IDs
    }

    @Test
    @DisplayName("Should be equal when values are the same")
    void shouldBeEqualWhenValuesAreSame() {
        // Given
        NationalId id1 = NationalId.of("12345678");
        NationalId id2 = NationalId.of("12.345.678"); // Different format, same value
        
        // Then
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values are different")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Given
        NationalId id1 = NationalId.of("12345678");
        NationalId id2 = NationalId.of("87654321");
        
        // Then
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Should return clean value as string representation")
    void shouldReturnCleanValueAsStringRepresentation() {
        // Given
        NationalId nationalId = NationalId.of("12.345.678-9");
        
        // When
        String result = nationalId.toString();
        
        // Then
        assertEquals("123456789", result);
    }

    @Test
    @DisplayName("Should validate correctly")
    void shouldValidateCorrectly() {
        // Given
        NationalId validId = NationalId.of("12345678");
        
        // When & Then
        assertTrue(validId.isValid());
    }

    @Test
    @DisplayName("Should handle edge cases in formatting")
    void shouldHandleEdgeCasesInFormatting() {
        // Given
        NationalId exactlyEight = NationalId.of("12345678");
        NationalId nineDigits = NationalId.of("123456789");
        NationalId twelveDigits = NationalId.of("123456789012");
        
        // When & Then
        assertEquals("12345678", exactlyEight.getFormattedValue());
        assertEquals("123.456.789", nineDigits.getFormattedValue());
        assertEquals("123.456.789.012", twelveDigits.getFormattedValue());
    }

    @Test
    @DisplayName("Should remove all non-numeric characters")
    void shouldRemoveAllNonNumericCharacters() {
        // Given
        String idWithMixedChars = "12a.345b-678c9d";
        
        // When
        NationalId nationalId = NationalId.of(idWithMixedChars);
        
        // Then
        assertEquals("123456789", nationalId.getValue());
    }
}