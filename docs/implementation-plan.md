# PLAN DETALLADO DE IMPLEMENTACIÓN - SISTEMA TICKETERO

**Versión:** 1.0  
**Tech Lead:** Senior Developer  
**Fecha:** Diciembre 2024  
**Arquitectura:** Clean Architecture / Hexagonal  

---

## 1. ESTRUCTURA DE PAQUETES JAVA (Clean Architecture/Hexagonal)

```
src/main/java/com/banco/ticketero/
├── domain/                           # CAPA DE DOMINIO (Core Business Logic)
│   ├── model/                        # Entidades de dominio
│   │   ├── ticket/
│   │   │   ├── Ticket.java          # Aggregate Root
│   │   │   ├── TicketId.java        # Value Object
│   │   │   ├── TicketStatus.java    # Enum
│   │   │   └── TicketCode.java      # Value Object
│   │   ├── queue/
│   │   │   ├── Queue.java           # Aggregate Root
│   │   │   ├── QueueId.java         # Value Object
│   │   │   └── QueueType.java       # Enum
│   │   ├── customer/
│   │   │   ├── Customer.java        # Aggregate Root
│   │   │   ├── CustomerId.java      # Value Object
│   │   │   └── NationalId.java      # Value Object
│   │   └── notification/
│   │       ├── Notification.java    # Aggregate Root
│   │       ├── NotificationId.java  # Value Object
│   │       └── NotificationType.java # Enum
│   ├── repository/                   # Interfaces de repositorio (Ports)
│   │   ├── TicketRepository.java
│   │   ├── QueueRepository.java
│   │   ├── CustomerRepository.java
│   │   └── NotificationRepository.java
│   ├── service/                      # Servicios de dominio
│   │   ├── TicketDomainService.java
│   │   ├── QueueDomainService.java
│   │   └── NotificationDomainService.java
│   └── exception/                    # Excepciones de dominio
│       ├── TicketNotFoundException.java
│       ├── InvalidTicketStatusException.java
│       └── QueueFullException.java
│
├── application/                      # CAPA DE APLICACIÓN (Use Cases)
│   ├── usecase/                      # Casos de uso
│   │   ├── ticket/
│   │   │   ├── CreateTicketUseCase.java
│   │   │   ├── GetTicketUseCase.java
│   │   │   ├── UpdateTicketStatusUseCase.java
│   │   │   └── CancelTicketUseCase.java
│   │   ├── queue/
│   │   │   ├── GetQueueStatusUseCase.java
│   │   │   ├── CallNextTicketUseCase.java
│   │   │   └── GetQueueMetricsUseCase.java
│   │   └── notification/
│   │       ├── SendNotificationUseCase.java
│   │       └── ScheduleNotificationUseCase.java
│   ├── dto/                          # DTOs de aplicación
│   │   ├── request/
│   │   │   ├── CreateTicketRequest.java
│   │   │   ├── UpdateTicketStatusRequest.java
│   │   │   └── SendNotificationRequest.java
│   │   └── response/
│   │       ├── TicketResponse.java
│   │       ├── QueueStatusResponse.java
│   │       └── NotificationResponse.java
│   ├── port/                         # Puertos de salida (Interfaces)
│   │   ├── out/
│   │   │   ├── NotificationPort.java
│   │   │   ├── MessageQueuePort.java
│   │   │   └── TelegramPort.java
│   │   └── in/
│   │       ├── TicketUseCasePort.java
│   │       ├── QueueUseCasePort.java
│   │       └── NotificationUseCasePort.java
│   └── service/                      # Servicios de aplicación
│       ├── TicketApplicationService.java
│       ├── QueueApplicationService.java
│       └── NotificationApplicationService.java
│
├── infrastructure/                   # CAPA DE INFRAESTRUCTURA (Adapters)
│   ├── adapter/                      # Adaptadores
│   │   ├── in/                       # Adaptadores de entrada
│   │   │   ├── web/                  # Controllers REST
│   │   │   │   ├── TicketController.java
│   │   │   │   ├── QueueController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── HealthController.java
│   │   │   └── scheduler/            # Tareas programadas
│   │   │       ├── NotificationScheduler.java
│   │   │       └── QueueCleanupScheduler.java
│   │   └── out/                      # Adaptadores de salida
│   │       ├── persistence/          # JPA Repositories
│   │       │   ├── entity/           # JPA Entities
│   │       │   │   ├── TicketEntity.java
│   │       │   │   ├── QueueEntity.java
│   │       │   │   ├── CustomerEntity.java
│   │       │   │   └── NotificationEntity.java
│   │       │   ├── repository/       # JPA Repository Implementations
│   │       │   │   ├── TicketJpaRepository.java
│   │       │   │   ├── QueueJpaRepository.java
│   │       │   │   ├── CustomerJpaRepository.java
│   │       │   │   └── NotificationJpaRepository.java
│   │       │   └── mapper/           # Entity ↔ Domain Mappers
│   │       │       ├── TicketEntityMapper.java
│   │       │       ├── QueueEntityMapper.java
│   │       │       └── CustomerEntityMapper.java
│   │       ├── notification/         # Adaptadores de notificación
│   │       │   ├── TelegramAdapter.java
│   │       │   ├── EmailAdapter.java
│   │       │   └── SmsAdapter.java
│   │       └── messaging/            # Message Queue Adapters
│   │           ├── RabbitMqAdapter.java
│   │           └── KafkaAdapter.java
│   ├── config/                       # Configuraciones
│   │   ├── DatabaseConfig.java
│   │   ├── TelegramConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── SchedulerConfig.java
│   │   └── SwaggerConfig.java
│   └── exception/                    # Exception Handlers
│       ├── GlobalExceptionHandler.java
│       ├── ValidationExceptionHandler.java
│       └── ErrorResponse.java
│
└── TicketeroApplication.java         # Main Application Class

src/main/resources/
├── db/migration/                     # Flyway Migrations
│   ├── V1__baseline.sql
│   ├── V2__user_tables.sql
│   └── V3__ticket_tables.sql
├── application.yml                   # Configuración principal
├── application-dev.yml               # Configuración desarrollo
├── application-prod.yml              # Configuración producción
└── logback-spring.xml               # Configuración logging

src/test/java/com/banco/ticketero/
├── domain/                           # Tests unitarios dominio
├── application/                      # Tests unitarios aplicación
├── infrastructure/                   # Tests integración
├── integration/                      # Tests end-to-end
└── testcontainers/                  # Tests con Testcontainers
```

### 1.1 PRINCIPIOS DE LA ESTRUCTURA

**Dependencias (Regla de Dependencia):**
```
Infrastructure → Application → Domain
```

**Responsabilidades por Capa:**

1. **Domain (Núcleo):**
   - Lógica de negocio pura
   - Entidades y Value Objects
   - Reglas de dominio
   - Sin dependencias externas

2. **Application (Casos de Uso):**
   - Orquestación de casos de uso
   - DTOs de entrada/salida
   - Interfaces (Ports)
   - Coordinación entre dominio e infraestructura

