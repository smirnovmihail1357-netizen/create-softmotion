# Create: SoftMotion

**English** | **[Русский](README.ru.md)**

[![Modrinth](https://img.shields.io/badge/Modrinth-createsoftmotion-blue)](https://modrinth.com/mod/createsoftmotion)
![MC](https://img.shields.io/badge/Minecraft-1.21.1-green) ![loader](https://img.shields.io/badge/NeoForge-21.1.230%2B-orange)

Client-side accessibility addon for [Create](https://modrinth.com/mod/create) that slows down the
**visual** rotation of Create machinery and contraptions, for players with visual hypersensitivity.

- Slows only the rendering of rotation: shafts and other rotating instances (`RotatingInstance`),
  Mechanical Bearings (`getInterpolatedAngle`) and bearing-mounted contraption entities (`setAngle`).
- Does **not** change Create kinetics: RPM, stress, kinetic networks, contraption behaviour, processing
  speed or any gameplay logic — only the picture.
- Client-only: works in singleplayer and on servers, and is not needed on a dedicated server.

## Config — `config/softmotion-client.toml`

| Option | Default | Description |
|---|---|---|
| `visualSpeedMultiplier` | `0.25` | `0.0` = visually frozen, `0.25` = four times slower, `1.0` = vanilla Create visuals |
| `slowBearingContraptions` | `true` | also slow assembled contraption entities attached to Mechanical Bearings |

A native config screen is registered through NeoForge's `IConfigScreenFactory` (opened from the mod list).
If NeoForge does not provide `ConfigurationScreen`, the mod still works through the TOML file.

## Build

Requires JDK 21+ (Gradle provisions a toolchain automatically) and internet access on the first build
(downloads NeoForge userdev assets).

First, fetch the Create dev jars into the local mirror `libs-maven/`:

```bash
./fetch-libs.sh
```

The jars are taken from the Modrinth CDN because `maven.createmod.net` currently serves an expired TLS
certificate; they are the same official Create 6.0.10 release files, including Flywheel.

```bash
./gradlew build      # artifact: build/libs/Create-SoftMotion-1.0.0.jar
./gradlew runClient  # dev client with Create 6.0.10 preinstalled
```

## Compatibility

| | |
|---|---|
| Minecraft | 1.21.1 (`[1.21.1, 1.21.2)`) |
| Loader | NeoForge 21.1.230+ |
| Create | 6.0.10+ (required, declared with `side = CLIENT`) |
| Side | client only (`client_side: required`, `server_side: unsupported`) |

All injections are `require = 0` and the mixin config sets `defaultRequire = 0`, so if a future Create
build renames a target the mod will not crash on startup — that injection simply will not apply.
Compatibility with other Minecraft or Create versions is not guaranteed.

## How it works

`visualSpeedMultiplier` is applied to the visual angle/rotational speed only:

- `RotatingInstance.setRotationalSpeed` — the Flywheel render instance for shafts, cogwheels and other
  kinetic blocks gets a scaled speed, while the block entity keeps its real RPM;
- `MechanicalBearingBlockEntity.getInterpolatedAngle` — the returned angle is replaced by a locally
  integrated *visual* angle that follows the real angle at the configured ratio (shortest-delta
  integration, so wrapping never produces a jump);
- `ControlledContraptionEntity.setAngle` — the same visual-angle state for contraption entities, skipped
  when `slowBearingContraptions` is off or the multiplier is `1.0`.

Visual angle states are kept in `WeakHashMap`s, so they do not leak block entities or entities.

## Modrinth

https://modrinth.com/mod/createsoftmotion

## License

MIT
