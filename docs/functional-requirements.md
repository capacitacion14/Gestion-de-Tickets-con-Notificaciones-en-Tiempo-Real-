# Requerimientos Funcionales - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Analista:** Analista de Negocio Senior

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requerimientos funcionales del Sistema Ticketero Digital, diseñado para modernizar la experiencia de atención en sucursales mediante:

- Digitalización completa del proceso de tickets
- Notificaciones automáticas en tiempo real vía Telegram
- Movilidad del cliente durante la espera
- Asignación inteligente de clientes a ejecutivos
- Panel de monitoreo para supervisión operacional

### 1.2 Alcance

Este documento cubre:

- ✅ 8 Requerimientos Funcionales (RF-001 a RF-008)
- ✅ 13 Reglas de Negocio (RN-001 a RN-013)
- ✅ Criterios de aceptación en formato Gherkin
- ✅ Modelo de datos funcional
- ✅ Matriz de trazabilidad

Este documento NO cubre:

- ❌ Arquitectura técnica (ver documento technical-architecture-proposal.md)
- ❌ Tecnologías de implementación
- ❌ Diseño de interfaces de usuario

### 1.3 Definiciones

| Término | Definición |
|---------|------------|
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Cola | Fila virtual de tickets esperando atención |
| Asesor | Ejecutivo bancario que atiende clientes |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Chat ID | Identificador único de usuario en Telegram |
| UUID | Identificador único universal para tickets |

---

## 2. Reglas de Negocio

Las siguientes reglas de negocio aplican transversalmente a todos los requerimientos funcionales:

**RN-001: Unicidad de Ticket Activo**  
Un cliente solo puede tener 1 ticket activo a la vez. Los estados activos son: EN_ESPERA, PROXIMO, ATENDIENDO. Si un cliente intenta crear un nuevo ticket teniendo uno activo, el sistema debe rechazar la solicitud con error HTTP 409 Conflict.

**RN-002: Prioridad de Colas**  
Las colas tienen prioridades numéricas para asignación automática:
- GERENCIA: prioridad 4 (máxima)
- EMPRESAS: prioridad 3
- PERSONAL_BANKER: prioridad 2
- CAJA: prioridad 1 (mínima)

Cuando un asesor se libera, el sistema asigna primero tickets de colas con mayor prioridad.

**RN-003: Orden FIFO Dentro de Cola**  
Dentro de una misma cola, los tickets se procesan en orden FIFO (First In, First Out). El ticket más antiguo (createdAt menor) se asigna primero.

**RN-004: Balanceo de Carga Entre Asesores**  
Al asignar un ticket, el sistema selecciona el asesor AVAILABLE con menor valor de assignedTicketsCount, distribuyendo equitativamente la carga de trabajo.

**RN-005: Formato de Número de Ticket**  
El número de ticket sigue el formato: [Prefijo][Número secuencial 01-99]
- Prefijo: 1 letra según el tipo de cola
- Número: 2 dígitos, del 01 al 99, reseteado diariamente

Ejemplos: C01, P15, E03, G02

**RN-006: Prefijos por Tipo de Cola**  
- CAJA → C
- PERSONAL_BANKER → P
- EMPRESAS → E
- GERENCIA → G

**RN-007: Reintentos Automáticos de Mensajes**  
Si el envío de un mensaje a Telegram falla, el sistema reintenta automáticamente hasta 3 veces antes de marcarlo como FALLIDO.

**RN-008: Backoff Exponencial en Reintentos**  
Los reintentos de mensajes usan backoff exponencial:
- Intento 1: inmediato
- Intento 2: después de 30 segundos
- Intento 3: después de 60 segundos
- Intento 4: después de 120 segundos

**RN-009: Estados de Ticket**  
Un ticket puede estar en uno de estos estados:
- EN_ESPERA: esperando asignación a asesor
- PROXIMO: próximo a ser atendido (posición ≤ 3)
- ATENDIENDO: siendo atendido por un asesor
- COMPLETADO: atención finalizada exitosamente
- CANCELADO: cancelado por cliente o sistema
- NO_ATENDIDO: cliente no se presentó cuando fue llamado

**RN-010: Cálculo de Tiempo Estimado**  
El tiempo estimado de espera se calcula como:

```
tiempoEstimado = posiciónEnCola × tiempoPromedioCola
```

Donde tiempoPromedioCola varía por tipo:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos

**RN-011: Auditoría Obligatoria**  
Todos los eventos críticos del sistema deben registrarse en auditoría con: timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado.

**RN-012: Umbral de Pre-aviso**  
El sistema envía el Mensaje 2 (pre-aviso) cuando la posición del ticket es ≤ 3, indicando que el cliente debe acercarse a la sucursal.

**RN-013: Estados de Asesor**  
Un asesor puede estar en uno de estos estados:
- AVAILABLE: disponible para recibir asignaciones
- BUSY: atendiendo un cliente (no recibe nuevas asignaciones)
- OFFLINE: no disponible (almuerzo, capacitación, etc.)

---

## 3. Enumeraciones

### 3.1 QueueType

Tipos de cola disponibles en el sistema:

| Valor | Display Name | Tiempo Promedio | Prioridad | Prefijo |
|-------|--------------|-----------------|-----------|---------|
| CAJA | Caja | 5 min | 1 | C |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 | P |
| EMPRESAS | Empresas | 20 min | 3 | E |
| GERENCIA | Gerencia | 30 min | 4 | G |

### 3.2 TicketStatus

Estados posibles de un ticket:

| Valor | Descripción | Es Activo? |
|-------|-------------|------------|
| EN_ESPERA | Esperando asignación | Sí |
| PROXIMO | Próximo a ser atendido | Sí |
| ATENDIENDO | Siendo atendido | Sí |
| COMPLETADO | Atención finalizada | No |
| CANCELADO | Cancelado | No |
| NO_ATENDIDO | Cliente no se presentó | No |

### 3.3 AdvisorStatus

Estados posibles de un asesor:

| Valor | Descripción | Recibe Asignaciones? |
|-------|-------------|----------------------|
| AVAILABLE | Disponible | Sí |
| BUSY | Atendiendo cliente | No |
| OFFLINE | No disponible | No |

### 3.4 MessageTemplate

Plantillas de mensajes para Telegram:

| Valor | Descripción | Momento de Envío |
|-------|-------------|------------------|
| totem_ticket_creado | Confirmación de creación | Inmediato al crear ticket |
| totem_proximo_turno | Pre-aviso | Cuando posición ≤ 3 |
| totem_es_tu_turno | Turno activo | Al asignar a asesor |

---

## 4. Requerimientos Funcionales

### RF-001: Crear Ticket Digital

**Descripción:** El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Terminal de autoservicio disponible y funcional
- Sistema de gestión de colas operativo
- Conexión a base de datos activa

**Modelo de Datos (Campos del Ticket):**