3. **Infrastructure (Adaptadores):**
   - Implementación de puertos
   - Acceso a datos (JPA)
   - APIs externas (Telegram)
   - Configuraciones técnicas

### 1.2 CONVENCIONES DE NAMING

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| **Entities** | `{Nombre}.java` | `Ticket.java` |
| **Value Objects** | `{Nombre}Id.java` o `{Nombre}.java` | `TicketId.java`, `NationalId.java` |
| **Repositories** | `{Entity}Repository.java` | `TicketRepository.java` |
| **Use Cases** | `{Accion}{Entity}UseCase.java` | `CreateTicketUseCase.java` |
| **Controllers** | `{Entity}Controller.java` | `TicketController.java` |
| **DTOs Request** | `{Accion}{Entity}Request.java` | `CreateTicketRequest.java` |
| **DTOs Response** | `{Entity}Response.java` | `TicketResponse.java` |
| **JPA Entities** | `{Entity}Entity.java` | `TicketEntity.java` |
| **Adapters** | `{Tecnologia}Adapter.java` | `TelegramAdapter.java` |

---

**🛑 PUNTO DE REVISIÓN 1**

He completado la **Estructura de Paquetes Java** siguiendo Clean Architecture/Hexagonal con:

✅ Separación clara de capas (Domain → Application → Infrastructure)  
✅ Convenciones de naming consistentes  
✅ Organización por agregados de dominio  
✅ Puertos y adaptadores bien definidos  
✅ Estructura de tests alineada  

**¿Procedo con la siguiente sección (Plan de Migraciones SQL) o necesitas revisar/ajustar algo en esta estructura?**

---

## 2. PLAN DE MIGRACIONES SQL (Flyway)

### 2.1 ESTRATEGIA DE MIGRACIONES

**Principios:**
- Una migración por funcionalidad
- Migraciones incrementales e irreversibles
- Naming: `V{VERSION}__{description}.sql`
- Baseline primero, luego entidades por dominio

### 2.2 ARCHIVOS DE MIGRACIÓN INICIALES

#### V1__baseline.sql
```sql
-- =====================================================
-- BASELINE: Configuración inicial de BD
-- Versión: 1.0
-- Descripción: Esquemas, extensiones y configuraciones
-- =====================================================

-- Crear esquema principal
CREATE SCHEMA IF NOT EXISTS ticketero;
SET search_path TO ticketero;

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Función para timestamps automáticos
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Secuencias para códigos de ticket
CREATE SEQUENCE ticket_code_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    MAXVALUE 9999
    CYCLE;

-- Tipos ENUM
CREATE TYPE ticket_status AS ENUM (
    'PENDING',
    'CALLED', 
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
    'NO_SHOW'
);

CREATE TYPE queue_type AS ENUM (
    'GENERAL',
    'PRIORITY',
    'BUSINESS',
    'VIP'
);

CREATE TYPE notification_type AS ENUM (
    'TICKET_CREATED',
    'TICKET_CALLED',
    'QUEUE_UPDATE',
    'REMINDER'
);

CREATE TYPE notification_status AS ENUM (
    'PENDING',
    'SENT',
    'FAILED',
    'CANCELLED'
);

-- Comentarios de documentación
COMMENT ON SCHEMA ticketero IS 'Sistema de gestión de tickets bancarios';
COMMENT ON TYPE ticket_status IS 'Estados posibles de un ticket';
COMMENT ON TYPE queue_type IS 'Tipos de cola de atención';
```

#### V2__user_tables.sql
```sql
-- =====================================================
-- USUARIOS Y CLIENTES
-- Versión: 2.0
-- Descripción: Tablas de clientes y configuración
-- =====================================================

SET search_path TO ticketero;

-- Tabla de clientes
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    national_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    telegram_chat_id BIGINT,
    is_vip BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_national_id_format CHECK (national_id ~ '^[0-9]{8,20}$'),
    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tabla de configuración de colas
CREATE TABLE queue_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    queue_type queue_type NOT NULL UNIQUE,
    max_capacity INTEGER NOT NULL DEFAULT 50,
    estimated_time_minutes INTEGER NOT NULL DEFAULT 15,
    is_active BOOLEAN DEFAULT TRUE,
    priority_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_max_capacity CHECK (max_capacity > 0 AND max_capacity <= 200),
    CONSTRAINT chk_estimated_time CHECK (estimated_time_minutes > 0 AND estimated_time_minutes <= 120),
    CONSTRAINT chk_priority_order CHECK (priority_order >= 1)
);

-- Índices para performance
CREATE INDEX idx_customers_national_id ON customers(national_id);
CREATE INDEX idx_customers_telegram_chat_id ON customers(telegram_chat_id) WHERE telegram_chat_id IS NOT NULL;
CREATE INDEX idx_customers_created_at ON customers(created_at DESC);
CREATE INDEX idx_queue_config_type ON queue_config(queue_type);
CREATE INDEX idx_queue_config_active ON queue_config(is_active) WHERE is_active = TRUE;

-- Triggers para updated_at
CREATE TRIGGER customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER queue_config_updated_at
    BEFORE UPDATE ON queue_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Datos iniciales de configuración
INSERT INTO queue_config (queue_type, max_capacity, estimated_time_minutes, priority_order) VALUES
('VIP', 10, 5, 1),
('BUSINESS', 20, 10, 2),
('PRIORITY', 30, 15, 3),
('GENERAL', 50, 20, 4);

-- Comentarios
COMMENT ON TABLE customers IS 'Información de clientes del banco';
COMMENT ON TABLE queue_config IS 'Configuración de tipos de cola';
COMMENT ON COLUMN customers.telegram_chat_id IS 'ID del chat de Telegram para notificaciones';
COMMENT ON COLUMN customers.is_vip IS 'Cliente VIP con prioridad especial';
```

