package com.banco.ticketero.domain.model.customer;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregate Root que representa un cliente del banco.
 * Contiene la lógica de negocio relacionada con clientes.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Customer {
    
    private final CustomerId id;
    private final NationalId nationalId;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String email;
    private final Long telegramChatId;
    private final boolean isVip;
    private final LocalDateTime createdAt;
    
    /**
     * Crea un nuevo cliente con los datos básicos.
     */
    public static Customer create(NationalId nationalId, String firstName, String lastName) {
        validateRequiredFields(nationalId, firstName, lastName);
        
        return Customer.builder()
                .id(CustomerId.generate())
                .nationalId(nationalId)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .isVip(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Crea un nuevo cliente VIP.
     */
    public static Customer createVip(NationalId nationalId, String firstName, String lastName, 
                                   String email, String phone) {
        validateRequiredFields(nationalId, firstName, lastName);
        validateEmail(email);
        
        return Customer.builder()
                .id(CustomerId.generate())
                .nationalId(nationalId)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .email(email.trim())
                .phone(phone != null ? phone.trim() : null)
                .isVip(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Actualiza el chat ID de Telegram para notificaciones.
     */
    public Customer withTelegramChatId(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new IllegalArgumentException("Telegram chat ID must be positive");
        }
        
        return Customer.builder()
                .id(this.id)
                .nationalId(this.nationalId)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phone(this.phone)
                .email(this.email)
                .telegramChatId(chatId)
                .isVip(this.isVip)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Actualiza la información de contacto del cliente.
     */
    public Customer updateContactInfo(String email, String phone) {
        if (email != null) {
            validateEmail(email);
        }
        
        return Customer.builder()
                .id(this.id)
                .nationalId(this.nationalId)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phone(phone != null ? phone.trim() : this.phone)
                .email(email != null ? email.trim() : this.email)
                .telegramChatId(this.telegramChatId)
                .isVip(this.isVip)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * Retorna el nombre completo del cliente.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Verifica si el cliente tiene Telegram configurado.
     */
    public boolean hasTelegramConfigured() {
        return telegramChatId != null && telegramChatId > 0;
    }
    
    /**
     * Verifica si el cliente puede recibir notificaciones.
     */
    public boolean canReceiveNotifications() {
        return hasTelegramConfigured() || (email != null && !email.trim().isEmpty());
    }
    
    private static void validateRequiredFields(NationalId nationalId, String firstName, String lastName) {
        if (nationalId == null) {
            throw new IllegalArgumentException("National ID is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (firstName.trim().length() < 2 || firstName.trim().length() > 50) {
            throw new IllegalArgumentException("First name must be between 2 and 50 characters");
        }
        if (lastName.trim().length() < 2 || lastName.trim().length() > 50) {
            throw new IllegalArgumentException("Last name must be between 2 and 50 characters");
        }
    }
    
    private static void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!email.matches(emailRegex)) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
}