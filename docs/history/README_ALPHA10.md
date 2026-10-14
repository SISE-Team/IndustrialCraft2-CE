# IndustrialCraft2-CE

IndustrialCraft2-CE is a community port of IndustrialCraft 2 for modern Minecraft and NeoForge. It retains the `ic2` mod ID, resource namespace, and public API packages.

| Component | Version |
| --- | --- |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.114 |
| Java | 25 |
| IndustrialCraft2-CE | 2.9.40-alpha.10 |
| JEI used for development and verification | 29.43.0.107 |

The port covers machines, EU/HU/KU networks, nuclear reactors, crops, fluids, upgrades, storage, and UU processing. Modern item and fluid rendering, crafting-book placement, machine menus, persistence, dedicated-server startup, and multiplayer recipe synchronization have been migrated.

## Build

Install JDK 25, then run:

```sh
./gradlew build
```

On Windows, use `gradlew.bat build`. The Gradle wrapper resolves NeoForge and the pinned JEI dependency; no external Minecraft installation or manually copied JEI JAR is needed to compile.

Artifacts are written to `build/libs/`:

- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.10.jar`
- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.10-sources.jar`

Install the normal JAR in the `mods` directory of a Minecraft 26.1.2 / NeoForge instance. JEI is optional for normal play and provides machine recipe browsing when installed.

## Verification

Alpha.10 indexes safe deferred recipe candidates, reuses parsed configuration paths and allocates cable-effect collections only when needed. It also fixes three recipe-replacement paths that could loop indefinitely. Native tag reloads, custom matching order, concurrent reads and voltage/shock behavior have dedicated regressions.

Eight independent Java 25 forks compare alpha.9 with alpha.10 using 2,000 warmup and 5,000 measured ticks per tier. With 2,048 machines, 4,096 cables and 512 committed inventory transactions per tick, median mean tick cost falls by **26.4%**, P99 by **26.0%**, and server-thread allocation by **28.4%**. All 120,000 paired measured ticks and a separate 100,000-tick sustained run pass. See [the alpha.10 report](PERFORMANCE_ALPHA10.md) and [audit](validation/performance-alpha10-audit.json) for absolute values, per-fork ranges, artifact hashes and reproduction.

The final alpha.10 code passes 2,728 targeted checks, including all 104 machine types, 219 behavior cases, 98 actual GUIs, 289 applicable processing recipes, the coal-powered workflow, all 14 ordinary cable variants and 33 energy-effect scenarios. The optional external silver-ore recipe has no ingredient in the isolated mod set and is explicitly unavailable. A clean source build has zero executable class differences; verification fixtures are excluded from the release. These finite native workloads do not measure full-modpack FPS. Earlier all-item and two-hour multiplayer results remain historical evidence below.

### Historical alpha.9 performance review

Alpha.9 uses Java 25 constructor prologues, cached entity factories, class-dependent network type caching, sparse journals for the five bundled storage boxes, and reusable cable geometry. It also fixes immediate energy-tile lookup after same-tick cable replacement. Voltage, loss and reactor arithmetic retain their existing execution model.

Eight independent JVM forks compare alpha.8 and alpha.9 on the same Java 25 runtime, in paired ABBA / BAAB order. At the largest native workload—2,048 machines, 4,096 cables and 512 committed inventory transactions per tick—the median mean tick cost falls from **6.152 to 4.894 ms (20.4%)**, and P99 falls from **10.718 to 6.849 ms (36.1%)**. Allocation for a 126-slot box transaction falls from 7,952 to 408 bytes. See [PERFORMANCE.md](PERFORMANCE.md) and [the performance audit](validation/performance-audit.json) for all metrics, fork variability, methodology and reproduction.

The comparison passes 38,400 measured stress ticks. A separate sustained run passes 100,000 measured ticks and 51.2 million measured transactions at the largest workload, collects actual processing output and records positive EU in all 1,024 macerators at completion; the measured phase lasts 251.1 real seconds with sprinting enabled. This is separate from the paired statistics and from the earlier two-hour run. Alpha.9 also passes 7,756 targeted regression checks covering all 104 machine types, 219 behavior cases, 98 client GUI entry points, 186 survival placements, cable mining, models, inventory transactions, a coal-powered workflow and immediate cable replacement. Its 1,295 release classes match the tested executable code byte-for-byte; an independent clean source build has zero executable differences. These are finite isolated IC2 workloads, not a complete-modpack FPS measurement.