- codigoReferencia: UUID único (ej: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
- numero: String formato específico por cola (ej: "C01", "P15", "E03", "G02")
- nationalId: String, identificación nacional del cliente
- telefono: String, número de teléfono para Telegram
- branchOffice: String, nombre de la sucursal
- queueType: Enum (CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA)
- status: Enum (EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO)
- positionInQueue: Integer, posición actual en cola (calculada en tiempo real)
- estimatedWaitMinutes: Integer, minutos estimados de espera
- createdAt: Timestamp, fecha/hora de creación
- assignedAdvisor: Relación a entidad Advisor (null inicialmente)
- assignedModuleNumber: Integer 1-5 (null inicialmente)

**Reglas de Negocio Aplicables:**
- RN-001: Un cliente solo puede tener 1 ticket activo a la vez
- RN-005: Número de ticket formato: [Prefijo][Número secuencial 01-99]
- RN-006: Prefijos por cola: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia
- RN-010: Cálculo de tiempo estimado: posiciónEnCola × tiempoPromedioCola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Creación exitosa de ticket para cola de Caja**

```gherkin
Given el cliente con nationalId "12345678-9" no tiene tickets activos
And el terminal está en pantalla de selección de servicio
When el cliente ingresa:
  | Campo        | Valor           |
  | nationalId   | 12345678-9      |
  | telefono     | +56912345678    |
  | branchOffice | Sucursal Centro |
  | queueType    | CAJA            |
Then el sistema genera un ticket con:
  | Campo                 | Valor Esperado                    |
  | codigoReferencia      | UUID válido                       |
  | numero                | "C[01-99]"                        |
  | status                | EN_ESPERA                         |
  | positionInQueue       | Número > 0                        |
  | estimatedWaitMinutes  | positionInQueue × 5               |
  | assignedAdvisor       | null                              |
  | assignedModuleNumber  | null                              |
And el sistema almacena el ticket en base de datos
And el sistema programa 3 mensajes de Telegram
And el sistema retorna HTTP 201 con JSON:
  {
    "identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "numero": "C01",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 25,
    "queueType": "CAJA"
  }
```

**Escenario 2: Error - Cliente ya tiene ticket activo**

```gherkin
Given el cliente con nationalId "12345678-9" tiene un ticket activo:
  | numero | status     | queueType      |
  | P05    | EN_ESPERA  | PERSONAL_BANKER|
When el cliente intenta crear un nuevo ticket con queueType CAJA
Then el sistema rechaza la creación
And el sistema retorna HTTP 409 Conflict con JSON:
  {
    "error": "TICKET_ACTIVO_EXISTENTE",
    "mensaje": "Ya tienes un ticket activo: P05",
    "ticketActivo": {
      "numero": "P05",
      "positionInQueue": 3,
      "estimatedWaitMinutes": 45
    }
  }
And el sistema NO crea un nuevo ticket
```

**Escenario 3: Validación - RUT/ID inválido**

```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa nationalId vacío
Then el sistema retorna HTTP 400 Bad Request con JSON:
  {
    "error": "VALIDACION_FALLIDA",
    "campos": {
      "nationalId": "El RUT/ID es obligatorio"
    }
  }
And el sistema NO crea el ticket
```

**Escenario 4: Validación - Teléfono en formato inválido**

```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa telefono "123"
Then el sistema retorna HTTP 400 Bad Request
And el mensaje de error especifica formato requerido "+56XXXXXXXXX"
```

**Escenario 5: Cálculo de posición - Primera persona en cola**

```gherkin
Given la cola de tipo PERSONAL_BANKER está vacía
When el cliente crea un ticket para PERSONAL_BANKER
Then el sistema calcula positionInQueue = 1
And estimatedWaitMinutes = 15
And el número de ticket es "P01"
```

**Escenario 6: Cálculo de posición - Cola con tickets existentes**

```gherkin
Given la cola de tipo EMPRESAS tiene 4 tickets EN_ESPERA
When el cliente crea un nuevo ticket para EMPRESAS
Then el sistema calcula positionInQueue = 5
And estimatedWaitMinutes = 100
And el cálculo es: 5 × 20min = 100min
```

**Escenario 7: Creación sin teléfono (cliente no quiere notificaciones)**

```gherkin
Given el cliente no proporciona número de teléfono
When el cliente crea un ticket
Then el sistema crea el ticket exitosamente
And el sistema NO programa mensajes de Telegram
```

**Postcondiciones:**
- Ticket almacenado en base de datos con estado EN_ESPERA
- 3 mensajes programados (si hay teléfono)
- Evento de auditoría registrado: "TICKET_CREADO"

**Endpoints HTTP:**
- `POST /api/tickets` - Crear nuevo ticket

---

### RF-002: Enviar Notificaciones Automáticas vía Telegram

**Descripción:** El sistema debe enviar automáticamente tres tipos de mensajes vía Telegram al cliente durante el ciclo de vida del ticket: (1) Confirmación inmediata al crear el ticket con número, posición y tiempo estimado, (2) Pre-aviso cuando quedan 3 personas adelante solicitando acercarse a sucursal, (3) Turno activo al asignar a un ejecutivo indicando módulo y nombre del asesor. El sistema debe implementar reintentos automáticos con backoff exponencial para garantizar la entrega de mensajes.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket creado con teléfono válido
- Telegram Bot configurado y activo
- Cliente tiene cuenta de Telegram asociada al teléfono

**Modelo de Datos (Entidad Mensaje):**

- id: BIGSERIAL (primary key)
- ticket_id: BIGINT (foreign key a ticket)
- plantilla: String (totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno)
- estadoEnvio: Enum (PENDIENTE, ENVIADO, FALLIDO)
- fechaProgramada: Timestamp
- fechaEnvio: Timestamp (nullable)
- telegramMessageId: String (nullable, retornado por Telegram API)
- intentos: Integer (contador de reintentos, default 0)

**Plantillas de Mensajes:**

**1. totem_ticket_creado:**
```
✅ <b>Ticket Creado</b>

Tu número de turno: <b>{numero}</b>
Posición en cola: <b>#{posicion}</b>
Tiempo estimado: <b>{tiempo} minutos</b>

Te notificaremos cuando estés próximo.
```

**2. totem_proximo_turno:**
```
⏰ <b>¡Pronto será tu turno!</b>

Turno: <b>{numero}</b>
Faltan aproximadamente 3 turnos.

Por favor, acércate a la sucursal.
```

**3. totem_es_tu_turno:**
```
🔔 <b>¡ES TU TURNO {numero}!</b>

Dirígete al módulo: <b>{modulo}</b>
Asesor: <b>{nombreAsesor}</b>
```

**Reglas de Negocio Aplicables:**
- RN-007: 3 reintentos automáticos para mensajes fallidos
- RN-008: Backoff exponencial (30s, 60s, 120s)
- RN-011: Auditoría obligatoria de envíos
- RN-012: Mensaje 2 cuando posición ≤ 3

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Envío exitoso del Mensaje 1 (Confirmación)**

```gherkin
Given un ticket fue creado con:
  | codigoReferencia | numero | positionInQueue | estimatedWaitMinutes | telefono     |
  | uuid-123         | C05    | 5               | 25                   | +56912345678 |
And el Telegram Bot está operativo
When el sistema programa el Mensaje 1 (totem_ticket_creado)
Then el sistema envía el mensaje a Telegram API
And Telegram API retorna HTTP 200 con:
  {
    "ok": true,
    "result": {
      "message_id": 12345
    }
  }
And el sistema actualiza el registro del mensaje:
  | estadoEnvio | fechaEnvio        | telegramMessageId | intentos |
  | ENVIADO     | 2025-12-15 10:00  | 12345             | 1        |
And el sistema registra evento de auditoría: "MENSAJE_ENVIADO"
```

**Escenario 2: Envío exitoso del Mensaje 2 (Pre-aviso)**

```gherkin
Given un ticket tiene:
  | numero | positionInQueue | status    |
  | P08    | 3               | EN_ESPERA |
And el mensaje totem_proximo_turno está PENDIENTE
When el sistema detecta que positionInQueue ≤ 3
Then el sistema envía el Mensaje 2 con texto:
  "⏰ ¡Pronto será tu turno!
   Turno: P08
   Faltan aproximadamente 3 turnos.
   Por favor, acércate a la sucursal."
And el mensaje se marca como ENVIADO
And el ticket cambia status a PROXIMO
```

**Escenario 3: Envío exitoso del Mensaje 3 (Turno Activo)**

```gherkin
Given un ticket fue asignado a asesor:
  | numero | assignedAdvisor | assignedModuleNumber |
  | E02    | María González  | 3                    |
And el mensaje totem_es_tu_turno está PENDIENTE
When el sistema procesa la asignación
Then el sistema envía el Mensaje 3 con texto:
  "🔔 ¡ES TU TURNO E02!
   Dirígete al módulo: 3
   Asesor: María González"
And el mensaje se marca como ENVIADO
And el ticket cambia status a ATENDIENDO
```

**Escenario 4: Fallo de red en primer intento, éxito en segundo**

```gherkin
Given un mensaje está PENDIENTE con intentos = 0
When el sistema intenta enviar el mensaje
And Telegram API retorna error de red (timeout)
Then el sistema marca estadoEnvio = PENDIENTE
And incrementa intentos = 1
And programa reintento después de 30 segundos (RN-008)
When transcurren 30 segundos
And el sistema reintenta el envío
And Telegram API retorna HTTP 200
Then el sistema marca estadoEnvio = ENVIADO
And actualiza intentos = 2
And almacena telegramMessageId
```

**Escenario 5: 3 reintentos fallidos → estado FALLIDO**

```gherkin
Given un mensaje está PENDIENTE con intentos = 0
When el sistema intenta enviar (intento 1)
And Telegram API retorna error 500
Then el sistema programa reintento después de 30s
When el sistema reintenta (intento 2)
And Telegram API retorna error 500
Then el sistema programa reintento después de 60s
When el sistema reintenta (intento 3)
And Telegram API retorna error 500
Then el sistema programa reintento después de 120s
When el sistema reintenta (intento 4)
And Telegram API retorna error 500
Then el sistema marca estadoEnvio = FALLIDO
And actualiza intentos = 4
And el sistema registra evento de auditoría: "MENSAJE_FALLIDO"
And el sistema NO programa más reintentos
```

**Escenario 6: Backoff exponencial entre reintentos**

```gherkin
Given un mensaje falló en el primer intento a las 10:00:00
When el sistema programa el reintento 2
Then la fechaProgramada es 10:00:30 (30 segundos después)
Given el reintento 2 falló a las 10:00:30
When el sistema programa el reintento 3
Then la fechaProgramada es 10:01:30 (60 segundos después)
Given el reintento 3 falló a las 10:01:30
When el sistema programa el reintento 4
Then la fechaProgramada es 10:03:30 (120 segundos después)
```

**Escenario 7: Cliente sin teléfono, no se programan mensajes**

```gherkin
Given un ticket fue creado sin teléfono:
  | codigoReferencia | numero | telefono |
  | uuid-456         | G01    | null     |
When el sistema procesa el ticket
Then el sistema NO crea registros de mensajes
And el sistema NO intenta enviar notificaciones
And el ticket funciona normalmente sin notificaciones
```

**Postcondiciones:**
- Mensaje insertado en BD con estado según resultado (ENVIADO/FALLIDO)
- telegramMessageId almacenado si envío exitoso
- Contador de intentos actualizado
- Evento de auditoría registrado

**Endpoints HTTP:**
- Ninguno (proceso interno automatizado por scheduler)

---

### RF-003: Calcular Posición y Tiempo Estimado

**Descripción:** El sistema debe calcular en tiempo real la posición exacta del cliente en cola y estimar el tiempo de espera basado en tres factores: (1) posición actual del ticket en la cola, (2) tiempo promedio de atención por tipo de cola, y (3) cantidad de ejecutivos disponibles. El cálculo debe actualizarse automáticamente cuando cambia el estado de otros tickets o la disponibilidad de ejecutivos.

**Prioridad:** Alta

**Actor Principal:** Sistema (cálculo automático)

**Precondiciones:**
- Ticket existe en el sistema
- Cola tiene configuración de tiempo promedio
- Sistema tiene información actualizada de ejecutivos

**Algoritmos de Cálculo:**

**Posición en Cola:**
```
posición = COUNT(tickets con status EN_ESPERA o PROXIMO 
                 en la misma cola 
                 con createdAt < createdAt del ticket actual) + 1
```

**Tiempo Estimado:**
```
tiempoEstimado = posición × tiempoPromedioCola

Donde tiempoPromedioCola:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos
```

**Reglas de Negocio Aplicables:**
- RN-003: Orden FIFO dentro de cola (createdAt determina orden)
- RN-010: Fórmula de cálculo de tiempo estimado

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Cálculo de posición - Primera persona en cola**

```gherkin
Given la cola PERSONAL_BANKER está vacía
When un cliente crea un ticket para PERSONAL_BANKER
Then el sistema calcula:
  | Campo                | Valor |
  | positionInQueue      | 1     |
  | estimatedWaitMinutes | 15    |
And el cálculo es: 1 × 15min = 15min
```

**Escenario 2: Cálculo de posición - Cola con tickets existentes**

```gherkin
Given la cola EMPRESAS tiene tickets EN_ESPERA:
  | numero | createdAt           |
  | E01    | 2025-12-15 10:00:00 |
  | E02    | 2025-12-15 10:05:00 |
  | E03    | 2025-12-15 10:10:00 |
  | E04    | 2025-12-15 10:15:00 |
When un cliente crea ticket E05 a las 10:20:00
Then el sistema calcula:
  | positionInQueue      | 5   |
  | estimatedWaitMinutes | 100 |
And el cálculo es: 5 × 20min = 100min
```

**Escenario 3: Recálculo automático cuando ticket adelante es completado**

```gherkin
Given un ticket tiene:
  | numero | positionInQueue | estimatedWaitMinutes | queueType |
  | C08    | 5               | 25                   | CAJA      |
And hay 4 tickets adelante en cola CAJA
When un ticket adelante cambia status a COMPLETADO
Then el sistema recalcula automáticamente:
  | positionInQueue      | 4  |
  | estimatedWaitMinutes | 20 |
And el cálculo actualizado es: 4 × 5min = 20min
```

**Escenario 4: Consulta de posición vía API**

```gherkin
Given un ticket existe:
  | numero | positionInQueue | estimatedWaitMinutes |
  | P12    | 8               | 120                  |
When el cliente consulta GET /api/tickets/P12/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "P12",
    "positionInQueue": 8,
    "estimatedWaitMinutes": 120,
    "queueType": "PERSONAL_BANKER",
    "status": "EN_ESPERA",
    "calculatedAt": "2025-12-15T10:30:00Z"
  }
And el tiempo de respuesta es < 1 segundo (RNF-002)
```

**Escenario 5: Diferentes tiempos por tipo de cola**

```gherkin
Given existen tickets en diferentes colas con posición 3:
  | numero | queueType       | positionInQueue |
  | C15    | CAJA            | 3               |
  | P08    | PERSONAL_BANKER | 3               |
  | E05    | EMPRESAS        | 3               |
  | G02    | GERENCIA        | 3               |
When el sistema calcula tiempos estimados
Then los resultados son:
  | numero | estimatedWaitMinutes | cálculo      |
  | C15    | 15                   | 3 × 5min     |
  | P08    | 45                   | 3 × 15min    |
  | E05    | 60                   | 3 × 20min    |
  | G02    | 90                   | 3 × 30min    |
```

**Escenario 6: Ticket en estado ATENDIENDO no cuenta para posición**

```gherkin
Given la cola CAJA tiene tickets:
  | numero | status      | createdAt           |
  | C01    | ATENDIENDO  | 2025-12-15 10:00:00 |
  | C02    | EN_ESPERA   | 2025-12-15 10:05:00 |
  | C03    | EN_ESPERA   | 2025-12-15 10:10:00 |
When el sistema calcula posición para C03
Then positionInQueue = 2 (no cuenta C01 porque está ATENDIENDO)
And estimatedWaitMinutes = 10
```

**Escenario 7: Orden FIFO respetado por createdAt**

```gherkin
Given la cola GERENCIA tiene tickets:
  | numero | createdAt           |
  | G03    | 2025-12-15 10:15:00 |
  | G01    | 2025-12-15 10:00:00 |
  | G02    | 2025-12-15 10:10:00 |
When el sistema calcula posiciones
Then el orden es:
  | numero | positionInQueue | Razón                    |
  | G01    | 1               | createdAt más antiguo    |
  | G02    | 2               | createdAt segundo        |
  | G03    | 3               | createdAt más reciente   |
```

**Postcondiciones:**
- Posición calculada correctamente según orden FIFO
- Tiempo estimado basado en fórmula RN-010
- Cálculo completado en < 1 segundo
- Valores actualizados en base de datos

**Endpoints HTTP:**
- `GET /api/tickets/{numero}/position` - Consultar posición y tiempo estimado
- `GET /api/tickets/{codigoReferencia}` - Consultar ticket completo (incluye posición)

---

### RF-004: Asignar Ticket a Ejecutivo Automáticamente

**Descripción:** El sistema debe asignar automáticamente el siguiente ticket en cola cuando un ejecutivo se libere, considerando tres criterios en orden de prioridad: (1) prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA), (2) balanceo de carga entre ejecutivos (seleccionar el ejecutivo con menor cantidad de tickets asignados), y (3) orden FIFO dentro de cada cola (ticket más antiguo primero). La asignación debe ser transaccional y atómica para evitar conflictos.

