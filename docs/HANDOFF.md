# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file exists so a new conversation, developer, or agent can resume without reconstructing the project from chat history.

## What are we building?

An Android-first children's drawing and art-learning application. The product behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance, and tools adapt to age and demonstrated ability.

## Product promise

**Draw together with a teacher who never runs out of patience.**

## Primary product differentiators

1. Live draw-along teaching rather than passive videos.
2. Child-controlled teaching pace.
3. Adaptive Help Ladder: Independent → Hint → Guide → Direction/Anchors → Trace → Assisted Success.
4. Drawing seamlessly continues into coloring.
5. Expressive companion participates throughout the lesson.
6. Age-adaptive UI and curriculum.
7. Serious Free Draw Studio that grows with the child.
8. Story Drawing and creative-expression features later.
9. Offline-first and privacy-conscious.

## Development philosophy

Use a core-engine + vertical-slice approach:

Specify → Build capability → Unit/performance test → Integrate into real child flow → Test → lock milestone → expand.

Do not build all engines in isolation and do not build the polished full app first.

## Current Phase 0 state

Complete:
- 0.1 Repository & product foundation
- 0.2 V1 product scope contract
- 0.3 UX architecture and screen inventory
- 0.4 lesson/content architecture and starter curriculum
- 0.5 Drawing/Lesson/Coloring core engine contracts and performance gates

Next:
- 0.6 companion behavior/voice contract and selected visual direction
- 0.7 final Phase 0 safety/quality/release review + Phase 1 issue set

The authoritative checklist is `docs/14_PHASE0_EXIT_GATE.md`.

## UX architecture locked

- Child shell: Home / Learn / Create / Gallery.
- Guided Drawing, Coloring and Free Draw use immersive creation workspaces with shell navigation hidden.
- Parent Zone is a separate gated graph.
- Safe Exit prevents system-back or close actions from silently destroying artwork.
- Incomplete sessions surface as Continue Drawing rather than being forced on startup.
- Canonical V1 screen IDs live in `docs/15_SCREEN_ARCHITECTURE.md`.

## Content architecture locked

- Draft 2020-12 machine-readable schema: `schemas/lesson.schema.json`.
- Validated reference lesson: `examples/cute-cat.lesson.json`.
- Source/compiled package and authoring contract: `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`.
- Taxonomy/curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`.
- Four starter journeys: First Shapes to Pictures, Animal Artist, Space Artist, Character Creator.
- Public target: 36 guided lessons; hard release floor 24 high-quality complete lessons.
- Mass content production waits until a representative set proves the schema and engines.

## Core engine contracts locked

- Drawing Engine 0.1: `docs/07_DRAWING_ENGINE_SPEC.md`.
- Drawing quality/performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`.
- Lesson Engine V1: `docs/08_LESSON_ENGINE_SPEC.md`.
- Coloring Engine V1: `docs/19_COLORING_ENGINE_SPEC.md`.
- Cross-engine ownership/handoffs: `docs/20_ENGINE_BOUNDARIES.md`.
- Stable AndroidX Ink 1.0.0 is the low-level inking substrate behind our owned drawing-domain interfaces; see ADR-003 and ADR-007.

Important engine rules:
- UI does not own lesson/coloring sequencing or artwork history.
- teacher/trace overlays never become child artwork.
- drawing→coloring handoff occurs only after durable child-document state exists.
- coloring uses the same editable artwork document, not a flattened screenshot.
- region fill is authored/deterministic rather than relying on fragile flood-fill over arbitrary anti-aliased line art.
- failures in narration, companion, coloring metadata or preview generation must not destroy child artwork.

## Immediate target after Phase 0

`Art Lab 0.1`: an internal engineering screen proving low-latency drawing, structured stroke capture, save/load and deterministic multi-speed playback.

## Never lose these constraints

- Required software/service spend should remain ₹0 for the development path whenever a professional free option exists.
- Core drawing/lesson use must not depend on internet access.
- Do not introduce cloud AI into the critical path early.
- No public child social features.
- No behavioral ads.
- Do not expose complex tools to younger children simply because the engine supports them.
- Important decisions belong in Git, not only chat.

## Resume protocol

At the start of any new chat or development session, inspect in this order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. open GitHub issues and recent commits
6. the specification for the next active workstream

When chat memory and repository state disagree, Git is authoritative.