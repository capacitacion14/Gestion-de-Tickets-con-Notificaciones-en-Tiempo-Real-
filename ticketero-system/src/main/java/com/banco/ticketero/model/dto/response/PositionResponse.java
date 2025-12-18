package com.banco.ticketero.model.dto.response;

import com.banco.ticketero.model.TicketStatus;

import java.time.LocalDateTime;

public record PositionResponse(
    String numero,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    TicketStatus status,
    LocalDateTime calculatedAt
) {}
