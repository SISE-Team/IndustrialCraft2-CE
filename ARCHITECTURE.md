# IndustrialCraft2-CE architecture

Alpha.11 separates independently written Java 25 algorithms, addon contracts and native NeoForge controllers. Original IC2 and the alpha.10 port are behavioral references; neither is loaded as a runtime dependency. All project Java packages use `ic2ce`. Resource IDs, registry IDs and persistent field names retain `ic2` compatibility.

## Build boundaries

| Module | Inputs | Responsibility |
| --- | --- | --- |
| `engine` | JDK 25 only | Bounded algorithms, immutable values and independent contract oracles |
| `api` | Engine and native game contracts | Addon-facing interfaces, descriptors and typed service bindings |
| `neoforge` | Engine, API, Minecraft and NeoForge | Live world/controller/capability operations and client services |
| Root distribution | Outputs of the three modules and `assets/` | One normal mod JAR and one matching sources JAR |
| Verification source set | Native runtime and fixture resources | Separately requested test JAR; excluded from production and sources |

The root has no runtime Java sources. The API imports no internal platform implementation. Assets have a separate source tree. Historical generators and reference implementations remain outside every production source set. The release audit checks the complete compiled fixture set, including probes outside `ic2ce.verification`.

## Device behavior

| Domain | Independent policies |
| --- | --- |
| Solid processing | Recipe-driven processing, induction ramping, crushing, cutting and legacy canning |
| Fluid processing | Pressurized boiler thermodynamics, paid fan condensation, whole-batch steam conversion, fertilizer credit and daylight distillation |
| Power and thermal | Buffered combustion/geothermal/biogas, permanent pellets, environmental generation, demand-driven HU/KU conversion and paid exchange batches |
| Electrical storage | Capacity/tier storage banks, fair rotating wireless contacts and high/low packet transformers |
| Cables | Native shapes and hit selection, insulation/foam state transitions, traffic sensing, redstone gates and electrical color admission |
| Mining and terrain | Pipe descent, bounded area sweeps, paid world changes, crop care and linked teleport reservations |
| Transport | Fair fluid allocation, ordered priority fluids, metered outlets and ordered item routing |
| Crafting and UU | Automatic crafting, full-layout UU assembly, positioned manual remainders and distinct amplification protocols |
| Agriculture | Growth/crossing/persistence in the crop plot and 23 species implementations for 37 registered species |
| Reactors | Rod pulses, per-component heat storage/exchange/venting, electrical/fluid modes and native hazards |
| Personal and utility | Revocable inventory ownership, item/energy invoices, arc contacts, pole motion and persisted chunk leases |
| Items and equipment | Physical battery payment, disposable fuel, mechanical/electrical tools, pigment transactions, optics, propulsion, consumables and vehicle hulls |

Shared code implements narrow contracts such as finite quantity accounting, bounded heat endpoints and committed transactions. Controllers retain their own inputs, scheduling, outputs and world effects. Renaming or relocating a class contributes zero logic-rewrite coverage.

## Algorithms and ownership

Recipe indexes publish immutable generations. Direct material buckets preserve precedence and insertion order; custom/component/tag predicates remain live. Native tag publication changes the epoch, invalidating both positive and negative candidates. Immutable material/tag descriptors are constructed once, while native holder membership stays live. Known final item/tag-only predicates prune generation-scoped candidate buckets. Warm lookup avoids a manager lock and iterator allocation. Unordered crafting uses bounded augmenting-path matching; positioned recipes validate all layout cells and owned remainders.

The energy network stages native topology changes and publishes weighted routes. Primitive component/topology buffers bound graph work. Native energy callbacks stay on the world thread because they can mutate game state. Transfers debit the actual source before crediting the sink, retain paid fractional HU/KU output and preserve overload/shock behavior.

Inventory transactions use owned rollback state and commit-only notifications. Exact registered storage types have sparse journals; unknown containers retain full snapshots for cross-slot side effects. Simulated transfers follow the same admission path as actual transfers. Component-aware stack equivalence prevents merging different contents, damage or energy.

World services use native level identity and immutable lookup generations. Lifecycle initialization has cancellation generations and real chunk-readiness checks. Chunk anchors persist selected chunks and reconcile actual owners after disk restart; shared tickets are released only when the last live lease ends. Dispatch snapshots preserve callbacks added during an existing dispatch.

Networking uses bounded binary buffers, stable type plans and typed field updates. Registry plans seal ordered registrations at the actual NeoForge registry phase. Native capabilities bind to explicit registered block/item types. Menus validate physical owner, coordinates, inventory and subinventory IDs before opening. Container, field and GUI edits retain actual server authority.

Java 25 is used for records, exhaustive pattern switches, immutable collections, `ClassValue` plans and constructor prologue validation. Primitive arrays and reusable workspaces address measured allocation. Native world ticking is synchronous, and the priority executor uses bounded platform workers. Performance statements require paired native measurements.

## Client lifetimes

Client-only bindings are isolated from dedicated initialization. Audio sessions distinguish one-shot and repeating sources and reject stale completion callbacks. Wall render states own face descriptors and tint data; models preserve native uncacheable geometry when any required cache key is absent.

Workbench navigation follows actual native screen opening/closing events. It stores world and positions because a full chunk packet can replace block-entity instances. Returning to the source is deferred until the next client tick, after the neighboring menu really closes. JEI recipe views preserve the physical server menu. Disconnect clears queued returns and connection-specific caches.

## Audit boundary

The exact alpha.10 source archive anchors ownership comparisons. The current inventory contains 911 runtime sources: 755 independently written implementations and 156 reviewed declarations, with no unreviewed retained business implementation. Declaration fingerprints classify public signatures/default forwarding contracts, native interfaces/markers, constant variant constructors, registration/data membership, constant enums, tuning constants and immutable grid metadata. They are excluded from the 755 count.

The normal artifact embeds 80 standalone engine classes and includes 1,038 native platform classes. Its complete compiled fixture set is excluded. Of 5,786 asset/data entries, 5,762 remain byte-identical to the baseline. Twenty-three language catalogs add 16 missing progression keys and a localized input narration label while preserving every existing value; one advancement background adopts the native asset-ID format. These metadata repairs have an explicit audit manifest. All 1,537 original sprite/metadata files match the original input through their migrated paths.

[Ownership](validation/rewrite-ownership.json), [source inventory](validation/rewrite-source-inventory.json), [declaration review](validation/rewrite-declaration-review.json) and domain naming ledgers are evidence. Implementation review and final gameplay acceptance are separate gates. Final acceptance binds full item/workflow/menu tests, connected multiplayer checks, disk reload and quiet paired performance to the delivered artifact. The requested final two-hour soak was withdrawn; its short run is reported separately, and prior two-hour evidence retains its original artifact attribution.
