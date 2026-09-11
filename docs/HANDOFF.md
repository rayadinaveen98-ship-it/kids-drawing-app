# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file exists so a new conversation, developer, or agent can resume without reconstructing the project from chat history.

## What are we building?

An Android-first children's drawing and art-learning application that behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance and tools adapt to age and demonstrated ability.

## Product promise

**Draw together with a teacher who never runs out of patience.**

## Development philosophy

Core-engine + vertical-slice:

Specify → Build capability → Test independently → Integrate into real child-facing flow → Test again → lock milestone → expand.

Git is authoritative when chat and repository state disagree.

## Current project state

**Phase 0 — COMPLETE.** See `docs/14_PHASE0_EXIT_GATE.md`.

**Phase 1 — ACTIVE.** Target milestone: `0.1.0-art-lab`.

Current progression:
- #8 P1.1 scaffold/CI — **COMPLETE**
- #9 P1.2 DrawingSurface/Ink adapter — **SOFTWARE MERGED; HARDWARE ACCEPTANCE PENDING**
- #10 P1.3 document/history — **COMPLETE**
- #11 P1.4 persistence/recovery — **SOFTWARE MERGED; PHYSICAL ACCEPTANCE PENDING**
- #12 P1.5 deterministic teacher playback/five paces — **NEXT SOFTWARE TASK**
- #13 P1.6 Art Lab controls/debug metrics
- #14 P1.7 tests/benchmarks/stress
- #15 P1.8 `0.1.0-art-lab` packaging/verification

P1.4 merged through PR #18 as `e92a5dc010864428fbd89c5cdb1abd38a02f3814`.

## Current installable test build

**`0.0.3-p1.4-test` / versionCode 3**

Purpose: physical verification of drawing + editable persistence.

Capabilities:
- low-latency finger/stylus Ink drawing path;
- authoritative operation history;
- Undo / Redo;
- explicit Save / Reload;
- startup restoration;
- debounced autosave;
- lifecycle `onStop` safety save;
- renderer rehydration from the persisted editable document;
- primary/backup recovery;
- corrupt-document tolerance;
- stale delayed-save rejection.

Evidence:
- exact CI branch head: `83e436798b74bece7af430411f588aa04c052110`;
- CI run #42 / `34631056947`: green;
- artifact ID: `10276705066`;
- artifact ZIP digest: `sha256:d84fdf7eb84e1bd44959287ccde736aafa5200de974243e37a05a6e09c2b387b`;
- APK size: `18,160,820` bytes;
- APK SHA-256: `628d7ed6ed5c33405b88d5f04df0d586ce03de400e3aad069d8136ca96c40228`.

Do **not** close #9 or #11 solely from CI. Record actual hardware evidence for finger drawing, Save/Reload, Undo/Redo, reopen/process recreation and stylus where available.

## UX architecture

- Child shell: Home / Learn / Create / Gallery.
- Guided Drawing, Coloring and Free Draw use immersive creation workspaces with shell navigation hidden.
- Parent Zone is a separate gated graph.
- Safe Exit prevents back/close from silently destroying artwork.
- Incomplete sessions surface as Continue Drawing.
- Canonical V1 screen IDs: `docs/15_SCREEN_ARCHITECTURE.md`.

## Content architecture

- Schema: `schemas/lesson.schema.json`.
- Reference lesson: `examples/cute-cat.lesson.json`.
- Package/authoring: `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`.
- Taxonomy/curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`.
- Public target: 30–40 guided lessons; hard floor: 24 high-quality complete lessons.

## Core engine contracts

- Drawing Engine: `docs/07_DRAWING_ENGINE_SPEC.md`.
- Performance gates: `docs/18_DRAWING_PERFORMANCE_GATES.md`.
- Lesson Engine: `docs/08_LESSON_ENGINE_SPEC.md`.
- Coloring Engine: `docs/19_COLORING_ENGINE_SPEC.md`.
- Boundaries/handoffs: `docs/20_ENGINE_BOUNDARIES.md`.
- Stable AndroidX Ink 1.0.0 is the low-level substrate behind owned interfaces; ADR-003/ADR-007.

Critical rules:
- UI never owns artwork/history/lesson truth;
- teacher/trace overlays never become child artwork;
- persistence owns editable operation data, not screenshots;
- Drawing → Coloring handoff requires durable artwork;
- companion/narration/preview failures must never destroy artwork.

## Companion / visual system

- Companion: `docs/09_COMPANION_SPEC.md`.
- Direction: **Premium Storybook Art Studio**.
- Visual system: `docs/21_VISUAL_SYSTEM.md`.
- Figma file key: `2lGC11EPu2tjgrpYivJ8hf`.
- Figma Starter MCP quota may block automated frame generation; Git visual spec remains authoritative.

## Safety/privacy/release baseline

- `docs/22_SAFETY_PRIVACY_RELEASE_REVIEW.md`
- `docs/23_PARENT_GATE_SPEC.md`
- `docs/11_TEST_STRATEGY.md`
- `docs/12_RELEASE_STRATEGY.md`

Defaults: no ads, no mandatory child account, no behavioral analytics for Alpha, local/private artwork, no unnecessary sensitive permissions, no third-party runtime SDK without child-directed/privacy/manifest review.

## Phase 1 outcome

`0.1.0-art-lab`: installable internal APK proving low-latency drawing, structured stroke capture, Undo/Redo, durable editable Save/Load, deterministic five-speed teacher playback, stress/recovery evidence and physical-device validation.

Art Lab is an engineering surface, not the polished production child UI.

## Next executable task

Start **#12 P1.5 deterministic teacher playback** on an isolated branch while P1.4 physical verification occurs.

Playback must:
- use owned recorded stroke data;
- use a deterministic virtual clock rather than frame-dependent geometry mutation;
- support locked pace profiles;
- pause/resume without geometry drift;
- remain a teacher overlay separate from child document history;
- support replay after persistence/reload without changing child artwork.

## Never lose these constraints

- required critical-path spend remains ₹0 where a professional free option exists;
- core drawing/lesson experience works offline;
- no cloud AI dependency in the early critical path;
- no public child social features;
- no behavioral ads;
- important decisions/evidence belong in Git, not only chat.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `docs/14_PHASE0_EXIT_GATE.md`
4. `ROADMAP.md`
5. Epic #7 and next incomplete dependency
6. relevant specs/issues

When chat memory and repository state disagree, Git is authoritative.
