# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lessons, all teaching modes, Help Ladder and recovery proven; physical 32/32 PASS.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
Production onboarding → lesson → coloring → Gallery journey physically passed 41/41.

## Phase 4 — Content & Studio Expansion 0.4 — COMPLETE
`0.4.0-content-studio`, versionCode 19.

## Phase 5 — Curriculum & Teaching Experience Expansion 0.5 — COMPLETE

**Release:** `0.5.0-curriculum-expansion`  
**versionCode:** **27**  
**Epic:** #73 — CLOSED / COMPLETED  
**Final slice:** P5.8 #88 — CLOSED / COMPLETED  
**Physically tested executable:** `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`  
**Final-candidate CI:** Android CI #580 / run `34927419291` — **GREEN**  
**Physical QA:** **52/52 PASS** on 2026-09-15  
**Squash merge:** `fbc118343dde860a9784d7a47356eb6a7fff73e1`  
**Merged-main CI:** Android CI #585 / run `34934427237` — **GREEN**.

Delivered:
- 24 production lessons across all four age bands;
- professional content-production validation and Content Lab workflow;
- Companion / Teacher Experience V2;
- foundations, nature/everyday, animals, vehicles/space, people/characters and older-child technique curriculum;
- deterministic local adaptive recommendations and child-requested adaptive Help;
- prerequisite-safe journey progression and explainable non-judgmental reasons;
- coloring resume → drawing resume → fresh precedence;
- preserved offline/lifecycle/recovery/Gallery/Coloring/Free Draw behavior;
- no invented Trace, automatic Help, punitive scoring, cloud profiling or core network dependency.

### P5.1 — Curriculum & Teaching Contract — COMPLETE
### P5.2 — Content Production System V2 — COMPLETE
### P5.3 — Companion / Teacher Experience V2 — COMPLETE
### P5.4 — Curriculum Expansion Set C — COMPLETE
### P5.5 — Curriculum Expansion Set D — COMPLETE
### P5.6 — Curriculum Expansion Set E — COMPLETE
### P5.7 — Local Adaptive Teaching — COMPLETE
### P5.8 — Cross-age Curriculum QA + 0.5 Release — COMPLETE

## Phase 6 — Parent Zone + Accessibility + Device Hardening 0.6 — ACTIVE

**Target:** `0.6.0-family-readiness`  
**Epic:** #91  
**Current slice:** P6.1 #92.

### Objective
Turn the accepted 0.5 learning product into a family-manageable, accessibility-strong and device-resilient product without weakening the child-first/offline-first architecture.

Phase 6 is intentionally **not** a curriculum-volume phase. The next product-quality jump is family trust, adult-only management, accessibility, device resilience and local-data safety while preserving the accepted 24-lesson teaching system.

### P6.1 — Parent Zone & Family Controls Contract — ACTIVE
Freeze before implementation:
- parent-gate purpose, threat model, state/timeout behavior and accessibility requirements;
- Parent Zone information architecture;
- parent-owned vs child-owned preferences/actions;
- non-judgmental local progress visibility;
- destructive-action scope/confirmation/isolation;
- privacy/network/permission contract;
- parent-gated export/external-navigation boundary;
- P6.1 acceptance matrix.

No Parent Zone implementation begins until this contract is accepted and merged.

### P6.2 — Parent Zone Foundation — PLANNED
Implement the parent gate, Parent Zone shell/navigation, local profile management and safe settings foundation.

### P6.3 — Parent Progress & Curriculum Visibility — PLANNED
Add local non-judgmental progress/history/curriculum context without grades, ranks, permanent ability labels, child comparison or cloud profiling.

### P6.4 — Accessibility System V2 — PLANNED
Reduced motion, larger/dynamic text resilience, touch-target/reachability audit, contrast/semantics, critical screen-reader flows where practical, handedness/layout considerations and age-appropriate control density.

### P6.5 — Device & Performance Hardening — PLANNED
Small-screen/tablet adaptation, low-memory/process-recreation resilience, startup/runtime performance, large-canvas stress, storage-pressure behavior and Android device/API matrix validation.

### P6.6 — Family Data & Recovery Controls — PLANNED
Parent-confirmed destructive actions, per-profile cleanup/reset boundaries, artwork protection, local recovery and corruption-safe UX.

### P6.7 — Cross-device Family Readiness QA + 0.6 Release — PLANNED
Full family/child flow regression, accessibility/device matrix, offline/privacy checks, exact APK evidence and physical acceptance.

### Phase-6 non-goals
- cloud accounts/sync;
- public social sharing;
- monetization/paywalls;
- unrestricted child-facing generative AI;
- punitive gamification, grades, ranks, XP or permanent ability labels;
- large curriculum expansion merely to increase lesson count;
- reopening accepted Drawing/Lesson/Coloring/adaptive internals without a concrete defect + explicit contract/ADR.

### Phase-6 exit target
A verified installable `0.6.0-family-readiness` APK where a parent can safely manage the local family experience, understand progress without grading the child, control relevant settings/data through a parent-gated surface, and trust the app across accessibility/device/lifecycle conditions while the child experience remains offline-first and autonomous.

## Phase 7 — Beta / Store Readiness 0.7 — PLANNED

Broader real-device matrix, crash/ANR and upgrade hardening, Google Play Families/privacy re-review, production signing/release pipeline, store assets/listing, closed-beta feedback and final usability cleanup.

## Phase 8 — Public V1 1.0 — PLANNED

Final public-release regression, family testing, privacy/safety review, production AAB/APK, release notes/store presence and monitored launch.

## Permanent delivery rule

Every meaningful Android milestone produces an installable APK when technically possible, tied to exact Git/CI evidence.