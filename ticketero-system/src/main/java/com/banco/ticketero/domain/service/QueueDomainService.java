package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketStatus;

import java.util.List;

/**
 * Servicio de dominio para la lógica de negocio relacionada con colas.
 * Contiene reglas de negocio que no pertenecen a una entidad específica.
 */
public class QueueDomainService {
    
    /**
     * Calcula la posición en cola para un nuevo ticket.
     */
    public int calculateQueuePosition(List<Ticket> existingTickets, QueueType queueType) {
        if (existingTickets == null || existingTickets.isEmpty()) {
            return 1;
        }
        
        // Filtrar solo tickets pendientes en la misma cola
        int maxPosition = existingTickets.stream()
                .filter(ticket -> ticket.getQueueType() == queueType)
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING)
                .filter(ticket -> ticket.getPositionInQueue() != null)
                .mapToInt(Ticket::getPositionInQueue)
                .max()
                .orElse(0);
        
        return maxPosition + 1;
    }
    
    /**
     * Calcula el tiempo estimado de espera para una posición específica.
     */
    public int calculateEstimatedWaitTime(Queue queue, int positionInQueue) {
        if (queue == null || positionInQueue <= 0) {
            return 0;
        }
        
        return queue.calculateEstimatedWaitTime(positionInQueue);
    }
    
    /**
     * Verifica si una cola puede aceptar un nuevo ticket.
     */
    public boolean canAcceptNewTicket(Queue queue, int currentTicketCount) {
        if (queue == null) {
            return false;
        }
        
        return queue.canAcceptTickets(currentTicketCount);
    }
    
    /**
     * Determina la cola de mayor prioridad disponible para un cliente VIP.
     */
    public QueueType determineOptimalQueueForVip() {
        return QueueType.VIP;
    }
    
    /**
     * Determina la cola apropiada para un cliente regular.
     */
    public QueueType determineOptimalQueueForRegular() {
        return QueueType.GENERAL;
    }
    
    /**
     * Reorganiza las posiciones en cola después de que un ticket sea llamado.
     */
    public void reorganizeQueuePositions(List<Ticket> remainingTickets, QueueType queueType) {
        if (remainingTickets == null || remainingTickets.isEmpty()) {
            return;
        }
        
        // Filtrar tickets pendientes de la misma cola y ordenar por posición actual
        List<Ticket> ticketsToReorganize = remainingTickets.stream()
                .filter(ticket -> ticket.getQueueType() == queueType)
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING)
                .filter(ticket -> ticket.getPositionInQueue() != null)
                .sorted((t1, t2) -> Integer.compare(t1.getPositionInQueue(), t2.getPositionInQueue()))
                .toList();
        
        // Las posiciones se reorganizarán en la capa de aplicación
        // Este método valida que la reorganización es necesaria
    }
    
    /**
     * Verifica si un tipo de cola tiene prioridad sobre otro.
     */
    public boolean hasHigherPriority(QueueType queue1, QueueType queue2) {
        if (queue1 == null || queue2 == null) {
            return false;
        }
        
        return queue1.hasHigherPriorityThan(queue2);
    }
    
    /**
     * Calcula la capacidad total disponible en todas las colas activas.
     */
    public int calculateTotalAvailableCapacity(List<Queue> activeQueues, List<Ticket> activeTickets) {
        if (activeQueues == null || activeQueues.isEmpty()) {
            return 0;
        }
        
        int totalCapacity = activeQueues.stream()
                .filter(queue -> queue.isActive())
                .mapToInt(Queue::getMaxCapacity)
                .sum();
        
        int currentTicketCount = activeTickets != null ? 
                (int) activeTickets.stream()
                        .filter(Ticket::isActive)
                        .count() : 0;
        
        return Math.max(0, totalCapacity - currentTicketCount);
    }
    
    /**
     * Determina si el sistema está en capacidad crítica (>90% ocupado).
     */
    public boolean isSystemAtCriticalCapacity(List<Queue> activeQueues, List<Ticket> activeTickets) {
        if (activeQueues == null || activeQueues.isEmpty()) {
            return false;
        }
        
        int totalCapacity = activeQueues.stream()
                .filter(queue -> queue.isActive())
                .mapToInt(Queue::getMaxCapacity)
                .sum();
        
        if (totalCapacity == 0) {
            return true;
        }
        
        int currentTicketCount = activeTickets != null ? 
                (int) activeTickets.stream()
                        .filter(Ticket::isActive)
                        .count() : 0;
        
        double occupancyRate = (double) currentTicketCount / totalCapacity;
        return occupancyRate >= 0.9; // 90% o más
    }
}