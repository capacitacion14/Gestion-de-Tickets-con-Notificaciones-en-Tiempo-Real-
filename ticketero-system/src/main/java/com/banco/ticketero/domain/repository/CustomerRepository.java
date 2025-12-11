package com.banco.ticketero.domain.repository;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de clientes.
 * Define las operaciones de persistencia sin implementación específica.
 */
public interface CustomerRepository {
    
    /**
     * Guarda un cliente en el repositorio.
     */
    Customer save(Customer customer);
    
    /**
     * Busca un cliente por su ID.
     */
    Optional<Customer> findById(CustomerId customerId);
    
    /**
     * Busca un cliente por su cédula de identidad.
     */
    Optional<Customer> findByNationalId(NationalId nationalId);
    
    /**
     * Busca un cliente por su chat ID de Telegram.
     */
    Optional<Customer> findByTelegramChatId(Long telegramChatId);
    
    /**
     * Verifica si existe un cliente con la cédula especificada.
     */
    boolean existsByNationalId(NationalId nationalId);
    
    /**
     * Obtiene todos los clientes VIP.
     */
    List<Customer> findVipCustomers();
    
    /**
     * Obtiene clientes que pueden recibir notificaciones.
     */
    List<Customer> findCustomersWithNotificationCapability();
    
    /**
     * Cuenta el total de clientes registrados.
     */
    long count();
    
    /**
     * Elimina un cliente por su ID.
     */
    void deleteById(CustomerId customerId);
}