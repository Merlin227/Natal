# Потоки данных в системе

## Основные потоки данных

### 1. Процесс регистрации и входа

```mermaid
flowchart TD
    Start([Пользователь открывает приложение]) --> Login{Вход или<br/>Регистрация?}
    
    Login -->|Регистрация| Reg[RegistrationActivity]
    Reg --> RegForm[Заполнение формы:<br/>Логин, Пароль, Дата/Время рождения, Город]
    RegForm --> RegAPI[POST /registration]
    RegAPI --> RegDB[(MySQL: INSERT Users)]
    RegDB --> RegSuccess{Успешно?}
    RegSuccess -->|Да| FirstAct[FirstActivity]
    RegSuccess -->|Нет| RegError[Ошибка: Логин занят]
    RegError --> RegForm
    
    Login -->|Вход| Main[MainActivity]
    Main --> LoginForm[Ввод логина/пароля]
    LoginForm --> LoginAPI[POST /receive-data]
    LoginAPI --> LoginDB[(MySQL: SELECT Users)]
    LoginDB --> LoginCheck{Пользователь<br/>найден?}
    LoginCheck -->|Нет| LoginError[Ошибка: Неверные данные]
    LoginError --> LoginForm
    LoginCheck -->|Да| SaveToken[POST /save-token]
    SaveToken --> TokenDB[(MySQL: UPDATE device_token)]
    TokenDB --> Notify[POST /send-login-notification]
    Notify --> FCM[Firebase Cloud Messaging]
    FCM --> Push[Push-уведомление на устройство]
    Push --> FirstAct
    
    Login -->|Гостевой режим| Guest[HoroscopeActivity]
    
    style Reg fill:#E3F2FD
    style Main fill:#E8F5E9
    style Guest fill:#FFF3E0
    style FirstAct fill:#F3E5F5
```

### 2. Получение и кэширование гороскопов

```mermaid
flowchart TD
    Start([Открытие HoroscopeActivity]) --> CheckCache{Кэш<br/>валиден?}
    
    CheckCache -->|Да| LoadCache[Загрузить из кэша]
    LoadCache --> Display[Отобразить гороскопы]
    
    CheckCache -->|Нет| API[GET /get-horoscopes]
    API --> ExtAPI1[Внешний API:<br/>Получить гороскопы]
    ExtAPI1 --> ExtAPI2[Внешний API:<br/>Перевести тексты]
    ExtAPI2 --> Response[Получить ответ]
    Response --> SaveCache[Сохранить в кэш]
    SaveCache --> CacheDB[(SharedPreferences)]
    CacheDB --> Display
    
    Display --> Refresh{Пользователь<br/>обновил?}
    Refresh -->|Да| ClearCache[Очистить кэш]
    ClearCache --> API
    Refresh -->|Нет| Export{Экспорт?}
    Export -->|Да| Share[Поделиться текстом]
    Export -->|Нет| End([Завершение])
    
    style CheckCache fill:#FFF9C4
    style API fill:#E1F5FE
    style SaveCache fill:#E8F5E9
```

### 3. Расчет натальной карты

```mermaid
flowchart TD
    Start([NatalActivity открыта]) --> GetData[Получить данные пользователя<br/>из Intent]
    GetData --> API[POST /get-planets<br/>name, password]
    API --> Verify[Проверка пользователя]
    Verify --> DB[(MySQL: SELECT Users<br/>JOIN Cities)]
    DB --> BirthData[Дата/время рождения<br/>Координаты города]
    BirthData --> Calc[AstrologyCalculator<br/>calculate_astrology]
    
    Calc --> PlanetSign[Планета в знаке<br/>Planet_sign]
    Calc --> PlanetHouse[Планета в доме<br/>Planet_house]
    
    PlanetSign --> Interpret[Формирование описаний]
    PlanetHouse --> Interpret
    
    Interpret --> Results[PlanetsResponse:<br/>planetName, zodiacSign,<br/>housePosition, description]
    Results --> Display[Отобразить на экране]
    Display --> End([Завершение])
    
    style Calc fill:#F3E5F5
    style Interpret fill:#E8F5E9
    style Display fill:#E1F5FE
```

### 4. Работа с сообществом

