# IndustrialCraft2-CE porting and verification

This document describes the Minecraft 26.1.2 / NeoForge 26.1.2.114 port and verification on 2026-10-12. The current project version is `2.9.40-alpha.6`, built with Java 25.

## Provenance

The migration input was `industrialcraft-2-2.9.40+ex119-1.19.2-forge.jar`, SHA-256 `5d06ca9146041043132a72bad9b5612e260e77a84a2f3773a3da9b0167ea1d3d`. The original input and complete decompilation were retained locally and used to compare behavior. They are not required to compile the checked-in source.

The `ic2` registry, resource, and API namespaces remain unchanged. The normal artifact contains neither Minecraft/LWJGL classes nor the verification harness. Original authors and licensing metadata are retained separately from project branding.

## Migration changes

- **Rendering:** migrate legacy block/item texture paths, replace obsolete model-loader declarations with valid models, add modern item definitions and battery charge dispatch, and register IC2 fluid models. The resource graph covers 764 roots and 1,154 reachable models with zero missing references. All 1,537 original PNG/mcmeta files retain their original bytes.
- **Recipes:** migrate shaped/shapeless crafting, placement/display metadata, tool remainders, charge transfer, UU recipes, and JEI categories. Crafting recipes support the vanilla recipe book; machine-only recipes retain their special placement behavior.
- **Dedicated server and multiplayer:** guard client-only initialization, synchronize the complete current recipe map, initialize client crop/fluid data after synchronization, fix machine-recipe network encoding, and clear connection-specific recipe state on disconnect.
- **Machines and persistence:** restore energy/fluid/item capabilities, modern entity collision callbacks, machine menus and network buttons, source-fluid pumping, storage drops, chunk-loader persistence, UU scanning/replication, and recipe processing. Inventory, stored energy, fluids, modes, owner profiles, and tool damage use current NBT/component APIs.
- **Upgrades:** prevent capacity and remote-range overflow, honor stack count, and restore advanced damage/charge/NBT filtering. Lossless real item/fluid transfers are checked separately from configuration calculations.
- **Localization:** complete primary English and Chinese catalogs, dynamic item/container/fluid names, crop cards, handheld menus, tooltips, and local chat messages. Existing translations in other locales are retained with English fallback.

## Completed validation

The alpha.4 behavior candidate's full verification used an isolated dedicated NeoForge server and two real clients, one in Simplified Chinese and one in English, with JEI and an existing independent native-window guard. The game installation, public libraries, assets, metadata, and original native libraries were left unchanged: 4,864 protected fixed files matched their baseline hashes.

| Area | Recorded coverage | Result |
| --- | --- | --- |
| Machine types | 104 types, 219 behavior cases, 98 actual menu entry points, 1,361 checks | 0 failures |
| Basic regression | 3,600 checks, 14 actual GUI cycles | 0 failures |
| Recipe registration | 407 crafting/UU recipes, 335 machine recipes, 17 source fluids | Passed |
| Upgrade applications | 11 variants, 38 capable machine types, 171 global conditions, 2,123 accepted applications | Passed |
| Upgrade behavior | 16 real processing/transfer/power cases, 24 advanced-filter transfers | Passed |
| Reactor component pairs | 29 component types, 5,220 placed scenarios | Passed |
| Additional reactor behavior | 16 cases including fuel exhaustion, breeding, fluid mode, full grids, and save/restore | Passed |
| Localization | 23 catalogs, 26,563 entries; real Java format checks | 0 errors |
| Runtime localization | Each client: 1,102 keys, 508 items, 91 creative variants, 37 crop cards, 5 chat checks | Passed |
| Multiplayer inventory | One native click per client; shared 13 ingots became 13 + 0 | No duplication or loss |
| Normal-artifact reload | 104/104 machine entities plus 16 persisted-state checks | Passed |

