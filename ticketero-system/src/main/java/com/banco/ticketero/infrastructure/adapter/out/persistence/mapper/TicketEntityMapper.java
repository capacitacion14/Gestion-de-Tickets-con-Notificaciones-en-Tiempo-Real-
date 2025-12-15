package com.banco.ticketero.infrastructure.adapter.out.persistence.mapper;

import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.*;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.QueueEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.springframework.stereotype.Component;

@Component
public class TicketEntityMapper {
    
    public Ticket toDomain(TicketEntity entity) {
        if (entity == null) return null;
        
        return Ticket.builder()
                .id(TicketId.of(entity.getId()))
                .ticketCode(TicketCode.of(entity.getTicketCode()))
                .customerId(CustomerId.of(entity.getCustomerId()))
                .queueType(mapQueueType(entity.getQueueType()))
                .status(mapTicketStatus(entity.getStatus()))
                .positionInQueue(entity.getPositionInQueue())
                .estimatedWaitTime(entity.getEstimatedWaitTime())
                .calledAt(entity.getCalledAt())
                .completedAt(entity.getCompletedAt())
                .cancelledAt(entity.getCancelledAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    public TicketEntity toEntity(Ticket domain) {
        if (domain == null) return null;
        
        return TicketEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .ticketCode(domain.getTicketCode().getValue())
                .customerId(domain.getCustomerId().getValue())
                .queueType(mapQueueTypeEnum(domain.getQueueType()))
                .status(mapTicketStatusEnum(domain.getStatus()))
                .positionInQueue(domain.getPositionInQueue())
                .estimatedWaitTime(domain.getEstimatedWaitTime())
                .calledAt(domain.getCalledAt())
                .completedAt(domain.getCompletedAt())
                .cancelledAt(domain.getCancelledAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
    
    private QueueType mapQueueType(QueueEntity.QueueTypeEnum entityEnum) {
        return QueueType.valueOf(entityEnum.name());
    }
    
    private QueueEntity.QueueTypeEnum mapQueueTypeEnum(QueueType domainEnum) {
        return QueueEntity.QueueTypeEnum.valueOf(domainEnum.name());
    }
    
    private TicketStatus mapTicketStatus(TicketEntity.TicketStatusEnum entityEnum) {
        return TicketStatus.valueOf(entityEnum.name());
    }
    
    private TicketEntity.TicketStatusEnum mapTicketStatusEnum(TicketStatus domainEnum) {
        return TicketEntity.TicketStatusEnum.valueOf(domainEnum.name());
    }
}