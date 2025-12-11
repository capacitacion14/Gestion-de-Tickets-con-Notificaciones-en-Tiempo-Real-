package com.banco.ticketero.domain.repository;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.model.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de tickets.
 * Define las operaciones de persistencia sin implementación específica.
 */
public interface TicketRepository {
    
    /**
     * Guarda un ticket en el repositorio.
     */
    Ticket save(Ticket ticket);
    
    /**
     * Busca un ticket por su ID.
     */
    Optional<Ticket> findById(TicketId ticketId);
    
    /**
     * Busca un ticket por su código único.
     */
    Optional<Ticket> findByTicketCode(TicketCode ticketCode);
    
    /**
     * Busca tickets por cliente.
     */
    List<Ticket> findByCustomerId(CustomerId customerId);
    
    /**
     * Busca tickets por estado.
     */
    List<Ticket> findByStatus(TicketStatus status);
    
    /**
     * Busca tickets por tipo de cola y estado.
     */
    List<Ticket> findByQueueTypeAndStatus(QueueType queueType, TicketStatus status);
    
    /**
     * Obtiene tickets pendientes en una cola específica ordenados por posición.
     */
    List<Ticket> findPendingTicketsByQueueTypeOrderedByPosition(QueueType queueType);
    
    /**
     * Obtiene el siguiente ticket a ser llamado en una cola.
     */
    Optional<Ticket> findNextTicketToCall(QueueType queueType);
    
    /**
     * Busca tickets activos de un cliente.
     */
    List<Ticket> findActiveTicketsByCustomerId(CustomerId customerId);
    
    /**
     * Busca tickets creados en un rango de fechas.
     */
    List<Ticket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Cuenta tickets por estado.
     */
    long countByStatus(TicketStatus status);
    
    /**
     * Cuenta tickets por tipo de cola y estado.
     */
    long countByQueueTypeAndStatus(QueueType queueType, TicketStatus status);
    
    /**
     * Obtiene tickets expirados (más de 2 horas sin actividad).
     */
    List<Ticket> findExpiredTickets();
    
    /**
     * Verifica si existe un ticket con el código especificado.
     */
    boolean existsByTicketCode(TicketCode ticketCode);
    
    /**
     * Obtiene la máxima posición en cola para un tipo específico.
     */
    Optional<Integer> findMaxPositionInQueue(QueueType queueType);
    
    /**
     * Elimina un ticket por su ID.
     */
    void deleteById(TicketId ticketId);
}