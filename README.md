# Advanced BaseFinder

A standalone, reusable **Minecraft Java Edition 26.2** client module for **client-side world analysis**: it detects anomalies in natural terrain generation and a suite of specialized findings (storage, amethyst, spawners, redstone, portals, structures, signs, beds, beacons, light anomalies, illegal blocks, nether highways, breadcrumbs, live activity), fuses them into **base candidates**, and surfaces them on an in-game radar + 3D overlay + status panel.

> This is a **module** meant to be integrated into an existing custom client — not a client, launcher, injector, or GUI framework. It uses **only information a normal client legitimately has** (loaded chunks, entities, received packets). It cannot see through unloaded chunks or read server-side data.

## Highlights

- **16 toggleable scanner subsystems** plus later additions (entity-stacking, redstone update-frequency, lighting-anomaly, and 12 advanced hidden-base scanners) — **31 in total**, each with its own settings, confidence and radar category.
- **Confidence fusion (evidence-aware)**: signals are grouped into evidence classes (habitation / marker / security / activity / ambient); a cluster only reads as a real **base** if it has habitation or a strong marker, otherwise it's labeled a **"Mining area"** or **"Activity"**. A corroboration bonus rewards multiple distinct evidence classes agreeing, and suppressors (village context, natural explanations) reduce false positives. Only candidates at/above your global **confidence floor (default 75%)** draw an overlay.
- **Village discrimination**: a lightweight VillageContext infers villages (villagers + bell) and down-weights village-explainable findings (farms, decoration, beds) so they don't masquerade as player bases.
- **Temporal persistence**, **real-time activity detection**, **stash-cluster analysis**, **nether-highway** and **breadcrumb** tracing, **AFK detection**, and **directional guidance**.
- **In-game rendering** via the host's renderer (Blaze3D on 26.2) — a passive click-through layer for world markers/radar and an interactive layer for settings/rules. No extra OS window. `WDA_EXCLUDEFROMCAPTURE` supported.
- **Visual custom-rule editor** (conditions, AND/OR, one level of nesting, actions incl. colour, force-hide, pin-radar, notifications).
- **Adaptive, off-thread scanning** that self-tunes worker count to protect FPS.
- **Host-delegated persistence** (the module saves nothing itself), **temporary findings only** (wiped on close), one configuration, no profiles/exports/history.
- **MC-free core**: the whole engine/UI builds and tests with **no Minecraft on the classpath** (81 passing unit + integration tests).

## Layout

```
src/main/java/com/basefinder/   core (no Minecraft dependency)
  world/      MC-agnostic world model (WorldSnapshot, BlockAccess, Blocks catalog)
  model/      Finding, BaseCandidate, Confidence, enums
  config/     Settings, GlobalSettings, SettingsBridge (host-delegated)
  engine/     AnalysisEngine, adaptive worker governor, sweeper, metrics, health
  scanner/    Scanner API + impl/ (31 scanners)
  fusion/     clustering + confidence fusion + temporal persistence
  rules/      visual rule model, conditions, actions, codec, validator
  notify/     notification policy (new-only, once, cooldown, rule overrides)
  guidance/   directional guidance
  diag/       two-level diagnostics with de-duplication
  ui/         Renderer seam, components, theme, views (radar/HUD/dashboard)
  api/        BaseFinderModule facade + UiModel/UiCommands seams
src/mc/java/com/basefinder/mc/   Minecraft 26.2 adapters (compile with -PmcJar)
src/test/...                     simulated-world tests
docs/                            INTEGRATION.md, BUILD.md, LIMITATIONS.md
examples/                        usage examples
```

## Build & test

```
./gradlew build        # compiles the core and runs all tests (no Minecraft needed)
./gradlew test
```

## Preview the GUI (no Minecraft)

See the real overlay in a desktop window — Night glass theme, draggable radar /
compact panel / dashboard, resize, UI-scale slider and fade:

```
./gradlew previewGui
```

Controls: **I** opens/closes the config dashboard, drag the radar / compact panel /
dashboard, drag the dashboard's bottom-right corner to resize, **R** resets the
layout, **Esc** closes. (The window uses sample data via a Java2D renderer that
implements the same `Renderer` the Minecraft `BlazeRenderer` does.)

See [docs/BUILD.md](docs/BUILD.md) to compile the Minecraft adapters and [docs/INTEGRATION.md](docs/INTEGRATION.md) to wire it into your client.

## Honesty about verification

Unit/integration tests validate the engine, scanners, fusion, rules, notifications and lifecycle against **simulated** world data. They do **not** prove in-game behaviour. The `src/mc` adapters are compile-verified only inside a real 26.2 client and use the deobfuscated 26.2 names; a few render/level hooks are left as explicit integration points rather than invented. See [docs/LIMITATIONS.md](docs/LIMITATIONS.md).
