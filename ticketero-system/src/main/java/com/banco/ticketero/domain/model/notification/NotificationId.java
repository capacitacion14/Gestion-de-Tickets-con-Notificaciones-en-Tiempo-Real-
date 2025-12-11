package com.banco.ticketero.domain.model.notification;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object que representa el identificador único de una notificación.
 * Inmutable y con validaciones de dominio.
 */
@Value
public class NotificationId {
    
    UUID value;
    
    private NotificationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Crea un nuevo NotificationId con un UUID generado automáticamente.
     */
    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }
    
    /**
     * Crea un NotificationId a partir de un UUID existente.
     */
    public static NotificationId of(UUID uuid) {
        return new NotificationId(uuid);
    }
    
    /**
     * Crea un NotificationId a partir de un string UUID.
     */
    public static NotificationId of(String uuid) {
        try {
            return new NotificationId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid, e);
        }
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}