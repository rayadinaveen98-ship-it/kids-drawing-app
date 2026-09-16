# Content V2.4 — Butterfly Cross-Age Pilot Contract

Status: **FROZEN PILOT CONTRACT — authoring gate active**  
Parent: #103  
Slice: #112  
Verified baseline: `05b3ddf2ba1acdd824a84f2720eb5ae47522966d` / Android CI #792 GREEN

## 1. Purpose

This pilot is the first deliberate Content Library V2 expansion after the capability contract, Catalog Index V2 and Content Studio workflow were proven on merged main.

It adds exactly four new Butterfly release lessons: one intentionally designed for each existing age band. The pilot proves age differentiation, per-lesson capability truth, deterministic authoring/export and catalog integration before any broad library generation begins.

The governing rule remains:

> A lesson may advertise an option only when that exact lesson has complete authored data and the accepted runtime can execute it end to end.

Shared subject matter does not imply shared capabilities. Each lesson owns its own teacher geometry, child-turn expectations, Help outputs, trace geometry when advertised, coloring behavior when advertised, strings and discovery artwork.

## 2. Scope guardrails

This slice may change:
- four new lesson package roots under `app/src/main/assets/lessons/`;
- the generated/committed Catalog Index V2 artifact;
- deterministic tests/evidence/CI assertions required to prove a 28-lesson release catalog;
- this pilot contract and pilot-specific content-health evidence.

This slice must not:
- add lesson-ID-specific runtime branches;
- add child-facing runtime AI;
- add accounts, network dependencies or Android permissions;
- weaken `LessonPackageLoader`, `LessonCapabilityValidator`, taxonomy validation, Content Studio promotion gates or Catalog Index V2 drift checks;
- change behavior or authored package data for the accepted 24 existing lessons;
- invent collection/content-family IDs;
- add new taxonomy IDs unless this frozen contract is deliberately revised first.

## 3. Frozen taxonomy policy

The pilot uses only IDs already registered by `CatalogTaxonomyV2`.

All four lessons use:
- categories from `animals` and `nature`;
- journey `journey.animal_artist`;
- only existing registered skill IDs listed in the lesson matrix below.

No collection or content-family ID is authored in V2.4. The four lessons are a pilot family by project scope, not by introducing an unreviewed runtime taxonomy dimension.

No prerequisites are authored between the four lessons. Age targeting and recommendation metadata differentiate them; one age band must not be forced to complete another age band's version first.

## 4. Frozen lesson identities

| Lesson ID | Child-facing title | Age band | Difficulty | Est. minutes |
|---|---|---|---:|---:|
| `butterfly-big-shapes` | Butterfly from Big Shapes | `little_artists` | 1 | 7 |
| `butterfly-pattern-play` | Butterfly Pattern Play | `creative_explorers` | 2 | 10 |
| `butterfly-symmetry-study` | Butterfly Symmetry & Proportion | `growing_artists` | 3 | 13 |
| `butterfly-observation-study` | Observational Butterfly | `young_artists` | 4 | 16 |

Every package begins at revision `1`, status `release`, minimum content API `1`, logical canvas `1000 × 1000`, and default-locale authored English strings.

## 5. Frozen metadata matrix

### `butterfly-big-shapes`
- categories: `animals`, `nature`
- skills: `shape.ellipse`, `line.curve`, `placement.symmetry`
- journey: `journey.animal_artist`
- tags: `butterfly`, `shapes`, `symmetry`, `beginner`

### `butterfly-pattern-play`
- categories: `animals`, `nature`
- skills: `shape.combine`, `placement.symmetry`, `pattern`, `creativity.variation`
- journey: `journey.animal_artist`
- tags: `butterfly`, `patterns`, `symmetry`, `creative`

### `butterfly-symmetry-study`
- categories: `animals`, `nature`
- skills: `proportion.basic`, `placement.symmetry`, `line.control`, `detail.layering`
- journey: `journey.animal_artist`
- tags: `butterfly`, `symmetry`, `proportion`, `details`

