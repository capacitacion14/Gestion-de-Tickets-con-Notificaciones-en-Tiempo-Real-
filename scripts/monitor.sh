#!/bin/bash

# Monitor de Tickets - Sistema Ticketero
# Actualización cada 7 segundos

API_BASE="http://localhost:8080/api"
REFRESH_INTERVAL=7

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Función para limpiar pantalla
clear_screen() {
    clear
}

# Función para obtener timestamp
get_timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

# Función para verificar si el servicio está activo
check_service() {
    curl -s "$API_BASE/health" > /dev/null 2>&1
    return $?
}

# Función para obtener estadísticas del scheduler
get_scheduler_stats() {
    local response=$(curl -s "$API_BASE/admin/scheduler/status" 2>/dev/null)
    if [[ $? -eq 0 && -n "$response" ]]; then
        local procesados=$(echo "$response" | jq -r '.ticketsProcesados // "N/A"' 2>/dev/null || echo "N/A")
        local vencidos=$(echo "$response" | jq -r '.ticketsVencidos // "N/A"' 2>/dev/null || echo "N/A")
        local ultima=$(echo "$response" | jq -r '.ultimaEjecucion // "N/A"' 2>/dev/null || echo "N/A")
        
        printf "%-20s | %-15s | %-25s\n" "Procesados" "Vencidos" "Última Ejecución"
        printf "%-20s-+-%-15s-+-%-25s\n" "--------------------" "---------------" "-------------------------"
        printf "%-20s | %-15s | %-25s\n" "$procesados" "$vencidos" "$ultima"
    else
        printf "%-20s | %-15s | %-25s\n" "ERROR" "ERROR" "ERROR"
    fi
}

# Función para obtener dashboard
get_dashboard() {
    local response=$(curl -s "$API_BASE/admin/dashboard" 2>/dev/null)
    if [[ $? -eq 0 && -n "$response" ]]; then
        local activos=$(echo "$response" | jq -r '.ticketsActivos // "N/A"' 2>/dev/null || echo "N/A")
        local vencidos=$(echo "$response" | jq -r '.ticketsVencidos // "N/A"' 2>/dev/null || echo "N/A")
        local updated=$(echo "$response" | jq -r '.lastUpdated // "N/A"' 2>/dev/null || echo "N/A")
        
        printf "%-15s | %-15s | %-25s\n" "Activos" "Vencidos" "Última Actualización"
        printf "%-15s-+-%-15s-+-%-25s\n" "---------------" "---------------" "-------------------------"
        printf "%-15s | %-15s | %-25s\n" "$activos" "$vencidos" "$updated"
    else
        printf "%-15s | %-15s | %-25s\n" "ERROR" "ERROR" "ERROR"
    fi
}

# Función para mostrar información de tickets en tabla
get_tickets_info() {
    printf "%-12s | %-12s | %-12s | %-15s\n" "Estado" "Duración" "Transición" "Descripción"
    printf "%-12s-+-%-12s-+-%-12s-+-%-15s\n" "------------" "------------" "------------" "---------------"
    printf "%-12s | %-12s | %-12s | %-15s\n" "PENDING" "Variable" "→ ATENDIENDO" "En cola"
    printf "%-12s | %-12s | %-12s | %-15s\n" "ATENDIENDO" "30 segundos" "→ COMPLETED" "Siendo atendido"
    printf "%-12s | %-12s | %-12s | %-15s\n" "COMPLETED" "Permanente" "Estado final" "Atención terminada"
}

# Función para mostrar información de colas en tabla
get_queue_info() {
    printf "%-10s | %-15s | %-15s | %-10s\n" "Cola" "Vigencia (min)" "Tiempo Prom." "Prefijo"
    printf "%-10s-+-%-15s-+-%-15s-+-%-10s\n" "----------" "---------------" "---------------" "----------"
    printf "%-10s | %-15s | %-15s | %-10s\n" "GENERAL" "60" "5" "G"
    printf "%-10s | %-15s | %-15s | %-10s\n" "PRIORITY" "120" "15" "P"
    printf "%-10s | %-15s | %-15s | %-10s\n" "VIP" "180" "20" "V"
}

