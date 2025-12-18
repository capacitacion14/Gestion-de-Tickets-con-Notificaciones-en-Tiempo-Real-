package com.banco.ticketero.service;

import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketProcessingScheduler {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;
    private final AssignmentService assignmentService;

    @Scheduled(fixedDelay = 10000) // Cada 10 segundos
    @Transactional
    public void processWaitingTickets() {
        log.info("🔄 Scheduler running at {}", LocalDateTime.now());
        
        // Procesar tickets EN_ESPERA que tengan más de 10 segundos
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        
        for (com.banco.ticketero.model.QueueType queueType : com.banco.ticketero.model.QueueType.values()) {
            List<Ticket> queueTickets = ticketRepository
                .findByQueueTypeAndStatusOrderByCreatedAtAsc(queueType, TicketStatus.EN_ESPERA);
            
            // Filtrar solo tickets con más de 10 segundos
            List<Ticket> eligibleTickets = queueTickets.stream()
                .filter(ticket -> ticket.getCreatedAt().isBefore(tenSecondsAgo))
                .toList();
            
            log.info("📋 Queue {}: {} total tickets, {} eligible (>10s)", queueType, queueTickets.size(), eligibleTickets.size());
            
            if (eligibleTickets.isEmpty()) continue;
            
            // Notificar "próximo turno" a los primeros 2 tickets elegibles
            for (int i = 0; i < Math.min(2, eligibleTickets.size()); i++) {
                Ticket ticket = eligibleTickets.get(i);
                if (!ticket.isProximoNotified() && ticket.getTelefono() != null) {
                    assignmentService.notifyProximoTurno(ticket);
                    ticket.setProximoNotified(true);
                    ticketRepository.save(ticket);
                    log.info("🔔 Proximity notification sent for ticket {}", ticket.getNumero());
                }
            }
            
            // Intentar asignar el primer ticket elegible si hay asesor disponible
            Ticket firstTicket = eligibleTickets.get(0);
            List<Advisor> availableAdvisors = advisorRepository
                .findAvailableAdvisorsForQueue(queueType.name());
            
            log.info("👥 Available advisors for {}: {}", queueType, availableAdvisors.size());
            
            if (availableAdvisors.isEmpty()) {
                log.warn("⚠️ No advisors available for queue {}", queueType);
            }
            
            if (!availableAdvisors.isEmpty()) {
                if (firstTicket.getTelefono() == null) {
                    log.warn("⚠️ Ticket {} has no phone number, skipping", firstTicket.getNumero());
                } else {
                    log.info("📞 Processing ticket {} with phone {}", firstTicket.getNumero(), firstTicket.getTelefono());
                }
            }
            
            if (!availableAdvisors.isEmpty() && firstTicket.getTelefono() != null) {
                Advisor advisor = availableAdvisors.get(0);
                
                assignmentService.notifyTuTurno(firstTicket, advisor);
                
                firstTicket.setStatus(TicketStatus.ATENDIENDO);
                firstTicket.setAssignedAdvisor(advisor);
                firstTicket.setAssignedModuleNumber(advisor.getModuleNumber());
                firstTicket.setUpdatedAt(LocalDateTime.now());
                
                advisor.setStatus(Advisor.AdvisorStatus.BUSY);
                advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() + 1);
                advisor.setLastAssignmentAt(LocalDateTime.now());
                
                ticketRepository.save(firstTicket);
                advisorRepository.save(advisor);
                
                log.info("🎫 Ticket {} assigned to advisor {} at module {}", 
                    firstTicket.getNumero(), advisor.getName(), advisor.getModuleNumber());
            }
        }
    }

    @Scheduled(fixedDelay = 10000) // Cada 10 segundos
    @Transactional
    public void completeProcessedTickets() {
        List<Ticket> inAttentionTickets = ticketRepository
            .findByStatus(TicketStatus.ATENDIENDO);

        for (Ticket ticket : inAttentionTickets) {
            // Simular tiempo de atención (20 segundos para testing)
            if (ticket.getUpdatedAt() != null && 
                ticket.getUpdatedAt().plusSeconds(20).isBefore(LocalDateTime.now())) {
                
                ticket.setStatus(TicketStatus.COMPLETADO);
                ticket.setCompletedAt(LocalDateTime.now());
                ticket.setUpdatedAt(LocalDateTime.now());
                
                if (ticket.getAssignedAdvisor() != null) {
                    Advisor advisor = ticket.getAssignedAdvisor();
                    advisor.setStatus(Advisor.AdvisorStatus.AVAILABLE);
                    advisor.setAssignedTicketsCount(
                        Math.max(0, advisor.getAssignedTicketsCount() - 1));
                    advisorRepository.save(advisor);
                    log.info("✅ Advisor {} is now available again", advisor.getName());
                }
                
                ticketRepository.save(ticket);
                log.info("✅ Ticket {} completed after 20 seconds", ticket.getNumero());
            }
        }
    }
}