# Системная архитектура приложения Natal

## Общая архитектура системы

```mermaid
graph TB
    subgraph "Уровень представления (Presentation Layer)"
        A[Android Application<br/>Kotlin + Material Design]
        A1[Activities & Fragments]
        A2[ViewModels]
        A3[Adapters]
        A4[UI Components]
    end

    subgraph "Уровень бизнес-логики (Business Layer)"
        B[Android Business Logic]
        B1[HoroscopeCacheManager]
        B2[User Session Manager]
        B3[Data Validators]
    end

    subgraph "Уровень данных (Data Layer)"
        C[Data Sources]
        C1[Retrofit API Services]
        C2[SharedPreferences]
        C3[Local Cache]
    end

    subgraph "Сетевой уровень (Network Layer)"
        N[HTTP Communication]
        N1[Retrofit Client]
        N2[Gson Converter]
        N3[OkHttp Interceptor]
    end

    subgraph "Backend API (FastAPI)"
        API[FastAPI Server]
        API1[Authentication Module]
        API2[Astrology Module]
        API3[Community Module]
        API4[Notification Module]
    end

    subgraph "Уровень данных сервера (Server Data Layer)"
        DB[(MySQL Database<br/>zachet)]
        DB1[Users & Auth Tables]
        DB2[Community Tables]
        DB3[Astrology Tables]
        DB4[Reference Tables]
    end

    subgraph "Внешние сервисы (External Services)"
        EXT[External APIs]
        EXT1[Firebase Cloud Messaging]
        EXT2[Horoscope API]
        EXT3[Translation API]
    end

    A --> A1
    A1 --> A2
    A2 --> A3
    A3 --> A4
    
    A2 --> B
    B --> B1
    B --> B2
    B --> B3
    
    B --> C
    C --> C1
    C --> C2
    C --> C3
    
    C1 --> N
    N --> N1
    N1 --> N2
    N2 --> N3
    
    N3 -->|HTTPS| API
    API --> API1
    API --> API2
    API --> API3
    API --> API4
    
    API1 --> DB
    API2 --> DB
    API3 --> DB
    API4 --> DB
    
    DB --> DB1
    DB --> DB2
    DB --> DB3
    DB --> DB4
    
    API2 --> EXT2
    API2 --> EXT3
    API4 --> EXT1
    EXT1 -->|Push| A

    style A fill:#4CAF50
    style API fill:#2196F3
    style DB fill:#FF9800
    style EXT fill:#F44336
```

## Компонентная архитектура

### Android приложение

```mermaid
graph LR
    subgraph "UI Components"
        UI1[MainActivity]
        UI2[FirstActivity]
        UI3[HoroscopeActivity]
        UI4[NatalActivity]
        UI5[CommunityActivity]
        UI6[ProfileActivity]
    end

    subgraph "Services"
        S1[ApiService]
        S2[CommunityApiService]
        S3[MyFirebaseMessagingService]
    end

    subgraph "Managers"
        M1[HoroscopeCacheManager]
    end

    subgraph "Models"
        MOD1[User]
        MOD2[Post]
        MOD3[Comment]
        MOD4[Horoscope]
    end

    UI1 --> S1
    UI2 --> S1
    UI3 --> S1
    UI3 --> M1
    UI4 --> S1
    UI5 --> S2
    UI6 --> S1
    
    S1 --> MOD1
    S2 --> MOD2
    S2 --> MOD3
    S1 --> MOD4
    
    M1 --> MOD4
    S3 --> MOD1

    style UI1 fill:#E3F2FD
    style S1 fill:#E8F5E9
    style M1 fill:#FFF3E0
```

### Backend сервер

