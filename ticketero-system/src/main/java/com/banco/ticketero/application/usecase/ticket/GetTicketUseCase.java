package com.banco.ticketero.application.usecase.ticket;

import com.banco.ticketero.application.dto.response.TicketResponse;
import com.banco.ticketero.domain.exception.TicketNotFoundException;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.repository.TicketRepository;

/**
 * Caso de uso para obtener información de un ticket.
 */
public class GetTicketUseCase {
    
    private final TicketRepository ticketRepository;
    
    public GetTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    /**
     * Obtiene un ticket por su ID.
     */
    public TicketResponse executeById(String ticketIdStr) {
        TicketId ticketId = TicketId.of(ticketIdStr);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        return toResponse(ticket);
    }
    
    /**
     * Obtiene un ticket por su código.
     */
    public TicketResponse executeByCode(String ticketCodeStr) {
        TicketCode ticketCode = TicketCode.of(ticketCodeStr);
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException(ticketCode));
        
        return toResponse(ticket);
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