Upgrade dimensions include suitability, stack counts 1/2/64, legal unordered pairs and both insertion orders, high-count overclock/storage/transformer combinations, and all nonempty presence combinations of four control upgrades. Reactor pair dimensions include horizontal/vertical adjacency, initial heat 0/5000/9000, and fresh/90%-worn components. Full 54-slot cooling and overload cases are also covered. Test reactors are spaced 256 blocks apart to prevent upstream nuclear ray propagation from contaminating neighboring scenarios.

The upstream remote-interface API has no machine declaring `RemotelyAccessible`; tests cover rejection by unsuitable machines and the range API. The upstream NBT-selector button is visible only in development mode; release GUI behavior retains that design, while NBT filtering is covered by transfer tests. Steam repressurizer conversion uses a verification-only external `forge:steam` target. The normal artifact retains the upstream empty target tag and requires an external steam provider for that conversion.

## Continuous two-hour run

The final behavior candidate ran from **06:33:05 to 08:33:05 UTC** on 2026-10-12 (14:33:05 to 16:33:05 UTC+8):

- Actual elapsed time: **7,200.04 seconds**.
- Normal world ticks: **144,001**, average **20 TPS**.
- Extended checks: **278,938**, **zero failures**.
- Samples: **120**; both actual clients remained connected with fresh heartbeats and matching recipe counts at every sample.
- Last sampled totals: **2,519 macerator outputs**, **2,878 furnace ingots**, **1,439,000 mB coolant delivered**.

The run checks all 104 machine entities, valid energy, real processing, item/fluid conservation, safe reactor production, client connectivity, TPS, and heap use. All machine types undergo typed NBT roundtrip checks every ten minutes. Accelerated setup/matrix ticks are excluded from the two-hour duration. Passed baseline/pair matrices from the earlier completed round were retained in the saved world; subsequent changed-filter, deep-reactor, multiplayer, and endurance checks were rerun. Failed exploratory rounds were archived rather than counted as passes.

After normal shutdown, read-only region parsing confirmed all 104 saved entities, finite nonnegative energy, valid inventory counts, five boxes containing 13 ingots each, and four tanks containing 2,000 mB coolant each. The same world was then reloaded using the normal artifact without verification classes or JVM flags. Vanilla read commands confirmed 104 machine types and 16 state checks. Both clients opened localized advanced-upgrade menus; the enabled damage filter survived restart. Offline-account/Realms request errors were recorded separately from mod/runtime failures.

## Project branding rebuild (alpha.4)

The public repository uses `IndustrialCraft2-CE` for the project, artifact names, display name, and build coordinates. Personal attribution is limited to author credits. This is a metadata, packaging, documentation, and build-dependency change after the behavior verification above; every game class and asset/data entry is compared against that verified candidate before publication. The project now resolves its pinned JEI dependency from Maven rather than a machine-specific `mods` path.

The final artifact is built from an exported clean Git source tree without the build cache. A clean compile changes debugger local-variable names in 26 anonymous classes relative to the older incremental compile. All 1,290 class models compare equal after normalizing only those debugger variable names and rebuilding constant-pool references; instructions, members, signatures, annotations, source lines, and other attributes are retained. Assets and data are byte-identical. This comparison does not modify the shipped class files.

The detailed behavior evidence is retained in the maintainer's local validation archives. The public `validation/project-release-audit.json` records the project-branding build hash and comparison against the behavior-tested candidate. It does not claim a second two-hour test of different game logic.

## Cable selection fix (alpha.5)

Legacy `PipeBlock` constructors took a radius in block units. The current constructor takes the full width in model pixels, where one block is 16 pixels. Passing the legacy `thickness / 2` value made the cable selection and collision boxes 32 times too thin. A ray aimed at a visible cable could therefore hit the floor behind it. The migrated constructor now passes `thickness * 16`.

