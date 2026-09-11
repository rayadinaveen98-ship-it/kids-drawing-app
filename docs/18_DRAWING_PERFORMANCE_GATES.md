# 18 — Drawing Engine Performance & Reliability Gates

**Status:** Phase 0.5 quality contract  
**Purpose:** make Drawing Engine quality measurable rather than subjective.

These are engineering targets for Art Lab and later V1 validation. If a target proves physically unrealistic on an agreed reference device, we investigate/root-cause and record an ADR or gate revision; we do not silently remove the test.

## 1. Measurement principles

- Test **release-like/profileable builds**, not debug-only performance.
- Emulator results can catch correctness regressions but do not satisfy hardware performance gates.
- Measure sustained drawing, not one perfect short interaction.
- Tag drawing workspace state in JankStats/benchmark data so unrelated screens do not hide regressions.
- Use Macrobenchmark/JankStats/frame metrics for repeatable frame timing.
- Measure CPU input processing separately from display/input-to-photon latency.
- Public V1 should include at least one external high-speed-video stylus/finger latency check if suitable equipment is already available; this project does not require purchasing a measurement rig.

## 2. Reference device classes

Specific models are recorded when Phase 1 hardware is available. The classes are stable even when models change.

### Class L — Low / minimum-performance target
- physical Android phone/tablet;
- Android 10+ preferred for the initial Art Lab baseline;
- approximately 4 GB RAM;
- 60 Hz display;
- entry/lower-mid CPU/GPU class;
- finger input mandatory.

### Class M — Mainstream target
- physical Android phone/tablet;
- Android 13+;
- approximately 6–8 GB RAM;
- 60/90/120 Hz display;
- contemporary mid-range CPU/GPU;
- finger input mandatory.

### Class S — Stylus target
- physical Android tablet/phone with active stylus;
- Android 13+ preferred;
- pressure-capable stylus where hardware supports it;
- tilt/orientation tested when hardware reports them.

At least one Class L and one Class M device are required before public V1. A Class S device is required before claiming a differentiated stylus experience.

## 3. Standard benchmark documents/workloads

### W1 — Normal child session
- 15 minutes of mixed short/medium strokes;
- approximately 500 completed operations;
- color/tool changes;
- periodic undo/redo;
- autosave enabled.

### W2 — Heavy document
- 2,000 completed operations;
- at least 250,000 total input samples across strokes/masks where practical;
- mixed sizes/colors;
- representative document transforms disabled unless under explicit transform test.

### W3 — Stress document
- 5,000 operations or 500,000 input samples, whichever reaches the test cap first;
- used for reliability/degradation testing, not as the expected everyday child document.

### W4 — Teacher playback
- scripted sequence containing short, long, curved and multi-stroke examples;
- repeated at all five paces;
- includes pause/resume and mid-stroke pace change loops.

## 4. Input-path CPU budget

Instrument from receipt/processing of an input batch through submission/update of the in-progress Ink path, excluding hardware display scanout.

### Class M target
- P95 ≤ 4 ms per processed input batch during sustained drawing;
- P99 ≤ 8 ms.

### Class L target
- P95 ≤ 8 ms;
- P99 ≤ 12 ms.

No disk/database serialization may occur synchronously on this measured hot path.

## 5. Frame timing / jank target

For drawing-workspace benchmarks, collect actual refresh-aware frame timing and JankStats data.

### Mainstream Class M at 60 Hz equivalent
Target under W1 continuous interaction:
- P95 UI frame duration ≤ 16.7 ms;
- P99 ≤ 33.4 ms;
- custom drawing-workspace jank rate ≤ 3% when evaluated against one-refresh-period target;
- no repeated multi-frame stalls caused by autosave/preview generation.

For 90/120 Hz devices, record native-refresh measurements as well; a high-refresh device may fall back to a lower stable drawing frame cadence only through an explicit performance decision, never accidental jank.

### Class L
Target under W1:
- P95 UI frame duration ≤ 25 ms;
- P99 ≤ 50 ms;
- custom drawing-workspace jank rate ≤ 5%;
- no visible multi-hundred-millisecond freeze during normal strokes.

These values are initial gates and must be calibrated against Phase 1 physical-device measurements rather than weakened based on debug/emulator behavior.

