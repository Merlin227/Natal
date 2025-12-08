from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from pymysql.cursors import DictCursor
import pymysql as mdb
from nat_map import *
from datetime import datetime
from typing import List, Optional, Any
import api
import aiohttp
import asyncio
from compatibility import *
from typing import Optional
from fastapi import Depends
from fastapi.security import HTTPBasic, HTTPBasicCredentials
import secrets

app = FastAPI()

security = HTTPBasic()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ProfileUpdate(BaseModel):
    login: str
    password: str
    new_password: Optional[str] = None
    city_name: Optional[str] = None
    date_birth: Optional[str] = None
    time_birth: Optional[str] = None

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


class CategoryCreate(BaseModel):
    name: str
    description: str



class PostCreate(BaseModel):
    title: str
    content: str
    category_id: int
    login: str
    password: str


class CommentCreate(BaseModel):
    content: str
    post_id: int
    parent_comment_id: Optional[int] = None
    login: str
    password: str


class VoteRequest(BaseModel):
    vote_type: int
    login: str
    password: str


class SubscriptionRequest(BaseModel):
    login: str
    password: str

class BaseResponse(BaseModel):
    status: str
    message: str
    data: Optional[Any] = None

initial_categories = [
    ("Астрология для начинающих", "Основы астрологии"),
    ("Натальная карта", "Обсуждение натальных карт"),
    ("Гороскопы", "Прогнозы и гороскопы"),
    ("Совместимость", "Вопросы совместимости"),
    ("Нумерология", "Числа и их значения"),
    ("Общие вопросы", "Другие вопросы по астрологии")
]

def get_connection():
    return mdb.connect(
        host="localhost",
        user="root",
        password="",
        database="zachet",
        autocommit=True
    )



def verify_user(login: str, password: str):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute(
            "SELECT id_user FROM Users WHERE login = %s AND pass = %s",
            (login, password)
        )
        user = cursor.fetchone()

        if user:
            return user[0]
        return None

    except Exception as e:
        print(f"Ошибка проверки пользователя: {e}")
        return None



async def get_current_user():


    class MockUser:
        def __init__(self):
            self.id = 1
            self.login = "test_user"

    return MockUser()


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
            planets=calculator.print_results(results)
        )


    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/compatibility", response_model=CompatibilityResponse)
async def compatibility(data: CompatibilityData):
    try:
        conn = get_connection()
        cursor = conn.cursor()
        cursor.execute("Select date_birth from Users where login = %s and pass = %s;", (data.login, data.password))
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
        if data.admin_login != "admin" or data.admin_password != "admin":
            return {
                "status": "False",
                "message": "Недостаточно прав для удаления пользователя"
            }

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT 1 FROM Users WHERE id_user = %s", (data.user_id,))
        user_exists = cursor.fetchone()

        if not user_exists:
            return {
                "status": "False",
                "message": f"Пользователь с ID {data.user_id} не найден"
            }

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


@app.get("/community/categories")
async def get_categories():
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SHOW TABLES LIKE 'Categories'")
        if not cursor.fetchone():
            return {
                "status": "True",
                "message": "Таблица категорий не создана",
                "data": []  # Используем data вместо categories
            }

        cursor.execute("""
            SELECT id, name, description, 
                   COALESCE((SELECT COUNT(*) FROM Subscriptions WHERE category_id = Categories.id), 0) as subscribers_count
            FROM Categories 
            WHERE is_active = TRUE OR is_active IS NULL
            ORDER BY name
        """)
        categories = cursor.fetchall()

        categories_list = [
            {
                "id": c[0],
                "name": c[1],
                "description": c[2],
                "subscribers": c[3]
            }
            for c in categories
        ]

        return BaseResponse(
            status="True",
            message=f"Найдено {len(categories_list)} категорий",
            data=categories_list  # Используем data
        )

    except Exception as e:
        print(f"Ошибка при получении категорий: {e}")
        return BaseResponse(
            status="False",
            message=f"Ошибка сервера: {str(e)}",
            data=[]
        )


