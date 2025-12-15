package com.banco.ticketero.application.usecase.ticket;

import com.banco.ticketero.application.dto.request.CreateTicketRequest;
import com.banco.ticketero.application.dto.response.TicketResponse;
import com.banco.ticketero.domain.exception.CustomerNotFoundException;
import com.banco.ticketero.domain.exception.QueueFullException;
import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import com.banco.ticketero.domain.repository.CustomerRepository;
import com.banco.ticketero.domain.repository.QueueRepository;
import com.banco.ticketero.domain.repository.TicketRepository;
import com.banco.ticketero.domain.service.QueueDomainService;
import com.banco.ticketero.domain.service.TicketDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("CreateTicketUseCase Tests")
class CreateTicketUseCaseTest {

    private CreateTicketUseCase createTicketUseCase;
    private TicketRepository ticketRepository;
    private CustomerRepository customerRepository;
    private QueueRepository queueRepository;
    private TicketDomainService ticketDomainService;
    private QueueDomainService queueDomainService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        customerRepository = mock(CustomerRepository.class);
        queueRepository = mock(QueueRepository.class);
        ticketDomainService = mock(TicketDomainService.class);
        queueDomainService = mock(QueueDomainService.class);
        
        createTicketUseCase = new CreateTicketUseCase(
                ticketRepository, customerRepository, queueRepository,
                ticketDomainService, queueDomainService
        );
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicketSuccessfully() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "GENERAL");
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Queue queue = Queue.create(QueueType.GENERAL);
        TicketCode ticketCode = TicketCode.fromSequence(1001);
        Ticket ticket = Ticket.create(customer.getId(), QueueType.GENERAL, ticketCode);
        Ticket ticketWithPosition = ticket.withPosition(1, 20);
        
        // Mock setup
        when(customerRepository.findByNationalId(any(NationalId.class))).thenReturn(Optional.of(customer));
        when(queueRepository.findByQueueType(QueueType.GENERAL)).thenReturn(Optional.of(queue));
        when(ticketRepository.findByQueueTypeAndStatus(QueueType.GENERAL, TicketStatus.PENDING))
                .thenReturn(List.of());
        when(queueDomainService.canAcceptNewTicket(queue, 0)).thenReturn(true);
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(ticketDomainService.generateNextTicketCode(anyList())).thenReturn(ticketCode);
        when(queueDomainService.calculateQueuePosition(anyList(), eq(QueueType.GENERAL))).thenReturn(1);
        when(queueDomainService.calculateEstimatedWaitTime(queue, 1)).thenReturn(20);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketWithPosition);
        
        // When
        TicketResponse response = createTicketUseCase.execute(request);
        
        // Then
        assertNotNull(response);
        assertEquals("T1001", response.ticketCode());
        assertEquals("GENERAL", response.queueType());
        assertEquals("PENDING", response.status());
        assertEquals(1, response.positionInQueue());
        assertEquals(20, response.estimatedWaitTime());
        
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "GENERAL");
        when(customerRepository.findByNationalId(any(NationalId.class))).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> createTicketUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw exception when queue is full")
    void shouldThrowExceptionWhenQueueIsFull() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "GENERAL");
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Queue queue = Queue.create(QueueType.GENERAL, 1, 20); // Max capacity 1
        
        when(customerRepository.findByNationalId(any(NationalId.class))).thenReturn(Optional.of(customer));
        when(queueRepository.findByQueueType(QueueType.GENERAL)).thenReturn(Optional.of(queue));
        when(ticketRepository.findByQueueTypeAndStatus(QueueType.GENERAL, TicketStatus.PENDING))
                .thenReturn(List.of(mock(Ticket.class))); // 1 existing ticket
        when(queueDomainService.canAcceptNewTicket(queue, 1)).thenReturn(false);
        
        // When & Then
        assertThrows(QueueFullException.class, () -> createTicketUseCase.execute(request));
    }

    @Test
    @DisplayName("Should throw exception for invalid queue type")
    void shouldThrowExceptionForInvalidQueueType() {
        // When & Then - The validation happens in CreateTicketRequest constructor
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CreateTicketRequest("12345678", "INVALID_QUEUE")
        );
        
        assertTrue(exception.getMessage().contains("Invalid queue type"));
    }

    @Test
    @DisplayName("Should handle empty existing tickets list")
    void shouldHandleEmptyExistingTicketsList() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "GENERAL");
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        Queue queue = Queue.create(QueueType.GENERAL);
        TicketCode ticketCode = TicketCode.fromSequence(1000);
        Ticket ticket = Ticket.create(customer.getId(), QueueType.GENERAL, ticketCode);
        Ticket ticketWithPosition = ticket.withPosition(1, 20);
        
        // Mock setup for empty lists
        when(customerRepository.findByNationalId(any(NationalId.class))).thenReturn(Optional.of(customer));
        when(queueRepository.findByQueueType(QueueType.GENERAL)).thenReturn(Optional.of(queue));
        when(ticketRepository.findByQueueTypeAndStatus(QueueType.GENERAL, TicketStatus.PENDING))
                .thenReturn(List.of());
        when(queueDomainService.canAcceptNewTicket(queue, 0)).thenReturn(true);
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(ticketDomainService.generateNextTicketCode(List.of())).thenReturn(ticketCode);
        when(queueDomainService.calculateQueuePosition(List.of(), QueueType.GENERAL)).thenReturn(1);
        when(queueDomainService.calculateEstimatedWaitTime(queue, 1)).thenReturn(20);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketWithPosition);
        
        // When
        TicketResponse response = createTicketUseCase.execute(request);
        
        // Then
        assertNotNull(response);
        assertEquals("T1000", response.ticketCode());
        assertEquals(1, response.positionInQueue());
    }

    @Test
    @DisplayName("Should verify all dependencies are called correctly")
    void shouldVerifyAllDependenciesAreCalledCorrectly() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678", "VIP");
        Customer customer = Customer.createVip(NationalId.of("12345678"), "John", "Doe", 
                                             "john@email.com", "123456789");
        Queue queue = Queue.create(QueueType.VIP);
        TicketCode ticketCode = TicketCode.fromSequence(1001);
        Ticket ticket = Ticket.create(customer.getId(), QueueType.VIP, ticketCode);
        Ticket ticketWithPosition = ticket.withPosition(1, 5);
        
        // Mock setup
        when(customerRepository.findByNationalId(any(NationalId.class))).thenReturn(Optional.of(customer));
        when(queueRepository.findByQueueType(QueueType.VIP)).thenReturn(Optional.of(queue));
        when(ticketRepository.findByQueueTypeAndStatus(QueueType.VIP, TicketStatus.PENDING))
                .thenReturn(List.of());
        when(queueDomainService.canAcceptNewTicket(queue, 0)).thenReturn(true);
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(ticketDomainService.generateNextTicketCode(anyList())).thenReturn(ticketCode);
        when(queueDomainService.calculateQueuePosition(anyList(), eq(QueueType.VIP))).thenReturn(1);
        when(queueDomainService.calculateEstimatedWaitTime(queue, 1)).thenReturn(5);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketWithPosition);
        
        // When
        TicketResponse response = createTicketUseCase.execute(request);
        
        // Then
        assertNotNull(response);
        
        // Verify all dependencies were called
        verify(customerRepository).findByNationalId(any(NationalId.class));
        verify(queueRepository).findByQueueType(QueueType.VIP);
        verify(ticketRepository).findByQueueTypeAndStatus(QueueType.VIP, TicketStatus.PENDING);
        verify(queueDomainService).canAcceptNewTicket(queue, 0);
        // Note: findAll() is not called in current implementation, using findByQueueTypeAndStatus instead
        verify(ticketDomainService).generateNextTicketCode(anyList());
        verify(queueDomainService).calculateQueuePosition(anyList(), eq(QueueType.VIP));
        verify(queueDomainService).calculateEstimatedWaitTime(queue, 1);
        verify(ticketRepository).save(any(Ticket.class));
    }
}