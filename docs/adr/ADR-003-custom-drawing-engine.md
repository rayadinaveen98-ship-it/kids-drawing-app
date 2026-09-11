# ADR-003 — Owned Drawing Engine

**Status:** Accepted; implementation clarified by ADR-007  
**Date:** 2026-09-11

## Decision
Own the product-level Drawing Engine contract rather than relying on a proprietary paid drawing SDK or embedding drawing behavior directly in UI code.

The owned engine includes our document semantics, tool presets, history, persistence envelope, deterministic teacher playback, eraser operations, lesson-facing interfaces and product quality gates.

## Clarification
Owning the engine does **not** mean reimplementing every low-level rendering primitive. Stable free/open Android platform and Jetpack components may be used behind our abstractions when they provide better latency, compatibility or correctness.

See ADR-007 for the stable Jetpack Ink substrate decision.

## Reason
The tutoring experience depends on structured stroke data, replay, pacing, assistance, persistence and long-term product control. These semantics must remain ours even if the renderer/input substrate evolves.