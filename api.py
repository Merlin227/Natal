import requests
import aiohttp
import asyncio
from googletrans import Translator
from deep_translator import GoogleTranslator

async def get_horoscope_async(session, sign):
    url = f"https://horoscope-app-api.vercel.app/api/v1/get-horoscope/daily?sign={sign}&day=TODAY"
    try:
        async with session.get(url) as response:
            if response.status == 200:
                return await response.json()
            else:
                return None
    except Exception as e:
        print(f"Ошибка для {sign}: {e}")
        return None


async def get_all_horoscopes_async():
    zodiac_signs = ['aries', 'taurus', 'gemini', 'cancer', 'leo', 'virgo',
                    'libra', 'scorpio', 'sagittarius', 'capricorn', 'aquarius', 'pisces']

    async with aiohttp.ClientSession() as session:
        tasks = [get_horoscope_async(session, sign) for sign in zodiac_signs]
        results = await asyncio.gather(*tasks)

        return dict(zip(zodiac_signs, results))


async def translate_text_async(text):
    try:
        loop = asyncio.get_event_loop()
        translated = await loop.run_in_executor(
            None,
            GoogleTranslator(source='en', target='ru').translate,
            text
        )
        return translated
    except Exception as e:
        print(f"Ошибка перевода: {e}")
        return text


async def translate_all_texts_async(description):

    zodiac_signs = ['aries', 'taurus', 'gemini', 'cancer', 'leo', 'virgo',
                    'libra', 'scorpio', 'sagittarius', 'capricorn', 'aquarius', 'pisces']

    texts_to_translate = []
    for sign in zodiac_signs:
        try:
            text = description[sign]['data']['horoscope_data']
            texts_to_translate.append(text)
        except (KeyError, TypeError):
            texts_to_translate.append("Horoscope not available")

    tasks = [translate_text_async(text) for text in texts_to_translate]
    translated_texts = await asyncio.gather(*tasks)

    return dict(zip(zodiac_signs, translated_texts))



# Использование
# horoscopes = asyncio.run(get_all_horoscopes_async())
# print(horoscopes['gemini']['data']['horoscope_data'])
# signs = ['Aries', 'Taurus', 'Gemini', 'Cancer', 'Leo', 'Virgo', 'Libra', 'Scorpio', 'Sagittarius', 'Capricorn', 'Aquarius', 'Pisces']
#
# result = get_with_params(
#     f"https://horoscope-app-api.vercel.app/api/v1/get-horoscope/daily?sign=Aries&day=TODAY"
# )
#
# if result:
#     print("Успешный ответ:")
#     print(result['data']['horoscope_data'])