The pre-fix verification recorded model/shape mismatches for all 14 ordinary cable variants and reproduced a client ray missing a visible cable. The targeted suite derives the expected core dimensions from the actual model JSON and checks selection and collision shapes for all connection, color, waterlogging, and detector/splitter power states. It also places cable items and sends real client mining actions to an integrated server in a disposable world, for isolated cores and connected arms in both creative and survival modes. Drop counts, the backing floor, neighboring cables and connections, and energy-network registration/removal are checked after deferred server updates settle.

The test also exposed an incorrect tool requirement introduced by the migrated metal-material factory. The original decompiled cable registration uses `Properties.of(Material.METAL)` without `requiresCorrectToolForDrops`, and the original pickaxe tag contains no cables. Cable registrations now use fresh metal-colored properties without that additional requirement, restoring the original bare-hand drops while retaining hardness and sound settings.

The cable constructor, cable registration properties, and property-gated verification dispatch change executable behavior from alpha.4. Assets and data remain byte-identical. The alpha.4 endurance run above has not been repeated for alpha.5; this release uses targeted cable regression evidence, recorded separately in `validation/cable-fix-audit.json`.

The final targeted run passed **14 ordinary cable variants, 17,408 shape states, 60 actual mining scenarios, and 696 checks**, with **zero failures**. Both redstone states of the splitter cable are included. Each survival case yielded exactly one cable item; each creative case yielded none. The normal release and verification artifact contain byte-identical copies of all 1,290 main classes.

A separate source copy restored only the legacy width argument and ran the final, unchanged harness. It reproduced shape mismatches for all 14 variants and a missed visible cable with matching client/server actor positions. This negative control is recorded in `validation/cable-legacy-width-control.json`; its expected failures are separate from the passing release results.

Build the verification artifact and launch it in an isolated instance with `-Dic2.verifyMigration=true -Dic2.verifyCables=true`. The instance needs a disposable existing world. Its real client runs the cable suite in the background, writes `ic2-cable-verification.json` to its game directory, and shuts down normally after completion. The test fixture is excluded from the normal artifact.

## Painter interaction fix (alpha.6)

The painter attempted to cast the client's `ClientLevel` to `ServerLevel` while charging durability, crashing during wool painting. A background real-client test using the alpha.5 painter reproduced the same `ClassCastException` reported from the full modpack.

Durability, conversion to an empty brush, and inventory refill now run only for a server player. The modern durability operation consumes a depleted stack, while vanilla entity-interaction cleanup clears the used hand if its original stack becomes empty. Damage is therefore evaluated on a copy before updating the held stack or installing its replacement, preserving the original item's identity for refill selection and preventing the returned brush from being cleared. Sheep painting uses the actual interaction hand.

The separate `PainterVerification` fixture sends real client block/entity interaction packets to an integrated server. It covers all 16 brush colors on wool and sheep with both hands, repeated same-color actions, creative durability immunity, and final-use behavior with refill disabled, no reserve, and one reserve. The client runs in the background, writes `ic2-painter-verification.json`, and shuts down normally. Build the verification artifact and select this suite with `-Dic2.verifyMigration=true -Dic2.verifyPainter=true` in a disposable instance with an existing test world.

The final painter run passed **80 actual client scenarios and 400 checks with zero failures**: 64 survival color/hand/target combinations, four representative creative cases, and 12 representative final-use/refill cases.

The alpha.4 machine/endurance and alpha.5 cable results remain separate historical evidence. The normal painter release shares all main class bytes with the tested verification artifact; only the painter and property-gated verification dispatch change class models from alpha.5. Painter results are recorded separately in `validation/painter-fix-audit.json`.

## Related tool and item-state fixes (alpha.7)

A follow-up audit traced client-reachable durability and world casts, stale hand selection, electric-tool charge changes, and mutations of copied item tags. It compared the migrated code with the original decompiled JAR and the current Minecraft item-interaction code. Guarded server-only calls, including the rubber-boots fall handler, wrench removal, debug acceleration, terraformers, chunk loading, and loot generation, retain their existing behavior.

