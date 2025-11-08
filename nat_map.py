import ephem
from datetime import datetime
import math
import pymysql as db
con = db.connect(host='localhost', user='root', password='', database='zachet', autocommit=True)

class AstrologyCalculator:
    def __init__(self):



        self.planets_ru = {
            'sun': 'Солнце',
            'moon': 'Луна',
            'mercury': 'Меркурий',
            'venus': 'Венера',
            'mars': 'Марс',
            'jupiter': 'Юпитер',
            'saturn': 'Сатурн',
            'uranus': 'Уран',
            'neptune': 'Нептун',
            'pluto': 'Плутон'
        }

        self.signs_ru = {
            0: 'Овен', 30: 'Телец', 60: 'Близнецы', 90: 'Рак',
            120: 'Лев', 150: 'Дева', 180: 'Весы', 210: 'Скорпион',
            240: 'Стрелец', 270: 'Козерог', 300: 'Водолей', 330: 'Рыбы'
        }

        self.houses_ru = {
            1: '1-й дом',
            2: '2-й дом',
            3: '3-й дом',
            4: '4-й дом',
            5: '5-й дом',
            6: '6-й дом',
            7: '7-й дом',
            8: '8-й дом',
            9: '9-й дом',
            10: '10-й дом',
            11: '11-й дом',
            12: '12-й дом'
        }

    def get_zodiac_sign(self, longitude):
        for start_degree, sign in self.signs_ru.items():
            if start_degree <= longitude < start_degree + 30:
                return sign
        return self.signs_ru[0]

    def calculate_house(self, longitude, ascendant):
        house_longitude = (longitude - ascendant + 360) % 360
        house_number = int(house_longitude / 30) + 1
        return house_number if house_number <= 12 else 1

    def calculate_ascendant(self, date_time, lat, lon):
        obs = ephem.Observer()
        obs.date = date_time
        obs.lat = str(lat)
        obs.lon = str(lon)

        sun = ephem.Sun(obs)
        ascendant_approx = (sun.ra + math.pi) % (2 * math.pi)
        return math.degrees(ascendant_approx)

    def get_planet_position(self, planet_name, date_time, obs):
        match planet_name:
            case 'sun':
                planet = ephem.Sun(obs)
            case 'moon':
                planet = ephem.Moon(obs)
            case 'mercury':
                planet = ephem.Mercury(obs)
            case 'venus':
                planet = ephem.Venus(obs)
            case 'mars':
                planet = ephem.Mars(obs)
            case 'jupiter':
                planet = ephem.Jupiter(obs)
            case 'saturn':
                planet = ephem.Saturn(obs)
            case 'uranus':
                planet = ephem.Uranus(obs)
            case 'neptune':
                planet = ephem.Neptune(obs)
            case 'pluto':
                planet = ephem.Pluto(obs)
            case _:
                return None

        planet.compute(obs)
        longitude = math.degrees(planet.hlong) % 360
        return longitude

    def calculate_astrology(self, birth_date, birth_time, latitude, longitude):

        obs = ephem.Observer()

        datetime_str = f"{birth_date} {birth_time}"
        obs.date = datetime_str
        obs.lat = str(latitude)
        obs.lon = str(longitude)

        ascendant = self.calculate_ascendant(datetime_str, latitude, longitude)

        results = {
            'planets_in_signs': [],
            'planets_in_houses': [],
            'ascendant': self.get_zodiac_sign(ascendant)
        }

        for planet_en, planet_ru in self.planets_ru.items():
            try:
                planet_longitude = self.get_planet_position(planet_en, datetime_str, obs)
                if planet_longitude is not None:
                    sign = self.get_zodiac_sign(planet_longitude)
                    house = self.calculate_house(planet_longitude, ascendant)
                    print(sign, house, end='\n')
                    cursor = con.cursor()
                    cursor.execute(
                        "SELECT interpretation from Planet_sign "
                        "join Planets on Planet_sign.id_planet = Planets.id_planet "
                        "join Signs on Signs.id_sign = Planet_sign.sign_id "
                        "where Signs.name = %s and Planets.planet_name = %s;",
                        (f'{sign}', f'{planet_ru}'))
                    #Сгенерировать данные в бд для Planet_sign


                    results['planets_in_signs'].append({
                        'planet': planet_ru,
                        'sign': sign,
                        'longitude': round(planet_longitude, 2),
                        'interpretation' : cursor.fetchall()
                    })

                    results['planets_in_houses'].append({
                        'planet': planet_ru,
                        'house': self.houses_ru[house],
                        'house_number': house
                    })
            except Exception as e:
                print(f"Ошибка при расчёте {planet_ru}: {e}")

        results['planets_in_houses'].sort(key=lambda x: x['house_number'])

        return results

    def print_results(self, results):

        for planet_info in results['planets_in_signs']:
            print(f"{planet_info['planet']:8} : {planet_info['sign']:10}")

        print("\n")

        for planet_info in results['planets_in_houses']:
            print(f"{planet_info['planet']:12} : {planet_info['house']}")


def main():
    calculator = AstrologyCalculator()

    print("Введите данные рождения:")

    try:

        birth_date = input("Дата рождения (ГГГГ-ММ-ДД): ")
        birth_time = input("Время рождения (ЧЧ:ММ:СС): ")
        latitude = float(input("Широта города рождения (например, 55.7558 для Москвы): "))
        longitude = float(input("Долгота города рождения (например, 37.6173 для Москвы): "))
        datetime.strptime(birth_date + ' ' + birth_time, '%Y-%m-%d %H:%M:%S')

        results = calculator.calculate_astrology(birth_date, birth_time, latitude, longitude)

        #print(results)

    except ValueError as e:
        print(f"Ошибка ввода данных: {e}")
    except Exception as e:
        print(f"Произошла ошибка: {e}")


if __name__ == "__main__":
    main()