# P6.5 Device & Performance Hardening — Acceptance Checklist

Status: **CONTRACT AUDIT 80/80 PASS — IMPLEMENTATION NOT YET ACCEPTED**  
Issue: #100  
Parent epic: #91  
Baseline: `c49434ab6acd32e8153164e46f86a58ec09bca0a` / Android CI #655 GREEN

`PASS` below means the requirement is explicitly present and consistent in the frozen P6.5 contract. It does **not** mean production implementation, physical hardware, runtime instrumentation or performance numbers have passed.

Later execution statuses use: `PASS`, `FAIL`, `PENDING-HARDWARE`, or `N/A` with rationale.

## A. Baseline, authority and scope — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C01 | P6.5 starts only from verified main baseline `c49434ab...` / CI #655 GREEN | PASS |
| C02 | P6.1 Parent Gate/session semantics remain authoritative | PASS |
| C03 | P6.3 progress/completion truth remains authoritative | PASS |
| C04 | P6.4 accessibility/reduced-motion/font-scale behavior remains authoritative | PASS |
| C05 | Drawing/Lesson/Coloring/Gallery/adaptive ownership semantics are not redefined | PASS |
| C06 | `docs/18_DRAWING_PERFORMANCE_GATES.md` thresholds are inherited unchanged | PASS |
| C07 | Correctness/data safety wins over benchmark improvement | PASS |
| C08 | No speculative engine rewrite without measured/source-proven defect | PASS |
| C09 | versionCode remains 30 through implementation stabilization | PASS |
| C10 | unavailable runtime/hardware evidence cannot be inferred from CI | PASS |

## B. Device geometry and responsive layout — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C11 | Compact width is defined as <600dp | PASS |
| C12 | Expanded width is defined as >=600dp | PASS |
| C13 | Constrained height is defined as <600dp | PASS |
| C14 | Regular height is defined as >=600dp | PASS |
| C15 | Device policy uses available geometry, never model-name branches | PASS |
| C16 | P6.4 font-scale policy composes independently with geometry | PASS |
| C17 | Compact width keeps critical controls horizontally reachable | PASS |
| C18 | Constrained-height art screens keep canvas + critical controls usable through scroll/reflow | PASS |
| C19 | Expanded general content is centered/capped instead of unlimited stretch | PASS |
| C20 | Viewport resize never mutates persisted logical document geometry | PASS |

## C. Activity, process and lifecycle recovery — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C21 | Saved product route identity may restore only where already accepted | PASS |
| C22 | Authoritative artwork/session state rehydrates from persistent stores, not stale UI snapshots | PASS |
| C23 | Recreated ParentAccessSession is always freshly locked | PASS |
| C24 | Restored PARENT_ZONE route renders Parent Gate until adult intent is re-established | PASS |
| C25 | Parent unlock state is never persisted in Bundle/SavedStateHandle/file | PASS |
| C26 | 5-minute Parent Zone expiry remains unchanged | PASS |
| C27 | >30-second background invalidation remains unchanged | PASS |
| C28 | Recreation cannot duplicate Gallery entries | PASS |
| C29 | Recreation cannot duplicate adaptive completion/finish transactions | PASS |
| C30 | In-progress work cannot become completion merely because of recreation | PASS |

## D. Persistence, storage pressure and recovery — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C31 | Existing atomic primary/backup/temp document persistence is preserved | PASS |
| C32 | Lesson-session atomic/backup behavior is preserved | PASS |
| C33 | Coloring-session atomic/backup behavior is preserved | PASS |
| C34 | Gallery corrupt-catalog refusal is preserved; no silent empty reset | PASS |
| C35 | Expected storage I/O failure must not cause uncaught app crash | PASS |
| C36 | UI may never claim successful save/finish when authoritative persistence failed | PASS |
| C37 | Previous known-good primary/backup stays recoverable after failed write | PASS |
| C38 | In-memory artwork remains open/retryable when safely possible after I/O failure | PASS |
| C39 | Storage failure copy is calm/actionable rather than technical | PASS |
| C40 | destructive cleanup/reset remains P6.6 ownership | PASS |