Shared hand durability now uses the current living-entity/hand overload. `DamageHandler` rejects client entities before accessing a server level. Cable attacks and rubber-log stripping explicitly damage the main-hand tool; crowbar break callbacks use the interaction hand. Stripping resin drops only on the server. Chainsaw shearing checks readiness before consuming energy, uses the supplied stack for either hand, and keeps world, drop, and charge changes on the server. The shared electric-tool helper simulates availability on the client and leaves charge and shutdown packets to the server.

Nano-saber activation and shutdown now write the modified item data component. Exhausted armor is replaced with `ItemStack.EMPTY`, avoiding a null equipment entry that crashes entity ticking and saving. Armor discharge accepts the armor's own tier rather than the saber's tier, retaining the declared 48,000 EU nano and 300,000 EU quantum costs. Quantum-armor color changes and washing save the copied display compound while preserving charge and unrelated data. The obscurator saves invalid-reference cleanup and removes both legacy and current tint keys.

`ToolInteractionVerification` runs a background real client against an integrated server in a disposable world. The final run passed **74 scenarios and 514 checks with zero failures**:

- **54 real client packet scenarios:** treetaps and electric treetaps, cable insulation and removal, chainsaw sheep/leaf shearing, rubber-log stripping, nano-saber switches, and invalid obscurator scan resets. The suite covers both hands where the action permits it, ordinary/final durability, creative behavior, empty/exact charge, already-shorn sheep, disabled shearing, and stale offhand-use state.
- **Four client API probes:** durability and charge simulation without local item mutation.
- **16 server API probes:** ordinary/final durability, machine-owned wear without double consumption, saber shutdown, nano/quantum armor exhaustion, and all four quantum armor pieces' color, replacement, washing, codec roundtrip, and charge/data preservation.

Assertions allow collected resin or rubber to occupy a slot after its tool breaks, while requiring the tool itself to be consumed. Creative entity interactions operate on a vanilla item copy, so the original charge remains unchanged. These fixture corrections are documented separately from production fixes; failed exploratory runs are retained locally and are not counted as release passes.

The final alpha.6 control uses the same fixture source and byte-identical fixture classes. Only its property-gated verification dispatch differs from published alpha.6 main code. It reproduces client cast errors and copied-tag failures before the nano-armor probe installs a null equipment stack and crashes the integrated server. That control is an expected failure and does not complete the full suite. See `validation/tool-interaction-control.json` for its completed cases and fatal error.

The normal release shares all 1,290 main class bytes with the passing verification artifact. Assets and data remain byte-identical to alpha.6; the class audit accounts for the changed tool/item helpers and verification dispatch. Results and hashes are recorded in `validation/tool-interaction-fix-audit.json` and `validation/tool-interaction-fix-results.json`.

Select this separate suite with `-Dic2.verifyMigration=true -Dic2.verifyTools=true`. Quantum color and armor-exhaustion checks are explicitly API probes; they do not claim a visual armor-rendering test or a complete combat matrix. No bundled block implements `ICoverHolder`, so the crowbar hand correction is reviewed against the native durability API rather than exercised on an invented cover block. Earlier machine, cable, painter, multiplayer, and two-hour results remain evidence for their recorded versions.

## Reproduction and limits

Build the normal and separate verification artifacts with:

```sh
./gradlew build migrationVerificationJar
python tools/check_release_branding.py
python tools/migrate_assets_2612.py
python tools/audit_localization.py --output build/localization-audit
java tools/LanguageFormatProbe.java build/localization-audit/localization-format-input.tsv
```

Use the verification artifact only in isolated disposable instances. The harness source is in `src/verification`; startup properties select migration, all-machine, dedicated, and extended scenarios. A dedicated extended run needs two actual clients and their probe report paths. Full runtime evidence requires the game, test worlds, and client interactions in addition to compilation.

All registered machine types and the listed type/pair/boundary/representative layouts are covered. Arbitrary 54-slot permutations, arbitrary filter values, and arbitrary other-modpack interactions are not exhaustively enumerated. The release therefore remains alpha.

