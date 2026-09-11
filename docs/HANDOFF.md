# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file exists so a new conversation, developer, or agent can resume without reconstructing the project from chat history.

## What are we building?

An Android-first children's drawing and art-learning application. The product behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance, and tools adapt to age and demonstrated ability.

## Product promise

**Draw together with a teacher who never runs out of patience.**

## Development philosophy

Use a core-engine + vertical-slice approach:

Specify → Build capability → Unit/performance test → Integrate into real child flow → Test → lock milestone → expand.

Git is authoritative when chat and repository state disagree.

## Current Phase 0 state

Complete:
- 0.1 Repository & product foundation
- 0.2 V1 product scope contract
- 0.3 UX architecture and screen inventory
- 0.4 lesson/content architecture and starter curriculum
- 0.5 Drawing/Lesson/Coloring core engine contracts and performance gates
- 0.6 companion behavior/voice and selected visual system

Active:
- 0.7 final safety/privacy/quality/release review + Phase 1 issue preparation

Authoritative checklist: `docs/14_PHASE0_EXIT_GATE.md`.

## UX architecture

- Child shell: Home / Learn / Create / Gallery.
- Guided Drawing, Coloring and Free Draw use immersive creation workspaces with shell navigation hidden.
- Parent Zone is a separate gated graph.
- Safe Exit prevents system-back or close actions from silently destroying artwork.
- Incomplete sessions surface as Continue Drawing rather than being forced on startup.
- Canonical V1 screen IDs: `docs/15_SCREEN_ARCHITECTURE.md`.

## Content architecture

- Schema: `schemas/lesson.schema.json`.
- Validated reference lesson: `examples/cute-cat.lesson.json`.
- Authoring/package contract: `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`.
- Taxonomy/curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`.
- Public target: 36 guided lessons; hard release floor 24 high-quality complete lessons.
- Mass content production waits until representative lessons prove schema/engines.

## Core engine contracts

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`.
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`.
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`.
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`.
- Cross-engine ownership/handoffs: `docs/20_ENGINE_BOUNDARIES.md`.
- Stable AndroidX Ink 1.0.0 is the low-level inking substrate behind owned drawing-domain interfaces; see ADR-003 and ADR-007.

Critical engine rules:
- UI does not own lesson/coloring sequencing or artwork history.
- teacher/trace overlays never become child artwork.
- drawing→coloring handoff occurs only after durable document state exists.
- coloring uses the same editable artwork document, not a flattened screenshot.
- failures in narration, companion, coloring metadata or preview generation must not destroy child artwork.

## Companion / visual system

- Companion contract: `docs/09_COMPANION_SPEC.md`.
- Selected direction: **Premium Storybook Art Studio**.
- Visual system: `docs/21_VISUAL_SYSTEM.md`.
- Figma workspace: `Kids Drawing App — Phase 0.6 Visual System`, file key `2lGC11EPu2tjgrpYivJ8hf`.
- Figma Starter MCP quota currently blocks further automated frame generation; this is not an engineering blocker.

Companion principles:
- quiet is valid;
- no shame/pressure;
- canvas always wins;
- semantic product events drive reactions;
- voice/animation never block core lesson state;
- older children receive a more mature, lower-chatter presentation.

## Safety/privacy/release baseline

- Current review: `docs/22_SAFETY_PRIVACY_RELEASE_REVIEW.md`.
- Parent Gate: `docs/23_PARENT_GATE_SPEC.md`.
- Test strategy: `docs/11_TEST_STRATEGY.md`.
- Release strategy: `docs/12_RELEASE_STRATEGY.md`.

Current product defaults:
- no ads;
- no mandatory child account;
- no behavioral analytics for Alpha;
- no third-party crash SDK required for Phase 1;
- local/private artwork by default;
- no location, contacts, phone, camera, microphone, Bluetooth, broad storage or AD_ID permissions in V1 without new explicit review;
- every third-party runtime SDK requires a child-directed/privacy/manifest review;
- first Play-bound build targets at least API 36 under the current 2026 requirement, rechecked at release time.

## Immediate target after Phase 0

`0.1.0-art-lab`: installable internal APK proving low-latency drawing, structured stroke capture, undo/redo, save/load and deterministic five-speed teacher playback.

## Never lose these constraints

- required software/service spend remains ₹0 on the critical development path where a professional free option exists;
- core drawing/lesson use works offline;
- no cloud AI dependency in the early critical path;
- no public child social features;
- no behavioral ads;
- do not expose complex tools to younger children simply because the engine supports them;
- important decisions belong in Git, not only chat.

## Resume protocol

At the start of any new chat or development session inspect:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. open GitHub issues and recent commits
6. the specification for the active workstream

When chat memory and repository state disagree, Git is authoritative.