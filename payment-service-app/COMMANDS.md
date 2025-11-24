# Payment System - Команды для запуска

## 1. Подготовка проекта

### Сборка Maven проекта
```bash
cd /home/josh/IdeaProjects/payment-system/payment-service-app
mvn clean compile
```

### Проверка checkstyle
```bash
mvn checkstyle:check
```

### Сборка JAR файла
```bash
mvn clean package -DskipTests
```

## 2. Docker команды

### Сборка Docker образа приложения
```bash
# Переименуем JAR файл для Dockerfile
cp target/payment-service-app-0.0.1-SNAPSHOT.jar target/app.jar

# Собираем Docker образ
docker build -t payment-service-app .
```

### Запуск всех сервисов через Docker Compose
```bash
# Запуск всех контейнеров
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка всех контейнеров
docker-compose down

# Остановка с удалением volumes
docker-compose down -v
```

## 3. Проверка работы сервисов

### Проверка статуса контейнеров
```bash
docker-compose ps
```

### Проверка доступности сервисов
```bash
# Spring Boot приложение
curl http://localhost:8080/api/v1/payments

# Keycloak
curl http://localhost:8085

# PostgreSQL (через pgAdmin)
# Откройте http://localhost:8081 в браузере
# Email: admin@email.com, Password: admin
```

## 4. Тестирование API

### Получение токена от Keycloak
```bash
# Используйте скрипт для получения токена
./get-token-and-test.sh
```

### Ручное тестирование API
```bash
# Получение токена
TOKEN=$(curl -X POST http://localhost:8085/realms/iprody-lms/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=user&password=password&grant_type=password&client_id=payment-client" \
  | jq -r '.access_token')

# Тест API с токеном
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/payments
```

## 5. Полезные команды

### Просмотр логов конкретного сервиса
```bash
docker-compose logs -f payment-service-app
docker-compose logs -f postgres
docker-compose logs -f keycloak
```

### Перезапуск конкретного сервиса
```bash
docker-compose restart payment-service-app
```

### Подключение к базе данных
```bash
docker exec -it postgres-db psql -U admin -d payment-db
```

### Очистка Docker ресурсов
```bash
# Удаление всех остановленных контейнеров
docker container prune

# Удаление неиспользуемых образов
docker image prune

# Полная очистка (осторожно!)
docker system prune -a
```

## 6. Порты сервисов

- **Spring Boot приложение**: http://localhost:8080
- **Keycloak**: http://localhost:8085
- **PostgreSQL**: localhost:5432
- **PgAdmin**: http://localhost:8081

## 7. Учетные данные

### Keycloak
- Admin: admin / admin
- Realm: iprody-lms
- Client: payment-client

### PostgreSQL
- User: admin
- Password: secret
- Database: payment-db

### PgAdmin
- Email: admin@email.com
- Password: admin
