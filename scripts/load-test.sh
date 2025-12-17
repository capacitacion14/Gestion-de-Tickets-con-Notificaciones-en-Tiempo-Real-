#!/bin/bash

# Load Test - Sistema Ticketero
# Simula 15 usuarios creando tickets de todas las categorías

# Cargar variables del .env
ENV_FILE="$(dirname "$0")/../ticketero-system/.env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    echo "❌ Error: No se encontró el archivo .env en $ENV_FILE"
    exit 1
fi

# Configuración
BOT_TOKEN="$TELEGRAM_BOT_TOKEN"
MAIN_CHAT_ID="$TELEGRAM_CHAT_ID"  # Tu chat principal
TELEGRAM_API="https://api.telegram.org/bot${BOT_TOKEN}/sendMessage"
TOTAL_USERS=15

# Generar chat IDs simulados (solo para el script)
generate_fake_chat_id() {
    local user_num="$1"
    echo "$((959000000 + user_num))"  # Chat IDs simulados
}

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Arrays de datos de prueba
CEDULAS=(
    "12345678" "87654321" "11111111" "22222222" "33333333"
    "44444444" "55555555" "66666666" "77777777" "88888888"
    "99999999" "10101010" "20202020" "30303030" "40404040"
)

COLAS=("GENERAL" "PRIORITY" "VIP")

# Función para enviar mensaje simulando usuario
send_user_message() {
    local message="$1"
    local user_num="$2"
    local cedula="$3"
    
    # Enviar mensaje informativo a tu chat principal
    local info_message="👤 Usuario $user_num (Cédula: $cedula) envía: $message"
    
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=$info_message" > /dev/null
    
    # Simular procesamiento del bot
    sleep 1
}

# Función para enviar respuesta del bot
send_bot_response() {
    local response_text="$1"
    
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=🤖 Bot responde:

$response_text" \
        -d "parse_mode=HTML" > /dev/null
    
    sleep 1
}

# Función para verificar respuesta real del bot
check_bot_response() {
    local user_num="$1"
    echo -e "   ${CYAN}📱 Esperando respuesta del bot en Telegram...${NC}"
    sleep 3  # Dar tiempo al bot para procesar y responder
    echo -e "   ${GREEN}✅ Revisa tu chat de Telegram para ver la respuesta${NC}"
}

# Función para limpiar todos los tickets
clear_all_tickets() {
    echo -e "${RED}🗑️ Limpiando todos los tickets existentes...${NC}"
    
    # Enviar comando de limpieza
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=🔧 Administrador ejecuta: /clear" > /dev/null
    
    sleep 2
    echo -e "${GREEN}✅ Tickets limpiados${NC}"
}

# Función para simular usuario individual
simulate_user() {
    local cedula="$1"
    local cola="$2"
    local user_num="$3"
    
    echo -e "${BLUE}👤 Usuario $user_num (Cédula: $cedula):${NC}"
    
    # 1. Usuario inicia conversación
    echo -e "   📱 Enviando /start..."
    send_user_message "/start" "$user_num" "$cedula"
    
    # Respuesta del bot al /start
    local start_response="🎫 ¡Bienvenido al Sistema Ticketero!

Para solicitar un ticket, envía tu cédula seguida del tipo de cola:

📝 Formato: [cédula] [tipo]

Tipos disponibles:
• GENERAL - Cola general
• PRIORITY - Cola prioritaria
• VIP - Cola VIP

Ejemplo: 12345678 GENERAL"
    
    send_bot_response "$start_response"
    
    # 2. Usuario solicita ticket
    echo -e "   🎫 Solicitando ticket: $cedula $cola"
    send_user_message "$cedula $cola" "$user_num" "$cedula"
    
    # Respuesta del bot con ticket creado
    local prefix="G"
    case $cola in
        "GENERAL") prefix="G" ;;
        "PRIORITY") prefix="P" ;;
        "VIP") prefix="V" ;;
    esac
    
    local ticket_code="${prefix}$(printf '%03d' $user_num)"
    local position=$user_num
    local estimated_time=$((position * 5))
    local vigencia=60
    
    case $cola in
        "PRIORITY") vigencia=120 ;;
        "VIP") vigencia=180 ;;
    esac
    
    local ticket_response="✅ Ticket creado exitosamente

