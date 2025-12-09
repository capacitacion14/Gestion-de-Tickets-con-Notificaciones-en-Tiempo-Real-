# Diagrama de Flujo de Negocio - Sistema Ticketero

## Flujo Completo del Cliente

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE NEGOCIO COMPLETO                        │
└─────────────────────────────────────────────────────────────────────┘

FASE 1: EMISIÓN DE TICKET
┌──────────┐
│ Cliente  │
│ llega a  │
│ sucursal │
└────┬─────┘
     │
     ▼
┌─────────────────┐
│ Terminal        │
│ Autoservicio    │
│                 │
│ 1. Ingresa RUT  │
│ 2. Ingresa Tel  │
│ 3. Selecciona   │
│    tipo cola    │
└────┬────────────┘
     │
     ▼
┌─────────────────────────┐
│ Sistema genera:         │
│ • Número: C05           │
│ • Posición: #5          │
│ • Tiempo: 25 min        │
└────┬────────────────────┘
     │
     ├──────────────────────┐
     │                      │
     ▼                      ▼
┌─────────────┐    ┌──────────────────┐
│ Imprime     │    │ Envía Mensaje 1  │
│ ticket      │    │ (Confirmación)   │
│ físico      │    │ vía Telegram     │
└─────────────┘    └──────────────────┘


FASE 2: ESPERA Y MOVILIDAD
┌──────────────┐
│ Cliente sale │
│ de sucursal  │
│ (café, etc)  │
└──────┬───────┘
       │
       │ Sistema monitorea
       │ automáticamente
       │
       ▼
┌─────────────────────┐
│ Posición cambia:    │
│ #5 → #4 → #3        │
└──────┬──────────────┘
       │
       │ Cuando posición ≤ 3
       │
       ▼
┌──────────────────────┐
│ Envía Mensaje 2      │
│ (Pre-aviso)          │
│ "¡Pronto tu turno!"  │
└──────┬───────────────┘
       │
       ▼
┌──────────────┐
│ Cliente      │
│ regresa a    │
│ sucursal     │
└──────────────┘


FASE 3: ASIGNACIÓN Y ATENCIÓN
┌─────────────────┐
│ Ejecutivo se    │
│ libera          │
└────┬────────────┘
     │
     ▼
┌─────────────────────────┐
│ Sistema asigna          │
│ automáticamente:        │
│ • Prioridad de cola     │
│ • Orden FIFO            │
│ • Balanceo de carga     │
└────┬────────────────────┘
     │
     ├──────────────────────┐
     │                      │
     ▼                      ▼
┌─────────────────┐  ┌──────────────────┐
│ Envía Mensaje 3 │  │ Notifica a       │
│ (Turno Activo)  │  │ ejecutivo en     │
│ "Módulo 3"      │  │ su terminal      │
└─────────────────┘  └──────────────────┘
     │
     ▼
┌──────────────────┐
│ Cliente va a     │
│ módulo indicado  │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Ejecutivo        │
│ atiende cliente  │
└────┬─────────────┘
     │
     ▼
┌──────────────────┐
│ Atención         │
│ completada       │
└──────────────────┘


FASE 4: SUPERVISIÓN (Paralelo)
┌──────────────────┐
│ Supervisor       │
│ monitorea        │
│ dashboard        │
└────┬─────────────┘
     │
     ▼
┌─────────────────────────┐
│ Ve en tiempo real:      │
│ • 15 en espera          │
│ • 5 siendo atendidos    │
│ • 3 ejecutivos libres   │
│ • Alertas de colas      │
└─────────────────────────┘
```

---

## Tipos de Cola y Prioridades

```
┌────────────────────────────────────────────────────┐
│              GESTIÓN DE 4 COLAS                    │
└────────────────────────────────────────────────────┘

PRIORIDAD 4 (MÁXIMA)
┌─────────────────────────┐
│ GERENCIA (G)            │
│ • Casos especiales      │
│ • Tiempo: 30 min        │
│ • Prefijo: G01, G02...  │
└─────────────────────────┘

PRIORIDAD 3
┌─────────────────────────┐
│ EMPRESAS (E)            │
│ • Clientes corporativos │
│ • Tiempo: 20 min        │
│ • Prefijo: E01, E02...  │
└─────────────────────────┘

PRIORIDAD 2
┌─────────────────────────┐
│ PERSONAL BANKER (P)     │
│ • Productos financieros │
│ • Tiempo: 15 min        │
│ • Prefijo: P01, P02...  │
└─────────────────────────┘

PRIORIDAD 1 (MÍNIMA)
┌─────────────────────────┐
│ CAJA (C)                │
│ • Transacciones básicas │
│ • Tiempo: 5 min         │
│ • Prefijo: C01, C02...  │
└─────────────────────────┘
```

---

## Estados del Ticket

```
┌──────────────────────────────────────────────────┐
│           CICLO DE VIDA DEL TICKET               │
└──────────────────────────────────────────────────┘

    [CREADO]
       │
       ▼
┌──────────────┐
│  EN_ESPERA   │ ◄─── Estado inicial
└──────┬───────┘      Esperando asignación
       │
       │ Cuando posición ≤ 3
       │
       ▼
┌──────────────┐
│   PROXIMO    │ ◄─── Pre-aviso enviado
└──────┬───────┘      Cliente debe acercarse
       │
       │ Ejecutivo disponible
       │
       ▼
┌──────────────┐
│  ATENDIENDO  │ ◄─── Asignado a ejecutivo
└──────┬───────┘      En módulo específico
       │
       │ Atención finalizada
       │
       ▼
┌──────────────┐
│  COMPLETADO  │ ◄─── Estado final exitoso
└──────────────┘


Estados Alternativos:
┌──────────────┐
│  CANCELADO   │ ◄─── Cliente cancela
└──────────────┘

┌──────────────┐
│ NO_ATENDIDO  │ ◄─── Cliente no se presentó
└──────────────┘
```

---

## Beneficios del Negocio

```
┌────────────────────────────────────────────────┐
│         IMPACTO EN MÉTRICAS DE NEGOCIO         │
└────────────────────────────────────────────────┘

ANTES (Sistema Manual)
├─ NPS: 45 puntos
├─ Abandonos: 15%
├─ Tickets/ejecutivo: 20/día
└─ Visibilidad: 0%

DESPUÉS (Sistema Digital)
├─ NPS: 65 puntos (+20)
├─ Abandonos: 5% (-10%)
├─ Tickets/ejecutivo: 24/día (+20%)
└─ Visibilidad: 100%

VALOR AGREGADO
├─ Cliente puede salir de sucursal
├─ Notificaciones en tiempo real
├─ Transparencia total del proceso
├─ Asignación optimizada
└─ Trazabilidad completa
```

---

**Versión:** 1.0  
**Fecha:** Diciembre 2025
