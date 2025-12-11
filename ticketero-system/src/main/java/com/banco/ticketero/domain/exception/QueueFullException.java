package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.queue.QueueType;

/**
 * Excepción lanzada cuando se intenta agregar un ticket a una cola que está llena.
 */
public class QueueFullException extends DomainException {
    
    public QueueFullException(QueueType queueType, int currentCapacity, int maxCapacity) {
        super(String.format("Queue %s is full: %d/%d tickets", queueType, currentCapacity, maxCapacity));
    }
    
    public QueueFullException(QueueType queueType) {
        super(String.format("Queue %s has reached maximum capacity", queueType));
    }
    
    public QueueFullException(String message) {
        super(message);
    }
}