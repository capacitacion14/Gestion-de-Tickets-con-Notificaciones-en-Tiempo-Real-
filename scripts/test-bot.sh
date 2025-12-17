#!/bin/bash

# Script para probar el bot de Telegram

# Cargar variables del .env
ENV_FILE="$(dirname "$0")/../ticketero-system/.env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    echo "❌ Error: No se encontró el archivo .env"
    exit 1
fi

BOT_TOKEN="$TELEGRAM_BOT_TOKEN"
CHAT_ID="$TELEGRAM_CHAT_ID"
TELEGRAM_API="https://api.telegram.org/bot${BOT_TOKEN}"

echo "🤖 Probando Bot de Telegram"
echo "Token: ${BOT_TOKEN:0:10}..."
echo "Chat ID: $CHAT_ID"
echo ""

# 1. Verificar que el bot existe
echo "1️⃣ Verificando bot..."
response=$(curl -s "${TELEGRAM_API}/getMe")
if echo "$response" | grep -q '"ok":true'; then
    bot_name=$(echo "$response" | jq -r '.result.first_name')
    bot_username=$(echo "$response" | jq -r '.result.username')
    echo "✅ Bot activo: $bot_name (@$bot_username)"
else
    echo "❌ Bot no válido: $response"
    exit 1
fi

# 2. Verificar últimos mensajes
echo ""
echo "2️⃣ Últimos mensajes recibidos:"
updates=$(curl -s "${TELEGRAM_API}/getUpdates?limit=5")
if echo "$updates" | grep -q '"ok":true'; then
    echo "$updates" | jq -r '.result[] | "📱 \(.message.from.first_name): \(.message.text) (Chat: \(.message.chat.id))"' 2>/dev/null || echo "Sin mensajes recientes"
else
    echo "❌ Error obteniendo updates: $updates"
fi

# 3. Enviar mensaje de prueba
echo ""
echo "3️⃣ Enviando mensaje de prueba..."
test_message="🧪 Test del bot - $(date '+%H:%M:%S')"
response=$(curl -s -X POST "${TELEGRAM_API}/sendMessage" \
    -d "chat_id=$CHAT_ID" \
    -d "text=$test_message")

if echo "$response" | grep -q '"ok":true'; then
    message_id=$(echo "$response" | jq -r '.result.message_id')
    echo "✅ Mensaje enviado exitosamente (ID: $message_id)"
else
    echo "❌ Error enviando mensaje: $response"
fi

# 4. Verificar servicio local
echo ""
echo "4️⃣ Verificando servicio local..."
if curl -s http://localhost:8080/api/health > /dev/null; then
    echo "✅ Servicio local activo (localhost:8080)"
else
    echo "❌ Servicio local no responde"
    echo "   Ejecuta: cd ticketero-system && ./run.sh"
fi

echo ""
echo "🔍 Para debug detallado:"
echo "   tail -f ticketero-system/app.log"