#### V3__ticket_tables.sql
```sql
-- =====================================================
-- TICKETS Y NOTIFICACIONES
-- Versión: 3.0
-- Descripción: Core del sistema de tickets
-- =====================================================

SET search_path TO ticketero;

-- Tabla principal de tickets
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_code VARCHAR(10) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    queue_type queue_type NOT NULL,
    status ticket_status NOT NULL DEFAULT 'PENDING',
    position_in_queue INTEGER,
    estimated_wait_time INTEGER, -- minutos
    called_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_position_positive CHECK (position_in_queue > 0),
    CONSTRAINT chk_estimated_wait_positive CHECK (estimated_wait_time >= 0),
    CONSTRAINT chk_status_timestamps CHECK (
        (status = 'CALLED' AND called_at IS NOT NULL) OR
        (status = 'COMPLETED' AND completed_at IS NOT NULL) OR
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL) OR
        (status IN ('PENDING', 'IN_PROGRESS', 'NO_SHOW'))
    )
);

-- Tabla de notificaciones
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    status notification_status NOT NULL DEFAULT 'PENDING',
    message TEXT NOT NULL,
    telegram_message_id BIGINT,
    scheduled_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_retry_count CHECK (retry_count >= 0 AND retry_count <= 5),
    CONSTRAINT chk_status_timestamps_notif CHECK (
        (status = 'SENT' AND sent_at IS NOT NULL) OR
        (status = 'FAILED' AND failed_at IS NOT NULL) OR
        (status IN ('PENDING', 'CANCELLED'))
    )
);

-- Tabla de métricas diarias (para dashboard)
CREATE TABLE daily_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL,
    queue_type queue_type NOT NULL,
    total_tickets INTEGER DEFAULT 0,
    completed_tickets INTEGER DEFAULT 0,
    cancelled_tickets INTEGER DEFAULT 0,
    avg_wait_time_minutes DECIMAL(5,2),
    max_wait_time_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_daily_metrics_date_queue UNIQUE (date, queue_type),
    CONSTRAINT chk_tickets_positive CHECK (total_tickets >= 0 AND completed_tickets >= 0 AND cancelled_tickets >= 0),
    CONSTRAINT chk_wait_times CHECK (avg_wait_time_minutes >= 0 AND max_wait_time_minutes >= 0)
);

-- Índices críticos para performance
CREATE INDEX idx_tickets_customer_id ON tickets(customer_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_queue_type ON tickets(queue_type);
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_tickets_position_queue ON tickets(queue_type, position_in_queue) WHERE status = 'PENDING';
CREATE INDEX idx_tickets_code ON tickets(ticket_code);

CREATE INDEX idx_notifications_ticket_id ON notifications(ticket_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at) WHERE status = 'PENDING';
CREATE INDEX idx_notifications_type ON notifications(type);

CREATE INDEX idx_daily_metrics_date ON daily_metrics(date DESC);
CREATE INDEX idx_daily_metrics_queue_type ON daily_metrics(queue_type);

-- Triggers para updated_at
CREATE TRIGGER tickets_updated_at
    BEFORE UPDATE ON tickets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Función para generar código de ticket
CREATE OR REPLACE FUNCTION generate_ticket_code()
RETURNS TEXT AS $$
DECLARE
    code TEXT;
BEGIN
    SELECT LPAD(nextval('ticket_code_seq')::TEXT, 4, '0') INTO code;
    RETURN 'T' || code;
END;
$$ LANGUAGE plpgsql;

-- Trigger para auto-generar código de ticket
CREATE OR REPLACE FUNCTION set_ticket_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.ticket_code IS NULL OR NEW.ticket_code = '' THEN
        NEW.ticket_code := generate_ticket_code();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tickets_set_code
    BEFORE INSERT ON tickets
    FOR EACH ROW
    EXECUTE FUNCTION set_ticket_code();

-- Comentarios de documentación
COMMENT ON TABLE tickets IS 'Tickets de atención generados por clientes';
COMMENT ON TABLE notifications IS 'Notificaciones enviadas a clientes vía Telegram';
COMMENT ON TABLE daily_metrics IS 'Métricas agregadas por día para dashboard';
COMMENT ON COLUMN tickets.ticket_code IS 'Código único del ticket (ej: T1001)';
COMMENT ON COLUMN tickets.position_in_queue IS 'Posición actual en la cola';
COMMENT ON COLUMN notifications.retry_count IS 'Número de reintentos de envío';
```

### 2.3 ORDEN DE EJECUCIÓN

1. **V1__baseline.sql** - Configuración base, tipos, funciones
2. **V2__user_tables.sql** - Clientes y configuración de colas
3. **V3__ticket_tables.sql** - Tickets, notificaciones y métricas

### 2.4 VALIDACIONES POST-MIGRACIÓN

```sql
-- Verificar que todas las tablas existen
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'ticketero';

-- Verificar índices críticos
SELECT indexname FROM pg_indexes 
WHERE schemaname = 'ticketero';

-- Verificar datos iniciales
SELECT queue_type, max_capacity FROM queue_config ORDER BY priority_order;

-- Test de generación de código
SELECT generate_ticket_code();
```

---

**🛑 PUNTO DE REVISIÓN 2**

He completado el **Plan de Migraciones SQL** con:

✅ **3 archivos Flyway** estructurados incrementalmente  
✅ **Baseline completo** con tipos, funciones y configuraciones  
✅ **Tablas de dominio** con constraints y validaciones  
✅ **Índices optimizados** para queries frecuentes  
✅ **Triggers automáticos** para timestamps y códigos  
✅ **Datos iniciales** para configuración de colas  
✅ **Validaciones** post-migración incluidas  

**¿Procedo con la siguiente sección (Configuración Inicial del Proyecto) o necesitas revisar/ajustar algo en las migraciones?**

---

## 3. CONFIGURACIÓN INICIAL DEL PROYECTO

### 3.1 CHECKLIST pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.banco</groupId>
    <artifactId>ticketero-system</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <name>Sistema Ticketero</name>
    <description>Sistema de gestión de tickets bancarios</description>
    
    <properties>
        <java.version>21</java.version>
        <lombok.version>1.18.30</lombok.version>
        <testcontainers.version>1.19.3</testcontainers.version>
        <telegram.version>6.8.0</telegram.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- Telegram Bot -->
        <dependency>
            <groupId>org.telegram</groupId>
            <artifactId>telegrambots</artifactId>
            <version>${telegram.version}</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <configuration>
                    <url>jdbc:postgresql://localhost:5432/ticketero_db</url>
                    <user>${DB_USERNAME}</user>
                    <password>${DB_PASSWORD}</password>
                    <schemas>
                        <schema>ticketero</schema>
                    </schemas>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 CONFIGURACIÓN application.yml

```yaml
# =====================================================
# CONFIGURACIÓN PRINCIPAL - application.yml
# =====================================================

spring:
  application:
    name: ticketero-system
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:ticketero_db}
    username: ${DB_USERNAME:ticketero_user}
    password: ${DB_PASSWORD:ticketero_pass}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: ticketero
    open-in-view: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: ticketero
    baseline-on-migrate: true
    validate-on-migrate: true

# Telegram Bot Configuration
telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:TicketeroBot}
  
# Queue Configuration
queue:
  config:
    max-capacity: 50
    cleanup-interval: 3600000  # 1 hora en ms
    notification-delay: 300000  # 5 minutos en ms

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# Logging
logging:
  level:
    com.banco.ticketero: INFO
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/ticketero.log

# Server
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api
```

### 3.3 ARCHIVO .env (Template)

```bash
# =====================================================
# VARIABLES DE ENTORNO - .env
# =====================================================

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ticketero_db
DB_USERNAME=ticketero_user
DB_PASSWORD=ticketero_pass

# Telegram Bot
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_BOT_USERNAME=YourBotUsername

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

### 3.4 DOCKER COMPOSE (docker-compose.yml)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: ticketero-postgres
    environment:
      POSTGRES_DB: ticketero_db
      POSTGRES_USER: ticketero_user
      POSTGRES_PASSWORD: ticketero_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ticketero-network

volumes:
  postgres_data:

networks:
  ticketero-network:
    driver: bridge
```