**Prioridad:** Crítica

**Actor Principal:** Sistema (proceso automático)

**Precondiciones:**
- Al menos un ejecutivo con status AVAILABLE
- Al menos un ticket con status EN_ESPERA o PROXIMO
- Ejecutivo tiene capacidad para atender el tipo de cola

**Modelo de Datos (Entidad Advisor):**

- id: BIGSERIAL (primary key)
- name: String, nombre completo del ejecutivo
- email: String, correo electrónico
- status: Enum (AVAILABLE, BUSY, OFFLINE)
- moduleNumber: Integer (1-5), número de módulo asignado
- supportedQueues: Array de QueueType, colas que puede atender
- assignedTicketsCount: Integer, contador de tickets asignados hoy
- lastAssignmentAt: Timestamp, última asignación recibida

**Algoritmo de Asignación:**

```
1. Filtrar ejecutivos con status = AVAILABLE
2. Ordenar tickets pendientes por:
   a. Prioridad de cola (descendente): GERENCIA(4) > EMPRESAS(3) > PERSONAL_BANKER(2) > CAJA(1)
   b. Fecha de creación (ascendente): más antiguo primero
3. Para el ticket de mayor prioridad:
   a. Filtrar ejecutivos que soportan ese tipo de cola
   b. Seleccionar ejecutivo con menor assignedTicketsCount
   c. Si hay empate, seleccionar el de lastAssignmentAt más antiguo
4. Asignar ticket al ejecutivo seleccionado:
   a. ticket.assignedAdvisor = ejecutivo
   b. ticket.assignedModuleNumber = ejecutivo.moduleNumber
   c. ticket.status = ATENDIENDO
   d. ejecutivo.status = BUSY
   e. ejecutivo.assignedTicketsCount++
   f. ejecutivo.lastAssignmentAt = now()
5. Programar Mensaje 3 (totem_es_tu_turno)
6. Registrar evento de auditoría
```

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA)
- RN-003: Orden FIFO dentro de cada cola
- RN-004: Balanceo de carga (menor assignedTicketsCount)
- RN-013: Estados de asesor (AVAILABLE, BUSY, OFFLINE)

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Asignación exitosa con un solo ticket en cola**

