package com.banco.ticketero.service;

import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.repository.OutboxMessageRepository;
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
                    log.error("❌ Notification failed after 3 attempts: {}", message.getId());
                } else {
                    log.warn("⚠️ Notification attempt {} failed", message.getIntentos());
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
        return switch (message.getPlantilla()) {
            case "CONFIRMACION" -> """
                ✅ Ticket creado exitosamente
                
                Recibirás notificaciones cuando sea tu turno.
                
                🔔 Te avisaremos:
                • Cuando falten pocos minutos
                • Cuando sea tu turno
                
                Usa /status para ver el estado.
                """;
            case "PROXIMO" -> """
                🔔 ¡ATENCIÓN!
                
                Eres el siguiente en la cola.
                Prepárate para ser atendido.
                
                📍 Dirígete al área de espera.
                """;
            case "TU_TURNO" -> """
                🎫 ¡ES TU TURNO!
                
                Dirígete al MÓDULO DE ATENCIÓN AHORA.
                
                🏢 Un ejecutivo te está esperando.
                ⏱️ No hagas esperar.
                """;
            default -> "Notificación del sistema de tickets";
        };
    }
}
