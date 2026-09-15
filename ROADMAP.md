# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lessons, all teaching modes, Help Ladder and recovery proven; physical 32/32 PASS.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
Production onboarding → lesson → coloring → Gallery journey physically passed 41/41.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
`0.4.0-content-studio`, versionCode 19.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — PHYSICALLY ACCEPTED / CLOSURE ACTIVE

**Target:** `0.5.0-curriculum-expansion`  
**Parent epic:** #73  
**Frozen curriculum:** **24 production lessons**  
**Final accepted candidate:** versionCode **27** / commit `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`  
**Final-candidate CI:** Android CI #580 / run `34927419291` — **GREEN**  
**Physical QA:** **52/52 PASS** on 2026-09-15.

### P5.1 — Curriculum & Teaching Contract — COMPLETE
### P5.2 — Content Production System V2 — COMPLETE
### P5.3 — Companion / Teacher Experience V2 — COMPLETE
### P5.4 — Curriculum Expansion Set C — COMPLETE
### P5.5 — Curriculum Expansion Set D — COMPLETE
### P5.6 — Curriculum Expansion Set E — COMPLETE
### P5.7 — Local Adaptive Teaching — COMPLETE
### P5.8 — Cross-age Curriculum QA + 0.5 Release — PHYSICAL PASS / MERGE CLOSURE ACTIVE

P5.8 has validated:
- all 24 lessons and all four age bands;
- journeys, prerequisites and browse/discovery;
- Draw With Me, Watch Then Draw and authored Trace;
- Companion + authored Help/Replay boundaries;
- deterministic local adaptive recommendations and child-requested adaptive Help;
- coloring resume → drawing resume → fresh precedence;
- lifecycle/recovery, Gallery, Coloring and Free Draw;
- Airplane Mode and privacy/permission boundaries;
- exact content quality **24 / 0 / 6**;
- final identity `0.5.0-curriculum-expansion`, versionCode 27;
- exact APK artifact/hash evidence;
- final physical QA **52/52 PASS**.

Remaining Phase-5 closure gates:
1. acceptance-doc exact-head CI GREEN;
2. PR #90 ready + squash merge;
3. merged-main CI GREEN;
4. close #88 and #73 completed.

Completion of those gates makes `0.5.0-curriculum-expansion` the fully closed Phase-5 release.

## Phase 6 — Parent Zone + Accessibility + Device Hardening 0.6 — NEXT

**Target:** `0.6.0-family-readiness`.

### Objective
Turn the accepted 0.5 learning product into a family-manageable, accessibility-strong and device-resilient product without weakening the child-first/offline-first architecture.

### Planned slices

- **P6.1 — Parent Zone & Family Controls Contract**  
  Freeze parent-gate model, parent-only information architecture, child-profile management, progress visibility boundaries, local data controls, settings ownership, privacy language and threat/safety constraints before implementation.

- **P6.2 — Parent Zone Foundation**  
  Implement parent gate, Parent Zone navigation, local profile management, safe settings shell and parent-only data-management surfaces.

- **P6.3 — Parent Progress & Curriculum Visibility**  
  Add non-judgmental local progress summaries, completed-work/history visibility, curriculum/journey context and suggested next areas without grades, ranks, ability labels or cloud profiling.

- **P6.4 — Accessibility System V2**  
  Reduced motion, larger text/dynamic-type resilience, touch-target/reachability audit, contrast/semantics, screen-reader-critical flows where practical, handedness/layout considerations and age-appropriate control density.

- **P6.5 — Device & Performance Hardening**  
  Small-screen/tablet adaptation, low-memory/process-recreation resilience, startup/runtime performance, large-canvas stress, storage-pressure behavior and Android device/API matrix checks.

- **P6.6 — Family Data & Recovery Controls**  
  Safe local reset/delete/export boundaries if supported, per-profile cleanup, artwork protection, corruption recovery UX and parent-confirmed destructive actions.

- **P6.7 — Cross-device Family Readiness QA + 0.6 Release**  
  Full family/child flow regression, accessibility/device matrix, offline/privacy checks, exact APK evidence and physical acceptance.

### Phase-6 non-goals

- cloud accounts/sync;
- public social sharing;
- monetization/paywalls;
- unrestricted child-facing generative AI;
- punitive gamification, grades, ranks, XP or permanent ability labels;
- reopening accepted Drawing/Lesson/Coloring/adaptive internals without a concrete defect + explicit contract/ADR.

### Phase-6 exit target

A verified installable `0.6.0-family-readiness` APK where a parent can safely manage the local family experience, understand progress without grading the child, control relevant settings/data through a parent-gated surface, and trust the app across accessibility/device/lifecycle conditions while the child experience remains offline-first and autonomous.

## Phase 7 — Beta / Store Readiness 0.7 — PLANNED

Broader real-device matrix, crash/ANR and upgrade hardening, Google Play Families/privacy re-review, production signing/release pipeline, store assets/listing, closed-beta feedback and final usability cleanup.

## Phase 8 — Public V1 1.0 — PLANNED

Final public-release regression, family testing, privacy/safety review, production AAB/APK, release notes/store presence and monitored launch.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.