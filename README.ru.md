# Create: SoftMotion

**[English](README.md)** | **Русский**

[![Modrinth](https://img.shields.io/badge/Modrinth-createsoftmotion-blue)](https://modrinth.com/mod/createsoftmotion)
![MC](https://img.shields.io/badge/Minecraft-1.21.1-green) ![loader](https://img.shields.io/badge/NeoForge-21.1.230%2B-orange)

Клиентский аддон доступности для [Create](https://modrinth.com/mod/create): замедляет **визуальное**
вращение механизмов и конструкций Create для игроков со зрительной гиперчувствительностью.

- Замедляется только отрисовка вращения: валы и прочие вращающиеся инстансы (`RotatingInstance`),
  Mechanical Bearing (`getInterpolatedAngle`) и сущности конструкций на вращателе (`setAngle`).
- **Не меняет** кинетику Create: RPM, нагрузку (stress), кинетические сети, поведение конструкций,
  скорость обработки предметов и вообще игровую логику — меняется только картинка.
- Только клиент: работает в одиночной игре и на серверах, на выделенном сервере не нужен.

## Настройки — `config/softmotion-client.toml`

| Опция | По умолчанию | Значение |
|---|---|---|
| `visualSpeedMultiplier` | `0.25` | `0.0` — визуальное вращение заморожено, `0.25` — в четыре раза медленнее, `1.0` — обычные визуальные скорости Create |
| `slowBearingContraptions` | `true` | замедлять также сущности конструкций, собранных на механическом вращателе |

Экран настроек открывается из списка модов (через `IConfigScreenFactory` NeoForge). Если NeoForge не
предоставляет `ConfigurationScreen`, мод продолжает работать с TOML-файлом.

## Сборка

Нужен JDK 21+ (Gradle сам подтянет toolchain) и доступ в интернет при первой сборке — скачиваются
ассеты NeoForge userdev.

Сначала скачайте dev-джары Create в локальное зеркало `libs-maven/`:

```bash
./fetch-libs.sh
```

Джары берутся с CDN Modrinth, потому что `maven.createmod.net` сейчас отдаёт просроченный TLS-сертификат;
это те же официальные файлы Create 6.0.10, включая Flywheel.

```bash
./gradlew build      # результат: build/libs/Create-SoftMotion-1.0.0.jar
./gradlew runClient  # dev-клиент с предустановленным Create 6.0.10
```

## Совместимость

| | |
|---|---|
| Minecraft | 1.21.1 (`[1.21.1, 1.21.2)`) |
| Лоадер | NeoForge 21.1.230+ |
| Create | 6.0.10+ (обязателен, объявлен как `side = CLIENT`) |
| Сторона | только клиент (`client_side: required`, `server_side: unsupported`) |

Все инъекции идут с `require = 0`, а в конфиге миксинов стоит `defaultRequire = 0`: если будущая сборка
Create переименует цель, мод не упадёт при запуске — просто эта инъекция не применится. Совместимость с
другими версиями Minecraft и Create не гарантируется.

## Как это работает

`visualSpeedMultiplier` применяется только к визуальному углу / скорости вращения:

- `RotatingInstance.setRotationalSpeed` — масштабируется скорость рендер-инстанса Flywheel для валов,
  шестерней и прочих кинетических блоков, а block entity сохраняет реальные обороты;
- `MechanicalBearingBlockEntity.getInterpolatedAngle` — возвращаемый угол заменяется локально
  просчитанным *визуальным* углом, который следует за реальным в заданном отношении (интегрирование по
  кратчайшей разнице, поэтому переход через 360° не даёт скачка);
- `ControlledContraptionEntity.setAngle` — то же состояние визуального угла для сущностей конструкций;
  пропускается, если `slowBearingContraptions` выключен или множитель равен `1.0`.

Состояния визуальных углов хранятся в `WeakHashMap`, поэтому block entity и сущности не утекают в памяти.

## Modrinth

https://modrinth.com/mod/createsoftmotion

## Лицензия

MIT
