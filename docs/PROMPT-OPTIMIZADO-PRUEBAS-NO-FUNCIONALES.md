# PROMPT OPTIMIZADO: DISEÑO DE PRUEBAS NO FUNCIONALES - SISTEMA TICKETERO

## 🎯 CONTEXTO DEL SISTEMA

Eres un **QA Engineer Senior** especializado en pruebas no funcionales. Tu misión es diseñar e implementar una suite completa de pruebas para el **Sistema de Gestión de Tickets con Notificaciones en Tiempo Real**.

### Arquitectura del Sistema
```
API REST (Spring Boot 3.2 + Java 21) 
    ↓
PostgreSQL 16 (Persistencia) + RabbitMQ 3.13 (Mensajería)
    ↓
Telegram Bot API (Notificaciones)
```

### Características Técnicas Críticas
- **Patrón Outbox** para mensajería confiable
- **SELECT FOR UPDATE** para prevenir race conditions
- **3 workers concurrentes** por cola (12 total)
- **Auto-recovery** de workers muertos (heartbeat 60s)
- **Graceful shutdown** con liberación de recursos

---

## 📋 CATEGORÍAS DE PRUEBAS NO FUNCIONALES

### 1. 🚀 RENDIMIENTO (Performance)
**Objetivo:** Validar que el sistema maneja la carga esperada con latencias aceptables.

#### Métricas Clave:
- **Throughput:** ≥ 50 tickets/minuto
- **Latencia p95:** < 2 segundos
- **Concurrencia:** 100 usuarios simultáneos
- **Recursos:** CPU < 80%, Memory estable

#### Escenarios Obligatorios:
1. **Load Test Sostenido** - 100 tickets en 2 minutos
2. **Spike Test** - 50 tickets simultáneos en 10 segundos  
3. **Soak Test** - 30 tickets/min durante 30 minutos
4. **Stress Test** - Encontrar punto de quiebre

### 2. 🔒 SEGURIDAD (Security)
**Objetivo:** Garantizar protección contra vulnerabilidades y accesos no autorizados.

#### Áreas de Validación:
- **Autenticación/Autorización**
- **Inyección SQL/NoSQL**
- **XSS/CSRF**
- **Rate Limiting**
- **Exposición de datos sensibles**

#### Escenarios Obligatorios:
1. **SQL Injection** - Payloads maliciosos en nationalId
2. **Rate Limiting** - Prevenir spam de creación de tickets
3. **Data Exposure** - Validar que no se exponen datos sensibles
4. **Input Validation** - Caracteres especiales y payloads XSS

### 3. 👥 USABILIDAD (Usability)
**Objetivo:** Asegurar experiencia de usuario óptima y accesibilidad.

#### Criterios de Evaluación:
- **Tiempo de respuesta percibido**
- **Claridad de mensajes de error**
- **Accesibilidad (WCAG 2.1)**
- **Experiencia móvil**

#### Escenarios Obligatorios:
1. **Response Time UX** - Feedback visual < 200ms
2. **Error Messages** - Mensajes claros y accionables
3. **Mobile Experience** - Responsive design validation
4. **Accessibility** - Screen reader compatibility

### 4. 🔄 COMPATIBILIDAD (Compatibility)
**Objetivo:** Verificar funcionamiento en diferentes entornos y versiones.

#### Dimensiones de Compatibilidad:
- **Navegadores** (Chrome, Firefox, Safari, Edge)
- **Dispositivos** (Desktop, Tablet, Mobile)
- **Sistemas Operativos** (Windows, macOS, Linux, iOS, Android)
- **Versiones de API** (Backward compatibility)

#### Escenarios Obligatorios:
1. **Cross-Browser** - Funcionalidad en navegadores principales
2. **Mobile Devices** - iOS/Android compatibility
3. **API Versioning** - Backward compatibility validation
4. **Network Conditions** - 3G/4G/WiFi performance