```gherkin
Given existe un ejecutivo AVAILABLE:
  | id | name           | status    | moduleNumber | assignedTicketsCount |
  | 1  | María González | AVAILABLE | 3            | 5                    |
And existe un ticket EN_ESPERA:
  | numero | queueType | createdAt           |
  | C05    | CAJA      | 2025-12-15 10:00:00 |
When el sistema ejecuta el proceso de asignación automática
Then el sistema asigna el ticket C05 al ejecutivo María González
And el ticket se actualiza:
  | assignedAdvisor | assignedModuleNumber | status      |
  | María González  | 3                    | ATENDIENDO  |
And el ejecutivo se actualiza:
  | status | assignedTicketsCount | lastAssignmentAt    |
  | BUSY   | 6                    | 2025-12-15 10:00:05 |
And el sistema programa Mensaje 3: "🔔 ¡ES TU TURNO C05! Dirígete al módulo: 3 Asesor: María González"
And el sistema registra evento de auditoría: "TICKET_ASIGNADO"
```

**Escenario 2: Prioridad de colas - GERENCIA antes que CAJA**

```gherkin
Given existe un ejecutivo AVAILABLE que soporta todas las colas
And existen tickets EN_ESPERA:
  | numero | queueType | prioridad | createdAt           |
  | C01    | CAJA      | 1         | 2025-12-15 09:00:00 |
  | G01    | GERENCIA  | 4         | 2025-12-15 09:30:00 |
When el sistema ejecuta el proceso de asignación
Then el sistema asigna G01 primero (prioridad 4 > prioridad 1)
And C01 permanece EN_ESPERA
```

**Escenario 3: Orden FIFO dentro de la misma cola**

```gherkin
Given existe un ejecutivo AVAILABLE
And existen tickets EN_ESPERA en cola PERSONAL_BANKER:
  | numero | createdAt           |
  | P03    | 2025-12-15 10:15:00 |
  | P01    | 2025-12-15 10:00:00 |
  | P02    | 2025-12-15 10:10:00 |
When el sistema ejecuta el proceso de asignación
Then el sistema asigna P01 (createdAt más antiguo)
And P02 y P03 permanecen EN_ESPERA
```

**Escenario 4: Balanceo de carga - Seleccionar ejecutivo con menor carga**

```gherkin
Given existen ejecutivos AVAILABLE:
  | id | name          | assignedTicketsCount | moduleNumber |
  | 1  | Juan Pérez    | 8                    | 1            |
  | 2  | Ana Martínez  | 3                    | 2            |
  | 3  | Carlos López  | 5                    | 4            |
And existe un ticket EN_ESPERA en cola CAJA
When el sistema ejecuta el proceso de asignación
Then el sistema selecciona a Ana Martínez (assignedTicketsCount = 3, el menor)
And el ticket se asigna al módulo 2
```

**Escenario 5: Ejecutivo solo soporta ciertas colas**

```gherkin
Given existen ejecutivos AVAILABLE:
  | id | name         | supportedQueues              | assignedTicketsCount |
  | 1  | Pedro Rojas  | [CAJA]                       | 2                    |
  | 2  | Laura Silva  | [PERSONAL_BANKER, EMPRESAS]  | 3                    |
And existe un ticket EN_ESPERA:
  | numero | queueType       |
  | P05    | PERSONAL_BANKER |
When el sistema ejecuta el proceso de asignación
Then el sistema asigna a Laura Silva (única que soporta PERSONAL_BANKER)
And Pedro Rojas NO es considerado
```

**Escenario 6: Sin ejecutivos disponibles - Ticket permanece en espera**

```gherkin
Given todos los ejecutivos tienen status BUSY o OFFLINE:
  | id | name          | status  |
  | 1  | Juan Pérez    | BUSY    |
  | 2  | Ana Martínez  | OFFLINE |
  | 3  | Carlos López  | BUSY    |
And existe un ticket EN_ESPERA
When el sistema ejecuta el proceso de asignación
Then el sistema NO asigna el ticket
And el ticket permanece EN_ESPERA
And el sistema reintentará en el próximo ciclo
```

**Escenario 7: Múltiples tickets - Asignación secuencial respetando prioridades**

```gherkin
Given existen 2 ejecutivos AVAILABLE
And existen tickets EN_ESPERA:
  | numero | queueType       | prioridad | createdAt           |
  | C01    | CAJA            | 1         | 2025-12-15 09:00:00 |
  | E01    | EMPRESAS        | 3         | 2025-12-15 09:10:00 |
  | P01    | PERSONAL_BANKER | 2         | 2025-12-15 09:05:00 |
  | G01    | GERENCIA        | 4         | 2025-12-15 09:15:00 |
When el sistema ejecuta el proceso de asignación
Then el orden de asignación es:
  | Orden | Ticket | Razón                           |
  | 1     | G01    | Prioridad 4 (máxima)            |
  | 2     | E01    | Prioridad 3 (segunda más alta)  |
And C01 y P01 permanecen EN_ESPERA (solo 2 ejecutivos disponibles)
```

