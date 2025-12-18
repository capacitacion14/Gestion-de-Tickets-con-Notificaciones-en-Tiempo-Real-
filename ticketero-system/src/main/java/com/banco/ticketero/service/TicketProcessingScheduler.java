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
    private final com.banco.ticketero.repository.OutboxMessageRepository outboxMessageRepository;

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
            
            // Intentar asignar el primer ticket elegible si hay asesor disponible
            Ticket firstTicket = eligibleTickets.get(0);
            List<Advisor> availableAdvisors = advisorRepository
                .findAvailableAdvisorsForQueue(queueType.name());
            
            log.info("👥 Available advisors for {}: {}", queueType, availableAdvisors.size());
            
            // Debug: mostrar todos los advisors
            List<Advisor> allAdvisors = advisorRepository.findAll();
            log.info("📊 Total advisors in DB: {}", allAdvisors.size());
            allAdvisors.forEach(advisor -> 
                log.info("👤 Advisor: {} - Status: {} - Queues: {}", 
                    advisor.getName(), advisor.getStatus(), advisor.getSupportedQueues())
            );
            
            log.info("📞 Ticket {} phone: '{}'", firstTicket.getNumero(), firstTicket.getTelefono());
            
            if (!availableAdvisors.isEmpty() && firstTicket.getTelefono() != null) {
                Advisor advisor = availableAdvisors.get(0);
                
                firstTicket.setStatus(TicketStatus.ATENDIENDO);
                firstTicket.setAssignedAdvisor(advisor);
                firstTicket.setAssignedModuleNumber(advisor.getModuleNumber());
                firstTicket.setUpdatedAt(LocalDateTime.now());
                
                advisor.setStatus(Advisor.AdvisorStatus.BUSY);
                advisor.setAssignedTicketsCount(advisor.getAssignedTicketsCount() + 1);
                advisor.setLastAssignmentAt(LocalDateTime.now());
                
                ticketRepository.save(firstTicket);
                advisorRepository.save(advisor);
                
                // Programar mensaje TU_TURNO inmediatamente
                String chatId = getChatId(firstTicket.getTelefono());
                if (chatId != null) {
                    com.banco.ticketero.model.entity.OutboxMessage tuTurno = com.banco.ticketero.model.entity.OutboxMessage.builder()
                        .ticketId(firstTicket.getCodigoReferencia())
                        .plantilla("TU_TURNO")
                        .estadoEnvio(com.banco.ticketero.model.entity.OutboxMessage.MessageStatus.PENDING)
                        .chatId(chatId)
                        .fechaProgramada(LocalDateTime.now())
                        .build();
                    outboxMessageRepository.save(tuTurno);
                    log.info("📩 TU_TURNO message scheduled IMMEDIATELY for ticket {}", firstTicket.getNumero());
                }
                
                log.info("🎫 Ticket {} assigned to advisor {} at module {}", 
                    firstTicket.getNumero(), advisor.getName(), advisor.getModuleNumber());
            }
        }
    }

    private String getChatId(String telefono) {
        if (telefono == null || telefono.isEmpty()) return null;
        if (telefono.startsWith("+56")) {
            return telefono.substring(3);
        }
        if (telefono.matches("^\\d+$")) {
            return telefono;
        }
        return null;
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