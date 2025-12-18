package com.banco.ticketero.service;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationFlowIntegrationTest {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private TicketProcessingScheduler scheduler;
    
    @Autowired
    private TelegramService telegramService;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private OutboxMessageRepository outboxRepository;
    
    @Autowired
    private AdvisorRepository advisorRepository;

    @BeforeEach
    void setUp() {
        // Clean data
        outboxRepository.deleteAll();
        ticketRepository.deleteAll();
        advisorRepository.deleteAll();
        
        // Create test advisor
        Advisor advisor = Advisor.builder()
            .name("Test Advisor")
            .email("test@banco.cl")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(1)
            .supportedQueues(List.of("CAJA"))
            .assignedTicketsCount(0)
            .build();
        advisorRepository.save(advisor);
    }

    @Test
    void shouldSendNotificationsInCorrectOrder() throws InterruptedException {
        // 1. Create ticket from Telegram
        String chatId = "123456789";
        TicketResponse ticket = ticketService.createFromTelegram("12345678", chatId, QueueType.CAJA);
        
        // Verify ticket created
        assertThat(ticket).isNotNull();
        assertThat(ticket.status()).isEqualTo(TicketStatus.EN_ESPERA);
        
        // 2. Check confirmation message was scheduled
        List<OutboxMessage> messages = outboxRepository.findByChatId(chatId);
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getPlantilla()).isEqualTo("CONFIRMACION");
        assertThat(messages.get(0).getEstadoEnvio()).isEqualTo(OutboxMessage.MessageStatus.PENDING);
        
        // 3. Process confirmation message
        telegramService.processPendingMessages();
        
        // Verify confirmation sent
        messages = outboxRepository.findByChatId(chatId);
        assertThat(messages.get(0).getEstadoEnvio()).isEqualTo(OutboxMessage.MessageStatus.SENT);
        
        // 4. Run scheduler to process waiting tickets
        scheduler.processWaitingTickets();
        
        // 5. Check proximity and turn notifications were created
        messages = outboxRepository.findByChatIdOrderByIdAsc(chatId);
        assertThat(messages).hasSize(3); // CONFIRMACION + PROXIMO + TU_TURNO
        
        OutboxMessage proximoMsg = messages.stream()
            .filter(m -> "PROXIMO".equals(m.getPlantilla()))
            .findFirst().orElse(null);
        assertThat(proximoMsg).isNotNull();
        
        OutboxMessage tuTurnoMsg = messages.stream()
            .filter(m -> "TU_TURNO".equals(m.getPlantilla()))
            .findFirst().orElse(null);
        assertThat(tuTurnoMsg).isNotNull();
        
        // 6. Verify ticket was assigned
        Ticket updatedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(updatedTicket).isNotNull();
        assertThat(updatedTicket.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(updatedTicket.getAssignedAdvisor()).isNotNull();
        assertThat(updatedTicket.getAssignedModuleNumber()).isEqualTo(1);
        
        // 7. Process remaining notifications
        telegramService.processPendingMessages();
        
        // Verify all messages sent
        messages = outboxRepository.findByChatId(chatId);
        long sentCount = messages.stream()
            .mapToLong(m -> m.getEstadoEnvio() == OutboxMessage.MessageStatus.SENT ? 1 : 0)
            .sum();
        assertThat(sentCount).isEqualTo(3);
        
        // 8. Verify advisor is busy
        Advisor advisor = advisorRepository.findAll().get(0);
        assertThat(advisor.getStatus()).isEqualTo(Advisor.AdvisorStatus.BUSY);
        assertThat(advisor.getAssignedTicketsCount()).isEqualTo(1);
        
        System.out.println("✅ NOTIFICATION FLOW TEST PASSED");
        System.out.println("📋 Ticket: " + ticket.numero());
        System.out.println("📱 Chat ID: " + chatId);
        System.out.println("📨 Messages sent: " + sentCount);
        System.out.println("👤 Advisor assigned: " + updatedTicket.getAssignedAdvisor().getName());
        System.out.println("🏢 Module: " + updatedTicket.getAssignedModuleNumber());
    }

    @Test
    void shouldCompleteTicketAndFreeAdvisor() throws InterruptedException {
        // Create and assign ticket
        TicketResponse ticket = ticketService.createFromTelegram("87654321", "987654321", QueueType.CAJA);
        scheduler.processWaitingTickets();
        
        // Verify ticket is being attended
        Ticket assignedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(assignedTicket.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        
        // Wait for completion (20 seconds in real scenario, but we'll simulate)
        Thread.sleep(100); // Small delay for test
        
        // Manually complete for test
        scheduler.completeProcessedTickets();
        
        // For test, manually set completion time to trigger completion
        assignedTicket.setUpdatedAt(assignedTicket.getUpdatedAt().minusSeconds(25));
        ticketRepository.save(assignedTicket);
        
        // Run completion scheduler
        scheduler.completeProcessedTickets();
        
        // Verify ticket completed and advisor freed
        Ticket completedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(completedTicket.getStatus()).isEqualTo(TicketStatus.COMPLETADO);
        
        Advisor advisor = advisorRepository.findAll().get(0);
        assertThat(advisor.getStatus()).isEqualTo(Advisor.AdvisorStatus.AVAILABLE);
        assertThat(advisor.getAssignedTicketsCount()).isEqualTo(0);
        
        System.out.println("✅ COMPLETION FLOW TEST PASSED");
        System.out.println("🎫 Ticket completed: " + completedTicket.getNumero());
        System.out.println("👤 Advisor available: " + advisor.getName());
    }

    @Test
    void shouldHandleMultipleTicketsInQueue() {
        // Create 3 tickets
        TicketResponse ticket1 = ticketService.createFromTelegram("11111111", "111", QueueType.CAJA);
        TicketResponse ticket2 = ticketService.createFromTelegram("22222222", "222", QueueType.CAJA);
        TicketResponse ticket3 = ticketService.createFromTelegram("33333333", "333", QueueType.CAJA);
        
        // Process queue
        scheduler.processWaitingTickets();
        
        // Verify only first ticket assigned
        Ticket t1 = ticketRepository.findById(ticket1.codigoReferencia()).orElse(null);
        Ticket t2 = ticketRepository.findById(ticket2.codigoReferencia()).orElse(null);
        Ticket t3 = ticketRepository.findById(ticket3.codigoReferencia()).orElse(null);
        
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
        
        // Verify proximity notifications for top 2 waiting tickets
        assertThat(t2.isProximoNotified()).isTrue();
        assertThat(t3.isProximoNotified()).isFalse(); // Only top 2 get proximity notification
        
        System.out.println("✅ QUEUE MANAGEMENT TEST PASSED");
        System.out.println("🎫 Assigned: " + t1.getNumero());
        System.out.println("⏳ Waiting (notified): " + t2.getNumero());
        System.out.println("⏳ Waiting: " + t3.getNumero());
    }
}