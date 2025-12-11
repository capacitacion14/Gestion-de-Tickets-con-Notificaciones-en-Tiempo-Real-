package com.banco.ticketero.application.usecase.queue;

import com.banco.ticketero.application.dto.response.QueueStatusResponse;
import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import com.banco.ticketero.domain.repository.QueueRepository;
import com.banco.ticketero.domain.repository.TicketRepository;

import java.util.List;

/**
 * Caso de uso para obtener el estado actual de las colas.
 */
public class GetQueueStatusUseCase {
    
    private final QueueRepository queueRepository;
    private final TicketRepository ticketRepository;
    
    public GetQueueStatusUseCase(QueueRepository queueRepository, TicketRepository ticketRepository) {
        this.queueRepository = queueRepository;
        this.ticketRepository = ticketRepository;
    }
    
    /**
     * Obtiene el estado de una cola específica.
     */
    public QueueStatusResponse execute(String queueTypeStr) {
        QueueType queueType = parseQueueType(queueTypeStr);
        
        Queue queue = queueRepository.findByQueueType(queueType)
                .orElseThrow(() -> new com.banco.ticketero.domain.exception.QueueNotFoundException(queueType));
        
        List<Ticket> pendingTickets = ticketRepository.findPendingTicketsByQueueTypeOrderedByPosition(queueType);
        
        return toResponse(queue, pendingTickets);
    }
    
    /**
     * Obtiene el estado de todas las colas activas.
     */
    public List<QueueStatusResponse> executeAll() {
        List<Queue> activeQueues = queueRepository.findActiveQueuesOrderedByPriority();
        
        return activeQueues.stream()
                .map(queue -> {
                    List<Ticket> pendingTickets = ticketRepository
                            .findPendingTicketsByQueueTypeOrderedByPosition(queue.getQueueType());
                    return toResponse(queue, pendingTickets);
                })
                .toList();
    }
    
    private QueueType parseQueueType(String queueTypeStr) {
        try {
            return QueueType.valueOf(queueTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid queue type: " + queueTypeStr);
        }
    }
    
    private QueueStatusResponse toResponse(Queue queue, List<Ticket> pendingTickets) {
        List<QueueStatusResponse.TicketSummary> ticketSummaries = pendingTickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING)
                .map(ticket -> new QueueStatusResponse.TicketSummary(
                        ticket.getTicketCode().getValue(),
                        ticket.getPositionInQueue() != null ? ticket.getPositionInQueue() : 0,
                        ticket.getEstimatedWaitTime() != null ? ticket.getEstimatedWaitTime() : 0
                ))
                .toList();
        
        return new QueueStatusResponse(
                queue.getQueueType().name(),
                pendingTickets.size(),
                queue.getMaxCapacity(),
                queue.getEstimatedTimeMinutes(),
                queue.isActive(),
                ticketSummaries
        );
    }
}