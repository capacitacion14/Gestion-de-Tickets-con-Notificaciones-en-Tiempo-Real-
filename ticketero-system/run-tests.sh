#!/bin/bash

# Script para ejecutar tests funcionales fácilmente
set -e

echo "🚀 Iniciando tests funcionales del Sistema Ticketero..."

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Instalando..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "📥 Descargando Docker Desktop para macOS..."
        curl -o Docker.dmg https://desktop.docker.com/mac/main/arm64/Docker.dmg
        hdiutil attach Docker.dmg
        cp -R "/Volumes/Docker 1/Docker.app" /Applications/
        hdiutil detach "/Volumes/Docker 1"
        echo "✅ Docker Desktop instalado. Ábrelo manualmente y espera que inicie."
        exit 1
    fi
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "📥 Descargando Maven..."
    curl -O https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
    tar -xzf apache-maven-3.9.6-bin.tar.gz
    export PATH=$PWD/apache-maven-3.9.6/bin:$PATH
fi

# Verificar que Docker esté corriendo
echo "🐳 Verificando Docker..."
if ! docker ps &> /dev/null; then
    echo "❌ Docker no está corriendo. Iniciando Docker Desktop..."
    open -a Docker
    echo "⏳ Esperando que Docker inicie (60 segundos)..."
    sleep 60
    
    # Verificar nuevamente
    if ! docker ps &> /dev/null; then
        echo "❌ Docker aún no está listo. Ejecuta manualmente: open -a Docker"
        echo "   Luego ejecuta este script nuevamente."
        exit 1
    fi
fi

echo "✅ Docker está corriendo"

# Ejecutar tests
echo "🧪 Ejecutando tests funcionales..."

case "${1:-all}" in
    "unit")
        echo "📋 Ejecutando solo tests unitarios..."
        mvn clean test -Dtest="*Test"
        ;;
    "integration")
        echo "🔗 Ejecutando solo tests de integración..."
        mvn clean test -Dtest="*IT"
        ;;
    "all")
        echo "📊 Ejecutando todos los tests..."
        mvn clean test
        ;;
    "coverage")
        echo "📈 Ejecutando tests con reporte de cobertura..."
        mvn clean test jacoco:report
        echo "📊 Reporte disponible en: target/site/jacoco/index.html"
        ;;
    *)
        echo "❓ Uso: $0 [unit|integration|all|coverage]"
        echo "   unit        - Solo tests unitarios"
        echo "   integration - Solo tests de integración"  
        echo "   all         - Todos los tests (default)"
        echo "   coverage    - Tests + reporte de cobertura"
        exit 1
        ;;
esac

echo "✅ Tests completados exitosamente!"