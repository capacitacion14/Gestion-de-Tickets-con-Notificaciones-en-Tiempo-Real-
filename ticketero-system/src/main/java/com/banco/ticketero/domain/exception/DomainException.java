package com.banco.ticketero.domain.exception;

/**
 * Excepción base para todas las excepciones del dominio.
 * Representa errores de lógica de negocio.
 */
public abstract class DomainException extends RuntimeException {
    
    protected DomainException(String message) {
        super(message);
    }
    
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}