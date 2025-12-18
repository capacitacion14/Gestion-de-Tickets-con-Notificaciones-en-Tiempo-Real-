package com.banco.ticketero.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 50)
    private String plantilla;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false, length = 20)
    private MessageStatus estadoEnvio;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "telegram_message_id", length = 50)
    private String telegramMessageId;

    @Builder.Default
    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "chat_id", length = 50)
    private String chatId;

    public enum MessageStatus {
        PENDING, SENT, FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (fechaProgramada == null) {
            fechaProgramada = LocalDateTime.now();
        }
    }
}