@app.get("/community/posts/{post_id}", response_model=BaseResponse)
async def get_post(post_id: int):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("""
            SELECT p.id, p.title, p.content, p.user_id, p.category_id, 
                   p.created_at, p.updated_at, p.comment_count, p.is_approved,
                   u.login as author_name, c.name as category_name,
                   COALESCE((SELECT COUNT(*) FROM Votes WHERE post_id = p.id AND vote_type = 1), 0) as upvotes,
                   COALESCE((SELECT COUNT(*) FROM Votes WHERE post_id = p.id AND vote_type = -1), 0) as downvotes
            FROM Posts p
            JOIN Users u ON p.user_id = u.id_user
            JOIN Categories c ON p.category_id = c.id
            WHERE p.id = %s AND (p.is_approved = TRUE OR p.is_approved IS NULL)
        """, (post_id,))

        post = cursor.fetchone()

        if not post:
            return {
                "status": "False",
                "message": "Пост не найден или не одобрен"
            }

        post_data = {
            "id": post[0],
            "title": post[1],
            "content": post[2],
            "user_id": post[3],
            "category_id": post[4],
            "created_at": post[5].strftime("%Y-%m-%d %H:%M:%S") if post[5] else None,
            "updated_at": post[6].strftime("%Y-%m-%d %H:%M:%S") if post[6] else None,
            "comment_count": post[7] if post[7] else 0,
            "is_approved": post[8] if post[8] else True,
            "author_name": post[9] if post[9] else "Неизвестный",
            "category_name": post[10] if post[10] else "Без категории",
            "upvotes": post[11] if post[11] else 0,
            "downvotes": post[12] if post[12] else 0
        }

        return BaseResponse(
            status="True",
            message="Пост найден",
            data=post_data
        )

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/community/categories")
async def create_category(category: CategoryCreate):
    try:
        conn = get_connection()
        cursor = conn.cursor()


        cursor.execute("""
            INSERT INTO Categories (name, description) 
            VALUES (%s, %s)
        """, (category.name, category.description))

        return {
            "status": "True",
            "message": f"Категория '{category.name}' создана",
            "category_id": cursor.lastrowid
        }

    except Exception as e:
        print(f"Ошибка создания категории: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.get("/community/posts")
async def get_posts(category_id: Optional[int] = None,
                    sort_by: str = "new",
                    page: int = 1,
                    limit: int = 20):
    try:
        offset = (page - 1) * limit

        sort_mapping = {
            "new": "p.created_at DESC",
            "hot": "(COALESCE(p.upvotes, 0) - COALESCE(p.downvotes, 0)) DESC, p.created_at DESC",
            "top": "COALESCE(p.upvotes, 0) DESC"
        }
        order_by = sort_mapping.get(sort_by, "p.created_at DESC")

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SHOW TABLES LIKE 'posts'")
        if not cursor.fetchone():
            return {
                "status": "False",
                "message": "Таблица постов не создана",
                "posts": [],
                "pagination": {
                    "page": page,
                    "limit": limit,
                    "total_posts": 0,
                    "total_pages": 0
                }
            }

        # Подсчет общего количества постов
        count_query = "SELECT COUNT(*) FROM posts p WHERE (p.is_approved = 1 OR p.is_approved IS NULL)"
        count_params = []

        if category_id:
            count_query += " AND p.category_id = %s"
            count_params.append(category_id)

        cursor.execute(count_query, count_params)
        total_posts = cursor.fetchone()[0]
        total_pages = (total_posts + limit - 1) // limit if total_posts > 0 else 0

        # Основной запрос с фиксом
        query = """
            SELECT 
                p.id, 
                p.title, 
                p.content, 
                p.user_id, 
                p.category_id, 
                p.created_at, 
                p.updated_at, 
                p.comment_count, 
                p.is_approved,
                p.upvotes,
                p.downvotes,
                u.login as author_name, 
                c.name as category_name
            FROM posts p
            JOIN Users u ON p.user_id = u.id_user
            JOIN Categories c ON p.category_id = c.id
            WHERE (p.is_approved = 1 OR p.is_approved IS NULL)
        """

        params = []

        if category_id:
            query += " AND p.category_id = %s"
            params.append(category_id)

        query += f" ORDER BY {order_by}"
        query += " LIMIT %s OFFSET %s"

        params.extend([limit, offset])

        cursor.execute(query, params)
        posts = cursor.fetchall()

        # Форматирование результатов
        formatted_posts = []
        for post in posts:
            formatted_posts.append({
                "id": post[0],
                "title": post[1],
                "content": post[2],
                "user_id": post[3],
                "category_id": post[4],
                "created_at": post[5].strftime("%Y-%m-%d %H:%M:%S") if post[5] else None,
                "updated_at": post[6].strftime("%Y-%m-%d %H:%M:%S") if post[6] else None,
                "comment_count": post[7] or 0,
                "is_approved": bool(post[8]) if post[8] is not None else True,
                "upvotes": post[9] or 0,
                "downvotes": post[10] or 0,
                "author_name": post[11] or "Неизвестный",
                "category_name": post[12] or "Без категории"
            })

        cursor.close()
        conn.close()

        return {
            "status": "True",
            "message": f"Найдено {len(formatted_posts)} постов",
            "posts": formatted_posts,
            "pagination": {
                "page": page,
                "limit": limit,
                "total_posts": total_posts,
                "total_pages": total_pages
            }
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        return {
            "status": "False",
            "message": f"Ошибка сервера: {str(e)}",
            "posts": [],
            "pagination": {
                "page": page,
                "limit": limit,
                "total_posts": 0,
                "total_pages": 0
            }
        }


@app.post("/community/posts")
async def create_post(post: PostCreate):
    try:
        user_id = verify_user(post.login, post.password)
        if not user_id:
            return {
                "status": "False",
                "message": "Неверные учетные данные"
            }

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT id FROM Categories WHERE id = %s AND (is_active = TRUE OR is_active IS NULL)",
                       (post.category_id,))
        category = cursor.fetchone()

        if not category:
            return {
                "status": "False",
                "message": "Категория не найдена"
            }

        cursor.execute("""
            INSERT INTO Posts (title, content, user_id, category_id) 
            VALUES (%s, %s, %s, %s)
        """, (post.title, post.content, user_id, post.category_id))

        return {
            "status": "True",
            "message": "Пост создан",
            "post_id": cursor.lastrowid
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/community/comments")
async def create_comment(comment: CommentCreate):
    try:
        user_id = verify_user(comment.login, comment.password)
        if not user_id:
            return {
                "status": "False",
                "message": "Неверные учетные данные"
            }

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT id FROM Posts WHERE id = %s", (comment.post_id,))
        post = cursor.fetchone()

        if not post:
            return {
                "status": "False",
                "message": "Пост не найден"
            }

        if comment.parent_comment_id:
            cursor.execute("SELECT id FROM Comments WHERE id = %s AND post_id = %s",
                           (comment.parent_comment_id, comment.post_id))
            parent_comment = cursor.fetchone()
            if not parent_comment:
                return {
                    "status": "False",
                    "message": "Родительский комментарий не найден"
                }

        cursor.execute("""
            INSERT INTO Comments (post_id, user_id, parent_comment_id, content) 
            VALUES (%s, %s, %s, %s)
        """, (comment.post_id, user_id, comment.parent_comment_id, comment.content))

        comment_id = cursor.lastrowid

        cursor.execute("""
            UPDATE Posts SET comment_count = COALESCE(comment_count, 0) + 1 
            WHERE id = %s
        """, (comment.post_id,))

        cursor.execute("""
            SELECT c.id, c.content, c.user_id, c.parent_comment_id, 
                   c.created_at, c.upvotes, c.downvotes,
                   u.login as author_name
            FROM Comments c
            JOIN Users u ON c.user_id = u.id_user
            WHERE c.id = %s
        """, (comment_id,))

        new_comment = cursor.fetchone()

        return {
            "status": "True",
            "message": "Комментарий добавлен",
            "comment": {
                "id": new_comment[0],
                "content": new_comment[1],
                "user_id": new_comment[2],
                "parent_comment_id": new_comment[3],
                "created_at": new_comment[4].strftime("%Y-%m-%d %H:%M:%S") if new_comment[4] else None,
                "upvotes": new_comment[5] if new_comment[5] else 0,
                "downvotes": new_comment[6] if new_comment[6] else 0,
                "author_name": new_comment[7] if new_comment[7] else "Неизвестный"
            }
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/community/vote")
async def vote(vote_req: VoteRequest,
               post_id: Optional[int] = None,
               comment_id: Optional[int] = None):
    try:
        user_id = verify_user(vote_req.login, vote_req.password)
        if not user_id:
            return {
                "status": "False",
                "message": "Неверные учетные данные"
            }

        if not (post_id or comment_id):
            return {
                "status": "False",
                "message": "Укажите post_id или comment_id"
            }

        if vote_req.vote_type not in [1, -1]:
            return {
                "status": "False",
                "message": "vote_type должен быть 1 (upvote) или -1 (downvote)"
            }

        conn = get_connection()
        cursor = conn.cursor()

        if post_id:
            cursor.execute("SELECT id FROM Posts WHERE id = %s", (post_id,))
            post = cursor.fetchone()
            if not post:
                return {
                    "status": "False",
                    "message": "Пост не найден"
                }

            if vote_req.vote_type == 1:
                cursor.execute("UPDATE Posts SET upvotes = COALESCE(upvotes, 0) + 1 WHERE id = %s", (post_id,))
            else:
                cursor.execute("UPDATE Posts SET downvotes = COALESCE(downvotes, 0) + 1 WHERE id = %s", (post_id,))

        elif comment_id:
            cursor.execute("SELECT id FROM Comments WHERE id = %s", (comment_id,))
            comment = cursor.fetchone()
            if not comment:
                return {
                    "status": "False",
                    "message": "Комментарий не найден"
                }

            if vote_req.vote_type == 1:
                cursor.execute("UPDATE Comments SET upvotes = COALESCE(upvotes, 0) + 1 WHERE id = %s", (comment_id,))
            else:
                cursor.execute("UPDATE Comments SET downvotes = COALESCE(downvotes, 0) + 1 WHERE id = %s",
                               (comment_id,))

        return {
            "status": "True",
            "message": "Голос учтен"
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.post("/community/subscribe/{category_id}")
async def subscribe(category_id: int, request: SubscriptionRequest):
    try:
        user_id = verify_user(request.login, request.password)
        if not user_id:
            return {
                "status": "False",
                "message": "Неверные учетные данные"
            }

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT id FROM Categories WHERE id = %s", (category_id,))
        category = cursor.fetchone()

        if not category:
            return {
                "status": "False",
                "message": "Категория не найдена"
            }

        return {
            "status": "True",
            "message": "Подписка оформлена"
        }

    except Exception as e:
        print(f"Ошибка базы данных: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка сервера: {str(e)}")


@app.get("/community/posts/{post_id}/comments")
async def get_comments(post_id: int, page: int = 1, limit: int = 50):
    try:
        offset = (page - 1) * limit

        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT id FROM Posts WHERE id = %s", (post_id,))
        post = cursor.fetchone()

        if not post:
            return {
                "status": "False",
                "message": "Пост не найден"
            }

        cursor.execute("""
            SELECT COUNT(*) FROM Comments 
            WHERE post_id = %s
        """, (post_id,))
        total_comments = cursor.fetchone()[0]
        total_pages = (total_comments + limit - 1) // limit if total_comments > 0 else 0

        cursor.execute("""
            SELECT c.id, c.content, c.user_id, c.parent_comment_id, 
                   c.created_at, c.upvotes, c.downvotes,
                   u.login as author_name
            FROM Comments c
            JOIN Users u ON c.user_id = u.id_user
            WHERE c.post_id = %s
            ORDER BY c.created_at ASC
            LIMIT %s OFFSET %s
        """, (post_id, limit, offset))

        comments = cursor.fetchall()

        comments_dict = {}
        root_comments = []

        for comment in comments:
            comment_id = comment[0]
            comments_dict[comment_id] = {
                "id": comment[0],
                "content": comment[1],
                "user_id": comment[2],
                "parent_comment_id": comment[3],
                "created_at": comment[4].strftime("%Y-%m-%d %H:%M:%S") if comment[4] else None,
                "upvotes": comment[5] if comment[5] else 0,
                "downvotes": comment[6] if comment[6] else 0,
                "author_name": comment[7] if comment[7] else "Неизвестный",
                "replies": []
            }

        for comment_id, comment_data in comments_dict.items():
            parent_id = comment_data["parent_comment_id"]
            if parent_id is None:
                root_comments.append(comment_data)
            elif parent_id in comments_dict:
                comments_dict[parent_id]["replies"].append(comment_data)

        return {
            "status": "True",
            "message": f"Найдено {total_comments} комментариев",
            "comments": root_comments,
            "pagination": {
                "page": page,
                "limit": limit,
                "total_comments": total_comments,
                "total_pages": total_pages
            }
        }

    except Exception as e:
        print(f"Ошибка базы данных при получении комментариев: {e}")
        return {
            "status": "False",
            "message": f"Ошибка сервера: {str(e)}",
            "comments": [],
            "pagination": {
                "page": page,
                "limit": limit,
                "total_comments": 0,
                "total_pages": 0
            }
        }


class ProfileUpdate(BaseModel):
    login: str
    password: str
    new_password: Optional[str] = None
    city_name: Optional[str] = None
    date_birth: Optional[str] = None
    time_birth: Optional[str] = None


@app.post("/user/profile")
async def get_user_profile(data: UserData):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        print(f"Запрос профиля для пользователя: {data.Name}")

        cursor.execute("""
            SELECT 
                u.id_user, 
                u.login, 
                u.pass, 
                DATE_FORMAT(u.date_birth, '%%Y-%%m-%%d') as date_birth,
                TIME_FORMAT(u.time_birth, '%%H:%%i:%%s') as time_birth,
                u.id_city,
                c.name as city_name
            FROM Users u
            LEFT JOIN Cities c ON u.id_city = c.id_city
            WHERE u.login = %s AND u.pass = %s
        """, (data.Name, data.Password))

        result = cursor.fetchone()

        if result:
            print(f"Найден пользователь: {result[1]}")

            city_data = None
            if result[5] and result[6]:
                city_data = {
                    "id": result[5],
                    "name": result[6]
                }

            return {
                "status": "True",
                "message": "Данные профиля получены",
                "profile": {
                    "id_user": result[0],
                    "login": result[1],
                    "date_birth": result[3] if result[3] else "",
                    "time_birth": result[4] if result[4] else "",
                    "city": city_data
                }
            }
        else:
            print(f"Пользователь не найден: {data.Name}")
            return {
                "status": "False",
                "message": "Пользователь не найден"
            }

    except Exception as e:
        print(f"Ошибка базы данных при получении профиля: {e}")
        return {
            "status": "False",
            "message": f"Ошибка сервера: {str(e)}"
        }


@app.post("/user/profile/update")
async def update_user_profile(data: ProfileUpdate):
    try:
        conn = get_connection()
        cursor = conn.cursor()

        print(f"Обновление профиля для пользователя: {data.login}")

        # Проверяем существование пользователя
        cursor.execute("SELECT id_user FROM Users WHERE login = %s AND pass = %s",
                       (data.login, data.password))
        user = cursor.fetchone()

        if not user:
            return {
                "status": "False",
                "message": "Неверные учетные данные"
            }

        user_id = user[0]

        # Собираем обновления
        updates = {}

        if data.new_password:
            updates['pass'] = data.new_password

        if data.city_name:
            cursor.execute("SELECT id_city FROM Cities WHERE name = %s", (data.city_name,))
            city = cursor.fetchone()
            if city:
                updates['id_city'] = city[0]
            else:
                return {
                    "status": "False",
                    "message": f"Город '{data.city_name}' не найден"
                }

        if data.date_birth:
            updates['date_birth'] = data.date_birth

        if data.time_birth:
            updates['time_birth'] = data.time_birth

        if updates:
            # Формируем SQL запрос
            set_clause = ", ".join([f"`{key}` = %s" for key in updates.keys()])
            sql = f"UPDATE Users SET {set_clause} WHERE id_user = %s"

            params = list(updates.values())
            params.append(user_id)

            cursor.execute(sql, params)
            conn.commit()

            print(f"Профиль обновлен для user_id: {user_id}")
            return {
                "status": "True",
                "message": "Профиль успешно обновлен"
            }
        else:
            return {
                "status": "False",
                "message": "Нет данных для обновления"
            }

    except Exception as e:
        print(f"Ошибка базы данных при обновлении профиля: {e}")
        return {
            "status": "False",
            "message": f"Ошибка сервера: {str(e)}"
        }


@app.get("/cities")
async def get_cities():
    try:
        conn = get_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT id_city, name FROM Cities ORDER BY name")
        cities = cursor.fetchall()

        cities_list = []
        for city in cities:
            cities_list.append({
                "id": city[0],
                "name": city[1]
            })

        print(f"Отправлено городов: {len(cities_list)}")
        return {
            "status": "True",
            "message": f"Найдено {len(cities_list)} городов",
            "cities": cities_list
        }

    except Exception as e:
        print(f"Ошибка базы данных при получении городов: {e}")
        return {
            "status": "False",
            "message": f"Ошибка сервера: {str(e)}"
        }

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)