## E. Drawing performance and Quality Lab — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C41 | Existing W1/W2/W3/W4 workload definitions remain authoritative | PASS |
| C42 | Class M input P95<=4ms / P99<=8ms remains unchanged | PASS |
| C43 | Class L input P95<=8ms / P99<=12ms remains unchanged | PASS |
| C44 | Class M W1 frame P95<=16.7ms / P99<=33.4ms / jank<=3% remains unchanged | PASS |
| C45 | Class L W1 frame P95<=25ms / P99<=50ms / jank<=5% remains unchanged | PASS |
| C46 | W2 Class M save P95<=1000ms and load-to-render P95<=1500ms remain unchanged | PASS |
| C47 | W2 undo/redo P95<=50ms M / <=100ms L remains unchanged | PASS |
| C48 | no synchronous disk/database I/O is allowed on measured input hot path | PASS |
| C49 | existing JankStats/PerformanceMetricsState Quality Lab is reused rather than duplicated | PASS |
| C50 | hardware numeric performance evidence comes from profile/release-like build on identified device | PASS |

## F. Coloring, raster memory and Gallery previews — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C51 | deterministic coloring-heavy stress coverage is required | PASS |
| C52 | coloring baseline is measured before structural CommittedColorRasterCache optimization | PASS |
| C53 | any coloring optimization preserves protected line-art isolation | PASS |
| C54 | line raster checkpoint/cache bounds remain explicit | PASS |
| C55 | coloring raster allocation remains bounded by document dimensions, not frame count | PASS |
| C56 | no full-bitmap-per-frame/history architecture may be introduced | PASS |
| C57 | standard-document line/color raster byte estimates are exposed in quality evidence | PASS |
| C58 | Gallery preview generation remains background, bounded and non-authoritative | PASS |
| C59 | Gallery preview decode is moved off UI composition thread | PASS |
| C60 | missing/corrupt Gallery preview yields placeholder while authoritative artwork reopen remains independent | PASS |

## G. Product timing, API/hardware, accessibility and privacy — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C61 | product timing covers profile resolution/Home/Gallery/lesson/coloring/Free Draw operations | PASS |
| C62 | repeated timings use monotonic elapsed time with sample count and median/P95 where appropriate | PASS |
| C63 | P6.5 invents no universal cross-device cold-start millisecond threshold | PASS |
| C64 | minSdk24/targetSdk36/compileSdk36 are explicitly represented in matrix | PASS |
| C65 | representative API bands A 24–28, B 29–32, C 33–35, D 36 are explicit | PASS |
| C66 | Class L/M/S device claims require actual known hardware evidence | PASS |
| C67 | unavailable device/API classes remain PENDING-HARDWARE instead of fake PASS | PASS |
| C68 | P6.4 semantics, Reduce motion, font scaling and critical target behavior are preserved | PASS |
| C69 | P6.5 adds no account/cloud/network dependency/new Android permission | PASS |
| C70 | performance measurements stay local/internal; no behavioral analytics upload | PASS |

## H. Soak, regression and release discipline — 10/10

| ID | Contract requirement | Spec audit |
|---|---|---|
| C71 | 30-minute soak remains part of hardware acceptance where hardware is available | PASS |
| C72 | soak verifies zero crash/corrupt known-good document/lost committed operation | PASS |
| C73 | soak includes repeated save/load and undo/redo | PASS |
| C74 | soak extends to lifecycle/background/Parent-session behavior | PASS |
| C75 | navigation loops must not create unbounded preview/raster memory growth | PASS |
| C76 | full automated regression must be green at versionCode 30 before QA bump | PASS |
| C77 | first P6.5 physical candidate uses monotonic versionCode >30 | PASS |
| C78 | exact candidate source SHA/profile APK size/SHA256 are frozen before physical QA | PASS |
| C79 | executable changes after physical acceptance require new version + fresh physical matrix | PASS |
| C80 | P6.5 closes only after acceptance-doc CI, squash merge and merged-main CI GREEN | PASS |

## Contract audit result

- Baseline/scope: **10/10**
- Device geometry: **10/10**
- Lifecycle/recreation: **10/10**
- Persistence/storage: **10/10**
- Drawing performance: **10/10**
- Coloring/raster/Gallery: **10/10**
- Product timing/API/accessibility/privacy: **10/10**
- Soak/release discipline: **10/10**

**Total specification audit: 80/80 PASS.**

Again: this is a contract completeness audit only. No P6.5 production implementation or hardware benchmark is accepted by this document.

## Implementation acceptance sequence

After contract CI is GREEN:
1. pure geometry policy + tests;
2. async Gallery preview decode;
3. compact-height/expanded-width targeted layout hardening;
4. Quality Lab coloring/product timing extensions;
5. lifecycle/recreation/storage-pressure coverage;
6. measured defect fixes only;
7. full versionCode-30 stabilization CI;
8. monotonic QA cut;
9. exact artifact evidence;
10. physical matrix with explicit pending hardware entries;
11. acceptance-doc CI;
12. squash merge + merged-main CI;
13. close #100 and activate P6.6.
