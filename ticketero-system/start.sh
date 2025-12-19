#!/bin/bash

# 🚀 Inicio Rápido - Sistema Ticketero

echo "🎯 Iniciando Sistema Ticketero..."

# Verificar que servicios estén corriendo
if ! docker-compose ps | grep -q "postgres.*Up"; then
    echo "⚠️  PostgreSQL no está corriendo. Ejecuta: ./setup.sh"
    exit 1
fi

# Cargar .env
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🏃 Ejecutando aplicación..."
echo ""
echo "📱 API: http://localhost:8080/api"
echo "❤️  Health: http://localhost:8080/actuator/health"
echo "🐰 RabbitMQ: http://localhost:15672"
echo ""
echo "🛑 Para detener: Ctrl+C"
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests