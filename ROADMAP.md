# Product Roadmap

## Phase 0 — Product Foundation

**Current state:** 0.1, 0.2 and 0.3 complete; teaching/content architecture is next.

### 0.1 Repository & Product Foundation — COMPLETE
- GitHub source of truth
- product vision
- platform/architecture direction
- ADR baseline
- status/handoff/conventions

### 0.2 V1 Scope Contract — COMPLETE
- PRD v0.2
- Must Have / Should Have / Later
- explicit non-goals
- V1 content target
- child + parent core journeys
- scope-change rule
- Phase 0 exit gate

### 0.3 UX Architecture — COMPLETE
- stable child shell
- canonical screen inventory and screen IDs
- immersive creation workspaces
- Parent Zone navigation graph
- safe back/exit rules
- resume/recovery contract
- age-adaptive presentation policy
- no-dead-end rules

### 0.4 Teaching & Content Architecture — NEXT
- machine-readable lesson schema
- authoring workflow
- category taxonomy
- skill taxonomy
- starter Art Journey curricula

### 0.5 Core Engine Contracts
- Drawing Engine 0.1 implementation contract
- performance/reference-device gates
- Lesson Engine state machine
- Coloring Engine V1 contract

### 0.6 Companion & Visual System
- companion V1 state/voice contract
- design-system foundations
- production-realistic visual direction for onboarding, Home and lesson workspace

### 0.7 Safety / Quality / Release Review
- privacy/permission/SDK review
- complete test gates
- release/Definition-of-Done review
- Phase 1 issue preparation

### Phase 0 exit condition
See `docs/14_PHASE0_EXIT_GATE.md`. Phase 1 begins only when unchecked critical exit items are resolved in Git.

---

## Phase 1 — Art Lab / Drawing Engine 0.1

Prove:
- low-latency finger drawing;
- pencil/brush abstraction;
- eraser;
- color and width;
- undo/redo;
- stroke data model;
- save/load;
- deterministic replay;
- 5 playback speeds;
- pause/resume playback;
- lifecycle persistence;
- basic stylus support.

Deliverable: an installable internal APK containing Art Lab 0.1 and automated Drawing Engine tests.

## Phase 2 — Lesson Engine 0.1

Prove structured lesson playback, wait-for-child steps, replay, speed switching, narration hooks, help-state hooks and lesson restoration.

## Phase 3 — First Vertical Slice

One end-to-end high-quality lesson:
Onboarding → recommendation → companion intro → guided drawing → assistance → coloring → completion → gallery.

## Phase 4 — Coloring Engine

Guided/self coloring, fills, brushes, palette logic and age-appropriate teaching.

## Phase 5 — Companion Engine

State-driven animation/expression/voice system integrated with lesson events.

## Phase 6 — Content System & Library

Age/difficulty/category/skill metadata, lesson packs, journeys, offline content management and authoring pipeline.

## Phase 7 — Adaptive Learning

Recommendations and help escalation based on observable learning signals, not punitive scoring.

## Phase 8 — Free Draw Studio

Age-progressive professional drawing tools.

## Phase 9 — Product Beta

Expanded content, Parent Zone, accessibility, device hardening, performance work and release readiness.

## Phase 10 — V1.0

Public production release only when product quality, safety/privacy requirements, content quality and release gates are met.