from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
import pymysql as mdb
from datetime import datetime
from typing import List
import api
import aiohttp
import asyncio

app = FastAPI()

# Настройки CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Модель данных
class UserData(BaseModel):
    Name: str
    Password: str

class UserRegistration(BaseModel):
    Login: str
    Password: str
    Name: str
    Item1: str
    BirthTime: str
    BirthDate: str

class HoroscopeData(BaseModel):
    title: str
    content: str

class HoroscopeResponse(BaseModel):
    status: str
    message: str
    horoscopes: List[HoroscopeData]


# Подключение к базе данных
def get_connection():
    return mdb.connect(
        host="localhost",
        user="root",
        password="",
        database="Natal",
        autocommit=True
    )


@app.post("/receive-data")
async def receive_data(data: UserData):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute(
            "SELECT 1 from Userss where login = %s and pass = %s;",
            (data.Name, data.Password))
        res = cursor.fetchall()
        if (res == ((1,),)):
            print(res)
            return {
                "status": "True",
                "message": f"Пользователь {data.Name} вошёл в систему.",
                "received_data": {
                    "name": data.Name,
                    "password": data.Password
                }
            }
        else:
            return {
                "status": "False",
                "message": f"Неверный логин или пароль",
                "received_data": {
                    "name": None,
                    "password": None
                }
            }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")

@app.post("/registration")
async def registration(data: UserRegistration):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute(
            "SELECT 1 from Userss where name = %s;",
            (data.Name))
        res = cursor.fetchall()
        if (res == ((1,),)):
            return {
                "status": "False",
                "message": f"Такой логин уже существует",
                "received_data": {
                    "name": None,
                    "password": None
                }
            }
        else:
            cursor.execute(
                "INSERT INTO `Userss`(`login`, `pass`, `name`, `id_city`, `time_birthday`, `date_birthday`) "
                "VALUES (%s, %s, %s, %s, %s, %s)",
                (data.Login, data.Password, data.Name, data.Item1, data.BirthTime, data.BirthDate))
            return {
                "status": "True",
                "message": f"Регестрация прошла успешно",
                "received_data": {
                    "name": data.Login,
                    "password": data.Password
                }
            }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")

@app.get("/get-data")
async def get_data():
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT * FROM Users;")
        records = cursor.fetchall()

        return {"records": records}

    except Exception as e:
        print(f"Ошибка при получении данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.get("/get-horoscopes", response_model=HoroscopeResponse)
async def get_horoscopes():
    # Получаем все гороскопы
    description = await api.get_all_horoscopes_async()

    # Параллельный перевод всех текстов
    translated_texts = await api.translate_all_texts_async(description)



    return HoroscopeResponse(
        status="True",
        message="Horoscopes retrieved successfully",
        horoscopes=[
            HoroscopeData(title="Овен", content=translated_texts['aries']),
            HoroscopeData(title="Телец", content=translated_texts['taurus']),
            HoroscopeData(title="Близнецы", content=translated_texts['gemini']),
            HoroscopeData(title="Рак", content=translated_texts['cancer']),
            HoroscopeData(title="Лев", content=translated_texts['leo']),
            HoroscopeData(title="Дева", content=translated_texts['virgo']),
            HoroscopeData(title="Весы", content=translated_texts['libra']),
            HoroscopeData(title="Скорпион", content=translated_texts['scorpio']),
            HoroscopeData(title="Стрелец", content=translated_texts['sagittarius']),
            HoroscopeData(title="Козерог", content=translated_texts['capricorn']),
            HoroscopeData(title="Водолей", content=translated_texts['aquarius']),
            HoroscopeData(title="Рыбы", content=translated_texts['pisces'])
        ]
    )





if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)