### `butterfly-observation-study`
- categories: `animals`, `nature`
- skills: `observation`, `contour.refinement`, `proportion`, `detail.texture`, `placement.symmetry`
- journey: `journey.animal_artist`
- tags: `butterfly`, `observation`, `contour`, `texture`

## 6. Frozen capability matrix

| Lesson | Draw With Me | Watch Then Draw | Trace & Learn | Help | Coloring |
|---|---|---|---|---|---|
| `butterfly-big-shapes` | READY | NOT_DECLARED | READY | READY: levels 1, 2, 4 | GUIDED_PREPARED |
| `butterfly-pattern-play` | READY | READY | READY | READY: levels 1, 2, 3, 4 | FREEHAND |
| `butterfly-symmetry-study` | READY | READY | NOT_DECLARED | READY: levels 1, 2, 3 | NONE |
| `butterfly-observation-study` | READY | READY | NOT_DECLARED | READY: levels 1, 2, 3 | NONE |

Rules:
- only modes listed READY appear in `supportedModes`;
- every Trace lesson provides accepted authored trace geometry for every structured drawing step;
- no non-Trace lesson may contain Help kind `trace_path` merely for convenience;
- each Help entry must resolve to real authored text and/or guide output appropriate to its kind;
- no lesson authors `authored_signal`, `enforceSuggestedColors=true`, audio declarations, a new `toolPreset`, or new `playAsGroup` behavior;
- existing reserved fields from imported legacy packages are irrelevant to these four new packages and are not introduced.

## 7. Frozen drawing plans

### A. Little Artists — `butterfly-big-shapes`
Goal: a recognizable Butterfly from large forgiving forms.

Steps:
1. `body_and_antennae` — long oval body plus two simple antenna curves.
2. `big_wings` — two large upper wings and two smaller lower wings, authored symmetrically.
3. `friendly_spots` — a small mirrored pair of wing spots so symmetry becomes visible without dense detail.

Each step authors:
- teacher stroke refs;
- `manual_done` child turn with expected authored stroke refs;
- level-1 narrated gentle hint;
- level-2 visual guide;
- level-4 trace path using an authored guide.

Coloring is `guided` with real prepared polygon regions:
- left wing;
- right wing;
- body.

Color suggestions, if any, remain non-enforced metadata. No runtime color constraint is implied.

### B. Creative Explorers — `butterfly-pattern-play`
Goal: construct a clean Butterfly and explore mirrored repeated marks.

Steps:
1. `body_centerline` — body/axis and antennae.
2. `upper_wings` — paired upper-wing construction.
3. `lower_wings` — paired lower-wing construction.
4. `pattern_pair` — an authored mirrored starter pattern; completion text invites additional child-created marks after the structured base.

Each step authors:
- teacher stroke refs;
- `manual_done` child turn with expected authored stroke refs;
- level-1 narrated gentle hint;
- level-2 visual guide;
- level-3 direction anchors;
- level-4 trace path.

Coloring is `self` / FREEHAND with exactly one regionless coloring step. Multiple regionless guided steps are forbidden.

### C. Growing Artists — `butterfly-symmetry-study`
Goal: deliberately control centerline, wing proportions and layered detail.

Steps:
1. `body_axis` — narrow body with explicit vertical axis relationship.
2. `upper_wing_proportion` — paired upper wings with controlled size and placement.
3. `lower_wing_proportion` — paired lower wings that relate clearly to upper-wing scale.
4. `vein_details` — a limited set of paired internal wing lines.

Each step authors:
- teacher stroke refs;
- `manual_done` child turn with expected authored stroke refs;
- level-1 narrated gentle hint;
- level-2 visual guide;
- level-3 direction anchors.

Trace & Learn and coloring are intentionally absent.

### D. Young Artists — `butterfly-observation-study`
Goal: a more observational Butterfly study using refined contour, proportion and texture without scoring or grading.

