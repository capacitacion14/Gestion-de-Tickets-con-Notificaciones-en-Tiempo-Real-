package com.banco.ticketero.infrastructure.adapter.out.persistence;

import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.repository.QueueRepository;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.QueueEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.repository.QueueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QueueRepositoryAdapter implements QueueRepository {
    
    private final QueueJpaRepository jpaRepository;
    
    @Override
    public Queue save(Queue queue) {
        QueueEntity entity = toEntity(queue);
        QueueEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<Queue> findById(QueueId queueId) {
        return jpaRepository.findById(queueId.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public Optional<Queue> findByQueueType(QueueType queueType) {
        QueueEntity.QueueTypeEnum entityType = QueueEntity.QueueTypeEnum.valueOf(queueType.name());
        return jpaRepository.findByQueueType(entityType)
                .map(this::toDomain);
    }
    
    @Override
    public List<Queue> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public List<Queue> findActiveQueuesOrderedByPriority() {
        return findAll().stream()
                .filter(Queue::isActive)
                .sorted((q1, q2) -> q1.getQueueType().compareTo(q2.getQueueType()))
                .toList();
    }
    
    @Override
    public boolean existsByQueueType(QueueType queueType) {
        QueueEntity.QueueTypeEnum entityType = QueueEntity.QueueTypeEnum.valueOf(queueType.name());
        return jpaRepository.existsByQueueType(entityType);
    }
    
    @Override
    public List<Queue> findHighPriorityActiveQueues() {
        return findAll().stream()
                .filter(Queue::isActive)
                .filter(queue -> queue.getQueueType() == QueueType.VIP || queue.getQueueType() == QueueType.BUSINESS)
                .toList();
    }
    
    @Override
    public long countActiveQueues() {
        return findAll().stream()
                .filter(Queue::isActive)
                .count();
    }
    
    @Override
    public void deleteById(QueueId queueId) {
        jpaRepository.deleteById(queueId.getValue());
    }
    
    private Queue toDomain(QueueEntity entity) {
        return Queue.builder()
                .id(QueueId.of(entity.getId()))
                .queueType(QueueType.valueOf(entity.getQueueType().name()))
                .maxCapacity(entity.getMaxCapacity())
                .estimatedTimeMinutes(entity.getEstimatedTimeMinutes())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    private QueueEntity toEntity(Queue domain) {
        return QueueEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .queueType(QueueEntity.QueueTypeEnum.valueOf(domain.getQueueType().name()))
                .maxCapacity(domain.getMaxCapacity())
                .estimatedTimeMinutes(domain.getEstimatedTimeMinutes())
                .isActive(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}