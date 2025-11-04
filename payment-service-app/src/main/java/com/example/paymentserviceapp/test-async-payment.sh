#!/bin/bash

echo "═══════════════════════════════════════════════════════════════"
echo "  ТЕСТИРОВАНИЕ АСИНХРОННОЙ ОБРАБОТКИ ПЛАТЕЖЕЙ"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Получаем токен (используем admin для создания платежей)
echo "🔑 Шаг 1: Получение токена аутентификации (admin)..."
TOKEN=$(docker exec payment-service-app wget -qO- --post-data="grant_type=password&client_id=basic_client&client_secret=myclient-secret&username=admin&password=adminpassword" --header="Content-Type: application/x-www-form-urlencoded" http://keycloak:8080/realms/iprody-lms/protocol/openid-connect/token | jq -r '.access_token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Ошибка: Не удалось получить токен"
    exit 1
fi

echo "✅ Токен получен успешно"
echo ""

# Тест 1: Платеж с четной суммой
echo "═══════════════════════════════════════════════════════════════"
echo "📝 ТЕСТ 1: Создание платежа с ЧЕТНОЙ суммой (100.00)"
echo "   Ожидаемый результат: PROCESSING → SUCCEEDED (через 30 сек)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "➤ Создаем платеж..."
RESPONSE1=$(curl -s -X POST "http://localhost:8080/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "inquiryRefId": "'$(uuidgen)'",
    "amount": 100.00,
    "currency": "USD",
    "note": "Test payment - even amount (should succeed)"
  }')

PAYMENT_GUID_1=$(echo $RESPONSE1 | jq -r '.guid')
STATUS_1=$(echo $RESPONSE1 | jq -r '.status')

echo "✅ Платеж создан: $PAYMENT_GUID_1"
echo "📊 Начальный статус: $STATUS_1"
echo ""

# Тест 2: Платеж с нечетной суммой
echo "═══════════════════════════════════════════════════════════════"
echo "📝 ТЕСТ 2: Создание платежа с НЕЧЕТНОЙ суммой (99.00)"
echo "   Ожидаемый результат: PROCESSING → CANCELED (через 30 сек)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "➤ Создаем платеж..."
RESPONSE2=$(curl -s -X POST "http://localhost:8080/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "inquiryRefId": "'$(uuidgen)'",
    "amount": 99.00,
    "currency": "USD",
    "note": "Test payment - odd amount (should be canceled)"
  }')

PAYMENT_GUID_2=$(echo $RESPONSE2 | jq -r '.guid')
STATUS_2=$(echo $RESPONSE2 | jq -r '.status')

echo "✅ Платеж создан: $PAYMENT_GUID_2"
echo "📊 Начальный статус: $STATUS_2"
echo ""

# Ожидание асинхронной обработки
echo "═══════════════════════════════════════════════════════════════"
echo "⏳ Ожидание асинхронной обработки (30 секунд)..."
echo "═══════════════════════════════════════════════════════════════"
for i in {30..1}; do
    echo -ne "\r⏱️  Осталось: $i секунд  "
    sleep 1
done
echo ""
echo "✅ Ожидание завершено"
echo ""

# Проверка финальных статусов
echo "═══════════════════════════════════════════════════════════════"
echo "🔍 ПРОВЕРКА ФИНАЛЬНЫХ СТАТУСОВ"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "➤ Проверяем платеж #1 (четная сумма)..."
FINAL_RESPONSE_1=$(curl -s -X GET "http://localhost:8080/api/v1/payments/$PAYMENT_GUID_1" \
  -H "Authorization: Bearer $TOKEN")
FINAL_STATUS_1=$(echo $FINAL_RESPONSE_1 | jq -r '.status')

echo "   Финальный статус: $FINAL_STATUS_1"
if [ "$FINAL_STATUS_1" = "SUCCEEDED" ]; then
    echo "   ✅ ТЕСТ ПРОЙДЕН"
else
    echo "   ❌ ТЕСТ ПРОВАЛЕН"
fi
echo ""

echo "➤ Проверяем платеж #2 (нечетная сумма)..."
FINAL_RESPONSE_2=$(curl -s -X GET "http://localhost:8080/api/v1/payments/$PAYMENT_GUID_2" \
  -H "Authorization: Bearer $TOKEN")
FINAL_STATUS_2=$(echo $FINAL_RESPONSE_2 | jq -r '.status')

echo "   Финальный статус: $FINAL_STATUS_2"
if [ "$FINAL_STATUS_2" = "CANCELED" ]; then
    echo "   ✅ ТЕСТ ПРОЙДЕН"
else
    echo "   ❌ ТЕСТ ПРОВАЛЕН"
fi
echo ""

# Итоговый результат
echo "═══════════════════════════════════════════════════════════════"
if [ "$FINAL_STATUS_1" = "SUCCEEDED" ] && [ "$FINAL_STATUS_2" = "CANCELED" ]; then
    echo "✅ ВСЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО!"
else
    echo "❌ НЕКОТОРЫЕ ТЕСТЫ ПРОВАЛЕНЫ"
fi
echo "═══════════════════════════════════════════════════════════════"