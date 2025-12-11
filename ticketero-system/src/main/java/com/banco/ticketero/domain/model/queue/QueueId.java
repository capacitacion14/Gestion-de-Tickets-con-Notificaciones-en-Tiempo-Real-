package com.banco.ticketero.domain.model.queue;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object que representa el identificador único de una cola.
 * Inmutable y con validaciones de dominio.
 */
@Value
public class QueueId {
    
    UUID value;
    
    private QueueId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("QueueId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Crea un nuevo QueueId con un UUID generado automáticamente.
     */
    public static QueueId generate() {
        return new QueueId(UUID.randomUUID());
    }
    
    /**
     * Crea un QueueId a partir de un UUID existente.
     */
    public static QueueId of(UUID uuid) {
        return new QueueId(uuid);
    }
    
    /**
     * Crea un QueueId a partir de un string UUID.
     */
    public static QueueId of(String uuid) {
        try {
            return new QueueId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}