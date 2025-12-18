package com.banco.ticketero.controller;

import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.TicketRepository;
import com.banco.ticketero.service.TicketProcessingScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final AdvisorRepository advisorRepository;
    private final TicketRepository ticketRepository;
    private final TicketProcessingScheduler scheduler;
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @PostMapping("/create-advisor")
    public String createAdvisor() {
        try {
            Advisor advisor = Advisor.builder()
                .name("Juan Pérez")
                .email("juan@test.com")
                .status(Advisor.AdvisorStatus.AVAILABLE)
                .moduleNumber(1)
                .supportedQueues("CAJA")
                .assignedTicketsCount(0)
                .build();
            
            advisorRepository.save(advisor);
            return "Advisor created: " + advisor.getName();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @DeleteMapping("/clean")
    public String cleanTickets() {
        try {
            ticketRepository.deleteAll();
            return "All tickets deleted";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/advisors")
    public Map<String, Object> checkAdvisors() {
        List<Advisor> all = advisorRepository.findAll();
        List<Advisor> caja = advisorRepository.findAvailableAdvisorsForQueue("CAJA");
        
        log.info("📊 Total advisors: {}", all.size());
        all.forEach(advisor -> 
            log.info("👤 {} - {} - {}", advisor.getName(), advisor.getStatus(), advisor.getSupportedQueues())
        );
        
        log.info("🏢 CAJA advisors: {}", caja.size());
        
        return Map.of(
            "total", all.size(),
            "available_for_caja", caja.size(),
            "advisors", all.stream().map(a -> 
                Map.of("name", a.getName(), "status", a.getStatus(), "queues", a.getSupportedQueues())
            ).toList()
        );
    }

    @GetMapping("/tickets")
    public Map<String, Object> checkTickets() {
        List<Ticket> all = ticketRepository.findAll();
        
        return Map.of(
            "total", all.size(),
            "tickets", all.stream().map(t -> 
                Map.of(
                    "numero", t.getNumero(),
                    "status", t.getStatus(),
                    "telefono", t.getTelefono(),
                    "queue", t.getQueueType(),
                    "advisor", t.getAssignedAdvisor() != null ? t.getAssignedAdvisor().getName() : "None"
                )
            ).toList()
        );
    }

    @PostMapping("/run-scheduler")
    public String runScheduler() {
        log.info("🔄 Running scheduler manually...");
        scheduler.processWaitingTickets();
        return "Scheduler executed";
    }
}