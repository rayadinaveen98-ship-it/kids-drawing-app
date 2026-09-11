# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Phase:** Phase 1 — Art Lab / Drawing Engine 0.1  
**Foundation version:** 0.0.1  
**Target milestone:** `0.1.0-art-lab`  
**Status:** Phase 0 complete; P1.1 Android scaffold implemented and under final CI verification  
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

## Phase 1 Android scaffold baseline

P1.1 issue: **#8 — Scaffold Android project, modules and CI baseline**.

Selected reproducible baseline:
- namespace/application ID: `com.navin.kidsdrawing`;
- AGP: `9.4.0`;
- Gradle: `9.6.1`;
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

GitHub Actions currently performs:
- clean Android SDK/JDK/Gradle provisioning without repository secrets;
- lesson schema/sample JSON parsing;
- official Gradle wrapper generation;
- debug unit tests;
- Android lint;
- debug APK assembly;
- merged APK permission inspection with an explicit allowlist;
- APK and generated wrapper artifact upload after all gates pass.

The only permitted merged-manifest permission in the Phase 1 scaffold is the AndroidX-generated app-internal `com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Any additional Android permission fails CI.

## Phase 1 backlog

Epic: **#7 — Phase 1 Art Lab / Drawing Engine 0.1**

Execution order/dependencies:
1. #8 — Scaffold Android project, modules and CI baseline.
2. #9 — Low-latency DrawingSurface with AndroidX Ink adapter.
3. #10 — Drawing document model and undo/redo history.
4. #11 — Atomic document persistence and recovery.
5. #12 — Deterministic teacher playback and five pace profiles.
6. #13 — Art Lab controls and debug metrics.
7. #14 — Tests, benchmarks and recovery stress suite.
8. #15 — Package/verify `0.1.0-art-lab` APK milestone.

Some implementation tasks may overlap after their prerequisites are stable, but milestone truth remains tied to issue acceptance criteria.

## Immediate next action

Finish the final P1.1 CI gate for **#8**. Once the workflow is fully green and the debug APK/wrapper artifacts are confirmed, close #8 and begin **#9 P1.2 — Low-latency DrawingSurface with AndroidX Ink adapter**.

## Figma note

Figma workspace exists: `Kids Drawing App — Phase 0.6 Visual System`, file key `2lGC11EPu2tjgrpYivJ8hf`.

The Starter-plan MCP quota currently prevents additional automated frame generation. The production visual system is already committed in `docs/21_VISUAL_SYSTEM.md`, so this does **not** block Art Lab engineering. Do not claim the three reference frames are already built.

## Current blockers

No product or architecture blocker.

P1.1 is waiting only on the final CI permission-allowlist verification and artifact confirmation.

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
