# P6.5 Device & Performance Hardening Contract

Status: **FROZEN CONTRACT — IMPLEMENTATION BLOCKED UNTIL CONTRACT CI GREEN**  
Issue: #100  
Parent epic: #91  
Target: `0.6.0-family-readiness`  
Baseline: `c49434ab6acd32e8153164e46f86a58ec09bca0a` / Android CI #655 GREEN

## 1. Contract intent

P6.5 makes the accepted product resilient across screen classes, recreation/resource pressure and sustained drawing/storage workloads. It is a hardening slice, not a feature-volume or engine-rewrite slice.

Correctness always wins over benchmark numbers. An optimization that can lose artwork, weaken recovery, bypass Parent Zone safety, distort lesson truth or regress P6.4 accessibility is rejected even when it improves timing.

## 2. Authority and precedence

This contract inherits, and does not weaken:
- Phase-6 Parent Zone/session rules;
- P6.3 progress-truth boundaries;
- P6.4 accessibility/reduced-motion/font-scale behavior;
- Drawing/Lesson/Coloring/Gallery/adaptive ownership semantics;
- `docs/18_DRAWING_PERFORMANCE_GATES.md` hardware performance targets.

If a P6.5 measurement conflicts with an older hard-coded implementation assumption, the accepted product truth remains authoritative and the implementation must adapt safely.

## 3. Evidence classes

Every P6.5 gate is one of:
- **AUTOMATED** — JVM/instrumentation-source/CI compile or deterministic file-system test;
- **MEASURED-DEVICE** — numeric result from an identified physical device/profile build;
- **PHYSICAL-FLOW** — manual physical behavior check on the exact QA APK;
- **PENDING-HARDWARE** — required evidence for a device class that is not actually available;
- **N/A** — only with written rationale.

A compiled instrumentation APK is not a runtime instrumentation PASS.

## 4. Device geometry policy

P6.5 introduces one pure presentation policy derived from available window width/height, never device model names.

Required geometry bands:
- **compact width**: `< 600dp`;
- **expanded width**: `>= 600dp`;
- **constrained height**: `< 600dp`;
- **regular height**: `>= 600dp`.

Rules:
1. font-scale policy from P6.4 remains independent and composes with device geometry;
2. age band may tune child-facing density but cannot be the only responsive signal;
3. compact-width layouts must avoid horizontal clipping of critical controls;
4. constrained-height art workspaces must keep both the canvas and critical actions reachable; controls may scroll rather than collapsing the canvas to unusable size;
5. expanded-width general content must be centered/capped rather than stretching text/cards edge-to-edge indefinitely;
6. drawing document geometry remains stable logical document space independent of viewport size;
7. resizing/recreation may cancel only transient input; committed geometry is never rescaled/mutated in storage;
8. no model-specific layout branches.

## 5. Critical screen device matrix

The following surfaces must be exercised in compact and expanded geometry where technically practical:
- onboarding;
- Home;
- Parent Gate;
- Parent Zone / Learning / Accessibility;
- Guided Lesson;
- Coloring;
- Free Draw;
- Gallery list/detail/completion.

At constrained height, Guided Lesson, Coloring and Free Draw receive priority because they combine a canvas with controls.

## 6. Activity/process recreation contract

### Child/product routes
On Activity recreation:
- route identity may restore through `rememberSaveable` where already accepted;
- selected lesson/category/journey/Gallery identity may restore when valid;
- authoritative artwork/session state must rehydrate from persistent stores, not from stale UI snapshots;
- a missing/incompatible persistent source must use the existing safe recovery result, never guessed state.

### Parent Zone
Adult authorization is intentionally ephemeral:
- a recreated Activity/process must create a fresh locked `ParentAccessSession`;
- restoring the `PARENT_ZONE` route must show the Parent Gate until adult intent is re-established;
- no `SavedStateHandle`, Bundle or file may persist an unlocked parent session;
- existing 5-minute session and >30-second background invalidation rules remain unchanged.

### Duplicate side effects
Recreation must not:
- duplicate a Gallery entry;
- duplicate adaptive completion;
- duplicate a finish transaction;
- convert in-progress work into completion.

## 7. Background / low-memory contract

1. lesson, coloring and Free Draw continue saving at accepted stable boundaries;
2. process kill after a committed/safely persisted operation must recover the last known-good persistent state;
3. uncommitted transient finger/stylus samples may be lost on process death; they must never be synthesized after restart;
4. no global unbounded bitmap/image cache may be introduced;
5. `onTrimMemory` handling is added only when there is an actual releasable cache/resource and must never discard authoritative unsaved child truth;
6. Class-L W1 soak and Class-M W3 memory gates remain those in `docs/18`;
7. missing hardware remains `PENDING-HARDWARE`.

