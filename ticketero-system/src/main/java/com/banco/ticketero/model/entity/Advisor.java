package com.banco.ticketero.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "advisor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Advisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdvisorStatus status;

    @Column(name = "module_number", nullable = false)
    private Integer moduleNumber;

    @Column(name = "supported_queues", length = 500)
    private String supportedQueues;

    @Column(name = "assigned_tickets_count")
    @Builder.Default
    private Integer assignedTicketsCount = 0;

    @Column(name = "last_assignment_at")
    private LocalDateTime lastAssignmentAt;

    public enum AdvisorStatus {
        AVAILABLE, BUSY, OFFLINE, BREAK
    }
}
