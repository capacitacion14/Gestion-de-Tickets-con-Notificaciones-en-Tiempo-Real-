package com.banco.ticketero.application.port.out;

/**
 * Puerto de salida para integración con Telegram.
 * Define las operaciones de comunicación con la API de Telegram.
 */
public interface TelegramPort {
    
    /**
     * Envía un mensaje a un chat específico de Telegram.
     */
    Long sendMessage(Long chatId, String message);
    
    /**
     * Envía un mensaje con botones inline.
     */
    Long sendMessageWithButtons(Long chatId, String message, String[][] buttons);
    
    /**
     * Edita un mensaje existente.
     */
    void editMessage(Long chatId, Long messageId, String newMessage);
    
    /**
     * Elimina un mensaje.
     */
    void deleteMessage(Long chatId, Long messageId);
    
    /**
     * Verifica si un chat ID es válido.
     */
    boolean isChatIdValid(Long chatId);
    
    /**
     * Obtiene información del chat.
     */
    TelegramChatInfo getChatInfo(Long chatId);
    
    /**
     * Record para información del chat de Telegram.
     */
    record TelegramChatInfo(
        Long chatId,
        String firstName,
        String lastName,
        String username,
        boolean isActive
    ) {}
}