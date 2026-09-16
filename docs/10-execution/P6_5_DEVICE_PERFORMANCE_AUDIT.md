# P6.5 Device & Performance Hardening — Source Audit

Status: **FROZEN SOURCE AUDIT**  
Issue: #100  
Parent epic: #91  
Target: `0.6.0-family-readiness`  
Verified baseline: `c49434ab6acd32e8153164e46f86a58ec09bca0a` / Android CI #655 GREEN  
Branch: `phase6/p6-5-device-performance-hardening`

## 1. Purpose

P6.5 hardens the already accepted family/accessibility product across device size, lifecycle/resource pressure and sustained workloads. This slice is not permission to redesign accepted engines or trade data correctness for benchmark numbers.

The audit separates:
- foundations already strong enough to preserve;
- measurable gaps that need hardening;
- hardware-only evidence that cannot be honestly claimed from CI;
- concrete defects that justify code changes.

## 2. Existing performance contract is authoritative

`docs/18_DRAWING_PERFORMANCE_GATES.md` already defines the drawing performance model and is inherited rather than replaced.

Existing device classes:
- **Class L** — low/minimum target, approximately 4 GB RAM, 60 Hz;
- **Class M** — mainstream target, approximately 6–8 GB RAM, 60/90/120 Hz;
- **Class S** — active-stylus target where hardware is available.

Existing workloads:
- **W1** — approximately 500 operations;
- **W2** — 2,000 operations / about 250k samples;
- **W3** — 5,000 operations stress;
- **W4** — teacher playback determinism.

Existing hardware performance gates remain unchanged, including input-processing, frame/jank, save/load, undo/redo, memory/soak and stylus requirements. Hardware that is not available must remain `PENDING-HARDWARE`, not be inferred from CI or another device.

## 3. Existing Quality Lab is a real foundation

`QualityLabActivity` already uses:
- AndroidX `JankStats` and `PerformanceMetricsState`;
- frame duration/jank monitoring;
- input dispatch timing;
- Java/native memory snapshots;
- Save×20 benchmark;
- Load→render×20 benchmark;
- visible Undo/Redo×20 benchmark;
- W1 committed-render ×600 frame pulse;
- a 30-minute W2 soak path with save/load, undo/redo and repeated rendering;
- device summary including manufacturer/model/API/RAM/heap class/refresh rate.

`ArtLabQualityWorkloadFactory` already supplies W1/W2/W3 deterministic documents. P6.5 should extend/promote this harness rather than create a second competing performance framework.

## 4. Drawing hot path and raster memory

`InkDrawingSurfaceView`:
- keeps AndroidX Ink isolated in infrastructure;
- uses stable logical document coordinates across viewport resize;
- cancels only a transient active gesture on resize;
- contains no synchronous file/database persistence in touch processing;
- records finished-stroke handoff latency;
- has an internal committed-projection benchmark hook.

`DrawingDocumentEngine`:
- serializes mutations with one mutex;
- stores operation history, not bitmap history;
- keeps redo operations rather than canvas snapshots;
- has no arbitrary operation truncation.

`CommittedRasterCache`:
- one base ARGB_8888 bitmap;
- bounded checkpoints: max 8;
- bounded projected-operation window around 56 operations;
- exposes estimated raster bytes.

At the default 1000×1000 logical document, line projection base + max checkpoints can reach roughly 36,000,000 bytes (~34.3 MiB) before other drawing/coloring allocations.

`CommittedColorRasterCache`:
- one additional 1000×1000 ARGB_8888 bitmap (~3.8 MiB);
- no checkpoint copies;
- **rebuilds the full active coloring-operation projection whenever an authoritative reconciliation cannot accept the provisional tail**.

The coloring rebuild is a measurable heavy-document risk. It must be benchmarked before any structural optimization is approved.

## 5. Persistence and recovery foundations