Steps:
1. `body_and_axis` — segmented body and center alignment.
2. `upper_wing_contours` — refined paired upper contours.
3. `hindwing_contours` — refined paired hindwing contours.
4. `vein_network` — selective internal structure lines.
5. `edge_texture` — restrained paired edge/detail marks.

Each step authors:
- teacher stroke refs;
- `manual_done` child turn with expected authored stroke refs;
- level-1 narrated observation hint;
- level-2 visual guide;
- level-3 direction anchors.

Trace & Learn and coloring are intentionally absent. The lesson must not introduce scores, ranks, mastery percentages or ability labels.

## 8. Asset contract

Every lesson package contains:

```text
lesson.json
strokes.json
strings/en.json
thumbnail.svg
preview.svg
```

`butterfly-big-shapes` additionally contains:

```text
coloring/regions.json
```

No other Butterfly package declares prepared coloring regions.

Required asset rules:
- all paths are relative and safe;
- SVG files contain no remote resources, scripts or external references;
- discovery artwork is deterministic and non-authoritative;
- every referenced string key resolves in `strings/en.json`;
- stroke and guide IDs are stable and unique per package;
- stroke points stay within the 1000 × 1000 canvas, contain at least two points and use monotonic `timeMs`;
- coloring polygons are valid, in-bounds and non-degenerate.

## 9. Expected aggregate release health after promotion

If exactly these four lessons are added and existing 24 packages are untouched, the expected catalog becomes:

- release lessons: `28`;
- quality errors: `0`;
- accepted journey warnings: remain exactly `6` (the four pilot lessons all belong to `journey.animal_artist`);
- age-band coverage:
  - `LITTLE_ARTISTS`: 9;
  - `CREATIVE_EXPLORERS`: 19;
  - `GROWING_ARTISTS`: 18;
  - `YOUNG_ARTISTS`: 11;
- difficulty distribution:
  - difficulty 1: 6;
  - difficulty 2: 10;
  - difficulty 3: 7;
  - difficulty 4: 4;
  - difficulty 5: 1;
- teaching modes:
  - `DRAW_WITH_ME`: 27;
  - `WATCH_THEN_DRAW`: 20;
  - `TRACE_AND_LEARN`: 6;
- Help-ready lessons: 28;
- Trace-ready lessons: 6;
- coloring capability distribution:
  - none: 22;
  - freehand: 2;
  - guided prepared: 4;
  - prepared-only: 0;
- `journey.animal_artist` size: 10;
- unknown taxonomy references: 0;
- duplicate identities: 0;
- prerequisite graph failures: 0;
- committed index drift: 0.

These numbers are expected evidence, not a reason to weaken validators if actual authored truth disagrees. Any mismatch must be investigated.

## 10. Deterministic acceptance gates

Before PR promotion, the branch must prove:
1. all 28 release packages decode through production `LessonPackageLoader`;
2. all 28 release packages pass `LessonCapabilityValidator`;
3. each Butterfly package matches this exact capability matrix;
4. every advertised teaching mode reaches an accepted Lesson Engine execution path;
5. every Butterfly string/asset/stroke/guide/coloring reference resolves;
6. Catalog Index V2 is regenerated only from fully validated package truth;
7. committed/generated index bytes match exactly with 28 entries;
8. taxonomy registry diagnostics remain empty;
9. existing 24 release package round-trip/equivalence evidence remains green;
10. Home remains metadata-only and selected package loading remains lazy;
11. Content Studio remains isolated from child runtime;
12. permission allowlist and P6.4 APK identity remain unchanged;
13. exact-head Android CI is fully green.

After merge, the push-triggered Android CI on the exact merged-main SHA must also be fully green before #112 may close.

## 11. Stop condition

This pilot does **not** authorize broad automatic lesson generation merely because four packages validate.

Only after #112 is merged-main green may #103 define the next controlled library-expansion batch, informed by pilot quality evidence and any physical APK review findings.
