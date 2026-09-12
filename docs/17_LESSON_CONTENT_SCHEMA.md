# 17 — Lesson Content Schema

**Status:** Phase 2 runtime/authoring contract  
**Machine-readable source:** `schemas/lesson.schema.json`

This document explains how lesson packages map into runtime behavior. The JSON Schema remains the structural source of truth; the Lesson Engine specification defines behavior.

## 1. Package layout

A bundled lesson lives under one package root, for example:

```text
lessons/cute-cat/
  lesson.json
  strokes.json
  thumbnail.svg
  preview.svg
  strings/
    en.json
```

All paths declared by `lesson.json` must be relative to the package root. Absolute paths, `..` traversal, blank path segments and escaping outside the package are rejected.

## 2. `lesson.json`

Required root fields:
- `schemaVersion` — currently `1.0`;
- `lessonId` — stable content identity;
- `revision` — positive integer used for resume compatibility;
- `status` — `draft`, `review`, or `release`;
- `minimumContentApi` — minimum runtime content API;
- `metadata`;
- `canvas`;
- `supportedModes`;
- `assets`;
- `drawing.steps`.

The runtime rejects unknown JSON properties rather than silently ignoring authoring mistakes.

## 3. Teaching modes

Supported mode IDs:
- `draw_with_me`;
- `watch_then_draw`;
- `trace_and_learn`.

A lesson may support one or more modes. Runtime behavior is defined in `docs/08_LESSON_ENGINE_SPEC.md`.

If `trace_and_learn` is declared, every drawing step must expose an authored trace source. In the initial runtime this means either:
- a `trace_path` Help Ladder entry with at least one valid `guideRef`; or
- non-empty `childTurn.expectedStrokeRefs`, which can act as the canonical trace source.

## 4. Drawing steps

Every step has:
- stable unique `id`;
- one or more `objectiveSkillIds`;
- `teacher.strokeRefs` defining the canonical teacher demonstration;
- a `childTurn` policy;
- optional Help Ladder entries;
- optional completion narration.

Teacher stroke references must resolve against the package stroke catalog before a lesson can start.

### Child completion policies
- `manual_done` — child explicitly chooses Done/Next;
- `any_stroke` — one committed child mark satisfies the step interaction;
- `authored_signal` — reserved for deterministic authored evaluators.

`allowReplay` and `allowSkip` are authored per step. UI cannot invent permission to skip.

## 5. Help Ladder

Help levels are authored from 1–5. Missing intermediate levels are valid and are skipped by the Lesson Engine.

Kinds:
1. `gentle_hint`
2. `visual_guide`
3. `direction_anchors`
4. `trace_path`
5. `assisted_success`

Guide IDs must resolve against the package stroke catalog. Help overlays are presentation-only and never become child artwork.

## 6. Stroke catalog

Phase 2 uses a product-owned `strokes.json` package asset:

```json
{
  "schemaVersion": "1.0",
  "strokes": [
    {
      "id": "head-outline",
      "points": [
        {"x": 100, "y": 100, "timeMs": 0, "pressure": 1.0}
      ]
    }
  ],
  "guides": [
    {"id": "guide-head", "strokeRefs": ["head-outline"]}
  ]
}
```

Rules:
- stroke and guide IDs are unique;
- a stroke has at least two points;
- coordinates are finite and inside the authored canvas;
- timestamps are monotonic within a stroke;
- pressure is `0..1`;
- every guide reference resolves to an authored stroke.

The Lesson Engine never exposes AndroidX Ink types. A later infrastructure adapter converts these product-owned authored points into the frozen Drawing Engine playback input.

## 7. Asset validation

P2.1 validates the structural and semantic assets needed to start a lesson, especially the stroke catalog and referenced IDs. Thumbnail/preview/string/audio paths are constrained to safe package-relative paths. Feature-specific adapters may additionally verify their file existence when those assets are consumed.

## 8. Revision compatibility

An active lesson session stores `lessonId + revision`. Resume behavior must validate that exact content revision before restoring progress. If an incompatible revision is unavailable, preserve child artwork and enter safe recovery instead of guessing.

## 9. Reference lesson

`app/src/main/assets/lessons/cute-cat/` is the permanent Phase 2 reference package. It is expected to remain executable in all three teaching modes and should be updated only through deliberate content-version changes.
