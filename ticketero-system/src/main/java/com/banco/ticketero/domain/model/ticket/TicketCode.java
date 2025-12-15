package com.banco.ticketero.domain.model.ticket;

import lombok.Value;

/**
 * Value Object que representa el código único de un ticket (ej: T1001).
 * Inmutable y con validaciones de dominio.
 */
@Value
public class TicketCode {
    
    String value;
    
    private TicketCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TicketCode cannot be null or empty");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("TicketCode must follow format T#### (e.g., T1001)");
        }
        this.value = value.trim().toUpperCase();
    }
    
    /**
     * Crea un TicketCode a partir de un string.
     */
    public static TicketCode of(String code) {
        return new TicketCode(code);
    }
    
    /**
     * Crea un TicketCode a partir de un número secuencial.
     */
    public static TicketCode fromSequence(int sequence) {
        if (sequence < 1000 || sequence > 9999) {
            throw new IllegalArgumentException("Sequence must be between 1000 and 9999");
        }
        return new TicketCode("T" + sequence);
    }
    
    /**
     * Extrae el número secuencial del código.
     */
    public int getSequenceNumber() {
        return Integer.parseInt(value.substring(1));
    }
    
    private boolean isValidFormat(String code) {
        if (code == null) {
            return false;
        }
        
        String trimmedCode = code.trim().toUpperCase();
        if (trimmedCode.length() != 5) {
            return false;
        }
        
        if (!trimmedCode.startsWith("T")) {
            return false;
        }
        
        try {
            int sequence = Integer.parseInt(trimmedCode.substring(1));
            return sequence >= 1000 && sequence <= 9999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}