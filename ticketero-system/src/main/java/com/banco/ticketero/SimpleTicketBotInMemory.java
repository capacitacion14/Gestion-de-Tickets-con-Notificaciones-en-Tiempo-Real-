package com.banco.ticketero;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@EnableScheduling
public class SimpleTicketBotInMemory extends TelegramLongPollingBot {

    private final AtomicInteger ticketCounter = new AtomicInteger(1);
    private final ConcurrentHashMap<String, TicketInfo> tickets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> vigenciaPorCola = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initVigencias() {
        vigenciaPorCola.put("GENERAL", 60);   // 60 minutos
        vigenciaPorCola.put("PRIORITY", 120); // 120 minutos  
        vigenciaPorCola.put("VIP", 180);      // 180 minutos
    }

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            
            log.info("📱 Mensaje recibido: {} desde chat: {}", messageText, chatId);
            
            if (messageText.startsWith("/")) {
                handleCommand(messageText, chatId);
            } else {
                handleTicketRequest(messageText, chatId);
            }
        }
    }

    private void handleCommand(String command, String chatId) {
        switch (command.toLowerCase()) {
            case "/start":
                sendMessage(chatId, """
                    🎫 ¡Bienvenido al Sistema Ticketero!
                    
                    Para solicitar un ticket, envía tu cédula seguida del tipo de cola:
                    
                    📝 Formato: [cédula] [tipo]
                    
                    Tipos disponibles:
                    • GENERAL - Cola general
                    • PRIORITY - Cola prioritaria  
                    • VIP - Cola VIP
                    
                    Ejemplo: 12345678 GENERAL
                    """);
                break;
            case "/help":
                sendMessage(chatId, """
                    📋 Ayuda - Sistema Ticketero
                    
                    Para crear un ticket:
                    [cédula] [tipo_cola]
                    
                    Ejemplo: 12345678 GENERAL
                    
                    Tipos de cola:
                    • GENERAL
                    • PRIORITY  
                    • VIP
                    """);
                break;
            case "/status":
                long ticketsPendientes = tickets.values().stream()
                    .filter(t -> t.status().equals("PENDING"))
                    .count();
                long ticketsAtendiendo = tickets.values().stream()
                    .filter(t -> t.status().equals("ATENDIENDO"))
                    .count();
                long ticketsCompletados = tickets.values().stream()
                    .filter(t -> t.status().equals("COMPLETED"))
                    .count();
                    
                sendMessage(chatId, String.format("""
                    📊 Estado del Sistema
                    
                    🎫 Tickets creados: %d
                    ⏳ En espera: %d
                    🔄 Atendiendo: %d
                    ✅ Completados: %d
                    💾 Total en memoria: %d
                    🕐 Hora actual: %s
                    
                    Vigencias por cola:
                    • GENERAL: %d min
                    • PRIORITY: %d min  
                    • VIP: %d min
                    """, 
                    ticketCounter.get() - 1,
                    ticketsPendientes,
                    ticketsAtendiendo,
                    ticketsCompletados,
                    tickets.size(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    vigenciaPorCola.get("GENERAL"),
                    vigenciaPorCola.get("PRIORITY"),
                    vigenciaPorCola.get("VIP")
                ));
                break;
            case "/check":
                processTicketQueue();
                sendMessage(chatId, "🔄 Procesamiento de cola ejecutado manualmente.");
                break;
            case "/notify":
                processNotifications();
                sendMessage(chatId, "📱 Notificaciones enviadas a todos los tickets activos.");
                break;
            case "/clear":
                tickets.clear();
                sendMessage(chatId, "🗑️ Todos los tickets han sido eliminados de la memoria.");
                break;
            default:
                sendMessage(chatId, "❌ Comando no reconocido. Usa /help para ver los comandos disponibles.");
        }
    }

    private void handleTicketRequest(String message, String chatId) {
        try {
            String[] parts = message.trim().split("\\s+");
            
            if (parts.length != 2) {
                sendMessage(chatId, """
                    ❌ Formato incorrecto
                    
                    Usa: [cédula] [tipo_cola]
                    Ejemplo: 12345678 GENERAL
                    
                    Tipos: GENERAL, PRIORITY, VIP
                    """);
                return;
            }

            String nationalId = parts[0];
            String queueType = parts[1].toUpperCase();

            if (!nationalId.matches("\\d{7,10}")) {
                sendMessage(chatId, "❌ Cédula inválida. Debe tener entre 7 y 10 dígitos.");
                return;
            }

            if (!queueType.matches("GENERAL|PRIORITY|VIP")) {
                sendMessage(chatId, "❌ Tipo de cola inválido. Usa: GENERAL, PRIORITY o VIP");
                return;
            }

            // Generar ticket en memoria
            int ticketNumber = ticketCounter.getAndIncrement();
            String ticketCode = String.format("%s%03d", queueType.charAt(0), ticketNumber);
            int position = (int) (Math.random() * 10) + 1;
            int estimatedTime = position * 5;
            
            // Calcular vigencia y expiración
            int vigenciaMinutos = vigenciaPorCola.get(queueType);
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(vigenciaMinutos);
            
            TicketInfo ticket = new TicketInfo(
                ticketCode, nationalId, queueType, "PENDING", 
                position, estimatedTime, chatId, LocalDateTime.now(),
                vigenciaMinutos, expiresAt, false, null
            );
            
            tickets.put(ticketCode, ticket);
            
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            sendMessage(chatId, String.format("""
                ✅ Ticket creado exitosamente
                
                🎫 Código: %s
                👤 Cédula: %s
                📋 Cola: %s
                📍 Posición: #%d
                ⏱️ Tiempo estimado: %d minutos
                ⏰ Vence en: %d minutos
                📅 Fecha: %s
                💾 Almacenado en memoria
                
                💬 Chat ID: %s
                
                Te notificaremos cuando estés próximo.
                Usa /status para ver estadísticas del sistema.
                """, 
                ticketCode, nationalId, queueType, position, estimatedTime, vigenciaMinutos, currentTime, chatId
            ));

            log.info("🎫 Ticket creado en memoria: Código={}, Cédula={}, Cola={}", 
                ticketCode, nationalId, queueType);

        } catch (Exception e) {
            log.error("❌ Error creando ticket via Telegram", e);
            sendMessage(chatId, "❌ Error al crear el ticket. Intenta nuevamente.");
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        
        try {
            execute(message);
            log.info("📤 Mensaje enviado a chat: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("❌ Error enviando mensaje a chat: {}", chatId, e);
        }
    }

    @Scheduled(fixedDelay = 10000) // Cada 10 segundos
    public void processTicketQueue() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. Completar tickets que llevan 30+ segundos ATENDIENDO
        tickets.entrySet().forEach(entry -> {
            TicketInfo ticket = entry.getValue();
            if (ticket.status().equals("ATENDIENDO") && ticket.atendiendoDesde() != null) {
                long segundosAtendiendo = java.time.Duration.between(ticket.atendiendoDesde(), now).getSeconds();
                if (segundosAtendiendo >= 30) {
                    // Completar ticket
                    TicketInfo completedTicket = new TicketInfo(
                        ticket.codigo(), ticket.nationalId(), ticket.queueType(),
                        "COMPLETED", 0, 0, ticket.chatId(),
                        ticket.createdAt(), ticket.vigenciaMinutos(), ticket.expiresAt(), 
                        false, null
                    );
                    
                    tickets.put(entry.getKey(), completedTicket);
                    
                    sendMessage(ticket.chatId(), String.format("""
                        ✅ Atención Completada
                        
                        Tu ticket %s ha sido atendido exitosamente.
                        
                        Gracias por usar nuestro sistema.
                        """, ticket.codigo()));
                    
                    log.info("✅ Ticket completado: {}", ticket.codigo());
                }
            }
        });
        
        // 2. Asignar siguiente ticket PENDING a ATENDIENDO
        boolean hayAtendiendo = tickets.values().stream()
            .anyMatch(t -> t.status().equals("ATENDIENDO"));
            
        if (!hayAtendiendo) {
            TicketInfo nextTicket = tickets.values().stream()
                .filter(t -> t.status().equals("PENDING"))
                .min((t1, t2) -> t1.createdAt().compareTo(t2.createdAt()))
                .orElse(null);
                
            if (nextTicket != null) {
                TicketInfo atendiendoTicket = new TicketInfo(
                    nextTicket.codigo(), nextTicket.nationalId(), nextTicket.queueType(),
                    "ATENDIENDO", 1, 0, nextTicket.chatId(),
                    nextTicket.createdAt(), nextTicket.vigenciaMinutos(), nextTicket.expiresAt(),
                    false, now
                );
                
                tickets.put(nextTicket.codigo(), atendiendoTicket);
                
                sendMessage(nextTicket.chatId(), String.format("""
                    🔔 ¡ES TU TURNO %s!
                    
                    Dirígete al módulo: 1
                    Asesor: María González
                    
                    Tu atención comenzó ahora (30 segundos).
                    """, nextTicket.codigo()));
                
                log.info("🔔 Ticket pasó a ATENDIENDO: {}", nextTicket.codigo());
            }
        }
    }
    
    @Scheduled(fixedDelay = 30000) // Cada 30 segundos  
    public void processNotifications() {
        int notificacionesEnviadas = 0;
        
        // Calcular posiciones en tiempo real
        java.util.List<TicketInfo> ticketsPendientes = tickets.values().stream()
            .filter(t -> t.status().equals("PENDING"))
            .sorted((t1, t2) -> t1.createdAt().compareTo(t2.createdAt()))
            .toList();
        
        // ENVIAR NOTIFICACIONES CON POSICIÓN ACTUALIZADA
        for (int i = 0; i < ticketsPendientes.size(); i++) {
            TicketInfo ticket = ticketsPendientes.get(i);
            int posicion = i + 1;
            int tiempoEstimado = posicion * 5;
            
            sendMessage(ticket.chatId(), String.format("""
                📊 Estado de tu ticket %s
                
                🎫 Código: %s
                📍 Posición en cola: #%d
                ⏱️ Tiempo estimado: %d minutos
                ⏰ Vence en: %d minutos
                📋 Cola: %s
                
                %s
                """, 
                ticket.codigo(),
                ticket.codigo(),
                posicion,
                tiempoEstimado,
                ticket.vigenciaMinutos(),
                ticket.queueType(),
                posicion == 1 ? "🔥 ¡Eres el siguiente!" : "Te mantendremos informado."));
            
            notificacionesEnviadas++;
        }
        
        if (notificacionesEnviadas > 0) {
            log.info("📱 Notificaciones enviadas: {} (posiciones actualizadas)", notificacionesEnviadas);
        }
    }

    // Clase interna para almacenar información del ticket
    private record TicketInfo(
        String codigo,
        String nationalId,
        String queueType,
        String status,
        int position,
        int estimatedTime,
        String chatId,
        LocalDateTime createdAt,
        int vigenciaMinutos,
        LocalDateTime expiresAt,
        boolean isExpired,
        LocalDateTime atendiendoDesde
    ) {}
}