# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`

This file exists so a new conversation, developer, or agent can resume without reconstructing the project from chat history.

## What are we building?

An Android-first children's drawing and art-learning application. The product behaves like an exceptionally patient personal art teacher. Content, UI complexity, guidance, and tools adapt to age and demonstrated ability.

## Primary product differentiators

1. Live draw-along teaching rather than passive videos.
2. Child-controlled teaching pace.
3. Adaptive Help Ladder: Independent → Hint → Guide → Trace → Assisted Success.
4. Drawing seamlessly continues into coloring.
5. Expressive companion participates throughout the lesson.
6. Age-adaptive UI and curriculum.
7. Serious Free Draw Studio that grows with the child.
8. Story Drawing and creative-expression features later.
9. Offline-first and privacy-conscious.

## Current development philosophy

Do not build all engines in isolation and do not build the complete polished app first. Use a core-engine + vertical-slice approach:

Specify → Build capability → Unit/performance test → Integrate into real child flow → Test → lock milestone → expand.

## Immediate target after Phase 0

`Art Lab 0.1`: an internal engineering screen proving low-latency drawing, stroke recording and deterministic multi-speed stroke playback.

## Never lose these constraints

- Required software/service spend should remain ₹0 for the development path whenever a professional free option exists.
- Core drawing/lesson use must not depend on internet access.
- Do not introduce cloud AI into the critical path early.
- No public child social features.
- No behavioral ads.
- Do not expose complex tools to younger children simply because the engine supports them.

## Resume protocol

At the start of any new chat or development session, inspect `PROJECT_STATUS.md`, this handoff, `ROADMAP.md`, recent commits, and open issues before making product or engineering changes. Git is authoritative when chat memory and repository state disagree.