## Native API and complete item review (alpha.8)

The review inventories all 916 Java source files (90,910 lines) and checks all 1,293 compiled main classes against the pinned Minecraft/NeoForge class signatures. This is full-tree automated coverage with targeted source review and independent gameplay cases; it does not claim every line was manually inspected. The inventory includes 876 compiled runtime source files and 40 historical `ic2/data/**` generators explicitly excluded by the existing Gradle source set; those generators are not shipped. All 876 runtime source files match the normal sources archive byte-for-byte. Six descriptor differences are intentional IC2 helper/API overloads. The 68 item methods absent from the native item API were checked for explicit call sites or their replacement components/events.

The original 1.19.2 JAR and its decompiled implementations were used to check callbacks, equipment, drop/tool rules and asset mappings. Repairs include:

- Current block, entity, armor, effect, container and tool callback signatures; current vanilla tag directories; original tool-independent cable drops.
- Native armor assets, durability, enchantability, repair tags and rarity; charge-dependent protection; real damage drains worn armor without draining held spare armor.
- Partial/multi-slot item transfers, distinct neighbor/access directions, exposed sided slots, native item/per-slot component capacities, traversal past full limited slots, and owned fluid-cell replacement with exact commit/rollback semantics.
- Server-owned battery/crop-cell changes; seed planting in either hand without clearing the other hand; potato identity/age bounds; initial crop environment and preserved terrain/storage values.
- Grass ground/plant distinctions, biome quart coordinates, all four vertical quart entries, dirty-state persistence and biome-only client notification.
- Managed-menu chunk ordering, missing client tile reconstruction, actual server player detection for scanner data, and scanner clone queries at the scanned position.
- Real coffee duration/effects and registered radiation/cure callbacks; legacy material cells reject world-fluid placement safely while retaining their crop-chemical use.
- Independent nano-saber upkeep clocks, creative mining rules and native animation; directional upgrade and toolbox models; complete obscurator scan data, both hands, native UVs/tints and world wall-face geometry.

The item ledger distinguishes native client packets from native server menu operations and capability transactions. It never counts merely spawning/enumerating an item or receiving an output as using it. Plain materials also have actual recycler processing evidence. Radioactive resources have native inventory-effect evidence; the remote-interface upgrade is checked for rejection because upstream declares no compatible `RemotelyAccessible` machine. Legacy/classic objects retain their original profile limits.

Vanilla crafting-book entries and actual auto-placement are checked independently of IC2/JEI machine-recipe synchronization. `NOT_PLACEABLE` remains appropriate for machine recipe wrappers, which are not crafting-table recipes. The normal artifact retains the upstream empty external steam target; the verification artifact alone supplies a target to exercise repressurizer integration.

Failed exploratory runs and fixture mistakes remain in local archives and are excluded from passing coverage. Examples include a dead multiplayer actor, unsuitable crop light/nutrients, transient input keys, unhydrated test farmland, a fluid-generator fixture outside ticking chunks, stray fixture item pickups, an overstrict one-call bulk-transfer expectation, and an operator process-filtering error before endurance timing. All 1,293 final artifact classes match the endurance candidate byte-for-byte. Resource differences consist of the explicitly labelled verification-only steam target and one corrected toolbox tooltip in the simplified/traditional Chinese JSON catalogs and retained properties. These text changes were checked separately; all other runtime resources match.

Current detailed results and evidence provenance are recorded in `validation/full-review-audit.json` and `validation/item-usage-ledger.json`. Pair/stack matrices exercise documented finite dimensions; arbitrary layouts, arbitrary counts/components, and arbitrary third-party modpacks are not mathematically exhausted. Raw `Enchantment` objects in the legacy custom protocol remain unsupported; native ItemStack enchantment components use the native stack codec and do not enter those branches.