The following stores already perform I/O on `Dispatchers.IO` and use primary/backup/temp atomic promotion patterns:
- `AtomicDrawingDocumentStore`;
- `AtomicLessonSessionStore`;
- `AtomicColoringSessionStore`;
- `AtomicGalleryCatalogStore`.

Verified behavior includes:
- temporary file + sync before promotion;
- backup retention/fallback;
- stale-save ordering guards for document/session timestamps;
- corruption represented distinctly rather than silently overwritten;
- Gallery catalog mutation refusal over a corrupt catalog;
- child artwork preservation on lesson/coloring recovery incompatibility paths.

P6.5 must not replace these with a faster but weaker persistence model.

## 6. Product lifecycle / recreation behavior

`ProductActivity` currently relies on normal Android recreation; the manifest does not mask configuration changes with `configChanges`.

Safe persisted/recreated route identity uses `rememberSaveable` for:
- current product route;
- lesson ID/revision;
- selected category/journey;
- selected Gallery entry;
- completion entry.

Adult authorization is intentionally **not** saveable:
- `ParentAccessSession` is created with plain `remember`;
- activity/process recreation creates a new locked adult session;
- if a saved route returns to `PARENT_ZONE`, a fresh locked session renders the Parent Gate instead of protected content.

Lesson/coloring/Free Draw recovery is grounded in persistent stores rather than Activity instance memory.

This is the correct security direction. P6.5 must test it under recreation/process-kill conditions rather than replacing it with `SavedStateHandle` merely because that API is absent.

## 7. Background behavior

Accepted product runtimes save at stable lifecycle boundaries:
- Lesson runtime persists document/session on background;
- Coloring runtime persists document/session on background;
- Free Draw requests persistence on `ON_STOP`;
- Parent session tracks background duration and invalidates according to the P6.1 contract.

No custom `onTrimMemory` policy currently exists. This is not automatically a defect because there is no process-wide image cache that can obviously be released. P6.5 should add trim-memory handling only if a concrete releasable cache/resource is introduced or measured need is proven.

## 8. Responsive-layout findings

P6.4 already hardened font-scale behavior, but device-width policy is limited.

### Home
- vertically scrollable and safe-inset aware;
- large text stacks Free Draw/Gallery;
- no window-width/tablet max-content-width policy;
- on very wide tablets cards/content can stretch unnecessarily.

### Free Draw
- canvas gets remaining height via `weight(1f)`;
- controls use a vertically scrollable tray with age-dependent maximum height;
- age + font scale drive control density, not compact-height or expanded-width device policy.

### Coloring
- canvas gets remaining height via `weight(1f)`;
- palette/tools/finish controls are outside the canvas and are not wrapped in a dedicated bounded scroll tray;
- large text stacks tool rows but compact-height devices can still create vertical pressure.

### Gallery
- adaptive grid uses 150dp or 220dp minimum card width depending on text scale;
- no expanded-width cap/column policy beyond adaptive grid behavior.

P6.5 therefore needs a small pure device-layout policy based on available width/height, not model names, and targeted reflow only where evidence shows pressure.

## 9. Gallery preview findings

Preview generation is reasonably bounded:
- generated on `Dispatchers.IO`;
- fixed 360×360 PNG;
- rendering uses three temporary 360×360 ARGB bitmaps and recycles intermediates;
- preview is explicitly non-authoritative.

However, Gallery card display currently does:

`BitmapFactory.decodeFile(...).asImageBitmap()` inside Compose `remember` evaluation.

That is synchronous disk/image decode on the UI composition path. This is a concrete P6.5 defect. Preview decode must move off the UI thread while retaining lazy-grid behavior and truthful placeholder/error handling.

## 10. Storage-pressure behavior

The atomic stores can throw I/O failures without corrupting previous known-good files.

UI/runtime behavior is mixed:
- Free Draw catches stroke/erase/save failures and can show child-safe copy;
- finish-to-Gallery returns typed failures;
- Gallery list/reopen has typed unavailable/error states;
- Coloring commit callbacks currently wrap runtime persistence with `runCatching` but do not consistently surface a child-visible save-failure message.

