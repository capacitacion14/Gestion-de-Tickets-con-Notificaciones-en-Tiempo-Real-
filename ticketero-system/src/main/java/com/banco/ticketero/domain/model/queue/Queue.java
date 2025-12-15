package com.banco.ticketero.domain.model.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregate Root que representa una cola de atención.
 * Contiene la lógica de negocio relacionada con la gestión de colas.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Queue {
    
    private final QueueId id;
    private final QueueType queueType;
    private final int maxCapacity;
    private final int estimatedTimeMinutes;
    private final boolean isActive;
    private final int priorityOrder;
    private final LocalDateTime createdAt;
    
    /**
     * Crea una nueva cola con configuración por defecto.
     */
    public static Queue create(QueueType queueType) {
        if (queueType == null) {
            throw new IllegalArgumentException("Queue type is required");
        }
        
        return Queue.builder()
                .id(QueueId.generate())
                .queueType(queueType)
                .maxCapacity(queueType.getDefaultMaxCapacity())
                .estimatedTimeMinutes(queueType.getDefaultEstimatedTimeMinutes())
                .priorityOrder(queueType.getPriorityOrder())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Crea una nueva cola con configuración personalizada.
     */
    public static Queue create(QueueType queueType, int maxCapacity, int estimatedTimeMinutes) {
        if (queueType == null) {
            throw new IllegalArgumentException("Queue type is required");
        }
        validateCapacity(maxCapacity);
        validateEstimatedTime(estimatedTimeMinutes);
        
        return Queue.builder()
                .id(QueueId.generate())
                .queueType(queueType)
                .maxCapacity(maxCapacity)
                .estimatedTimeMinutes(estimatedTimeMinutes)
                .priorityOrder(queueType.getPriorityOrder())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Actualiza la capacidad máxima de la cola.
     */
    public Queue withMaxCapacity(int newMaxCapacity) {
        validateCapacity(newMaxCapacity);
        
        return Queue.builder()
                .id(this.id)
                .queueType(this.queueType)
                .maxCapacity(newMaxCapacity)
                .estimatedTimeMinutes(this.estimatedTimeMinutes)
                .priorityOrder(this.priorityOrder)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Actualiza el tiempo estimado de atención.
     */
    public Queue withEstimatedTime(int newEstimatedTimeMinutes) {
        validateEstimatedTime(newEstimatedTimeMinutes);
        
        return Queue.builder()
                .id(this.id)
                .queueType(this.queueType)
                .maxCapacity(this.maxCapacity)
                .estimatedTimeMinutes(newEstimatedTimeMinutes)
                .priorityOrder(this.priorityOrder)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Activa o desactiva la cola.
     */
    public Queue withActiveStatus(boolean active) {
        return Queue.builder()
                .id(this.id)
                .queueType(this.queueType)
                .maxCapacity(this.maxCapacity)
                .estimatedTimeMinutes(this.estimatedTimeMinutes)
                .priorityOrder(this.priorityOrder)
                .isActive(active)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Verifica si la cola puede aceptar más tickets.
     */
    public boolean canAcceptTickets(int currentTicketCount) {
        return isActive && currentTicketCount < maxCapacity;
    }
    
    /**
     * Verifica si la cola está llena.
     */
    public boolean isFull(int currentTicketCount) {
        return currentTicketCount >= maxCapacity;
    }
    
    /**
     * Calcula el tiempo estimado de espera basado en la posición en la cola.
     */
    public int calculateEstimatedWaitTime(int positionInQueue) {
        if (positionInQueue <= 0) {
            return 0;
        }
        return positionInQueue * estimatedTimeMinutes;
    }
    
    /**
     * Verifica si esta cola tiene mayor prioridad que otra.
     */
    public boolean hasHigherPriorityThan(Queue other) {
        return this.priorityOrder < other.priorityOrder;
    }
    
    /**
     * Verifica si es una cola de alta prioridad.
     */
    public boolean isHighPriority() {
        return queueType.isHighPriority();
    }
    
    /**
     * Getter para isActive (compatibilidad con mappers).
     */
    public Boolean getIsActive() {
        return isActive;
    }
    
    /**
     * Método isActive() para compatibilidad.
     */
    public boolean isActive() {
        return isActive;
    }
    
    private static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be positive");
        }
        if (capacity > 200) {
            throw new IllegalArgumentException("Max capacity cannot exceed 200");
        }
    }
    
    private static void validateEstimatedTime(int estimatedTime) {
        if (estimatedTime <= 0) {
            throw new IllegalArgumentException("Estimated time must be positive");
        }
        if (estimatedTime > 120) {
            throw new IllegalArgumentException("Estimated time cannot exceed 120 minutes");
        }
    }
}