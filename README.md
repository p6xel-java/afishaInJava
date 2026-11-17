<div align="center">

#  Afisha

**Веб-приложение для поиска мероприятий и покупки электронных билетов**

Java 17 · Spring Boot 4 · PostgreSQL · Spring Security · JWT · YooKassa · Docker

[Возможности](#-возможности) · [Быстрый старт](#-быстрый-старт) · [API](#-api) · [Оплата билетов](#-как-работает-оплата) · [Разработка](#-локальная-разработка)

</div>

##  О проекте

**Afisha** — учебный full-stack проект платформы мероприятий. Пользователи могут просматривать афишу, регистрироваться, входить в аккаунт и оплачивать билеты через YooKassa. После подтверждения оплаты система формирует ссылку на электронный билет, генерирует QR-код и отправляет письмо.

Frontend написан с помощью ИИ на HTML, CSS и JavaScript и обслуживается самим Spring Boot — отдельный Node.js-сервер не требуется. Backend предоставляет REST API и интерактивную OpenAPI-документацию.

##  Возможности

- Просмотр, поиск и сортировка мероприятий в веб-интерфейсе.
- Регистрация и вход по email/паролю; аутентификация с помощью JWT.
- Получение профиля текущего пользователя.
- Создание мероприятий пользователем с ролью `ADMIN`.
- Импорт мероприятий из публичного API KudaGo.
- Создание платежа и перенаправление на страницу оплаты YooKassa.
- Обработка webhook `payment.succeeded`, проверка платежа через API YooKassa и перевод билета в статус `PAID`.
- Генерация QR-кода, отправка билета по email и просмотр оплаченного билета по уникальному токену.
- Swagger UI, Docker Compose и тесты на JUnit/Mockito.

##  Технологии

| Компонент | Технологии |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1.1, Spring Web MVC |
| Безопасность | Spring Security, JWT (JJWT), BCrypt |
| Данные | PostgreSQL 16, Spring Data JPA, Hibernate |
| Платежи | YooKassa REST API |
| Email | Spring Mail, Mailpit (локально) |
| QR-коды | ZXing |
| API-документация | springdoc-openapi, Swagger UI |
| Frontend | HTML, CSS, Vanilla JavaScript |
| Инфраструктура | Docker, Docker Compose, ngrok |
| Сборка и тесты | Maven Wrapper, JUnit, Mockito |

##  Быстрый старт

### Требования

- Git;
- Docker и Docker Compose;
- тестовый магазин YooKassa (`shopId` и секретный ключ);
- аккаунт ngrok с **authtoken** и зарезервированным доменом для фиксированного публичного URL.

### 1. Клонируйте репозиторий

```bash
git clone https://github.com/p6xel-java/afishaInJava.git
cd afishaInJava
```

### 2. Настройте переменные окружения

```bash
cp .env.example .env
```

Добавьте в `.env` значения (вместо примеров — свои реквизиты):

```dotenv
JWT_SECRET=<random-secret-at-least-32-bytes>
YOOKASSA_SHOP_ID=<your-test-shop-id>
YOOKASSA_SECRET_KEY=<your-test-secret-key>
NGROK_AUTHTOKEN=<your-ngrok-authtoken>
NGROK_DOMAIN=<your-reserved-domain.ngrok-free.app>
```




```yaml
APP_BASE_URL: https://<your-reserved-domain.ngrok-free.app>
```

В `.env` при этом используйте `NGROK_DOMAIN` **без** `https://`; в `APP_BASE_URL` укажите полный URL с `https://`.

### 3. Запустите контейнеры

```bash
docker compose up --build -d
```

Проверить состояние и логи:

```bash
docker compose ps
docker compose logs -f app
```

| Сервис | Адрес |
| --- | --- |
| Веб-приложение | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Mailpit (входящие тестовые письма) | http://localhost:8025 |
| ngrok inspector | http://localhost:4041 |
| PostgreSQL (на хосте) | `localhost:5432` |

Mailpit принимает письма локально и **не отправляет** их на реальные почтовые ящики.

Остановить приложение:

```bash
docker compose down
```

Данные PostgreSQL сохраняются в именованном Docker volume `postgres_data`. Команда `docker compose down -v` **удаляет** этот volume вместе с данными.

##  API

Базовый путь: `/api/v1`. Полные схемы запросов, ответов и описания ошибок доступны в [Swagger UI](http://localhost:8080/swagger-ui/index.html) после запуска.

| Метод | Endpoint | Назначение | Доступ |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Регистрация пользователя | Публичный |
| `POST` | `/auth/login` | Вход, получение JWT | Публичный |
| `GET` | `/users/me` | Профиль текущего пользователя | JWT |
| `GET` | `/events` | Список мероприятий | Публичный |
| `GET` | `/events/{id}` | Мероприятие по ID | Публичный |
| `POST` | `/events` | Создание мероприятия | `ADMIN` |
| `POST` | `/payments/intent` | Создание платежа | JWT |
| `POST` | `/payments/webhook` | Webhook от YooKassa | Публичный URL |
| `GET` | `/tickets/{token}` | Данные оплаченного билета | По токену билета |


### Примеры запросов

**Регистрация**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "user@example.com",
    "password": "strongPassword123",
    "firstName": "Иван",
    "lastName": "Иванов"
  }'
```

**Вход**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"strongPassword123"}'
```

Возьмите JWT из ответа и передавайте его в заголовке `Authorization: Bearer <JWT>`.

**Просмотр мероприятий**

```bash
curl http://localhost:8080/api/v1/events
```

**Создание платежа**

```bash
curl -X POST http://localhost:8080/api/v1/payments/intent \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <JWT>' \
  -d '{"eventId": 1}'
```



**Создание мероприятия** (нужна роль `ADMIN`):

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <ADMIN_JWT>' \
  -d '{
    "title": "Концерт",
    "description": "Живое выступление",
    "venue": "Концертный зал",
    "price": 1500.00,
    "startDateTime": "2027-01-15T19:30:00"
  }'
```

Регистрация создает обычного пользователя; механизм назначения роли `ADMIN` через публичный API не предусмотрен.

##  Как работает оплата



QR-код содержит ссылку вида `/ticket.html?token=...`. API `/tickets/{token}` возвращает информацию только для билета со статусом `PAID`; в базе хранится хеш токена, а не сам токен.

### Настройка webhook YooKassa

1. Запустите контейнеры и проверьте работу публичного адреса ngrok.
2. В настройках уведомлений тестового магазина YooKassa укажите `https://<your-reserved-domain.ngrok-free.app>/api/v1/payments/webhook`.
3. Подпишитесь на событие `payment.succeeded`.
4. Проведите тестовую оплату и проверьте статус билета, логи `app` и письмо в Mailpit.

> Возврат пользователя на `payment-result.html` сам по себе **не подтверждает оплату**. Подтверждение выполняется при обработке webhook.

##  Структура проекта

```text
.
├── compose.yaml                # PostgreSQL, приложение, Mailpit, ngrok
├── Dockerfile                  # Multi-stage сборка Java-приложения
├── pom.xml                     # Maven-зависимости и плагины
├── .env.example                # Шаблон локальных переменных
├── mvnw / mvnw.cmd             # Maven Wrapper
└── src/
    ├── main/
    │   ├── java/com/example/afisha/
    │   │   ├── config/        # Конфигурация Spring и безопасности
    │   │   ├── controller/    # REST API
    │   │   ├── dto/           # Модели запросов и ответов
    │   │   ├── entity/        # JPA-сущности
    │   │   ├── exception/     # Обработка ошибок
    │   │   ├── repository/    # Доступ к PostgreSQL
    │   │   ├── security/      # JWT-фильтр и утилиты
    │   │   └── service/       # Бизнес-логика и интеграции
    │   └── resources/
    │       ├── application.properties
    │       └── static/        # HTML, CSS, JavaScript
    └── test/                  # Автоматические тесты
```

##  Локальная разработка

Для запуска Java-приложения вне Docker нужны **JDK 17**, доступная PostgreSQL и SMTP-служба. Проще всего поднять зависимости отдельно:

```bash
docker compose up -d db mailpit
```

Задайте переменные окружения `JWT_SECRET`, `YOOKASSA_SHOP_ID`, `YOOKASSA_SECRET_KEY`, а также при необходимости `SPRING_DATASOURCE_*` и `SPRING_MAIL_*`. Убедитесь, что исправлены свойства YooKassa, описанные в разделе «Быстрый старт».

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Проверка тестов:

```bash
./mvnw test
```

Сборка JAR:

```bash
./mvnw clean package
```


##  Безопасность и ограничения

- JWT используется для защищенных маршрутов; пароли хешируются BCrypt.
- URL webhook открыт для приема запросов из интернета. Сервис дополнительно запрашивает платеж у YooKassa и сверяет его параметры.
- Текущие Compose-настройки предназначены для **локальной разработки**, а не готового production-развертывания: используйте свои секреты, ограничивайте доступ к БД и сервисам, настраивайте HTTPS и мониторинг.
- Импортированные события могут не иметь пригодной для покупки цены или даты; покупка таких событий недоступна.

##  Автор

Разработка: [p6xel-java](https://github.com/p6xel-java)