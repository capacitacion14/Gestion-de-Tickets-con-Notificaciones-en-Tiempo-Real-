package com.banco.ticketero.service;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService Unit Tests")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private TicketService ticketService;

    private CreateTicketRequest validRequest;
    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTicketRequest(
            "12345678",
            "+56912345678",
            "Sucursal Centro",
            QueueType.CAJA
        );

        savedTicket = Ticket.builder()
            .codigoReferencia(UUID.randomUUID())
            .numero("C001")
            .nationalId("12345678")
            .telefono("+56912345678")
            .branchOffice("Sucursal Centro")
            .queueType(QueueType.CAJA)
            .status(TicketStatus.EN_ESPERA)
            .positionInQueue(1)
            .estimatedWaitMinutes(5)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Crear ticket con datos válidos debe retornar TicketResponse")
    void create_validRequest_returnsTicketResponse() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.countPositionInQueue(any(), any())).thenReturn(0L);

        // When
        TicketResponse response = ticketService.create(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.numero()).isEqualTo("C001");
        assertThat(response.nationalId()).isEqualTo("12345678");
        assertThat(response.queueType()).isEqualTo(QueueType.CAJA);
        assertThat(response.status()).isEqualTo(TicketStatus.EN_ESPERA);
    }
}