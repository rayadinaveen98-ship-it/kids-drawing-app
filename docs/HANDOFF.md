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

## Current development philosophy

Do not build all engines in isolation and do not build the complete polished app first. Use a core-engine + vertical-slice approach:

Specify → Build capability → Unit/performance test → Integrate into real child flow → Test → lock milestone → expand.

## Current Phase 0 state

Complete:
- 0.1 Repository & product foundation
- 0.2 V1 product scope contract
- 0.3 UX architecture and screen inventory

Next:
- 0.4 machine-readable lesson/content architecture and starter curriculum
- 0.5 Drawing/Lesson/Coloring engine contracts
- 0.6 companion contract and selected visual direction
- 0.7 final Phase 0 safety/quality/release review

The authoritative checklist is `docs/14_PHASE0_EXIT_GATE.md`.

## UX architecture now locked

- Child shell: Home / Learn / Create / Gallery.
- Guided Drawing, Coloring and Free Draw use immersive creation workspaces with shell navigation hidden.
- Parent Zone is a separate gated graph.
- Safe Exit prevents system-back or close actions from silently destroying artwork.
- Incomplete sessions surface as Continue Drawing rather than being forced on startup.
- Canonical V1 screen IDs live in `docs/15_SCREEN_ARCHITECTURE.md`.

## V1 scope highlights

- Primary ages: approximately 4–12 in four adaptive experience bands.
- Public V1 content target: 30–40 guided lessons; hard release floor 24 high-quality complete lessons.
- Must Have: onboarding/local profile, personalized Home, structured guided drawing, five paces, Help Ladder, coloring, Free Draw, companion, Gallery, Parent Zone, offline persistence.
- Should Have: Match My Speed, Draw From Memory, gentle achievements, downloadable packs, basic parent insights and optional supportive stroke similarity.
- Explicitly not early V1: ads, public social systems, mandatory account, strict drawing scores, unrestricted AI chat, advanced pro editing workflows.

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