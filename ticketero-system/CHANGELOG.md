# Changelog - Sistema Ticketero

## [1.1.0] - 2025-01-XX

### ✅ Nuevas Funcionalidades

#### **RF-009: Gestión de Vigencia de Tickets**
- ✅ Vigencia configurable por tipo de cola (60-240 minutos)
- ✅ Cálculo automático de `expiresAt` al crear ticket
- ✅ Información de vencimiento visible en consultas
- ✅ Configuración por cola en `application.yml`

#### **RF-010: Cancelación Automática de Tickets Vencidos**
- ✅ Scheduler cada 60 segundos (`TicketLifecycleManager`)
- ✅ Cancelación automática de tickets vencidos
- ✅ Notificación `totem_ticket_vencido` al cliente
- ✅ Recálculo automático de posiciones en cola

#### **RF-002 Ampliado: Notificaciones Progresivas**
- ✅ 6 tipos de mensajes (antes 3):
  1. `totem_ticket_creado` - Confirmación inmediata
  2. `totem_faltan_15_min` - 15 minutos restantes
  3. `totem_faltan_5_min` - 5 minutos restantes  
  4. `totem_proximo_turno` - 3 posiciones restantes
  5. `totem_es_tu_turno` - Asignado a ejecutivo
  6. `totem_ticket_vencido` - Ticket expirado

### 🔧 Mejoras Técnicas

#### **Nuevos Componentes**
- ✅ `QueueType` enum con configuración de vigencia
- ✅ `TicketStatus` enum con estado `VENCIDO`
- ✅ `TicketLifecycleManager` con @Scheduled
- ✅ `AdminController` para gestión del scheduler

#### **Bot Telegram Mejorado**
- ✅ Vigencia por cola (GENERAL: 60min, PRIORITY: 120min, VIP: 180min)
- ✅ Verificación automática de tickets vencidos
- ✅ Comando `/check` para verificación manual
- ✅ Comando `/status` con estadísticas de vigencia
- ✅ Notificaciones progresivas simuladas

#### **Endpoints Nuevos**
- ✅ `GET /api/admin/scheduler/status` - Estado del scheduler
- ✅ `POST /api/admin/scheduler/run` - Ejecutar manualmente
- ✅ `GET /api/admin/dashboard` - Dashboard básico

### 📋 Reglas de Negocio Nuevas

- ✅ **RN-014**: Vigencia de tickets configurable por cola
- ✅ **RN-015**: Cancelación automática por vencimiento
- ✅ **RN-016**: Notificaciones de cola progresivas
- ✅ **RN-017**: Recálculo automático de posiciones

### 📚 Documentación Actualizada

- ✅ `functional-requirements.md` - RF-009 y RF-010 agregados
- ✅ `database-design.md` - Nuevos campos y queries
- ✅ `ticket-lifecycle-management.md` - Nuevo documento completo
- ✅ `02-technical-architecture.md` - Componente scheduler agregado

### 🔄 Configuración

```yaml
ticketero:
  scheduler:
    cancel-expired:
      enabled: true
      fixed-delay: 60000  # 60 segundos
    notifications:
      enabled: true
      fixed-delay: 30000  # 30 segundos
  queue-config:
    caja:
      vigencia-minutos: 60
    personal-banker:
      vigencia-minutos: 120
    empresas:
      vigencia-minutos: 180
    gerencia:
      vigencia-minutos: 240
```

### 🧪 Testing

Para probar las nuevas funcionalidades:

1. **Crear ticket**: Envía tu cédula + tipo de cola al bot
2. **Ver vigencia**: Usa `/status` para ver configuración
3. **Verificar vencimiento**: Usa `/check` para forzar verificación
4. **Monitorear**: `GET /api/admin/scheduler/status`

### 📊 Métricas

- **Tickets procesados**: Contador automático
- **Tickets vencidos**: Contador por ciclo de scheduler
- **Tiempo de ejecución**: Medición de performance
- **Última ejecución**: Timestamp del último ciclo

---

## [1.0.0] - 2025-01-XX

### ✅ Funcionalidades Base

- ✅ Bot Telegram básico
- ✅ Creación de tickets en memoria
- ✅ Notificaciones inmediatas
- ✅ Comandos básicos (`/start`, `/help`, `/status`)
- ✅ Health check endpoint

---

**Próximas versiones:**
- [ ] Persistencia en base de datos PostgreSQL
- [ ] Asignación automática a ejecutivos
- [ ] Panel web de administración
- [ ] Métricas avanzadas con Prometheus