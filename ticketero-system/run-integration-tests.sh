#!/bin/bash

# Script para ejecutar tests funcionales E2E con docker-compose
# Uso: ./run-integration-tests.sh [test-class]

set -e

echo "🚀 Iniciando Tests Funcionales E2E - Sistema Ticketero"
echo "=================================================="

# Función para limpiar al salir
cleanup() {
    echo "🧹 Limpiando recursos..."
    docker-compose -f docker-compose-test.yml down -v
    exit $1
}

# Capturar señales para limpiar
trap 'cleanup $?' EXIT INT TERM

# 1. Iniciar servicios de testing
echo "📦 Iniciando servicios de testing (PostgreSQL + RabbitMQ)..."
docker-compose -f docker-compose-test.yml up -d

# 2. Esperar que los servicios estén listos
echo "⏳ Esperando que los servicios estén listos..."
sleep 10

# Verificar PostgreSQL
echo "🔍 Verificando PostgreSQL..."
until docker exec postgres-test pg_isready -U test -d ticketero_test; do
    echo "Esperando PostgreSQL..."
    sleep 2
done

# Verificar RabbitMQ
echo "🔍 Verificando RabbitMQ..."
until docker exec rabbitmq-test rabbitmq-diagnostics ping; do
    echo "Esperando RabbitMQ..."
    sleep 2
done

echo "✅ Servicios listos!"

# 3. Ejecutar tests
if [ -z "$1" ]; then
    echo "🧪 Ejecutando todos los tests de integración..."
    mvn test -Dtest="*IT" -Dspring.profiles.active=test
else
    echo "🧪 Ejecutando test específico: $1"
    mvn test -Dtest="$1" -Dspring.profiles.active=test
fi

TEST_EXIT_CODE=$?

# 4. Mostrar resultados
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ Tests completados exitosamente!"
    echo "📊 Generando reporte..."
    mvn surefire-report:report
    echo "📄 Reporte disponible en: target/site/surefire-report.html"
else
    echo "❌ Tests fallaron con código: $TEST_EXIT_CODE"
    echo "📋 Ver detalles en: target/surefire-reports/"
fi

exit $TEST_EXIT_CODE