# IndustrialCraft2-CE

IndustrialCraft2-CE implements IndustrialCraft gameplay for Minecraft 26.1.2, NeoForge 26.1.2.114 and Java 25. Alpha.11 replaces the earlier port's runtime implementations with independently written controllers and shared, bounded algorithms. Existing IC2 materials, registry IDs and save fields are retained. Alpha.12 optimizes crop scheduling and initial crop synchronization.

| Component | Version |
| --- | --- |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.114 |
| Java | 25 |
| IndustrialCraft2-CE | 2.9.40-alpha.12 |
| Optional JEI | 29.43.0.107 |

Java packages use `ic2ce`, and game integration uses `ic2ce.neoforge`. Addons must recompile against `ic2ce.api`. The mod ID and resource namespace remain `ic2`, preserving existing items, recipes, models and saved block entities.

## Project layout

| Path | Responsibility |
| --- | --- |
| `engine/` | JDK-only algorithms, quantity ledgers and contract oracles |
| `api/` | Independently compiled addon contracts |
| `platform/neoforge/` | Native device controllers, registration, capabilities, networking and client views |
| `assets/` | Retained IC2 materials and migrated resource definitions |
| `verification/` | Isolated native gameplay fixtures in a separate verification artifact |
| `tools/` | Build, namespace, source, material, language and release audits |
| `validation/` | Versioned machine-readable evidence |

The root project produces one mod JAR. Machine families have separate implementations for their resource and timing rules. Induction heating, pressurized steam, fractional UU amplification, fair routing, mining sweeps and reactor components share only the relevant primitives. See [ARCHITECTURE.md](ARCHITECTURE.md) for these boundaries.

## Build and install

Use JDK 25:

```sh
./gradlew build
```

On Windows, use `gradlew.bat build`. The wrapper resolves the pinned NeoForge and JEI dependencies. The standalone engine's executable contract suite runs as part of `check`.

The distribution is written to `build/libs/`:

- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.12.jar`
- `IndustrialCraft2-CE-26.1.2-2.9.40-alpha.12-sources.jar`

Place the normal JAR in a compatible instance's `mods` directory. JEI is optional and adds machine recipe browsing. Craftable IC2 recipes support the vanilla recipe book; machine-only processing uses the actual machine inventories.

## Acceptance and performance

The [source inventory](validation/rewrite-source-inventory.json) audits 912 runtime Java files: 756 independently written implementations and 156 reviewed declarations. Declarations include public signatures, markers, data constants and registration membership; they are explicitly excluded from implementation counts. No retained business implementation remains. [Declaration review](validation/rewrite-declaration-review.json) fingerprints every remaining declaration, and [ownership](validation/rewrite-ownership.json) records implementation and naming changes separately.

The alpha.11 whole-runtime acceptance passed all 23 native report gates: 508 actually used items, 186 survival placements, 366 vanilla recipe-book placements, 104 machine types, 223 behavior cases, 98 actual GUI entry points, JEI navigation and 289 available processing recipes. The external silver-ore recipe is unavailable in the isolated mod set and is excluded from that recipe count. Cable, painter, tool, lifecycle and coupled workflow suites passed. A normal JAR reload verified all 104 machines and 16 persisted states. [Acceptance evidence](validation/rewrite-acceptance.json) binds each gate to the exact artifact.

The alpha.11 artifact freshly passes 5,220 finite reactor component-pair scenarios, 16 reactor functions, 24 advanced-filter cases and 16 upgrade functions in an actual native world. Multiplayer covers 11 upgrade types across 38 upgrade-capable machine types and 171 global variants; byte-identical implementation and fixture classes bind the prior global upgrade rows through [explicit provenance](validation/rewrite-matrix-provenance.json), and final-world functionality repeats with two actual clients. [Fresh physical matrices](validation/rewrite-final-physical-matrices.json) retain all final rows. Coverage does not enumerate every possible 54-slot arrangement or world state.

The final two-hour requirement was withdrawn. The alpha.11 artifact has a normally closed 17.9-minute connected workload, plus six circuit and six crop pressure JVMs. Earlier two-hour evidence remains attributed to its prior artifact. [Run scope](validation/rewrite-long-run-scope.json) records the withdrawal without a final two-hour pass claim.

Alpha.12 adds world-owned crop due phases and dirty queues for unattended forced farms. Its final paired crop comparison against alpha.10 reduces mean tick cost by 67.09% / 90.00% / 95.15% at 1,024 / 4,096 / 8,192 plots; allocation falls by 50.42% / 83.78% / 91.20%. Actual player arrival restores native tickers and receives full crop fields. A finite 51-check real-client test verifies arrival, cadence, freezing, logout and removal. See [PERFORMANCE.md](PERFORMANCE.md) for the exact workload, fork ranges and all results, and [alpha.11 data](PERFORMANCE_ALPHA11.md) for the earlier whole-runtime comparison.

For isolated verification:

```sh
./gradlew build migrationVerificationJar
python tools/check_release_branding.py
python tools/audit_localization.py --output validation/localization
```

The verification artifact is destructive test tooling for disposable instances. It is excluded from the normal JAR and normal sources. See [PORTING.md](PORTING.md) for provenance, fixture controls and evidence rules.

## Credits and licensing

IndustrialCraft2-CE is an independent reimplementation of IndustrialCraft gameplay, rewritten with the original IC2 source code used as a reference for behavior and compatibility.

The original IC2 textures, visual assets and sound effects used in this project remain the property of the official IndustrialCraft 2 (IC2) project and their respective original copyright holders. IndustrialCraft2-CE does not claim ownership of these materials. Original copyright notices and author credits are retained.

Viking602 is credited for the IndustrialCraft2-CE work; distribution filenames, build coordinates and code identifiers use the project name. Original licensing terms remain in [LICENSE](LICENSE), [NOTICE](NOTICE) and [licenses/](licenses/); the rewrite does not grant additional rights to original IC2 code or reused materials.

Report reproducible issues through [GitHub Issues](https://github.com/SISE-Team/IndustrialCraft2-CE/issues), including game, loader and mod versions, relevant logs and reproduction steps. [Earlier release documentation](docs/history/README_ALPHA10.md) preserves the alpha.8–alpha.10 validation history.
