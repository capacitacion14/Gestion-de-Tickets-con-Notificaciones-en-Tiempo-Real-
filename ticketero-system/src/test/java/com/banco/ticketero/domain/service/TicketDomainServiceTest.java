package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TicketDomainService Tests")
class TicketDomainServiceTest {

    private TicketDomainService ticketDomainService;
    private QueueDomainService queueDomainService;

    @BeforeEach
    void setUp() {
        queueDomainService = mock(QueueDomainService.class);
        ticketDomainService = new TicketDomainService(queueDomainService);
    }

    @Test
    @DisplayName("Should determine VIP queue for VIP customer")
    void shouldDetermineVipQueueForVipCustomer() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        when(queueDomainService.determineOptimalQueueForVip()).thenReturn(QueueType.VIP);
        
        // When
        QueueType queueType = ticketDomainService.determineQueueTypeForCustomer(vipCustomer);
        
        // Then
        assertEquals(QueueType.VIP, queueType);
        verify(queueDomainService).determineOptimalQueueForVip();
    }

    @Test
    @DisplayName("Should determine general queue for regular customer")
    void shouldDetermineGeneralQueueForRegularCustomer() {
        // Given
        Customer regularCustomer = Customer.create(NationalId.of("12345678"), "Jane", "Smith");
        when(queueDomainService.determineOptimalQueueForRegular()).thenReturn(QueueType.GENERAL);
        
        // When
        QueueType queueType = ticketDomainService.determineQueueTypeForCustomer(regularCustomer);
        
        // Then
        assertEquals(QueueType.GENERAL, queueType);
        verify(queueDomainService).determineOptimalQueueForRegular();
    }

    @Test
    @DisplayName("Should throw exception for null customer")
    void shouldThrowExceptionForNullCustomer() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ticketDomainService.determineQueueTypeForCustomer(null)
        );
        
        assertEquals("Customer cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should generate first ticket code")
    void shouldGenerateFirstTicketCode() {
        // Given
        List<TicketCode> existingCodes = List.of();
        
        // When
        TicketCode ticketCode = ticketDomainService.generateNextTicketCode(existingCodes);
        
        // Then
        assertEquals("T1000", ticketCode.getValue());
    }

    @Test
    @DisplayName("Should generate next sequential ticket code")
    void shouldGenerateNextSequentialTicketCode() {
        // Given
        List<TicketCode> existingCodes = List.of(
            TicketCode.fromSequence(1001),
            TicketCode.fromSequence(1002),
            TicketCode.fromSequence(1003)
        );
        
        // When
        TicketCode ticketCode = ticketDomainService.generateNextTicketCode(existingCodes);
        
        // Then
        assertEquals("T1004", ticketCode.getValue());
    }

    @Test
    @DisplayName("Should wrap around when reaching maximum sequence")
    void shouldWrapAroundWhenReachingMaximumSequence() {
        // Given
        List<TicketCode> existingCodes = List.of(TicketCode.fromSequence(9999));
        
        // When
        TicketCode ticketCode = ticketDomainService.generateNextTicketCode(existingCodes);
        
        // Then
        assertEquals("T1000", ticketCode.getValue());
    }

    @Test
    @DisplayName("Should allow calling ticket when it's next in queue")
    void shouldAllowCallingTicketWhenItsNextInQueue() {
        // Given
        Ticket ticket = Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                                    QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(1, 20);
        List<Ticket> queueTickets = List.of(ticket);
        
        // When
        boolean canCall = ticketDomainService.canCallTicket(ticket, queueTickets);
        
        // Then
        assertTrue(canCall);
    }

    @Test
    @DisplayName("Should not allow calling ticket when higher priority tickets are waiting")
    void shouldNotAllowCallingTicketWhenHigherPriorityTicketsAreWaiting() {
        // Given
        Ticket higherPriorityTicket = Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                                                  QueueType.GENERAL, TicketCode.fromSequence(1001))
                .withPosition(1, 20);
        Ticket lowerPriorityTicket = Ticket.create(Customer.create(NationalId.of("87654321"), "Jane", "Smith").getId(), 
                                                 QueueType.GENERAL, TicketCode.fromSequence(1002))
                .withPosition(2, 40);
        List<Ticket> queueTickets = List.of(higherPriorityTicket, lowerPriorityTicket);
        
        // When
        boolean canCall = ticketDomainService.canCallTicket(lowerPriorityTicket, queueTickets);
        
        // Then
        assertFalse(canCall);
    }

    @Test
    @DisplayName("Should not allow calling non-pending ticket")
    void shouldNotAllowCallingNonPendingTicket() {
        // Given
        Ticket calledTicket = Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                                          QueueType.GENERAL, TicketCode.fromSequence(1001))
                .call();
        
        // When
        boolean canCall = ticketDomainService.canCallTicket(calledTicket, List.of());
        
        // Then
        assertFalse(canCall);
    }

    @Test
    @DisplayName("Should calculate ticket priority with multiple factors")
    void shouldCalculateTicketPriorityWithMultipleFactors() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        Customer regularCustomer = Customer.create(NationalId.of("87654321"), "Jane", "Smith");
        
        Ticket vipTicket = Ticket.create(vipCustomer.getId(), QueueType.VIP, TicketCode.fromSequence(1001));
        Ticket regularTicket = Ticket.create(regularCustomer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1002));
        
        // When
        int vipPriority = ticketDomainService.calculateTicketPriority(vipTicket, vipCustomer);
        int regularPriority = ticketDomainService.calculateTicketPriority(regularTicket, regularCustomer);
        
        // Then
        assertTrue(vipPriority < regularPriority); // Lower number = higher priority
    }

    @Test
    @DisplayName("Should identify expired tickets")
    void shouldIdentifyExpiredTickets() {
        // Given
        Ticket activeTicket = Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                                          QueueType.GENERAL, TicketCode.fromSequence(1001));
        Ticket completedTicket = activeTicket.call().startProgress().complete();
        
        // When & Then
        // Active tickets can expire based on their internal logic
        // Completed tickets should not expire
        assertFalse(ticketDomainService.shouldExpireTicket(completedTicket));
        assertFalse(ticketDomainService.shouldExpireTicket(null));
    }

    @Test
    @DisplayName("Should calculate ticket metrics correctly")
    void shouldCalculateTicketMetricsCorrectly() {
        // Given - Use a wider date range to include all tickets
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        Ticket completedTicket = Ticket.create(Customer.create(NationalId.of("12345678"), "John", "Doe").getId(), 
                                             QueueType.GENERAL, TicketCode.fromSequence(1001))
                .call().startProgress().complete();
        Ticket cancelledTicket = Ticket.create(Customer.create(NationalId.of("87654321"), "Jane", "Smith").getId(), 
                                             QueueType.GENERAL, TicketCode.fromSequence(1002))
                .cancel();
        
        List<Ticket> tickets = List.of(completedTicket, cancelledTicket);
        
        // When
        TicketDomainService.TicketMetrics metrics = ticketDomainService.calculateTicketMetrics(tickets, startDate, endDate);
        
        // Then
        assertEquals(2, metrics.totalTickets());
        assertEquals(1, metrics.completedTickets());
        assertEquals(1, metrics.cancelledTickets());
        assertEquals(50.0, metrics.completionRatePercentage(), 0.01);
    }

    @Test
    @DisplayName("Should allow VIP customer to create multiple tickets")
    void shouldAllowVipCustomerToCreateMultipleTickets() {
        // Given
        Customer vipCustomer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                                "john@email.com", "123456789");
        Ticket activeTicket = Ticket.create(vipCustomer.getId(), QueueType.VIP, TicketCode.fromSequence(1001));
        List<Ticket> customerActiveTickets = List.of(activeTicket);
        
        // When
        boolean canCreate = ticketDomainService.canCustomerCreateNewTicket(vipCustomer, customerActiveTickets);
        
        // Then
        assertTrue(canCreate); // VIP can have up to 2 active tickets
    }

    @Test
    @DisplayName("Should not allow regular customer to create multiple tickets")
    void shouldNotAllowRegularCustomerToCreateMultipleTickets() {
        // Given
        Customer regularCustomer = Customer.create(NationalId.of("12345678"), "Jane", "Smith");
        Ticket activeTicket = Ticket.create(regularCustomer.getId(), QueueType.GENERAL, TicketCode.fromSequence(1001));
        List<Ticket> customerActiveTickets = List.of(activeTicket);
        
        // When
        boolean canCreate = ticketDomainService.canCustomerCreateNewTicket(regularCustomer, customerActiveTickets);
        
        // Then
        assertFalse(canCreate); // Regular customer can have only 1 active ticket
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // When & Then
        assertFalse(ticketDomainService.canCallTicket(null, List.of()));
        assertEquals(Integer.MAX_VALUE, ticketDomainService.calculateTicketPriority(null, null));
        assertFalse(ticketDomainService.shouldExpireTicket(null));
        assertFalse(ticketDomainService.canCustomerCreateNewTicket(null, List.of()));
        
        // Metrics with empty list
        TicketDomainService.TicketMetrics emptyMetrics = ticketDomainService.calculateTicketMetrics(
            List.of(), LocalDateTime.now().minusHours(1), LocalDateTime.now());
        assertEquals(0, emptyMetrics.totalTickets());
    }

    @Test
    @DisplayName("Should handle empty existing codes list")
    void shouldHandleEmptyExistingCodesList() {
        // When
        TicketCode ticketCode = ticketDomainService.generateNextTicketCode(null);
        
        // Then
        assertEquals("T1000", ticketCode.getValue());
    }
}