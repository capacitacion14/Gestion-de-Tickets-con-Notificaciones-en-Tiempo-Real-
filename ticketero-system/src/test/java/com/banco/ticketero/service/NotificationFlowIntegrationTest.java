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

import java.time.LocalDateTime;
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
            .supportedQueues("CAJA")
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
        
        // 2. Check confirmation and alert messages were scheduled
        List<OutboxMessage> messages = outboxRepository.findByChatId(chatId);
        assertThat(messages).hasSize(2); // CONFIRMACION + ALERTA
        
        boolean hasConfirmacion = messages.stream().anyMatch(m -> "CONFIRMACION".equals(m.getPlantilla()));
        boolean hasAlerta = messages.stream().anyMatch(m -> "ALERTA".equals(m.getPlantilla()));
        assertThat(hasConfirmacion).isTrue();
        assertThat(hasAlerta).isTrue();
        
        // 3. Wait for ticket to be eligible (>10 seconds old)
        Thread.sleep(100); // Small delay for test
        
        // 4. Simulate ticket being old enough (modify created time)
        Ticket ticketEntity = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(ticketEntity).isNotNull();
        ticketEntity.setCreatedAt(LocalDateTime.now().minusSeconds(15));
        ticketRepository.save(ticketEntity);
        
        // 5. Run scheduler to process waiting tickets
        scheduler.processWaitingTickets();
        
        // 6. Check TU_TURNO notification was created
        messages = outboxRepository.findByChatId(chatId);
        assertThat(messages).hasSize(3); // CONFIRMACION + ALERTA + TU_TURNO
        
        boolean hasTuTurno = messages.stream().anyMatch(m -> "TU_TURNO".equals(m.getPlantilla()));
        assertThat(hasTuTurno).isTrue();
        
        // 7. Verify ticket was assigned
        Ticket updatedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(updatedTicket).isNotNull();
        assertThat(updatedTicket.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(updatedTicket.getAssignedAdvisor()).isNotNull();
        assertThat(updatedTicket.getAssignedModuleNumber()).isEqualTo(1);
        
        // 8. Verify advisor is busy
        Advisor busyAdvisor = advisorRepository.findAll().get(0);
        assertThat(busyAdvisor.getStatus()).isEqualTo(Advisor.AdvisorStatus.BUSY);
        assertThat(busyAdvisor.getAssignedTicketsCount()).isEqualTo(1);
        
        // Test completed successfully
    }

    @Test
    void shouldAssignTicketToAdvisor() throws InterruptedException {
        // Create advisor
        Advisor advisor = Advisor.builder()
            .name("Assignment Advisor")
            .email("assignment@banco.cl")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(2)
            .supportedQueues("CAJA")
            .assignedTicketsCount(0)
            .build();
        advisorRepository.save(advisor);
        
        // Create ticket
        TicketResponse ticket = ticketService.createFromTelegram("87654321", "987654321", QueueType.CAJA);
        
        // Make ticket eligible and process
        Ticket ticketEntity = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        ticketEntity.setCreatedAt(LocalDateTime.now().minusSeconds(15));
        ticketRepository.save(ticketEntity);
        
        scheduler.processWaitingTickets();
        
        // Verify ticket is being attended
        Ticket assignedTicket = ticketRepository.findById(ticket.codigoReferencia()).orElse(null);
        assertThat(assignedTicket.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(assignedTicket.getAssignedAdvisor()).isNotNull();
        assertThat(assignedTicket.getAssignedModuleNumber()).isEqualTo(2);
        
        // Verify advisor is busy
        Advisor busyAdvisor = advisorRepository.findAll().stream()
            .filter(a -> a.getName().equals("Assignment Advisor"))
            .findFirst().orElse(null);
        assertThat(busyAdvisor.getStatus()).isEqualTo(Advisor.AdvisorStatus.BUSY);
        assertThat(busyAdvisor.getAssignedTicketsCount()).isEqualTo(1);
        
        // Assignment test completed successfully
    }

    @Test
    void shouldHandleMultipleTicketsInQueue() {
        // Create advisor
        Advisor advisor = Advisor.builder()
            .name("Queue Advisor")
            .email("queue@banco.cl")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(3)
            .supportedQueues("CAJA")
            .assignedTicketsCount(0)
            .build();
        advisorRepository.save(advisor);
        
        // Create 3 tickets
        TicketResponse ticket1 = ticketService.createFromTelegram("11111111", "111", QueueType.CAJA);
        TicketResponse ticket2 = ticketService.createFromTelegram("22222222", "222", QueueType.CAJA);
        TicketResponse ticket3 = ticketService.createFromTelegram("33333333", "333", QueueType.CAJA);
        
        // Make only first ticket eligible (>10 seconds old)
        Ticket t1Entity = ticketRepository.findById(ticket1.codigoReferencia()).orElse(null);
        t1Entity.setCreatedAt(LocalDateTime.now().minusSeconds(15));
        ticketRepository.save(t1Entity);
        
        // Process queue
        scheduler.processWaitingTickets();
        
        // Verify only first ticket assigned
        Ticket t1 = ticketRepository.findById(ticket1.codigoReferencia()).orElse(null);
        Ticket t2 = ticketRepository.findById(ticket2.codigoReferencia()).orElse(null);
        Ticket t3 = ticketRepository.findById(ticket3.codigoReferencia()).orElse(null);
        
        assertThat(t1.getStatus()).isEqualTo(TicketStatus.ATENDIENDO);
        assertThat(t2.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
        assertThat(t3.getStatus()).isEqualTo(TicketStatus.EN_ESPERA);
        
        // Queue management test completed successfully
    }
}