**Escenario 8: Desempate por lastAssignmentAt cuando assignedTicketsCount es igual**

```gherkin
Given existen ejecutivos AVAILABLE con misma carga:
  | id | name         | assignedTicketsCount | lastAssignmentAt    |
  | 1  | Juan Pérez   | 5                    | 2025-12-15 09:00:00 |
  | 2  | Ana Martínez | 5                    | 2025-12-15 09:30:00 |
And existe un ticket EN_ESPERA
When el sistema ejecuta el proceso de asignación
Then el sistema selecciona a Juan Pérez (lastAssignmentAt más antiguo)
```

**Postcondiciones:**
- Ticket asignado con status ATENDIENDO
- Ejecutivo marcado como BUSY
- Contador assignedTicketsCount incrementado
- Mensaje 3 programado para envío
- Evento de auditoría registrado
- Transacción completada atómicamente

**Endpoints HTTP:**
- Ninguno (proceso automático interno)
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de ejecutivo manualmente (trigger de asignación)

---

### RF-005: Gestionar Múltiples Colas

**Descripción:** El sistema debe gestionar cuatro tipos de cola con diferentes características operacionales: CAJA (transacciones básicas, 5 min promedio, prioridad baja), PERSONAL_BANKER (productos financieros, 15 min promedio, prioridad media), EMPRESAS (clientes corporativos, 20 min promedio, prioridad media), GERENCIA (casos especiales, 30 min promedio, prioridad máxima). Cada cola opera independientemente con sus propias métricas y configuraciones.

**Prioridad:** Alta

**Actor Principal:** Sistema

**Precondiciones:**
- Sistema inicializado con configuración de colas
- Base de datos operativa

**Configuración de Colas:**

| QueueType | Display Name | Tiempo Promedio | Prioridad | Prefijo | Descripción |
|-----------|--------------|-----------------|-----------|---------|---------------|
| CAJA | Caja | 5 min | 1 | C | Transacciones básicas, depósitos, retiros |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 | P | Productos financieros, créditos, inversiones |
| EMPRESAS | Empresas | 20 min | 3 | E | Clientes corporativos, cuentas empresariales |
| GERENCIA | Gerencia | 30 min | 4 | G | Casos especiales, reclamos, situaciones complejas |

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas para asignación
- RN-006: Prefijos por tipo de cola
- RN-010: Tiempo promedio por cola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consultar estado de cola específica**

```gherkin
Given la cola PERSONAL_BANKER tiene:
  | Tickets EN_ESPERA | Tickets ATENDIENDO | Ejecutivos AVAILABLE |
  | 8                 | 3                  | 2                    |
When el supervisor consulta GET /api/admin/queues/PERSONAL_BANKER
Then el sistema retorna HTTP 200 con JSON:
  {
    "queueType": "PERSONAL_BANKER",
    "displayName": "Personal Banker",
    "averageTimeMinutes": 15,
    "priority": 2,
    "ticketsWaiting": 8,
    "ticketsInProgress": 3,
    "availableAdvisors": 2,
    "estimatedWaitForNext": 15
  }
```

**Escenario 2: Estadísticas de cola con métricas detalladas**

```gherkin
Given la cola EMPRESAS tiene tickets completados hoy:
  | Ticket | Tiempo Atención (min) |
  | E01    | 18                    |
  | E02    | 22                    |
  | E03    | 20                    |
  | E04    | 25                    |
When el supervisor consulta GET /api/admin/queues/EMPRESAS/stats
Then el sistema retorna:
  {
    "queueType": "EMPRESAS",
    "ticketsCompletedToday": 4,
    "averageActualTime": 21.25,
    "configuredAverageTime": 20,
    "variance": 1.25,
    "longestWaitTime": 25,
    "shortestWaitTime": 18
  }
```

**Escenario 3: Listar todas las colas con resumen**

```gherkin
Given el sistema tiene las 4 colas configuradas
When el supervisor consulta GET /api/admin/queues
Then el sistema retorna HTTP 200 con array de 4 colas:
  [
    {"queueType": "CAJA", "ticketsWaiting": 5, "priority": 1},
    {"queueType": "PERSONAL_BANKER", "ticketsWaiting": 8, "priority": 2},
    {"queueType": "EMPRESAS", "ticketsWaiting": 3, "priority": 3},
    {"queueType": "GERENCIA", "ticketsWaiting": 1, "priority": 4}
  ]
```

**Escenario 4: Cola vacía sin tickets**

```gherkin
Given la cola GERENCIA no tiene tickets activos
When el supervisor consulta GET /api/admin/queues/GERENCIA
Then el sistema retorna:
  {
    "queueType": "GERENCIA",
    "ticketsWaiting": 0,
    "ticketsInProgress": 0,
    "estimatedWaitForNext": 0
  }
```

**Escenario 5: Validación de tipo de cola inválido**

```gherkin
Given el sistema tiene 4 tipos de cola válidos
When el supervisor consulta GET /api/admin/queues/INVALIDA
Then el sistema retorna HTTP 404 con JSON:
  {
    "error": "QUEUE_NOT_FOUND",
    "mensaje": "Tipo de cola no existe: INVALIDA",
    "tiposValidos": ["CAJA", "PERSONAL_BANKER", "EMPRESAS", "GERENCIA"]
  }
```

**Postcondiciones:**
- Información de colas disponible en tiempo real
- Métricas calculadas correctamente
- Respuestas en < 1 segundo

**Endpoints HTTP:**
- `GET /api/admin/queues` - Listar todas las colas con resumen
- `GET /api/admin/queues/{type}` - Consultar estado de cola específica
- `GET /api/admin/queues/{type}/stats` - Estadísticas detalladas de cola

---

### RF-006: Consultar Estado del Ticket

**Descripción:** El sistema debe permitir al cliente consultar en cualquier momento el estado de su ticket mediante el código de referencia (UUID) o número de ticket, mostrando: estado actual, posición en cola, tiempo estimado actualizado, ejecutivo asignado si aplica, y módulo de atención. La consulta debe estar disponible 24/7 y retornar información actualizada en tiempo real.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Ticket existe en el sistema
- Sistema operativo

**Reglas de Negocio Aplicables:**
- RN-009: Estados de ticket
- RN-010: Cálculo de tiempo estimado

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consulta exitosa de ticket EN_ESPERA**

```gherkin
Given existe un ticket:
  | codigoReferencia | numero | status    | positionInQueue | estimatedWaitMinutes |
  | uuid-123         | C05    | EN_ESPERA | 5               | 25                   |
When el cliente consulta GET /api/tickets/uuid-123
Then el sistema retorna HTTP 200 con JSON:
  {
    "codigoReferencia": "uuid-123",
    "numero": "C05",
    "status": "EN_ESPERA",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 25,
    "queueType": "CAJA",
    "createdAt": "2025-12-15T10:00:00Z",
    "assignedAdvisor": null,
    "assignedModuleNumber": null
  }
```

**Escenario 2: Consulta de ticket ATENDIENDO con ejecutivo asignado**

```gherkin
Given existe un ticket:
  | numero | status      | assignedAdvisor | assignedModuleNumber |
  | P08    | ATENDIENDO  | María González  | 3                    |
When el cliente consulta GET /api/tickets/P08/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "P08",
    "status": "ATENDIENDO",
    "positionInQueue": 0,
    "estimatedWaitMinutes": 0,
    "assignedAdvisor": "María González",
    "assignedModuleNumber": 3,
    "mensaje": "Dirígete al módulo 3"
  }
```

**Escenario 3: Consulta de ticket COMPLETADO**

```gherkin
Given existe un ticket:
  | numero | status      | completedAt         |
  | E02    | COMPLETADO  | 2025-12-15 11:30:00 |
When el cliente consulta GET /api/tickets/E02/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "E02",
    "status": "COMPLETADO",
    "completedAt": "2025-12-15T11:30:00Z",
    "mensaje": "Tu atención ha sido completada"
  }
```

