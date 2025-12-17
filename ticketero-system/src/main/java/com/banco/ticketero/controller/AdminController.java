package com.banco.ticketero.controller;

import com.banco.ticketero.service.TicketLifecycleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final TicketLifecycleManager lifecycleManager;

    @GetMapping("/scheduler/status")
    public ResponseEntity<TicketLifecycleManager.SchedulerStats> getSchedulerStatus() {
        return ResponseEntity.ok(lifecycleManager.getStats());
    }

    @PostMapping("/scheduler/run")
    public ResponseEntity<Map<String, Object>> runSchedulerManually() {
        log.info("🔧 Ejecutando scheduler manualmente desde admin");
        
        try {
            lifecycleManager.cancelExpiredTickets();
            lifecycleManager.processNotifications();
            
            return ResponseEntity.ok(Map.of(
                "message", "Scheduler ejecutado exitosamente",
                "timestamp", LocalDateTime.now(),
                "success", true
            ));
        } catch (Exception e) {
            log.error("❌ Error ejecutando scheduler manualmente", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Error ejecutando scheduler: " + e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "success", false
            ));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        // Simulación de dashboard básico
        return ResponseEntity.ok(Map.of(
            "ticketsActivos", 15,
            "ticketsVencidos", 3,
            "schedulerStats", lifecycleManager.getStats(),
            "lastUpdated", LocalDateTime.now()
        ));
    }
}