### 3.5 CHECKLIST DE CONFIGURACIÓN INICIAL

#### ✅ Fase 0: Setup Básico

**Archivos de Configuración:**
- [ ] `pom.xml` con dependencias correctas
- [ ] `application.yml` configuración base
- [ ] `.env` template creado
- [ ] `docker-compose.yml` para infraestructura
- [ ] `.gitignore` configurado

**Estructura de Directorios:**
- [ ] `src/main/java/com/banco/ticketero/` creado
- [ ] `src/main/resources/db/migration/` creado
- [ ] `src/test/java/com/banco/ticketero/` creado
- [ ] `logs/` directorio creado

**Validaciones:**
- [ ] `mvn clean compile` ejecuta sin errores
- [ ] `docker-compose up postgres` funciona
- [ ] Conexión a BD exitosa
- [ ] Migraciones Flyway ejecutan correctamente
- [ ] Actuator endpoints responden

**Comandos de Verificación:**
```bash
# Compilar proyecto
mvn clean compile

# Levantar BD
docker-compose up -d postgres

# Ejecutar migraciones
mvn flyway:migrate

# Verificar health
curl http://localhost:8080/api/actuator/health
```

---

**🛑 PUNTO DE REVISIÓN 3**

He completado la **Configuración Inicial del Proyecto** con:

✅ **pom.xml completo** con todas las dependencias necesarias  
✅ **application.yml** con configuración por perfiles  
✅ **Variables de entorno** template y configuración Docker  
✅ **Checklist de setup** con validaciones paso a paso  
✅ **Comandos de verificación** para cada componente  

**¿Procedo con la siguiente sección (Checklist de Implementación por Fases) o necesitas revisar/ajustar algo en la configuración?**

---

## 4. CHECKLIST DE IMPLEMENTACIÓN POR FASES

### FASE 0: SETUP INICIAL (4-6 horas)

#### 4.1 Configuración Base
- [ ] **Tarea 0.1**: Crear repositorio Git y estructura inicial
  - Crear repo en GitHub/GitLab
  - Configurar .gitignore para Java/Maven
  - Crear README.md básico
  - **Estimación**: 30 min

- [ ] **Tarea 0.2**: Configurar proyecto Maven
  - Crear pom.xml con dependencias
  - Configurar plugins (Spring Boot, Flyway)
  - Validar compilación: `mvn clean compile`
  - **Estimación**: 45 min

- [ ] **Tarea 0.3**: Configurar base de datos
  - Crear docker-compose.yml
  - Levantar PostgreSQL: `docker-compose up -d postgres`
  - Crear archivos de migración V1, V2, V3
  - Ejecutar migraciones: `mvn flyway:migrate`
  - **Estimación**: 90 min

- [ ] **Tarea 0.4**: Configurar aplicación Spring Boot
  - Crear application.yml (dev/prod)
  - Configurar .env template
  - Crear clase principal TicketeroApplication
  - Verificar startup: `mvn spring-boot:run`
  - **Estimación**: 60 min

#### Criterios de Aceptación Fase 0:
✅ Aplicación compila sin errores  
✅ Base de datos conecta correctamente  
✅ Migraciones ejecutan exitosamente  
✅ Actuator health endpoint responde  
✅ Logs se generan correctamente  

---

### FASE 1: DOMINIO (8-10 horas)

#### 4.2 Entidades de Dominio
- [ ] **Tarea 1.1**: Crear Value Objects
  - TicketId, CustomerId, NotificationId
  - TicketCode, NationalId
  - **Estimación**: 90 min

- [ ] **Tarea 1.2**: Crear Enums de dominio
  - TicketStatus, QueueType, NotificationType
  - **Estimación**: 45 min

- [ ] **Tarea 1.3**: Crear entidades principales
  - Ticket (Aggregate Root)
  - Customer (Aggregate Root)
  - Queue, Notification
  - **Estimación**: 180 min

- [ ] **Tarea 1.4**: Crear interfaces de repositorio
  - TicketRepository, CustomerRepository
  - QueueRepository, NotificationRepository
  - **Estimación**: 60 min

- [ ] **Tarea 1.5**: Crear servicios de dominio
  - TicketDomainService (lógica de negocio)
  - QueueDomainService (gestión de colas)
  - **Estimación**: 120 min

- [ ] **Tarea 1.6**: Crear excepciones de dominio
  - TicketNotFoundException
  - InvalidTicketStatusException, QueueFullException
  - **Estimación**: 45 min

#### Criterios de Aceptación Fase 1:
✅ Todas las entidades compilan sin errores  
✅ Value Objects inmutables y validados  
✅ Lógica de dominio sin dependencias externas  
✅ Tests unitarios de dominio pasan (>80% cobertura)  

---

### FASE 2: APLICACIÓN (6-8 horas)

#### 4.3 DTOs y Casos de Uso
- [ ] **Tarea 2.1**: Crear DTOs Request
  - CreateTicketRequest, UpdateTicketStatusRequest
  - SendNotificationRequest
  - **Estimación**: 90 min

- [ ] **Tarea 2.2**: Crear DTOs Response
  - TicketResponse, QueueStatusResponse
  - NotificationResponse
  - **Estimación**: 90 min

- [ ] **Tarea 2.3**: Crear puertos (interfaces)
  - NotificationPort, TelegramPort
  - TicketUseCasePort, QueueUseCasePort
  - **Estimación**: 60 min

- [ ] **Tarea 2.4**: Implementar casos de uso principales
  - CreateTicketUseCase, GetTicketUseCase
  - UpdateTicketStatusUseCase
  - **Estimación**: 150 min

- [ ] **Tarea 2.5**: Implementar casos de uso de cola
  - GetQueueStatusUseCase, CallNextTicketUseCase
  - **Estimación**: 120 min

- [ ] **Tarea 2.6**: Servicios de aplicación
  - TicketApplicationService
  - QueueApplicationService
  - **Estimación**: 90 min

#### Criterios de Aceptación Fase 2:
✅ DTOs con validaciones Jakarta  
✅ Casos de uso orquestan correctamente  
✅ Puertos definidos sin implementación  
✅ Tests unitarios de aplicación pasan  

---

### FASE 3: PERSISTENCIA (8-10 horas)

#### 4.4 JPA Entities y Repositories
- [ ] **Tarea 3.1**: Crear JPA Entities
  - TicketEntity, CustomerEntity
  - QueueEntity, NotificationEntity
  - **Estimación**: 180 min

- [ ] **Tarea 3.2**: Crear JPA Repositories
  - TicketJpaRepository, CustomerJpaRepository
  - Queries derivadas y custom
  - **Estimación**: 120 min

- [ ] **Tarea 3.3**: Crear mappers Entity ↔ Domain
  - TicketEntityMapper, CustomerEntityMapper
  - **Estimación**: 90 min

