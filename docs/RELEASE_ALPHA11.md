# IndustrialCraft2-CE alpha.11

Alpha.11 replaces the earlier port runtime with independently written Java 25 implementations and NeoForge controllers. The project separates the JDK-only engine, addon API, native platform, retained assets and isolated verification fixtures. Machine families keep distinct timing, resource and world-effect rules. Runtime Java packages are `ic2ce`; addons recompile against `ic2ce.api`. Registry/resource IDs and save fields keep the `ic2` namespace.

The source inventory contains 911 runtime Java files: 755 implementations and 156 reviewed declarations. Retained declarations do not count as rewritten business logic. Original IC2 code is behavioral reference only. All 1,537 original sprite/metadata files remain exact through migrated paths; 24 explicitly reviewed resource repairs add missing progression/narration keys and correct the advancement background ID.

Native verification covers actual item use, machine placement and behavior, processing recipes, GUI/JEI navigation, cables, pigment tools, equipment, upgrades, reactors, lifecycle, disk restart and normal-JAR reload. The final two-hour requirement was withdrawn; connected short-run evidence and native pressure comparisons retain separate scopes. Exact completion and coverage are recorded in [the acceptance index](../validation/rewrite-acceptance.json).

Three independent JVMs per artifact show circuit mean tick-cost reductions of 27.16%, 11.14% and 18.26% at 128, 512 and 1,024 circuits. Allocation reductions are 38.92%, 18.54% and 21.92%. Crop allocation falls, while crop mean timing and entity construction regress in measured cases. Full fork ranges, P99 values and unfavorable outcomes are in [PERFORMANCE.md](../PERFORMANCE.md); these results do not measure full-modpack FPS.

Distribution names contain only the project name. Viking602 remains an author credit. Normal and sources archives exclude destructive verification code and reference trees. Licensing and original material notices remain unchanged.
