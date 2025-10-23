from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware  # Важно для мобильного приложения!

# Создаем экземпляр приложения
app = FastAPI()

# ВАЖНО: Настройка CORS (Cross-Origin Resource Sharing)
# Это разрешит вашему мобильному приложению обращаться к серверу.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Для разработки можно указать "*" (все источники).
                          # В продакшене замените на точный адрес вашего приложения.
    allow_credentials=True,
    allow_methods=["*"],  # Разрешить все методы (GET, POST, etc.)
    allow_headers=["*"],  # Разрешить все заголовки
)

# Модель данных для примера (Pydantic модель)
class UserRequest(BaseModel):
    name: str
    email: str

# Простой GET-запрос для проверки связи
@app.get("/")
async def read_root():
    return {"message": "Hello from Python Server!"}

# Эндпоинт для получения данных по ID
@app.get("/user/{user_id}")
async def read_user(user_id: int):
    return {"user_id": user_id, "name": "John Doe"}

# Эндпоинт для приема данных (POST-запрос)
@app.post("/user/")
async def create_user(user: UserRequest):
    # Здесь можно сохранить пользователя в базу данных
    return {"message": f"User {user.name} with email {user.email} created successfully!"}