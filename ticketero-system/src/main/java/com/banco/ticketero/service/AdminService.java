package com.banco.ticketero.service;

import com.banco.ticketero.model.dto.response.DashboardResponse;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final TicketRepository ticketRepository;
    private final AdvisorRepository advisorRepository;

    public DashboardResponse getDashboard() {
        log.info("Getting admin dashboard data");
        
        long totalTickets = ticketRepository.count();
        long waitingTickets = ticketRepository.countByStatus(com.banco.ticketero.model.TicketStatus.EN_ESPERA);
        long inProgressTickets = ticketRepository.countByStatus(com.banco.ticketero.model.TicketStatus.ATENDIENDO);
        long completedTickets = ticketRepository.countByStatus(com.banco.ticketero.model.TicketStatus.COMPLETADO);
        
        long availableAdvisors = advisorRepository.countByStatus(Advisor.AdvisorStatus.AVAILABLE);
        long busyAdvisors = advisorRepository.countByStatus(Advisor.AdvisorStatus.BUSY);
        long onBreakAdvisors = advisorRepository.countByStatus(Advisor.AdvisorStatus.BREAK);
        
        return new DashboardResponse(
            totalTickets,
            waitingTickets,
            inProgressTickets,
            completedTickets,
            availableAdvisors,
            busyAdvisors,
            onBreakAdvisors
        );
    }

    public List<com.banco.ticketero.model.entity.Ticket> getQueueByType(String queueType) {
        log.info("Getting queue data for type: {}", sanitizeForLog(queueType));
        
        try {
            com.banco.ticketero.model.QueueType type = com.banco.ticketero.model.QueueType.valueOf(queueType);
            return ticketRepository.findByQueueTypeAndStatusOrderByCreatedAtAsc(
                type, 
                com.banco.ticketero.model.TicketStatus.EN_ESPERA
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid queue type requested: {}", sanitizeForLog(queueType));
            throw new IllegalArgumentException("Invalid queue type: " + queueType);
        }
    }

    @Transactional
    public void updateAdvisorStatus(Long advisorId, String status) {
        log.info("Updating advisor {} status to: {}", advisorId, sanitizeForLog(status));
        
        Advisor advisor = advisorRepository.findById(advisorId)
            .orElseThrow(() -> new RuntimeException("Advisor not found: " + advisorId));
        
        try {
            Advisor.AdvisorStatus newStatus = Advisor.AdvisorStatus.valueOf(status);
            advisor.setStatus(newStatus);
            advisorRepository.save(advisor);
            log.info("Advisor {} status updated successfully", advisorId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid advisor status: {}", sanitizeForLog(status));
            throw new IllegalArgumentException("Invalid advisor status: " + status);
        }
    }

    public List<Advisor> getAllAdvisors() {
        log.info("Getting all advisors");
        return advisorRepository.findAll();
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_");
    }
}