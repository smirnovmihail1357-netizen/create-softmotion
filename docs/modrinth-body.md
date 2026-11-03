**🇬🇧 English** · **🇷🇺 Русский**

Full docs: [README.md](https://github.com/smirnovmihail1357-netizen/create-softmotion/blob/main/README.md) (English) · [README.ru.md](https://github.com/smirnovmihail1357-netizen/create-softmotion/blob/main/README.ru.md) (Русский)

**Create: SoftMotion** is a client-side accessibility addon for **Create**, designed for players with visual hypersensitivity or anyone who finds rapidly rotating machinery uncomfortable to look at.

The mod reduces the **visual** rotation speed of Create machinery and assembled contraptions without changing their actual mechanical behaviour.

Open source (MIT): [https://github.com/smirnovmihail1357-netizen/create-softmotion](https://github.com/smirnovmihail1357-netizen/create-softmotion)

### Why I made this mod

The high rotation speed of Create contraptions bothers me. I use this mod myself while playing, so I decided to share it with other players who may find it useful.

### What the mod does

The mod applies client-side visual changes to rotating Create components and contraptions:

* reduces the visual rotation speed of shafts, cogwheels and other rotating instances;
* slows down other rotating components rendered through Create and Flywheel;
* reduces the visual rotation speed of Mechanical Bearings;
* can optionally slow down assembled contraptions attached to Mechanical Bearings;
* lets you tune the visual rotation multiplier in the config.

### What it does not change

* the actual RPM of any mechanism;
* stress calculations and kinetic networks;
* machine processing speed and contraption behaviour;
* any server-side gameplay mechanic.

The multiplier only affects how movement is displayed on the client. Create machinery keeps running at its normal mechanical speed, so the mod gives no gameplay advantage and is purely an accessibility feature.

### Configuration — `config/softmotion-client.toml`

| Option | Default | Meaning |
|---|---|---|
| `visualSpeedMultiplier` | `0.25` | `0.0` = visually frozen, `0.25` = four times slower, `1.0` = vanilla Create visuals |
| `slowBearingContraptions` | `true` | also slow the assembled contraption entities attached to Mechanical Bearings |

Any value between `0.0` and `1.0` can be used for fine tuning. The config screen opens from the mod list; if NeoForge does not provide its `ConfigurationScreen`, the TOML file still works.

Labels are localized in English and Russian.

### How it works

* `RotatingInstance.setRotationalSpeed` — the Flywheel render instance of shafts, cogwheels and other kinetic blocks gets a scaled speed, while the block entity keeps its real RPM.
* `MechanicalBearingBlockEntity.getInterpolatedAngle` — the returned angle is replaced by a locally integrated **visual** angle that follows the real angle at the configured ratio (shortest-delta integration, so wrapping past 360° never causes a jump).
* `ControlledContraptionEntity.setAngle` — the same visual-angle state for contraption entities; skipped when `slowBearingContraptions` is off or the multiplier is `1.0`.

Visual angle states are stored in `WeakHashMap`s, so block entities and entities are not leaked in memory. All injections use `require = 0` (`defaultRequire = 0` in the mixin config): if a future Create build renames a target, the mod will not crash on startup — that injection simply will not apply.

### Installation

The mod only needs to be installed on the **client**. It is not needed on a dedicated server because it does not modify server-side mechanics, kinetic networks, RPM or contraption behaviour.

Required: **NeoForge** and **Create**.

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.230 or newer |
| Create | 6.0.10 or newer (required, client-side) |

Compatibility with other Minecraft or Create versions is not guaranteed.

### Source code and license

[https://github.com/smirnovmihail1357-netizen/create-softmotion](https://github.com/smirnovmihail1357-netizen/create-softmotion) — MIT, build with `./fetch-libs.sh` then `./gradlew build` (artifact `build/libs/Create-SoftMotion-1.0.0.jar`).

---

## Русский

**Create: SoftMotion** — это клиентский аддон доступности для **Create**, предназначенный для игроков со зрительной гиперчувствительностью, а также для тех, кому некомфортно смотреть на быстро вращающиеся механизмы.

Мод уменьшает **визуальную** скорость вращения механизмов и собранных конструкций Create, не изменяя их реальную механическую работу.

Открытый код (MIT): [https://github.com/smirnovmihail1357-netizen/create-softmotion](https://github.com/smirnovmihail1357-netizen/create-softmotion)

### Почему я создал этот мод

Меня раздражает большая скорость вращения конструкций Create. Я сам использую этот мод во время игры, поэтому решил поделиться им с другими игроками, которым он также может быть полезен.

### Что делает мод

Мод применяет клиентские визуальные изменения к вращающимся механизмам и конструкциям Create:

* уменьшает визуальную скорость вращения валов, шестерней и других вращающихся инстансов;
* замедляет другие вращающиеся компоненты, отрисовываемые через Create и Flywheel;
* уменьшает визуальную скорость вращения механических вращателей;
* может опционально замедлять собранные конструкции, прикреплённые к механическим вращателям;
* позволяет настраивать множитель визуальной скорости в конфиге.

### Что мод не меняет

* реальные обороты (RPM) любого механизма;
* расчёты нагрузки (stress) и кинетические сети;
* скорость обработки предметов механизмами и поведение конструкций;
* любую серверную игровую механику.

Множитель влияет только на то, как движение отображается на клиенте. Механизмы Create продолжают работать с обычной механической скоростью, поэтому мод не даёт игрового преимущества и является именно функцией доступности.

### Настройки — `config/softmotion-client.toml`

| Опция | По умолчанию | Значение |
|---|---|---|
| `visualSpeedMultiplier` | `0.25` | `0.0` — визуальное вращение заморожено, `0.25` — в четыре раза медленнее, `1.0` — обычные визуальные скорости Create |
| `slowBearingContraptions` | `true` | замедлять также сущности конструкций на механическом вращателе |

Можно использовать любое промежуточное значение от `0.0` до `1.0`. Экран настроек открывается из списка модов; если NeoForge не предоставляет `ConfigurationScreen`, работает обычный TOML-файл.

Подписи локализованы на английском и русском.

### Как это работает

* `RotatingInstance.setRotationalSpeed` — масштабируется скорость рендер-инстанса Flywheel для валов, шестерней и других кинетических блоков, а block entity сохраняет реальные обороты.
* `MechanicalBearingBlockEntity.getInterpolatedAngle` — возвращаемый угол заменяется локально просчитанным **визуальным** углом, который следует за реальным в заданном отношении (интегрирование по кратчайшей разнице, поэтому переход через 360° не даёт скачка).
* `ControlledContraptionEntity.setAngle` — то же состояние визуального угла для сущностей конструкций; пропускается, если `slowBearingContraptions` выключен или множитель равен `1.0`.

Состояния визуальных углов хранятся в `WeakHashMap`, поэтому block entity и сущности не утекают в памяти. Все инъекции идут с `require = 0` (`defaultRequire = 0` в конфиге миксинов): если будущая сборка Create переименует цель, мод не упадёт при запуске — просто эта инъекция не применится.

### Установка

Мод нужно ставить только на **клиент**. На выделенный сервер его ставить не требуется, потому что он не изменяет серверную механику, кинетические сети, реальные обороты или поведение конструкций.

Необходимы: **NeoForge** и **Create**.

| | |
|---|---|
| Minecraft | 1.21.1 |
| Лоадер | NeoForge 21.1.230 и выше |
| Create | 6.0.10 и выше (обязателен, на стороне клиента) |

Совместимость с другими версиями Minecraft и Create не гарантируется.

### Исходники и лицензия

[https://github.com/smirnovmihail1357-netizen/create-softmotion](https://github.com/smirnovmihail1357-netizen/create-softmotion) — MIT, сборка: `./fetch-libs.sh`, затем `./gradlew build` (результат — `build/libs/Create-SoftMotion-1.0.0.jar`).
