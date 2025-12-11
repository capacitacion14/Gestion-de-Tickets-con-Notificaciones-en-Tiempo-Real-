package com.banco.ticketero.domain.repository;

import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueId;
import com.banco.ticketero.domain.model.queue.QueueType;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio para la gestión de colas.
 * Define las operaciones de persistencia sin implementación específica.
 */
public interface QueueRepository {
    
    /**
     * Guarda una cola en el repositorio.
     */
    Queue save(Queue queue);
    
    /**
     * Busca una cola por su ID.
     */
    Optional<Queue> findById(QueueId queueId);
    
    /**
     * Busca una cola por su tipo.
     */
    Optional<Queue> findByQueueType(QueueType queueType);
    
    /**
     * Obtiene todas las colas activas ordenadas por prioridad.
     */
    List<Queue> findActiveQueuesOrderedByPriority();
    
    /**
     * Obtiene todas las colas.
     */
    List<Queue> findAll();
    
    /**
     * Verifica si existe una cola del tipo especificado.
     */
    boolean existsByQueueType(QueueType queueType);
    
    /**
     * Obtiene colas de alta prioridad activas.
     */
    List<Queue> findHighPriorityActiveQueues();
    
    /**
     * Cuenta el total de colas activas.
     */
    long countActiveQueues();
    
    /**
     * Elimina una cola por su ID.
     */
    void deleteById(QueueId queueId);
}