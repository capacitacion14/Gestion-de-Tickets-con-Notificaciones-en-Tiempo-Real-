package com.banco.ticketero.service;

import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;
    private final OutboxMessageRepository outboxMessageRepository;

    @Transactional
    public boolean assignNextTicket() {
        List<Ticket> pendingTickets = ticketRepository.findNextTicketsToAssign();
        
        if (pendingTickets.isEmpty()) {
            log.debug("No pending tickets to assign");
            return false;
        }

        Ticket ticket = pendingTickets.get(0);
        List<Advisor> availableAdvisors = advisorRepository
            .findAvailableAdvisorsForQueue(ticket.getQueueType().name());

        if (availableAdvisors.isEmpty()) {
            log.debug("No available advisors for queue: {}", ticket.getQueueType());
            return false;
        }

        Advisor advisor = availableAdvisors.get(0);
        
        ticket.setStatus(TicketStatus.ATENDIENDO);
        ticket.setAssignedAdvisor(advisor);
        ticket.setAssignedModuleNumber(advisor.getModuleNumber());
        
        advisor.setStatus(Advisor.AdvisorStatus.BUSY);
        advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() + 1);
        advisor.setLastAssignmentAt(LocalDateTime.now());

        ticketRepository.save(ticket);
        advisorRepository.save(advisor);

        log.info("Ticket {} assigned to advisor {}", ticket.getNumero(), advisor.getName());
        return true;
    }

    @Transactional
    public void completeTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() == TicketStatus.COMPLETADO) {
            log.debug("Ticket {} already completed", ticket.getNumero());
            return;
        }

        ticket.setStatus(TicketStatus.COMPLETADO);
        ticket.setCompletedAt(LocalDateTime.now());

        if (ticket.getAssignedAdvisor() != null) {
            Advisor advisor = ticket.getAssignedAdvisor();
            advisor.setStatus(Advisor.AdvisorStatus.AVAILABLE);
            advisorRepository.save(advisor);
        }

        ticketRepository.save(ticket);
        log.info("Ticket {} completed", ticket.getNumero());
    }

    public java.util.Optional<Advisor> findAvailableAdvisor(com.banco.ticketero.model.QueueType queueType) {
        return advisorRepository.findAll().stream()
            .filter(advisor -> advisor.getStatus() == Advisor.AdvisorStatus.AVAILABLE)
            .filter(advisor -> advisor.getSupportedQueues() != null && advisor.getSupportedQueues().contains(queueType.name()))
            .findFirst();
    }
    
    public void notifyProximoTurno(Ticket ticket) {
        String chatId = getChatId(ticket);
        if (chatId != null) {
            OutboxMessage message = OutboxMessage.builder()
                .ticketId(ticket.getCodigoReferencia())
                .plantilla("PROXIMO")
                .estadoEnvio(OutboxMessage.MessageStatus.PENDING)
                .chatId(chatId)
                .fechaProgramada(LocalDateTime.now())
                .build();
            
            outboxMessageRepository.save(message);
            log.info("🔔 Scheduled proximity notification for ticket {}", ticket.getNumero());
        }
    }
    
    public void notifyTuTurno(Ticket ticket, Advisor advisor) {
        String chatId = getChatId(ticket);
        if (chatId != null) {
            OutboxMessage message = OutboxMessage.builder()
                .ticketId(ticket.getCodigoReferencia())
                .plantilla("TU_TURNO")
                .estadoEnvio(OutboxMessage.MessageStatus.PENDING)
                .chatId(chatId)
                .fechaProgramada(LocalDateTime.now())
                .build();
            
            outboxMessageRepository.save(message);
            log.info("🎫 Scheduled turn notification for ticket {} with advisor {}", 
                ticket.getNumero(), advisor.getName());
        }
    }
    
    private String getChatId(Ticket ticket) {
        String telefono = ticket.getTelefono();
        if (telefono == null || telefono.isEmpty()) return null;
        
        // Si es formato chileno (+56...), extraer el número después de +56
        if (telefono.startsWith("+56")) {
            return telefono.substring(3); // Quitar "+56" y usar solo el número
        }
        
        // Si es solo números (chat_id), usar como está
        return telefono;
    }
}
