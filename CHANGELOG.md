# Changelog

## 0.1.0-art-lab — 2026-09-12

### Added
- Native Android Art Lab built with Kotlin + Jetpack Compose.
- AndroidX Ink-backed low-latency drawing behind owned Drawing Engine abstractions.
- 1000×1000 logical document coordinate system.
- Pencil/Eraser/color/width tool state.
- Non-destructive erase-mask operations.
- Undo/Redo/Clear/New document operations.
- Editable operation-based document/history model.
- Atomic save/reload/autosave with backup recovery and stale-save protection.
- Deterministic teacher stroke playback at 0.4×, 0.7×, 1×, 1.5× and 2×.
- Pause/Resume/Replay and captured-child-stroke teacher playback.
- Teacher overlay isolation from child artwork/history/persistence.
- Responsive Art Lab engineering controls and diagnostics.
- Dedicated Quality Lab with W1/W2/W3 stress fixtures, timing metrics, frame metrics, memory telemetry and 30-minute soak harness.
- Bounded raster/checkpoint committed-rendering path for normal drawing and history operations.
- CI guards for AndroidX Ink architecture boundaries and permission allowlist.
- Android instrumentation coverage for production Ink codec compilation/integration.

### Verified
- Class-M physical device input timing P95/P99 2–3 ms.
- W2 Save×20 worst recorded P95 360 ms.
- W2 Load→editable×20 P95 500 ms.
- W2 visible Undo/Redo×20 P95 19 ms / P99 36 ms.
- W3 5,000-operation workload remains editable without crash/OOM.
- W1 committed-frame gate: 603 measured frames, 0 native jank, 0.5% over 16.7 ms.
- 30-minute soak: 4,587 cycles, 0 failures, persisted timeline verified.

### Known non-blocking coverage gap
- Stylus-specific pressure/tilt/palm/inverted-eraser behavior and externally instrumented input-to-visible latency remain PENDING-HARDWARE until suitable stylus/equipment is available.

## 0.0.1-foundation — 2026-09-11

### Added
- Initial product vision.
- Android-first / offline-first architectural direction.
- Core learning loop.
- Age-adaptive experience model.
- Custom Drawing Engine decision.
- Structured lesson architecture direction.
- Companion and adaptive-assistance concepts.
- Child privacy principles.
- Git-as-source-of-truth continuation policy.