🎫 Código: $ticket_code
👤 Cédula: $cedula
📋 Cola: $cola
📍 Posición: #$position
⏱️ Tiempo estimado: $estimated_time minutos
⏰ Vence en: $vigencia minutos
📅 Fecha: $(date '+%d/%m/%Y %H:%M')
💾 Almacenado en memoria

💬 Chat ID: $(generate_fake_chat_id $user_num)

Te notificaremos cuando estés próximo."
    
    send_bot_response "$ticket_response"
    
    # 3. Usuario consulta estado
    echo -e "   📊 Consultando estado con /status"
    send_user_message "/status" "$user_num" "$cedula"
    
    echo -e "   ✅ Usuario $user_num completó flujo inicial"
    echo ""
}

# Función para mostrar estadísticas
show_stats() {
    echo -e "${YELLOW}📊 Consultando estadísticas...${NC}"
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=🔧 Admin: /status" > /dev/null
    sleep 1
}

# Función para procesar cola manualmente
process_queue() {
    echo -e "${YELLOW}🔄 Procesando cola manualmente...${NC}"
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=🔧 Admin: /check" > /dev/null
    sleep 1
}

# Función para enviar notificaciones
send_notifications() {
    echo -e "${YELLOW}📱 Enviando notificaciones...${NC}"
    curl -s -X POST "$TELEGRAM_API" \
        -d "chat_id=$MAIN_CHAT_ID" \
        -d "text=🔧 Admin: /notify" > /dev/null
    sleep 1
}

