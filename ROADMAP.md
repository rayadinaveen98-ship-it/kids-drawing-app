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
**Physical QA:** **52/52 PASS**  
**Squash merge:** `fbc118343dde860a9784d7a47356eb6a7fff73e1`  
**Merged-main CI:** #585 GREEN.

Delivered:
- 24 production lessons across all four age bands;
- Content Production System V2 and validation;
- Companion / Teacher Experience V2;
- deterministic local adaptive teaching and child-requested Help;
- prerequisite-safe journeys and explainable recommendations;
- coloring resume → drawing resume → fresh precedence;
- offline/lifecycle/recovery/Gallery/Coloring/Free Draw acceptance;
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
**Current slice:** **P6.5 #100 — Device & Performance Hardening**.

### Objective
Turn the accepted 0.5 learning product into a family-manageable, accessibility-strong and device-resilient product without weakening the child-first/offline-first architecture.

Phase 6 is intentionally not a curriculum-volume phase. The quality jump is family trust, adult-only management, accessibility, device resilience and local-data safety while preserving the accepted teaching system.

### P6.1 — Parent Zone & Family Controls Contract — COMPLETE
- issue #92 CLOSED;
- PR #93 squash merge `9e1323b3643106aee3e50134f1b971e8086998ed`;
- merged-main CI #591 GREEN;
- contract audit **84/84 PASS**.

### P6.2 — Parent Zone Foundation — COMPLETE
- issue #94 CLOSED;
- accepted executable `528467acdcfde9c4d6ea01959d57157376cb081d`;
- version `0.6.0-family-readiness-p6.2-qa1`, versionCode **28**;
- profile artifact **10385266255**;
- physical QA **30/30 PASS**;
- acceptance-doc CI #602 GREEN;
- squash merge `38177435662f0b111b54138892ab65587b2d8abe`;
- merged-main CI #603 GREEN.

### P6.3 — Parent Progress & Curriculum Visibility — COMPLETE
- issue #96 CLOSED / COMPLETED;
- contract audit **64/64 PASS**;
- contract CI #606 GREEN;
- accepted executable `f155abc894d21b8cc09112a018fdf53ae25e4447`;
- version `0.6.0-family-readiness-p6.3-qa1`, versionCode **29**;
- profile artifact **10387978868**;
- profile SHA256 `93ccc4cbbf3be6b1d89e9d0094810dcf02a0654a410650752a3b8e47bf7d5b54`;
- physical QA **24/24 PASS**;
- acceptance-doc CI #616 GREEN;
- squash merge `fc81703b57419d14c9baf50a0fb4fb91554652f0`;
- merged-main CI #617 GREEN.

Delivered by P6.3:
- protected Parent Zone → Learning read-only progress surface;
- genuine local completion/recent lesson truth;
- authored categories/skills/journeys/prerequisite context;
- timestamped Gallery activity with Lesson vs Free Draw provenance;
- separate drawing/coloring in-progress state;
- no fabricated completion dates or new analytics history store;
- no grades, ranks, mastery %, XP/streak pressure, permanent ability labels, comparisons or parent-facing raw Help counts.

### P6.4 — Accessibility System V2 — COMPLETE
- issue #98 **CLOSED / COMPLETED**;
- contract audit **74/74 PASS**;
- contract CI #623 GREEN;
- stabilization head `701d29e465a12f9e6d6f2a3331e82a8725fbd11a`, CI #647 GREEN;
- accepted executable `fec854c3321d1966dc05437c2c4a3651c2323ae1`;
- version `0.6.0-family-readiness-p6.4-qa1`, versionCode **30**;
- profile artifact **10434518471**;
- profile SHA256 `2727d18d8f21c0e60612f8134aeb06b502941e816ea5390c757007f5dc31acce`;
- candidate CI #648 GREEN;
- physical QA **30/30 PASS**;
- acceptance-doc head `862b51c40aa98d0235f6ba0cfc5afb6901b5342c`;
- acceptance-doc CI #653 GREEN;
- PR #99 squash merge `d4efa03341b86641b1cf2342c857ad2021e9005a`;
- merged-main CI #654 / run `35070272716` GREEN.

Delivered by P6.4:
- persistent local Reduce motion preference;
- system-font-scale layout policy;
- Parent Accessibility & Audio surface;
- reduced-motion Parent Gate visuals with unchanged 2.5-second safety rule;
- radio/checkbox/navigation semantics and non-color selected cues;
- human-readable palette semantics;
- targeted contrast hardening;
- large-text/reachability adaptation across Home, onboarding, Guided Lesson, Coloring, Free Draw and Gallery;
- honest direct-touch canvas/read-only Gallery semantics;
- truthful handedness behavior without speculative mirroring;
- no account/cloud/network/new-permission/multi-profile migration or ownership rewrite.

The physically accepted P6.4 executable remains profile artifact **10434518471** from `fec854c...`; later documentation, merge or future commits do not replace that tested binary.

### P6.5 — Device & Performance Hardening — ACTIVE
Issue: **#100**.

Verified start baseline:
- P6.4 squash merge `d4efa03341b86641b1cf2342c857ad2021e9005a`;
- merged-main Android CI #654 / run `35070272716` GREEN.

Planned scope:
- small-screen and tablet adaptation/validation;
- configuration/process recreation and low-memory resilience;
- startup and key-flow runtime performance baselines;
- large drawing/canvas stress and repeated undo/redo/save stress;
- Gallery/storage-pressure and safe failure behavior;
- Android API/device-class compatibility matrix;
- lifecycle/background/return stress for child and Parent Zone boundaries;
- focused automated regression/performance instrumentation where practical;
- exact monotonic QA APK + physical device matrix before merge.

Frozen constraints:
- preserve Drawing/Lesson/Coloring/Gallery/adaptive truth and ownership semantics;
- preserve P6.1 Parent Gate/session rules and P6.4 accessibility behavior;
- no account/cloud/network dependency or new permission;
- no data loss hidden behind optimizations;
- no speculative engine rewrite without a concrete measured defect;
- keep core offline-first and deterministic;
- keep versionCode **30** during P6.5 stabilization; reserve >30 only after full implementation is automated-green.

Next gate:
1. source-level device/layout/lifecycle/storage/performance audit;
2. measurable P6.5 device/performance contract + acceptance matrix;
3. contract CI GREEN;
4. implementation/hardening;
5. automated-green stabilization;
6. monotonic QA APK + physical matrix;
7. acceptance-doc CI → squash merge → merged-main CI → close P6.5.

### P6.6 — Family Data & Recovery Controls — PLANNED
Parent-confirmed destructive actions, single-profile cleanup/reset boundaries, artwork protection, local recovery and corruption-safe UX.

### P6.7 — Cross-device Family Readiness QA + 0.6 Release — PLANNED
Full family/child flow regression, accessibility/device matrix, offline/privacy checks, exact APK evidence and physical acceptance for final `0.6.0-family-readiness`.

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
