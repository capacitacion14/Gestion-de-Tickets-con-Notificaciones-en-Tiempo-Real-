package com.banco.ticketero.domain.model.ticket;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object que representa el identificador único de un ticket.
 * Inmutable y con validaciones de dominio.
 */
@Value
public class TicketId {
    
    UUID value;
    
    private TicketId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("TicketId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Crea un nuevo TicketId con un UUID generado automáticamente.
     */
    public static TicketId generate() {
        return new TicketId(UUID.randomUUID());
    }
    
    /**
     * Crea un TicketId a partir de un UUID existente.
     */
    public static TicketId of(UUID uuid) {
        return new TicketId(uuid);
    }
    
    /**
     * Crea un TicketId a partir de un string UUID.
     */
    public static TicketId of(String uuid) {
        try {
            return new TicketId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}