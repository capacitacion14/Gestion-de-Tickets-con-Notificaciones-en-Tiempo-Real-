package com.banco.ticketero.controller;

import com.banco.ticketero.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> update) {
        log.info("📨 Webhook recibido: {}", update);
        
        try {
            telegramBotService.processUpdate(update);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK"); // Telegram requiere 200 OK siempre
        }
    }

    @GetMapping("/webhook/info")
    public ResponseEntity<Map<String, Object>> getWebhookInfo() {
        return ResponseEntity.ok(telegramBotService.getWebhookInfo());
    }

    @PostMapping("/webhook/setup")
    public ResponseEntity<Map<String, Object>> setupWebhook(@RequestParam String webhookUrl) {
        log.info("🔧 Configurando webhook: {}", webhookUrl);
        return ResponseEntity.ok(telegramBotService.setupWebhook(webhookUrl));
    }
}