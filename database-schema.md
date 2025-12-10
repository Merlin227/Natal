# Схема базы данных

## ER-диаграмма базы данных

```mermaid
erDiagram
    Users ||--o{ Posts : "создает"
    Users ||--o{ Comments : "пишет"
    Users ||--o{ Votes : "голосует"
    Users ||--o{ Subscriptions : "подписывается"
    Users ||--o{ History : "имеет"
    Users }o--|| Cities : "живет в"
    Users }o--|| Roles : "имеет роль"
    
    Posts ||--o{ Comments : "содержит"
    Posts }o--|| Categories : "принадлежит"
    Posts ||--o{ Votes : "имеет"
    
    Comments ||--o{ Comments : "ответ на"
    Comments ||--o{ Votes : "имеет"
    
    Categories ||--o{ Posts : "содержит"
    Categories ||--o{ Subscriptions : "имеет подписчиков"
    
    Planets ||--o{ Planet_sign : "в знаке"
    Planets ||--o{ Planet_house : "в доме"
    
    Signs ||--o{ Planet_sign : "содержит планету"
    Houses ||--o{ Planet_house : "содержит планету"

    Users {
        int id_user PK
        string login
        string pass
        date date_birth
        time time_birth
        int id_city FK
        int id_role FK
        string device_token
    }
    
    Posts {
        int id PK
        string title
        text content
        int user_id FK
        int category_id FK
        timestamp created_at
        timestamp updated_at
        int upvotes
        int downvotes
        int comment_count
        boolean is_approved
    }
    
    Comments {
        int id PK
        int post_id FK
        int user_id FK
        int parent_comment_id FK
        text content
        timestamp created_at
        int upvotes
        int downvotes
    }
    
    Categories {
        int id PK
        string name
        text description
        timestamp created_at
        boolean is_active
    }
    
    Cities {
        int id_city PK
        string name
        decimal latitude
        decimal longitude
    }
    
    Roles {
        int id_role PK
        string name
    }
    
    Planets {
        int id_planet PK
        string planet_name
        text general_meaning
    }
    
    Signs {
        int id_sign PK
        string name
        text description
    }
    
    Houses {
        int id_house PK
        text description
    }
    
    Planet_sign {
        int id PK
        int sign_id FK
        int id_planet FK
        text interpretation
    }
    
    Planet_house {
        int id PK
        int id_planet FK
        int house_id FK
        text interpretation
    }
    
    Votes {
        int id PK
        int user_id FK
        int post_id FK
        int comment_id FK
        int vote_type
        timestamp created_at
    }
    
    Subscriptions {
        int id PK
        int user_id FK
        int category_id FK
        timestamp created_at
    }
    
    History {
        int id_history PK
        int id_user FK
        string action_type
        text action_description
        datetime action_date
        string ip_address
    }
```

## Описание таблиц

### Основные таблицы пользователей

**Users** - Пользователи системы
- Хранит учетные данные (логин, пароль)
- Дата и время рождения для астрологических расчетов
- Связь с городом для определения координат
- Роль пользователя (администратор, пользователь, астролог)
- Токен устройства для push-уведомлений

**Roles** - Роли пользователей
- Администратор
- Пользователь
- Астролог

**Cities** - Города
- Название города
- Географические координаты (широта, долгота)

### Таблицы сообщества

**Posts** - Посты сообщества
- Заголовок и содержание
- Автор (связь с Users)
- Категория
- Система голосования (upvotes/downvotes)
- Количество комментариев
- Модерация (is_approved)

**Comments** - Комментарии
- Содержание комментария
- Связь с постом
- Автор комментария
- Поддержка вложенных комментариев (parent_comment_id)
- Система голосования

**Categories** - Категории постов
- Название и описание
- Активность категории

**Votes** - Голоса
- Голосование за посты и комментарии
- Тип голоса (1 - upvote, -1 - downvote)

**Subscriptions** - Подписки
- Подписки пользователей на категории

### Астрологические таблицы

**Planets** - Планеты
- Название планеты
- Общее значение

**Signs** - Знаки зодиака
- Название знака
- Описание

**Houses** - Дома натальной карты
- Номер дома (1-12)
- Описание значения дома

**Planet_sign** - Планеты в знаках
- Интерпретация планеты в конкретном знаке зодиака

**Planet_house** - Планеты в домах
- Интерпретация планеты в конкретном доме

### Служебные таблицы

**History** - История действий
- Логирование действий пользователей
- Тип действия и описание
- IP-адрес
