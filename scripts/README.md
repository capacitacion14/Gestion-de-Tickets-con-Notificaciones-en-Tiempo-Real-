# Scripts de Monitoreo - Sistema Ticketero

## 📊 Monitor de Tickets (`monitor.sh`)

Script de monitoreo en tiempo real para el Sistema Ticketero que muestra información relevante de tickets, colas y schedulers.

### 🚀 Uso Básico

```bash
# Ejecutar con configuración default (refresh cada 7 segundos)
./scripts/monitor.sh

# Ejecutar con intervalo personalizado
./scripts/monitor.sh -i 5

# Ejecutar con URL personalizada
./scripts/monitor.sh -u http://prod:8080/api
```

### 📋 Información Mostrada

#### **Estado del Servicio**
- ✅/❌ Estado de conexión (localhost:8080)
- 🕐 Timestamp actual
- 🔄 Intervalo de refresh

#### **Estadísticas del Scheduler**
- 📊 Tickets procesados total
- 📊 Tickets vencidos total  
- 📊 Última ejecución del scheduler

#### **Dashboard del Sistema**
- 🎯 Tickets activos en memoria
- 🎯 Tickets vencidos
- 🎯 Última actualización

#### **Configuración de Colas**
- 📋 GENERAL: 60 min vigencia, 5 min promedio
- 📋 PRIORITY: 120 min vigencia, 15 min promedio
- 📋 VIP: 180 min vigencia, 20 min promedio

#### **Actividad del Sistema**
- 📝 Estado de schedulers (cada 10s y 30s)
- 📝 Procesamiento automático de cola
- 📝 Monitoreo en tiempo real

#### **Comandos Telegram Disponibles**
- 🔧 `/status` - Ver estadísticas
- 🔧 `/check` - Procesar cola manualmente
- 🔧 `/notify` - Enviar notificaciones
- 🔧 `/clear` - Limpiar memoria
- 🔧 `[cedula] [cola]` - Crear ticket

### ⚙️ Opciones de Configuración

| Opción | Descripción | Default |
|--------|-------------|---------|
| `-h, --help` | Mostrar ayuda | - |
| `-i, --interval` | Intervalo de refresh (segundos) | 7 |
| `-u, --url` | URL base de la API | http://localhost:8080/api |

### 📦 Dependencias

#### **Requeridas:**
- `curl` - Para llamadas HTTP a la API

#### **Opcionales:**
- `jq` - Para formateo JSON (mejora la visualización)

### 🎨 Características

- ✅ **Colores**: Output colorizado para mejor legibilidad
- ✅ **Tiempo Real**: Refresh automático cada 7 segundos
- ✅ **Responsive**: Se adapta al tamaño de terminal
- ✅ **Error Handling**: Manejo de errores de conexión
- ✅ **Configurable**: Parámetros personalizables
- ✅ **Cross-Platform**: Compatible con macOS/Linux

### 🧪 Ejemplos de Uso

#### **Monitoreo Standard:**
```bash
./scripts/monitor.sh
```

#### **Monitoreo Rápido (cada 3 segundos):**
```bash
./scripts/monitor.sh -i 3
```

#### **Monitoreo de Producción:**
```bash
./scripts/monitor.sh -u https://ticketero-prod.com/api -i 10
```

### 🔧 Troubleshooting

#### **Error: curl no está instalado**
```bash
# macOS
brew install curl

# Ubuntu/Debian
sudo apt-get install curl
```

#### **Advertencia: jq no está instalado**
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq
```

#### **Error de conexión**
- Verificar que el servicio esté ejecutándose en puerto 8080
- Verificar la URL de la API
- Verificar conectividad de red

### 📊 Output de Ejemplo

```
╔══════════════════════════════════════════════════════════════╗
║                    MONITOR DE TICKETS                        ║
║                  Sistema Ticketero v1.1                     ║
╚══════════════════════════════════════════════════════════════╝

🕐 Timestamp: 2025-01-15 14:30:25
🔄 Refresh: cada 7 segundos

✅ Servicio: ACTIVO (localhost:8080)

📊 SCHEDULER STATS:
├─ Tickets Procesados: 15
├─ Tickets Vencidos: 3
└─ Última Ejecución: 2025-01-15T14:30:20Z

🎯 DASHBOARD:
├─ Tickets Activos: 5
├─ Tickets Vencidos: 3
└─ Última Actualización: 2025-01-15T14:30:25Z

🎫 TICKETS EN MEMORIA:
├─ Sistema usa memoria interna (ConcurrentHashMap)
├─ Estados: PENDING → ATENDIENDO → COMPLETED
├─ Duración ATENDIENDO: 30 segundos
└─ Vigencias: GENERAL(60min), PRIORITY(120min), VIP(180min)
```

### 🚀 Integración con CI/CD

El script puede integrarse en pipelines de CI/CD para monitoreo automatizado:

```yaml
# GitHub Actions example
- name: Monitor Tickets
  run: |
    ./scripts/monitor.sh -i 1 &
    MONITOR_PID=$!
    sleep 30
    kill $MONITOR_PID
```