# Provenance and verification

The current build targets Minecraft 26.1.2 / NeoForge 26.1.2.114 on Java 25. Alpha.11 independently implements runtime behavior under `ic2ce` Java packages while reusing IC2 materials and preserving `ic2` registry/resource IDs and persistence fields. Addons must recompile against `ic2ce.api`.

## Reference material

The behavioral input is `industrialcraft-2-2.9.40+ex119-1.19.2-forge.jar`, SHA-256 `5d06ca9146041043132a72bad9b5612e260e77a84a2f3773a3da9b0167ea1d3d`. The original JAR and complete decompilation are retained locally as reference. The exact alpha.10 commit `c4e628d5d31a78a636ef6151feb4ed657ca78592` and its normal/source archives anchor implementation ownership and performance comparisons. Neither reference is required to build or loaded at runtime.

Original material notices are preserved. All 1,537 original PNG/metadata files match the original bytes after path migration. The resource graph contains 764 roots, 1,154 reachable models and one native advancement background, with no missing references. Of 5,786 asset/data entries, 5,762 match alpha.10 byte-for-byte; the audited exceptions add 16 missing progression text keys in 23 catalogs, localize the text-input narration label and fix that background identifier. Every existing translated value and advancement condition remains unchanged. Release/source audits exclude reference trees, fixtures and game/library classes.

## Evidence rules

Each native run uses a fresh disposable instance and records the precise artifact SHA-256. Hidden real clients perform physical packets, placement, mining, GUI/JEI navigation and item interactions against integrated or dedicated native servers. World writes are confined to those instances. Shared Minecraft JARs, libraries, native libraries, assets and version metadata are unchanged; the protected-file audit covers 4,864 fixed files.

Spawning a registered item, obtaining a recipe output or opening a menu does not establish that its behavior works. Item ledgers record actual use or intentional rejection. Machine results record device-specific behavior separately from GUI opening. Recipe tests process real inputs and verify outputs; vanilla recipe-book tests exercise actual placement. Upgrade/reactor matrices describe their finite admitted combinations and do not claim every possible world configuration is enumerable.

The final tuned two-hour endurance requirement was explicitly withdrawn. Its 17.9-minute two-client workload is preserved as a controlled short run with no two-hour pass claim. Both clients stayed connected and alive in the recorded samples. Earlier two-hour evidence remains linked to its own artifact. Pressure tests use balanced fresh JVMs and exact quantity checks independently of elapsed real-world duration. A normal, uninstrumented JAR reloads the normally closed test world; vanilla commands verify all 104 machine entities and retained state after the fixture code is absent.

Failed exploratory runs and incorrectly specified assertions are preserved locally. Only complete reports without failed assertions count as passing acceptance, and historical passes remain attributed to their original artifact. Client/server logs are checked alongside report booleans. Expected offline authentication/Realms errors and deliberate rejection fixtures are classified explicitly.

## Reproduction

```sh
./gradlew build migrationVerificationJar
python tools/check_release_branding.py
python tools/audit_localization.py --output validation/localization
python tools/audit_rewrite.py --baseline BASELINE.jar --baseline-sources BASELINE-sources.jar --output source-audit.json
```

`tools/launch_validation.py` supports isolated machine, item, workflow, recipe, cable, tool, painter and lifecycle modes. `tools/launch_dedicated_validation.py` controls an owned server and records command lines, artifact hashes, process lifetime and controlled shutdown. These launchers require the local disposable test setup and must not target a valued world. Their `ic2.verify*` JVM flags deliberately retain existing fixture selector compatibility; the runtime Java namespace is `ic2ce`.

Native fixtures live in `verification/` and compile only into the separate verification artifact. The production sources JAR includes the actual API, engine and native implementation sources. The standalone engine uses independent conservation, matching, graph and quantity oracles and runs through `gradlew check`.

Final performance uses balanced fresh JVM forks, identical Java 25 heap/GC/CPU settings, instruction-parity checks and raw per-fork ranges. Server tick/allocation measurements and connected short-run operation are reported separately; earlier two-hour records retain their original artifact attribution. [Earlier porting and regression evidence](docs/history/PORTING_ALPHA7.md), [alpha.10 performance](PERFORMANCE_ALPHA10.md) and the versioned validation records retain their original limits.

## Alpha.12 crop follow-up

Alpha.12 adds world-owned due phases and dirty queues for unattended farms, while actual player presence restores native crop tickers. Native chunk/border/entity-loaded/freeze rules remain admission gates. `ChunkWatchEvent.Sent` sends full crop fields after the real client receives a chunk. Pressure fixtures preserve exact physical plot and growth callback counts. The finite real-client transition run covers arrival, phenotype/care synchronization, observed cadence, freeze, logout and block removal. Alpha.11 all-item/machine evidence keeps its original artifact attribution; alpha.12 adds targeted crop/native/workflow/clean-source acceptance.

The repository history was rewritten to English Conventional Commits at the user's request. Commit trees and author/committer metadata remain identical; [the history map](validation/conventional-history-rewrite.json) resolves old evidence IDs. Future commits follow [CONTRIBUTING.md](CONTRIBUTING.md).
