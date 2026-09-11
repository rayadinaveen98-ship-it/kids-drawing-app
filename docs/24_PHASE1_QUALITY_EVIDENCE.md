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

### Baseline CI

Initial deterministic P1.7 suite passed GitHub Actions run **#68** (`34638994968`) on head `39e9cfa5eb812c7eaca288fd831cb3d51c3bf8fb`.

Later P1.7 heads must remain green; this row records only the first heavy-suite proof, not final milestone acceptance.

## 2. Production Android Ink codec

| Gate | Status | Evidence |
|---|---|---|
| Android instrumentation source compiles against real Ink 1.0 codec | PENDING-HARDWARE | CI compiles `assembleDebugAndroidTest`; final run to be recorded |
| production Ink stylus pressure/tilt/orientation round-trip executes on Android | PENDING-HARDWARE | `InkStrokePersistenceInstrumentationTest` |
| production Ink finger round-trip does not invent stylus axes | PENDING-HARDWARE | `InkStrokePersistenceInstrumentationTest` |

Host tests intentionally use `JvmStrokePayloadCodec` because AndroidX Ink payload serialization loads native Android code. Host success must never be described as execution of the production Ink codec.

## 3. Physical frame / input performance

| Gate | Class L | Class M | Class S | Notes |
|---|---|---|---|---|
| W1 frame P95/P99 | PENDING-HARDWARE | PENDING-HARDWARE | PENDING-HARDWARE | JankStats Quality Lab |
| W1 jank rate | PENDING-HARDWARE | PENDING-HARDWARE | PENDING-HARDWARE | M ≤3%, L ≤5% |
| input processing P95/P99 | PENDING-HARDWARE | PENDING-HARDWARE | PENDING-HARDWARE | record measured path/proxy explicitly |
| visible input latency | — | — | PENDING-HARDWARE | external high-speed check when suitable equipment exists |

Do not classify a user's device into L/M/S without recording model/API/RAM/display characteristics.

## 4. Heavy document responsiveness

| Gate | Status | Target |
|---|---|---|
| W2 save P95 | PENDING-HARDWARE | Class M ≤1,000 ms; Class L ≤2,000 ms |
| W2 load → stable editable render P95 | PENDING-HARDWARE | Class M ≤1,500 ms; Class L ≤3,000 ms |
| W2 single Undo/Redo visible P95 | PENDING-HARDWARE | M ≤50 ms; L ≤100 ms |
| W3 opens and remains editable without OOM on Class M | PENDING-HARDWARE | zero OOM/corruption |

Quality Lab contains repeatable W2/W3 loaders and measurement readouts. A one-off number is exploratory evidence; P95 sign-off requires enough repeated samples to support a percentile.

## 5. Reliability / memory

| Gate | Status | Evidence required |
|---|---|---|
| 30-minute W1-style soak | PENDING-HARDWARE | `docs/25_PHASE1_SOAK_PROTOCOL.md` |
| zero crash/corruption/lost committed ops during soak | PENDING-HARDWARE | completed soak record |
| no unbounded retained-heap growth across 3 cycles | PENDING-HARDWARE | profiler/heap observations |
| W3 no OOM on Class M | PENDING-HARDWARE | physical device result |

## 6. Functional physical evidence already completed

The user physically verified `0.0.5-p1.6-test` on Android and reported all requested behavior working: Pencil/color/width, true eraser with Undo/Redo, Clear + Undo, New/Save/Reload/reopen, canonical and captured teacher playback, five paces, Pause/Resume/Replay, debug overlay, and phone layout.

This is valid functional evidence but is **not** retroactively treated as Class L/M/S performance evidence because device classification and measured workload timing were not recorded.

## 7. P1.7 exit rule

Issue #14 can close when:
1. final P1.7 CI is green;
2. automated correctness rows remain PASS;
3. production Android instrumentation is compiled and executed where available;
4. required physical measurements are recorded against device classes;
5. any unavailable hardware-only item remains explicitly `PENDING-HARDWARE` only where the Phase 1 gate permits it;
6. every observed P0/P1 quality failure has a GitHub issue and disposition.
