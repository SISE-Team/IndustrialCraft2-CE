# IndustrialCraft2-CE architecture

This guide is for contributors. It describes where code belongs and how the modules depend on each other. The previous release-audit description is kept in [docs/history/ARCHITECTURE_1.0.md](docs/history/ARCHITECTURE_1.0.md).

## Modules

Dependencies point one way:

```text
engine  ──►  api  ──►  platform/neoforge  ──►  root distribution JAR
(JDK only)   (addon contracts)   (NeoForge controllers)
```

| Module | May depend on | Holds |
| --- | --- | --- |
| `engine` | JDK 25 only | Game rules as pure Java: arithmetic, graphs, ledgers, value types. Contract tests run without the game. |
| `api` | `engine`, Minecraft, NeoForge | Public addon contracts under `ic2ce.api`. Never imports the platform. |
| `platform/neoforge` | `engine`, `api`, Minecraft, NeoForge, JEI | Block entities, items, menus, networking and every world interaction, under `ic2ce.neoforge`. |
| `verification` | the platform | In-game fixtures, packaged only into the separately built verification JAR. |

`./gradlew check` runs `checkLayering`, which fails the build when `engine` references Minecraft, NeoForge, `ic2ce.api` or the platform, when `api` references the platform, or when any source uses the retired `ic2ce.core` namespace.

## Engine

Put a rule in `engine` when it can be stated without the world: given numbers in, numbers out. Examples are `agriculture.CropBreeding`, `reactor.ReactorCore`, `energy.SinkRoutes` and `recipe.MaterialCostCompiler`.

- Randomness is passed in as suppliers or explicit draws so the platform keeps its draw order.
- Every engine class has cases in `engine/src/test/java/ic2ce/engine/EngineContractTest.java`, which `./gradlew build` runs. Prefer comparing against an independent oracle over random inputs.

## Platform packages

`ic2ce.neoforge` is organized by game domain. A domain package holds its block entities, its registered content and, in `menu` and `client` subpackages, its menus and screens.

| Package | Responsibility |
| --- | --- |
| `agriculture` | Crop plots, species and crop machines |
| `cable`, `energy` | Conductors, the energy network (`energy.grid`), storage banks and transformers |
| `power`, `thermal`, `kinetic` | EU, heat and kinetic generators and converters |
| `processing` | Machines that transform items or fluids, including UU fabrication (`processing.uu`) |
| `reactor` | The nuclear core, chambers, ports and reactor components (`reactor.part`) |
| `transport`, `storage` | Item and fluid routing, boxes and tanks |
| `mining`, `terrain`, `utility`, `personal`, `crafting`, `explosion`, `vehicle` | Their named device families |
| `item.*` | Items that are not owned by a device domain: tools, armor, containers, batteries, upgrades |
| `material` | Plain crafting materials |
| `world` | World-generated and decorative blocks, per-world state, tick dispatch, direction and area helpers |
| `block`, `component`, `inventory`, `menu`, `client`, `network`, `fluid`, `recipe` | Shared native building blocks used by the domains |
| `registry` | Registration plumbing, creative tabs, tags and the shared `register` helpers |
| `runtime`, `diagnostics`, `config` | Side services, environment binding, logging and configuration |

The mod entrypoint and NeoForge event handlers stay in the `ic2ce.neoforge` root.

## Runtime services

There is no global service locator. Use the owner of each service directly:

| Need | Use |
| --- | --- |
| A project identifier | `ProjectIds.id(name)` |
| Logging | `ProjectLog.LOG` |
| Registration and environment queries | `EnvProxy.get()` |
| Side proxy, keyboard, audio, network managers | `SideServices` |
| Non-world randomness | `SharedRandom.INSTANCE` (prefer the level's random source when one is available) |

Client implementations are installed by the mod entrypoint on the first registry callback, so a dedicated server never loads client classes.

## Adding a machine

1. Write any world-independent rule in `engine` and add contract cases.
2. Add the block entity in its domain package, with its menu in `<domain>.menu` and screen in `<domain>.client`.
3. Declare the block, block entity, item and menu fields in that domain's `<Domain>Blocks`, `<Domain>BlockEntities`, `<Domain>Items` and `<Domain>Menus` holders, using the central `register` helpers.
4. A new holder must also be added to the matching `init()` in `registry/BlockDefinitions`, `BlockEntityDefinitions`, `ItemDefinitions` or `MenuDefinitions`. Blocks load before block entities, which load before items and menus.
5. Add the item to its tab in `registry/CreativeContents` and its assets under `assets/src/main/resources`.
6. Run `./gradlew build migrationVerificationJar`, then `python3 tools/dump_registry_ids.py` and refresh the baseline with `--write` when the ID change is intended.

Registry, resource and persistence IDs keep the original `ic2` namespace; never rename one without a save migration.

## Moving code

Use `tools/relocate.py` with a table under `tools/relocations/` to move types between packages. It rewrites imports, fully qualified names and reflective class names in every source root and keeps the `validation/` ownership ledgers in step. After a move, `tools/audit_rewrite.py` must be rerun against the baseline JARs to refresh `validation/rewrite-source-inventory.json`.