**Escenario 4: Ticket no existe**

```gherkin
Given no existe ticket con codigoReferencia "uuid-999"
When el cliente consulta GET /api/tickets/uuid-999
Then el sistema retorna HTTP 404 con JSON:
  {
    "error": "TICKET_NOT_FOUND",
    "mensaje": "No se encontró ticket con identificador: uuid-999"
  }
```

**Escenario 5: Consulta por número de ticket**

```gherkin
Given existe un ticket con numero "G01"
When el cliente consulta GET /api/tickets/G01/position
Then el sistema retorna HTTP 200 con información del ticket
And el tiempo de respuesta es < 1 segundo
```

**Postcondiciones:**
- Información actualizada retornada
- Sin modificación del estado del ticket
- Respuesta en < 1 segundo

**Endpoints HTTP:**
- `GET /api/tickets/{codigoReferencia}` - Consultar por UUID
- `GET /api/tickets/{numero}/position` - Consultar por número de ticket

---

### RF-007: Panel de Monitoreo para Supervisor

**Descripción:** El sistema debe proveer un dashboard en tiempo real que muestre: resumen de tickets por estado, cantidad de clientes en espera por cola, estado de ejecutivos, tiempos promedio de atención, y alertas de situaciones críticas. El dashboard debe actualizarse automáticamente cada 5 segundos sin intervención del usuario.

**Prioridad:** Media

**Actor Principal:** Supervisor

**Precondiciones:**
- Usuario autenticado con rol SUPERVISOR
- Sistema operativo

**Reglas de Negocio Aplicables:**
- RN-011: Auditoría de accesos al panel

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Dashboard con resumen completo**

```gherkin
Given el sistema tiene:
  | Tickets EN_ESPERA | Tickets ATENDIENDO | Tickets COMPLETADOS hoy | Ejecutivos AVAILABLE |
  | 15                | 5                  | 45                      | 3                    |
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna HTTP 200 con JSON:
  {
    "summary": {
      "ticketsWaiting": 15,
      "ticketsInProgress": 5,
      "ticketsCompletedToday": 45,
      "availableAdvisors": 3,
      "busyAdvisors": 5,
      "offlineAdvisors": 2
    },
    "queuesSummary": [
      {"queueType": "CAJA", "waiting": 8, "avgWaitMinutes": 40},
      {"queueType": "PERSONAL_BANKER", "waiting": 5, "avgWaitMinutes": 75},
      {"queueType": "EMPRESAS", "waiting": 2, "avgWaitMinutes": 40},
      {"queueType": "GERENCIA", "waiting": 0, "avgWaitMinutes": 0}
    ],
    "alerts": [],
    "lastUpdated": "2025-12-15T10:30:00Z"
  }
```

**Escenario 2: Alerta de cola crítica (más de 15 esperando)**

```gherkin
Given la cola CAJA tiene 18 tickets EN_ESPERA
When el supervisor consulta GET /api/admin/dashboard
Then el sistema incluye alerta:
  {
    "alerts": [
      {
        "type": "CRITICAL_QUEUE",
        "severity": "HIGH",
        "queueType": "CAJA",
        "message": "Cola CAJA tiene 18 clientes esperando (umbral: 15)",
        "ticketsWaiting": 18
      }
    ]
  }
```

**Escenario 3: Estado de ejecutivos**

```gherkin
Given existen ejecutivos:
  | name          | status    | moduleNumber | currentTicket |
  | Juan Pérez    | BUSY      | 1            | C05           |
  | Ana Martínez  | AVAILABLE | 2            | null          |
  | Carlos López  | OFFLINE   | 4            | null          |
When el supervisor consulta GET /api/admin/advisors
Then el sistema retorna HTTP 200 con array de ejecutivos:
  [
    {"name": "Juan Pérez", "status": "BUSY", "moduleNumber": 1, "currentTicket": "C05"},
    {"name": "Ana Martínez", "status": "AVAILABLE", "moduleNumber": 2},
    {"name": "Carlos López", "status": "OFFLINE", "moduleNumber": 4}
  ]
```

**Escenario 4: Actualización automática cada 5 segundos**

```gherkin
Given el supervisor tiene el dashboard abierto
When transcurren 5 segundos
Then el sistema envía actualización automática vía WebSocket o polling
And los datos se refrescan sin recarga de página
```

**Escenario 5: Resumen de tiempos promedio**

```gherkin
Given existen tickets completados hoy con tiempos de atención
When el supervisor consulta GET /api/admin/summary
Then el sistema retorna:
  {
    "averageWaitTimeMinutes": 12,
    "averageServiceTimeMinutes": 18,
    "totalTicketsToday": 45,
    "peakHour": "10:00-11:00",
    "ticketsPerHour": 5.6
  }
```

**Escenario 6: Cambiar estado de ejecutivo manualmente**

```gherkin
Given un ejecutivo tiene status AVAILABLE
When el supervisor ejecuta PUT /api/admin/advisors/1/status con body:
  {"status": "OFFLINE"}
Then el sistema actualiza el ejecutivo a OFFLINE
And el sistema retorna HTTP 200
And el sistema registra evento de auditoría: "ADVISOR_STATUS_CHANGED"
```

**Postcondiciones:**
- Dashboard actualizado en tiempo real
- Alertas generadas automáticamente
- Acceso registrado en auditoría
- Respuesta en < 1 segundo

**Endpoints HTTP:**
- `GET /api/admin/dashboard` - Dashboard completo
- `GET /api/admin/summary` - Resumen de métricas
- `GET /api/admin/advisors` - Estado de ejecutivos
- `GET /api/admin/advisors/stats` - Estadísticas de ejecutivos
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de ejecutivo

---

### RF-008: Registrar Auditoría de Eventos

**Descripción:** El sistema debe registrar todos los eventos relevantes del ciclo de vida de tickets y acciones administrativas, incluyendo: creación de tickets, asignaciones, cambios de estado, envío de mensajes, y acciones de usuarios. Cada registro debe incluir timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado para trazabilidad completa.

**Prioridad:** Alta

**Actor Principal:** Sistema (automático)

**Precondiciones:**
- Sistema de auditoría configurado
- Base de datos operativa

**Modelo de Datos (Entidad AuditLog):**

- id: BIGSERIAL (primary key)
- timestamp: Timestamp, fecha/hora del evento
- eventType: String (TICKET_CREADO, TICKET_ASIGNADO, TICKET_COMPLETADO, MENSAJE_ENVIADO, etc.)
- actor: String, usuario o sistema que ejecutó la acción
- entityType: String (TICKET, ADVISOR, MESSAGE)
- entityId: String, identificador de la entidad afectada
- previousState: JSON, estado anterior (nullable)
- newState: JSON, estado nuevo
- metadata: JSON, información adicional del evento

**Tipos de Eventos:**

| EventType | Descripción | Actor | EntityType |
|-----------|--------------|-------|------------|
| TICKET_CREADO | Ticket creado | SYSTEM | TICKET |
| TICKET_ASIGNADO | Ticket asignado a ejecutivo | SYSTEM | TICKET |
| TICKET_COMPLETADO | Atención finalizada | ADVISOR | TICKET |
| TICKET_CANCELADO | Ticket cancelado | SYSTEM/USER | TICKET |
| MENSAJE_ENVIADO | Mensaje Telegram enviado | SYSTEM | MESSAGE |
| MENSAJE_FALLIDO | Mensaje falló después de reintentos | SYSTEM | MESSAGE |
| ADVISOR_STATUS_CHANGED | Estado de ejecutivo cambiado | SUPERVISOR | ADVISOR |
| DASHBOARD_ACCESSED | Acceso al panel administrativo | SUPERVISOR | SYSTEM |

**Reglas de Negocio Aplicables:**
- RN-011: Auditoría obligatoria para todos los eventos críticos

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Auditoría de creación de ticket**

