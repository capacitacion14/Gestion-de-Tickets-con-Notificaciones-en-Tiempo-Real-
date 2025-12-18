package com.banco.ticketero.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramPollingService {

    private final TelegramBotService telegramBotService;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.api-url:https://api.telegram.org}")
    private String apiUrl;

    private long lastUpdateId = 0;

    @Scheduled(fixedDelay = 2000) // Poll cada 2 segundos
    public void pollUpdates() {
        try {
            String url = String.format("%s/bot%s/getUpdates?offset=%d&timeout=1", 
                apiUrl, botToken, lastUpdateId + 1);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("ok"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
                
                for (Map<String, Object> update : updates) {
                    processUpdate(update);
                }
            }
        } catch (Exception e) {
            log.error("Error polling Telegram updates: {}", e.getMessage());
        }
    }

    private void processUpdate(Map<String, Object> update) {
        try {
            Object updateIdObj = update.get("update_id");
            if (updateIdObj instanceof Number) {
                long updateId = ((Number) updateIdObj).longValue();
                if (updateId > lastUpdateId) {
                    lastUpdateId = updateId;
                }
            }
            
            telegramBotService.processUpdate(update);
            log.debug("Processed update: {}", update.get("update_id"));
            
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }
}