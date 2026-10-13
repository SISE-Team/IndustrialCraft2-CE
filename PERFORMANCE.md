# Java 25 performance review (alpha.9)

Alpha.9 reduces work in block construction, network type classification and native inventory transactions. Both sides of the comparison run **the same Java 25.0.2**, so these measurements describe code changes rather than a JDK upgrade.

## Changes and rationale

| Area | Change | Intended effect |
| --- | --- | --- |
| Block state initialization | Initialize callback-visible fields before `super(...)`; remove the temporary `ThreadLocal` and `InitData` carrier | Avoid construction-context lookups/allocation and make constructor callbacks see initialized fields |
| Block entity factories | `ClassValue<MethodHandle>` caches adapted public constructors | Avoid repeated reflective constructor lookup and temporary parameter arrays |
| Network encoder | `ClassValue<EncodedType>` and concurrent addon lookup | Remove the global synchronized type-cache lookup while preserving type IDs and lookup precedence |
| Standard storage boxes | Lazy per-slot `SnapshotJournal<ItemStack>` | Copy only modified slots while retaining native nested commit/rollback behavior |
| Cable geometry | Memoize the native constructor calculator by its six connection bits | Build at most 64 geometry variants per cable; preserve the native state lookup and later general shape-calculator behavior |

Java 25's permanent [flexible constructor bodies](https://docs.oracle.com/en/java/javase/25/language/flexible-constructor-bodies.html) allow the first change. `ClassValue` and method handles are established Java APIs, not new Java 25 features; the [ClassValue contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ClassValue.html) fits class-dependent immutable cache data. No preview/incubator feature or additional JVM flag is required by the release.

Sparse journals apply only to the five exact bundled storage-box classes. Custom containers and other inventories retain the whole-container journal because their setters may have cross-slot effects. Nested transactions and a custom coupled-container rollback are exercised with the actual NeoForge transaction implementation.

The review also exposes a correctness problem during immediate replacement: a queued removal disables an energy tile before its map entry is deleted. Public tile lookup now hides that disabled entry and resolves queued changes, so a new cable can register at the same position. This is a lifecycle correction; voltage, losses, distribution and reactor arithmetic are unchanged.

## Method

Eight independent JVM forks run in paired **ABBA / BAAB** order: four alpha.8 baseline forks and four alpha.9 forks. Identical compiled fixture classes are attached to byte-identical copies of each version's release classes. Exploratory failures are archived and excluded from these statistics.

- AMD Ryzen 7 9800X3D, eight cores / 16 logical processors; Windows.
- Azul Java 25.0.2, G1, fixed 4 GiB initial/maximum heap, 16 active processors.
- Compact object headers disabled on both sides; JFR `profile` enabled on both sides.
- Same flat-world seed and isolated native NeoForge server; 128, 512 and 1,024 independent circuits.
- Every circuit contains one creative generator, four tin cables and an eight-overclocker macerator. Inputs are consumed and outputs are produced by actual machine/energy callbacks.
- 512 committed native storage transactions per tick, distributed over the five storage-box types.
- Each tier receives 600 warmup ticks and 1,600 measured ticks: **38,400 measured stress ticks** across the eight forks. All 128/512/1,024 macerators receive real EU in every passing phase.

Sprinting removes the 20 TPS ceiling. Wall time, server-thread CPU and allocated bytes use the same native pre/post tick boundary. Input/output maintenance after that boundary is excluded. Microbenchmarks use warmed production methods, observe mutations/output and consume encoded bytes through a bulk SHA-256 sink. The codec numbers are aggregate work across platform threads, not network socket bandwidth.

## Results

Values below are medians of four independent forks per version. Lower values are better. Per-fork ranges, paired differences and the limited four-pair bootstrap interval are recorded in [the performance audit](validation/performance-audit.json).

