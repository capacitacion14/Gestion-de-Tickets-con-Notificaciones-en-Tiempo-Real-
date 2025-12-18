package com.banco.ticketero.service;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.banco.ticketero.model.dto.response.PositionResponse;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.Ticket;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final AtomicInteger ticketCounter = new AtomicInteger(1);

    @Transactional
    public TicketResponse createFromTelegram(String nationalId, String chatId, QueueType queueType) {
        log.info("Creating Telegram ticket for nationalId: {}, chatId: {}, queue: {}", nationalId, chatId, queueType);

        String numero = generateTicketNumber(queueType);
        
        Ticket ticket = Ticket.builder()
            .numero(numero)
            .nationalId(nationalId)
            .telefono(chatId)
            .branchOffice("Telegram Bot")
            .queueType(queueType)
            .status(TicketStatus.EN_ESPERA)
            .build();

        Ticket saved = ticketRepository.save(ticket);

        long position = ticketRepository.countPositionInQueue(
            saved.getQueueType(), 
            saved.getCreatedAt()
        ) + 1;
        
        int estimatedTime = saved.getQueueType().calculateEstimatedTime((int) position);
        
        saved.setPositionInQueue((int) position);
        saved.setEstimatedWaitMinutes(estimatedTime);
        
        saved = ticketRepository.save(saved);

        scheduleNotificationMessages(saved);

        log.info("Telegram ticket created: {}, position: {}, chatId: {}", saved.getNumero(), position, chatId);

        return toResponse(saved);
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        log.info("Creating ticket for nationalId: {}, queue: {}", request.nationalId(), request.queueType());

        String numero = generateTicketNumber(request.queueType());
        
        Ticket ticket = Ticket.builder()
            .numero(numero)
            .nationalId(request.nationalId())
            .telefono(request.telefono())
            .branchOffice(request.branchOffice())
            .queueType(request.queueType())
            .status(TicketStatus.EN_ESPERA)
            .build();

        Ticket saved = ticketRepository.save(ticket);

        long position = ticketRepository.countPositionInQueue(
            saved.getQueueType(), 
            saved.getCreatedAt()
        ) + 1;
        
        int estimatedTime = saved.getQueueType().calculateEstimatedTime((int) position);
        
        saved.setPositionInQueue((int) position);
        saved.setEstimatedWaitMinutes(estimatedTime);
        
        saved = ticketRepository.save(saved);

        scheduleNotificationMessages(saved);

        log.info("Ticket created: {}, position: {}", saved.getNumero(), position);

        return toResponse(saved);
    }

    public Optional<TicketResponse> findByCodigoReferencia(UUID codigoReferencia) {
        return ticketRepository.findById(codigoReferencia)
            .map(this::toResponse);
    }

    public Optional<TicketResponse> findByNumero(String numero) {
        return ticketRepository.findByNumero(numero)
            .map(this::toResponse);
    }

    public PositionResponse calculatePosition(UUID codigoReferencia) {
        Ticket ticket = ticketRepository.findById(codigoReferencia)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        long position = ticketRepository.countPositionInQueue(
            ticket.getQueueType(), 
            ticket.getCreatedAt()
        ) + 1;

        int estimatedTime = ticket.getQueueType().calculateEstimatedTime((int) position);

        return new PositionResponse(
            ticket.getNumero(),
            (int) position,
            estimatedTime,
            ticket.getStatus(),
            LocalDateTime.now()
        );
    }

    public java.util.List<TicketResponse> findAll() {
        return ticketRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private String generateTicketNumber(QueueType queueType) {
        int number = ticketCounter.getAndIncrement();
        return String.format("%s%03d", queueType.getPrefijo(), number);
    }

    private void scheduleNotificationMessages(Ticket ticket) {
        String chatId = getChatId(ticket.getTelefono());
        if (chatId == null) {
            log.debug("No chat ID found for ticket: {}", ticket.getNumero());
            return;
        }

        OutboxMessage confirmacion = OutboxMessage.builder()
            .ticketId(ticket.getCodigoReferencia())
            .plantilla("CONFIRMACION")
            .estadoEnvio(OutboxMessage.MessageStatus.PENDING)
            .chatId(chatId)
            .build();

        // Mensaje de alerta 5 segundos después
        OutboxMessage alerta = OutboxMessage.builder()
            .ticketId(ticket.getCodigoReferencia())
            .plantilla("ALERTA")
            .estadoEnvio(OutboxMessage.MessageStatus.PENDING)
            .fechaProgramada(LocalDateTime.now().plusSeconds(5))
            .chatId(chatId)
            .build();

        outboxMessageRepository.save(confirmacion);
        outboxMessageRepository.save(alerta);
        log.debug("Messages scheduled for ticket: {} (confirmation + alert)", ticket.getNumero());
    }
    
    private String getChatId(String telefono) {
        if (telefono == null || telefono.isEmpty()) return null;
        
        // Si es formato chileno (+56...), extraer el número después de +56
        if (telefono.startsWith("+56")) {
            return telefono.substring(3);
        }
        
        // Si es solo números (chat_id de Telegram), usar como está
        if (telefono.matches("^\\d+$")) {
            return telefono;
        }
        
        // Si no es un formato reconocido, no enviar notificaciones
        return null;
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getNationalId(),
            ticket.getTelefono(),
            ticket.getQueueType(),
            ticket.getStatus(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
            ticket.getAssignedModuleNumber(),
            ticket.getCreatedAt(),
            ticket.getExpiresAt()
        );
    }
}
