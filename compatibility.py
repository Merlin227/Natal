from datetime import datetime


def calculate_life_path_number(birth_date):

    day = birth_date.day
    month = birth_date.month
    year = birth_date.year


    total = 0
    for number in [day, month, year]:
        total += sum(int(d) for d in str(number))


    while total > 9:
        total = sum(int(d) for d in str(total))

    return total


def numerology_compatibility(date1, date2):
    num1 = calculate_life_path_number(date1)
    num2 = calculate_life_path_number(date2)

    difference = abs(num1 - num2)

    match difference:
        case 0:
            return 100, "Идеальное сочетание энергий. Ваши вибрации полностью синхронизированы, создавая гармоничный союз."
        case 1:
            return 90, "Отличная совместимость. Вы дополняете друг друга, сохраняя индивидуальность и взаимное уважение."
        case 2:
            return 80, "Хорошая совместимость. Ваши энергии хорошо сочетаются, создавая прочную и стабильную связь."
        case 3:
            return 70, "Умеренная совместимость. Есть взаимопонимание, но потребуется работа над гармонизацией отношений."
        case 4:
            return 60, "Средняя совместимость. Вы учитесь друг у друга, но различия требуют компромиссов с обеих сторон."
        case 5:
            return 50, "Нейтральная совместимость. Есть точки соприкосновения, но много различий в подходах к жизни."
        case 6:
            return 40, "Слабая совместимость. Значительные различия создают сложности в построении гармоничных отношений."
        case 7:
            return 30, "Низкая совместимость. Разные жизненные ритмы и ценности требуют больших усилий для взаимопонимания."
        case 8:
            return 20, "Очень низкая совместимость. Практически отсутствует взаимопонимание, отношения будут постоянно испытываться на прочность."
        case _:
            return 10, "Крайне низкая гармония. Очень сложно построить что-то общее из-за противоположных энергетических вибраций."


date1 = datetime(2000, 1, 6)
date2 = datetime(1999, 9, 9)
print(numerology_compatibility(date1, date2))
