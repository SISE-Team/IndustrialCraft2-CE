# Continued performance review (alpha.10)

Alpha.10 reduces recipe-search work and temporary allocations in machine/energy ticks. At the largest isolated native workload, median mean tick cost falls by **26.4%**, P99 by **26.0%**, and server-thread allocation per tick by **28.4%** compared with alpha.9. Both versions run on the same Java 25.0.2 runtime. These are implementation improvements, not a Java-version comparison.

The review also fixes three recipe-replacement paths that could loop indefinitely when a distinct input object replaced an existing matching recipe. An unchanged, bounded fixture reproduces all three failures on alpha.9 and passes on alpha.10. Inspection of the supplied original 1.19.2 JAR bytecode confirms the incorrect cache-removal input in the original implementation as well.

## Changes and reasoning

The earlier alpha.9 sustained JFR recording identified `RecipeInputIngredient.matches`, configuration-path parsing and cable-effect collection allocation as remaining hot paths. Of 20,793 execution samples inside that stress phase, 8,708 first entered IC2 through ingredient matching. Allocation weights are sampled estimates; that phase also includes fixture maintenance outside the tick timing boundary. The actual comparison below uses separate exact server-thread allocation counters.

| Area | Change | Behavior retained |
| --- | --- | --- |
| Deferred recipe inputs | Cache an ordered candidate list per item, filtering only resolved vanilla item/tag ingredients | Existing direct-cache priority, fallback ordering, and a fresh actual predicate match on every query |
| Recipe invalidation | Clear candidates on insertion/removal and observe a volatile epoch on native tag reload | Both positive and negative tag results are refreshed; retired managers do not accumulate in a global listener registry |
| Concurrent recipe reads | Synchronize candidate construction and cache invalidation per manager | Safe publication when independent readers encounter a cold candidate cache; arbitrary concurrent recipe-map mutation is not introduced as a new API guarantee |
| Configuration reads | Keep one immutable parsed-path record with volatile publication | Sections and values are read afresh, including after direct mutation, removal and reinsertion; the cache retains only one path |
| Cable effects | Allocate removal, stripping, sink-overload and shock collections only when an effect is actually recorded | Existing voltage thresholds, packet maxima, shock arithmetic, identity semantics and effect order |
| Recipe replacement | Remove the matched old input from the recipe map and caches | Both machine-manager overloads and list-manager replacement terminate and install the requested recipe |

The candidate index only excludes a recipe when its already-resolved delegate is the exact standard `RecipeInputIngredient` class and its noncustom ingredient has an exact vanilla direct/named holder set. Custom ingredients, custom holder sets, component-sensitive predicates and unresolved lazy inputs remain in their original order and are matched again. Creating the index does not force unresolved registry access. Each manager holds at most one candidate list per queried item; lists are discarded on cache invalidation.

The configuration cache uses an established Java record and immutable list. No additional preview feature or JVM flag is required. Energy propagation and world mutation keep their existing execution model. Comparator and neighboring-block notifications were inspected but retained because removing them would require separate observable-behavior evidence. This round does not attribute improvements to the unchanged factory, codec or storage-journal implementations from alpha.9.

## Paired native measurement

Eight independent processes run in paired **ABBA / BAAB** order: four alpha.9 baseline forks and four alpha.10 forks. All 75 compiled fixture classes are identical on both sides. Each candidate's normal release classes remain byte-identical in its measured artifact.

- AMD Ryzen 7 9800X3D, eight cores / 16 logical processors; Windows.
- Azul Java 25.0.2, G1, fixed 4 GiB initial/maximum heap, 16 active processors.
- Compact object headers disabled and JFR `profile` enabled on both sides.
- Same isolated NeoForge 26.1.2.114 server, Minecraft 26.1.2 and flat-world seed.
- Three tiers: 128, 512 and 1,024 independent circuits. Each circuit has one creative generator, four tin cables and one eight-overclocker macerator.
- Actual placed machines receive EU, consume recipe inputs and produce outputs. Each tick also commits 512 native transactions across the five standard storage-box types.
- Every tier receives **2,000 warmup ticks and 5,000 measured ticks**, totaling **120,000 paired measured stress ticks**.

Sprinting removes the normal 20 TPS ceiling. Wall-clock tick cost, server-thread CPU and server-thread allocated bytes use the same native pre/post tick boundary. Fixture input/output maintenance after that boundary is excluded. Test clients exit before the paired experiment; no build or other test load is launched alongside it. Ordinary OS/background activity is still present.

Values below are medians of four independent forks per version. Lower is better; MB is decimal.

| Metric | alpha.9 | alpha.10 | Reduction |
| --- | ---: | ---: | ---: |
| 256 machines + 512 cables, mean tick cost | 0.869 ms | 0.715 ms | 17.7% |
| 1,024 machines + 2,048 cables, mean tick cost | 1.897 ms | 1.340 ms | 29.4% |
| 2,048 machines + 4,096 cables, mean tick cost | 4.295 ms | 3.161 ms | 26.4% |
| Largest workload, P95 tick cost | 5.874 ms | 4.389 ms | 25.3% |
| Largest workload, P99 tick cost | 6.899 ms | 5.108 ms | 26.0% |
| Largest workload, server-thread CPU/tick | 4.091 ms | 2.977 ms | 27.2% |
| Largest workload, server-thread allocation/tick | 3.415 MB | 2.446 MB | 28.4% |

The largest workload's per-fork ranges show process variability:

| Metric | alpha.9 range | alpha.10 range |
| --- | ---: | ---: |
| Mean tick cost (ms) | 4.209–4.391 | 3.144–3.165 |
| P99 tick cost (ms) | 6.506–7.166 | 5.022–5.184 |
| Allocation/tick (MB) | 3.257–3.433 | 2.365–2.529 |

