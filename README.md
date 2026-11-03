# Create: SoftMotion

[English](#english) | [Русский](#русский)

## English

Client-side accessibility addon for [Create](https://modrinth.com/mod/create-1-20-1) that slows down the
**visual** rotation of Create machinery and contraptions for players with visual hypersensitivity.

- Slows rendered rotation of rotating shafts/instances (`RotatingInstance`), Mechanical Bearings
  (`getInterpolatedAngle`) and bearing-mounted contraption entities (`setAngle`).
- Does **not** change Create kinetic speed, stress, RPM, kinetic networks or any gameplay logic.
- Client-only. Works in singleplayer and on servers as a pure client mod.

### Config (`config/softmotion-client.toml`)

| Option | Default | Description |
|---|---|---|
| `visualSpeedMultiplier` | `0.25` | `0.0` = visually frozen, `1.0` = vanilla Create visuals |
| `slowBearingContraptions` | `true` | Also slow visually contraption entities on Mechanical Bearings |

### Build

Requires JDK 21+ (Gradle provisions a toolchain automatically) and internet access on the first build
(downloads NeoForge userdev assets).

```bash
./gradlew build      # jar: build/libs/Create-SoftMotion-1.0.0.jar
./gradlew runClient  # dev client with Create 6.0.10 preinstalled
```

### Compatibility

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.230+ |
| Create | 6.0.10+ (required, client-side) |
| Side | Client only |

## Русский

Клиентский аддон доступности для Create: замедляет **визуальное** вращение механизмов и конструкций
для игроков с визуальной гиперчувствительностью.

- Замедляет только отрисовку вращения: валы/инстансы, Mechanical Bearing, конструкции на вращателе.
- **Не меняет** кинетику Create: RPM, stress, сети и игровую логику — только картинку.
- Клиентский мод: работает и в одиночной игре, и на серверах.

Настройки — `config/softmotion-client.toml` (множитель визуальной скорости 0.0–1.0, по умолчанию 0.25).

Сборка: `./gradlew build`, готовый jar в `build/libs/`.

## Modrinth

https://modrinth.com/mod/createsoftmotion

## License

MIT
