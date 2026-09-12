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
- stable AndroidX Ink substrate behind owned interfaces
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
- current Play/Families baseline
- permission and third-party SDK admission rules
- analytics/crash-reporting Alpha policy
- Parent Gate/export policy
- reconciled test/quality gates
- APK/AAB/versioning strategy
- Phase 1 issue set prepared

---

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE

**Milestone:** `0.1.0-art-lab`  
**GitHub Epic:** #7  
**Tag:** `v0.1.0-art-lab`

Proven baseline:
- low-latency finger drawing;
- Pencil/Eraser/color/width;
- operation-based undo/redo and undoable clear;
- structured editable document;
- atomic save/load/recovery;
- deterministic teacher playback at five paces;
- lifecycle persistence;
- Art Lab + Quality Lab;
- automated and physical-device quality evidence;
- installable milestone APK tied to known Git evidence.

The Phase 1 Drawing Engine is frozen as the foundation for later phases.

---

## Phase 2 — Lesson Engine 0.2 — IMPLEMENTATION + PHYSICAL QA COMPLETE

**Milestone:** `0.2.0-lesson-engine`  
**GitHub Epic:** #28  
**Target tag:** `v0.2.0-lesson-engine`

Proven:
- strict structured lesson content loading/validation;
- deterministic lesson-session state machine and semantic snapshots;
- Draw With Me;
- Watch Then Draw;
- Trace & Learn;
- replay and five-speed switching;
- Help Ladder hooks and authored guide overlays;
- lifecycle/session restoration;
- child-document-first recovery;
- fresh runtime generations and stale callback protection;
- recoverable teacher playback failure + retry;
- post-drawing/coloring handoff recovery;
- real Lesson Lab over the real Drawing Engine + bundled Cute Cat lesson;
- physical Samsung SM-A546E/API 36 verification: **32/32 required scenarios PASS**;
- no observed crash/deadlock in the full physical matrix;
- installable verified APK, SHA-256 and CI evidence recorded.

Release evidence: `docs/releases/0.2.0-lesson-engine.md`.

Repository tag/GitHub Release administration remains a separate final operation if `v0.2.0-lesson-engine` is not yet present, because the connected GitHub tool cannot create tags/releases.

---

## Phase 3 — First Vertical Slice — NEXT

**Milestone target:** `0.3.0-vertical-slice`

Build one end-to-end premium child journey using the frozen Drawing + Lesson Engine foundation:

Onboarding → age/profile setup → recommendation → companion intro → guided drawing → adaptive assistance → coloring → completion → gallery.

Phase 3 should prove product integration and child-facing quality, not redesign the already-proven engine architecture.

Key goals:
- production child shell and navigation;
- age-prioritized onboarding/recommendation;
- one premium authored lesson presented as a real child experience rather than an engineering lab;
- companion presentation integrated with lesson events;
- coloring handoff integrated with a usable first coloring experience;
- completion/reward/gallery flow;
- lifecycle continuity across the full journey;
- accessibility, child-safe interaction and premium visual polish;
- installable APK and real-device QA.

---

## Phase 4 — Coloring Engine

Productionize and broaden the V1 guided/self-color contract with authored fills, freehand color, persistence and age-appropriate teaching.

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