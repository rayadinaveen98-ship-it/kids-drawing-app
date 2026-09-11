# 14 — Phase 0 Exit Gate

Phase 0 ends only when the product is sufficiently specified that Phase 1 can implement Art Lab / Drawing Engine 0.1 without inventing fundamental product decisions during coding.

## Phase 0 workstreams

### 0.1 Repository & product foundation — COMPLETE
- GitHub source of truth exists.
- README, status, roadmap, handoff, conventions and ADR baseline exist.
- Android-first, offline-first, custom drawing engine and ₹0 critical-path decisions are recorded.

### 0.2 V1 product scope — COMPLETE
- Product statement and target audience defined.
- Must Have / Should Have / Later boundaries defined.
- Explicit non-goals defined.
- Public V1 content target defined.
- Scope-change rule defined.
- Acceptance criteria exist for core Must-Have systems.

### 0.3 UX architecture — COMPLETE
- first-launch journey defined;
- guided lesson journey defined;
- stuck/help journey defined;
- coloring journey defined;
- Free Draw journey defined;
- resume/recovery journey defined;
- Gallery journey defined;
- Parent Zone journeys defined;
- canonical screen inventory/navigation architecture finalized;
- age-adaptive presentation policy defined;
- safe exit/back behavior defined;
- no critical dead-end flow remains unresolved in the V1 information architecture.

### 0.4 Teaching & content system — COMPLETE
- teaching modes defined;
- pace semantics defined;
- Help Ladder defined;
- lesson metadata and machine-readable Draft 2020-12 JSON Schema defined;
- validated sample lesson committed;
- source vs compiled package contract defined;
- lesson-authoring workflow defined using free tools;
- category taxonomy and reusable skill taxonomy defined;
- difficulty and age tagging rules defined;
- four starter Art Journey curricula defined;
- public V1 target catalog mapped to category/age coverage;
- representative content set defined before mass production.

### 0.5 Core engine contracts — IN PROGRESS
Exit requirements:
- Drawing Engine 0.1 data model defined;
- input/rendering architecture defined;
- history/undo model defined;
- deterministic replay contract defined;
- save/load document format direction defined;
- measurable performance/reference-device gates defined;
- Lesson Engine state machine defined;
- Coloring Engine V1 responsibilities defined;
- engine boundaries/interfaces clear enough to prevent UI logic from owning engine behavior.

### 0.6 Companion & visual system — IN PROGRESS
Exit requirements:
- companion semantic state machine finalized for V1;
- companion placement/obstruction rules defined;
- voice/narration behavior defined;
- visual product principles defined;
- design-system foundations defined;
- at least one selected production-realistic visual direction for onboarding, Home and lesson workspace exists before polished UI implementation.

### 0.7 Safety, privacy, quality & release — IN PROGRESS
Exit requirements:
- child safety/privacy engineering baseline reviewed;
- permission/third-party SDK policy defined;
- parent-gate behavior defined;
- test layers and Drawing Engine stress cases defined;
- milestone Definition of Done defined;
- versioning/release sequence defined;
- public-launch legal/store compliance review explicitly tracked as a later pre-release gate.

## Phase 0 final exit checklist

Phase 1 may begin when all of the following are true:

- [x] Permanent GitHub source of truth established.
- [x] Product vision and promise documented.
- [x] V1 Must / Should / Later scope documented.
- [x] Explicit V1 non-goals documented.
- [x] Core child and parent journeys documented.
- [x] Final screen/information architecture locked.
- [x] Teaching modes and Help Ladder documented.
- [x] Lesson package/schema draft locked and validated with example content.
- [x] Initial curriculum/category/skill taxonomy locked.
- [ ] Drawing Engine 0.1 technical contract locked.
- [ ] Drawing performance gates/reference devices defined.
- [ ] Lesson Engine state machine locked.
- [ ] Coloring Engine V1 contract documented.
- [ ] Companion V1 state/voice contract locked.
- [ ] Visual design direction selected.
- [x] Child safety/privacy baseline documented.
- [x] Test strategy documented.
- [x] Version/release strategy documented.
- [x] Project conventions and Definition of Done documented.
- [ ] Phase 1 Art Lab implementation issue set prepared.

## Professional rule

A checked item means the decision is documented in Git and sufficiently precise for implementation. Discussion in chat alone does not satisfy an exit item.