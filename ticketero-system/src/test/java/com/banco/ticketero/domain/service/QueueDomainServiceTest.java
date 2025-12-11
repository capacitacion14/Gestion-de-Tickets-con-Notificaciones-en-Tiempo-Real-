package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueueDomainService Tests")
class QueueDomainServiceTest {

    private QueueDomainService queueDomainService;

    @BeforeEach
    void setUp() {
        queueDomainService = new QueueDomainService();
    }

    @Test
    @DisplayName("Should calculate queue position for first ticket")
    void shouldCalculateQueuePositionForFirstTicket() {
        // Given
        List<Ticket> existingTickets = List.of();
        
        // When
        int position = queueDomainService.calculateQueuePosition(existingTickets, QueueType.GENERAL);
        
        // Then
        assertEquals(1, position);
    }

    @Test
    @DisplayName("Should calculate queue position after existing tickets")
    void shouldCalculateQueuePositionAfterExistingTickets() {
        // Given
        Ticket ticket1 = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(1, 20);
        Ticket ticket2 = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1002))
                .withPosition(2, 40);
        List<Ticket> existingTickets = List.of(ticket1, ticket2);
        
        // When
        int position = queueDomainService.calculateQueuePosition(existingTickets, QueueType.GENERAL);
        
        // Then
        assertEquals(3, position);
    }

    @Test
    @DisplayName("Should ignore non-pending tickets when calculating position")
    void shouldIgnoreNonPendingTicketsWhenCalculatingPosition() {
        // Given
        Ticket pendingTicket = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(1, 20);
        Ticket calledTicket = pendingTicket.call();
        List<Ticket> existingTickets = List.of(pendingTicket, calledTicket);
        
        // When
        int position = queueDomainService.calculateQueuePosition(existingTickets, QueueType.GENERAL);
        
        // Then
        assertEquals(2, position); // Only counts pending ticket
    }

    @Test
    @DisplayName("Should calculate estimated wait time")
    void shouldCalculateEstimatedWaitTime() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL, 50, 15);
        int position = 3;
        
        // When
        int waitTime = queueDomainService.calculateEstimatedWaitTime(queue, position);
        
        // Then
        assertEquals(45, waitTime); // 3 * 15 minutes
    }

    @Test
    @DisplayName("Should return zero wait time for invalid inputs")
    void shouldReturnZeroWaitTimeForInvalidInputs() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL);
        
        // When & Then
        assertEquals(0, queueDomainService.calculateEstimatedWaitTime(null, 1));
        assertEquals(0, queueDomainService.calculateEstimatedWaitTime(queue, 0));
        assertEquals(0, queueDomainService.calculateEstimatedWaitTime(queue, -1));
    }

    @Test
    @DisplayName("Should verify if queue can accept new ticket")
    void shouldVerifyIfQueueCanAcceptNewTicket() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL, 10, 15);
        
        // When & Then
        assertTrue(queueDomainService.canAcceptNewTicket(queue, 5)); // Under capacity
        assertFalse(queueDomainService.canAcceptNewTicket(queue, 10)); // At capacity
        assertFalse(queueDomainService.canAcceptNewTicket(queue, 15)); // Over capacity
        assertFalse(queueDomainService.canAcceptNewTicket(null, 5)); // Null queue
    }

    @Test
    @DisplayName("Should determine optimal queue for VIP")
    void shouldDetermineOptimalQueueForVip() {
        // When
        QueueType queueType = queueDomainService.determineOptimalQueueForVip();
        
        // Then
        assertEquals(QueueType.VIP, queueType);
    }

    @Test
    @DisplayName("Should determine optimal queue for regular customer")
    void shouldDetermineOptimalQueueForRegular() {
        // When
        QueueType queueType = queueDomainService.determineOptimalQueueForRegular();
        
        // Then
        assertEquals(QueueType.GENERAL, queueType);
    }

    @Test
    @DisplayName("Should compare queue priorities correctly")
    void shouldCompareQueuePrioritiesCorrectly() {
        // When & Then
        assertTrue(queueDomainService.hasHigherPriority(QueueType.VIP, QueueType.GENERAL));
        assertTrue(queueDomainService.hasHigherPriority(QueueType.BUSINESS, QueueType.PRIORITY));
        assertFalse(queueDomainService.hasHigherPriority(QueueType.GENERAL, QueueType.VIP));
        assertFalse(queueDomainService.hasHigherPriority(null, QueueType.VIP));
        assertFalse(queueDomainService.hasHigherPriority(QueueType.VIP, null));
    }

    @Test
    @DisplayName("Should calculate total available capacity")
    void shouldCalculateTotalAvailableCapacity() {
        // Given
        Queue vipQueue = Queue.create(QueueType.VIP, 10, 5);
        Queue generalQueue = Queue.create(QueueType.GENERAL, 50, 20);
        List<Queue> activeQueues = List.of(vipQueue, generalQueue);
        
        Ticket activeTicket1 = Ticket.create(CustomerId.generate(), QueueType.VIP, TicketCode.fromSequence(1001));
        Ticket activeTicket2 = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1002));
        Ticket completedTicket = activeTicket1.call().startProgress().complete();
        List<Ticket> tickets = List.of(activeTicket1, activeTicket2, completedTicket);
        
        // When
        int availableCapacity = queueDomainService.calculateTotalAvailableCapacity(activeQueues, tickets);
        
        // Then
        assertEquals(58, availableCapacity); // 60 total - 2 active tickets
    }

    @Test
    @DisplayName("Should identify system at critical capacity")
    void shouldIdentifySystemAtCriticalCapacity() {
        // Given
        Queue smallQueue = Queue.create(QueueType.GENERAL, 10, 20);
        List<Queue> activeQueues = List.of(smallQueue);
        
        // Create 9 active tickets (90% capacity)
        List<Ticket> tickets = List.of(
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1001)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1002)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1003)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1004)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1005)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1006)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1007)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1008)),
            Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1009))
        );
        
        // When & Then
        assertTrue(queueDomainService.isSystemAtCriticalCapacity(activeQueues, tickets));
        
        // Test with fewer tickets (80% capacity)
        List<Ticket> fewerTickets = tickets.subList(0, 8);
        assertFalse(queueDomainService.isSystemAtCriticalCapacity(activeQueues, fewerTickets));
    }

    @Test
    @DisplayName("Should handle empty queues for capacity calculations")
    void shouldHandleEmptyQueuesForCapacityCalculations() {
        // Given
        List<Queue> emptyQueues = List.of();
        List<Ticket> tickets = List.of();
        
        // When & Then
        assertEquals(0, queueDomainService.calculateTotalAvailableCapacity(emptyQueues, tickets));
        assertFalse(queueDomainService.isSystemAtCriticalCapacity(emptyQueues, tickets));
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // When & Then
        assertEquals(1, queueDomainService.calculateQueuePosition(null, QueueType.GENERAL));
        assertEquals(0, queueDomainService.calculateTotalAvailableCapacity(null, null));
        assertFalse(queueDomainService.isSystemAtCriticalCapacity(null, null));
        
        // Reorganize should not throw with null inputs
        assertDoesNotThrow(() -> queueDomainService.reorganizeQueuePositions(null, QueueType.GENERAL));
    }

    @Test
    @DisplayName("Should filter tickets by queue type when calculating position")
    void shouldFilterTicketsByQueueTypeWhenCalculatingPosition() {
        // Given
        Ticket vipTicket = Ticket.create(CustomerId.generate(), QueueType.VIP, TicketCode.fromSequence(1001))
                .withPosition(1, 5);
        Ticket generalTicket = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1002))
                .withPosition(1, 20);
        List<Ticket> existingTickets = List.of(vipTicket, generalTicket);
        
        // When
        int vipPosition = queueDomainService.calculateQueuePosition(existingTickets, QueueType.VIP);
        int generalPosition = queueDomainService.calculateQueuePosition(existingTickets, QueueType.GENERAL);
        
        // Then
        assertEquals(2, vipPosition); // Next position in VIP queue
        assertEquals(2, generalPosition); // Next position in GENERAL queue
    }
}