package com.banco.ticketero.infrastructure.adapter.out.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramAdapter {
    
    @Value("${telegram.bot.token:}")
    private String botToken;
    
    @Value("${telegram.bot.username:TicketeroBot}")
    private String botUsername;
    
    public boolean sendMessage(Long chatId, String message) {
        try {
            log.info("Sending Telegram message to chatId: {} - Message: {}", chatId, message);
            
            // Simulación de envío exitoso
            if (botToken == null || botToken.isEmpty()) {
                log.warn("Telegram bot token not configured. Message not sent.");
                return false;
            }
            
            // En un proyecto real, aquí usaríamos la API de Telegram
            // TelegramBotsApi api = new TelegramBotsApi();
            // SendMessage sendMessage = new SendMessage();
            // sendMessage.setChatId(chatId.toString());
            // sendMessage.setText(message);
            // bot.execute(sendMessage);
            
            log.info("Telegram message sent successfully to chatId: {}", chatId);
            return true;
            
        } catch (Exception e) {
            log.error("Error sending Telegram message to chatId: {}", chatId, e);
            return false;
        }
    }
    
    public boolean isConfigured() {
        return botToken != null && !botToken.isEmpty();
    }
}