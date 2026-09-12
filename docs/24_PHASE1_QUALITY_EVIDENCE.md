# 24 — Phase 1 Drawing Engine Quality Evidence

**Milestone target:** `0.1.0-art-lab`  
**Quality workstream:** #14 / P1.7  
**Gates:** `docs/18_DRAWING_PERFORMANCE_GATES.md`

This ledger is the authoritative Phase 1 quality status. A gate may only be `PASS`, `FAIL`, `PENDING-HARDWARE`, or `NOT-APPLICABLE` with written rationale.

## 1. Automated correctness evidence

| Gate | Status | Evidence |
|---|---|---|
| W2 generated workload = 2,000 operations | PASS | `StressFixtureFactory` + `DrawingEngineQualityStressTest` |
| W2 total input samples = 250,000 | PASS | deterministic assertion in host test |
| W3 generated workload = 5,000 operations | PASS | deterministic assertion in host test |
| W2 100-action Undo + 100-action Redo preserves exact order | PASS | `DrawingEngineQualityStressTest` |
| repeated Clear → Undo restores large document | PASS | 50 loops on 1,000-op document |
| W4 100 replays at each of five paces | PASS | 500 deterministic replays total |
| pause/resume does not advance while paused | PASS | 100 randomized pause points |
| 100 randomized mid-play pace changes preserve progress | PASS | seeded deterministic test |
| frame-chunking does not change final teacher geometry | PASS | fine/coarse clock chunk comparison |
| W2 app-owned persistence envelope round-trip | PASS | IDs/order/metadata/tool/sample count asserted |
| 20 repeated persistence round-trips have zero drift | PASS | exact document equality after cycle 20 |
| W3 5,000-operation persistence envelope decodes in order | PASS | host codec/envelope stress |
| 100 overlapping save requests preserve newest snapshot | PASS | serialized atomic store stress |
| corrupt primary recovers previous known-good backup | PASS | repeated-save corruption test |
| persisted sample timestamps remain monotonic | PASS | W2 post-round-trip assertion |
| Ink architecture boundary | PASS | CI `verify-ink-boundary.sh` |
| Art Lab permission allowlist | PASS | CI APK manifest inspection |

### CI evidence

Initial deterministic P1.7 suite passed GitHub Actions run **#68** (`34638994968`) on head `39e9cfa5eb812c7eaca288fd831cb3d51c3bf8fb`.

The first versioned physical-quality build `0.0.6-p1.7-test` / versionCode 6 passed run **#84** (`34641431268`) on exact head `bc22c0b0183891f4790e4bdd81acc8061959928f`, including host stress tests, lint, app APK assembly, instrumentation APK compilation, architecture guard and permission allowlist.

## 2. Production Android Ink codec

| Gate | Status | Evidence |
|---|---|---|
| Android instrumentation source compiles against real Ink 1.0 codec | PASS | CI run #84 compiles `assembleDebugAndroidTest` |
| production Ink stylus pressure/tilt/orientation round-trip executes on Android | PENDING-HARDWARE | `InkStrokePersistenceInstrumentationTest`; requires instrumentation execution |
| production Ink finger round-trip does not invent stylus axes | PENDING-HARDWARE | `InkStrokePersistenceInstrumentationTest`; requires instrumentation execution |

Host tests intentionally use `JvmStrokePayloadCodec` because AndroidX Ink payload serialization loads native Android code. Host success must never be described as execution of the production Ink codec.

## 3. First classified physical device — Class M

Physical evidence was recorded from the P1.7 Quality Lab on:

- model: **Samsung SM-A546E**;
- Android/API: **API 36**;
- reported RAM: **7,436 MB**;
- reported app heap class: **256 MB**;
- display refresh: **120 Hz**;
- input used for this evidence: finger.

This satisfies the project definition of **Class M / mainstream**: Android 13+, approximately 6–8 GB RAM and a contemporary 60/90/120 Hz device.

### Input-dispatch proxy

The Quality Lab's conservative `dispatchTouchEvent` upper-bound measurement repeatedly showed:

- P95: **2 ms**;
- P99: **3 ms**;
- observed max: approximately **5.5–11.2 ms** depending on run;
- sample counts observed in screenshots ranged from hundreds to more than 9,000 events.