```gherkin
Given un cliente crea un ticket exitosamente
When el sistema completa la creación
Then el sistema registra en audit_log:
  {
    "eventType": "TICKET_CREADO",
    "actor": "SYSTEM",
    "entityType": "TICKET",
    "entityId": "uuid-123",
    "previousState": null,
    "newState": {
      "numero": "C05",
      "status": "EN_ESPERA",
      "queueType": "CAJA"
    },
    "metadata": {
      "nationalId": "12345678-9",
      "branchOffice": "Sucursal Centro"
    }
  }
```

**Escenario 2: Auditoría de asignación de ticket**

```gherkin
Given un ticket es asignado a un ejecutivo
When el sistema completa la asignación
Then el sistema registra en audit_log:
  {
    "eventType": "TICKET_ASIGNADO",
    "actor": "SYSTEM",
    "entityType": "TICKET",
    "entityId": "uuid-123",
    "previousState": {"status": "EN_ESPERA", "assignedAdvisor": null},
    "newState": {"status": "ATENDIENDO", "assignedAdvisor": "María González"},
    "metadata": {
      "advisorId": 1,
      "moduleNumber": 3
    }
  }
```

**Escenario 3: Auditoría de envío de mensaje**

```gherkin
Given un mensaje Telegram es enviado exitosamente
When el sistema recibe confirmación de Telegram API
Then el sistema registra en audit_log:
  {
    "eventType": "MENSAJE_ENVIADO",
    "actor": "SYSTEM",
    "entityType": "MESSAGE",
    "entityId": "msg-456",
    "newState": {"estadoEnvio": "ENVIADO"},
    "metadata": {
      "plantilla": "totem_ticket_creado",
      "telegramMessageId": "12345",
      "intentos": 1
    }
  }
```

**Escenario 4: Auditoría de cambio de estado de ejecutivo**

```gherkin
Given un supervisor cambia el estado de un ejecutivo
When el sistema procesa PUT /api/admin/advisors/1/status
Then el sistema registra en audit_log:
  {
    "eventType": "ADVISOR_STATUS_CHANGED",
    "actor": "supervisor@banco.com",
    "entityType": "ADVISOR",
    "entityId": "1",
    "previousState": {"status": "AVAILABLE"},
    "newState": {"status": "OFFLINE"},
    "metadata": {
      "reason": "Almuerzo",
      "changedBy": "supervisor@banco.com"
    }
  }
```

**Escenario 5: Consulta de auditoría por ticket**

```gherkin
Given existen registros de auditoría para ticket "C05"
When el supervisor consulta GET /api/admin/audit?entityId=uuid-123
Then el sistema retorna HTTP 200 con array de eventos:
  [
    {"eventType": "TICKET_CREADO", "timestamp": "2025-12-15T10:00:00Z"},
    {"eventType": "MENSAJE_ENVIADO", "timestamp": "2025-12-15T10:00:05Z"},
    {"eventType": "TICKET_ASIGNADO", "timestamp": "2025-12-15T10:15:00Z"},
    {"eventType": "TICKET_COMPLETADO", "timestamp": "2025-12-15T10:30:00Z"}
  ]
And los eventos están ordenados por timestamp ascendente
```

**Postcondiciones:**
- Evento registrado en base de datos
- Timestamp preciso almacenado
- Información completa de cambios de estado
- Trazabilidad completa del ciclo de vida

**Endpoints HTTP:**
- `GET /api/admin/audit` - Consultar registros de auditoría (con filtros)
- `GET /api/admin/audit?entityId={id}` - Auditoría por entidad
- `GET /api/admin/audit?eventType={type}` - Auditoría por tipo de evento
- `GET /api/admin/audit?actor={actor}` - Auditoría por actor

---

## 5. Matrices de Trazabilidad

### 5.1 Matriz RF → Beneficio de Negocio

| RF | Requerimiento | Beneficio de Negocio | Métrica de Éxito |
|----|---------------|----------------------|-------------------|
| RF-001 | Crear Ticket Digital | Digitalización del proceso | 100% tickets digitales |
| RF-002 | Enviar Notificaciones | Movilidad del cliente | Reducción abandonos 15% → 5% |
| RF-003 | Calcular Posición | Visibilidad tiempos de espera | NPS 45 → 65 puntos |
| RF-004 | Asignar Automáticamente | Optimización de recursos | +20% tickets atendidos/ejecutivo |
| RF-005 | Gestionar Colas | Priorización inteligente | Tiempo espera GERENCIA < 30min |
| RF-006 | Consultar Estado | Transparencia para cliente | NPS 45 → 65 puntos |
| RF-007 | Panel de Monitoreo | Supervisión operacional | Detección colas críticas < 1min |
| RF-008 | Auditoría | Trazabilidad completa | 100% eventos registrados |

### 5.2 Matriz RF → Endpoints HTTP

| RF | Método | Endpoint | Descripción |
|----|--------|----------|---------------|
| RF-001 | POST | /api/tickets | Crear nuevo ticket |
| RF-002 | - | - | Proceso interno automatizado |
| RF-003 | GET | /api/tickets/{numero}/position | Consultar posición |
| RF-003 | GET | /api/tickets/{uuid} | Consultar ticket completo |
| RF-004 | - | - | Proceso interno automatizado |
| RF-004 | PUT | /api/admin/advisors/{id}/status | Cambiar estado ejecutivo |
| RF-005 | GET | /api/admin/queues | Listar todas las colas |
| RF-005 | GET | /api/admin/queues/{type} | Consultar cola específica |
| RF-005 | GET | /api/admin/queues/{type}/stats | Estadísticas de cola |
| RF-006 | GET | /api/tickets/{uuid} | Consultar por UUID |
| RF-006 | GET | /api/tickets/{numero}/position | Consultar por número |
| RF-007 | GET | /api/admin/dashboard | Dashboard completo |
| RF-007 | GET | /api/admin/summary | Resumen de métricas |
| RF-007 | GET | /api/admin/advisors | Estado de ejecutivos |
| RF-007 | GET | /api/admin/advisors/stats | Estadísticas ejecutivos |
| RF-007 | PUT | /api/admin/advisors/{id}/status | Cambiar estado ejecutivo |
| RF-008 | GET | /api/admin/audit | Consultar auditoría |

**Total de Endpoints: 16**

### 5.3 Matriz RF → Reglas de Negocio

| RF | Reglas de Negocio Aplicables |
|----|------------------------------|
| RF-001 | RN-001, RN-005, RN-006, RN-010 |
| RF-002 | RN-007, RN-008, RN-011, RN-012 |
| RF-003 | RN-003, RN-010 |
| RF-004 | RN-002, RN-003, RN-004, RN-013 |
| RF-005 | RN-002, RN-006, RN-010 |
| RF-006 | RN-009, RN-010 |
| RF-007 | RN-011 |
| RF-008 | RN-011 |

### 5.4 Matriz de Dependencias entre RFs

| RF | Depende de | Descripción de Dependencia |
|----|------------|------------------------------|
| RF-002 | RF-001 | Notificaciones requieren ticket creado |
| RF-003 | RF-001 | Cálculo requiere tickets en cola |
| RF-004 | RF-001, RF-003 | Asignación requiere tickets y cálculo de posición |
| RF-006 | RF-001 | Consulta requiere ticket existente |
| RF-007 | RF-001, RF-004, RF-005 | Dashboard requiere datos de tickets, asignaciones y colas |
| RF-008 | Todos | Auditoría registra eventos de todos los RFs |

---

## 6. Modelo de Datos Consolidado

### 6.1 Entidades Principales

**Ticket**
- codigoReferencia: UUID (PK)
- numero: String
- nationalId: String
- telefono: String (nullable)
- branchOffice: String
- queueType: Enum
- status: Enum
- positionInQueue: Integer
- estimatedWaitMinutes: Integer
- createdAt: Timestamp
- assignedAdvisor: FK a Advisor (nullable)
- assignedModuleNumber: Integer (nullable)
- completedAt: Timestamp (nullable)

