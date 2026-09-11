# 05 — Content Architecture v0.2

**Status:** Phase 0.4 locked overview

## Core rule

Lessons are structured content interpreted by engines, never hard-coded tutorial screens. A single lesson source should support multiple teaching modes and pace profiles wherever pedagogically sensible.

## Authoritative content contracts

- Machine-readable lesson schema: `schemas/lesson.schema.json`
- Validated example lesson: `examples/cute-cat.lesson.json`
- Package and authoring contract: `docs/16_LESSON_PACKAGE_AND_AUTHORING.md`
- Category/skill taxonomy and starter curriculum: `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`

## Content dimensions

Each release lesson declares:
- stable lesson ID and revision;
- age bands;
- difficulty 1–5;
- estimated duration;
- category IDs;
- target skill IDs;
- supported teaching modes;
- prerequisite/related journey metadata where relevant;
- drawing steps;
- authored Help Ladder entries where needed;
- coloring support and coloring steps where applicable;
- localizable narration/string keys;
- asset references.

Age band, difficulty, category and skill are independent dimensions. A child is never assigned a permanent skill label merely because they used an easier lesson.

## Source and compiled content

Human-readable authoring sources are validated and then compiled/packaged for app consumption. Runtime package encoding may optimize stroke data later without changing the conceptual authoring contract.

All geometry is authored in a logical lesson coordinate system rather than device pixels.

## Pace model

Teacher geometry is authored once. Content may provide normal demonstration timing; the Lesson/Drawing engines apply Extra Slow, Slow, Normal, Fast and Very Fast timing profiles at runtime.

Do not author five geometry copies of one demonstration.

## Help model

Independent attempt is the default. Optional authored assistance progresses through:
1. gentle hint;
2. visual guide;
3. direction/anchors;
4. trace path;
5. assisted success.

Help must preserve the child's existing work.

## Localization and narration

Child-facing copy is referenced by semantic string keys. The default locale must be complete for release content. Android TTS can render text in V1; optional prerecorded audio can later map to the same semantic content without changing lesson logic.

## Initial category families

- Foundations
- Animals
- Nature
- Everyday / Food / Toys
- Vehicles
- Space
- People
- Characters

The taxonomy is extensible, but released IDs should remain stable for progress/recommendation compatibility.

## Starter Art Journeys

Phase 0 locks four initial curriculum directions:
- `journey.first_shapes_to_pictures`
- `journey.animal_artist`
- `journey.space_artist`
- `journey.character_creator`

Each journey must represent actual skill progression, not merely a themed playlist.

## Public V1 catalog target

Target: **36 guided lessons** across the core categories and age bands.

Hard release floor: **24 complete, high-quality lessons** if reducing quantity materially improves teaching quality and reliability while preserving credible coverage for all promised age bands/interests.

## Content production rule

Before scaling to dozens of lessons, the representative content set must prove:
- tracing-friendly early-child lesson;
- normal Draw With Me lesson with Help Ladder;
- Watch Then Draw behavior;
- grouped multi-stroke demonstration;
- guided coloring regions;
- older-child detail/proportion lesson;
- open-ended creative variation.

If these cannot be authored cleanly through the schema and engines, catalog expansion pauses and architecture is corrected first.

## Validation

Schema correctness alone is not release readiness. Automated validation must check structure/references/assets/localization; content QA must additionally evaluate pacing, step boundaries, age fit, help usefulness, creative freedom and final artwork quality.

The initial `cute-cat` example was validated against the Draft 2020-12 lesson schema before commit.