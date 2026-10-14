# Alpha.12 crop performance

Alpha.12 removes the measured crop mean-time regression in the tested unattended forced farms. It groups due phases by world and avoids native idle per-block tickers when the world has no players. A dirty queue retains persistence and state changes; player arrival restores native per-block tickers before the block-entity phase. Growth remains on the server world thread, uses the same phase/cadence and retains native chunk, entity-loaded, world-border and tick-freeze admission.

The production baseline is the original alpha.10 port, SHA-256 `fad86afd76ad31241aedf582a41a8efc9dd45a7d5ab200cee8e2efb0fcf666b6`. Candidate production SHA-256 is `fa6b2c89e13bd70474fa15d5527125ef1c21ee542ee11bb9d20a8e21edd8ab85`. Each artifact runs three fresh JVMs with Java 25, G1, 4 GiB heaps and 16 active processors in B1/A1/A2/B2/B3/A3 order. Compact headers are disabled. Benchmark classes are byte-identical to the accepted alpha.11 observers, whose instructions match the baseline after namespace normalization. All forks check exactly 16 growth callbacks per plot, totaling 1,277,952 callbacks across 73,728 measured ticks. Each scale has 1,024 warmup ticks and 4,096 measured ticks.

| Crop plots | Alpha.10 mean ms/tick | Alpha.12 mean ms/tick | Mean change | Allocation change | P99 change |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1,024 | 0.05187 | 0.01707 | -67.09% | -50.42% | -55.13% |
| 4,096 | 0.17240 | 0.01725 | -90.00% | -83.78% | -79.46% |
| 8,192 | 0.33996 | 0.01650 | -95.15% | -91.20% | -91.66% |

These results apply to unattended forced farms with no online players, matching the established fixture. They do not measure the observed-player path, full-modpack FPS or every world arrangement. A real TCP client transition test passes 51 checks: unattended and observed growth counts, native ticker restoration, full client phenotype and care state, freezing, logout and actual block removal. Native regression (9,584 checks), physical planting (334 checks) and coupled workflow (66 checks) pass separately. Allocation is newly allocated objects per tick, not total retained heap. Raw fork ranges and every result are in [the comparison](validation/crop-performance-alpha12.json).

Initial allocation-only and per-plot scheduling changes did not eliminate the regression. Their private raw forks remain retained; they are not pooled into these final medians. [Alpha.11 performance](PERFORMANCE_ALPHA11.md) preserves the prior positive and unfavorable results.

## Connected circuit regression comparison

The final alpha.12 artifact also ran the six fresh JVM circuit benchmark with identical Java and transaction settings. All correctness and conservation gates passed. The 128 / 512 / 1,024 circuit mean changes against alpha.10 are -22.01% / -7.05% / -12.51%. This separate comparison checks the shared world and native ticker changes; it is not a controlled before/after measure of the crop patch alone. [Complete circuit data](validation/circuit-performance-alpha12.json) retains every fork and all metrics, including construction and inventory allocation regressions.
