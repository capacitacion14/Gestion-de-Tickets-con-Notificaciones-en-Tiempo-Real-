package com.banco.ticketero.domain.service;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de dominio para la lógica de negocio relacionada con tickets.
 * Contiene reglas de negocio complejas que involucran múltiples entidades.
 */
public class TicketDomainService {
    
    private final QueueDomainService queueDomainService;
    
    public TicketDomainService(QueueDomainService queueDomainService) {
        this.queueDomainService = queueDomainService;
    }
    
    /**
     * Determina el tipo de cola apropiado para un cliente.
     */
    public QueueType determineQueueTypeForCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        if (customer.isVip()) {
            return queueDomainService.determineOptimalQueueForVip();
        }
        
        return queueDomainService.determineOptimalQueueForRegular();
    }
    
    /**
     * Genera el próximo código de ticket disponible.
     */
    public TicketCode generateNextTicketCode(List<TicketCode> existingCodes) {
        if (existingCodes == null || existingCodes.isEmpty()) {
            return TicketCode.fromSequence(1000);
        }
        
        int maxSequence = existingCodes.stream()
                .mapToInt(TicketCode::getSequenceNumber)
                .max()
                .orElse(999);
        
        int nextSequence = maxSequence + 1;
        
        // Si llegamos al límite, reiniciar desde 1000
        if (nextSequence > 9999) {
            nextSequence = 1000;
        }
        
        return TicketCode.fromSequence(nextSequence);
    }
    
    /**
     * Valida si un ticket puede ser llamado.
     */
    public boolean canCallTicket(Ticket ticket, List<Ticket> queueTickets) {
        if (ticket == null) {
            return false;
        }
        
        if (ticket.getStatus() != TicketStatus.PENDING) {
            return false;
        }
        
        // Verificar que no hay tickets con mayor prioridad esperando
        return !hasHigherPriorityTicketsWaiting(ticket, queueTickets);
    }
    
    /**
     * Verifica si hay tickets de mayor prioridad esperando.
     */
    private boolean hasHigherPriorityTicketsWaiting(Ticket ticket, List<Ticket> queueTickets) {
        if (queueTickets == null || queueTickets.isEmpty()) {
            return false;
        }
        
        return queueTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.PENDING)
                .filter(t -> t.getPositionInQueue() != null)
                .filter(t -> ticket.getPositionInQueue() != null)
                .anyMatch(t -> t.getPositionInQueue() < ticket.getPositionInQueue());
    }
    
    /**
     * Calcula la prioridad efectiva de un ticket basada en múltiples factores.
     */
    public int calculateTicketPriority(Ticket ticket, Customer customer) {
        if (ticket == null || customer == null) {
            return Integer.MAX_VALUE; // Menor prioridad
        }
        
        int basePriority = ticket.getQueueType().getPriorityOrder();
        
        // Ajustar por tiempo de espera (más tiempo = mayor prioridad)
        long waitingMinutes = ticket.getMinutesSinceCreation();
        int timeBonus = (int) (waitingMinutes / 30); // Bonus cada 30 minutos
        
        // Ajustar por tipo de cliente
        int customerBonus = customer.isVip() ? 10 : 0;
        
        return Math.max(1, basePriority - timeBonus - customerBonus);
    }
    
    /**
     * Determina si un ticket debe ser marcado como expirado.
     */
    public boolean shouldExpireTicket(Ticket ticket) {
        if (ticket == null || ticket.isFinished()) {
            return false;
        }
        
        return ticket.isExpired();
    }
    
    /**
     * Calcula métricas de rendimiento para un conjunto de tickets.
     */
    public TicketMetrics calculateTicketMetrics(List<Ticket> tickets, LocalDateTime startDate, LocalDateTime endDate) {
        if (tickets == null || tickets.isEmpty()) {
            return new TicketMetrics(0, 0, 0, 0.0, 0.0);
        }
        
        List<Ticket> periodTickets = tickets.stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate) && t.getCreatedAt().isBefore(endDate))
                .toList();
        
        int totalTickets = periodTickets.size();
        long completedTickets = periodTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.COMPLETED)
                .count();
        long cancelledTickets = periodTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED || t.getStatus() == TicketStatus.NO_SHOW)
                .count();
        
        double averageServiceTime = periodTickets.stream()
                .filter(t -> t.getServiceTimeMinutes() != null)
                .mapToLong(Ticket::getServiceTimeMinutes)
                .average()
                .orElse(0.0);
        
        double completionRate = totalTickets > 0 ? (double) completedTickets / totalTickets * 100 : 0.0;
        
        return new TicketMetrics(totalTickets, (int) completedTickets, (int) cancelledTickets, 
                               averageServiceTime, completionRate);
    }
    
    /**
     * Verifica si un cliente puede crear un nuevo ticket.
     */
    public boolean canCustomerCreateNewTicket(Customer customer, List<Ticket> customerActiveTickets) {
        if (customer == null) {
            return false;
        }
        
        // Los clientes VIP pueden tener hasta 2 tickets activos
        int maxActiveTickets = customer.isVip() ? 2 : 1;
        
        long activeTicketCount = customerActiveTickets != null ? 
                customerActiveTickets.stream()
                        .filter(Ticket::isActive)
                        .count() : 0;
        
        return activeTicketCount < maxActiveTickets;
    }
    
    /**
     * Record para métricas de tickets.
     */
    public record TicketMetrics(
            int totalTickets,
            int completedTickets,
            int cancelledTickets,
            double averageServiceTimeMinutes,
            double completionRatePercentage
    ) {}
}