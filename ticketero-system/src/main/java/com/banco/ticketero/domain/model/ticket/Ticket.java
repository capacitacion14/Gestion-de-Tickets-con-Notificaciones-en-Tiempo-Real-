package com.banco.ticketero.domain.model.ticket;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.QueueType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregate Root que representa un ticket de atención.
 * Contiene la lógica de negocio relacionada con el ciclo de vida del ticket.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Ticket {
    
    private final TicketId id;
    private final TicketCode ticketCode;
    private final CustomerId customerId;
    private final QueueType queueType;
    private final TicketStatus status;
    private final Integer positionInQueue;
    private final Integer estimatedWaitTime;
    private final LocalDateTime calledAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    
    /**
     * Crea un nuevo ticket para un cliente.
     */
    public static Ticket create(CustomerId customerId, QueueType queueType, TicketCode ticketCode) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (queueType == null) {
            throw new IllegalArgumentException("Queue type is required");
        }
        if (ticketCode == null) {
            throw new IllegalArgumentException("Ticket code is required");
        }
        
        return Ticket.builder()
                .id(TicketId.generate())
                .ticketCode(ticketCode)
                .customerId(customerId)
                .queueType(queueType)
                .status(TicketStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Asigna una posición en la cola al ticket.
     */
    public Ticket withPosition(int position, int estimatedWaitMinutes) {
        if (position <= 0) {
            throw new IllegalArgumentException("Position must be positive");
        }
        if (estimatedWaitMinutes < 0) {
            throw new IllegalArgumentException("Estimated wait time cannot be negative");
        }
        if (!status.isActive()) {
            throw new IllegalStateException("Cannot assign position to inactive ticket");
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(this.status)
                .positionInQueue(position)
                .estimatedWaitTime(estimatedWaitMinutes)
                .calledAt(this.calledAt)
                .completedAt(this.completedAt)
                .cancelledAt(this.cancelledAt)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Llama al ticket para atención.
     */
    public Ticket call() {
        if (!status.canTransitionTo(TicketStatus.CALLED)) {
            throw new IllegalStateException("Cannot call ticket in status: " + status);
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(TicketStatus.CALLED)
                .positionInQueue(null) // No longer in queue
                .estimatedWaitTime(null)
                .calledAt(LocalDateTime.now())
                .completedAt(this.completedAt)
                .cancelledAt(this.cancelledAt)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Marca el ticket como en progreso.
     */
    public Ticket startProgress() {
        if (!status.canTransitionTo(TicketStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Cannot start progress for ticket in status: " + status);
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(TicketStatus.IN_PROGRESS)
                .positionInQueue(this.positionInQueue)
                .estimatedWaitTime(this.estimatedWaitTime)
                .calledAt(this.calledAt)
                .completedAt(this.completedAt)
                .cancelledAt(this.cancelledAt)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Completa el ticket exitosamente.
     */
    public Ticket complete() {
        if (!status.canTransitionTo(TicketStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot complete ticket in status: " + status);
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(TicketStatus.COMPLETED)
                .positionInQueue(null)
                .estimatedWaitTime(null)
                .calledAt(this.calledAt)
                .completedAt(LocalDateTime.now())
                .cancelledAt(this.cancelledAt)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Cancela el ticket.
     */
    public Ticket cancel() {
        if (!status.canTransitionTo(TicketStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel ticket in status: " + status);
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(TicketStatus.CANCELLED)
                .positionInQueue(null)
                .estimatedWaitTime(null)
                .calledAt(this.calledAt)
                .completedAt(this.completedAt)
                .cancelledAt(LocalDateTime.now())
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Marca el ticket como no show (cliente no se presentó).
     */
    public Ticket markAsNoShow() {
        if (!status.canTransitionTo(TicketStatus.NO_SHOW)) {
            throw new IllegalStateException("Cannot mark as no show ticket in status: " + status);
        }
        
        return Ticket.builder()
                .id(this.id)
                .ticketCode(this.ticketCode)
                .customerId(this.customerId)
                .queueType(this.queueType)
                .status(TicketStatus.NO_SHOW)
                .positionInQueue(null)
                .estimatedWaitTime(null)
                .calledAt(this.calledAt)
                .completedAt(this.completedAt)
                .cancelledAt(LocalDateTime.now()) // Use cancelled timestamp for no show
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Verifica si el ticket está activo (puede ser procesado).
     */
    public boolean isActive() {
        return status.isActive();
    }
    
    /**
     * Verifica si el ticket está en estado final.
     */
    public boolean isFinished() {
        return status.isFinalState();
    }
    
    /**
     * Verifica si el ticket está esperando en cola.
     */
    public boolean isWaitingInQueue() {
        return status == TicketStatus.PENDING && positionInQueue != null;
    }
    
    /**
     * Calcula el tiempo transcurrido desde la creación.
     */
    public long getMinutesSinceCreation() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * Calcula el tiempo de atención (desde llamada hasta completado).
     */
    public Long getServiceTimeMinutes() {
        if (calledAt == null || completedAt == null) {
            return null;
        }
        return java.time.Duration.between(calledAt, completedAt).toMinutes();
    }
    
    /**
     * Verifica si el ticket ha expirado (más de 2 horas sin actividad).
     */
    public boolean isExpired() {
        if (isFinished()) {
            return false; // Finished tickets don't expire
        }
        
        LocalDateTime lastActivity = calledAt != null ? calledAt : createdAt;
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).toHours() >= 2;
    }
}