The [machine-readable audit](validation/performance-alpha10-audit.json) retains every fork's metrics, adjacent-pair differences, a limited four-pair bootstrap interval, raw-report hashes and artifact hashes. Four pairs on one workstation do not establish a universal confidence bound. The old [alpha.9 report](PERFORMANCE.md) remains a separate alpha.8-to-alpha.9 experiment with a different warmup schedule; its percentages are not multiplied into this result.

All forks agree on registry/state fingerprints, all **165,376 ordinary/foam cable shape states**, and encoded-byte digests. Processing, powered-machine counts, conservation and transaction rollback checks pass. Slightly different processing totals are expected from native scheduling; every run must independently demonstrate real processing and power rather than merely constructing machines.

## Regression and sustained operation

The final alpha.10 executable code passes **2,728 targeted checks**, with zero failures:

| Suite | Checks | Scope |
| --- | ---: | --- |
| Native machine client | 1,361 | 104 machine types, 219 behavior cases, 98 actual GUI entry points |
| Native processing | 290 | 289 applicable standard recipes executed by placed machines during normal ticks |
| Coupled workflow | 66 | 12 coal-powered production/crafting stages and real EU delivery through all 14 ordinary cable variants |
| Recipe/configuration/effect controls | 1,011 | Ordered matching, invalidation, replacement, concurrent reads, native reload and energy effects |

The external silver-ore macerator recipe has no registered ingredient in this isolated mod set and is explicitly recorded as unavailable, rather than counted as an executed recipe. All 289 applicable cases pass.

The 1,011 control checks comprise 485 recipe-candidate checks, five replacement checks, 242 configuration checks, 271 energy-effect checks and eight reload checks. They include 80,000 concurrent cold-cache recipe queries from eight platform workers, 80,000 concurrent read-only configuration queries, a dynamic custom holder set and component-sensitive predicates. Two actual server resource reloads change item-tag membership, invalidate retained-manager positive/negative candidates and replace the native machine-recipe manager.

Energy effects cover 33 finite threshold/setting/shared-path scenarios. They invoke the production effect method with real grid/path objects, instrumented IC2 energy API tiles and actual living entities in loaded server chunks. They check cable removal versus stripping, sink-overload callbacks, shared-path maximum/summed shock behavior and near/far entity damage. These API/effect scenarios are distinct from the placed ordinary-cable EU workflow; they do not claim every possible network topology or explosion outcome was enumerated.

An unchanged baseline control reports three expected replacement failures, with lookup bounded to 64 calls so the old infinite loops cannot hang the server. Its other controls pass. Initial exploratory fixture mistakes (legacy custom-name matching assumptions and querying entities before their chunks became accessible) were corrected and excluded. A timing pilot made before the final concurrency correction is also excluded; all eight published forks use the final code and final fixture.

A separate final alpha.10 process completes **100,000 measured ticks** after 2,000 warmup ticks, with 2,048 machines, 4,096 cables and **51.2 million measured committed transactions**. It produces **12,208,320 processing outputs including warmup**, has positive EU in all 1,024 macerators at completion, passes conservation and shuts down normally. Its measured phase lasts **137.733 real seconds** with sprinting enabled. This longer run is excluded from the paired timing statistics and is not described as another two-hour run.

JFR records 26 G1 collections in that measured phase, 212.684 ms total GC pause and 37.447 ms maximum pause. After-GC heap samples range from 359.1 to 371.6 MB. These finite samples do not prove absence of every long-term leak.

## Release boundary and reproduction

The normal release contains **1,296 main classes**, all byte-identical to the final measured candidate and final native client regressions. An independent clean source build with no build cache has **zero executable class differences**; 21 debugger local-variable-name differences are normalized only for comparison. All 876 included Java source files match the sources archive. All 5,786 asset/data entries remain identical to alpha.9, all 1,537 original sprites are preserved, and all 4,864 protected Minecraft files retain their hashes. Verification classes and test-only external-steam targets are excluded from the release.

- Normal JAR SHA-256: `fad86afd76ad31241aedf582a41a8efc9dd45a7d5ab200cee8e2efb0fcf666b6`
- Sources JAR SHA-256: `8d0a5688ce33f2df68ba9265f9605b089b890598252fce7f0bbb5d8ce0533201`

Build the normal and separate verification artifacts with `./gradlew build migrationVerificationJar`. Install the verification artifact only in a disposable native server. The performance fixture is selected with:

```text
-Dic2.verifyMigration=true
-Dic2.verifyAllMachines=true
-Dic2.verifyPerformance=true
-Dic2.perfScales=128,512,1024
-Dic2.perfWarmupTicks=2000
-Dic2.perfMeasuredTicks=5000
-Dic2.perfTransactionsPerTick=512
```

Use a fresh process and world for each fork; hold Java, heap, GC, processor count, instrumentation and machine layout constant. The fixture writes `ic2-performance-verification.json` and shuts down normally. For the separate sustained run, set `ic2.perfScales=1024` and `ic2.perfMeasuredTicks=100000`. For the semantic controls, replace the performance selector with `-Dic2.verifyHotpaths=true`; that suite writes `ic2-hotpath-regression.json` and performs two actual resource reloads. The ordinary machine/processing and coupled-workflow suites remain separate native-client runs.

This is an isolated IC2 automation workload, not a full-modpack FPS measurement. The previous complete 508-item ledger, upgrade/reactor matrices and two-client/two-hour run retain their historical artifact provenance. The release remains alpha; arbitrary addon predicates, world sizes and other-modpack interactions have not been exhaustively enumerated.