```mermaid
graph TB
    subgraph "FastAPI Application"
        APP[FastAPI App]
        MID[CORS Middleware]
    end

    subgraph "API Routes"
        R1[Auth Routes<br/>/receive-data<br/>/registration]
        R2[User Routes<br/>/user/profile<br/>/get-all-users]
        R3[Horoscope Routes<br/>/get-horoscopes]
        R4[Natal Routes<br/>/get-planets]
        R5[Compatibility Routes<br/>/compatibility]
        R6[Community Routes<br/>/community/*]
        R7[Notification Routes<br/>/send-login-notification]
    end

    subgraph "Business Logic"
        BL1[User Service]
        BL2[Astrology Calculator]
        BL3[Compatibility Calculator]
        BL4[Community Service]
        BL5[Notification Service]
    end

    subgraph "Data Access"
        DA1[Database Connection]
        DA2[SQL Queries]
    end

    subgraph "External Integrations"
        EI1[Firebase Admin SDK]
        EI2[Horoscope API Client]
        EI3[Translation API Client]
    end

    APP --> MID
    MID --> R1
    MID --> R2
    MID --> R3
    MID --> R4
    MID --> R5
    MID --> R6
    MID --> R7

    R1 --> BL1
    R2 --> BL1
    R3 --> BL2
    R4 --> BL2
    R5 --> BL3
    R6 --> BL4
    R7 --> BL5

    BL1 --> DA1
    BL2 --> DA1
    BL3 --> DA1
    BL4 --> DA1
    BL5 --> DA1

    DA1 --> DA2
    DA2 --> DB[(MySQL)]

    BL2 --> EI2
    BL2 --> EI3
    BL5 --> EI1

    style APP fill:#2196F3
    style BL2 fill:#9C27B0
    style DA1 fill:#FF9800
    style EI1 fill:#F44336
```

## Технологический стек

### Клиент (Android)
- **Язык**: Kotlin
- **UI Framework**: Android Views + Material Design
- **Архитектура**: Activity-based (можно мигрировать на MVVM)
- **Сеть**: Retrofit 2.9.0
- **JSON**: Gson
- **Асинхронность**: Kotlin Coroutines
- **Кэширование**: SharedPreferences
- **Push-уведомления**: Firebase Cloud Messaging

### Сервер (Backend)
- **Фреймворк**: FastAPI (Python)
- **База данных**: MySQL (PyMySQL)
- **Валидация**: Pydantic
- **CORS**: FastAPI CORS Middleware
- **Push-уведомления**: Firebase Admin SDK
- **Внешние API**: aiohttp (асинхронные запросы)

### База данных
- **СУБД**: MySQL 5.7+
- **Кодировка**: utf8mb4_unicode_ci
- **Движок**: InnoDB
- **Связи**: Foreign Keys

### Внешние сервисы
- **Firebase Cloud Messaging**: Push-уведомления
- **Horoscope API**: Получение гороскопов
- **Translation API**: Перевод текстов

## Безопасность

```mermaid
graph TB
    subgraph "Уровни безопасности"
        S1[Transport Layer<br/>HTTPS/TLS]
        S2[Authentication<br/>Login/Password]
        S3[Authorization<br/>Role-based]
        S4[Data Validation<br/>Pydantic Models]
        S5[SQL Injection Protection<br/>Parameterized Queries]
    end

    Client[Android Client] -->|HTTPS| S1
    S1 --> API[FastAPI Server]
    API --> S2
    S2 --> S3
    API --> S4
    API --> S5
    S5 --> DB[(Database)]

    style S1 fill:#4CAF50
    style S2 fill:#2196F3
    style S5 fill:#F44336
```

## Масштабируемость

```mermaid
graph TB
    subgraph "Текущая архитектура"
        A1[Single Android App]
        A2[Single FastAPI Server]
        A3[Single MySQL Database]
    end

    subgraph "Возможности масштабирования"
        M1[Horizontal Scaling<br/>Несколько API серверов]
        M2[Database Replication<br/>Master-Slave]
        M3[Load Balancer<br/>Nginx/HAProxy]
        M4[Caching Layer<br/>Redis]
        M5[CDN<br/>Static Assets]
    end

    A1 --> M3
    M3 --> M1
    M1 --> M4
    M4 --> M2
    A2 -.->|Миграция| M1
    A3 -.->|Миграция| M2

    style M1 fill:#4CAF50
    style M2 fill:#2196F3
    style M4 fill:#FF9800
```
