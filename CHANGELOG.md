# Changelog

## 0.2.0-lesson-engine — 2026-09-12

### Added
- Structured Lesson Engine over the frozen Drawing Engine foundation.
- Strict lesson package loading, runtime validation and typed diagnostics.
- Deterministic lesson-session state machine with semantic snapshots.
- Draw With Me mode.
- Watch Then Draw overview + child drawing pass.
- Trace & Learn with authored trace guides.
- Help Ladder escalation, Reduce Help and Dismiss Help.
- Replay, Pause/Resume, valid/invalid Skip behavior and typed command rejection.
- Five deterministic teaching paces: 0.4×, 0.7×, 1×, 1.5× and 2×.
- Versioned SHA-256 lesson-session persistence and atomic recovery.
- Child-document-first session restoration.
- Fresh runtime generations after process recreation and stale teacher callback rejection.
- Recoverable teacher playback failure + Retry.
- Post-drawing coloring handoff contracts and retryable handoff failure.
- Real Lesson Lab integrating the production Lesson Engine, Drawing Engine and bundled Cute Cat lesson package.
- Responsive portrait Lesson Lab layout with safe system insets and adaptive action grids.

### Verified
- P2.6 exact-head CI #201 GREEN.
- Responsive hotfix exact-head CI #203 GREEN.
- Merged-main Android CI #204 / `34685747984` GREEN.
- Verified profile artifact ID `10294878922`.
- APK size `14,984,842` bytes.
- APK SHA-256 `656bee202bc71d5b487f287c6dc3a8b3f05f4cc50eea58c7477fd0a59534e109`.
- Physical Samsung SM-A546E/API 36 matrix: **32/32 PASS**.
- All three modes and all five paces passed physically.
- Force-stop/relaunch recovery passed for teacher playback, child turn, HelpActive and post-drawing state.
- Fresh teacher request generation physically proved `g0 → g1` after recreation.
- Persisted diagnostics remained `teacher/guide in child history: 0 · PASS`.
- Art Lab and Quality Lab regression checks passed.
- No crash/deadlock observed in the full physical matrix.

### Release administration
- Target tag: `v0.2.0-lesson-engine`.
- Tag/GitHub Release creation is not exposed by the connected GitHub tool in this execution environment; do not claim the tag/release object exists until verified in GitHub.

### Known non-blocking coverage gap
- The existing Phase 1 stylus-specific pressure/tilt/palm/inverted-eraser and externally instrumented input-to-visible latency gap remains PENDING-HARDWARE.

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