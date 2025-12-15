package com.banco.ticketero.infrastructure.adapter.out.persistence.repository;

import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.QueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QueueJpaRepository extends JpaRepository<QueueEntity, UUID> {
    
    Optional<QueueEntity> findByQueueType(QueueEntity.QueueTypeEnum queueType);
    
    boolean existsByQueueType(QueueEntity.QueueTypeEnum queueType);
}