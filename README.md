# IndustrialCraft2-CE

IndustrialCraft2-CE is a community port of IndustrialCraft 2 for modern Minecraft and NeoForge. It retains the `ic2` mod ID, resource namespace, and public API packages.

| Component | Version |
| --- | --- |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.114 |
| Java | 25 |
| IndustrialCraft2-CE | 2.9.40-alpha.6 |
| JEI used for development and verification | 29.43.0.107 |

The port covers machines, EU/HU/KU networks, nuclear reactors, crops, fluids, upgrades, storage, and UU processing. Modern item and fluid rendering, crafting-book placement, machine menus, persistence, dedicated-server startup, and multiplayer recipe synchronization have been migrated.

## Build

Install JDK 25, then run:

```sh
./gradlew build
```

On Windows, use `gradlew.bat build`. The Gradle wrapper resolves NeoForge and the pinned JEI dependency; no external Minecraft installation or manually copied JEI JAR is needed to compile.

Artifacts are written to `build/libs/`:

- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.6.jar`
- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.6-sources.jar`

Install the normal JAR in the `mods` directory of a Minecraft 26.1.2 / NeoForge instance. JEI is optional for normal play and provides machine recipe browsing when installed.

## Verification

The alpha.4 behavior candidate completed 104 machine types, 219 behavior cases, 98 actual menu entry points, 2,123 upgrade applications, and 5,220 reactor component-pair scenarios. Two real clients then ran continuously for two hours at an average 20 TPS: 144,001 world ticks, 278,938 checks, and zero recorded failures. Chinese and English runtime localization checks and normal-JAR save/reload checks also passed.

Alpha.5 fixes cable selection and collision dimensions for the current `PipeBlock` API and restores upstream tool-independent cable drops. The targeted cable suite compares every registered ordinary cable state against the rendered model and exercises real client mining packets in creative and survival modes, including connected cable arms, drops, neighboring connections, and energy-network cleanup. All 14 ordinary cable variants passed 17,408 shape states and 60 actual mining scenarios, with 696 checks and zero failures. The earlier two-hour run remains alpha.4 evidence.

Alpha.6 fixes the painter crash during client block/entity interactions, makes durability and refill changes server-authoritative, and honors the selected hand when painting sheep. Its separate runtime suite passed 80 real client scenarios and 400 checks with zero failures, covering wool, sheep, all 16 brush colors, both hands, repeated same-color uses, creative mode, exhaustion, and automatic refill.

See [PORTING.md](PORTING.md) for the test dimensions, evidence provenance, and limits. These results cover the recorded scenarios; arbitrary 54-slot layouts and arbitrary interactions with other modpacks have not been exhaustively enumerated. This release remains alpha.

The verification harness is a separate build target:

```sh
./gradlew migrationVerificationJar
python tools/check_release_branding.py
```

The verification JAR creates destructive test fixtures and must be installed only in an isolated disposable instance. Do not install it together with the normal JAR. It is excluded from the regular release and runtime classpath.

## Credits and notices

Original IndustrialCraft 2 credits are retained in the mod metadata. The community port is maintained by SISE-Team, with port author **Viking602**.

The original input identifies its license as **All rights reserved**. This repository preserves that designation rather than applying a blanket open-source license to upstream code or assets. See [LICENSE](LICENSE) and [NOTICE](NOTICE) for provenance and third-party notices.

Report reproducible problems through [GitHub Issues](https://github.com/SISE-Team/IndustrialCraft2-CE/issues), including the game/loader version, mod version, relevant logs, and reproduction steps.