- [ ] **Tarea 3.4**: Implementar adaptadores de persistencia
  - TicketRepositoryAdapter
  - CustomerRepositoryAdapter
  - **Estimación**: 120 min

- [ ] **Tarea 3.5**: Configuración de base de datos
  - DatabaseConfig, JPA properties
  - Connection pooling
  - **Estimación**: 60 min

#### Criterios de Aceptación Fase 3:
✅ Entities mapean correctamente a tablas  
✅ Repositories ejecutan queries sin errores  
✅ Mappers convierten Domain ↔ Entity  
✅ Tests de integración con BD pasan  

---

### FASE 4: API REST (6-8 horas)

#### 4.5 Controllers y Exception Handling
- [ ] **Tarea 4.1**: Crear controllers principales
  - TicketController (CRUD completo)
  - QueueController (status, métricas)
  - **Estimación**: 150 min

- [ ] **Tarea 4.2**: Crear controller de dashboard
  - DashboardController (métricas en tiempo real)
  - **Estimación**: 90 min

- [ ] **Tarea 4.3**: Exception handling global
  - GlobalExceptionHandler
  - ErrorResponse DTO
  - **Estimación**: 90 min

- [ ] **Tarea 4.4**: Validación de requests
  - @Valid en controllers
  - Validaciones custom
  - **Estimación**: 60 min

- [ ] **Tarea 4.5**: Documentación API
  - Swagger/OpenAPI configuration
  - **Estimación**: 45 min

#### Criterios de Aceptación Fase 4:
✅ Endpoints REST responden correctamente  
✅ Validaciones funcionan (400 para datos inválidos)  
✅ Exception handling retorna errores consistentes  
✅ Swagger UI accesible y funcional  

---

### FASE 5: INTEGRACIÓN TELEGRAM (4-6 horas)

#### 4.6 Adaptador Telegram
- [ ] **Tarea 5.1**: Configurar Telegram Bot
  - TelegramConfig, bot token
  - **Estimación**: 45 min

- [ ] **Tarea 5.2**: Implementar TelegramAdapter
  - Envío de mensajes
  - Manejo de errores
  - **Estimación**: 120 min

- [ ] **Tarea 5.3**: Casos de uso de notificación
  - SendNotificationUseCase
  - ScheduleNotificationUseCase
  - **Estimación**: 90 min

- [ ] **Tarea 5.4**: Scheduler para notificaciones
  - NotificationScheduler
  - Procesamiento asíncrono
  - **Estimación**: 90 min

#### Criterios de Aceptación Fase 5:
✅ Bot Telegram responde a mensajes  
✅ Notificaciones se envían correctamente  
✅ Scheduler procesa notificaciones pendientes  
✅ Manejo de errores de Telegram API  

---

### FASE 6: TESTING (8-10 horas)

#### 4.7 Tests Completos
- [ ] **Tarea 6.1**: Tests unitarios de dominio
  - Entidades, Value Objects, Servicios
  - **Estimación**: 120 min

- [ ] **Tarea 6.2**: Tests unitarios de aplicación
  - Casos de uso, DTOs
  - **Estimación**: 120 min

- [ ] **Tarea 6.3**: Tests de integración
  - Repositories con Testcontainers
  - **Estimación**: 150 min

- [ ] **Tarea 6.4**: Tests end-to-end
  - Controllers con MockMvc
  - **Estimación**: 120 min

- [ ] **Tarea 6.5**: Tests de Telegram
  - Mock de Telegram API
  - **Estimación**: 90 min

#### Criterios de Aceptación Fase 6:
✅ Cobertura de tests >80%  
✅ Tests unitarios ejecutan <5 segundos  
✅ Tests de integración con BD pasan  
✅ Tests E2E cubren happy paths  

---

### FASE 7: DEPLOYMENT Y MONITOREO (4-6 horas)

#### 4.8 Producción
- [ ] **Tarea 7.1**: Configuración de producción
  - application-prod.yml
  - Variables de entorno
  - **Estimación**: 60 min

- [ ] **Tarea 7.2**: Docker para aplicación
  - Dockerfile optimizado
  - Docker-compose completo
  - **Estimación**: 90 min

- [ ] **Tarea 7.3**: Monitoreo y métricas
  - Actuator endpoints
  - Logging configuration
  - **Estimación**: 90 min

- [ ] **Tarea 7.4**: Health checks
  - Database health
  - Telegram API health
  - **Estimación**: 60 min

#### Criterios de Aceptación Fase 7:
✅ Aplicación despliega en Docker  
✅ Health checks funcionan  
✅ Métricas se exponen correctamente  
✅ Logs estructurados y útiles  

---

### RESUMEN DE FASES

| Fase | Duración | Componentes Clave | Dependencias |
|------|----------|-------------------|-------------|
| **0 - Setup** | 4-6h | Proyecto, BD, Config | Ninguna |
| **1 - Dominio** | 8-10h | Entities, Services | Fase 0 |
| **2 - Aplicación** | 6-8h | Use Cases, DTOs | Fase 1 |
| **3 - Persistencia** | 8-10h | JPA, Repositories | Fase 2 |
| **4 - API REST** | 6-8h | Controllers, Validation | Fase 3 |
| **5 - Telegram** | 4-6h | Bot Integration | Fase 4 |
| **6 - Testing** | 8-10h | Unit, Integration, E2E | Todas |
| **7 - Deploy** | 4-6h | Docker, Monitoring | Fase 6 |

**TOTAL ESTIMADO: 48-66 horas (6-8 días de desarrollo)**

---

**🛑 PUNTO DE REVISIÓN 4**

He completado el **Checklist de Implementación por Fases** con:

✅ **8 fases estructuradas** desde setup hasta deployment  
✅ **Tareas granulares** con estimaciones realistas  
✅ **Criterios de aceptación** claros por fase  
✅ **Dependencias explícitas** entre fases  
✅ **Estimación total** de 48-66 horas de desarrollo  

**¿Procedo con la siguiente sección (Orden de Implementación de Código) o necesitas revisar/ajustar algo en las fases?**

---

## 5. ORDEN DE IMPLEMENTACIÓN DE CÓDIGO (ESTRICTO)

### 5.1 REGLA DE ORO: DEPENDENCIAS HACIA ADENTRO

```
Domain (Core) ← Application ← Infrastructure
```

**Principio**: Nunca implementar una capa que dependa de otra no implementada.

### 5.2 SECUENCIA OBLIGATORIA

#### PASO 1: DOMAIN LAYER (Núcleo del Sistema)

**1.1 Value Objects** (Sin dependencias)
```java
// Orden de implementación:
1. TicketId.java
2. CustomerId.java  
3. NotificationId.java
4. TicketCode.java
5. NationalId.java
```

**1.2 Enums** (Sin dependencias)
```java
// Orden de implementación:
1. TicketStatus.java
2. QueueType.java
3. NotificationType.java
```