| Metric | alpha.8 | alpha.9 | Reduction |
| --- | ---: | ---: | ---: |
| 256 machines + 512 cables, mean tick cost | 1.488 ms | 0.857 ms | 42.4% |
| 1,024 machines + 2,048 cables, mean tick cost | 2.692 ms | 1.907 ms | 29.2% |
| 2,048 machines + 4,096 cables, mean tick cost | 6.152 ms | 4.894 ms | 20.4% |
| Largest workload, P95 tick cost | 8.401 ms | 6.005 ms | 28.5% |
| Largest workload, P99 tick cost | 10.718 ms | 6.849 ms | 36.1% |
| Largest workload, server-thread allocation/tick | 6.269 MB | 4.315 MB | 31.2% |
| 126-slot box transaction | 3.019 µs | 0.801 µs | 73.5% |
| 126-slot box allocation/transaction | 7,952 B | 408 B | 94.9% |
| Entity creation across 146 registered factories | 0.964 µs | 0.866 µs | 10.2% |
| Entity creation allocation | 893.9 B | 762.6 B | 14.7% |
| Encoding + digest, one worker | 651.8 ns/op | 572.1 ns/op | 12.2% |
| Encoding + digest, four workers | 833.4 ns/op | 162.4 ns/op | 80.5% |
| Encoding + digest, eight workers | 995.0 ns/op | 95.2 ns/op | 90.4% |

All forks have identical registry/state fingerprints, identical outline/collision/occlusion fingerprints for **165,376 cable states including foam**, and identical encoded-byte digests. Native processing, powered-machine counts, transaction conservation and nested rollback checks pass throughout.

## Sustained stability and regression

A separate alpha.9 process completes **100,000 measured ticks**, after 1,200 warmup ticks, with 2,048 machines, 4,096 cables and 512 transactions per tick. It commits **51.2 million measured transactions**, produces 12,111,664 processing outputs including warmup, records positive EU in all 1,024 macerators at completion, passes conservation checks and shuts down normally. The measured phase lasts **251.138 real seconds** with sprinting enabled. Its longer warmup/measurement and concurrent background activity differ from the paired experiment; its timing is excluded from the comparison above.

JFR records 62 G1 collections during that measured phase, 253.852 ms total GC pause and a 37.913 ms maximum GC pause. After-GC heap samples range from 353.2 to 368.0 MB. These are finite phase samples, not a proof that arbitrary long-running worlds cannot leak.

Targeted native client/server regressions pass **7,756 checks**: all 104 machine types, 219 behavior cases, 98 actual GUI entry points, 186 survival placements, item models, inventory transfers, cable mining, a coal-powered workflow and same-tick replacement. An unchanged replacement fixture reports 56 expected immediate-lookup failures on alpha.8 and zero failures across 1,289 checks on alpha.9. Both versions can resume later EU delivery; the control distinguishes immediate lookup from eventual recovery.

The normal alpha.9 JAR has 1,295 game classes byte-identical to the measured candidate, no verification classes, and 5,786 asset/data entries identical to alpha.8. An independent source build with no build cache has zero executable class differences; 17 debugger local-variable-name differences are normalized only for comparison. The published sources match all 876 included Java source files. All 4,864 protected Minecraft files and 1,537 original sprites retain their hashes. The previous two-client/two-hour run and complete 508-item usage ledger remain alpha.8 evidence.

## Limits and reproduction

This is a repeatable synthetic automation load in an isolated IC2 server on one workstation with ordinary background OS activity. It is not a measured FPS improvement for the complete modpack. Baseline and optimized forks vary; the audit preserves their ranges. JFR allocation weights are sampled estimates, while the reported per-thread allocation counters are measured independently.

CPU-bound tick/energy work remains on its existing execution model. Virtual threads are useful for waiting workloads and do not justify moving Minecraft world mutations off their owning thread. Scoped values, preview stable values, structured concurrency and incubating vectors are not introduced into these hot paths. Compact object headers and different GC settings would require a separate runtime comparison.

Build `migrationVerificationJar` and use it only in a disposable native NeoForge server with:

```text
-Dic2.verifyMigration=true
-Dic2.verifyAllMachines=true
-Dic2.verifyPerformance=true
-Dic2.perfScales=128,512,1024
-Dic2.perfWarmupTicks=600
-Dic2.perfMeasuredTicks=1600
-Dic2.perfTransactionsPerTick=512
```

The fixture writes `ic2-performance-verification.json` and stops the server normally. `-Dic2.verifyCableReplacement=true`, instead of the performance selector, runs the separate same-tick replacement regression. The fixture sources, raw timing-array hashes, artifact hashes and regression provenance are kept separate from the normal release JAR.
