# Kercher Visual (Fabric 1.21.4)

Клиентский визуал-мод:
- Watermark "Kercher" с бегущим градиентом + FPS / пинг / координаты
- Keystrokes (WASD + Space) с подсветкой цветом темы
- Hit Ring: кольцо расходится от прицела при ударе по мобу/игроку
- Красная мигающая виньетка при низком HP

Клавиши (меняются в настройках управления):
- Right Shift — вкл/выкл
- Right Alt — следующая тема (Kercher / Ocean / Toxic / Sunset / Rainbow)

Сборка: `./gradlew build` -> build/libs/kercher-1.0.0.jar (Java 21).
Если нет gradle wrapper: скопируй папку `gradle/`, `gradlew`, `gradlew.bat` из
https://github.com/FabricMC/fabric-example-mod (ветка 1.21.4) в корень проекта.

## Сборка без установки Java (через GitHub)
1. Создай репозиторий на github.com и загрузи в него всё содержимое этой папки (включая `.github`).
2. Открой вкладку Actions -> build -> дождись зелёной галочки (1-3 мин).
3. Внизу страницы запуска скачай артефакт `kercher-visual-jar`, внутри будет готовый jar.
4. Положи jar в папку mods вместе с Fabric API для 1.21.4.
