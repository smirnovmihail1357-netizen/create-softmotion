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
| `visualSpeedMultiplier` | `0.25` | `0.0` = visually frozen, `0.25` = four times slower, `1.0` = vanilla Create visuals. Used when the formula is empty, and as a fallback if the formula is invalid |
| `visualSpeedFormula` | `""` | optional formula evaluated per element; overrides `visualSpeedMultiplier` when non-empty |
| `slowBearingContraptions` | `true` | also slow assembled contraption entities attached to Mechanical Bearings |

A native config screen is registered through NeoForge's `IConfigScreenFactory` (opened from the mod list).
If NeoForge does not provide `ConfigurationScreen`, the mod still works through the TOML file.

### Formula syntax

The formula returns a **multiplier of the real speed**, clamped to `0.0..1.0`. It is evaluated for
each rotating element, so the slowdown can depend on how fast that element is actually spinning.

Variables:

| Name | Meaning |
|---|---|
| `rpm` | real absolute speed of the element in Create units (Create's RPM-like speed value) |
| `speed` | `rpm * 6` — Flywheel's rotational-speed unit (degrees per second) |

Operators: `+ - * / % ^` (`^` is power, right-associative), comparisons (`< <= > >= == !=`),
logical `&& || !`, and ternary `condition ? a : b`. Constants: `pi`, `e`.
Functions: `abs`, `min`, `max`, `clamp`, `sqrt`, `sign`, `floor`, `ceil`, `round`, `pow`, `log`,
`exp`, `sin`, `cos`, `tan`, `if(condition, a, b)`.

Examples:

```toml
visualSpeedFormula = "min(1, 8 / (rpm + 0.001))"        # faster machines slowed down more
visualSpeedFormula = "clamp(0.25 + rpm / 200, 0.1, 1)"  # gentle speed-dependent slowdown
visualSpeedFormula = "if(rpm > 100, 0.1, 0.5)"          # two speed bands
```

A broken formula is logged once to the console and the plain `visualSpeedMultiplier` is used instead.
The formula is compiled once per config change, not on every frame.

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
./gradlew build      # artifact: build/libs/Create-SoftMotion-1.1.1.jar
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

`visualSpeedMultiplier` (or the formula's result) is applied to the visual angle/rotational speed only:

- `RotatingInstance.setRotationalSpeed` — the Flywheel render instance for shafts, cogwheels and other
  kinetic blocks gets a scaled speed, while the block entity keeps its real RPM;
- `MechanicalBearingBlockEntity.getInterpolatedAngle` — the returned angle is replaced by a locally
  integrated *visual* angle that follows the real angle at the configured ratio (shortest-delta
  integration, so wrapping never produces a jump);
- `ControlledContraptionEntity.setAngle` — the same visual-angle state for contraption entities, skipped
  when `slowBearingContraptions` is off or the multiplier is `1.0`.

Visual angle states are kept in `WeakHashMap`s, so they do not leak block entities or entities.

### Known limitations

- The rendered angle follows the real angle by shortest-delta integration. If a real angle changes by
  more than 180° between two rendered calls (extremely high RPM with a very low frame rate), the
  integration can misinterpret it as rotation in the opposite direction.
- For bearings and assembled contraption entities Create does not expose the driving speed to the
  mixins, so the `rpm` variable is estimated from the real-angle change over wall-clock time (with
  smoothing); formula results for those elements are approximate. Shafts and other Flywheel rotating
  instances report the exact speed.
- Results are clamped to `0.0..1.0`: the formula can only slow visuals, never speed them past vanilla.

## Modrinth

https://modrinth.com/mod/createsoftmotion

## License

MIT
