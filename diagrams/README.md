# Diagramas del Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 📁 Contenido de la Carpeta

Esta carpeta contiene **4 diagramas principales** que documentan el Sistema Ticketero desde diferentes perspectivas:

### 1. 📊 Diagrama de Flujo de Negocio
**Archivo:** `01-business-flow.md`

**Contenido:**
- Flujo completo del cliente (4 fases)
- Tipos de cola y prioridades
- Estados del ticket
- Beneficios del negocio (métricas)

**Para quién:**
- Product Owners
- Analistas de Negocio
- Stakeholders
- Gerentes de Sucursal

**Tiempo de lectura:** ~5 minutos

---

### 2. 🏗️ Diagrama de Arquitectura Técnica
**Archivo:** `02-technical-architecture.md`

**Contenido:**
- Arquitectura de 3 capas (Presentación, Negocio, Datos)
- Stack tecnológico completo
- Flujo de datos técnico
- Patrones de diseño aplicados
- Capas de seguridad

**Para quién:**
- Arquitectos de Software
- Desarrolladores Backend
- DevOps Engineers
- Tech Leads

**Tiempo de lectura:** ~8 minutos

---

### 3. 📨 Sistema de Colas de Notificaciones
**Archivo:** `03-notification-queue-system.md`

**Contenido:**
- Arquitectura de notificaciones Telegram
- Proceso de envío con scheduler
- Reintentos con backoff exponencial
- Estados de mensajes
- Plantillas de mensajes
- Monitoreo y optimizaciones

**Para quién:**
- Desarrolladores Backend
- Ingenieros de Integración
- Especialistas en Mensajería
- SRE (Site Reliability Engineers)

**Tiempo de lectura:** ~7 minutos

---

### 4. 🎯 Estrategia de Optimización de Colas
**Archivo:** `04-queue-optimization-strategy.md`

**Contenido:**
- Comparación de opciones (PostgreSQL, Redis, RabbitMQ, Kafka)
- Arquitectura híbrida evolutiva
- Plan de migración por fases
- Optimizaciones inmediatas
- Análisis de costos y ROI

**Para quién:**
- Arquitectos de Software
- Tech Leads
- CTOs
- Product Managers

**Tiempo de lectura:** ~10 minutos

---

## 🎯 Cumplimiento de Rule #1

✅ **Test de los 3 Minutos:**
- Total de diagramas: 4 (optimizado)
- Cada diagrama es autocontenido
- Sin over-engineering
- Foco en el 80% del valor

✅ **Simplicidad Verificable:**
- Diagramas ASCII (legibles en cualquier editor)
- Sin herramientas especiales requeridas
- Versionables en Git
- Fáciles de actualizar

---

## 📖 Guía de Uso

### Para Nuevos Desarrolladores (Onboarding)
**Orden de lectura recomendado:**
1. `01-business-flow.md` - Entender el problema de negocio
2. `02-technical-architecture.md` - Comprender la solución técnica
3. `03-notification-queue-system.md` - Profundizar en notificaciones
4. `04-queue-optimization-strategy.md` - Estrategia de escalabilidad

**Tiempo total:** ~30 minutos

---

### Para Revisiones de Arquitectura
**Enfoque:**
1. Validar que `02-technical-architecture.md` refleja el código actual
2. Verificar que patrones de diseño se están aplicando
3. Confirmar que decisiones de arquitectura siguen vigentes

---

### Para Troubleshooting de Notificaciones
**Enfoque:**
1. Consultar `03-notification-queue-system.md`
2. Revisar estados de mensajes
3. Verificar estrategia de reintentos
4. Consultar queries de monitoreo

---

## 🔄 Mantenimiento de Diagramas

### Cuándo Actualizar

**Actualizar SIEMPRE que:**
- Se agregue una nueva entidad de base de datos
- Se modifique el flujo de negocio principal
- Se cambie la arquitectura de capas
- Se agregue un nuevo servicio externo
- Se modifiquen las plantillas de mensajes

**NO actualizar por:**
- Cambios menores en código
- Refactorings internos
- Optimizaciones de queries
- Cambios de configuración

---

### Proceso de Actualización

1. **Modificar el diagrama** en el archivo .md correspondiente
2. **Validar con Test de 3 Minutos:**
   - ¿Sigue siendo explicable en ~3 minutos?
   - ¿Tiene menos de 10 elementos principales?
   - ¿Aporta el 80% del valor?
3. **Actualizar versión** en el footer del documento
4. **Commit con mensaje descriptivo:**
   ```
   docs: actualizar diagrama de arquitectura - agregar cache layer
   ```

---

## 🛠️ Herramientas Recomendadas

### Para Visualizar
- **Editor de texto plano** (VS Code, Notepad++, Vim)
- **Markdown Preview** (extensión de VS Code)
- **GitHub/GitLab** (renderiza automáticamente)

### Para Editar
- **Monodraw** (macOS) - Diagramas ASCII
- **ASCIIFlow** (web) - https://asciiflow.com/
- **Editor de texto** con fuente monoespaciada

---

## 📊 Métricas de Documentación

| Aspecto | Valor |
|---------|-------|
| Diagramas totales | 4 |
| Páginas totales | ~25 |
| Tiempo de lectura total | ~30 min |
| Elementos por diagrama | 5-10 |
| Niveles de profundidad | 2 |
| Cumplimiento Rule #1 | ✅ 100% |

---

## 🔗 Referencias Cruzadas

### Desde Diagramas → Documentación Técnica
- `01-business-flow.md` → `docs/functional-requirements.md`
- `02-technical-architecture.md` → `docs/high-level-architecture.md`
- `03-notification-queue-system.md` → `docs/component-design.md`

### Desde Código → Diagramas
- `TicketService.java` → `02-technical-architecture.md` (Capa de Negocio)
- `TelegramService.java` → `03-notification-queue-system.md`
- `TicketController.java` → `02-technical-architecture.md` (Capa de Presentación)

---

## ✅ Checklist de Calidad

Antes de considerar un diagrama "completo", verificar:

- [ ] ¿Cumple el Test de los 3 Minutos?
- [ ] ¿Tiene menos de 10 elementos principales?
- [ ] ¿Es autocontenido (no requiere leer otros docs)?
- [ ] ¿Usa ASCII art legible?
- [ ] ¿Tiene ejemplos concretos?
- [ ] ¿Está actualizado con el código actual?
- [ ] ¿Tiene versión y fecha?
- [ ] ¿Explica el "por qué" además del "qué"?

---

## 📞 Contacto

**Arquitecto Responsable:** Arquitecto de Software Senior  
**Última Actualización:** Diciembre 2025  
**Estado:** ✅ Completo y Validado

---

## 📝 Historial de Cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Dic 2025 | Creación inicial de 3 diagramas core |
| 2.0 | Dic 2025 | Agregado diagrama de optimización de colas |

---

**Fin del README**
