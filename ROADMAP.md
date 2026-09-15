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
**Physical QA:** **52/52 PASS**  
**Squash merge:** `fbc118343dde860a9784d7a47356eb6a7fff73e1`  
**Merged-main CI:** #585 GREEN.

Delivered:
- 24 production lessons across all four age bands;
- content-production validation and Content Lab workflow;
- Companion / Teacher Experience V2;
- deterministic local adaptive recommendations and child-requested adaptive Help;
- prerequisite-safe journey progression and explainable non-judgmental reasons;
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
**Current slice:** **P6.4 #98 — Accessibility System V2**.

### Objective
Turn the accepted 0.5 learning product into a family-manageable, accessibility-strong and device-resilient product without weakening the child-first/offline-first architecture.

Phase 6 is intentionally not a curriculum-volume phase. The product-quality jump is family trust, adult-only management, accessibility, device resilience and local-data safety while preserving the accepted teaching system.

### P6.1 — Parent Zone & Family Controls Contract — COMPLETE
- issue #92 CLOSED;
- PR #93 squash merge `9e1323b3643106aee3e50134f1b971e8086998ed`;
- merged-main CI #591 GREEN;
- 84/84 contract audit PASS.

### P6.2 — Parent Zone Foundation — COMPLETE
- issue #94 CLOSED;
- accepted executable `528467acdcfde9c4d6ea01959d57157376cb081d`;
- version `0.6.0-family-readiness-p6.2-qa1`, versionCode **28**;
- profile artifact **10385266255**;
- physical QA **30/30 PASS**;
- acceptance-doc CI #602 GREEN;
- squash merge `38177435662f0b111b54138892ab65587b2d8abe`;
- merged-main CI #603 GREEN.

Delivered by P6.2:
- 2.5-second Adult Intent Gate + accessible two-confirmation fallback;
- memory-only adult session with 5-minute expiry and safe invalidation;
- protected Parent Zone routing and six-section shell;
- atomic editing of the existing single local child profile;
- truthful offline/privacy/version surfaces;
- no new permission, account, cloud dependency or multi-profile migration.

### P6.3 — Parent Progress & Curriculum Visibility — COMPLETE
- issue #96 CLOSED / COMPLETED;
- contract audit **64/64 PASS**;
- contract CI #606 GREEN;
- implementation CIs #607, #608 and #609 GREEN;
- accepted executable `f155abc894d21b8cc09112a018fdf53ae25e4447`;
- version `0.6.0-family-readiness-p6.3-qa1`, versionCode **29**;
- profile artifact **10387978868**;
- profile SHA256 `93ccc4cbbf3be6b1d89e9d0094810dcf02a0654a410650752a3b8e47bf7d5b54`;
- candidate CI #610 GREEN;
- physical QA **24/24 PASS**;
- acceptance-doc CI #616 GREEN;
- squash merge `fc81703b57419d14c9baf50a0fb4fb91554652f0`;
- merged-main CI #617 / run `34954128416` GREEN.

Delivered by P6.3:
- protected Parent Zone → Learning read-only progress surface;
- genuine local completed/recent lesson truth;
- authored categories/skills/journeys/prerequisite context;
- timestamped Gallery activity with Lesson vs Free Draw provenance;
- clearly separate drawing/coloring in-progress state;
- no fabricated completion dates or new analytics history store;
- no grades, ranks, mastery %, XP/streak pressure, permanent ability labels, comparisons or parent-facing raw Help counts.

### P6.4 — Accessibility System V2 — ACTIVE
Issue: **#98**.

Required direction:
- complete a source-level accessibility/contrast/motion/layout/semantics audit before production changes;
- spoken labels, descriptive hints and screen-reader-friendly state descriptions on major child and parent flows;
- explicit selected/toggled/current-state semantics and non-color-only indicators;
- reduced-motion/static alternatives for decorative/transitional motion while preserving safety/timing semantics;
- font scaling and large-text resilient layouts, including responsive stacking where needed;
- contrast audit with targeted fixes for real usages rather than blanket palette claims;
- focus/order/reachability audit across Home, Parent Gate/Zone/Learning, onboarding/profile controls, lesson/drawing/coloring and Gallery/Free Draw critical paths;
- evidence-backed left-handed/layout adaptations only;
- automated semantics/layout/policy coverage where practical;
- exact monotonic APK + focused physical accessibility QA before merge.

Inherited constraints:
- critical effective targets >=48×48dp;
- no color-only critical state;
- critical controls remain reachable at large font/small screen via reflow/scroll;
- no mandatory multi-finger, precision, shake or rapid-tap critical interaction;
- reduced motion cannot weaken the 2.5-second Parent Gate/session/ownership semantics;
- no new account/cloud/network dependency or Android permission;
- accepted Drawing/Lesson/Coloring/Gallery/adaptive truth semantics remain frozen absent a concrete defect + explicit amendment;
- 0.6 remains single-profile;
- versionCode remains **29** through contract + implementation stabilization; reserve >29 only after full P6.4 implementation is automated-green.

Pre-contract notes:
- `docs/10-execution/P6_4_BASELINE_AUDIT_NOTES.md`;
- `docs/10-execution/P6_4_TRANSITION_NOTE.md`.

Next contract artifacts:
- `P6_4_ACCESSIBILITY_V2_AUDIT.md`;
- `P6_4_ACCESSIBILITY_V2_CONTRACT.md`;
- `P6_4_ACCEPTANCE_CHECKLIST.md`.

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