**1.3 Domain Entities** (Dependen de Value Objects y Enums)
```java
// Orden de implementación:
1. Customer.java        // Sin dependencias de otras entities
2. Queue.java          // Sin dependencias de otras entities  
3. Ticket.java         // Depende de Customer
4. Notification.java   // Depende de Ticket y Customer
```

**1.4 Repository Interfaces** (Puertos del dominio)
```java
// Orden de implementación:
1. CustomerRepository.java
2. QueueRepository.java
3. TicketRepository.java
4. NotificationRepository.java
```

**1.5 Domain Services** (Lógica de negocio)
```java
// Orden de implementación:
1. QueueDomainService.java     // Lógica de colas
2. TicketDomainService.java    // Depende de Queue
3. NotificationDomainService.java // Depende de Ticket
```

**1.6 Domain Exceptions**
```java
// Orden de implementación:
1. TicketNotFoundException.java
2. InvalidTicketStatusException.java
3. QueueFullException.java
```

#### PASO 2: APPLICATION LAYER (Casos de Uso)

**2.1 DTOs Request** (Sin dependencias del dominio)
```java
// Orden de implementación:
1. CreateTicketRequest.java
2. UpdateTicketStatusRequest.java
3. SendNotificationRequest.java
```

**2.2 DTOs Response** (Sin dependencias del dominio)
```java
// Orden de implementación:
1. TicketResponse.java
2. QueueStatusResponse.java
3. NotificationResponse.java
```

**2.3 Output Ports** (Interfaces hacia infraestructura)
```java
// Orden de implementación:
1. TelegramPort.java
2. NotificationPort.java
3. MessageQueuePort.java
```

**2.4 Use Cases** (Dependen de Domain y Ports)
```java
// Orden de implementación:
1. GetTicketUseCase.java           // Solo lectura
2. CreateTicketUseCase.java        // Depende de repositories
3. UpdateTicketStatusUseCase.java  // Depende de domain services
4. CancelTicketUseCase.java        // Depende de casos anteriores
5. GetQueueStatusUseCase.java      // Solo lectura
6. CallNextTicketUseCase.java      // Depende de queue logic
7. SendNotificationUseCase.java    // Depende de notification port
8. ScheduleNotificationUseCase.java // Depende de casos anteriores
```

**2.5 Application Services** (Orquestadores)
```java
// Orden de implementación:
1. TicketApplicationService.java   // Orquesta use cases de ticket
2. QueueApplicationService.java    // Orquesta use cases de queue
3. NotificationApplicationService.java // Orquesta notificaciones
```

#### PASO 3: INFRASTRUCTURE LAYER (Adaptadores)

**3.1 JPA Entities** (Mapean a BD)
```java
// Orden de implementación:
1. CustomerEntity.java     // Sin FK a otras entities
2. QueueEntity.java       // Sin FK a otras entities
3. TicketEntity.java      // FK a Customer
4. NotificationEntity.java // FK a Ticket y Customer
```

**3.2 Entity Mappers** (Domain ↔ JPA)
```java
// Orden de implementación:
1. CustomerEntityMapper.java
2. QueueEntityMapper.java
3. TicketEntityMapper.java
4. NotificationEntityMapper.java
```

**3.3 JPA Repositories** (Spring Data)
```java
// Orden de implementación:
1. CustomerJpaRepository.java
2. QueueJpaRepository.java
3. TicketJpaRepository.java
4. NotificationJpaRepository.java
```

**3.4 Repository Adapters** (Implementan puertos del dominio)
```java
// Orden de implementación:
1. CustomerRepositoryAdapter.java  // Implementa CustomerRepository
2. QueueRepositoryAdapter.java     // Implementa QueueRepository
3. TicketRepositoryAdapter.java    // Implementa TicketRepository
4. NotificationRepositoryAdapter.java // Implementa NotificationRepository
```

**3.5 External Adapters** (APIs externas)
```java
// Orden de implementación:
1. TelegramAdapter.java            // Implementa TelegramPort
2. EmailAdapter.java               // Backup notifications
3. SmsAdapter.java                 // Backup notifications
```

**3.6 Configuration Classes**
```java
// Orden de implementación:
1. DatabaseConfig.java             // Configuración JPA
2. TelegramConfig.java             // Configuración bot
3. SchedulerConfig.java            // Tareas programadas
4. SwaggerConfig.java              // Documentación API
```

**3.7 Exception Handling**
```java
// Orden de implementación:
1. ErrorResponse.java              // DTO de error
2. GlobalExceptionHandler.java     // Manejo global
3. ValidationExceptionHandler.java // Validaciones específicas
```

**3.8 REST Controllers** (Última capa)
```java
// Orden de implementación:
1. HealthController.java           // Sin dependencias de negocio
2. TicketController.java           // Depende de TicketApplicationService
3. QueueController.java            // Depende de QueueApplicationService
4. DashboardController.java        // Depende de múltiples services
```

**3.9 Schedulers** (Tareas programadas)
```java
// Orden de implementación:
1. QueueCleanupScheduler.java      // Limpieza de colas
2. NotificationScheduler.java      // Procesamiento de notificaciones
```

### 5.3 VALIDACIÓN POR PASO

#### Después de cada PASO, validar:

**Post-PASO 1 (Domain):**
```bash
# Compilación sin errores
mvn clean compile

# Tests unitarios de dominio
mvn test -Dtest="**/domain/**/*Test"

# Cobertura >80% en domain
mvn jacoco:report
```

**Post-PASO 2 (Application):**
```bash
# Compilación sin errores
mvn clean compile

# Tests unitarios de application
mvn test -Dtest="**/application/**/*Test"

# Validar que use cases orquestan correctamente
```

**Post-PASO 3 (Infrastructure):**
```bash
# Compilación completa
mvn clean compile

# Tests de integración
mvn test -Dtest="**/infrastructure/**/*Test"

# Levantar aplicación
mvn spring-boot:run

# Verificar endpoints
curl http://localhost:8080/api/actuator/health
```

### 5.4 ANTI-PATTERNS A EVITAR

❌ **NO implementar Controller antes que Use Cases**
❌ **NO implementar JPA Entity antes que Domain Entity**  
❌ **NO implementar Adapter antes que Port**
❌ **NO saltarse tests unitarios por paso**
❌ **NO implementar múltiples capas simultáneamente**

### 5.5 CHECKLIST DE ORDEN CORRECTO

**Antes de implementar cualquier clase:**
- [ ] ¿Todas sus dependencias están implementadas?
- [ ] ¿Pertenece a la capa correcta según el orden?
- [ ] ¿Tiene test unitario correspondiente?
- [ ] ¿Compila sin errores la capa actual?

**Ejemplo de validación:**
```java
// ❌ INCORRECTO: Implementar TicketController antes que TicketApplicationService
public class TicketController {
    private final TicketApplicationService service; // ¡No existe aún!
}

// ✅ CORRECTO: Implementar TicketApplicationService primero
public class TicketApplicationService {
    private final CreateTicketUseCase createUseCase; // Ya implementado
}
```

