package com.banco.ticketero.infrastructure.adapter.out.persistence.repository;

import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.QueueEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {
    
    Optional<TicketEntity> findByTicketCode(String ticketCode);
    
    List<TicketEntity> findByCustomerId(UUID customerId);
    
    List<TicketEntity> findByStatus(TicketEntity.TicketStatusEnum status);
    
    List<TicketEntity> findByQueueTypeAndStatus(QueueEntity.QueueTypeEnum queueType, TicketEntity.TicketStatusEnum status);
    
    @Query("SELECT t FROM TicketEntity t WHERE t.queueType = :queueType AND t.status = 'PENDING' ORDER BY t.positionInQueue")
    List<TicketEntity> findPendingTicketsByQueueTypeOrderedByPosition(@Param("queueType") QueueEntity.QueueTypeEnum queueType);
    
    long countByStatus(TicketEntity.TicketStatusEnum status);
    
    long countByQueueTypeAndStatus(QueueEntity.QueueTypeEnum queueType, TicketEntity.TicketStatusEnum status);
    
    boolean existsByTicketCode(String ticketCode);
}