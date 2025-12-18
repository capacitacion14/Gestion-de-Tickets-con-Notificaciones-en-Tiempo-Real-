package com.banco.ticketero.service;

import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.model.QueueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotService {

    private final TicketService ticketService;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.api-url:https://api.telegram.org}")
    private String apiUrl;

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^\\d{7,8}$");

    public void processUpdate(Map<String, Object> update) {
        log.debug("🔍 Procesando update: {}", update);

        if (!update.containsKey("message")) {
            log.debug("Update sin mensaje, ignorando");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        
        if (!message.containsKey("text")) {
            log.debug("Mensaje sin texto, ignorando");
            return;
        }

        String text = (String) message.get("text");
        @SuppressWarnings("unchecked")
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        String chatId = chat.get("id").toString();

        log.info("📱 Mensaje recibido de chat {}: {}", chatId, text);

        processCommand(chatId, text.trim());
    }

    private void processCommand(String chatId, String text) {
        try {
            if (text.startsWith("/start")) {
                sendWelcomeMessage(chatId);
            } else if (text.startsWith("/help")) {
                sendHelpMessage(chatId);
            } else if (text.startsWith("/status")) {
                sendTicketStatus(chatId);
            } else if (text.startsWith("/ticket")) {
                handleTicketCommand(chatId, text);
            } else if (NATIONAL_ID_PATTERN.matcher(text).matches()) {
                // Si es solo un número de cédula, crear ticket en cola CAJA por defecto
                createTicketFromNationalId(chatId, text, QueueType.CAJA);
            } else if (text.matches("^\\d+$")) {
                // Es un número pero no cumple el patrón de cédula
                sendInvalidCedulaMessage(chatId, text);
            } else {
                sendUnknownCommandMessage(chatId);
            }
        } catch (Exception e) {
            log.error("❌ Error procesando comando '{}' para chat {}: {}", text, chatId, e.getMessage(), e);
            sendErrorMessage(chatId);
        }
    }

    private void handleTicketCommand(String chatId, String text) {
        // Formato: /ticket 12345678 CAJA
        String[] parts = text.split("\\s+");
        
        if (parts.length < 2) {
            sendMessage(chatId, "❌ Formato incorrecto. Usa: /ticket 12345678 [CAJA|PERSONAL_BANKER|EMPRESAS|GERENCIA]");
            return;
        }

        String nationalId = parts[1];
        if (!NATIONAL_ID_PATTERN.matcher(nationalId).matches()) {
            sendMessage(chatId, "❌ Cédula inválida. Debe tener 7-8 dígitos.");
            return;
        }

        QueueType queueType = QueueType.CAJA; // Por defecto
        if (parts.length >= 3) {
            try {
                queueType = QueueType.valueOf(parts[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMessage(chatId, "❌ Cola inválida. Opciones: CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA");
                return;
            }
        }

        createTicketFromNationalId(chatId, nationalId, queueType);
    }

    private void createTicketFromNationalId(String chatId, String nationalId, QueueType queueType) {
        try {
            TicketResponse ticket = ticketService.createFromTelegram(nationalId, chatId, queueType);
            log.info("✅ Ticket creado via Telegram - Número: {}, Chat: {}", ticket.numero(), chatId);
            
            String confirmationMessage = String.format("""
                ✅ Ticket creado exitosamente
                
                🎫 Número: %s
                🆔 Código: %s
                👤 Cédula: %s
                🏢 Cola: %s
                ⏰ Creado: %s
                ⏱️ Tiempo estimado: %d minutos
                
                Recibirás notificaciones cuando sea tu turno.
                """,
                ticket.numero(),
                ticket.codigoReferencia(),
                ticket.nationalId(),
                ticket.queueType().name(),
                ticket.createdAt().toString().replace('T', ' ').substring(0, 19),
                ticket.estimatedWaitMinutes()
            );
            
            sendMessage(chatId, confirmationMessage);

        } catch (Exception e) {
            log.error("❌ Error creando ticket para cédula {} en chat {}: {}", nationalId, chatId, e.getMessage(), e);
            sendMessage(chatId, "❌ Error creando el ticket. Intenta nuevamente o contacta soporte.");
        }
    }

    private void sendWelcomeMessage(String chatId) {
        String message = """
            🤖 ¡Bienvenido al Sistema de Tickets!
            
            Puedes crear tickets de las siguientes formas:
            
            📝 Envía tu cédula:
            12345678
            
            📝 O usa el comando completo:
            /ticket 12345678 CAJA
            
            🏢 Colas disponibles:
            • CAJA - Operaciones generales
            • PERSONAL_BANKER - Asesoría personalizada  
            • EMPRESAS - Clientes empresariales
            • GERENCIA - Atención gerencial
            
            ℹ️ Usa /help para más información
            """;
        
        sendMessage(chatId, message);
    }

    private void sendHelpMessage(String chatId) {
        String message = """
            📋 Comandos disponibles:
            
            /start - Mensaje de bienvenida
            /help - Esta ayuda
            /status - Estado de todos los tickets
            /ticket <cédula> [cola] - Crear ticket
            
            📝 Ejemplos:
            • 12345678 - Ticket en CAJA
            • /ticket 12345678 PERSONAL_BANKER
            • /ticket 87654321 EMPRESAS
            
            🏢 Colas: CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA
            
            ⚠️ Nota: Solo números de cédula de 7-8 dígitos
            """;
        
        sendMessage(chatId, message);
    }

    private void sendUnknownCommandMessage(String chatId) {
        sendMessage(chatId, "❓ Comando no reconocido. Usa /help para ver los comandos disponibles.");
    }

    private void sendInvalidCedulaMessage(String chatId, String invalidCedula) {
        String message = String.format("""
            ❌ Cédula inválida: %s
            
            📋 Formato correcto:
            • Debe tener entre 7 y 8 dígitos
            • Solo números, sin puntos ni espacios
            
            ✅ Ejemplos válidos:
            • 1234567
            • 12345678
            
            💡 Intenta nuevamente con el formato correcto.
            """, invalidCedula);
        
        sendMessage(chatId, message);
        log.warn("🚫 Cédula inválida recibida en chat {}: {}", chatId, invalidCedula);
    }

    private void sendErrorMessage(String chatId) {
        sendMessage(chatId, "❌ Ocurrió un error interno. Por favor intenta nuevamente.");
    }

    private void sendTicketStatus(String chatId) {
        try {
            var allTickets = ticketService.findAll();
            
            if (allTickets.isEmpty()) {
                sendMessage(chatId, "📋 No hay tickets en el sistema.");
                return;
            }

            // Debug: ver estados reales
            allTickets.forEach(t -> log.info("Ticket {}: status = '{}'", t.numero(), t.status().toString()));
            
            long enEspera = allTickets.stream().filter(t -> "EN_ESPERA".equals(t.status().toString())).count();
            long atendiendo = allTickets.stream().filter(t -> "ATENDIENDO".equals(t.status().toString())).count();
            long completado = allTickets.stream().filter(t -> "COMPLETADO".equals(t.status().toString())).count();
            
            String message = String.format("""
                📊 ESTADO DE TICKETS
                
                Total: %d tickets
                ⏳ En espera: %d
                🔄 Atendiendo: %d
                ✅ Completados: %d
                """, allTickets.size(), enEspera, atendiendo, completado);
            
            sendMessage(chatId, message);
            log.info("📊 Status enviado a chat {}: {} tickets", chatId, allTickets.size());
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo status para chat {}: {}", chatId, e.getMessage(), e);
            sendMessage(chatId, "❌ Error obteniendo el estado de los tickets. Intenta nuevamente.");
        }
    }

    private void sendMessage(String chatId, String text) {
        try {
            String url = String.format("%s/bot%s/sendMessage", apiUrl, botToken);
            
            Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "text", text
            );

            restTemplate.postForObject(url, payload, String.class);
            log.debug("📤 Mensaje enviado a chat {}: {}", chatId, text.substring(0, Math.min(50, text.length())));
            
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje a chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    public Map<String, Object> getWebhookInfo() {
        try {
            String url = String.format("%s/bot%s/getWebhookInfo", apiUrl, botToken);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("❌ Error obteniendo info del webhook: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    public Map<String, Object> setupWebhook(String webhookUrl) {
        try {
            String url = String.format("%s/bot%s/setWebhook", apiUrl, botToken);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("url", webhookUrl);
            payload.put("allowed_updates", new String[]{"message"});
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, payload, Map.class);
            
            log.info("🔧 Webhook configurado: {} -> {}", webhookUrl, response);
            return response;
            
        } catch (Exception e) {
            log.error("❌ Error configurando webhook: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }
}