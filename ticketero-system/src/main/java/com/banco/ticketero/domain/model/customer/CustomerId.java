package com.banco.ticketero.domain.model.customer;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object que representa el identificador único de un cliente.
 * Inmutable y con validaciones de dominio.
 */
@Value
public class CustomerId {
    
    UUID value;
    
    private CustomerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Crea un nuevo CustomerId con un UUID generado automáticamente.
     */
    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
    
    /**
     * Crea un CustomerId a partir de un UUID existente.
     */
    public static CustomerId of(UUID uuid) {
        return new CustomerId(uuid);
    }
    
    /**
     * Crea un CustomerId a partir de un string UUID.
     */
    public static CustomerId of(String uuid) {
        try {
            return new CustomerId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}