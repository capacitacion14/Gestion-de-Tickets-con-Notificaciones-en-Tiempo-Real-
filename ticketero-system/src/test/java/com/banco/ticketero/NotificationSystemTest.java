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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "telegram.bot.token=test-token"
})
@Transactional
class NotificationSystemTest {

    @Autowired TicketService ticketService;
    @Autowired TicketProcessingScheduler scheduler;
    @Autowired TicketRepository ticketRepository;
    @Autowired OutboxMessageRepository outboxRepository;
    @Autowired AdvisorRepository advisorRepository;

    @Test
    void testCompleteNotificationFlow() {
        // Setup: Create advisor
        Advisor advisor = Advisor.builder()
            .name("Test Advisor")
            .email("test@test.com")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(1)
            .supportedQueues(List.of("CAJA"))
            .assignedTicketsCount(0)
            .build();
        advisorRepository.save(advisor);

        // 1. Create ticket
        var ticket = ticketService.createFromTelegram("12345678", "123456789", QueueType.CAJA);
        
        // Verify confirmation message created
        List<OutboxMessage> messages = outboxRepository.findByChatId("123456789");
        assertEquals(1, messages.size());
        assertEquals("CONFIRMACION", messages.get(0).getPlantilla());
        assertEquals(OutboxMessage.MessageStatus.PENDING, messages.get(0).getEstadoEnvio());

        // 2. Process queue
        scheduler.processWaitingTickets();

        // Verify ticket assigned and notifications created
        Ticket updatedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElseThrow();
        assertEquals(TicketStatus.ATENDIENDO, updatedTicket.getStatus());
        assertNotNull(updatedTicket.getAssignedAdvisor());
        assertEquals(1, updatedTicket.getAssignedModuleNumber());

        // Verify all 3 notifications exist
        messages = outboxRepository.findByChatId("123456789");
        assertEquals(3, messages.size());
        
        boolean hasConfirmacion = messages.stream().anyMatch(m -> "CONFIRMACION".equals(m.getPlantilla()));
        boolean hasProximo = messages.stream().anyMatch(m -> "PROXIMO".equals(m.getPlantilla()));
        boolean hasTuTurno = messages.stream().anyMatch(m -> "TU_TURNO".equals(m.getPlantilla()));
        
        assertTrue(hasConfirmacion, "Missing CONFIRMACION message");
        assertTrue(hasProximo, "Missing PROXIMO message");
        assertTrue(hasTuTurno, "Missing TU_TURNO message");

        // Verify advisor is busy
        Advisor busyAdvisor = advisorRepository.findById(advisor.getId()).orElseThrow();
        assertEquals(Advisor.AdvisorStatus.BUSY, busyAdvisor.getStatus());
        assertEquals(1, busyAdvisor.getAssignedTicketsCount());

        System.out.println("✅ NOTIFICATION SYSTEM TEST PASSED");
        System.out.println("📋 Ticket: " + ticket.numero() + " → " + updatedTicket.getStatus());
        System.out.println("📨 Messages: " + messages.size() + " (CONFIRMACION, PROXIMO, TU_TURNO)");
        System.out.println("👤 Advisor: " + busyAdvisor.getName() + " → " + busyAdvisor.getStatus());
    }

    @Test
    void testQueueProcessing() {
        // Setup advisor
        advisorRepository.save(Advisor.builder()
            .name("Queue Advisor")
            .email("queue@test.com")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(2)
            .supportedQueues(List.of("CAJA"))
            .assignedTicketsCount(0)
            .build());

        // Create 3 tickets
        var t1 = ticketService.createFromTelegram("11111111", "111", QueueType.CAJA);
        var t2 = ticketService.createFromTelegram("22222222", "222", QueueType.CAJA);
        var t3 = ticketService.createFromTelegram("33333333", "333", QueueType.CAJA);

        // Process queue
        scheduler.processWaitingTickets();

        // Verify only first ticket assigned
        Ticket ticket1 = ticketRepository.findById(t1.codigoReferencia()).orElseThrow();
        Ticket ticket2 = ticketRepository.findById(t2.codigoReferencia()).orElseThrow();
        Ticket ticket3 = ticketRepository.findById(t3.codigoReferencia()).orElseThrow();

        assertEquals(TicketStatus.ATENDIENDO, ticket1.getStatus());
        assertEquals(TicketStatus.EN_ESPERA, ticket2.getStatus());
        assertEquals(TicketStatus.EN_ESPERA, ticket3.getStatus());

        // Verify proximity notifications
        assertTrue(ticket2.isProximoNotified(), "Second ticket should have proximity notification");
        assertFalse(ticket3.isProximoNotified(), "Third ticket should NOT have proximity notification");

        System.out.println("✅ QUEUE PROCESSING TEST PASSED");
        System.out.println("🎫 T1: " + ticket1.getStatus() + " (assigned)");
        System.out.println("⏳ T2: " + ticket2.getStatus() + " (notified: " + ticket2.isProximoNotified() + ")");
        System.out.println("⏳ T3: " + ticket3.getStatus() + " (notified: " + ticket3.isProximoNotified() + ")");
    }
}