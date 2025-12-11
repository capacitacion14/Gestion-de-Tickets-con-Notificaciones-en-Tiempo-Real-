package com.banco.ticketero.domain.exception;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;

/**
 * Excepción lanzada cuando no se encuentra un cliente solicitado.
 */
public class CustomerNotFoundException extends DomainException {
    
    public CustomerNotFoundException(CustomerId customerId) {
        super("Customer not found with ID: " + customerId);
    }
    
    public CustomerNotFoundException(NationalId nationalId) {
        super("Customer not found with national ID: " + nationalId);
    }
    
    public CustomerNotFoundException(Long telegramChatId) {
        super("Customer not found with Telegram chat ID: " + telegramChatId);
    }
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
}