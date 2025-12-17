package com.banco.ticketero.testutil;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.service.TicketLifecycleManager;

import java.time.LocalDateTime;

public class TestDataBuilder {

    // ========== ENUMS ==========
    
    public static QueueType defaultQueueType() {
        return QueueType.CAJA;
    }

    public static QueueType vipQueueType() {
        return QueueType.GERENCIA;
    }

    public static TicketStatus activeStatus() {
        return TicketStatus.EN_ESPERA;
    }

    public static TicketStatus completedStatus() {
        return TicketStatus.COMPLETADO;
    }

    // ========== SCHEDULER STATS ==========

    public static TicketLifecycleManager.SchedulerStats defaultStats() {
        return new TicketLifecycleManager.SchedulerStats(
            10,
            2,
            LocalDateTime.now()
        );
    }

    // ========== DTOs (cuando se implementen) ==========
    
    // TODO: Agregar builders cuando se implementen las clases:
    // - CreateTicketRequest
    // - TicketResponse
    // - PositionResponse
    
    // ========== ENTITIES (cuando se implementen) ==========
    
    // TODO: Agregar builders cuando se implementen las clases:
    // - Ticket
    // - Advisor
    // - Message
    // - AuditLog
}