**Status: PASS for the Class M input CPU proxy gate.** The gate is P95 ≤4 ms / P99 ≤8 ms. Because the measured window includes more than the narrower Ink processing path, passing the upper bound is strong evidence that the inner processing path is within budget. This is still not stylus-to-photon latency evidence.

### W1 frame gate

**Status: PENDING-HARDWARE.** The supplied screenshots are from W2/W3 heavy documents and from Save/Load/History benchmark loops. Their JankStats numbers are exploratory only and must not be compared to the W1 continuous-interaction frame gate. A dedicated W1 loader/measurement reset is being added before frame P95/P99/jank is classified.

Observed heavy-workload frame snapshots included P95 values from ~199 ms to the 500 ms histogram cap and native jank rates around 8–36%. These values include deliberate full-document reconciliation/benchmark work and are not accepted as W1 evidence.

## 4. Heavy document responsiveness — Class M

| Gate | Status | Physical evidence | Target |
|---|---|---|---|
| W2 save P95 | PASS | slower recorded Save×20 run: P95 **360 ms**, P99 **396 ms**; a warm run also showed P95 8 ms / P99 28 ms | ≤1,000 ms |
| W2 load → stable editable render P95 | PASS | Load→render×20 P95 **500 ms** | ≤1,500 ms |
| W2 single Undo/Redo visible P95 | **FAIL** | Visible Undo/Redo×20: n=40, P95 **500 ms**, P99 **500 ms** | ≤50 ms |
| W3 opens and remains editable without OOM on Class M | PASS | 5,000-op W3 loaded; user drew/erased and renderer remained alive without OOM/crash | zero OOM/corruption |

The Undo/Redo failure is tracked as **Issue #23**. The current path fully reconciles and rehydrates the W2 renderer after every history step; renderer projection caching/incremental reconciliation is required before this gate can pass.

### W3 memory observations

During supplied W2/W3 screenshots, Java heap was roughly **22–62 MiB** and native allocated memory roughly **85–203 MiB**. These are point observations only. They support the no-OOM result but do **not** prove the retained-heap/no-growth soak gate.

## 5. Reliability / memory

| Gate | Status | Evidence required |
|---|---|---|
| 30-minute W1-style soak | PENDING-HARDWARE | `docs/25_PHASE1_SOAK_PROTOCOL.md` |
| zero crash/corruption/lost committed ops during soak | PENDING-HARDWARE | completed soak record |
| no unbounded retained-heap growth across 3 cycles | PENDING-HARDWARE | profiler/heap observations |
| W3 no OOM on Class M | PASS | SM-A546E physical W3 interaction |

## 6. Functional physical evidence already completed

The user physically verified `0.0.5-p1.6-test` on Android and reported all requested behavior working: Pencil/color/width, true eraser with Undo/Redo, Clear + Undo, New/Save/Reload/reopen, canonical and captured teacher playback, five paces, Pause/Resume/Replay, debug overlay, and phone layout.

The same device then supplied measured P1.7 Quality Lab evidence from `0.0.6-p1.7-test`, allowing the SM-A546E to be formally classified as Class M.

## 7. Current disposition

PASS:
- deterministic correctness/stress suite;
- CI architecture/privacy gates;
- instrumentation APK compilation;
- Class M input-dispatch upper-bound P95/P99;
- Class M W2 save P95;
- Class M W2 load→editable P95;
- Class M W3 opens/remains editable without OOM.

FAIL:
- Class M W2 visible single Undo/Redo P95 — **Issue #23**.

PENDING-HARDWARE / re-test:
- proper W1 frame P95/P99/jank after dedicated W1 measurement path;
- 30-minute soak + retained-heap observation;
- active-stylus correctness/latency where suitable stylus hardware is available;
- execution of the Android instrumentation Ink round-trip test.

## 8. P1.7 exit rule

Issue #14 can close when:
1. final P1.7 CI is green;
2. automated correctness rows remain PASS;
3. production Android instrumentation is compiled and executed where available;
4. required physical measurements are recorded against device classes;
5. any unavailable hardware-only item remains explicitly `PENDING-HARDWARE` only where the Phase 1 gate permits it;
6. every observed P0/P1 quality failure has a GitHub issue and disposition;
7. Issue #23 is resolved and the W2 visible Undo/Redo Class M gate is re-measured at P95 ≤50 ms.
