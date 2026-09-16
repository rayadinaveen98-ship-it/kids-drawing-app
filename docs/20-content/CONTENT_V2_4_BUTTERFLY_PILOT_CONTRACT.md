# Content V2.4 — Butterfly Cross-Age Pilot Contract

Status: **FROZEN PILOT CONTRACT — authoring gate active**  
Parent: #103  
Slice: #112  
Verified baseline: `05b3ddf2ba1acdd824a84f2720eb5ae47522966d` / Android CI #792 GREEN

## 1. Purpose

This is the first deliberate Content Library V2 expansion after capability validation, Catalog Index V2 and Content Studio were proven on merged main. It adds exactly four Butterfly release lessons: one intentionally designed for each existing age band.

> A lesson may advertise an option only when that exact lesson has complete authored data and the accepted runtime can execute it end to end.

Shared subject matter does not imply shared capabilities. Each lesson owns its own teacher geometry, child-turn expectations, Help outputs, trace geometry when advertised, coloring behavior when advertised, strings and discovery artwork.

## 2. Scope guardrails

Allowed changes:
- four new lesson package roots under `app/src/main/assets/lessons/`;
- regenerated/committed Catalog Index V2;
- deterministic tests/evidence/CI assertions needed for a 28-lesson catalog;
- this contract and pilot-specific evidence.

Forbidden:
- lesson-ID-specific runtime branches;
- child-facing runtime AI;
- accounts, network dependencies or Android permissions;
- weakening `LessonPackageLoader`, `LessonCapabilityValidator`, taxonomy validation, Content Studio promotion gates or Catalog Index V2 drift checks;
- modifying authored package data/behavior of the accepted 24 existing lessons;
- inventing collection/content-family IDs or unreviewed taxonomy IDs.

## 3. Frozen taxonomy policy

The pilot uses only existing `CatalogTaxonomyV2` IDs. All four lessons use categories from `animals` and `nature`, journey `journey.animal_artist`, and only the registered skill IDs listed below. No collection/content-family ID is authored. No prerequisites are authored between age variants.

## 4. Frozen identities and metadata

| Lesson ID | Title | Age band | Difficulty | Minutes | Minimum content API |
|---|---|---|---:|---:|---:|
| `butterfly-big-shapes` | Butterfly from Big Shapes | `little_artists` | 1 | 7 | **2** |
| `butterfly-pattern-play` | Butterfly Pattern Play | `creative_explorers` | 2 | 10 | 1 |
| `butterfly-symmetry-study` | Butterfly Symmetry & Proportion | `growing_artists` | 3 | 13 | 1 |
| `butterfly-observation-study` | Observational Butterfly | `young_artists` | 4 | 16 | 1 |

All begin at revision `1`, status `release`, canvas `1000 × 1000`, `paper_warm`, with authored English strings. `butterfly-big-shapes` requires content API 2 because it declares prepared coloring regions; the other three require API 1.

Metadata:
- `butterfly-big-shapes`: categories `animals`, `nature`; skills `shape.ellipse`, `line.curve`, `placement.symmetry`; tags `butterfly`, `shapes`, `symmetry`, `beginner`.
- `butterfly-pattern-play`: categories `animals`, `nature`; skills `shape.combine`, `placement.symmetry`, `pattern`, `creativity.variation`; tags `butterfly`, `patterns`, `symmetry`, `creative`.
- `butterfly-symmetry-study`: categories `animals`, `nature`; skills `proportion.basic`, `placement.symmetry`, `line.control`, `detail.layering`; tags `butterfly`, `symmetry`, `proportion`, `details`.
- `butterfly-observation-study`: categories `animals`, `nature`; skills `observation`, `contour.refinement`, `proportion`, `detail.texture`, `placement.symmetry`; tags `butterfly`, `observation`, `contour`, `texture`.

## 5. Frozen capability matrix

| Lesson | Draw With Me | Watch Then Draw | Trace & Learn | Help | Coloring |
|---|---|---|---|---|---|
| `butterfly-big-shapes` | READY | NOT_DECLARED | READY | levels 1, 2, 4 | GUIDED_PREPARED |
| `butterfly-pattern-play` | READY | READY | READY | levels 1, 2, 3, 4 | FREEHAND |
| `butterfly-symmetry-study` | READY | READY | NOT_DECLARED | levels 1, 2, 3 | NONE |
| `butterfly-observation-study` | READY | READY | NOT_DECLARED | levels 1, 2, 3 | NONE |

