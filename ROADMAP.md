# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE

All Phase 0 workstreams are locked in Git. Completion evidence: `docs/14_PHASE0_EXIT_GATE.md`.

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

### 0.3 UX Architecture — COMPLETE
- stable child shell
- canonical screen inventory and screen IDs
- immersive creation workspaces
- Parent Zone navigation graph
- safe back/exit rules
- resume/recovery contract
- age-adaptive presentation policy

### 0.4 Teaching & Content Architecture — COMPLETE
- validated machine-readable lesson schema and sample
- package/authoring workflow
- category/skill/difficulty/age taxonomy
- four starter Art Journeys
- public V1 catalog allocation

### 0.5 Core Engine Contracts — COMPLETE
- Drawing Engine 0.1 contract
- stable AndroidX Ink 1.0.0 substrate behind owned interfaces
- performance/reference-device gates
- Lesson Engine state machine
- Coloring Engine V1 contract
- cross-engine ownership and atomic handoffs

### 0.6 Companion & Visual System — COMPLETE
- companion behavior/priority/rate-limit/placement contract
- voice/offline fallback/accessibility/safety language
- selected direction: Premium Storybook Art Studio
- design-system and Compose token direction
- Figma workspace established; automated frame generation can resume when free Starter MCP quota permits

### 0.7 Safety / Quality / Release Review — COMPLETE
- current 2026 Play/Families baseline
- permission and third-party SDK admission rules
- analytics/crash-reporting Alpha policy
- Parent Gate/export policy
- reconciled test/quality gates
- APK/AAB/versioning strategy
- Phase 1 Art Lab issue set prepared

---

## Phase 1 — Art Lab / Drawing Engine 0.1 — ACTIVE

**Milestone:** `0.1.0-art-lab`  
**GitHub Epic:** #7

Implementation backlog:
1. #8 — scaffold Android project/modules/CI
2. #9 — low-latency DrawingSurface + Ink adapter
3. #10 — document model + undo/redo history
4. #11 — atomic persistence/recovery
5. #12 — deterministic teacher playback + five pace profiles
6. #13 — Art Lab controls + debug metrics
7. #14 — tests/benchmarks/recovery stress
8. #15 — package/verify milestone APK

### Phase 1 proof target
- low-latency finger drawing;
- basic stylus support;
- Pencil/Eraser/color/width;
- operation-based undo/redo and undoable clear;
- structured editable document;
- save/load/recovery;
- deterministic teacher playback;
- Extra Slow / Slow / Normal / Fast / Very Fast;
- playback pause/resume/replay/mid-play pace change;
- lifecycle persistence;
- debug metrics;
- automated tests and physical-device quality evidence;
- installable APK tied to known Git commit/tag and release evidence.

### Deliverable
An installable `0.1.0-art-lab` internal APK and Drawing Engine quality evidence. No polished production child UI is required in this phase.

---

## Phase 2 — Lesson Engine 0.1

Prove structured lesson execution, child-turn boundaries, all three teaching modes, replay, speed switching, Help Ladder hooks, narration hooks and session restoration.

## Phase 3 — First Vertical Slice

One end-to-end high-quality lesson:
Onboarding → recommendation → companion intro → guided drawing → assistance → coloring → completion → gallery.

## Phase 4 — Coloring Engine

Implement and validate the defined V1 guided/self-color contract with authored fills, freehand color, persistence and age-appropriate teaching.

## Phase 5 — Companion Engine

Productionize state-driven animation/expression/voice integrated with lesson events.

## Phase 6 — Content System & Library

Age/difficulty/category/skill metadata, lesson packs, journeys, offline content management and authoring tooling.

## Phase 7 — Adaptive Learning

Recommendations and help escalation based on observable learning signals, not punitive scoring.

## Phase 8 — Free Draw Studio

Age-progressive professional drawing tools.

## Phase 9 — Product Beta

Expanded content, Parent Zone, accessibility, device hardening, performance and release readiness.

## Phase 10 — V1.0

Public release only when product quality, safety/privacy requirements, content quality and then-current store/release gates are met.