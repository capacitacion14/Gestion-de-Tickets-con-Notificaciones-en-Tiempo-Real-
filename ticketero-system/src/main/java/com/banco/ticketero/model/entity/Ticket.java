package com.banco.ticketero.model.entity;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @Column(name = "codigo_referencia")
    private UUID codigoReferencia;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "national_id", nullable = false, length = 50)
    private String nationalId;

    @Column(length = 20)
    private String telefono;

    @Column(name = "branch_office", nullable = false, length = 100)
    private String branchOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false, length = 20)
    private QueueType queueType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "position_in_queue")
    private Integer positionInQueue;

    @Column(name = "estimated_wait_minutes")
    private Integer estimatedWaitMinutes;

    @Column(name = "vigencia_minutos", nullable = false)
    private Integer vigenciaMinutos;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_advisor_id")
    @ToString.Exclude
    private Advisor assignedAdvisor;

    @Column(name = "assigned_module_number")
    private Integer assignedModuleNumber;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 50)
    private String cancelReason;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "proximo_notified", nullable = false)
    @Builder.Default
    private boolean proximoNotified = false;

    @PrePersist
    protected void onCreate() {
        if (codigoReferencia == null) {
            codigoReferencia = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (vigenciaMinutos == null && queueType != null) {
            vigenciaMinutos = queueType.getVigenciaMinutos();
        }
        if (expiresAt == null && createdAt != null && vigenciaMinutos != null) {
            expiresAt = createdAt.plusMinutes(vigenciaMinutos);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
