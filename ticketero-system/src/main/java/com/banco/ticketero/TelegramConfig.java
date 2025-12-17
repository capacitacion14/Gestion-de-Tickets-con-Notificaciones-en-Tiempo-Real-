package com.banco.ticketero;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramConfig {

    private final SimpleTicketBotInMemory simpleTicketBot;

    @PostConstruct
    public void init() {
        String token = simpleTicketBot.getBotToken();
        String username = simpleTicketBot.getBotUsername();
        
        log.info("🔍 Iniciando configuración Telegram - Token: {}, Username: {}", 
            token != null && !token.isEmpty() ? "CONFIGURADO" : "NO CONFIGURADO",
            username != null && !username.isEmpty() ? username : "NO CONFIGURADO");
        
        if (token == null || token.isEmpty() || username == null || username.isEmpty()) {
            log.warn("⚠️ Bot de Telegram NO configurado - Token o Username vacío. Bot funcionará en modo deshabilitado.");
            return;
        }
        
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(simpleTicketBot);
            log.info("🤖 Bot de Telegram registrado exitosamente: {}", username);
        } catch (TelegramApiException e) {
            log.error("❌ Error registrando bot de Telegram: {}", e.getMessage());
            log.error("💡 Verifica que el token y username sean correctos en el archivo .env");
        }
    }
}