```mermaid
flowchart TD
    Start([CommunityActivity]) --> LoadCats[GET /community/categories]
    LoadCats --> DisplayCats[Отобразить категории]
    
    DisplayCats --> UserAction{Действие<br/>пользователя?}
    
    UserAction -->|Просмотр постов| LoadPosts[GET /community/posts<br/>category_id, sort_by, page]
    LoadPosts --> DB1[(MySQL: SELECT Posts<br/>JOIN Users, Categories)]
    DB1 --> DisplayPosts[Отобразить посты]
    
    DisplayPosts --> PostAction{Действие с<br/>постом?}
    PostAction -->|Открыть| PostDetail[GET /community/posts/{id}]
    PostDetail --> LoadComments[GET /community/posts/{id}/comments]
    LoadComments --> DB2[(MySQL: SELECT Comments<br/>JOIN Users)]
    DB2 --> DisplayComments[Отобразить комментарии]
    
    PostAction -->|Создать| CreatePost[POST /community/posts]
    CreatePost --> Verify1[Проверка пользователя]
    Verify1 --> DB3[(MySQL: INSERT Posts)]
    DB3 --> RefreshPosts[Обновить список]
    
    PostAction -->|Комментировать| CreateComment[POST /community/comments]
    CreateComment --> Verify2[Проверка пользователя]
    Verify2 --> DB4[(MySQL: INSERT Comments<br/>UPDATE comment_count)]
    DB4 --> RefreshComments[Обновить комментарии]
    
    PostAction -->|Голосовать| Vote[POST /community/vote]
    Vote --> Verify3[Проверка пользователя]
    Verify3 --> DB5[(MySQL: UPDATE upvotes/downvotes)]
    DB5 --> RefreshVotes[Обновить голоса]
    
    style LoadPosts fill:#E3F2FD
    style CreatePost fill:#E8F5E9
    style Vote fill:#FFF3E0
```

### 5. Push-уведомления

```mermaid
sequenceDiagram
    participant App as Android App
    participant FCM as Firebase SDK
    participant Server as FastAPI
    participant FCMService as Firebase Admin
    participant Device as Устройство

    Note over App,Device: Инициализация FCM

    App->>FCM: FirebaseMessaging.getInstance()
    FCM->>FCMService: Request device token
    FCMService-->>FCM: FCM Token
    FCM-->>App: Token получен
    App->>App: Сохранить в SharedPreferences
    
    Note over App,Device: Сохранение токена на сервере
    
    App->>Server: POST /save-token<br/>{user_name, device_token}
    Server->>Server: UPDATE Users SET device_token
    Server-->>App: {status: "True"}
    
    Note over App,Device: Отправка уведомления о входе
    
    App->>Server: POST /send-login-notification<br/>{user_name, timestamp}
    Server->>Server: SELECT device_token FROM Users
    Server->>FCMService: messaging.send(message)
    FCMService->>Device: Push notification
    Device->>App: MyFirebaseMessagingService<br/>onMessageReceived()
    App->>App: Отобразить уведомление
```

## Архитектура обработки данных

```mermaid
graph TB
    subgraph "Клиент (Android)"
        C1[UI Layer<br/>Activities]
        C2[Business Logic<br/>ViewModels/Managers]
        C3[Data Layer<br/>API Services]
        C4[Cache Layer<br/>SharedPreferences]
    end
    
    subgraph "Сеть"
        N1[Retrofit<br/>HTTP Client]
        N2[Gson<br/>JSON Parser]
    end
    
    subgraph "Сервер (FastAPI)"
        S1[API Endpoints<br/>REST API]
        S2[Business Logic<br/>Астрология, Совместимость]
        S3[Data Access<br/>MySQL Queries]
        S4[External APIs<br/>Гороскопы, Перевод]
    end
    
    subgraph "База данных"
        D1[(MySQL<br/>zachet)]
    end
    
    subgraph "Внешние сервисы"
        E1[Firebase Cloud Messaging]
        E2[Horoscope API]
        E3[Translation API]
    end
    
    C1 --> C2
    C2 --> C3
    C3 --> C4
    C3 --> N1
    N1 --> N2
    N2 --> S1
    S1 --> S2
    S2 --> S3
    S2 --> S4
    S3 --> D1
    S4 --> E2
    S4 --> E3
    S1 --> E1
    
    style C1 fill:#4CAF50
    style S1 fill:#2196F3
    style D1 fill:#FF9800
    style E1 fill:#F44336
```