## 8. Drawing performance contract — inherited unchanged

P6.5 reuses the existing W1/W2/W3/W4 workloads and targets from `docs/18`.

### Input processing
Class M:
- P95 <= 4 ms;
- P99 <= 8 ms.

Class L:
- P95 <= 8 ms;
- P99 <= 12 ms.

No synchronous disk/database serialization is allowed on the measured touch hot path.

### Frame timing under W1
Class M at 60-Hz equivalent:
- P95 <= 16.7 ms;
- P99 <= 33.4 ms;
- custom drawing-workspace jank <= 3%.

Class L:
- P95 <= 25 ms;
- P99 <= 50 ms;
- jank <= 5%;
- no repeated multi-hundred-ms normal-stroke freeze.

### Save/load W2
Class M:
- save P95 <= 1000 ms on background I/O;
- no foreground file-I/O block >16 ms attributable to save;
- load to stable editable render P95 <=1500 ms.

Class L may use up to 2× elapsed limits but still may not perform blocking file I/O on the input/UI thread.

### Undo/redo W2
- Class M P95 <=50 ms;
- Class L P95 <=100 ms;
- 100-action sequence: no corruption/crash.

These numbers are not revised inside P6.5 without a separate evidence-backed ADR.

## 9. Coloring stress contract

The existing drawing W2 workload does not fully characterize coloring projection rebuild cost. P6.5 must add deterministic coloring-heavy stress coverage that includes:
- color strokes;
- color erase masks;
- prepared-region fills where authored data permits;
- undo/redo;
- authoritative reconcile after save/load/recreation.

Before optimizing `CommittedColorRasterCache`, record a baseline with the existing implementation.

Optimization is permitted only if a measured or deterministic stress gate demonstrates unacceptable cost/resource behavior. Any optimization must preserve exact visible and editable coloring semantics and protected line-art isolation.

## 10. Raster memory contract

1. line-art checkpoint cache remains bounded;
2. coloring raster allocation remains bounded by document dimensions rather than frame count;
3. no per-frame full bitmap snapshot history;
4. P6.5 quality output records estimated line/color raster bytes for the standard 1000×1000 document;
5. any new cache must declare a byte/count ceiling and have deterministic eviction;
6. eviction may affect only rebuildable derivatives, never authoritative document/session truth.

## 11. Gallery preview contract

### Generation
- preview remains a non-authoritative derivative;
- generation remains background I/O;
- fixed-size preview behavior remains bounded;
- preview failure must not make saved artwork disappear from authoritative Gallery truth.

### Decode/display
Current synchronous `BitmapFactory.decodeFile` during composition is prohibited after P6.5.

Required behavior:
- disk/image decode executes off the UI thread;
- missing/corrupt preview yields a stable placeholder, not a crash;
- Gallery scrolling remains lazy;
- no unbounded bitmap cache;
- preview decode cannot be used as artwork truth;
- opening an artwork still reloads authoritative `DrawingDocument`.

## 12. Storage-pressure / I/O failure contract

For document/session/catalog/preview persistence failure:
1. no uncaught app crash from expected storage I/O failure paths;
2. never show success when the authoritative save/finish failed;
3. keep previous known-good primary/backup recoverable;
4. never overwrite a typed corrupt catalog merely to make UI look empty;
5. keep in-memory artwork open when possible so the child can retry;
6. child-facing failure copy is calm and actionable, not technical;
7. adult destructive cleanup/reset remains P6.6, not P6.5;
8. tests should use fault injection/temp-directory conditions where deterministic rather than relying only on filling a real device disk.

## 13. Product-level timing contract

P6.5 adds repeatable timing probes around product operations not covered by the drawing Quality Lab:
- profile/draft resolution;
- Home model load;
- Gallery list;
- Gallery authoritative reopen;
- lesson recovery/start;
- coloring recovery;
- Free Draw recovery.

Measurement rules:
- use monotonic elapsed time;
- run off a profile/release-like build for physical numbers;
- report sample count, median/P95 where repeated measurement is meaningful;
- keep device identity/API/RAM/refresh rate with physical numeric results;
- do not compare numbers across unlike devices as if they were the same baseline;
- no hard universal startup millisecond threshold is invented in P6.5;
- investigate material same-device regression rather than hiding it.

Cold app startup may be measured with Macrobenchmark/`am start -W` if available. If that tooling is not physically run, cold-start numeric status is `PENDING-HARDWARE/TOOLING`, not PASS.

## 14. Quality Lab evolution

The existing Quality Lab remains the internal measurement surface.

P6.5 may extend it with:
- coloring-heavy workload;
- product-store/recovery timing probes;
- raster byte estimates;
- lifecycle/storage fault probes that are safe in the internal lab;
- clearer PASS/FAIL/PENDING presentation against inherited thresholds.