### Historical alpha.8 full review

Alpha.8 restores native equipment assets, damage, rarity and repair components; moves vanilla block/item tags to current paths; repairs sided and transactional inventory/fluid transfers; fixes crop seeds, potato growth, ground and biome changes; and restores scanner results, cable callbacks, coffee and radiation callbacks, and stateful item/wall rendering. Component transfers respect one-item coil/motor/conductor slots and continue past full slots.

The alpha.8 artifact passed 14 fresh runtime reports with **10,157 checks and zero failures**. Its 1,293 main classes match its tested artifact byte-for-byte. Alpha.8 recorded coverage includes:

| Scope | Passed coverage |
| --- | --- |
| Registered items | 508/508 with explicit actual-use evidence |
| Block-item placement | 186 native placements |
| Machines | 104 types, 219 behavior cases, 98 actual GUI entry points |
| Crafting | 372 native result operations; 366 vanilla recipe-book cases |
| Machine processing | 289 applicable standard recipes; 41 solid-canning recipes |
| Functional item scenarios | 237 primary cases, plus separate material, crop, tool and component suites |
| Cable mining | 14 variants, 17,408 shape states, 60 native mining scenarios |
| Coupled power/processing | Finite coal supply through generation, cables, storage, processing and crafting; all 14 ordinary cable variants deliver EU |

The [alpha.8 item usage ledger](validation/item-usage-ledger.json) distinguishes native client packets, server menu operations, capability transactions, processing and intentional rejection. Spawning an item or receiving a recipe output alone does not count as use. [Alpha.8 machine results](validation/full-review-machine-results.json), [the full review](validation/full-review-audit.json), [source inventory](validation/full-review-source-inventory.json) and [component-slot controls](validation/component-slot-limit-audit.json) preserve that measured coverage and artifact provenance.

Two actual clients also completed a fresh continuous two-hour run on the final alpha.8 executable code: 7,200.039 real seconds, 144,001 world ticks, 278,932 checks including the retained finite matrices, approximately 20.00 TPS, and zero failures. Both clients were connected and alive in every recorded sample. The same normally closed world then passed a normal-JAR reload for all 104 machine types and 16 persisted-state checks, using a fresh run marker to exclude historical command-storage results.

All 23 localization catalogs passed key-retention and format checks; Java formatted 26,563 entries without errors. English, simplified Chinese and traditional Chinese catalogs are complete. A remaining traditional-Chinese English toolbox tooltip and the corresponding simplified-Chinese mistranslation were corrected separately, without changing the tested executable code.

Earlier release results are retained separately:

The alpha.4 behavior candidate completed 104 machine types, 219 behavior cases, 98 actual menu entry points, 2,123 upgrade applications, and 5,220 reactor component-pair scenarios. Two real clients then ran continuously for two hours at an average 20 TPS: 144,001 world ticks, 278,938 checks, and zero recorded failures. Chinese and English runtime localization checks and normal-JAR save/reload checks also passed.

Alpha.5 fixes cable selection and collision dimensions for the current `PipeBlock` API and restores upstream tool-independent cable drops. The targeted cable suite compares every registered ordinary cable state against the rendered model and exercises real client mining packets in creative and survival modes, including connected cable arms, drops, neighboring connections, and energy-network cleanup. All 14 ordinary cable variants passed 17,408 shape states and 60 actual mining scenarios, with 696 checks and zero failures. The earlier two-hour run remains alpha.4 evidence.

Alpha.6 fixes the painter crash during client block/entity interactions, makes durability and refill changes server-authoritative, and honors the selected hand when painting sheep. Its separate runtime suite passed 80 real client scenarios and 400 checks with zero failures, covering wool, sheep, all 16 brush colors, both hands, repeated same-color uses, creative mode, exhaustion, and automatic refill.

Alpha.7 repairs related treetap, cable-cutter, and chainsaw client interactions, corrects tool break callbacks, and prevents duplicate client charge and drop changes. It also persists nano-saber power state, safely clears exhausted armor, allows the declared quantum-armor discharge cost, saves quantum-armor color edits, and saves obscurator scan resets. Its focused suite passed 74 scenarios and 514 checks with zero failures: 54 real client packet scenarios, four client API probes, and 16 server API probes. An unchanged harness on alpha.6 reproduced client cast errors, state failures, and a server crash caused by a null armor stack.

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