## 6. End-to-end visible latency

Software frame timing does not fully measure stylus-to-photon latency.

Before public V1 stylus-quality sign-off:
- perform high-speed-video comparison on at least Class S when suitable equipment is available;
- document measured stylus position vs last rendered stroke position during slow and fast lines;
- investigate any persistent visible separation/gap;
- target P95 input-to-visible latency ≤ 50 ms and median ≤ 35 ms on Class S, with lower preferred.

If external measurement is unavailable during Art Lab, this gate remains **pending**, not assumed passed.

## 7. Save/load responsiveness

Using W2 on Class M:
- document save serialization/write completes P95 ≤ 1,000 ms on background I/O;
- foreground/input thread experiences no single save-induced block > 16 ms attributable to file I/O;
- load to first stable editable render P95 ≤ 1,500 ms;
- preview thumbnail generation never blocks drawing input.

Class L limits may be up to 2× these elapsed times but still may not block the input/UI thread on file I/O.

## 8. Undo/redo responsiveness

Under W2:
- normal single-stroke Undo/Redo visibly applies P95 ≤ 50 ms on Class M;
- Class L P95 ≤ 100 ms;
- repeated 100-action undo/redo sequence must not corrupt ordering or crash;
- clear-document undo restores all prior content accurately.

## 9. Teacher playback determinism

For W4:
- 100 repeated replays per pace complete without missing/duplicated source samples;
- pause/resume at randomized points preserves progression;
- 100 randomized pace changes do not reset the stroke;
- final visible stroke at every pace matches the canonical completed source within renderer-defined visual tolerance;
- source lesson data remains byte/logically unchanged after replay;
- no teacher overlay operation appears in the child document/history.

## 10. Persistence integrity

Automated round-trip tests must verify:
- operation count/order preserved;
- stable IDs preserved;
- brush/tool/color metadata preserved;
- Ink input data decodes successfully;
- timestamps remain monotonic;
- pressure/tilt/orientation presence is preserved when recorded;
- no cumulative visible geometry drift after repeated save/load cycles;
- interrupted atomic save never destroys the previous known-good document.

Run at least 20 save/load cycles on representative documents in test automation.

## 11. Memory / leak gates

Under a 30-minute W1-style session:
- no OOM on Class L;
- no monotonic unbounded heap growth once old transient stroke objects/previews are eligible for collection;
- after three identical workload + idle/GC cycles, retained heap growth attributable to drawing infrastructure should stay within 10% between cycles unless explained by intentional cache growth;
- document memory should scale approximately with document content, not with number of frames rendered.

W3 must remain openable/editable without OOM on Class M. Performance degradation is allowed under W3, corruption/crash is not.

## 12. Stylus correctness gates

On Class S where the hardware exposes features:
- pressure produces the configured brush response;
- tilt produces the configured response only for brushes that use tilt;
- stylus eraser maps to an eraser action when supported/enabled;
- finger/palm contacts during active stylus drawing do not create stray marks under the selected policy;
- cancellation/interruption never leaves an invalid in-progress stroke.

## 13. Reliability soak

Automated/manual soak sequence:
- 30 minutes continuous mixed drawing;
- 500 undo/redo operations across the run;
- 100 save requests;
- 100 teacher play/pause/resume/pacing operations;
- repeated background/foreground transitions;
- rotate/configuration change only where the product surface supports it.

Gate: zero crashes, zero corrupted documents, zero lost committed operations.

## 14. Tooling

Preferred free measurement stack:
- AndroidX Benchmark / Macrobenchmark;
- JankStats / FrameMetrics where appropriate;
- Android Studio Profiler / Perfetto;
- Android instrumentation tests;
- deterministic unit/property tests for document/history/playback;
- optional high-speed phone camera for external latency observation.

## 15. Gate status semantics

Every gate has one of:
- `PASS`
- `FAIL`
- `PENDING-HARDWARE`
- `NOT-APPLICABLE` with written rationale

There is no `looks fine` status.

Drawing Engine 0.1 may proceed to the first vertical-slice integration only after all required Phase 1 gates are PASS, with any hardware-only end-to-end latency item explicitly tracked as PENDING-HARDWARE rather than forgotten.