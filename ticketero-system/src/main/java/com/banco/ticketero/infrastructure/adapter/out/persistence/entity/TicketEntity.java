package com.banco.ticketero.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets", schema = "ticketero")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "ticket_code", unique = true, nullable = false)
    private String ticketCode;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false)
    private QueueEntity.QueueTypeEnum queueType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatusEnum status;
    
    @Column(name = "position_in_queue")
    private Integer positionInQueue;
    
    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;
    
    @Column(name = "called_at")
    private LocalDateTime calledAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatusEnum.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum TicketStatusEnum {
        PENDING, CALLED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    }
}