P6.5 must harden resource/storage failure UX so:
- the app does not crash;
- it never claims a save succeeded when persistence failed;
- previous known-good data remains recoverable;
- in-memory artwork is not silently discarded merely to recover from an I/O error.

Destructive reset/recovery controls remain P6.6 ownership and are not pulled into P6.5.

## 11. Startup/product-level measurement gap

Drawing performance has a mature Quality Lab, but product startup/key-route timing is not yet represented by an equivalent repeatable product-level measurement path.

`ProductRoot` correctly renders a neutral Loading state while asynchronously resolving the completed profile/draft from DataStore, so there is no fake onboarding flash.

P6.5 needs measurement for:
- Activity creation → first stable onboarding/home surface;
- Home repository load;
- Gallery list/open;
- lesson recovery/start;
- coloring recovery;
- Free Draw recovery.

Cross-device absolute startup thresholds are not invented in this slice. Measurements must be recorded per device/build; regressions are investigated against same-device baselines. Drawing-specific absolute thresholds continue to come from `docs/18`.

## 12. Android/API matrix truth

Current build:
- minSdk 23;
- targetSdk 36;
- compileSdk 36;
- versionCode 30 during P6.5 stabilization.

CI compilation does not prove runtime compatibility across API 23–36. P6.5 should define representative API bands rather than pretend every API/device combination has run.

Recommended representative bands for automated/manual evidence:
- API 23–28 legacy/min band;
- API 29–32 middle band;
- API 33–35 modern band;
- API 36 target/current band.

Physical hardware classes remain separately recorded as L/M/S when actually known.

## 13. Risk register

| Risk | Evidence | P6.5 response |
|---|---|---|
| UI-thread Gallery preview decode | source-proven | move decode off UI thread + test |
| Coloring full-projection rebuild cost | source-proven architecture, performance not yet measured | add representative coloring stress measurement; optimize only on failed gate |
| Compact-height Coloring pressure | source-level layout | bounded/reachable control presentation + physical check |
| Wide-tablet visual stretch | source-level lack of width policy | pure width/height policy + centered/capped content where appropriate |
| Process recreation adult leakage | security-sensitive | explicit recreation tests; parent must relock |
| Low-memory/process kill data loss | persistence-critical | kill/relaunch + recovery matrix |
| Storage-full/I/O failure UX | mixed error surfacing | typed/caught safe failure, never false success |
| Startup/key-route regressions | no product-level measurement path | add internal repeatable timing harness/markers |
| New optimization corrupts truth | permanent risk | correctness gates before performance claims |
| Missing hardware classes | hardware availability | `PENDING-HARDWARE`, never inferred PASS |

## 14. Approved implementation direction after contract CI

P6.5 implementation may include, subject to the frozen contract:
1. pure device/window presentation policy with compact/expanded bands;
2. targeted tablet max-width/centering and compact-height control scrolling;
3. asynchronous Gallery preview decoding;
4. product-level quality timing/recreation/storage probes integrated with the existing Quality Lab philosophy;
5. coloring-heavy benchmark coverage and only evidence-backed optimization;
6. safe storage-pressure messaging where persistence failure is currently swallowed;
7. automated lifecycle/recovery/layout policy tests and instrumentation source coverage;
8. exact v31-or-higher QA candidate only after versionCode-30 stabilization is fully green.

## 15. Explicit non-solutions

P6.5 must not:
- truncate child artwork/history to hit a benchmark;
- lower existing `docs/18` thresholds without an explicit evidence-backed ADR;
- persist Parent Zone authorization across recreation/process death;
- add cloud telemetry or behavioral analytics;
- add a network/account dependency;
- introduce a new Android permission;
- silently reset corrupt local truth;
- add device-model special cases where width/height/resource-class policy is sufficient;
- claim hardware performance gates from CI compile-only evidence.
