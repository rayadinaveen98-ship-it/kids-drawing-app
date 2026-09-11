# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Phase:** Phase 1 — Art Lab / Drawing Engine 0.1  
**Foundation version:** 0.0.1  
**Target milestone:** `0.1.0-art-lab`  
**Status:** P1.1 scaffold complete; P1.2 DrawingSurface implementation active in draft PR #16  
**Last updated:** 2026-09-11

## Phase 0 — COMPLETE

The full product foundation is locked in Git:
- product vision, PRD and V1 boundaries;
- age-adaptive UX and canonical screen architecture;
- structured lesson schema, authoring pipeline, taxonomy and starter curriculum;
- Drawing, Lesson and Coloring engine contracts;
- cross-engine ownership/handoff rules;
- measurable Drawing Engine performance/reference-device gates;
- Companion V1 behavior/voice/accessibility contract;
- Premium Storybook Art Studio visual direction/design-system foundation;
- child safety/privacy, permission and third-party SDK policy;
- Parent Gate contract;
- testing and release strategy;
- current 2026 Google Play/Families policy baseline.

Authoritative Phase 0 completion evidence: `docs/14_PHASE0_EXIT_GATE.md`.

## Selected architecture highlights

- Android-first native Kotlin + Jetpack Compose.
- Stable AndroidX Ink 1.0.0 as low-level inking substrate behind owned drawing-domain interfaces.
- Offline-first core product; no mandatory backend/account.
- No ads; no behavioral analytics required for Alpha.
- No sensitive permissions/network dependency in Art Lab unless a concrete reviewed need appears.
- UI observes/commands engines but does not own engine truth, playback timing, lesson sequencing or artwork history.
- Editable operation-based artwork; previews are derivatives.
- deterministic five-speed teacher playback.
- public V1 content target 30–40 lessons; hard quality floor 24.
- visual direction: **Premium Storybook Art Studio**.

## Phase 1 Android scaffold baseline — COMPLETE

P1.1 issue **#8** is closed as completed.

Selected reproducible baseline:
- namespace/application ID: `com.navin.kidsdrawing`;
- AGP: `9.4.0`;
- Gradle: `9.6.1` via committed official Gradle wrapper;
- JDK: `17`;
- compileSdk: `36`;
- targetSdk: `36`;
- minSdk: `23`;
- versionCode: `2`;
- versionName: `0.0.2-dev`;
- Compose BOM: `2026.06.00`;
- AndroidX Ink: `1.0.0` stable.

### minSdk rationale

`minSdk 23` is the initial Art Lab device-support assumption. It keeps Phase 1 aligned with the current AndroidX baseline while preserving broad Android device coverage for an India-first kids product. This remains an assumption to validate against real target-device distribution and physical-device performance before public V1; it is not a permanent product-policy promise.

### CI baseline

GitHub Actions now runs with `contents: read` and performs:
- clean Android SDK/JDK provisioning without repository secrets;
- Gradle wrapper validation/cache setup using the committed wrapper;
- lesson schema/sample JSON parsing;
- debug unit tests;
- Android lint;
- debug APK assembly;
- merged APK permission inspection with an explicit allowlist;
- APK artifact upload after all gates pass.

The only permitted merged-manifest permission in the Phase 1 scaffold is the AndroidX-generated app-internal `com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Any additional Android permission fails CI.

Verified P1.1 CI run: `34624824285` — all gates green.

## Phase 1 backlog

Epic: **#7 — Phase 1 Art Lab / Drawing Engine 0.1**

Execution order/dependencies:
1. #8 — Scaffold Android project, modules and CI baseline. **COMPLETE**
2. #9 — Low-latency DrawingSurface with AndroidX Ink adapter. **ACTIVE — draft PR #16**
3. #10 — Drawing document model and undo/redo history.
4. #11 — Atomic document persistence and recovery.
5. #12 — Deterministic teacher playback and five pace profiles.
6. #13 — Art Lab controls and debug metrics.
7. #14 — Tests, benchmarks and recovery stress suite.
8. #15 — Package/verify `0.1.0-art-lab` APK milestone.

Some implementation tasks may overlap after their prerequisites are stable, but milestone truth remains tied to issue acceptance criteria.

## Current P1.2 implementation direction

Draft PR **#16** isolates the first DrawingSurface work from `main` while CI validates it. The branch currently introduces:
- product-owned drawing surface/stroke models with no AndroidX Ink types;
- a pure logical document ↔ viewport mapper with resize/rotation tests;
- stable AndroidX Ink 1.0.0 contained inside drawing infrastructure;
- stable `androidx.input:input-motionprediction:1.0.0` for predicted MotionEvents;
- view-backed wet/committed stroke handoff hosted in Compose;
- finger/stylus metadata capture, single-primary-pointer protection and cancellation safety;
- internal Art Lab responsiveness/pressure metrics.

No persistence/history work moves into the input hot path; those remain later owned engine slices.

## Immediate next action

Drive draft PR **#16 / Issue #9** to green CI, resolve exact Ink API/compiler findings, then verify the issue acceptance boundary before merging. Physical-device finger/stylus/performance evidence remains required where the acceptance criteria cannot be proven by CI alone.

## Figma note

Figma workspace exists: `Kids Drawing App — Phase 0.6 Visual System`, file key `2lGC11EPu2tjgrpYivJ8hf`.

The Starter-plan MCP quota currently prevents additional automated frame generation. The production visual system is already committed in `docs/21_VISUAL_SYSTEM.md`, so this does **not** block Art Lab engineering. Do not claim the three reference frames are already built.

## Current blockers

No product or architecture blocker.

Physical-device performance gates may later be marked `PENDING-HARDWARE` where a required device class is not available, but they cannot be silently assumed passed.

## Continuation rule

A new chat/developer session should inspect, in order:
1. `PROJECT_STATUS.md`
2. `docs/HANDOFF.md`
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. open GitHub issues, beginning with Epic #7 and its next incomplete dependency
6. relevant specification docs

When chat memory and repository state disagree, Git is authoritative.
