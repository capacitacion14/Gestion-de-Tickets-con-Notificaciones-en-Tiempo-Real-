package com.banco.ticketero.model.dto.response;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
    UUID codigoReferencia,
    String numero,
    String nationalId,
    String telefono,
    QueueType queueType,
    TicketStatus status,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    String assignedAdvisor,
    Integer assignedModuleNumber,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {}
