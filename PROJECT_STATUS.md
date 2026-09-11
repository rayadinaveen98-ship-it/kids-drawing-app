# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Phase:** Phase 1 — Art Lab / Drawing Engine 0.1  
**Foundation version:** 0.0.1  
**Target milestone:** `0.1.0-art-lab`  
**Status:** P1.1 complete; P1.2 software merged with physical-device gates pending; P1.3 document/history implementation starting  
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

## P1.1 Android scaffold — COMPLETE

Issue **#8** is closed as completed.

Selected reproducible baseline:
- namespace/application ID: `com.navin.kidsdrawing`;
- AGP: `9.4.0`;
- Gradle: `9.6.1` via committed official Gradle wrapper;
- JDK: `17`;
- compileSdk/targetSdk: `36`;
- minSdk: `23`;
- versionCode: `2`;
- versionName: `0.0.2-dev`;
- Compose BOM: `2026.06.00`;
- AndroidX Ink: `1.0.0` stable.

`minSdk 23` is the initial Art Lab device-support assumption. It preserves broad coverage while staying aligned with the selected AndroidX baseline; physical-device distribution/performance still must validate it before public V1.

CI runs with `contents: read`, validates the committed Gradle wrapper, parses lesson JSON, runs unit tests + Android lint, assembles the debug APK, enforces the AndroidX Ink architecture boundary, checks the merged APK permission allowlist, and uploads the APK only after all gates pass.

The only permitted merged-manifest permission is the AndroidX-generated app-internal `com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

## P1.2 DrawingSurface — SOFTWARE MERGED / HARDWARE GATES PENDING

Issue **#9** remains open intentionally. PR **#16** was squash-merged to `main` as commit `00c851d73fd47fd5d5d6f1c88b44e51df80fab86` after final PR-head CI run **#16 / run `34626370788`** passed every software gate.

Merged P1.2 capabilities:
- View-backed AndroidX Ink 1.0 wet-stroke authoring and committed rendering hosted in Compose;
- product-owned stroke/domain models with no Ink types outside infrastructure;
- logical `1000 × 1000` document coordinates and viewport mapping with resize/rotation tests;
- stable MotionEvent prediction via `androidx.input:input-motionprediction:1.0.0`;
- finger/stylus pressure metadata and nullable tilt/orientation only where the device exposes those axes;
- single-primary-pointer ownership, secondary-pointer protection and Android 13+ canceled-pointer handling;
- inverted stylus does not silently become ordinary black ink;
- safe cancellation/finish handoff and resize-time transient cancellation;
- Art Lab stroke/sample/pressure/handoff diagnostics;
- CI-enforced `androidx.ink` import boundary limited to `drawing/infrastructure`.

Physical-device acceptance is not inferred from CI. Repeated finger strokes, cancellation, resize/rotation behavior and responsiveness require real-hardware verification. Stylus-specific checks remain `PENDING-HARDWARE` until a suitable active-stylus device is available.

P1.2 CI APK from PR-head run `34626370788`:
- artifact ID: `10273803825`;
- extracted APK size: `18,062,506` bytes;
- APK SHA-256: `196357c1fba520a6a367f18617ad89d78d831d28f7cd611527c45289d8377fe8`.

## Phase 1 backlog

Epic: **#7 — Phase 1 Art Lab / Drawing Engine 0.1**

Execution order/dependencies:
1. #8 — Scaffold Android project, modules and CI baseline. **COMPLETE**
2. #9 — Low-latency DrawingSurface with AndroidX Ink adapter. **SOFTWARE MERGED; HARDWARE ACCEPTANCE PENDING**
3. #10 — Drawing document model and undo/redo history. **ACTIVE**
4. #11 — Atomic document persistence and recovery.
5. #12 — Deterministic teacher playback and five pace profiles.
6. #13 — Art Lab controls and debug metrics.
7. #14 — Tests, benchmarks and recovery stress suite.
8. #15 — Package/verify `0.1.0-art-lab` APK milestone.

Hardware-only P1.2 evidence may remain pending while P1.3 proceeds because the software/domain boundary needed by #10 is now merged and green. Milestone truth still follows the issue acceptance gates; #9 is not marked complete until hardware checks are recorded.

## Immediate next action

Implement **#10 / P1.3** on an isolated branch: authoritative `DrawingDocument`, ordered document operations, engine-owned state/history, undo/redo, undoable Clear, redo invalidation after a new edit, and long-sequence/property-style unit coverage. Keep AndroidX Ink types isolated from the document/history domain and keep persistence out of this slice.

In parallel, post-merge `main` CI run **#17 / `34626975173`** validates the merged P1.2 commit. Physical Art Lab verification uses `docs/22_P1_2_HARDWARE_VERIFICATION.md`.

## Figma note

Figma workspace exists: `Kids Drawing App — Phase 0.6 Visual System`, file key `2lGC11EPu2tjgrpYivJ8hf`.

The Starter-plan MCP quota currently prevents additional automated frame generation. The production visual system is already committed in `docs/21_VISUAL_SYSTEM.md`, so this does **not** block Art Lab engineering. Do not claim the three reference frames are already built.

## Current blockers

No product or architecture blocker to P1.3.

Physical-device performance gates may be `PENDING-HARDWARE` where the required device class is unavailable, but they can never be silently assumed passed.

## Continuation rule

A new chat/developer session should inspect, in order:
1. `PROJECT_STATUS.md`
2. `docs/HANDOFF.md`
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. open GitHub issues, beginning with Epic #7 and its next incomplete dependency
6. relevant specification docs

When chat memory and repository state disagree, Git is authoritative.
