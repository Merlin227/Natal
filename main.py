from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
import pymysql as mdb
from nat_map import *
from datetime import datetime
from typing import List, Optional
import api
import aiohttp
import asyncio
from compatibility import *
from datetime import datetime

app = FastAPI()


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class CompatibilityResult(BaseModel):
    percentage: int
    description: str


class CompatibilityResponse(BaseModel):
    status: str
    message: str
    compatibility_result: Optional[CompatibilityResult] = None

class CompatibilityData(BaseModel):
    login: str
    password: str
    partner_birth_date: str

class UserData(BaseModel):
    Name: str
    Password: str

class UserRegistration(BaseModel):
    Login: str
    Password: str
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

class PlanetsRequest(BaseModel):
    name: str
    password: str

class PlanetData(BaseModel):
    planetName: str
    zodiacSign: str
    housePosition: str
    description: str


class PlanetsResponse(BaseModel):
    status: str
    message: str
    planets: List[PlanetData]

class DeleteUserRequest(BaseModel):
    user_id: int
    admin_login: str
    admin_password: str

class UserResponse(BaseModel):
    id: int
    login: str
    passw: str
    id_city: int
    time_birth: str
    date_birth: str

class UsersListResponse(BaseModel):
    status: str
    message: str
    users: List[UserResponse]

class AdminAuth(BaseModel):
    admin_login: str
    admin_password: str


def get_connection():
    return mdb.connect(
        host="localhost",
        user="root",
        password="",
        database="zachet",
        autocommit=True
    )


@app.post("/receive-data")
async def receive_data(data: UserData):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute(
            "SELECT 1 from Users where login = %s and pass = %s;",
            (data.Name, data.Password))
        res = cursor.fetchall()
        if (res == ((1,),)):

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
            "SELECT 1 from Users where login = %s;",
            (data.Login))
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
                "INSERT INTO `Users`(`login`, `pass`, `id_city`, `time_birth`, `date_birth`) "
                "VALUES (%s, %s, (select Cities.id_city from Cities where Cities.name = %s), %s, %s)",
                (data.Login, data.Password, data.Item1, data.BirthTime, data.BirthDate))
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
    description = await api.get_all_horoscopes_async()
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

@app.post("/get-planets", response_model=PlanetsResponse)
async def get_planets(data: PlanetsRequest):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT Users.date_birth, Users.time_birth, Cities.latitude, Cities.longitude "
                       "FROM Users JOIN Cities ON Users.id_city = Cities.id_city "
                       "WHERE Users.login = %s AND Users.pass = %s;",
                       (data.name, data.password))
        res = cursor.fetchall()
        calculator = AstrologyCalculator()
        results = calculator.calculate_astrology(res[0][0], res[0][1], res[0][2], res[0][3])

        return PlanetsResponse(
            status="True",
            message="Horoscopes retrieved successfully",
            planets= calculator.print_results(results)
        )


    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")

@app.post("/compatibility", response_model=CompatibilityResponse)
async def compatibility(data: CompatibilityData):
    try:
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute("Select date_birth from Users where login = %s and pass = %s;",(data.login, data.password))
        res = cursor.fetchall()
        print(res[0][0])
        print(data.partner_birth_date, data.login, data.password)
        result = numerology_compatibility(datetime.strptime(data.partner_birth_date, "%Y-%m-%d").date(), res[0][0])


        return CompatibilityResponse(
            status="True",
            message="Расчет совместимости выполнен успешно",
            compatibility_result=result
        )

    except Exception as e:
        print(f"Ошибка при получении данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.get("/get-all-users")
async def get_all_users():
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("""
            SELECT Users.id_user, Users.login, Users.pass, Cities.name, Users.time_birth, Users.date_birth 
            FROM Users join Cities on Users.id_city = Cities.id_city 
            ORDER BY id_user;
        """)

        records = cursor.fetchall()

        users_list = []
        for record in records:
            user_dict = {
                "id_user": record[0],
                "login": record[1],
                "pass": record[2],
                "id_city": record[3],
                "time_birth": record[4],
                "date_birth": record[5]
            }
            users_list.append(user_dict)

        return {
            "status": "True",
            "message": f"Найдено {len(users_list)} пользователей",
            "users": users_list
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/get-user-details")
async def get_user_details(data: UserData):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("""
            SELECT Users.*, Cities.name as city_name 
            FROM Users 
            LEFT JOIN Cities ON Users.id_city = Cities.id_city 
            WHERE Users.login = %s
        """, (data.Name,))

        result = cursor.fetchone()

        if result:
            return {
                "status": "True",
                "message": "Данные пользователя получены",
                "user_data": {
                    "id": result[0],
                    "login": result[1],
                    "password": result[2],
                    "id_city": result[3],
                    "city_name": result[6],
                    "time_birth": result[4],
                    "date_birth": result[5]
                }
            }
        else:
            return {
                "status": "False",
                "message": "Пользователь не найден"
            }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/delete-user")
async def delete_user(data: DeleteUserRequest):
    try:
        # Проверка прав администратора
        if data.admin_login != "admin" or data.admin_password != "admin":
            return {
                "status": "False",
                "message": "Недостаточно прав для удаления пользователя"
            }

        conn = get_connection()
        cursor = conn.cursor()

        # Сначала проверяем, существует ли пользователь
        cursor.execute("SELECT 1 FROM Users WHERE id_user = %s", (data.user_id,))
        user_exists = cursor.fetchone()

        if not user_exists:
            return {
                "status": "False",
                "message": f"Пользователь с ID {data.user_id} не найден"
            }

        # Удаляем пользователя
        cursor.execute("DELETE FROM Users WHERE id_user = %s", (data.user_id,))

        if cursor.rowcount > 0:
            return {
                "status": "True",
                "message": f"Пользователь с ID {data.user_id} успешно удален"
            }
        else:
            return {
                "status": "False",
                "message": f"Не удалось удалить пользователя с ID {data.user_id}"
            }

    except Exception as e:
        print(f"Ошибка базы данных при удалении пользователя: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)