**Advisor**
- id: BIGSERIAL (PK)
- name: String
- email: String
- status: Enum
- moduleNumber: Integer
- supportedQueues: Array
- assignedTicketsCount: Integer
- lastAssignmentAt: Timestamp

**Message**
- id: BIGSERIAL (PK)
- ticket_id: FK a Ticket
- plantilla: String
- estadoEnvio: Enum
- fechaProgramada: Timestamp
- fechaEnvio: Timestamp (nullable)
- telegramMessageId: String (nullable)
- intentos: Integer

**AuditLog**
- id: BIGSERIAL (PK)
- timestamp: Timestamp
- eventType: String
- actor: String
- entityType: String
- entityId: String
- previousState: JSON (nullable)
- newState: JSON
- metadata: JSON

### 6.2 Enumeraciones

**QueueType:** CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA

**TicketStatus:** EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO

**AdvisorStatus:** AVAILABLE, BUSY, OFFLINE

**MessageTemplate:** totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno

**MessageStatus:** PENDIENTE, ENVIADO, FALLIDO

---

## 7. Casos de Uso Principales

### CU-001: Flujo Completo de Atención

**Actor:** Cliente

**Flujo:**
1. Cliente crea ticket (RF-001)
2. Sistema envía Mensaje 1 de confirmación (RF-002)
3. Cliente consulta estado periódicamente (RF-006)
4. Sistema calcula posición en tiempo real (RF-003)
5. Cuando posición ≤ 3, sistema envía Mensaje 2 (RF-002)
6. Ejecutivo se libera, sistema asigna ticket (RF-004)
7. Sistema envía Mensaje 3 con módulo (RF-002)
8. Cliente es atendido
9. Sistema registra todos los eventos (RF-008)

**Resultado:** Cliente atendido con visibilidad completa del proceso

### CU-002: Supervisión Operacional

**Actor:** Supervisor

**Flujo:**
1. Supervisor accede al dashboard (RF-007)
2. Sistema muestra estado de colas (RF-005)
3. Sistema muestra estado de ejecutivos (RF-007)
4. Sistema genera alerta de cola crítica (RF-007)
5. Supervisor cambia estado de ejecutivo (RF-004)
6. Sistema registra acción en auditoría (RF-008)

**Resultado:** Supervisión en tiempo real con capacidad de intervención

### CU-003: Análisis de Trazabilidad

**Actor:** Analista de Negocio

**Flujo:**
1. Analista consulta auditoría de ticket específico (RF-008)
2. Sistema retorna historial completo de eventos
3. Analista identifica cuellos de botella
4. Analista consulta estadísticas de colas (RF-005)
5. Analista genera reporte de mejora

**Resultado:** Trazabilidad completa para análisis y mejora continua

---

## 8. Validaciones y Reglas de Formato

### 8.1 Validaciones de Entrada

| Campo | Validación | Ejemplo Válido | Ejemplo Inválido |
|-------|------------|------------------|--------------------|
| nationalId | No vacío, formato RUT chileno | 12345678-9 | "" o "123" |
| telefono | Formato +56XXXXXXXXX o null | +56912345678 | "123" |
| queueType | Enum válido | CAJA | "INVALIDA" |
| branchOffice | No vacío | Sucursal Centro | "" |

### 8.2 Reglas de Formato

**Número de Ticket:**
- Formato: [Prefijo][Número 01-99]
- Ejemplos: C01, P15, E03, G02
- Reseteo: Diario a las 00:00

**UUID:**
- Formato: UUID v4 estándar
- Ejemplo: a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6

**Timestamps:**
- Formato: ISO 8601
- Ejemplo: 2025-12-15T10:30:00Z
- Zona horaria: UTC

---

## 9. Checklist de Validación

### 9.1 Completitud

- [x] 8 Requerimientos Funcionales documentados
- [x] 45+ escenarios Gherkin totales
- [x] 13 Reglas de Negocio numeradas
- [x] 16 Endpoints HTTP mapeados
- [x] 4 Entidades definidas (Ticket, Advisor, Message, AuditLog)
- [x] 5 Enumeraciones especificadas
- [x] 3 Casos de Uso principales
- [x] Matrices de trazabilidad completas

### 9.2 Claridad

- [x] Formato Gherkin correcto (Given/When/Then/And)
- [x] Ejemplos JSON en respuestas HTTP
- [x] Sin ambigüedades en descripciones
- [x] Algoritmos documentados paso a paso
- [x] Reglas de negocio referenciadas correctamente

### 9.3 Trazabilidad

- [x] RF → Beneficio de Negocio mapeado
- [x] RF → Endpoints HTTP mapeado
- [x] RF → Reglas de Negocio mapeado
- [x] Dependencias entre RFs identificadas
- [x] Casos de uso vinculados a RFs

### 9.4 Verificabilidad

- [x] Criterios de aceptación medibles
- [x] Métricas de éxito cuantificables
- [x] Ejemplos concretos en cada escenario
- [x] Respuestas HTTP con códigos de estado
- [x] Tiempos de respuesta especificados

---

## 10. Glosario

| Término | Definición |
|---------|-------------|
| **Ticket** | Turno digital asignado a un cliente para ser atendido en sucursal |
| **Cola** | Fila virtual de tickets esperando atención, organizada por tipo de servicio |
| **Asesor/Ejecutivo** | Empleado bancario que atiende clientes en módulo de atención |
| **Módulo** | Estación de trabajo de un asesor, numerados del 1 al 5 |
| **Chat ID** | Identificador único de usuario en Telegram para envío de mensajes |
| **UUID** | Identificador único universal, formato estándar para código de referencia |
| **FIFO** | First In, First Out - Primero en entrar, primero en salir |
| **Backoff Exponencial** | Estrategia de reintentos con tiempos crecientes (30s, 60s, 120s) |
| **Dashboard** | Panel de control en tiempo real para supervisión operacional |
| **Auditoría** | Registro de eventos del sistema para trazabilidad y análisis |
| **NPS** | Net Promoter Score - Métrica de satisfacción del cliente |
| **RUT** | Rol Único Tributario - Identificación nacional en Chile |
| **Webhook** | Mecanismo de notificación automática vía HTTP |
| **Balanceo de Carga** | Distribución equitativa de tickets entre ejecutivos disponibles |

---

## 11. Resumen Ejecutivo

### Estadísticas del Documento

- **Requerimientos Funcionales:** 8
- **Escenarios Gherkin:** 45+
- **Reglas de Negocio:** 13
- **Endpoints HTTP:** 16
- **Entidades de Datos:** 4
- **Enumeraciones:** 5
- **Casos de Uso:** 3

### Cobertura de Beneficios

| Beneficio Esperado | RFs que Contribuyen | Estado |
|--------------------|---------------------|--------|
| Mejora NPS 45 → 65 | RF-001, RF-002, RF-003, RF-006 | ✅ Cubierto |
| Reducción abandonos 15% → 5% | RF-002, RF-003, RF-006 | ✅ Cubierto |
| +20% tickets/ejecutivo | RF-004, RF-005 | ✅ Cubierto |
| Trazabilidad completa | RF-008 | ✅ Cubierto |

### Próximos Pasos

1. **Revisión por Stakeholders:** Validación de requerimientos con áreas de negocio
2. **Diseño de Arquitectura:** Definir arquitectura técnica basada en estos RFs
3. **Estimación de Esfuerzo:** Calcular story points y planificar sprints
4. **Prototipado:** Crear mockups de interfaces de usuario
5. **Desarrollo:** Implementación iterativa por RFs priorizados

---

## Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Analista de Negocio | | | |
| Product Owner | | | |
| Arquitecto de Software | | | |
| Líder Técnico | | | |

---

**Fin del Documento de Requerimientos Funcionales**

**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Estado:** Completo - Pendiente de Aprobación

