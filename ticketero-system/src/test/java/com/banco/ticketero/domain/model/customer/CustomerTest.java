package com.banco.ticketero.domain.model.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Aggregate Root Tests")
class CustomerTest {

    private final NationalId validNationalId = NationalId.of("12345678");
    private final String validFirstName = "Juan";
    private final String validLastName = "Pérez";
    private final String validEmail = "juan.perez@email.com";
    private final String validPhone = "123456789";

    @Test
    @DisplayName("Should create customer with basic information")
    void shouldCreateCustomerWithBasicInformation() {
        // When
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        
        // Then
        assertNotNull(customer);
        assertNotNull(customer.getId());
        assertEquals(validNationalId, customer.getNationalId());
        assertEquals(validFirstName, customer.getFirstName());
        assertEquals(validLastName, customer.getLastName());
        assertFalse(customer.isVip());
        assertNotNull(customer.getCreatedAt());
        assertNull(customer.getPhone());
        assertNull(customer.getEmail());
        assertNull(customer.getTelegramChatId());
    }

    @Test
    @DisplayName("Should create VIP customer with complete information")
    void shouldCreateVipCustomerWithCompleteInformation() {
        // When
        Customer customer = Customer.createVip(validNationalId, validFirstName, validLastName, 
                                            validEmail, validPhone);
        
        // Then
        assertNotNull(customer);
        assertNotNull(customer.getId());
        assertEquals(validNationalId, customer.getNationalId());
        assertEquals(validFirstName, customer.getFirstName());
        assertEquals(validLastName, customer.getLastName());
        assertEquals(validEmail, customer.getEmail());
        assertEquals(validPhone, customer.getPhone());
        assertTrue(customer.isVip());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    @DisplayName("Should trim whitespace from names")
    void shouldTrimWhitespaceFromNames() {
        // Given
        String firstNameWithSpaces = "  Juan  ";
        String lastNameWithSpaces = "  Pérez  ";
        
        // When
        Customer customer = Customer.create(validNationalId, firstNameWithSpaces, lastNameWithSpaces);
        
        // Then
        assertEquals("Juan", customer.getFirstName());
        assertEquals("Pérez", customer.getLastName());
    }

    @Test
    @DisplayName("Should throw exception when national ID is null")
    void shouldThrowExceptionWhenNationalIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Customer.create(null, validFirstName, validLastName)
        );
        
