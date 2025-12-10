# Архитектура системы Natal

## Общая архитектура системы

```mermaid
graph TB
    subgraph "Клиентский уровень"
        A[Android приложение<br/>Kotlin]
        A1[MainActivity<br/>Вход/Регистрация]
        A2[FirstActivity<br/>Главное меню]
        A3[HoroscopeActivity<br/>Гороскопы]
        A4[NatalActivity<br/>Натальная карта]
        A5[CompatibilityActivity<br/>Совместимость]
        A6[CommunityActivity<br/>Сообщество]
        A7[ProfileActivity<br/>Профиль]
        A8[AdminActivity<br/>Админ-панель]
        A9[Firebase Messaging<br/>Push-уведомления]
    end

    subgraph "Прикладной уровень"
        B[FastAPI сервер<br/>Python]
        B1[API Endpoints<br/>REST API]
        B2[Бизнес-логика<br/>Астрология]
        B3[Авторизация<br/>Проверка пользователей]
        B4[Firebase Admin SDK<br/>Отправка уведомлений]
        B5[Внешние API<br/>Гороскопы/Перевод]
    end

    subgraph "Уровень данных"
        C[(MySQL Database<br/>zachet)]
        C1[Users<br/>Пользователи]
        C2[Posts<br/>Посты]
        C3[Comments<br/>Комментарии]
        C4[Categories<br/>Категории]
        C5[Planets/Signs/Houses<br/>Астрология]
        C6[Cities<br/>Города]
    end

    subgraph "Внешние сервисы"
        D[Firebase Cloud Messaging<br/>Push-уведомления]
        E[Внешний API<br/>Гороскопы]
    end

    A -->|HTTP/HTTPS| B
    A1 --> B1
    A2 --> B1
    A3 --> B1
    A4 --> B1
    A5 --> B1
    A6 --> B1
    A7 --> B1
    A8 --> B1
    A9 -->|FCM Token| D

    B1 --> B2
    B1 --> B3
    B2 --> B5
    B4 --> D

    B1 -->|SQL запросы| C
    B2 --> C
    B3 --> C1

    C --> C1
    C --> C2
    C --> C3
    C --> C4
    C --> C5
    C --> C6

    B5 -->|HTTP| E
    D -->|Push| A9

    style A fill:#3DDC84
    style B fill:#009688
    style C fill:#F57C00
    style D fill:#FFA726
    style E fill:#42A5F5
```

## Компоненты системы

### Клиентский уровень (Android)
- **MainActivity**: Экран входа и регистрации
- **FirstActivity**: Главное меню приложения
- **HoroscopeActivity**: Просмотр гороскопов с кэшированием
- **NatalActivity**: Отображение натальной карты
- **CompatibilityActivity**: Расчет совместимости
- **CommunityActivity**: Сообщество с постами и комментариями
- **ProfileActivity**: Управление профилем
- **AdminActivity**: Административная панель
- **Firebase Messaging**: Получение push-уведомлений

### Прикладной уровень (FastAPI)
- **API Endpoints**: REST API для всех операций
- **Бизнес-логика**: Расчеты астрологии, совместимости
- **Авторизация**: Проверка пользователей и прав доступа
- **Firebase Admin SDK**: Отправка push-уведомлений
- **Внешние API**: Интеграция с сервисами гороскопов

### Уровень данных (MySQL)
- **Users**: Пользователи системы
- **Posts**: Посты сообщества
- **Comments**: Комментарии к постам
- **Categories**: Категории постов
- **Planets/Signs/Houses**: Астрологические данные
- **Cities**: Города для расчета координат
