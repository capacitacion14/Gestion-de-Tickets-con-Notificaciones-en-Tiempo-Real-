# GUÍA DE EJECUCIÓN DE TESTS

## 📦 Tests Implementados

### Resumen
- **Total de tests:** 24
- **Clases de test:** 4
- **Cobertura esperada:** >80% en clases existentes

### Estructura Implementada

```
src/test/java/com/banco/ticketero/
├── controller/
│   └── AdminControllerTest.java (6 tests) ✅
├── service/
│   └── TicketLifecycleManagerTest.java (8 tests) ✅
├── model/
│   ├── QueueTypeTest.java (6 tests) ✅
│   └── TicketStatusTest.java (4 tests) ✅
└── testutil/
    └── TestDataBuilder.java ✅
```

## 🚀 Comandos de Ejecución

### Ejecutar todos los tests
```bash
./mvnw test
# o si tienes Maven instalado:
mvn test
```

### Ejecutar tests por capa
```bash
# Tests de Controllers
./mvnw test -Dtest="*ControllerTest"

# Tests de Services
./mvnw test -Dtest="*ServiceTest"

# Tests de Models
./mvnw test -Dtest="QueueTypeTest,TicketStatusTest"
```

### Ejecutar test específico
```bash
./mvnw test -Dtest="QueueTypeTest"
./mvnw test -Dtest="AdminControllerTest"
./mvnw test -Dtest="TicketLifecycleManagerTest"
```

### Generar reporte de cobertura con Jacoco
```bash
./mvnw clean test jacoco:report

# Ver reporte en navegador
open target/site/jacoco/index.html
# En Linux: xdg-open target/site/jacoco/index.html
```

### Ejecutar con logs detallados
```bash
./mvnw test -X
```

## 📊 Detalles de Tests por Clase

### 1. QueueTypeTest (6 tests)
- ✅ calculateEstimatedTime_conPosicion1_debeRetornarTiempoPromedio
- ✅ calculateEstimatedTime_conPosicion5_debeMultiplicarCorrectamente
- ✅ getPrefijo_debeRetornarPrefijoCorrectoPorCola
- ✅ getPrioridad_debeRetornarOrdenCorrecto
- ✅ getVigenciaMinutos_debeRetornarTiempoVigencia
- ✅ getTiempoPromedioMinutos_debeRetornarTiempoAtencion

### 2. TicketStatusTest (4 tests)
- ✅ isActivo_conEstadosActivos_debeRetornarTrue
- ✅ isActivo_conEstadosInactivos_debeRetornarFalse
- ✅ getDescripcion_debeRetornarTextoDescriptivo
- ✅ getEstadosActivos_debeRetornarSoloActivos

### 3. TicketLifecycleManagerTest (8 tests)
**cancelExpiredTickets():**
- ✅ execute_debeIncrementarContadorProcesados
- ✅ execute_debeCompletarseEnMenosDe1Segundo
- ✅ execute_noDebeLanzarExcepciones

**processNotifications():**
- ✅ execute_noDebeLanzarExcepciones
- ✅ execute_debeCompletarseRapidamente

**getStats():**
- ✅ execute_debeRetornarEstadisticasValidas
- ✅ execute_despuesDeEjecucion_debeActualizarStats
- ✅ execute_debeRetornarTimestampReciente

### 4. AdminControllerTest (6 tests)
**getSchedulerStatus():**
- ✅ execute_debeRetornarStats

**runSchedulerManually():**
- ✅ execute_debeEjecutarAmbosSchedulers
- ✅ execute_debeRetornarMensajeExito
- ✅ execute_conError_debeRetornar500

**getDashboard():**
- ✅ execute_debeRetornarDashboardCompleto
- ✅ execute_debeIncluirSchedulerStats

## 🔧 Dependencias Agregadas

### pom.xml
```xml
<!-- Testing Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
</dependency>
```

### Plugin Jacoco
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## ✅ Criterios de Aceptación Cumplidos

- [x] QueueTypeTest: 6/6 tests implementados
- [x] TicketStatusTest: 4/4 tests implementados
- [x] TicketLifecycleManagerTest: 8/8 tests implementados
- [x] AdminControllerTest: 6/6 tests implementados
- [x] TestDataBuilder creado
- [x] Dependencias de testing agregadas
- [x] Jacoco configurado
- [x] Estructura src/test/java/ creada

## 📋 Convenciones Utilizadas

### Naming Pattern
```
methodName_condition_expectedBehavior()
```

### Estructura AAA (Arrange-Act-Assert)
```java
@Test
void methodName_condition_expectedBehavior() {
    // Given - Setup datos y mocks
    
    // When - Ejecutar método bajo prueba
    
    // Then - Verificar resultado
}
```

### Organización con @Nested
```java
@Nested
@DisplayName("methodName()")
class MethodName {
    // Tests agrupados por método
}
```

## 🎯 Próximos Pasos

### Tests Pendientes (requieren implementar services primero)
- [ ] TicketServiceTest (12 tests)
- [ ] AssignmentServiceTest (8 tests)
- [ ] TelegramServiceTest (10 tests)
- [ ] QueueServiceTest (6 tests)
- [ ] AuditServiceTest (5 tests)

**Total pendiente:** 41 tests adicionales para alcanzar 85%+ cobertura

## 🚨 Troubleshooting

### Si Maven no está instalado
```bash
# Usar Maven Wrapper incluido en el proyecto
./mvnw test
```

### Si los tests fallan
```bash
# Limpiar y recompilar
./mvnw clean compile test

# Ver logs detallados
./mvnw test -X
```

### Si Jacoco no genera reporte
```bash
# Asegurarse de ejecutar en orden
./mvnw clean test jacoco:report
```

## 📚 Referencias

- **JUnit 5:** https://junit.org/junit5/docs/current/user-guide/
- **Mockito:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **AssertJ:** https://assertj.github.io/doc/
- **Jacoco:** https://www.jacoco.org/jacoco/trunk/doc/

---

**Versión:** 1.0  
**Fecha:** Diciembre 2024  
**Estado:** ✅ Tests Implementados y Listos para Ejecución
