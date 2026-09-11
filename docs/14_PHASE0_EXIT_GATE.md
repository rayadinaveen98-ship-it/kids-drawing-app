# 14 — Phase 0 Exit Gate

Phase 0 ends only when the product is sufficiently specified that Phase 1 can implement Art Lab / Drawing Engine 0.1 without inventing fundamental product decisions during coding.

## Phase 0 workstreams

### 0.1 Repository & product foundation — COMPLETE
- GitHub source of truth exists.
- README, status, roadmap, handoff, conventions and ADR baseline exist.
- Android-first, offline-first, custom drawing engine and ₹0 critical-path decisions are recorded.

### 0.2 V1 product scope — COMPLETE
- product statement/target audience;
- Must/Should/Later scope;
- non-goals;
- content target;
- acceptance criteria and scope-change rule.

### 0.3 UX architecture — COMPLETE
- child/parent journeys;
- canonical screen IDs;
- creation workspace rules;
- age-adaptive presentation;
- safe exit/resume;
- no critical dead ends.

### 0.4 Teaching & content system — COMPLETE
- teaching modes / pace semantics / Help Ladder;
- validated JSON lesson schema and sample;
- source/compiled package contract;
- authoring workflow;
- category/skill/difficulty/age taxonomy;
- starter journeys and public catalog target.

### 0.5 Core engine contracts — COMPLETE
- Drawing Engine document/input/render/history/persistence/playback contract;
- stable AndroidX Ink 1.0.0 substrate behind owned interfaces;
- measurable performance/reference-device gates;
- Lesson Engine state machine/recovery;
- Coloring Engine guided/self-color contract;
- atomic cross-engine handoffs and ownership boundaries.

### 0.6 Companion & visual system — COMPLETE
- companion semantic states/priorities/rate limiting;
- placement/obstruction rules;
- voice/offline fallback/accessibility/safety language;
- selected visual direction: Premium Storybook Art Studio;
- design-system foundations and Compose translation rules;
- Figma workspace established for later visual-frame automation when free quota permits.

### 0.7 Safety, privacy, quality & release — IN PROGRESS
Completed:
- current 2026 Google Play Families/data-practice baseline reviewed;
- current targetSdk baseline reviewed (API 36+ for new standard Android submissions from 2026-08-31 under current policy);
- default-deny sensitive permission policy defined;
- third-party SDK admission policy defined;
- Alpha analytics/crash-reporting strategy defined;
- artwork privacy/export policy defined;
- Parent Gate contract defined;
- test strategy reconciled with engine/vertical-slice gates;
- APK/AAB/version release strategy reconciled;
- public-launch compliance re-review explicitly separated from internal Alpha completion.

Remaining:
- create concrete Phase 1 Art Lab implementation issue set and then mark Phase 0 complete.

## Phase 0 final exit checklist

- [x] Permanent GitHub source of truth established.
- [x] Product vision and promise documented.
- [x] V1 Must / Should / Later scope documented.
- [x] Explicit V1 non-goals documented.
- [x] Core child and parent journeys documented.
- [x] Final screen/information architecture locked.
- [x] Teaching modes and Help Ladder documented.
- [x] Lesson package/schema locked and validated with example content.
- [x] Initial curriculum/category/skill taxonomy locked.
- [x] Drawing Engine 0.1 technical contract locked.
- [x] Drawing performance gates/reference devices defined.
- [x] Lesson Engine state machine locked.
- [x] Coloring Engine V1 contract documented.
- [x] Companion V1 state/voice contract locked.
- [x] Visual design direction selected.
- [x] Child safety/privacy baseline documented.
- [x] Current platform-policy/permission/SDK review locked.
- [x] Parent-gate implementation contract locked.
- [x] Test strategy documented/reconciled.
- [x] Version/release strategy documented/reconciled.
- [x] Project conventions and Definition of Done documented.
- [ ] Phase 1 Art Lab implementation issue set prepared.

## Professional rule

A checked item means the decision is documented in Git and sufficiently precise for implementation. Discussion in chat alone does not satisfy an exit item.