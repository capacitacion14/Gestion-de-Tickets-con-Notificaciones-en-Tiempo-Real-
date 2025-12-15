# 🚀 Guía de Ejecución - Sistema Ticketero

## 📋 Prerrequisitos

- ☑️ **Java 17+** instalado
- ☑️ **Maven 3.6+** instalado  
- ☑️ **Docker** instalado (para PostgreSQL)
- ☑️ **Bot de Telegram** (opcional)

## 🔧 Paso 1: Configurar Base de Datos

### Opción A: Docker (Recomendado)
```bash
docker run --name ticketero-db \
  -e POSTGRES_DB=ticketero \
  -e POSTGRES_USER=ticketero \
  -e POSTGRES_PASSWORD=ticketero123 \
  -p 5432:5432 \
  -d postgres:15
```

### Opción B: PostgreSQL Local
- Base de datos: `ticketero`
- Usuario: `ticketero`
- Password: `ticketero123`
- Puerto: `5432`

## 🤖 Paso 2: Configurar Telegram Bot (Opcional)

### 2.1 Crear Bot
1. Buscar `@BotFather` en Telegram
2. Enviar `/newbot`
3. Seguir instrucciones
4. Guardar el **token**

### 2.2 Obtener Chat ID
1. Enviar mensaje a tu bot
2. Visitar: `https://api.telegram.org/bot<TOKEN>/getUpdates`
3. Copiar el `chat.id`

## ⚙️ Paso 3: Configurar Variables

Editar el archivo `.env`:

```bash
# Base de Datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ticketero
DB_USERNAME=ticketero
DB_PASSWORD=ticketero123

# Telegram (opcional)
TELEGRAM_BOT_TOKEN=tu_token_aqui
TELEGRAM_BOT_USERNAME=tu_bot_username

# Aplicación
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

## 🏃 Paso 4: Ejecutar Aplicación

### Opción A: Script Automático
```bash
./run.sh
```

### Opción B: Maven Directo
```bash
mvn spring-boot:run
```

### Opción C: JAR Compilado
```bash
mvn clean package
java -jar target/ticketero-system-1.0.0.jar
```

## 🌐 Paso 5: Verificar Funcionamiento

### URLs Importantes:
- **API Base:** http://localhost:8080/api
- **Health Check:** http://localhost:8080/api/actuator/health
- **Métricas:** http://localhost:8080/api/actuator/metrics

### Endpoints Principales:
- `POST /api/tickets` - Crear ticket
- `GET /api/tickets/{id}` - Consultar ticket
- `GET /api/queues` - Ver colas
- `GET /api/queues/{type}/tickets` - Tickets por cola

## 🧪 Paso 6: Probar API

### Crear Ticket:
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678",
    "queueType": "GENERAL"
  }'
```

### Consultar Ticket:
```bash
curl http://localhost:8080/api/tickets/1
```

### Ver Colas:
```bash
curl http://localhost:8080/api/queues
```

## 🔧 Solución de Problemas

### Error de Base de Datos:
```bash
# Verificar PostgreSQL
docker ps | grep ticketero-db

# Ver logs
docker logs ticketero-db

# Reiniciar contenedor
docker restart ticketero-db
```

### Error de Puerto:
```bash
# Cambiar puerto en .env
SERVER_PORT=8081

# O usar variable directa
SERVER_PORT=8081 mvn spring-boot:run
```

### Error de Telegram:
- Dejar `TELEGRAM_BOT_TOKEN` vacío si no tienes bot
- El sistema funciona sin Telegram
- Revisar logs para errores de configuración

## 📱 Configuración de Telegram

### Variables Necesarias:
```bash
TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_BOT_USERNAME=MiTicketeroBot
```

### Probar Bot:
1. Enviar mensaje a tu bot
2. Revisar logs de la aplicación
3. Verificar que aparezca el mensaje en consola

## 🛑 Detener Aplicación

- **Ctrl+C** en terminal
- **Docker:** `docker stop ticketero-db`

## 📊 Monitoreo

### Health Check:
```bash
curl http://localhost:8080/api/actuator/health
```

### Métricas:
```bash
curl http://localhost:8080/api/actuator/metrics
```

### Logs:
- Revisar consola para logs en tiempo real
- Nivel INFO por defecto
- DEBUG para desarrollo

---

## 🎯 Resumen Rápido

1. **Iniciar PostgreSQL:** `docker run --name ticketero-db -e POSTGRES_DB=ticketero -e POSTGRES_USER=ticketero -e POSTGRES_PASSWORD=ticketero123 -p 5432:5432 -d postgres:15`

2. **Configurar .env** (opcional para Telegram)

3. **Ejecutar:** `./run.sh`

4. **Probar:** http://localhost:8080/api/actuator/health

¡Listo! 🎉