# Función para mostrar actividad reciente en tabla
get_recent_activity() {
    printf "%-20s | %-15s | %-20s\n" "Proceso" "Intervalo" "Estado"
    printf "%-20s-+-%-15s-+-%-20s\n" "--------------------" "---------------" "--------------------"
    printf "%-20s | %-15s | %-20s\n" "Queue Processor" "10 segundos" "🟢 ACTIVO"
    printf "%-20s | %-15s | %-20s\n" "Notifications" "30 segundos" "🟢 ACTIVO"
    printf "%-20s | %-15s | %-20s\n" "Monitor" "7 segundos" "🟢 ACTIVO"
}

# Función para mostrar comandos en tabla
show_commands() {
    printf "%-15s | %-30s\n" "Comando" "Descripción"
    printf "%-15s-+-%-30s\n" "---------------" "------------------------------"
    printf "%-15s | %-30s\n" "/status" "Ver estadísticas del sistema"
    printf "%-15s | %-30s\n" "/check" "Procesar cola manualmente"
    printf "%-15s | %-30s\n" "/notify" "Enviar notificaciones"
    printf "%-15s | %-30s\n" "/clear" "Limpiar memoria"
    printf "%-15s | %-30s\n" "[cedula] [cola]" "Crear nuevo ticket"
}

# Función principal de monitoreo
monitor_tickets() {
    while true; do
        clear_screen
        
        echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${BLUE}║                    MONITOR DE TICKETS                        ║${NC}"
        echo -e "${BLUE}║                  Sistema Ticketero v1.1                     ║${NC}"
        echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        
        echo -e "${CYAN}🕐 Timestamp: $(get_timestamp)${NC}"
        echo -e "${CYAN}🔄 Refresh: cada ${REFRESH_INTERVAL} segundos${NC}"
        echo ""
        
        # Verificar estado del servicio
        if check_service; then
            echo -e "${GREEN}✅ Servicio: ACTIVO (localhost:8080)${NC}"
        else
            echo -e "${RED}❌ Servicio: INACTIVO (localhost:8080)${NC}"
        fi
        echo ""
        
        # Obtener estadísticas del scheduler
        echo -e "${YELLOW}📊 SCHEDULER STATS:${NC}"
        get_scheduler_stats
        echo ""
        
        # Obtener dashboard
        echo -e "${PURPLE}🎯 DASHBOARD:${NC}"
        get_dashboard
        echo ""
        
        # Información de tickets
        echo -e "${CYAN}🎫 ESTADOS DE TICKETS:${NC}"
        get_tickets_info
        echo ""
        
        # Información de colas
        echo -e "${BLUE}📋 CONFIGURACIÓN DE COLAS:${NC}"
        get_queue_info
        echo ""
        
        # Actividad reciente
        echo -e "${YELLOW}📝 PROCESOS ACTIVOS:${NC}"
        get_recent_activity
        echo ""
        
        # Comandos disponibles
        echo -e "${GREEN}🔧 COMANDOS TELEGRAM:${NC}"
        show_commands
        echo ""
        
        echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${BLUE}║ Presiona Ctrl+C para salir del monitor                      ║${NC}"
        echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
        
        # Esperar antes del siguiente refresh
        sleep $REFRESH_INTERVAL
    done
}

# Función para mostrar ayuda
show_help() {
    echo "Monitor de Tickets - Sistema Ticketero"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -i, --interval Intervalo de refresh en segundos (default: 7)"
    echo "  -u, --url      URL base de la API (default: http://localhost:8080/api)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                    # Monitor con configuración default"
    echo "  $0 -i 5              # Refresh cada 5 segundos"
    echo "  $0 -u http://prod:8080/api  # URL personalizada"
}

# Procesar argumentos de línea de comandos
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -i|--interval)
            REFRESH_INTERVAL="$2"
            shift 2
            ;;
        -u|--url)
            API_BASE="$2"
            shift 2
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

if ! command -v jq &> /dev/null; then
    echo "⚠️  Advertencia: jq no está instalado (funcionalidad limitada)"
fi

# Iniciar monitoreo
echo "🚀 Iniciando monitor de tickets..."
echo "📡 API Base: $API_BASE"
echo "🔄 Intervalo: ${REFRESH_INTERVAL}s"
echo ""
sleep 2

monitor_tickets