The final component-slot regression uses actual electric heat generators, electric kinetic generators and liquid heat exchangers. Capability inserts respect their one-item coil/motor/conductor limits, transaction abort restores contents, and repeated transfers reach all ten slots while conserving 64 supplied items. The two genuine defects are independently reproduced in `validation/component-slot-limit-audit.json`.

The final language review also replaces a remaining English traditional-Chinese toolbox tooltip and corrects the corresponding simplified-Chinese meaning. Both JSON and legacy properties now state that the item can be stored in a toolbox only while disabled. The endurance clients use the earlier catalogs; the corrected text passes the separate current-catalog/key/Java-format audit and changes no executable behavior. No identical English text containing a four-letter English word remains in either Chinese JSON catalog.

The final alpha.8 evidence records:

- **508/508 registered items used**, 186 native block-item placements, 372 native crafting-result operations, and 366 vanilla recipe-book cases.
- **104 machine types**, 219 behavior cases and 98 actual GUI entry points (96 machine-owned and two delegated reactor entry points). Integrated GUI timing counts client ticks; shared multiplayer timing counts actual rendered frames.
- **14 fresh runtime reports, 10,157 checks, zero failures**, including 237 primary functional item cases, 289 applicable standard machine recipes, 41 solid-canning recipes, and 239 native inventory/equipment/transaction checks.
- **Finite coal workflow**: 12 stages including native circuit crafting, 15,773 normal machine callbacks and 24 of the original 64 coal remaining. All 14 ordinary cable variants also deliver actual coal-generated EU into storage. Sprinting is confined to isolated processing fixtures and excluded from endurance wall time.
- **Native rendering**: all 508 registered item models, 36 directional cases and 26 source/flowing fluid models; cable regressions cover 17,408 shape states and 60 actual mining scenarios.
- **Finite matrices retained from passed r3 evidence**: 2,123 accepted applications across 11 upgrade types, 38 capable machine types and 171 global variants; 5,220 pair scenarios for 29 reactor components. The final r14 run independently reruns 24 advanced-filter and 16 deeper reactor cases.
- **Fresh real-time multiplayer endurance**: 7,200.039 seconds, 144,001 world ticks, 120 samples, two connected live clients throughout, approximately 20.00 TPS and zero failures. Competing native menu pickups conserve all 13 shared ingots.
- **Save/reload**: all 104 normally closed machine entities have valid persisted state; the normal JAR, without verification classes or JVM flags, reloads those 104 types and passes 16 additional inventory/fluid/ownership/mode checks. A unique r14 marker proves that the command results were freshly produced.
- **Release boundary**: all 1,293 executable classes match the tested candidate exactly; an independent source build has zero executable differences (23 debugger-local-name differences only), identical resources, no test/game classes and unchanged hashes for 4,864 protected Minecraft files. All 1,537 original sprites remain byte-identical.

The dedicated Windows logs include Netty kqueue/epoll platform-probing and Log4j DebugFile diagnostics for native transports supported on other operating systems. These contain no IC2 stack frames; both server startups, native TCP multiplayer and normal shutdowns succeed. Offline test-account/Realms errors and the earlier intentional-shutdown channel close are likewise classified separately in the log audit.

See [the alpha.8 audit](validation/full-review-audit.json), [machine results](validation/full-review-machine-results.json), [item ledger](validation/item-usage-ledger.json), [source inventory](validation/full-review-source-inventory.json), and [component-slot negative controls](validation/component-slot-limit-audit.json). The documented type/pair/stack/boundary dimensions are finite; arbitrary reactor layouts and third-party interactions remain outside exhaustive coverage. The repressurizer integration test supplies an external steam target only in the verification artifact; the normal artifact retains the upstream external-provider requirement.

## Java 25 hot-path review (alpha.9)

