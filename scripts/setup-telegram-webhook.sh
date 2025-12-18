#!/bin/bash

# Script para configurar el webhook de Telegram

ENV_FILE="$(dirname "$0")/../ticketero-system/.env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    echo "❌ Error: No se encontró el archivo .env"
    exit 1
fi

BOT_TOKEN="$TELEGRAM_BOT_TOKEN"
WEBHOOK_URL="${1:-http://localhost:8080/api/telegram/webhook}"

echo "🤖 Configurando Webhook de Telegram"
echo "Token: ${BOT_TOKEN:0:10}..."
echo "Webhook URL: $WEBHOOK_URL"
echo ""

# 1. Verificar que el servicio esté corriendo
echo "1️⃣ Verificando servicio local..."
if curl -s http://localhost:8080/api/health > /dev/null; then
    echo "✅ Servicio local activo"
else
    echo "❌ Servicio local no responde"
    echo "   Ejecuta: cd ticketero-system && ./run.sh"
    exit 1
fi

# 2. Configurar webhook via API local
echo ""
echo "2️⃣ Configurando webhook via API local..."
response=$(curl -s -X POST "http://localhost:8080/api/telegram/webhook/setup?webhookUrl=$WEBHOOK_URL")
echo "Respuesta: $response"

# 3. Verificar configuración
echo ""
echo "3️⃣ Verificando configuración..."
webhook_info=$(curl -s "http://localhost:8080/api/telegram/webhook/info")
echo "Info del webhook: $webhook_info"

# 4. Enviar mensaje de prueba
echo ""
echo "4️⃣ Enviando mensaje de prueba..."
if [[ -n "$TELEGRAM_CHAT_ID" ]]; then
    test_message="🔧 Webhook configurado correctamente - $(date '+%H:%M:%S')"
    curl -s -X POST "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
        -d "chat_id=$TELEGRAM_CHAT_ID" \
        -d "text=$test_message" > /dev/null
    echo "✅ Mensaje de prueba enviado al chat $TELEGRAM_CHAT_ID"
else
    echo "⚠️ TELEGRAM_CHAT_ID no configurado, saltando mensaje de prueba"
fi

echo ""
echo "🎯 Configuración completada!"
echo ""
echo "📱 Para probar el bot:"
echo "   1. Abre Telegram"
echo "   2. Busca: @Ticketero14_amazonQ_Bot"
echo "   3. Envía: /start"
echo "   4. Envía tu cédula: 12345678"
echo ""
echo "🔍 Para debug:"
echo "   tail -f ticketero-system/app.log"