package com.banco.ticketero.domain.model.ticket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketStatus Enum Tests")
class TicketStatusTest {

    @Test
    @DisplayName("Should allow valid transitions from PENDING")
    void shouldAllowValidTransitionsFromPending() {
        // Given
        TicketStatus pending = TicketStatus.PENDING;
        
        // Then
        assertTrue(pending.canTransitionTo(TicketStatus.CALLED));
        assertTrue(pending.canTransitionTo(TicketStatus.CANCELLED));
        
        assertFalse(pending.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertFalse(pending.canTransitionTo(TicketStatus.COMPLETED));
        assertFalse(pending.canTransitionTo(TicketStatus.NO_SHOW));
        assertFalse(pending.canTransitionTo(TicketStatus.PENDING));
    }

    @Test
    @DisplayName("Should allow valid transitions from CALLED")
    void shouldAllowValidTransitionsFromCalled() {
        // Given
        TicketStatus called = TicketStatus.CALLED;
        
        // Then
        assertTrue(called.canTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(called.canTransitionTo(TicketStatus.NO_SHOW));
        assertTrue(called.canTransitionTo(TicketStatus.CANCELLED));
        
        assertFalse(called.canTransitionTo(TicketStatus.PENDING));
        assertFalse(called.canTransitionTo(TicketStatus.COMPLETED));
        assertFalse(called.canTransitionTo(TicketStatus.CALLED));
    }

    @Test
    @DisplayName("Should allow valid transitions from IN_PROGRESS")
    void shouldAllowValidTransitionsFromInProgress() {
        // Given
        TicketStatus inProgress = TicketStatus.IN_PROGRESS;
        
        // Then
        assertTrue(inProgress.canTransitionTo(TicketStatus.COMPLETED));
        assertTrue(inProgress.canTransitionTo(TicketStatus.CANCELLED));
        
        assertFalse(inProgress.canTransitionTo(TicketStatus.PENDING));
        assertFalse(inProgress.canTransitionTo(TicketStatus.CALLED));
        assertFalse(inProgress.canTransitionTo(TicketStatus.NO_SHOW));
        assertFalse(inProgress.canTransitionTo(TicketStatus.IN_PROGRESS));
    }

    @ParameterizedTest
    @EnumSource(names = {"COMPLETED", "CANCELLED", "NO_SHOW"})
    @DisplayName("Should not allow transitions from final states")
    void shouldNotAllowTransitionsFromFinalStates(TicketStatus finalStatus) {
        // Given all possible target states
        TicketStatus[] allStates = TicketStatus.values();
        
        // Then - No transitions should be allowed from final states
        for (TicketStatus targetStatus : allStates) {
            assertFalse(finalStatus.canTransitionTo(targetStatus),
                String.format("Should not allow transition from %s to %s", finalStatus, targetStatus));
        }
    }

    @Test
    @DisplayName("Should identify final states correctly")
    void shouldIdentifyFinalStatesCorrectly() {
        // Final states
        assertTrue(TicketStatus.COMPLETED.isFinalState());
        assertTrue(TicketStatus.CANCELLED.isFinalState());
        assertTrue(TicketStatus.NO_SHOW.isFinalState());
        
        // Non-final states
        assertFalse(TicketStatus.PENDING.isFinalState());
        assertFalse(TicketStatus.CALLED.isFinalState());
        assertFalse(TicketStatus.IN_PROGRESS.isFinalState());
    }

    @Test
    @DisplayName("Should identify active states correctly")
    void shouldIdentifyActiveStatesCorrectly() {
        // Active states
        assertTrue(TicketStatus.PENDING.isActive());
        assertTrue(TicketStatus.CALLED.isActive());
        assertTrue(TicketStatus.IN_PROGRESS.isActive());
        
        // Inactive states
        assertFalse(TicketStatus.COMPLETED.isActive());
        assertFalse(TicketStatus.CANCELLED.isActive());
        assertFalse(TicketStatus.NO_SHOW.isActive());
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        // Given
        TicketStatus[] expectedValues = {
            TicketStatus.PENDING,
            TicketStatus.CALLED,
            TicketStatus.IN_PROGRESS,
            TicketStatus.COMPLETED,
            TicketStatus.CANCELLED,
            TicketStatus.NO_SHOW
        };
        
        // When
        TicketStatus[] actualValues = TicketStatus.values();
        
        // Then
        assertEquals(6, actualValues.length);
        assertArrayEquals(expectedValues, actualValues);
    }

    @Test
    @DisplayName("Should maintain enum order for business logic")
    void shouldMaintainEnumOrderForBusinessLogic() {
        // Given - Expected order based on typical workflow
        TicketStatus[] values = TicketStatus.values();
        
        // Then - Verify logical order
        assertEquals(TicketStatus.PENDING, values[0]);
        assertEquals(TicketStatus.CALLED, values[1]);
        assertEquals(TicketStatus.IN_PROGRESS, values[2]);
        assertEquals(TicketStatus.COMPLETED, values[3]);
        assertEquals(TicketStatus.CANCELLED, values[4]);
        assertEquals(TicketStatus.NO_SHOW, values[5]);
    }

    @Test
    @DisplayName("Should handle enum valueOf correctly")
    void shouldHandleEnumValueOfCorrectly() {
        // Given
        String statusName = "PENDING";
        
        // When
        TicketStatus status = TicketStatus.valueOf(statusName);
        
        // Then
        assertEquals(TicketStatus.PENDING, status);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        String invalidStatusName = "INVALID_STATUS";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> TicketStatus.valueOf(invalidStatusName));
    }
}