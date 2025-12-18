#!/bin/bash

# Script para ejecutar el Sistema Ticketero

echo "🚀 Iniciando Sistema Ticketero..."

# Cargar variables de entorno
if [ -f .env ]; then
    echo "📋 Cargando configuración desde .env..."
    export $(cat .env | grep -v '^#' | xargs)
fi

# Configurar Java 21
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.9/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado. Instala Java 21+"
    exit 1
fi

echo "☕ Usando Java: $(java -version 2>&1 | head -n 1)"

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no está instalado. Instala Maven 3.6+"
    exit 1
fi

# Verificar base de datos
echo "🔍 Verificando conexión a base de datos..."
if ! nc -z localhost 5432 2>/dev/null; then
    echo "⚠️  PostgreSQL no está ejecutándose en puerto 5432"
    echo "💡 Ejecuta: docker run --name ticketero-db -e POSTGRES_DB=ticketero -e POSTGRES_USER=ticketero -e POSTGRES_PASSWORD=ticketero123 -p 5432:5432 -d postgres:15"
    read -p "¿Continuar de todas formas? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Compilar y ejecutar
echo "🔨 Compilando proyecto..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "❌ Error en compilación"
    exit 1
fi

echo "🏃 Ejecutando aplicación..."
echo "📱 API disponible en: http://localhost:8080/api"
echo "❤️  Health check: http://localhost:8080/api/actuator/health"
echo "📊 Métricas: http://localhost:8080/api/actuator/metrics"
echo ""
echo "🛑 Para detener: Ctrl+C"
echo ""

mvn spring-boot:run