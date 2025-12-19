package com.banco.ticketero;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import com.banco.ticketero.service.TicketProcessingScheduler;
import com.banco.ticketero.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSystemTest {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private TicketProcessingScheduler scheduler;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private OutboxMessageRepository outboxRepository;
    
    @Autowired
    private AdvisorRepository advisorRepository;

    @Test
    void testCompleteNotificationFlow() {
        // Setup: Create advisor
        Advisor advisor = Advisor.builder()
            .name("Test Advisor")
            .email("test@test.com")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(1)
            .supportedQueues("CAJA")
            .assignedTicketsCount(0)
            .build();
        advisorRepository.save(advisor);

        // 1. Create ticket
        var ticket = ticketService.createFromTelegram("12345678", "123456789", QueueType.CAJA);
        
        // Verify confirmation and alert messages created
        List<OutboxMessage> messages = outboxRepository.findByChatId("123456789");
        assertEquals(2, messages.size()); // CONFIRMACION + ALERTA
        
        boolean hasConfirmacion = messages.stream().anyMatch(m -> "CONFIRMACION".equals(m.getPlantilla()));
        boolean hasAlerta = messages.stream().anyMatch(m -> "ALERTA".equals(m.getPlantilla()));
        assertTrue(hasConfirmacion, "Missing CONFIRMACION message");
        assertTrue(hasAlerta, "Missing ALERTA message");

        // 2. Make ticket eligible and process queue
        Ticket ticketEntity = ticketRepository.findById(ticket.codigoReferencia()).orElseThrow();
        ticketEntity.setCreatedAt(ticketEntity.getCreatedAt().minusSeconds(15));
        ticketRepository.save(ticketEntity);
        
        scheduler.processWaitingTickets();

        // Verify ticket assigned and TU_TURNO notification created
        Ticket updatedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElseThrow();
        assertEquals(TicketStatus.ATENDIENDO, updatedTicket.getStatus());
        assertNotNull(updatedTicket.getAssignedAdvisor());
        assertEquals(1, updatedTicket.getAssignedModuleNumber());

        // Verify all 3 notifications exist
        messages = outboxRepository.findByChatId("123456789");
        assertEquals(3, messages.size()); // CONFIRMACION + ALERTA + TU_TURNO
        
        hasConfirmacion = messages.stream().anyMatch(m -> "CONFIRMACION".equals(m.getPlantilla()));
        hasAlerta = messages.stream().anyMatch(m -> "ALERTA".equals(m.getPlantilla()));
        boolean hasTuTurno = messages.stream().anyMatch(m -> "TU_TURNO".equals(m.getPlantilla()));
        
        assertTrue(hasConfirmacion, "Missing CONFIRMACION message");
        assertTrue(hasAlerta, "Missing ALERTA message");
        assertTrue(hasTuTurno, "Missing TU_TURNO message");

        // Verify advisor is busy
        Advisor busyAdvisor = advisorRepository.findById(advisor.getId()).orElseThrow();
        assertEquals(Advisor.AdvisorStatus.BUSY, busyAdvisor.getStatus());
        assertEquals(1, busyAdvisor.getAssignedTicketsCount());

        // Notification system test completed successfully
    }

    @Test
    void testQueueProcessing() {
        // Setup advisor
        advisorRepository.save(Advisor.builder()
            .name("Queue Advisor")
            .email("queue@test.com")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(2)
            .supportedQueues("CAJA")
            .assignedTicketsCount(0)
            .build());

        // Create 3 tickets
        var t1 = ticketService.createFromTelegram("11111111", "111", QueueType.CAJA);
        var t2 = ticketService.createFromTelegram("22222222", "222", QueueType.CAJA);
        var t3 = ticketService.createFromTelegram("33333333", "333", QueueType.CAJA);

        // Make only first ticket eligible (>10 seconds old)
        Ticket t1Entity = ticketRepository.findById(t1.codigoReferencia()).orElseThrow();
        t1Entity.setCreatedAt(t1Entity.getCreatedAt().minusSeconds(15));
        ticketRepository.save(t1Entity);
        
        // Process queue
        scheduler.processWaitingTickets();

        // Verify only first ticket assigned
        Ticket ticket1 = ticketRepository.findById(t1.codigoReferencia()).orElseThrow();
        Ticket ticket2 = ticketRepository.findById(t2.codigoReferencia()).orElseThrow();
        Ticket ticket3 = ticketRepository.findById(t3.codigoReferencia()).orElseThrow();

        assertEquals(TicketStatus.ATENDIENDO, ticket1.getStatus());
        assertEquals(TicketStatus.EN_ESPERA, ticket2.getStatus());
        assertEquals(TicketStatus.EN_ESPERA, ticket3.getStatus());

        // Queue processing test completed successfully
    }
}