Alpha.9 replaces temporary constructor context with Java 25 constructor prologues, caches entity constructors with `ClassValue<MethodHandle>`, removes the encoder's global synchronized type-cache lookup, snapshots only mutated slots in the five exact bundled storage-box classes, and memoizes the native cable geometry calculator by its six connection bits. Other/custom containers retain whole-container rollback. Cable shapes preserve all 165,376 ordinary/foam outline, collision and occlusion states. The stress review also fixes disabled energy-tile entries hiding queued same-position replacements; an unchanged negative-control fixture distinguishes 56 alpha.8 lookup failures from 1,289 passing alpha.9 checks.

The [performance report](PERFORMANCE.md) and [machine-readable audit](validation/performance-audit.json) record eight independent forks on the same Java 25.0.2 runtime and hardware. The largest measured load has 2,048 machines, 4,096 cables and 512 native inventory transactions per tick. Median mean tick cost falls from 6.152 to 4.894 ms (20.4%); P99 falls from 10.718 to 6.849 ms (36.1%). All 38,400 paired measured ticks pass, and encoded bytes remain identical. A separate 100,000-tick sustained run commits 51.2 million measured transactions and processes real inputs without failure; it uses sprinting and lasts 251.138 real seconds in its measured phase, so it is not described as another two-hour run or included in the paired timing statistics.

All 104 machine types, 219 behavior cases and 98 actual GUI entry points are rerun, along with 186 survival placements, native cable mining, inventory/model checks and the coal-powered workflow. The targeted suites plus replacement regression pass 7,756 checks. The normal release's 1,295 game classes exactly match the tested candidate. An independent source build without the build cache has zero executable class differences, with 17 debugger local-variable-name differences only. Sources match all 876 included Java files; assets/data remain byte-identical to alpha.8; 4,864 protected Minecraft files remain unchanged. Verification fixtures are excluded from the release and sources JARs. The alpha.8 all-item ledger, reactor/upgrade matrices and two-client endurance evidence retain their original artifact provenance.

## Continued hot-path review (alpha.10)

Alpha.10 adds ordered candidate indexing for resolved vanilla recipe inputs, native-tag invalidation, synchronized cold-cache construction, a bounded immutable configuration-path cache and lazy cable-effect collections. Custom/component-sensitive inputs and custom holder sets retain repeated predicate evaluation and original ordering. It also repairs both machine-manager replacement overloads and list-manager replacement to remove the matched existing input. The original supplied JAR bytecode confirms the old cache-removal mistake; identical controls reproduce three bounded replacement failures on alpha.9 and zero on alpha.10.

The [alpha.10 report](PERFORMANCE_ALPHA10.md) and [audit](validation/performance-alpha10-audit.json) record eight independent Java 25 forks, 120,000 paired measured ticks and a separate 100,000-tick sustained run with 51.2 million measured transactions. At 2,048 machines and 4,096 cables, mean tick cost falls by 26.4%, P99 by 26.0% and server-thread allocation by 28.4% against freshly measured alpha.9. The longer 2,000/5,000-tick schedule is applied to both sides; historical alpha.9 percentages are not compounded into this comparison.

Final-code native suites pass 2,728 checks: 104 machine types, 219 behavior cases, 98 actual GUI entry points, 289 applicable normal-tick processing recipes, the 12-stage coal workflow and 14 ordinary cable EU variants. One external silver-ore recipe is unavailable in the isolated registry. The 1,011 hot-path controls include two actual resource reloads, 80,000 concurrent recipe reads, 80,000 concurrent configuration reads and 33 energy-effect scenarios using instrumented API tiles and actual native living entities. These checks have explicit finite scope and do not enumerate arbitrary network or third-party layouts.

All 1,296 release classes match the final measured and native-client artifacts. A clean source build has zero executable differences (21 debugger local-name differences only); all 876 included Java sources match. Assets/data are byte-identical to alpha.9, 1,537 original sprites are preserved and 4,864 protected Minecraft files remain unchanged. Initial invalid fixture runs and the pre-concurrency timing pilot are archived and excluded. Earlier 508-item, upgrade/reactor and two-hour multiplayer results retain their original version provenance.