        assertEquals("National ID is required", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "A", "Very long name that definitely exceeds fifty characters limit"})
    @DisplayName("Should throw exception for invalid first name")
    void shouldThrowExceptionForInvalidFirstName(String invalidFirstName) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Customer.create(validNationalId, invalidFirstName, validLastName)
        );
        
        assertTrue(exception.getMessage().contains("First name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "B", "Very long last name that definitely exceeds fifty characters limit"})
    @DisplayName("Should throw exception for invalid last name")
    void shouldThrowExceptionForInvalidLastName(String invalidLastName) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Customer.create(validNationalId, validFirstName, invalidLastName)
        );
        
        assertTrue(exception.getMessage().contains("Last name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "test.domain.com"})
    @DisplayName("Should throw exception for invalid email format")
    void shouldThrowExceptionForInvalidEmailFormat(String invalidEmail) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Customer.createVip(validNationalId, validFirstName, validLastName, 
                                   invalidEmail, validPhone)
        );
        
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Should update Telegram chat ID")
    void shouldUpdateTelegramChatId() {
        // Given
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        Long chatId = 123456789L;
        
        // When
        Customer updatedCustomer = customer.withTelegramChatId(chatId);
        
        // Then
        assertEquals(chatId, updatedCustomer.getTelegramChatId());
        // Verify immutability - original customer unchanged
        assertNull(customer.getTelegramChatId());
        // Verify other fields remain the same
        assertEquals(customer.getId(), updatedCustomer.getId());
        assertEquals(customer.getNationalId(), updatedCustomer.getNationalId());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -123456})
    @DisplayName("Should throw exception for invalid Telegram chat ID")
    void shouldThrowExceptionForInvalidTelegramChatId(Long invalidChatId) {
        // Given
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customer.withTelegramChatId(invalidChatId)
        );
        
        assertEquals("Telegram chat ID must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should update contact information")
    void shouldUpdateContactInformation() {
        // Given
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        String newEmail = "new.email@domain.com";
        String newPhone = "987654321";
        
        // When
        Customer updatedCustomer = customer.updateContactInfo(newEmail, newPhone);
        
        // Then
        assertEquals(newEmail, updatedCustomer.getEmail());
        assertEquals(newPhone, updatedCustomer.getPhone());
        // Verify immutability
        assertNull(customer.getEmail());
        assertNull(customer.getPhone());
    }

    @Test
    @DisplayName("Should return full name")
    void shouldReturnFullName() {
        // Given
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        
        // When
        String fullName = customer.getFullName();
        
        // Then
        assertEquals("Juan Pérez", fullName);
    }

    @Test
    @DisplayName("Should identify when Telegram is configured")
    void shouldIdentifyWhenTelegramIsConfigured() {
        // Given
        Customer customerWithoutTelegram = Customer.create(validNationalId, validFirstName, validLastName);
        Customer customerWithTelegram = customerWithoutTelegram.withTelegramChatId(123456789L);
        
        // Then
        assertFalse(customerWithoutTelegram.hasTelegramConfigured());
        assertTrue(customerWithTelegram.hasTelegramConfigured());
    }

    @Test
    @DisplayName("Should identify when customer can receive notifications")
    void shouldIdentifyWhenCustomerCanReceiveNotifications() {
        // Given
        Customer basicCustomer = Customer.create(validNationalId, validFirstName, validLastName);
        Customer customerWithEmail = basicCustomer.updateContactInfo(validEmail, null);
        Customer customerWithTelegram = basicCustomer.withTelegramChatId(123456789L);
        
        // Then
        assertFalse(basicCustomer.canReceiveNotifications());
        assertTrue(customerWithEmail.canReceiveNotifications());
        assertTrue(customerWithTelegram.canReceiveNotifications());
    }

    @Test
    @DisplayName("Should handle null values in contact update")
    void shouldHandleNullValuesInContactUpdate() {
        // Given
        Customer customer = Customer.createVip(validNationalId, validFirstName, validLastName, 
                                             validEmail, validPhone);
        
        // When - Update with null values should keep existing values
        Customer updatedCustomer = customer.updateContactInfo(null, null);
        
        // Then
        assertEquals(validEmail, updatedCustomer.getEmail());
        assertEquals(validPhone, updatedCustomer.getPhone());
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Customer customer = Customer.create(validNationalId, validFirstName, validLastName);
        CustomerId originalId = customer.getId();
        LocalDateTime originalCreatedAt = customer.getCreatedAt();
        
        // When - Perform operations that return new instances
        Customer withTelegram = customer.withTelegramChatId(123456789L);
        Customer withContact = customer.updateContactInfo(validEmail, validPhone);
        
        // Then - Original customer should remain unchanged
        assertEquals(originalId, customer.getId());
        assertEquals(originalCreatedAt, customer.getCreatedAt());
        assertNull(customer.getTelegramChatId());
        assertNull(customer.getEmail());
        assertNull(customer.getPhone());
        
        // New instances should have different values
        assertNotSame(customer, withTelegram);
        assertNotSame(customer, withContact);
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats() {
        // Given valid email formats
        String[] validEmails = {
            "test@domain.com",
            "user.name@domain.co.uk",
            "user+tag@domain.org",
            "user_name@domain-name.com"
        };
        
        // Then - Should not throw exceptions
        for (String email : validEmails) {
            assertDoesNotThrow(() -> 
                Customer.createVip(validNationalId, validFirstName, validLastName, email, validPhone)
            );
        }
    }

    @Test
    @DisplayName("Should handle empty email as valid for VIP creation")
    void shouldHandleEmptyEmailAsValidForVipCreation() {
        // When & Then - Empty email should be allowed
        assertDoesNotThrow(() -> 
            Customer.createVip(validNationalId, validFirstName, validLastName, "", validPhone)
        );
        
        assertDoesNotThrow(() -> 
            Customer.createVip(validNationalId, validFirstName, validLastName, "   ", validPhone)
        );
    }
}