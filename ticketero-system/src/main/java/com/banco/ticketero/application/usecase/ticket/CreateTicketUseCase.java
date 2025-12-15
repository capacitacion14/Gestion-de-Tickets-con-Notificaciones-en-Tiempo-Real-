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
import com.banco.ticketero.domain.repository.CustomerRepository;
import com.banco.ticketero.domain.repository.QueueRepository;
import com.banco.ticketero.domain.repository.TicketRepository;
import com.banco.ticketero.domain.service.QueueDomainService;
import com.banco.ticketero.domain.service.TicketDomainService;

import java.util.List;

/**
 * Caso de uso para crear un nuevo ticket.
 * Orquesta la lógica de creación sin contener lógica de negocio.
 */
public class CreateTicketUseCase {
    
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final QueueRepository queueRepository;
    private final TicketDomainService ticketDomainService;
    private final QueueDomainService queueDomainService;
    
    public CreateTicketUseCase(TicketRepository ticketRepository,
                              CustomerRepository customerRepository,
                              QueueRepository queueRepository,
                              TicketDomainService ticketDomainService,
                              QueueDomainService queueDomainService) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.queueRepository = queueRepository;
        this.ticketDomainService = ticketDomainService;
        this.queueDomainService = queueDomainService;
    }
    
    /**
     * Ejecuta el caso de uso de creación de ticket.
     */
    public TicketResponse execute(CreateTicketRequest request) {
        // 1. Buscar o crear cliente
        Customer customer = findOrCreateCustomer(request.nationalId());
        
        // 2. Determinar tipo de cola apropiado
        QueueType queueType = parseQueueType(request.queueType());
        Queue queue = findQueue(queueType);
        
        // 3. Verificar capacidad de la cola
        List<Ticket> existingTickets = ticketRepository.findByQueueTypeAndStatus(queueType, 
                com.banco.ticketero.domain.model.ticket.TicketStatus.PENDING);
        
        if (!queueDomainService.canAcceptNewTicket(queue, existingTickets.size())) {
            throw new QueueFullException(queueType, existingTickets.size(), queue.getMaxCapacity());
        }
        
        // 4. Generar código de ticket
        List<TicketCode> existingCodes = existingTickets.stream()
                .map(Ticket::getTicketCode)
                .toList();
        TicketCode ticketCode = ticketDomainService.generateNextTicketCode(existingCodes);
        
        // 5. Crear ticket
        Ticket ticket = Ticket.create(customer.getId(), queueType, ticketCode);
        
        // 6. Asignar posición en cola
        int position = queueDomainService.calculateQueuePosition(existingTickets, queueType);
        int estimatedWaitTime = queueDomainService.calculateEstimatedWaitTime(queue, position);
        ticket = ticket.withPosition(position, estimatedWaitTime);
        
        // 7. Guardar ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // 8. Convertir a DTO de respuesta
        return toResponse(savedTicket);
    }
    
    private Customer findOrCreateCustomer(String nationalIdStr) {
        NationalId nationalId = NationalId.of(nationalIdStr);
        return customerRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new CustomerNotFoundException(nationalId));
    }
    
    private QueueType parseQueueType(String queueTypeStr) {
        try {
            return QueueType.valueOf(queueTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid queue type: " + queueTypeStr);
        }
    }
    
    private Queue findQueue(QueueType queueType) {
        return queueRepository.findByQueueType(queueType)
                .orElseThrow(() -> new com.banco.ticketero.domain.exception.QueueNotFoundException(queueType));
    }
    
    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId().toString(),
                ticket.getTicketCode().getValue(),
                ticket.getCustomerId().toString(),
                ticket.getQueueType().name(),
                ticket.getStatus().name(),
                ticket.getPositionInQueue(),
                ticket.getEstimatedWaitTime(),
                ticket.getCalledAt(),
                ticket.getCompletedAt(),
                ticket.getCancelledAt(),
                ticket.getCreatedAt()
        );
    }
}