package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.queue.QueueId;
import com.banco.ticketero.domain.model.queue.QueueType;

/**
 * Excepción lanzada cuando no se encuentra una cola solicitada.
 */
public class QueueNotFoundException extends DomainException {
    
    public QueueNotFoundException(QueueId queueId) {
        super("Queue not found with ID: " + queueId);
    }
    
    public QueueNotFoundException(QueueType queueType) {
        super("Queue not found for type: " + queueType);
    }
    
    public QueueNotFoundException(String message) {
        super(message);
    }
}