Rules:
- only READY modes appear in `supportedModes`;
- every Trace lesson provides accepted authored trace geometry for every structured step;
- non-Trace lessons do not use `trace_path` Help;
- every Help entry resolves to real authored text and/or guide geometry;
- no package authors `authored_signal`, `enforceSuggestedColors=true`, audio declarations, new `toolPreset`, or `playAsGroup` behavior.

## 6. Frozen drawing plans

### `butterfly-big-shapes` — Little Artists
1. `body_and_antennae`: long oval body + two simple antenna curves.
2. `big_wings`: two large upper wings + two smaller lower wings.
3. `friendly_spots`: mirrored pair of simple wing spots.

Every step: teacher strokes, `manual_done`, expected strokes, level-1 narrated hint, level-2 visual guide, level-4 trace guide. Coloring is `guided` with real polygon regions `left-wing-region`, `right-wing-region`, `body-region`.

### `butterfly-pattern-play` — Creative Explorers
1. `body_centerline`
2. `upper_wings`
3. `lower_wings`
4. `pattern_pair`

Every step: teacher strokes, `manual_done`, expected strokes, Help levels 1/2/3/4 with real trace geometry. Coloring is `self` FREEHAND with exactly one regionless coloring step.

### `butterfly-symmetry-study` — Growing Artists
1. `body_axis`
2. `upper_wing_proportion`
3. `lower_wing_proportion`
4. `vein_details`

Every step: teacher strokes, `manual_done`, expected strokes, Help levels 1/2/3. Trace and coloring are intentionally absent.

### `butterfly-observation-study` — Young Artists
1. `body_and_axis`
2. `upper_wing_contours`
3. `hindwing_contours`
4. `vein_network`
5. `edge_texture`

Every step: teacher strokes, `manual_done`, expected strokes, Help levels 1/2/3. Trace and coloring are intentionally absent. No scores, ranks, mastery percentages or ability labels.

## 7. Asset contract

Every package contains:

```text
lesson.json
strokes.json
strings/en.json
thumbnail.svg
preview.svg
```

`butterfly-big-shapes` additionally contains `coloring/regions.json` and declares it from `assets.coloringRegions`.

Required rules:
- relative safe paths only;
- safe self-contained SVGs;
- every referenced string resolves;
- stroke/guide/region IDs are stable and unique;
- strokes stay within 1000 × 1000, have at least two points and monotonic `timeMs`;
- prepared polygons are in-bounds, non-degenerate and non-self-intersecting.

## 8. Expected aggregate release health

With only these four additions and the existing 24 packages untouched:
- release lessons: `28`;
- errors: `0`;
- accepted warnings: exactly `6`;
- age bands: `LITTLE_ARTISTS=9`, `CREATIVE_EXPLORERS=19`, `GROWING_ARTISTS=18`, `YOUNG_ARTISTS=11`;
- difficulty: `1=6`, `2=10`, `3=7`, `4=4`, `5=1`;
- modes: `DRAW_WITH_ME=27`, `WATCH_THEN_DRAW=20`, `TRACE_AND_LEARN=6`;
- Help-ready: `28`;
- Trace-ready: `6`;
- coloring: none `22`, freehand `2`, guided-prepared `4`, prepared-only `0`;
- `journey.animal_artist` size: `10`;
- unknown taxonomy references: `0`;
- duplicate identities: `0`;
- prerequisite graph failures: `0`;
- committed index drift: `0`.

These are expected evidence values, not reasons to weaken validators if authored truth disagrees.

## 9. Deterministic acceptance gates

Before merge:
1. all 28 release packages load through production `LessonPackageLoader`;
2. all 28 pass `LessonCapabilityValidator`;
3. each Butterfly package matches this exact capability matrix;
4. every advertised mode reaches the accepted Lesson Engine path;
5. every Butterfly string/asset/stroke/guide/region reference resolves;
6. Catalog Index V2 is regenerated from validated package truth;
7. committed/generated index bytes match exactly with 28 entries;
8. taxonomy diagnostics remain empty;
9. existing 24 package round-trip/equivalence evidence remains green;
10. Home remains metadata-only and selected package loading remains lazy;
11. Content Studio remains isolated from child runtime;
12. Android permission allowlist and P6.4 APK identity remain unchanged;
13. exact-head Android CI is fully green.

After merge, push-triggered Android CI on the exact merged-main SHA must also be fully green before #112 closes.

## 10. Stop condition

This pilot does not authorize broad automatic lesson generation. Only after #112 is merged-main green may #103 define the next controlled library-expansion batch, informed by pilot evidence and physical APK review.
