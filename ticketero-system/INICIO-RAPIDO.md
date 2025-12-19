# 🚀 Inicio Rápido - Sistema Ticketero

## Requisitos
- ☕ **Java 21+**
- 🐳 **Docker** (corriendo)

## Levantar el Proyecto (2 comandos)

### 1. Setup inicial (solo la primera vez)
```bash
./setup.sh
```

### 2. Iniciar aplicación
```bash
./start.sh
```

## ✅ Listo!

- **API:** http://localhost:8080/api
- **Health:** http://localhost:8080/actuator/health
- **RabbitMQ:** http://localhost:15672 (ticketero_user/ticketero_pass)

## 👥 Ejecutivos Disponibles

El sistema inicia con **8 ejecutivos DISPONIBLES** listos para tomar tickets:

| Nombre | Módulo | Colas |
|--------|--------|-------|
| María González | 1 | CAJA, PERSONAL_BANKER |
| Juan Pérez | 2 | CAJA |
| Carmen López | 3 | CAJA |
| Ana Silva | 4 | PERSONAL_BANKER, EMPRESAS |
| Roberto Díaz | 5 | PERSONAL_BANKER |
| Patricia Morales | 6 | EMPRESAS, GERENCIA |
| Luis Herrera | 7 | EMPRESAS |
| Sandra Vega | 8 | GERENCIA |

## 🎯 Crear Ticket de Prueba

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "queueType": "CAJA"
  }'
```

## 🛑 Detener

```bash
docker-compose down
```