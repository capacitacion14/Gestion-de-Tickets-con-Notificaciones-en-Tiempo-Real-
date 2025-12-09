# Diseño de APIs REST - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 1. Endpoints Públicos

### POST /api/tickets
Crear nuevo ticket digital.

**Request:**
```json
{
  "nationalId": "12345678-9",
  "telefono": "+56912345678",
  "branchOffice": "Sucursal Centro",
  "queueType": "CAJA"
}
```

**Response 201 Created:**
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "C01",
  "queueType": "CAJA",
  "status": "EN_ESPERA",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "createdAt": "2025-12-15T10:00:00Z"
}
```

**Response 409 Conflict:**
```json
{
  "error": "TICKET_ACTIVO_EXISTENTE",
  "mensaje": "Ya tienes un ticket activo: P05"
}
```

---

### GET /api/tickets/{codigoReferencia}
Consultar ticket por UUID.

**Response 200 OK:**
```json
{
  "codigoReferencia": "uuid-123",
  "numero": "C05",
  "status": "EN_ESPERA",
  "positionInQueue": 5,
  "estimatedWaitMinutes": 25,
  "queueType": "CAJA",
  "createdAt": "2025-12-15T10:00:00Z"
}
```

---

### GET /api/tickets/{numero}/position
Consultar posición por número de ticket.

**Response 200 OK:**
```json
{
  "numero": "P12",
  "positionInQueue": 8,
  "estimatedWaitMinutes": 120,
  "status": "EN_ESPERA",
  "calculatedAt": "2025-12-15T10:30:00Z"
}
```

---

## 2. Endpoints Administrativos

### GET /api/admin/dashboard
Dashboard completo en tiempo real.

**Response 200 OK:**
```json
{
  "summary": {
    "ticketsWaiting": 15,
    "ticketsInProgress": 5,
    "ticketsCompletedToday": 45,
    "availableAdvisors": 3
  },
  "queuesSummary": [
    {"queueType": "CAJA", "waiting": 8, "avgWaitMinutes": 40}
  ],
  "alerts": [],
  "lastUpdated": "2025-12-15T10:30:00Z"
}
```

---

### GET /api/admin/queues
Listar todas las colas.

**Response 200 OK:**
```json
[
  {
    "queueType": "CAJA",
    "displayName": "Caja",
    "ticketsWaiting": 5,
    "priority": 1
  }
]
```

---

### PUT /api/admin/advisors/{id}/status
Cambiar estado de ejecutivo.

**Request:**
```json
{
  "status": "OFFLINE"
}
```

**Response 200 OK:**
```json
{
  "id": 1,
  "name": "María González",
  "status": "OFFLINE",
  "moduleNumber": 3
}
```

---

### GET /api/admin/audit
Consultar auditoría con filtros.

**Query Params:**
- entityId (opcional)
- eventType (opcional)
- actor (opcional)

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "timestamp": "2025-12-15T10:00:00Z",
    "eventType": "TICKET_CREADO",
    "actor": "SYSTEM",
    "entityId": "uuid-123"
  }
]
```

---

## 3. Códigos de Estado HTTP

| Código | Uso |
|--------|-----|
| 200 | Consulta exitosa |
| 201 | Recurso creado |
| 204 | Operación exitosa sin contenido |
| 400 | Validación fallida |
| 404 | Recurso no encontrado |
| 409 | Conflicto (ticket activo existente) |
| 500 | Error interno del servidor |

---

**Fin del Documento de Diseño de APIs**
