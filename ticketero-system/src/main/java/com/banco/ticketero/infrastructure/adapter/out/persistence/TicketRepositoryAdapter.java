package com.banco.ticketero.infrastructure.adapter.out.persistence;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.*;
import com.banco.ticketero.domain.repository.TicketRepository;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.QueueEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.mapper.TicketEntityMapper;
import com.banco.ticketero.infrastructure.adapter.out.persistence.repository.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {
    
    private final TicketJpaRepository jpaRepository;
    private final TicketEntityMapper mapper;
    
    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = mapper.toEntity(ticket);
        TicketEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Ticket> findById(TicketId ticketId) {
        return jpaRepository.findById(ticketId.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Ticket> findByTicketCode(TicketCode ticketCode) {
        return jpaRepository.findByTicketCode(ticketCode.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Ticket> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        TicketEntity.TicketStatusEnum entityStatus = TicketEntity.TicketStatusEnum.valueOf(status.name());
        return jpaRepository.findByStatus(entityStatus)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<Ticket> findByQueueTypeAndStatus(QueueType queueType, TicketStatus status) {
        QueueEntity.QueueTypeEnum entityQueueType = QueueEntity.QueueTypeEnum.valueOf(queueType.name());
        TicketEntity.TicketStatusEnum entityStatus = TicketEntity.TicketStatusEnum.valueOf(status.name());
        return jpaRepository.findByQueueTypeAndStatus(entityQueueType, entityStatus)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public List<Ticket> findPendingTicketsByQueueTypeOrderedByPosition(QueueType queueType) {
        QueueEntity.QueueTypeEnum entityQueueType = QueueEntity.QueueTypeEnum.valueOf(queueType.name());
        return jpaRepository.findPendingTicketsByQueueTypeOrderedByPosition(entityQueueType)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public Optional<Ticket> findNextTicketToCall(QueueType queueType) {
        return findPendingTicketsByQueueTypeOrderedByPosition(queueType)
                .stream()
                .findFirst();
    }
    
    @Override
    public List<Ticket> findActiveTicketsByCustomerId(CustomerId customerId) {
        return findByCustomerId(customerId)
                .stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING || 
                                ticket.getStatus() == TicketStatus.CALLED ||
                                ticket.getStatus() == TicketStatus.IN_PROGRESS)
                .toList();
    }
    
    @Override
    public List<Ticket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .filter(ticket -> ticket.getCreatedAt().isAfter(startDate) && 
                                ticket.getCreatedAt().isBefore(endDate))
                .toList();
    }
    
    @Override
    public long countByStatus(TicketStatus status) {
        TicketEntity.TicketStatusEnum entityStatus = TicketEntity.TicketStatusEnum.valueOf(status.name());
        return jpaRepository.countByStatus(entityStatus);
    }
    
    @Override
    public long countByQueueTypeAndStatus(QueueType queueType, TicketStatus status) {
        QueueEntity.QueueTypeEnum entityQueueType = QueueEntity.QueueTypeEnum.valueOf(queueType.name());
        TicketEntity.TicketStatusEnum entityStatus = TicketEntity.TicketStatusEnum.valueOf(status.name());
        return jpaRepository.countByQueueTypeAndStatus(entityQueueType, entityStatus);
    }
    
    @Override
    public List<Ticket> findExpiredTickets() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        return findByCreatedAtBetween(LocalDateTime.now().minusDays(1), twoHoursAgo);
    }
    
    @Override
    public boolean existsByTicketCode(TicketCode ticketCode) {
        return jpaRepository.existsByTicketCode(ticketCode.getValue());
    }
    
    @Override
    public Optional<Integer> findMaxPositionInQueue(QueueType queueType) {
        return findPendingTicketsByQueueTypeOrderedByPosition(queueType)
                .stream()
                .mapToInt(Ticket::getPositionInQueue)
                .max()
                .stream()
                .boxed()
                .findFirst();
    }
    
    @Override
    public void deleteById(TicketId ticketId) {
        jpaRepository.deleteById(ticketId.getValue());
    }
    
    @Override
    public List<Ticket> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}