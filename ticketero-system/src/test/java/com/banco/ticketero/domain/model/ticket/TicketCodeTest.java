package com.banco.ticketero.domain.model.ticket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketCode Value Object Tests")
class TicketCodeTest {

    @Test
    @DisplayName("Should create TicketCode from valid string")
    void shouldCreateTicketCodeFromValidString() {
        // Given
        String validCode = "T1001";
        
        // When
        TicketCode ticketCode = TicketCode.of(validCode);
        
        // Then
        assertNotNull(ticketCode);
        assertEquals("T1001", ticketCode.getValue());
    }

    @Test
    @DisplayName("Should create TicketCode from sequence number")
    void shouldCreateTicketCodeFromSequence() {
        // Given
        int sequence = 1234;
        
        // When
        TicketCode ticketCode = TicketCode.fromSequence(sequence);
        
        // Then
        assertNotNull(ticketCode);
        assertEquals("T1234", ticketCode.getValue());
        assertEquals(1234, ticketCode.getSequenceNumber());
    }

    @Test
    @DisplayName("Should normalize code to uppercase")
    void shouldNormalizeCodeToUppercase() {
        // Given
        String lowerCaseCode = "t1001";
        
        // When
        TicketCode ticketCode = TicketCode.of(lowerCaseCode);
        
        // Then
        assertEquals("T1001", ticketCode.getValue());
    }

    @Test
    @DisplayName("Should trim whitespace from code")
    void shouldTrimWhitespaceFromCode() {
        // Given
        String codeWithSpaces = "  T1001  ";
        
        // When
        TicketCode ticketCode = TicketCode.of(codeWithSpaces);
        
        // Then
        assertEquals("T1001", ticketCode.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "T100", "T10000", "A1001", "T1ABC", "1001", "TT1001"})
    @DisplayName("Should throw exception for invalid code formats")
    void shouldThrowExceptionForInvalidFormats(String invalidCode) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TicketCode.of(invalidCode)
        );
        
        assertTrue(exception.getMessage().contains("must follow format T####"));
    }

    @Test
    @DisplayName("Should throw exception when code is null")
    void shouldThrowExceptionWhenCodeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TicketCode.of(null)
        );
        
        assertEquals("TicketCode cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {999, 10000, 0, -1})
    @DisplayName("Should throw exception for invalid sequence numbers")
    void shouldThrowExceptionForInvalidSequence(int invalidSequence) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> TicketCode.fromSequence(invalidSequence)
        );
        
        assertTrue(exception.getMessage().contains("must be between 1000 and 9999"));
    }

    @Test
    @DisplayName("Should extract correct sequence number")
    void shouldExtractCorrectSequenceNumber() {
        // Given
        TicketCode ticketCode = TicketCode.of("T5678");
        
        // When
        int sequence = ticketCode.getSequenceNumber();
        
        // Then
        assertEquals(5678, sequence);
    }

    @Test
    @DisplayName("Should be equal when codes are the same")
    void shouldBeEqualWhenCodesAreSame() {
        // Given
        TicketCode code1 = TicketCode.of("T1001");
        TicketCode code2 = TicketCode.of("t1001"); // Different case
        
        // Then
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when codes are different")
    void shouldNotBeEqualWhenCodesAreDifferent() {
        // Given
        TicketCode code1 = TicketCode.of("T1001");
        TicketCode code2 = TicketCode.of("T1002");
        
        // Then
        assertNotEquals(code1, code2);
    }

    @Test
    @DisplayName("Should return code string representation")
    void shouldReturnCodeStringRepresentation() {
        // Given
        TicketCode ticketCode = TicketCode.of("T1001");
        
        // When
        String result = ticketCode.toString();
        
        // Then
        assertEquals("T1001", result);
    }

    @Test
    @DisplayName("Should handle boundary sequence numbers")
    void shouldHandleBoundarySequenceNumbers() {
        // Given & When
        TicketCode minCode = TicketCode.fromSequence(1000);
        TicketCode maxCode = TicketCode.fromSequence(9999);
        
        // Then
        assertEquals("T1000", minCode.getValue());
        assertEquals(1000, minCode.getSequenceNumber());
        assertEquals("T9999", maxCode.getValue());
        assertEquals(9999, maxCode.getSequenceNumber());
    }
}