### 5.6 TIEMPO ESTIMADO POR PASO

| Paso | Componentes | Tiempo Estimado | Acumulado |
|------|-------------|-----------------|----------|
| **1 - Domain** | 20 clases | 8-10 horas | 10h |
| **2 - Application** | 15 clases | 6-8 horas | 18h |
| **3 - Infrastructure** | 25 clases | 12-15 horas | 33h |
| **Testing** | Todos los componentes | 8-10 horas | 43h |
| **Integration** | E2E, deployment | 4-6 horas | 49h |

**TOTAL: ~49 horas de desarrollo puro**

---

**🛑 PUNTO DE REVISIÓN 5**

He completado el **Orden de Implementación de Código** con:

✅ **Secuencia estricta** Domain → Application → Infrastructure  
✅ **Orden granular** clase por clase con dependencias  
✅ **Validaciones por paso** con comandos específicos  
✅ **Anti-patterns** claramente identificados  
✅ **Checklist de verificación** antes de cada implementación  
✅ **Estimaciones realistas** por paso y acumuladas  

**¿Procedo con la sección final (Criterios de Aceptación) y generar el archivo .md completo?**

---

## 6. CRITERIOS DE ACEPTACIÓN POR FASE

### 6.1 DEFINICIÓN DE "TERMINADO" (Definition of Done)

**Una fase está TERMINADA cuando:**
✅ Código compila sin errores ni warnings  
✅ Tests unitarios pasan (cobertura >80%)  
✅ Tests de integración pasan (si aplica)  
✅ Documentación actualizada  
✅ Code review aprobado  
✅ Funcionalidad validada manualmente  

### 6.2 CRITERIOS ESPECÍFICOS POR FASE

#### FASE 0: SETUP INICIAL ✅

**Criterios Técnicos:**
- [ ] `mvn clean compile` ejecuta sin errores
- [ ] `docker-compose up postgres` levanta BD correctamente
- [ ] `mvn flyway:migrate` ejecuta las 3 migraciones
- [ ] `mvn spring-boot:run` inicia aplicación
- [ ] `curl http://localhost:8080/api/actuator/health` retorna 200

**Criterios Funcionales:**
- [ ] Estructura de directorios creada según arquitectura
- [ ] Variables de entorno configuradas
- [ ] Logs se generan en `logs/ticketero.log`

**Validación:**
```bash
# Comando de validación completa Fase 0
mvn clean compile && \
docker-compose up -d postgres && \
mvn flyway:migrate && \
mvn spring-boot:run &
sleep 10 && curl http://localhost:8080/api/actuator/health
```

#### FASE 1: DOMINIO ✅

**Criterios Técnicos:**
- [ ] Todas las entidades de dominio compilan
- [ ] Value Objects son inmutables (final fields)
- [ ] Enums tienen todos los valores requeridos
- [ ] Repository interfaces definidas (sin implementación)
- [ ] Domain services contienen lógica de negocio pura
- [ ] Excepciones de dominio heredan de RuntimeException

**Criterios Funcionales:**
- [ ] `Ticket` puede cambiar de estado según reglas de negocio
- [ ] `Customer` valida formato de national_id
- [ ] `Queue` maneja capacidad máxima
- [ ] `Notification` tiene tipos correctos

**Tests Requeridos:**
- [ ] Tests unitarios para cada entidad (>80% cobertura)
- [ ] Tests de Value Objects (inmutabilidad, validaciones)
- [ ] Tests de Domain Services (lógica de negocio)

**Validación:**
```bash
mvn test -Dtest="**/domain/**/*Test" && \
mvn jacoco:report && \
echo "Verificar cobertura >80% en target/site/jacoco/index.html"
```

#### FASE 2: APLICACIÓN ✅

**Criterios Técnicos:**
- [ ] DTOs con validaciones Jakarta (@NotNull, @Valid)
- [ ] Use Cases orquestan correctamente (sin lógica de negocio)
- [ ] Puertos (interfaces) definidos sin implementación
- [ ] Application Services delegan a Use Cases

**Criterios Funcionales:**
- [ ] `CreateTicketUseCase` valida datos y crea ticket
- [ ] `GetQueueStatusUseCase` retorna estado actual
- [ ] `SendNotificationUseCase` programa envío
- [ ] DTOs mapean correctamente desde/hacia dominio

**Tests Requeridos:**
- [ ] Tests unitarios de Use Cases con mocks
- [ ] Tests de validación de DTOs
- [ ] Tests de Application Services

**Validación:**
```bash
mvn test -Dtest="**/application/**/*Test" && \
echo "Verificar que Use Cases no tienen lógica de negocio"
```

#### FASE 3: PERSISTENCIA ✅

**Criterios Técnicos:**
- [ ] JPA Entities mapean correctamente a tablas
- [ ] Repositories ejecutan queries sin errores
- [ ] Mappers convierten Domain ↔ Entity correctamente
- [ ] Transacciones configuradas (@Transactional)

**Criterios Funcionales:**
- [ ] CRUD completo de tickets funciona
- [ ] Queries custom retornan datos correctos
- [ ] Relaciones FK se mantienen consistentes
- [ ] Índices mejoran performance de queries

**Tests Requeridos:**
- [ ] Tests de integración con Testcontainers
- [ ] Tests de JPA Repositories
- [ ] Tests de mappers (bidireccionales)

**Validación:**
```bash
mvn test -Dtest="**/infrastructure/persistence/**/*Test" && \
echo "Verificar conexión a BD y queries"
```

#### FASE 4: API REST ✅

**Criterios Técnicos:**
- [ ] Controllers retornan ResponseEntity con status HTTP correctos
- [ ] Validaciones @Valid funcionan (400 para datos inválidos)
- [ ] Exception handling retorna errores consistentes
- [ ] Swagger UI accesible en `/swagger-ui.html`

**Criterios Funcionales:**
- [ ] `POST /api/tickets` crea ticket y retorna 201
- [ ] `GET /api/tickets/{id}` retorna ticket o 404
- [ ] `PUT /api/tickets/{id}/status` actualiza estado
- [ ] `GET /api/queues/status` retorna estado de colas
- [ ] `GET /api/dashboard` retorna métricas

**Tests Requeridos:**
- [ ] Tests de controllers con MockMvc
- [ ] Tests de validación (requests inválidos)
- [ ] Tests de exception handling

**Validación:**
```bash
# Levantar app y probar endpoints
mvn spring-boot:run &
sleep 15
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"nationalId":"12345678","queueType":"GENERAL"}'
```

#### FASE 5: INTEGRACIÓN TELEGRAM ✅

**Criterios Técnicos:**
- [ ] TelegramAdapter implementa TelegramPort
- [ ] Bot responde a comandos básicos
- [ ] Scheduler procesa notificaciones pendientes
- [ ] Manejo de errores de Telegram API

