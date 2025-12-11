package com.banco.ticketero.domain.model.ticket;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.QueueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ticket Aggregate Root Tests")
class TicketTest {

    private final CustomerId validCustomerId = CustomerId.generate();
    private final QueueType validQueueType = QueueType.GENERAL;
    private final TicketCode validTicketCode = TicketCode.fromSequence(1001);

    @Test
    @DisplayName("Should create ticket with required information")
    void shouldCreateTicketWithRequiredInformation() {
        // When
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // Then
        assertNotNull(ticket);
        assertNotNull(ticket.getId());
        assertEquals(validCustomerId, ticket.getCustomerId());
        assertEquals(validQueueType, ticket.getQueueType());
        assertEquals(validTicketCode, ticket.getTicketCode());
        assertEquals(TicketStatus.PENDING, ticket.getStatus());
        assertNotNull(ticket.getCreatedAt());
        assertNull(ticket.getPositionInQueue());
        assertNull(ticket.getEstimatedWaitTime());
        assertNull(ticket.getCalledAt());
        assertNull(ticket.getCompletedAt());
        assertNull(ticket.getCancelledAt());
    }

    @Test
    @DisplayName("Should throw exception when customer ID is null")
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Ticket.create(null, validQueueType, validTicketCode)
        );
        
        assertEquals("Customer ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when queue type is null")
    void shouldThrowExceptionWhenQueueTypeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Ticket.create(validCustomerId, null, validTicketCode)
        );
        
        assertEquals("Queue type is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when ticket code is null")
    void shouldThrowExceptionWhenTicketCodeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Ticket.create(validCustomerId, validQueueType, null)
        );
        
        assertEquals("Ticket code is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should assign position in queue")
    void shouldAssignPositionInQueue() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        int position = 5;
        int estimatedWait = 100;
        
        // When
        Ticket ticketWithPosition = ticket.withPosition(position, estimatedWait);
        
        // Then
        assertEquals(position, ticketWithPosition.getPositionInQueue());
        assertEquals(estimatedWait, ticketWithPosition.getEstimatedWaitTime());
        // Verify immutability
        assertNull(ticket.getPositionInQueue());
        assertNull(ticket.getEstimatedWaitTime());
    }

    @Test
    @DisplayName("Should throw exception for invalid position")
    void shouldThrowExceptionForInvalidPosition() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ticket.withPosition(0, 100)
        );
        
        assertEquals("Position must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative estimated wait time")
    void shouldThrowExceptionForNegativeEstimatedWaitTime() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ticket.withPosition(1, -1)
        );
        
        assertEquals("Estimated wait time cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should call ticket from pending status")
    void shouldCallTicketFromPendingStatus() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When
        Ticket calledTicket = ticket.call();
        
        // Then
        assertEquals(TicketStatus.CALLED, calledTicket.getStatus());
        assertNotNull(calledTicket.getCalledAt());
        assertNull(calledTicket.getPositionInQueue()); // Removed from queue
        assertNull(calledTicket.getEstimatedWaitTime());
        // Verify immutability
        assertEquals(TicketStatus.PENDING, ticket.getStatus());
        assertNull(ticket.getCalledAt());
    }

    @Test
    @DisplayName("Should start progress from called status")
    void shouldStartProgressFromCalledStatus() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket calledTicket = ticket.call();
        
        // When
        Ticket inProgressTicket = calledTicket.startProgress();
        
        // Then
        assertEquals(TicketStatus.IN_PROGRESS, inProgressTicket.getStatus());
        assertEquals(TicketStatus.CALLED, calledTicket.getStatus()); // Original unchanged
    }

    @Test
    @DisplayName("Should complete ticket from in progress status")
    void shouldCompleteTicketFromInProgressStatus() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket inProgressTicket = ticket.call().startProgress();
        
        // When
        Ticket completedTicket = inProgressTicket.complete();
        
        // Then
        assertEquals(TicketStatus.COMPLETED, completedTicket.getStatus());
        assertNotNull(completedTicket.getCompletedAt());
        assertNull(completedTicket.getPositionInQueue());
        assertNull(completedTicket.getEstimatedWaitTime());
    }

    @Test
    @DisplayName("Should cancel ticket from valid statuses")
    void shouldCancelTicketFromValidStatuses() {
        // Given
        Ticket pendingTicket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket calledTicket = pendingTicket.call();
        Ticket inProgressTicket = calledTicket.startProgress();
        
        // When & Then
        Ticket cancelledFromPending = pendingTicket.cancel();
        assertEquals(TicketStatus.CANCELLED, cancelledFromPending.getStatus());
        assertNotNull(cancelledFromPending.getCancelledAt());
        
        Ticket cancelledFromCalled = calledTicket.cancel();
        assertEquals(TicketStatus.CANCELLED, cancelledFromCalled.getStatus());
        
        Ticket cancelledFromProgress = inProgressTicket.cancel();
        assertEquals(TicketStatus.CANCELLED, cancelledFromProgress.getStatus());
    }

    @Test
    @DisplayName("Should mark ticket as no show from called status")
    void shouldMarkTicketAsNoShowFromCalledStatus() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket calledTicket = ticket.call();
        
        // When
        Ticket noShowTicket = calledTicket.markAsNoShow();
        
        // Then
        assertEquals(TicketStatus.NO_SHOW, noShowTicket.getStatus());
        assertNotNull(noShowTicket.getCancelledAt()); // Uses cancelled timestamp
        assertNull(noShowTicket.getPositionInQueue());
    }

    @Test
    @DisplayName("Should throw exception for invalid state transitions")
    void shouldThrowExceptionForInvalidStateTransitions() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket completedTicket = ticket.call().startProgress().complete();
        
        // When & Then - Cannot transition from completed state
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> completedTicket.call()
        );
        
        assertTrue(exception.getMessage().contains("Cannot call ticket in status"));
    }

    @Test
    @DisplayName("Should identify active tickets correctly")
    void shouldIdentifyActiveTicketsCorrectly() {
        // Given
        Ticket pendingTicket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket calledTicket = pendingTicket.call();
        Ticket inProgressTicket = calledTicket.startProgress();
        Ticket completedTicket = inProgressTicket.complete();
        Ticket cancelledTicket = pendingTicket.cancel();
        
        // Then
        assertTrue(pendingTicket.isActive());
        assertTrue(calledTicket.isActive());
        assertTrue(inProgressTicket.isActive());
        assertFalse(completedTicket.isActive());
        assertFalse(cancelledTicket.isActive());
    }

    @Test
    @DisplayName("Should identify finished tickets correctly")
    void shouldIdentifyFinishedTicketsCorrectly() {
        // Given
        Ticket pendingTicket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket completedTicket = pendingTicket.call().startProgress().complete();
        Ticket cancelledTicket = pendingTicket.cancel();
        
        // Then
        assertFalse(pendingTicket.isFinished());
        assertTrue(completedTicket.isFinished());
        assertTrue(cancelledTicket.isFinished());
    }

    @Test
    @DisplayName("Should identify tickets waiting in queue")
    void shouldIdentifyTicketsWaitingInQueue() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket ticketWithPosition = ticket.withPosition(3, 60);
        Ticket calledTicket = ticketWithPosition.call();
        
        // Then
        assertFalse(ticket.isWaitingInQueue()); // No position assigned
        assertTrue(ticketWithPosition.isWaitingInQueue()); // Has position and pending
        assertFalse(calledTicket.isWaitingInQueue()); // Called, no longer waiting
    }

    @Test
    @DisplayName("Should calculate service time correctly")
    void shouldCalculateServiceTimeCorrectly() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When - Ticket not called yet
        assertNull(ticket.getServiceTimeMinutes());
        
        // When - Ticket called but not completed
        Ticket calledTicket = ticket.call();
        assertNull(calledTicket.getServiceTimeMinutes());
        
        // When - Ticket completed (simulate time passage)
        Ticket completedTicket = calledTicket.startProgress().complete();
        Long serviceTime = completedTicket.getServiceTimeMinutes();
        
        // Then
        assertNotNull(serviceTime);
        assertTrue(serviceTime >= 0);
    }

    @Test
    @DisplayName("Should calculate minutes since creation")
    void shouldCalculateMinutesSinceCreation() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When
        long minutesSinceCreation = ticket.getMinutesSinceCreation();
        
        // Then
        assertTrue(minutesSinceCreation >= 0);
        assertTrue(minutesSinceCreation < 5); // Should be very recent
    }

    @Test
    @DisplayName("Should not expire finished tickets")
    void shouldNotExpireFinishedTickets() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket completedTicket = ticket.call().startProgress().complete();
        
        // Then
        assertFalse(completedTicket.isExpired()); // Finished tickets don't expire
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        TicketId originalId = ticket.getId();
        TicketStatus originalStatus = ticket.getStatus();
        
        // When - Perform operations that return new instances
        Ticket withPosition = ticket.withPosition(1, 20);
        Ticket called = ticket.call();
        Ticket cancelled = ticket.cancel();
        
        // Then - Original ticket should remain unchanged
        assertEquals(originalId, ticket.getId());
        assertEquals(originalStatus, ticket.getStatus());
        assertNull(ticket.getPositionInQueue());
        assertNull(ticket.getCalledAt());
        assertNull(ticket.getCancelledAt());
        
        // New instances should have different values
        assertNotSame(ticket, withPosition);
        assertNotSame(ticket, called);
        assertNotSame(ticket, cancelled);
    }

    @ParameterizedTest
    @EnumSource(names = {"COMPLETED", "CANCELLED", "NO_SHOW"})
    @DisplayName("Should not allow position assignment to inactive tickets")
    void shouldNotAllowPositionAssignmentToInactiveTickets(TicketStatus finalStatus) {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        Ticket finalTicket = switch (finalStatus) {
            case COMPLETED -> ticket.call().startProgress().complete();
            case CANCELLED -> ticket.cancel();
            case NO_SHOW -> ticket.call().markAsNoShow();
            default -> throw new IllegalArgumentException("Unexpected status: " + finalStatus);
        };
        
        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> finalTicket.withPosition(1, 20)
        );
        
        assertEquals("Cannot assign position to inactive ticket", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle zero estimated wait time")
    void shouldHandleZeroEstimatedWaitTime() {
        // Given
        Ticket ticket = Ticket.create(validCustomerId, validQueueType, validTicketCode);
        
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> ticket.withPosition(1, 0));
        
        Ticket ticketWithZeroWait = ticket.withPosition(1, 0);
        assertEquals(0, ticketWithZeroWait.getEstimatedWaitTime());
    }
}