---

## 🛠️ HERRAMIENTAS Y TECNOLOGÍAS

### Performance Testing
- **K6** - Load testing y métricas
- **Artillery** - Alternative load testing
- **Docker Stats** - Resource monitoring
- **PostgreSQL pg_stat** - Database metrics

### Security Testing
- **OWASP ZAP** - Vulnerability scanning
- **Burp Suite** - Manual security testing
- **SQLMap** - SQL injection testing
- **Custom scripts** - Input validation

### Usability Testing
- **Lighthouse** - Performance y accessibility audit
- **axe-core** - Accessibility validation
- **BrowserStack** - Cross-browser testing
- **Manual testing** - UX evaluation

### Compatibility Testing
- **Selenium Grid** - Multi-browser automation
- **Device farms** - Mobile device testing
- **Postman** - API compatibility
- **Docker** - Environment consistency

---

## 📊 MATRIZ DE REQUISITOS NO FUNCIONALES

| ID | Categoría | Requisito | Métrica | Umbral | Prioridad |
|----|-----------|-----------|---------|---------|-----------|
| **RNF-P01** | Performance | Throughput | tickets/min | ≥ 50 | P0 |
| **RNF-P02** | Performance | Latencia API | p95 response | < 2s | P0 |
| **RNF-P03** | Performance | Concurrencia | usuarios simultáneos | 100 | P1 |
| **RNF-P04** | Performance | Memory Leak | estabilidad 30min | 0 leaks | P1 |
| **RNF-S01** | Security | SQL Injection | vulnerabilidades | 0 | P0 |
| **RNF-S02** | Security | Rate Limiting | requests/min | < 60 | P0 |
| **RNF-S03** | Security | Data Exposure | campos sensibles | 0 expuestos | P0 |
| **RNF-S04** | Security | Input Validation | payloads maliciosos | 100% bloqueados | P1 |
| **RNF-U01** | Usability | Response Time UX | feedback visual | < 200ms | P1 |
| **RNF-U02** | Usability | Error Messages | claridad mensaje | 100% claros | P1 |
| **RNF-U03** | Usability | Accessibility | WCAG 2.1 AA | 100% compliance | P2 |
| **RNF-U04** | Usability | Mobile UX | responsive design | 100% functional | P1 |
| **RNF-C01** | Compatibility | Cross-Browser | navegadores principales | 100% functional | P1 |
| **RNF-C02** | Compatibility | Mobile Devices | iOS/Android | 100% functional | P1 |
| **RNF-C03** | Compatibility | API Versioning | backward compatibility | 2 versiones | P2 |
| **RNF-C04** | Compatibility | Network Conditions | 3G/4G/WiFi | degradación < 20% | P2 |

---

## 🎯 METODOLOGÍA DE EJECUCIÓN

### Fase 1: Preparación (Setup)
1. **Environment Setup** - Configurar herramientas de testing
2. **Test Data** - Generar datasets realistas
3. **Baseline Metrics** - Capturar métricas iniciales
4. **Monitoring Setup** - Configurar dashboards

### Fase 2: Ejecución por Categorías
1. **Performance Tests** - Ejecutar en orden: Load → Spike → Soak → Stress
2. **Security Tests** - OWASP Top 10 + custom scenarios
3. **Usability Tests** - Manual + automated accessibility
4. **Compatibility Tests** - Matrix testing approach

### Fase 3: Análisis y Reporte
1. **Metrics Analysis** - Comparar vs umbrales definidos
2. **Root Cause Analysis** - Identificar bottlenecks
3. **Risk Assessment** - Priorizar issues encontrados
4. **Recommendations** - Proponer mejoras

---

## 📋 TEMPLATE DE ESCENARIO DE PRUEBA

