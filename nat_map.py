import swisseph as swe
from datetime import datetime
import pytz


# # Простейший расчет
# def simple_natal_chart():
#     # Настройка
#     swe.set_ephe_path(None)  # используем встроенные эфемериды
#
#     # Данные рождения
#     birth_date = datetime(1990, 5, 15, 14, 30, 0)
#     birth_date = pytz.timezone('Europe/Moscow').localize(birth_date)
#
#     # Юлианская дата
#     jd = swe.julday(birth_date.year, birth_date.month, birth_date.day,
#                     birth_date.hour + birth_date.minute / 60.0)
#
#     # Основные планеты
#     planets = ['Sun', 'Moon', 'Mercury', 'Venus', 'Mars']
#
#     print("Натальная карта:")
#     for i, planet in enumerate(planets):
#         pos = swe.calc_ut(jd, i)
#         longitude = pos[0][0]
#         sign_num = int(longitude / 30)
#         signs = ["Овен", "Телец", "Близнецы", "Рак", "Лев", "Дева",
#                  "Весы", "Скорпион", "Стрелец", "Козерог", "Водолей", "Рыбы"]
#
#         print(f"{planet}: {longitude:.1f}° - {signs[sign_num]}")
#
#
# # Запуск
# simple_natal_chart()

from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


class SynastryRequest(BaseModel):
    chart_id_1: str
    chart_id_2: str


@router.post("/api/synastry/calculate")
async def calculate_synastry(request: SynastryRequest):
    # Получаем карты из БД
    chart1 = await get_chart_from_db(request.chart_id_1)
    chart2 = await get_chart_from_db(request.chart_id_2)

    # Расчет совместимости
    calculator = SynastryCalculator(chart1, chart2)
    aspects = calculator.calculate_aspects()
    score = calculator.calculate_compatibility_score()

    # Анализ ключевых аспектов
    analysis = analyze_key_aspects(aspects)

    return {
        "compatibility_score": round(score),
        "aspects": aspects,
        "analysis": analysis,
        "strengths": get_relationship_strengths(aspects),
        "challenges": get_relationship_challenges(aspects)
    }


def analyze_key_aspects(aspects):
    """Анализ самых важных аспектов для отношений"""
    key_aspects = []
    important_combinations = [
        ('sun', 'moon'), ('venus', 'mars'),
        ('moon', 'moon'), ('venus', 'venus')
    ]

    for aspect in aspects:
        planet_pair = (aspect['planet1'], aspect['planet2'])
        if planet_pair in important_combinations:
            key_aspects.append(aspect)

    return key_aspects