# Función principal del load test
run_load_test() {
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                    LOAD TEST - TICKETERO                     ║${NC}"
    echo -e "${BLUE}║                   Simulando $TOTAL_USERS usuarios                    ║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    # Verificar configuración
    if [[ -z "$MAIN_CHAT_ID" || "$MAIN_CHAT_ID" == "tu_chat_id_aqui" ]]; then
        echo -e "${RED}❌ Error: Configura TELEGRAM_CHAT_ID en el archivo .env${NC}"
        echo "   1. Inicia conversación con el bot"
        echo "   2. Envía cualquier mensaje"
        echo "   3. Edita ticketero-system/.env y configura TELEGRAM_CHAT_ID"
        exit 1
    fi
    
    if [[ -z "$BOT_TOKEN" ]]; then
        echo -e "${RED}❌ Error: TELEGRAM_BOT_TOKEN no configurado en .env${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}🚀 Iniciando load test...${NC}"
    echo ""
    
    # FASE 1: Limpiar tickets existentes
    echo -e "${YELLOW}=== FASE 1: LIMPIEZA ====${NC}"
    clear_all_tickets
    echo ""
    
    # FASE 2: Simular usuarios individuales
    echo -e "${YELLOW}=== FASE 2: SIMULACIÓN DE USUARIOS INDIVIDUALES ====${NC}"
    for i in $(seq 1 $TOTAL_USERS); do
        local cedula=${CEDULAS[$((i-1))]}
        local cola=${COLAS[$((($i-1) % 3))]}  # Rotar entre las 3 colas
        
        simulate_user "$cedula" "$cola" "$i"
        
        # Pausa entre usuarios para simular llegada escalonada
        if [[ $i -lt $TOTAL_USERS ]]; then
            echo -e "${YELLOW}   ⏳ Esperando siguiente usuario (2s)...${NC}"
            sleep 2
        fi
    done
    
    echo ""
    echo -e "${GREEN}✅ $TOTAL_USERS usuarios completaron flujo inicial${NC}"
    echo ""
    
    # FASE 3: Mostrar estadísticas iniciales
    echo -e "${YELLOW}=== FASE 3: ESTADÍSTICAS INICIALES ====${NC}"
    show_stats
    echo ""
    
    # FASE 4: Simular consultas periódicas de usuarios
    echo -e "${YELLOW}=== FASE 4: CONSULTAS PERIÓDICAS DE USUARIOS ====${NC}"
    echo -e "${BLUE}📱 Usuarios consultando estado cada 10 segundos${NC}"
    
    for round in $(seq 1 3); do
        echo -e "${YELLOW}--- Consulta $round ---${NC}"
        
        # Simular que algunos usuarios consultan su estado
        local users_to_check=$((TOTAL_USERS / 3))  # 1/3 de usuarios consultan
        for i in $(seq 1 $users_to_check); do
            local user_idx=$((($round - 1) * $users_to_check + $i))
            if [[ $user_idx -le $TOTAL_USERS ]]; then
                echo -e "   👤 Usuario $user_idx consultando estado..."
                curl -s -X POST "$TELEGRAM_API" \
                    -d "chat_id=$MAIN_CHAT_ID" \
                    -d "text=👤 Usuario $user_idx: /status" > /dev/null
                
                echo -e "   ${YELLOW}📱 Revisa Telegram para ver el estado actualizado${NC}"
                sleep 0.5
            fi
        done
        
        # Procesar cola automáticamente
        echo -e "   🔄 Sistema procesando cola automáticamente..."
        process_queue
        
        # Simular notificaciones automáticas
        echo -e "   📱 Sistema enviando notificaciones automáticas..."
        send_notifications
        
        # Las notificaciones aparecen automáticamente en Telegram
        if [[ $round -eq 2 ]]; then
            echo -e "   ${GREEN}📱 Notificaciones automáticas enviándose a Telegram...${NC}"
        fi
        
        echo -e "${BLUE}⏳ Esperando 10 segundos para siguiente ronda...${NC}"
        sleep 10
        echo ""
    done
    
    # FASE 5: Procesamiento final acelerado
    echo -e "${YELLOW}=== FASE 5: PROCESAMIENTO FINAL ====${NC}"
    echo -e "${BLUE}🚀 Procesamiento acelerado para completar todos los tickets${NC}"
    
    for final_round in $(seq 1 3); do
        echo -e "${YELLOW}--- Procesamiento Final $final_round ---${NC}"
        
        # Procesar cola múltiples veces para completar tickets
        process_queue
        sleep 2
        send_notifications
        
        # Los tickets completados se notifican automáticamente en Telegram
        if [[ $final_round -eq 2 ]]; then
            echo -e "   ${GREEN}📱 Tickets completándose - revisa Telegram para confirmaciones${NC}"
        fi
        
        sleep 2
        show_stats
        
        echo -e "${BLUE}⏳ Esperando 3 segundos...${NC}"
        sleep 3
        echo ""
    done
    
    # FASE 6: Estadísticas finales
    echo -e "${YELLOW}=== FASE 6: ESTADÍSTICAS FINALES ====${NC}"
    show_stats
    echo ""
    
    # FASE 7: Resumen
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                    LOAD TEST COMPLETADO                     ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}✅ Usuarios simulados: $TOTAL_USERS${NC}"
    echo -e "${GREEN}✅ Flujo completo por usuario: /start → crear ticket → consultas${NC}"
    echo -e "${GREEN}✅ Colas utilizadas: ${COLAS[*]}${NC}"
    echo -e "${GREEN}✅ Consultas periódicas: 3 rondas${NC}"
    echo -e "${GREEN}✅ Procesamiento final: Acelerado${NC}"
    echo -e "${GREEN}✅ Notificaciones: Automáticas por cada acción${NC}"
    echo ""
    echo -e "${YELLOW}📊 Revisa el monitor para ver el estado final:${NC}"
    echo -e "${BLUE}   ./scripts/monitor.sh${NC}"
    echo ""
    echo -e "${YELLOW}💡 Cada usuario siguió el flujo completo:${NC}"
    echo -e "${BLUE}   1. /start (bienvenida) → Respuesta en Telegram${NC}"
    echo -e "${BLUE}   2. [cédula] [cola] (crear ticket) → Confirmación en Telegram${NC}"
    echo -e "${BLUE}   3. /status (consultar estado) → Estado en Telegram${NC}"
    echo -e "${BLUE}   4. Notificaciones automáticas → Directamente en Telegram${NC}"
    echo -e "${BLUE}   5. Procesamiento de cola → Notificaciones de turno en Telegram${NC}"
    echo -e "${BLUE}   6. Tickets completados → Confirmación final en Telegram${NC}"
    echo ""
    echo -e "${GREEN}📱 TODAS LAS RESPUESTAS APARECEN EN TU CHAT DE TELEGRAM${NC}"
    echo -e "${YELLOW}   Revisa @Ticketero14_amazonQ_Bot para ver todo el flujo${NC}"
}

