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
**Current slice:** **P6.3 #96 — Parent Progress & Curriculum Visibility**.

### Objective
Turn the accepted 0.5 learning product into a family-manageable, accessibility-strong and device-resilient product without weakening the child-first/offline-first architecture.

Phase 6 is intentionally **not** a curriculum-volume phase. The product-quality jump is family trust, adult-only management, accessibility, device resilience and local-data safety while preserving the accepted 24-lesson teaching system.

### P6.1 — Parent Zone & Family Controls Contract — COMPLETE
- issue #92 CLOSED / COMPLETED;
- PR #93 squash-merged;
- merge `9e1323b3643106aee3e50134f1b971e8086998ed`;
- merged-main CI #591 GREEN;
- 84/84 contract audit PASS.

### P6.2 — Parent Zone Foundation — COMPLETE
- issue #94 CLOSED / COMPLETED;
- PR #95 squash-merged;
- physically accepted executable source `528467acdcfde9c4d6ea01959d57157376cb081d`;
- versionName `0.6.0-family-readiness-p6.2-qa1`, versionCode **28**;
- authoritative profile artifact **10385266255**;
- profile SHA256 `b438d69ef7cabd7e023c963602e08c887b73074e219df2d03355a308d4849402`;
- candidate CI #596 GREEN;
- physical QA **30/30 PASS**;
- acceptance-doc CI #602 GREEN;
- squash merge `38177435662f0b111b54138892ab65587b2d8abe`;
- merged-main CI #603 / run `34944176393` **GREEN**.

Delivered by P6.2:
- 2.5-second Adult Intent Gate + accessible two-confirmation fallback;
- memory-only adult session with 5-minute expiry and safe invalidation;
- protected Parent Zone routing;
- Family, Learning, Accessibility & Audio, Storage & Data, Safety & Privacy and About shell;
- atomic editing of the existing single local child profile;
- truthful offline/privacy/version surfaces;
- no new permission, account, cloud dependency or multi-profile migration.

### P6.3 — Parent Progress & Curriculum Visibility — ACTIVE
Issue: #96.

Build a local, read-only, non-judgmental parent Learning surface grounded only in real persisted product truth.

Required direction:
- audit and reuse existing local truth rather than create surveillance analytics;
- completion/history must reflect genuine stored product events;
- show useful recent learning, completed/explored lessons, curriculum areas, journeys and authored skills where supported;
- clearly separate completion truth, saved-artwork activity, active/resume state and recommendations;
- derive curriculum context from authored categories, skills, journeys and prerequisites;
- handle missing/corrupt local state safely;
- no grades, scores, ranks, mastery percentages, XP/streak pressure, permanent ability labels or sibling/peer comparison;
- no cloud profiling, new account, network dependency or new Android permission;
- remain single-profile in 0.6.

### P6.4 — Accessibility System V2 — PLANNED
Reduced motion, larger/dynamic text resilience, touch-target/reachability audit, contrast/semantics, critical screen-reader flows where practical, handedness/layout considerations and age-appropriate control density.

### P6.5 — Device & Performance Hardening — PLANNED
Small-screen/tablet adaptation, low-memory/process-recreation resilience, startup/runtime performance, large-canvas stress, storage-pressure behavior and Android device/API matrix validation.

### P6.6 — Family Data & Recovery Controls — PLANNED
Parent-confirmed destructive actions, single-profile cleanup/reset boundaries, artwork protection, local recovery and corruption-safe UX.

### P6.7 — Cross-device Family Readiness QA + 0.6 Release — PLANNED
Full family/child flow regression, accessibility/device matrix, offline/privacy checks, exact APK evidence and physical acceptance.

### Phase-6 non-goals
- cloud accounts/sync;
- public social sharing;
- monetization/paywalls;
- unrestricted child-facing generative AI;
- punitive gamification, grades, ranks, XP or permanent ability labels;
- multi-child/profile-ID migration during 0.6 without a dedicated ownership migration contract;
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