It must not become child-facing or required for normal app use.

## 15. Android API / device compatibility matrix

Build range:
- minSdk 24;
- targetSdk 36;
- compileSdk 36.

Representative runtime bands:
- **Band A:** API 24–28;
- **Band B:** API 29–32;
- **Band C:** API 33–35;
- **Band D:** API 36.

Required compatibility semantics:
- compile/lint across the configured SDK remains green;
- device/emulator runtime evidence is recorded only where actually executed;
- at least the available physical QA device runs the exact candidate end-to-end;
- missing bands remain explicitly pending for P6.7/Phase 7 rather than falsely marked green.

## 16. Physical device classes

Keep the existing Class L / M / S definitions from `docs/18`.

P6.5 does not infer a class from an unreported device. If the tester does not provide model/API/RAM, record `device class not established` and keep class-specific numeric gates pending.

Before public V1, at least Class L + Class M are still required; Class S is required before a differentiated stylus-quality claim.

## 17. Soak contract

The existing 30-minute soak remains required where hardware is available:
- sustained W1/W2-style document use;
- repeated undo/redo;
- repeated save/load;
- memory tracking;
- background/foreground cycles;
- final authoritative operation-timeline equality.

P6.5 extends soak acceptance to verify:
- Parent session invalidation remains correct after background intervals;
- child route/session recovery remains correct;
- no Gallery/adaptive completion duplication;
- no cumulative preview/raster memory growth from navigation loops.

Gate: zero crash, zero corrupted known-good document, zero lost committed operations.

## 18. Responsive UI implementation boundaries

Approved hardening is targeted:
- centered/max-width general content on expanded displays;
- compact-height scroll/reflow for art controls;
- window geometry policy shared where practical;
- Gallery adaptive columns may be tuned by width class;
- canvas/document coordinate model is not redesigned.

Not approved without separate evidence:
- a new navigation architecture;
- tablet-only feature branches;
- device-model whitelists;
- changing lesson modes/content because a tablet has more space;
- forcing landscape/portrait orientation.

## 19. Accessibility preservation

Every P6.5 layout/performance change must preserve P6.4:
- Android/system font scale remains text-size authority;
- Reduce motion behavior remains local and truthful;
- Parent Gate timing is unchanged;
- selected/toggled semantics remain non-color-only;
- TalkBack labels/states remain understandable;
- direct-touch canvas limitations remain honestly described;
- minimum critical target behavior is not reduced for compact screens.

Performance is not a reason to remove accessibility semantics or text.

## 20. Offline/privacy/security boundaries

P6.5 adds:
- no account;
- no cloud sync;
- no network dependency;
- no telemetry/behavior analytics upload;
- no new Android permission;
- no child profiling beyond accepted local state.

Performance measurements stay local/internal unless manually copied into QA evidence.

## 21. Versioning and release discipline

- versionCode remains **30** during P6.5 contract + implementation stabilization;
- implementation changes must become fully automated-green before a QA bump;
- first P6.5 QA candidate uses monotonic versionCode **>30** (expected 31 unless another release consumes it);
- exact profile APK hash/size/source SHA is frozen before physical QA;
- executable changes after physical QA require another monotonic version and fresh physical matrix;
- docs-only commits never replace the physically tested binary.

## 22. Implementation sequence after contract CI GREEN

1. add pure device geometry/layout policy + unit tests;
2. move Gallery preview decode off UI thread + failure/placeholder tests;
3. harden compact-height/expanded-width layouts in the smallest necessary surfaces;
4. add/extend Quality Lab product timing + coloring stress probes;
5. add recreation/lifecycle/storage-pressure automated coverage where practical;
6. fix only measured storage/performance defects while preserving atomic truth;
7. run full regression at versionCode 30;
8. only after complete stabilization CI GREEN, cut monotonic QA candidate;
9. package exact profile/debug/hash/size evidence;
10. run focused physical device/performance matrix;
11. acceptance docs CI;
12. squash merge;
13. merged-main CI;
14. close #100 and activate P6.6.

## 23. Definition of done

P6.5 is complete only when:
- this contract and checklist are CI-green before implementation;
- source-proven UI-thread preview decode is removed;
- compact/expanded/constrained-height behavior is explicit and tested;
- adult-session recreation security is proven;
- accepted persistent truth survives lifecycle/resource-pressure checks;
- existing drawing performance gates remain intact;
- coloring-heavy performance has measured evidence before any deep optimization;
- storage failures are safe and truthful;
- automated regression is green on a complete stabilization head;
- an exact monotonic profile APK is physically tested;
- unavailable hardware gates are honestly pending rather than fabricated;
- PR is squash-merged and merged-main CI is green.
