# Create: SoftMotion

**English** | **[Русский](README.ru.md)**

[![Modrinth](https://img.shields.io/badge/Modrinth-createsoftmotion-blue)](https://modrinth.com/mod/createsoftmotion)
![MC](https://img.shields.io/badge/Minecraft-1.19.2-green) ![loader](https://img.shields.io/badge/Forge%2043.2%2B%20%7C%20Fabric%200.15%2B-orange)

Client-side accessibility addon for [Create](https://modrinth.com/mod/create) that slows down the
**visual** rotation of Create machinery and contraptions, for players with visual hypersensitivity.

- Slows only the rendering of rotation: flywheel render instances (`KineticData.setRotationalSpeed`),
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
If NeoForge does not provide `ConfigurationScreen`, the mod still works through the TOML file. On the
Forge/Fabric builds of this branch there is no generic config screen API, so the TOML file is the way
to change settings (on Fabric it is written with defaults on first launch).

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

This branch targets Minecraft 1.19.2 with a multi-module [Architectury](https://modrinth.com/mod/architectury-api)
toolchain (`architectury-plugin` + `dev.architectury.loom`): `common` (mixins + math + config facade),
`forge` (Forge 43.2.4+) and `fabric` (Fabric Loader 0.15+).

Requires a JDK 17–21 for the Gradle launcher (the wrapper is Gradle 8.12, which refuses newer launchers)
and internet access on the first build (downloads loom + Forge userdev assets + the Create 0.5.1.i jars
from the Modrinth maven; `maven.createmod.net` hosts nothing for MC 1.18/1.19). The flywheel and
Registrate jars are committed under `libs-maven/` because they only ship nested inside the Create jars
— `fetch-libs.sh` regenerates that directory.

```bash
./gradlew build
# forge artifact:  forge/build/libs/create-softmotion-forge-1.1.1+1.19.2.jar
# fabric artifact: fabric/build/libs/create-softmotion-fabric-1.1.1+1.19.2.jar
./gradlew :forge:runClient   # dev client with Create 0.5.1.i preinstalled
./gradlew :fabric:runClient  # dev client with create-fabric 0.5.1-i-build.1630 preinstalled
```

The main branch holds the NeoForge 1.21.1 build; other Minecraft versions live in per-version branches
(`mc1.20.1`, `mc1.19.2`, `mc1.18.2`, `mc1.16.5`).

## Compatibility

| | |
|---|---|
| Minecraft | 1.19.2 |
| Loader | Forge 43.2.3+, Fabric Loader 0.14+ |
| Create | 0.5.1.i on Forge, create-fabric 0.5.1-i-build.1630 on Fabric (required, client side) |
| Side | client only (`client_side: required`, `server_side: unsupported`) |

Note: the create-fabric port for this Minecraft version is BETA (build 1630); the Fabric build of
SoftMotion targets that beta line. The Forge build targets the stable Create 0.5.1.i release.

All injections are `require = 0` and the mixin config sets `defaultRequire = 0`, so if a future Create
build renames a target the mod will not crash on startup — that injection simply will not apply.
Compatibility with other Minecraft or Create versions is not guaranteed.

## How it works

`visualSpeedMultiplier` (or the formula's result) is applied to the visual angle/rotational speed only:

- `KineticData.setRotationalSpeed` — the single choke point every Create 0.5.1 flywheel render instance
  (shafts, cogwheels, belts, gearboxes, mixers) funnels its rendered rotational speed through, gets a
  scaled speed, while the block entity keeps its real RPM;
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
