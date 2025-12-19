#!/bin/bash

# 🚀 Setup Automático - Sistema Ticketero
# Configura y levanta todo el proyecto automáticamente

set -e  # Salir si hay error

echo "🎯 Sistema Ticketero - Setup Automático"
echo "======================================"

# Verificar Java 21
echo "☕ Verificando Java 21..."
if ! java -version 2>&1 | grep -q "21\|22\|23"; then
    echo "❌ Necesitas Java 21+. Versión actual:"
    java -version 2>&1 | head -n 1 || echo "Java no encontrado"
    exit 1
fi
echo "✅ Java OK: $(java -version 2>&1 | head -n 1)"

# Verificar Docker
echo "🐳 Verificando Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado"
    exit 1
fi
if ! docker info &> /dev/null; then
    echo "❌ Docker no está ejecutándose"
    exit 1
fi
echo "✅ Docker OK"

# Crear .env si no existe
if [ ! -f .env ]; then
    echo "📝 Creando archivo .env..."
    cp .env.example .env
    echo "✅ Archivo .env creado"
fi

# Levantar servicios
echo "🚀 Levantando servicios (PostgreSQL + RabbitMQ)..."
docker-compose up -d postgres rabbitmq

# Esperar que estén listos
echo "⏳ Esperando servicios..."
sleep 10

# Verificar servicios
echo "🔍 Verificando servicios..."
if ! docker-compose ps | grep -q "postgres.*Up"; then
    echo "❌ PostgreSQL no está listo"
    docker-compose logs postgres
    exit 1
fi

if ! docker-compose ps | grep -q "rabbitmq.*Up"; then
    echo "❌ RabbitMQ no está listo"
    docker-compose logs rabbitmq
    exit 1
fi

echo "✅ Servicios listos"

# Compilar proyecto (sin tests)
echo "🔨 Compilando proyecto..."
./mvnw clean compile -DskipTests -q

echo ""
echo "🎉 ¡Setup completado!"
echo ""
echo "📋 Para ejecutar:"
echo "   ./start.sh"
echo ""
echo "📋 Servicios disponibles:"
echo "   🗄️  PostgreSQL: localhost:5432"
echo "   🐰 RabbitMQ: localhost:15672 (admin: ticketero_user/ticketero_pass)"
echo ""