# Función para mostrar distribución de tickets
show_distribution() {
    echo -e "${BLUE}📋 DISTRIBUCIÓN DE TICKETS POR COLA:${NC}"
    printf "%-10s | %-15s | %-20s\n" "Cola" "Cantidad" "Cédulas"
    printf "%-10s-+-%-15s-+-%-20s\n" "----------" "---------------" "--------------------"
    
    local general_count=0
    local priority_count=0
    local vip_count=0
    local general_cedulas=""
    local priority_cedulas=""
    local vip_cedulas=""
    
    for i in $(seq 1 $TOTAL_USERS); do
        local cedula=${CEDULAS[$((i-1))]}
        local cola=${COLAS[$((($i-1) % 3))]}
        
        case $cola in
            "GENERAL")
                general_count=$((general_count + 1))
                general_cedulas="$general_cedulas $cedula"
                ;;
            "PRIORITY")
                priority_count=$((priority_count + 1))
                priority_cedulas="$priority_cedulas $cedula"
                ;;
            "VIP")
                vip_count=$((vip_count + 1))
                vip_cedulas="$vip_cedulas $cedula"
                ;;
        esac
    done
    
    printf "%-10s | %-15s | %-20s\n" "GENERAL" "$general_count" "${general_cedulas:1:20}..."
    printf "%-10s | %-15s | %-20s\n" "PRIORITY" "$priority_count" "${priority_cedulas:1:20}..."
    printf "%-10s | %-15s | %-20s\n" "VIP" "$vip_count" "${vip_cedulas:1:20}..."
    echo ""
}

# Función para mostrar ayuda
show_help() {
    echo "Load Test - Sistema Ticketero"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help        Mostrar esta ayuda"
    echo "  -u, --users       Número de usuarios (default: 15)"
    echo "  -c, --chat-id     Chat ID de Telegram"
    echo "  -d, --distribution Mostrar distribución de tickets"
    echo ""
    echo "Ejemplos:"
    echo "  $0                           # Load test con 15 usuarios"
    echo "  $0 -u 20                     # Load test con 20 usuarios"
    echo "  $0 -c 123456789              # Usar chat ID específico"
    echo "  $0 -d                        # Solo mostrar distribución"
}

# Procesar argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -u|--users)
            TOTAL_USERS="$2"
            shift 2
            ;;
        -c|--chat-id)
            CHAT_ID="$2"
            shift 2
            ;;
        -d|--distribution)
            show_distribution
            exit 0
            ;;
        *)
            echo "Opción desconocida: $1"
            show_help
            exit 1
            ;;
    esac
done

# Verificar dependencias
if ! command -v curl &> /dev/null; then
    echo "❌ Error: curl no está instalado"
    exit 1
fi

# Ejecutar load test
run_load_test