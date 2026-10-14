# Alpha.11 performance comparison

Alpha.11 reduces average tick cost and allocation in the measured connected processing/energy circuits. Crop allocation also falls, while crop mean timing and native entity construction show regressions. This report preserves both outcomes and does not claim a universal speedup.

## Artifacts and method

The production baseline is alpha.10 (`fad86afd76ad31241aedf582a41a8efc9dd45a7d5ab200cee8e2efb0fcf666b6`). The production candidate is alpha.11 (`07b4bfca99dc6e57c6c2292bdc17c2d6ad25e16c03beb6093965233ae2d83c2c`). Benchmark fixtures are separate: baseline circuit fixture `7ef3f005eb7530576bbf0c45c7e05c7c6ddc010d5512f01028d0112cfd3e0b0b`, baseline crop fixture `2f7c2d82f34fdf0bfaceee582c795e1c151b2125e18119e8ac5f01828580823b`, candidate fixture `727677479a229d3f3fde90ebefbcaf17a4cb59e839e9b7e58abb4207ba1fe83f`. Each baseline fixture preserves all alpha.10 production classes. Candidate production classes match the delivered normal JAR.

Each comparison uses three fresh JVMs per artifact in balanced B1/A1/A2/B2/B3/A3 order, Java 25.0.2, G1, 4 GiB heaps, 16 active processors, compact headers disabled and JFR. Owned gameplay clients, compilation and the endurance server were closed during these final measurements. Unrelated user applications were left running and background process counts were recorded. Benchmark instructions match after explicitly documented namespace and constant-pool-number normalization. Every fork passed correctness, conservation and quantity gates. Values are medians of the three forks, with full ranges and raw fork reports in the linked JSON.

## Connected circuits

One circuit contains one generator, four cables and one upgraded macerator. Each scale runs 600 warmup ticks and 1,600 measured ticks, including 512 committed native inventory transactions per tick. MSPT below describes this fixture workload.

| Circuits (machines / cables) | Baseline mean ms | Alpha.11 mean ms | Mean change | Allocated bytes/tick change | P99 change |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 128 (256 / 512) | 0.61225 | 0.44596 | -27.16% | -38.92% | -25.05% |
| 512 (1,024 / 2,048) | 1.19334 | 1.06035 | -11.14% | -18.54% | -11.51% |
| 1,024 (2,048 / 4,096) | 4.36667 | 3.56951 | -18.26% | -21.92% | +3.31% |

The 1,024-circuit P99 is 3.31% higher; its fork ranges overlap (baseline 5.39–8.04 ms, candidate 5.44–7.56 ms). Mean cost improves at every tested scale. These are server workloads, not full-modpack FPS measurements.

## Crop plots

Real forced farm chunks contain 1,024, 4,096 or 8,192 crop plots. Each scale runs 1,024 warmup ticks and 4,096 measured ticks. An identical bounded species observer verifies exactly 16 growth callbacks per plot: 1,277,952 callbacks across 73,728 measured ticks. Random block ticks are disabled equally to avoid unrelated soil/age changes.

| Crop plots | Baseline mean ms | Alpha.11 mean ms | Mean change | Allocated bytes/tick change | P99 change |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1,024 | 0.05434 | 0.06176 | +13.65% | -24.34% | -7.42% |
| 4,096 | 0.17411 | 0.17923 | +2.94% | -51.63% | -0.52% |
| 8,192 | 0.35114 | 0.35310 | +0.56% | -56.55% | +8.09% |

Crop allocation improves by 24.34–56.55%, but mean cost increases by 13.65% at the smallest scale and 2.94%/0.56% at the larger scales. The larger mean ranges overlap. At 8,192 plots, P99 increases by 8.09%. Lower observed maximum ticks are retained in the raw data; maxima depend on GC and OS scheduling and do not establish general tail-latency improvement.

## Inventory, construction and codec

| Native workload | Baseline ns/op | Alpha.11 ns/op | Time change | Allocation change |
| --- | ---: | ---: | ---: | ---: |
| Inventory, 27 slots (case 1) | 936.23 | 717.61 | -23.35% | +2.19% |
| Inventory, 45 slots (case 2) | 1009.56 | 731.19 | -27.57% | +7.03% |
| Inventory, 45 slots (case 3) | 787.85 | 575.46 | -26.96% | +0.00% |
| Inventory, 63 slots (case 4) | 675.58 | 528.60 | -21.76% | +0.00% |
| Inventory, 126 slots (case 5) | 718.79 | 546.13 | -24.02% | +0.00% |
| Factory, 146 entity types | 766.58 | 873.81 | +13.99% | +1.05% |
| Codec, 1 workers | 417.52 | 137.87 | -66.98% | -100.00% |
| Codec, 4 workers | 165.29 | 36.92 | -77.66% | -100.00% |
| Codec, 8 workers | 94.06 | 20.72 | -77.97% | -100.00% |

Native entity construction is 13.99% slower with 1.05% more allocation. The first two inventory cases allocate 2.19% and 7.03% more despite lower measured time. Other inventory allocation is unchanged. Codec allocation changes from 152.0004 to 0.0004 bytes/op in this isolated descriptor workload; world callbacks remain synchronous.

## Changes and evidence

The final recipe path caches immutable material/tag descriptors rather than rebuilding and weakly interning keys on each candidate test. Candidate pruning resolves known item/tag-only predicates early; arbitrary component/custom predicates still execute live. Actual tag publication invalidates both positive and negative candidate generations. Native regression tests exercise real tag reloads. Sparse committed inventory journals, primitive graph workspaces and bounded binary codec plans address other measured hot paths. Crop scheduling coalesces persistence notifications and caches immutable field/biome lookup plans. Java 25 features support these implementations; the results do not isolate the effect of the Java version itself.

The first final candidate regressed at the larger circuit scales. JFR identified repeated tag construction, and [its measured regression comparison](validation/rewrite-performance-pre-tuning.json) remains separate. Only the final artifact above is used for these tables. Historical candidates are not pooled.

[Circuit and microbenchmark data](validation/rewrite-performance.json), [crop data](validation/rewrite-crop-performance.json) and [CSV metrics](validation/rewrite-performance.csv) preserve every final fork and unfavorable result. [Earlier partial rewrite measurements](docs/history/PERFORMANCE_REWRITE_PARTIAL.md) and [alpha.10 measurements](PERFORMANCE_ALPHA10.md) remain attributed to their own builds. The final two-hour gameplay requirement was withdrawn; the final tuned connected short run is reported separately. Closed-world normal-JAR reload remains a separate acceptance gate.