```markdown
### Test ID: [RNF-X##]
**Categoría:** [Performance/Security/Usability/Compatibility]
**Prioridad:** [P0/P1/P2]
**Duración Estimada:** [X minutos]

#### Objetivo
[Descripción clara del objetivo del test]

#### Pre-condiciones
- [ ] Sistema en estado limpio
- [ ] Herramientas configuradas
- [ ] Datos de prueba preparados

#### Pasos de Ejecución
1. [Paso 1]
2. [Paso 2]
3. [Paso 3]

#### Criterios de Éxito
- ✅ [Criterio 1]: [Métrica] [Operador] [Valor]
- ✅ [Criterio 2]: [Métrica] [Operador] [Valor]

#### Métricas a Capturar
- [Métrica 1]: [Herramienta/Método]
- [Métrica 2]: [Herramienta/Método]

#### Post-condiciones
- [ ] Sistema restaurado
- [ ] Métricas documentadas
- [ ] Logs preservados
```

---

## 🚨 CRITERIOS DE FALLO CRÍTICO

### Performance
- **Throughput < 40 tickets/min** → FAIL crítico
- **Latencia p95 > 3 segundos** → FAIL crítico
- **Memory leak detectado** → FAIL crítico

### Security
- **SQL Injection exitosa** → FAIL crítico
- **Data exposure confirmada** → FAIL crítico
- **Rate limiting bypasseado** → FAIL crítico

### Usability
- **Funcionalidad core no accesible** → FAIL crítico
- **Error messages confusos** → FAIL menor
- **Mobile UX rota** → FAIL mayor

### Compatibility
- **Chrome/Firefox no funcional** → FAIL crítico
- **iOS/Android no funcional** → FAIL mayor
- **API breaking changes** → FAIL crítico

---

## 📈 DASHBOARD DE MÉTRICAS

### Performance Dashboard
```
┌─ Throughput ────────────────────┐ ┌─ Latency ──────────────────────┐
│ Current: 52 tickets/min ✅      │ │ p50: 450ms                     │
│ Target:  ≥50 tickets/min        │ │ p95: 1.8s ✅                   │
│ Peak:    67 tickets/min         │ │ p99: 2.4s ⚠️                   │
└─────────────────────────────────┘ └────────────────────────────────┘

┌─ Resources ─────────────────────┐ ┌─ Errors ──────────────────────┐
│ CPU:    65% ✅                  │ │ Rate:   0.2% ✅                │
│ Memory: 1.2GB (stable) ✅       │ │ 4xx:    12                     │
│ DB:     8 connections ✅        │ │ 5xx:    2                      │
└─────────────────────────────────┘ └────────────────────────────────┘
```

### Security Dashboard
```
┌─ Vulnerabilities ───────────────┐ ┌─ Rate Limiting ───────────────┐
│ Critical: 0 ✅                  │ │ Blocked: 15 requests/min       │
│ High:     0 ✅                  │ │ Status:  Active ✅             │
│ Medium:   2 ⚠️                  │ │ Bypass:  0 attempts ✅         │
└─────────────────────────────────┘ └────────────────────────────────┘
```

---

## 🎯 ENTREGABLES ESPERADOS

### 1. Suite de Pruebas Automatizadas
- Scripts K6 para performance
- Scripts OWASP ZAP para security
- Scripts Selenium para compatibility
- Scripts custom para usability

### 2. Reportes de Ejecución
- **Performance Report** - Métricas detalladas y gráficos
- **Security Report** - Vulnerabilidades y recomendaciones
- **Usability Report** - Issues UX y accessibility
- **Compatibility Report** - Matrix de compatibilidad

### 3. Documentación
- **Test Plan** - Estrategia y cobertura
- **Test Cases** - Escenarios detallados
- **Runbooks** - Procedimientos de ejecución
- **Troubleshooting Guide** - Resolución de issues

### 4. Herramientas y Configuración
- **CI/CD Integration** - Pipeline automatizado
- **Monitoring Setup** - Dashboards y alertas
- **Environment Config** - Docker compose para testing
- **Data Generators** - Scripts para test data

