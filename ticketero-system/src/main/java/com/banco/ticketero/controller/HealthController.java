package com.banco.ticketero.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    @Value("${telegram.bot.token:NOT_SET}")
    private String botToken;

    @Value("${telegram.bot.username:NOT_SET}")
    private String botUsername;

    @GetMapping("/telegram")
    public Map<String, Object> checkTelegramConnection() {
        boolean tokenConfigured = !botToken.equals("NOT_SET") && !botToken.isEmpty();
        boolean usernameConfigured = !botUsername.equals("NOT_SET") && !botUsername.isEmpty();
        
        log.info("🔍 Verificando configuración Telegram - Token: {}, Username: {}", 
            tokenConfigured ? "CONFIGURADO" : "NO CONFIGURADO",
            usernameConfigured ? "CONFIGURADO" : "NO CONFIGURADO");

        return Map.of(
            "telegram_status", tokenConfigured && usernameConfigured ? "CONFIGURED" : "NOT_CONFIGURED",
            "token_configured", tokenConfigured,
            "username_configured", usernameConfigured,
            "bot_username", usernameConfigured ? botUsername : "NOT_SET",
            "timestamp", LocalDateTime.now()
        );
    }
}