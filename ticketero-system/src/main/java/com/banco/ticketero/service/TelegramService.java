package com.banco.ticketero.service;

import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.repository.OutboxMessageRepository;
import com.banco.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final TicketRepository ticketRepository;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.api-url:https://api.telegram.org}")
    private String apiUrl;

    @Scheduled(fixedDelay = 2000) // Cada 2 segundos
    @Transactional
    public void processPendingMessages() {
        List<OutboxMessage> pending = outboxMessageRepository
            .findByEstadoEnvioAndFechaProgramadaBefore(
                OutboxMessage.MessageStatus.PENDING,
                LocalDateTime.now()
            );
        
        if (!pending.isEmpty()) {
            log.info("📤 Processing {} pending messages", pending.size());
        }

        for (OutboxMessage message : pending) {
            try {
                sendMessageDirectly(message.getChatId(), buildMessageText(message));
                message.setEstadoEnvio(OutboxMessage.MessageStatus.SENT);
                message.setFechaEnvio(LocalDateTime.now());
                log.info("✅ Notification sent: {} to chat {}", message.getPlantilla(), message.getChatId());
            } catch (Exception e) {
                message.setIntentos(message.getIntentos() + 1);
                if (message.getIntentos() >= 3) {
                    message.setEstadoEnvio(OutboxMessage.MessageStatus.FAILED);
                    log.error("❌ Notification failed after 3 attempts: {} - Error: {}", message.getId(), e.getMessage());
                } else {
                    log.warn("⚠️ Notification attempt {} failed - Error: {}", message.getIntentos(), e.getMessage());
                }
            }
            outboxMessageRepository.save(message);
        }
    }

    private void sendMessageDirectly(String chatId, String text) {
        String url = String.format("%s/bot%s/sendMessage", apiUrl, botToken);
        
        java.util.Map<String, Object> payload = java.util.Map.of(
            "chat_id", chatId,
            "text", text
        );
        
        restTemplate.postForObject(url, payload, String.class);
    }

    private String buildMessageText(OutboxMessage message) {
        var ticketOpt = ticketRepository.findById(message.getTicketId());
        String ticketNumber = ticketOpt.map(ticket -> ticket.getNumero()).orElse("N/A");
        String advisorName = ticketOpt.map(ticket -> 
            ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : "N/A"
        ).orElse("N/A");
        String moduleNumber = ticketOpt.map(ticket -> 
            ticket.getAssignedModuleNumber() != null ? ticket.getAssignedModuleNumber().toString() : "N/A"
        ).orElse("N/A");
            
        return switch (message.getPlantilla()) {
            case "CONFIRMACION" -> """
                ✅ Ticket creado exitosamente
                
                Recibirás notificaciones cuando sea tu turno.
                
                🔔 Te avisaremos:
                • Cuando falten pocos minutos
                • Cuando sea tu turno
                
                Usa /status para ver el estado.
                """;
            case "ALERTA" -> String.format("""
                ⏰ ¡NOTIFICACIÓN!
                
                🎫 Ticket: %s
                Faltan pocos minutos para que seas atendido.
                
                🚨 Esté alerta y prepárate.
                📍 Dirígete al área de espera.
                """, ticketNumber);
            case "PROXIMO" -> String.format("""
                🔔 ¡ATENCIÓN!
                
                🎫 Ticket: %s
                Eres el siguiente en la cola.
                Prepárate para ser atendido.
                
                📍 Dirígete al área de espera.
                """, ticketNumber);
            case "TU_TURNO" -> String.format("""
                🎫 ¡ES TU TURNO!
                
                🎫 Ticket: %s
                👤 Ejecutivo: %s
                🏢 Módulo: %s
                
                Dirígete al MÓDULO DE ATENCIÓN AHORA.
                ⏱️ No hagas esperar.
                """, ticketNumber, advisorName, moduleNumber);
            default -> "Notificación del sistema de tickets";
        };
    }
}
