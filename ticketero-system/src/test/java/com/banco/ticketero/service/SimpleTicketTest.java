package com.banco.ticketero.service;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.repository.AdvisorRepository;
import com.banco.ticketero.repository.OutboxMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SimpleTicketTest {

    private static final int EXPECTED_INITIAL_MESSAGES = 2; // CONFIRMACION + ALERTA
    private static final String TEST_CHAT_ID = "123456789";
    private static final String TEST_NATIONAL_ID = "12345678";
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private OutboxMessageRepository outboxRepository;
    
    @Autowired
    private AdvisorRepository advisorRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        advisorRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create ticket with confirmation and alert notifications")
    void shouldCreateTicketWithNotifications() {
        // Create ticket from Telegram
        TicketResponse ticket = ticketService.createFromTelegram(TEST_NATIONAL_ID, TEST_CHAT_ID, QueueType.CAJA);
        
        // Verify ticket created
        assertThat(ticket).isNotNull();
        assertThat(ticket.status()).isEqualTo(TicketStatus.EN_ESPERA);
        assertThat(ticket.numero()).startsWith("C");
        
        // Verify messages scheduled
        List<OutboxMessage> messages = outboxRepository.findByChatId(TEST_CHAT_ID);
        assertThat(messages).hasSize(EXPECTED_INITIAL_MESSAGES);
        
        boolean hasConfirmacion = messages.stream().anyMatch(m -> "CONFIRMACION".equals(m.getPlantilla()));
        boolean hasAlerta = messages.stream().anyMatch(m -> "ALERTA".equals(m.getPlantilla()));
        
        assertThat(hasConfirmacion).isTrue();
        assertThat(hasAlerta).isTrue();
    }

    @Test
    @DisplayName("Should create and persist advisor successfully")
    void shouldCreateAdvisor() {
        // Create advisor
        Advisor advisor = Advisor.builder()
            .name("Test Advisor")
            .email("test@banco.cl")
            .status(Advisor.AdvisorStatus.AVAILABLE)
            .moduleNumber(1)
            .supportedQueues("CAJA")
            .assignedTicketsCount(0)
            .build();
        
        Advisor saved = advisorRepository.save(advisor);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Advisor");
        assertThat(saved.getStatus()).isEqualTo(Advisor.AdvisorStatus.AVAILABLE);
    }
}