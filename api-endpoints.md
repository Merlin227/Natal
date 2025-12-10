# API Endpoints - Схема взаимодействия

## Схема API endpoints

```mermaid
graph LR
    subgraph "Аутентификация"
        A1[POST /receive-data<br/>Вход]
        A2[POST /registration<br/>Регистрация]
        A3[POST /save-token<br/>Сохранение FCM токена]
        A4[POST /send-login-notification<br/>Уведомление о входе]
    end

    subgraph "Пользователи"
        U1[GET /get-all-users<br/>Список пользователей]
        U2[POST /get-user-details<br/>Детали пользователя]
        U3[POST /user/profile<br/>Получить профиль]
        U4[POST /user/profile/update<br/>Обновить профиль]
        U5[POST /delete-user<br/>Удалить пользователя]
    end

    subgraph "Гороскопы"
        H1[GET /get-horoscopes<br/>Получить гороскопы]
    end

    subgraph "Натальная карта"
        N1[POST /get-planets<br/>Позиции планет]
    end

    subgraph "Совместимость"
        C1[POST /compatibility<br/>Расчет совместимости]
    end

    subgraph "Сообщество - Категории"
        COM1[GET /community/categories<br/>Список категорий]
        COM2[POST /community/categories<br/>Создать категорию]
        COM3["POST /community/subscribe/[id]<br/>Подписаться"]
    end

    subgraph "Сообщество - Посты"
        COM4[GET /community/posts<br/>Список постов]
        COM5["GET /community/posts/[id]<br/>Детали поста"]
        COM6[POST /community/posts<br/>Создать пост]
    end

    subgraph "Сообщество - Комментарии"
        COM7["GET /community/posts/[id]/comments<br/>Комментарии поста"]
        COM8[POST /community/comments<br/>Создать комментарий]
    end

    subgraph "Сообщество - Голосование"
        COM9[POST /community/vote<br/>Голосовать]
    end

    subgraph "Справочники"
        S1[GET /cities<br/>Список городов]
        S2[GET /get-data<br/>Все пользователи]
    end

    style A1 fill:#4CAF50
    style A2 fill:#4CAF50
    style H1 fill:#2196F3
    style N1 fill:#9C27B0
    style C1 fill:#FF9800
    style COM4 fill:#F44336
    style COM5 fill:#F44336
    style COM6 fill:#F44336
```

## Детальная схема API

```mermaid
sequenceDiagram
    participant Client as Android App
    participant API as FastAPI Server
    participant DB as MySQL Database
    participant FCM as Firebase Cloud Messaging
    participant ExtAPI as External API

    Note over Client,ExtAPI: Процесс входа и получения данных

    Client->>API: POST /receive-data (login, password)
    API->>DB: SELECT Users WHERE login = ?
    DB-->>API: User data
    API-->>Client: status: "True", message: "..."
    
    Client->>API: POST /save-token (user_name, device_token)
    API->>DB: UPDATE Users SET device_token = ?
    DB-->>API: Success
    API-->>Client: status: "True"
    
    Client->>API: POST /send-login-notification
    API->>DB: SELECT device_token FROM Users
    DB-->>API: device_token
    API->>FCM: Send notification
    FCM-->>Client: Push notification

    Note over Client,ExtAPI: Получение гороскопов

    Client->>API: GET /get-horoscopes
    API->>ExtAPI: Get horoscopes (async)
    ExtAPI-->>API: Horoscopes data
    API->>ExtAPI: Translate texts (async)
    ExtAPI-->>API: Translated texts
    API-->>Client: HoroscopeResponse

    Note over Client,DB: Натальная карта

    Client->>API: POST /get-planets (name, password)
    API->>DB: SELECT date_birth, time_birth, coordinates
    DB-->>API: User birth data
    API->>API: Calculate astrology (nat_map)
    API-->>Client: PlanetsResponse

    Note over Client,DB: Сообщество

    Client->>API: GET /community/posts?category_id=1&sort_by=new
    API->>DB: SELECT Posts JOIN Users JOIN Categories
    DB-->>API: Posts data
    API-->>Client: Posts list with pagination

    Client->>API: POST /community/posts (title, content, category_id)
    API->>DB: Verify user
    API->>DB: INSERT INTO Posts
    DB-->>API: post_id
    API-->>Client: status: "True", post_id: ...

    Client->>API: POST /community/comments
    API->>DB: INSERT INTO Comments
    API->>DB: UPDATE Posts SET comment_count = comment_count + 1
    DB-->>API: Success
    API-->>Client: Comment data
```

## Группировка endpoints по функциональности

### 🔐 Аутентификация и авторизация
- `POST /receive-data` - Вход в систему
- `POST /registration` - Регистрация нового пользователя
- `POST /save-token` - Сохранение FCM токена устройства
- `POST /send-login-notification` - Отправка уведомления о входе

### 👤 Управление пользователями
- `GET /get-all-users` - Получить список всех пользователей (админ)
- `POST /get-user-details` - Получить детали пользователя
- `POST /user/profile` - Получить профиль пользователя
- `POST /user/profile/update` - Обновить профиль
- `POST /delete-user` - Удалить пользователя (админ)

### 🔮 Гороскопы
- `GET /get-horoscopes` - Получить гороскопы для всех знаков зодиака

### 🌟 Натальная карта
- `POST /get-planets` - Получить позиции планет в натальной карте

### 💑 Совместимость
- `POST /compatibility` - Рассчитать совместимость двух пользователей

### 👥 Сообщество

#### Категории
- `GET /community/categories` - Список категорий
- `POST /community/categories` - Создать категорию
- `POST /community/subscribe/{category_id}` - Подписаться на категорию

#### Посты
- `GET /community/posts` - Список постов (с фильтрацией и пагинацией)
- `GET /community/posts/{post_id}` - Детали поста
- `POST /community/posts` - Создать пост

#### Комментарии
- `GET /community/posts/{post_id}/comments` - Комментарии поста
- `POST /community/comments` - Создать комментарий

#### Голосование
- `POST /community/vote` - Голосовать за пост/комментарий

### 📋 Справочники
- `GET /cities` - Список городов
- `GET /get-data` - Получить все данные (тестовый endpoint)

