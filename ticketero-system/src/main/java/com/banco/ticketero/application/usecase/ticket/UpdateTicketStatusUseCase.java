package com.banco.ticketero.application.usecase.ticket;

import com.banco.ticketero.application.dto.request.UpdateTicketStatusRequest;
import com.banco.ticketero.application.dto.response.TicketResponse;
import com.banco.ticketero.domain.exception.InvalidTicketStatusException;
import com.banco.ticketero.domain.exception.TicketNotFoundException;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import com.banco.ticketero.domain.repository.TicketRepository;

/**
 * Caso de uso para actualizar el estado de un ticket.
 */
public class UpdateTicketStatusUseCase {
    
    private final TicketRepository ticketRepository;
    
    public UpdateTicketStatusUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    /**
     * Actualiza el estado de un ticket.
     */
    public TicketResponse execute(String ticketIdStr, UpdateTicketStatusRequest request) {
        // 1. Buscar ticket
        TicketId ticketId = TicketId.of(ticketIdStr);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        // 2. Parsear nuevo estado
        TicketStatus newStatus = parseTicketStatus(request.newStatus());
        
        // 3. Aplicar transición de estado
        Ticket updatedTicket = applyStatusTransition(ticket, newStatus);
        
        // 4. Guardar cambios
        Ticket savedTicket = ticketRepository.save(updatedTicket);
        
        // 5. Retornar respuesta
        return toResponse(savedTicket);
    }
    
    private TicketStatus parseTicketStatus(String statusStr) {
        try {
            return TicketStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ticket status: " + statusStr);
        }
    }
    
    private Ticket applyStatusTransition(Ticket ticket, TicketStatus newStatus) {
        return switch (newStatus) {
            case CALLED -> {
                if (!ticket.getStatus().canTransitionTo(TicketStatus.CALLED)) {
                    throw new InvalidTicketStatusException(ticket.getStatus(), newStatus);
                }
                yield ticket.call();
            }
            case IN_PROGRESS -> {
                if (!ticket.getStatus().canTransitionTo(TicketStatus.IN_PROGRESS)) {
                    throw new InvalidTicketStatusException(ticket.getStatus(), newStatus);
                }
                yield ticket.startProgress();
            }
            case COMPLETED -> {
                if (!ticket.getStatus().canTransitionTo(TicketStatus.COMPLETED)) {
                    throw new InvalidTicketStatusException(ticket.getStatus(), newStatus);
                }
                yield ticket.complete();
            }
            case CANCELLED -> {
                if (!ticket.getStatus().canTransitionTo(TicketStatus.CANCELLED)) {
                    throw new InvalidTicketStatusException(ticket.getStatus(), newStatus);
                }
                yield ticket.cancel();
            }
            case NO_SHOW -> {
                if (!ticket.getStatus().canTransitionTo(TicketStatus.NO_SHOW)) {
                    throw new InvalidTicketStatusException(ticket.getStatus(), newStatus);
                }
                yield ticket.markAsNoShow();
            }
            default -> throw new InvalidTicketStatusException(ticket.getStatus(), 
                    "transition to " + newStatus);
        };
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