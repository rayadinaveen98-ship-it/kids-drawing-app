# ADR-007 — Stable Jetpack Ink as Low-Level Inking Substrate

**Status:** Accepted  
**Date:** 2026-09-11

## Context

AndroidX Ink 1.0.0 is a stable first-party Jetpack library designed for high-performance inking. It provides low-latency authoring, stroke input/geometry, brushes, rendering and stable stroke-input serialization while remaining an on-device library rather than a hosted service.

## Decision

Use **AndroidX Ink 1.0.0 stable** as the preferred low-level inking substrate behind the Kids Drawing Engine abstraction.

Likely stable modules include the minimum set we actually need from:
- `ink-authoring`
- `ink-strokes`
- `ink-brush`
- `ink-rendering-*`
- `ink-storage`
- `ink-geometry`

Exact dependencies will be minimized during Phase 1 scaffolding.

## What remains ours

Jetpack Ink does not become our product architecture. We continue to own:
- `DrawingDocument` and product document versioning;
- product stroke/operation IDs and metadata;
- brush/tool preset semantics;
- undo/redo command history;
- teacher stroke playback timing;
- lesson/help integration;
- eraser-mask behavior required beyond stable Ink capabilities;
- persistence envelope and artwork metadata;
- engine interfaces exposed to UI and other engines;
- performance/reliability quality gates.

## Stability rule

The public/critical path uses stable AndroidX Ink APIs only unless a later ADR explicitly approves a preview dependency after risk review.

In particular, experimental/alpha-only partial-stroke eraser APIs are not required for V1 architecture.

## Fallback/escape hatch

Our product APIs must not expose AndroidX Ink types outside the drawing infrastructure boundary when avoidable. This allows us to replace or augment the substrate later without rewriting Lesson Engine, Gallery, content schemas or application UI.

## Consequences

Positive:
- avoids needless reinvention of low-latency inking infrastructure;
- keeps development cost at ₹0;
- provides first-party stroke/brush/rendering primitives;
- reduces risk around input modeling and serialization;
- preserves our ability to build differentiated tutoring/playback semantics.

Tradeoff:
- adds a Jetpack dependency and requires an adapter/domain boundary;
- some V1 behaviors, especially partial visual erasing and teacher replay semantics, still require our own product-layer implementation and tests.