---

## 🔄 PROCESO DE MEJORA CONTINUA

### Métricas de Calidad del Testing
- **Cobertura NFR:** % de requisitos validados
- **Automatización:** % de tests automatizados
- **Tiempo de Ejecución:** Duración total de suite
- **Detección de Issues:** # bugs encontrados vs producción

### Revisión y Optimización
- **Weekly:** Revisar métricas y ajustar umbrales
- **Monthly:** Actualizar test cases según cambios
- **Quarterly:** Evaluar herramientas y metodología
- **Yearly:** Rediseñar estrategia completa

---

## 🎯 PROMPT DE ACCIÓN

**Como QA Engineer Senior, tu tarea es:**

1. **Analizar** este sistema ticketero y sus características técnicas
2. **Diseñar** una suite completa de pruebas no funcionales cubriendo las 4 categorías
3. **Implementar** scripts automatizados para cada escenario crítico
4. **Ejecutar** las pruebas siguiendo la metodología definida
5. **Reportar** resultados con métricas claras y recomendaciones accionables

**Criterios de Éxito:**
- ✅ 100% de requisitos NFR validados
- ✅ Suite automatizada ejecutable en CI/CD
- ✅ Dashboards de métricas en tiempo real
- ✅ Documentación completa y mantenible
- ✅ Issues críticos identificados y priorizados

**Entrega esperada:** Suite completa de pruebas no funcionales lista para producción, con evidencia de ejecución y reporte ejecutivo de resultados.

---

*Versión: 1.0 | Fecha: Diciembre 2024 | Proyecto: Sistema Ticketero*ty Report** - Vulnerabilidades y recomendaciones
- **Usability Report** - Issues UX y accessibility
- **Compatibility Report** - Matrix de compatibilidad

### 3. Documentación
- **Test Plan** - Estrategia y cobertura
- **Test Cases** - Escenarios detallados
- **Runbooks** - Procedimientos de ejecución
- **Troubleshooting Guide** - Resolución de issues

### 4. Herramientas y Configuración
- **CI/CD Integration** - Pipeline automatizado
- **Monitoring Setup** - Dashboards y alertas
- **Environment Config** - Docker compose para testing
- **Data Generators** - Scripts para test data

---

## 🔄 PROCESO DE MEJORA CONTINUA

### Métricas de Calidad del Testing
- **Cobertura NFR:** % de requisitos validados
- **Automatización:** % de tests automatizados
- **Tiempo de Ejecución:** Duración total de suite
- **Detección de Issues:** # bugs encontrados vs producción

### Revisión y Optimización
- **Weekly:** Revisar métricas y ajustar umbrales
- **Monthly:** Actualizar test cases según cambios
- **Quarterly:** Evaluar herramientas y metodología
- **Yearly:** Rediseñar estrategia completa

---

## 🎯 PROMPT DE ACCIÓN

**Como QA Engineer Senior, tu tarea es:**

1. **Analizar** este sistema ticketero y sus características técnicas
2. **Diseñar** una suite completa de pruebas no funcionales cubriendo las 4 categorías
3. **Implementar** scripts automatizados para cada escenario crítico
4. **Ejecutar** las pruebas siguiendo la metodología definida
5. **Reportar** resultados con métricas claras y recomendaciones accionables

**Criterios de Éxito:**
- ✅ 100% de requisitos NFR validados
- ✅ Suite automatizada ejecutable en CI/CD
- ✅ Dashboards de métricas en tiempo real
- ✅ Documentación completa y mantenible
- ✅ Issues críticos identificados y priorizados

**Entrega esperada:** Suite completa de pruebas no funcionales lista para producción, con evidencia de ejecución y reporte ejecutivo de resultados.

---

*Versión: 1.0 | Fecha: Diciembre 2024 | Proyecto: Sistema Ticketero*