**Criterios Funcionales:**
- [ ] Notificación de ticket creado se envía
- [ ] Notificación de llamada a cola se envía
- [ ] Bot responde con estado de ticket
- [ ] Reintentos automáticos en caso de fallo

**Tests Requeridos:**
- [ ] Tests con mock de Telegram API
- [ ] Tests de scheduler
- [ ] Tests de manejo de errores

**Validación:**
```bash
# Configurar bot token y probar
export TELEGRAM_BOT_TOKEN="your_test_token"
mvn spring-boot:run
# Enviar mensaje al bot y verificar respuesta
```

#### FASE 6: TESTING COMPLETO ✅

**Criterios Técnicos:**
- [ ] Cobertura total >80%
- [ ] Tests unitarios <5 segundos total
- [ ] Tests de integración <30 segundos
- [ ] Tests E2E cubren happy paths

**Criterios Funcionales:**
- [ ] Flujo completo: crear ticket → notificar → llamar → completar
- [ ] Manejo de errores en cada paso
- [ ] Performance aceptable (response time <500ms)

**Validación:**
```bash
mvn clean test && \
mvn jacoco:report && \
echo "Verificar cobertura total en jacoco report"
```

#### FASE 7: DEPLOYMENT Y MONITOREO ✅

**Criterios Técnicos:**
- [ ] Aplicación despliega en Docker sin errores
- [ ] Health checks funcionan correctamente
- [ ] Métricas se exponen en `/actuator/metrics`
- [ ] Logs estructurados y útiles

**Criterios Funcionales:**
- [ ] Sistema funciona en ambiente productivo
- [ ] Monitoreo detecta problemas
- [ ] Backup de BD configurado
- [ ] Rollback plan definido

**Validación:**
```bash
docker-compose up -d && \
sleep 30 && \
curl http://localhost:8080/api/actuator/health && \
curl http://localhost:8080/api/actuator/metrics
```

### 6.3 CHECKLIST FINAL DE ENTREGA

#### ✅ FUNCIONALIDADES CORE
- [ ] Cliente puede crear ticket con cédula
- [ ] Sistema asigna código único (T1001, T1002...)
- [ ] Cliente recibe notificación Telegram con código
- [ ] Ejecutivo puede ver cola de tickets pendientes
- [ ] Ejecutivo puede llamar siguiente ticket
- [ ] Cliente recibe notificación cuando es llamado
- [ ] Sistema actualiza estado de ticket
- [ ] Dashboard muestra métricas en tiempo real

#### ✅ CALIDAD DE CÓDIGO
- [ ] Arquitectura Clean/Hexagonal implementada
- [ ] Cobertura de tests >80%
- [ ] Sin code smells críticos
- [ ] Documentación API completa
- [ ] Logs informativos y estructurados

#### ✅ OPERACIONES
- [ ] Aplicación despliega con Docker
- [ ] Base de datos migra automáticamente
- [ ] Health checks configurados
- [ ] Métricas expuestas para monitoreo
- [ ] Variables de entorno documentadas

#### ✅ SEGURIDAD Y PERFORMANCE
- [ ] Validación de inputs en todos los endpoints
- [ ] Manejo seguro de errores (no exponer stack traces)
- [ ] Response time <500ms para operaciones normales
- [ ] Conexiones de BD con pooling
- [ ] Rate limiting en endpoints públicos

---

## 7. COMANDOS DE VALIDACIÓN FINAL

### 7.1 VALIDACIÓN COMPLETA DEL SISTEMA

```bash
#!/bin/bash
# validation-script.sh - Validación completa del sistema

echo "🚀 Iniciando validación completa del Sistema Ticketero..."

# 1. Compilación
echo "📦 Compilando proyecto..."
mvn clean compile || exit 1

# 2. Tests
echo "🧪 Ejecutando tests..."
mvn test || exit 1

# 3. Cobertura
echo "📊 Generando reporte de cobertura..."
mvn jacoco:report

# 4. Levantar infraestructura
echo "🐳 Levantando infraestructura..."
docker-compose up -d postgres
sleep 10

# 5. Migraciones
echo "🗄️ Ejecutando migraciones..."
mvn flyway:migrate || exit 1

# 6. Levantar aplicación
echo "🌟 Iniciando aplicación..."
mvn spring-boot:run &
APP_PID=$!
sleep 20

# 7. Validar endpoints
echo "🔍 Validando endpoints..."
curl -f http://localhost:8080/api/actuator/health || exit 1
curl -f http://localhost:8080/api/actuator/metrics || exit 1

# 8. Test funcional básico
echo "✅ Probando creación de ticket..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"nationalId":"12345678","queueType":"GENERAL"}')

echo "Response: $RESPONSE"

# 9. Cleanup
echo "🧹 Limpiando..."
kill $APP_PID
docker-compose down

echo "✅ ¡Validación completa exitosa!"
```

### 7.2 CHECKLIST DE ENTREGA FINAL

**Antes de marcar el proyecto como COMPLETADO:**

- [ ] Script de validación ejecuta sin errores
- [ ] Documentación README actualizada
- [ ] Variables de entorno documentadas en .env.example
- [ ] Docker Compose funciona en máquina limpia
- [ ] Swagger UI accesible y completo
- [ ] Logs no contienen información sensible
- [ ] Performance aceptable bajo carga normal
- [ ] Rollback plan documentado

---

## 📋 RESUMEN EJECUTIVO

### ENTREGABLES FINALES

1. **Código Fuente Completo**
   - 60+ clases Java siguiendo Clean Architecture
   - Cobertura de tests >80%
   - Documentación inline y README

2. **Base de Datos**
   - 3 migraciones Flyway
   - Esquema optimizado con índices
   - Datos de configuración inicial

3. **API REST**
   - 8+ endpoints documentados
   - Validación completa de inputs
   - Exception handling robusto

4. **Integración Telegram**
   - Bot funcional con comandos
   - Notificaciones automáticas
   - Manejo de errores y reintentos

5. **Infraestructura**
   - Docker Compose para desarrollo
   - Configuración por ambientes
   - Monitoreo y health checks

### MÉTRICAS DE ÉXITO

| Métrica | Objetivo | Validación |
|---------|----------|------------|
| **Cobertura Tests** | >80% | `mvn jacoco:report` |
| **Response Time** | <500ms | Load testing |
| **Uptime** | >99% | Health checks |
| **Error Rate** | <1% | Logs monitoring |
| **Code Quality** | A+ | SonarQube |

### TIEMPO TOTAL ESTIMADO

**48-66 horas de desarrollo** distribuidas en:
- Setup y configuración: 6 horas
- Desarrollo core: 35 horas  
- Testing e integración: 15 horas
- Deployment y documentación: 10 horas

---

**🎯 PLAN DE IMPLEMENTACIÓN COMPLETADO**

**Versión:** 1.0  
**Estado:** Listo para ejecución  
**Próximo paso:** Iniciar Fase 0 - Setup Inicial  

---