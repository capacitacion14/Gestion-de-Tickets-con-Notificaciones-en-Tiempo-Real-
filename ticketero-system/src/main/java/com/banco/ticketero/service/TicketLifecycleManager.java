package com.banco.ticketero.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class TicketLifecycleManager {

    private final AtomicInteger ticketsProcesados = new AtomicInteger(0);
    private final AtomicInteger ticketsVencidos = new AtomicInteger(0);

    @Scheduled(fixedDelay = 60000) // Cada 60 segundos
    public void cancelExpiredTickets() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulación del proceso de cancelación
            int procesados = ticketsProcesados.incrementAndGet();
            int vencidos = (int) (Math.random() * 3); // 0-2 tickets vencidos por ciclo
            
            if (vencidos > 0) {
                ticketsVencidos.addAndGet(vencidos);
                log.info("🔄 Scheduler ejecutado: {} tickets procesados, {} vencidos", 
                    procesados, vencidos);
            }
            
        } catch (Exception e) {
            log.error("❌ Error en scheduler de cancelación", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("⏱️ Scheduler cancelación completado en {}ms", duration);
        }
    }

    @Scheduled(fixedDelay = 30000) // Cada 30 segundos
    public void processNotifications() {
        try {
            // Simulación del proceso de notificaciones
            log.debug("📱 Procesando notificaciones progresivas...");
            
            // Aquí iría la lógica de:
            // 1. Recalcular posiciones
            // 2. Verificar umbrales (15min, 5min, 3pos)
            // 3. Enviar notificaciones pendientes
            
        } catch (Exception e) {
            log.error("❌ Error en scheduler de notificaciones", e);
        }
    }

    public SchedulerStats getStats() {
        return new SchedulerStats(
            ticketsProcesados.get(),
            ticketsVencidos.get(),
            LocalDateTime.now()
        );
    }

    public record SchedulerStats(
        int ticketsProcesados,
        int ticketsVencidos,
        LocalDateTime ultimaEjecucion
    ) {}
}