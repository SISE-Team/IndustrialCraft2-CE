# Partial rewrite performance

These measurements describe an exact frozen intermediate implementation. The whole-project rewrite remains incomplete. Baseline production bytes match alpha.10 SHA-256 `fad86afd76ad31241aedf582a41a8efc9dd45a7d5ab200cee8e2efb0fcf666b6`; the candidate private verification artifact is `4b8e240668abf33c857abd0e9515a81f5435279efcc0457babc40cfec595387b`.

Six separate Java 25.0.2 JVMs run in balanced order B1/A1/A2/B2/B3/A3, with G1, 4 GiB heaps, 16 active processors and compact object headers disabled. Five benchmark classes have identical bytecode instructions after documented symbol and constant-pool-number normalization. All forks passed native quantity, checksum and correctness gates.

Each circuit contains a generator, four cables and an upgraded macerator. Each scale runs 600 warm-up ticks, 1,600 measured ticks and 512 real inventory transactions per tick. Values below are medians across the three independent forks of each artifact. Allocation is measured on the native server thread.

| Circuits | Baseline mean MSPT | Candidate mean MSPT | Time change | Allocation change |
|---:|---:|---:|---:|---:|
| 128 | 0.59026 | 0.44613 | -24.42% | -39.85% |
| 512 | 1.24180 | 1.05076 | -15.38% | -19.68% |
| 1,024 | 3.89095 | 3.21351 | -17.41% | -21.98% |

The multiplayer r7 server and owned GUI test clients ran concurrently. These samples are partial scope evidence and do not establish a universal speedup or final release timing. Exact fork ranges and percentile/CPU measurements are included in the machine-readable report. Prior r5/r6 results are preserved separately; different candidates and background conditions are not pooled.

Construction of 146 native block entity types changed from 848.14 to 896.82 ns/operation (+5.74%). Allocation changed from 767.27 to 797.47 bytes/operation (+3.94%). Construction allocation remains an optimization target. Native codec timings at 1/4/8 workers improved by 70.68%/66.80%/77.83%; that isolated path changed allocation from 152.0004 to 0.0004 bytes/operation.

The candidate includes automatic/UU crafting, stable native grid views, owned personal inventories and transactional item transfer/capability work, with exact-type quantity updates preserving rollback. Subsequent manual-workbench and agriculture changes are outside this frozen artifact. Final acceptance still requires the complete implementation, every gameplay suite, multiplayer/long-run checks and a paired measurement without concurrent owned workloads.

`validation/rewrite-performance-partial.json` contains exact artifacts, medians, fork ranges and all five native inventory sizes. Normal and source distribution archives contain no destructive verification classes.

## Provisional crop pressure comparison

Six fresh native servers place 1,024, 4,096 and 8,192 real crop blocks. Each scale uses 1,024 warmup ticks and 4,096 measured ticks. A high-duration species observer confirms exactly 16 growth calls per plot; all 1,277,952 measured callbacks across both artifacts passed. Three observer classes have identical normalized bytecode. Every baseline production class remains byte-identical to alpha.10.

The frozen baseline fixture is `2f7c2d82f34fdf0bfaceee582c795e1c151b2125e18119e8ac5f01828580823b` and the frozen candidate is `3a92cc83813b2033c1cf8768cbf40a673fe5512b8ebfdcf394544d89ed2a7aa6`. The candidate includes cached phases, field-specific updates, coalesced persistence notifications and weak biome bonus caches invalidated on native tag publication. Later species bodies are outside this artifact.

| Crops | Baseline mean MSPT | Candidate mean MSPT | Time change | Allocation change | Maximum-tick change |
|---:|---:|---:|---:|---:|---:|
| 1,024 | 0.05433 | 0.05850 | +7.66% | -25.76% | -79.23% |
| 4,096 | 0.17534 | 0.17678 | +0.82% | -51.68% | -94.29% |
| 8,192 | 0.33838 | 0.34638 | +2.36% | -56.37% | -87.63% |

Mean timing is approximately unchanged at the two larger sizes and slightly slower at the smallest size. Allocation falls substantially. Phased growth reduces the maximum tick in these three-fork medians, but maxima are sensitive to GC and OS scheduling; this is not a claim about universal tail latency. The first implementation had significant average-time and allocation regressions; its failed performance evidence is retained separately.

Owned gameplay tests and compilation ran during part of the comparison. All fork ranges and percentiles are in `validation/rewrite-crop-performance-partial.json`. Whole-project replacement and final quiet acceptance remain pending.
