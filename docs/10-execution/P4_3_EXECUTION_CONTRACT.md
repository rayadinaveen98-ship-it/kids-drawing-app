# P4.3 Execution Contract — Representative Content Set A

**Issue:** #60  
**Parent:** #57  
**Depends on:** #58, #59  
**Branch:** `phase4/p4-3-representative-content-a`  
**Baseline:** merged P4.2 main `131eee68fd24dad4a80743e10149a867defb59ce`

## Objective

Prove that the frozen lesson/content architecture can ship materially different child teaching experiences through structured packages only, with no lesson-ID branching in product UI.

## Frozen principles

1. Lessons remain structured content interpreted by the existing Lesson + Drawing engines.
2. No P4.3 change may special-case a lesson ID in product UI/runtime routing.
3. Release content must load through `LessonCatalog` and the same strict `LessonPackageLoader` used by production.
4. Teacher/trace/help overlays remain outside child artwork/history.
5. Existing `cute-cat` revision 1 remains unchanged so Phase 3 persisted identity compatibility is preserved.
6. All new geometry uses the canonical logical 1000×1000 lesson canvas and one authored timing source; the engine still owns five pace profiles.
7. Child-facing copy stays semantic/localizable and patient, never punitive.
8. P4.3 coloring stays disabled for the four new lessons; prepared-region coloring is intentionally P4.5 scope.

## Representative lessons

### `smiling-sun` — tracing-friendly first picture
- Journey: `journey.first_shapes_to_pictures`
- Ages: Little Artists + Creative Explorers
- Difficulty: 1
- Modes: Trace & Learn + Draw With Me
- Skills: circle control, straight rays, relative placement
- Quality intent: very simple recognizable artwork with explicit trace support on every step.

### `friendly-owl` — full Help Ladder animal
- Journey: `journey.animal_artist`
- Ages: Creative Explorers + Growing Artists
- Difficulty: 2
- Mode: Draw With Me
- Skills: ellipse/circle construction, symmetry, curved wings, shape combination
- Help contract: every drawing step authors levels 1→5 (`gentle_hint`, `visual_guide`, `direction_anchors`, `trace_path`, `assisted_success`).

### `simple-rocket` — Watch Then Draw
- Journey: `journey.space_artist`
- Ages: Creative Explorers + Growing Artists
- Difficulty: 2
- Mode: Watch Then Draw
- Skills: ellipse construction, symmetry, shape combination, curve control
- Quality intent: clear remember-then-draw chunks with replayable grouped demonstrations.

### `easy-flower` — grouped multi-stroke demonstration
- Journey: `journey.first_shapes_to_pictures`
- Prerequisite: `smiling-sun`
- Ages: Little Artists + Creative Explorers + Growing Artists
- Difficulty: 2
- Modes: Draw With Me + Watch Then Draw
- Skills: circles/ellipses, repeated placement, stem/leaf construction
- Grouping contract: petal ring and stem/leaves are authored with `playAsGroup=true` and multiple stroke refs.

## Automated acceptance

1. Production catalog exposes exactly the existing Cute Cat plus all four P4.3 lessons with no catalog diagnostics.
2. Every new package direct-loads successfully through `LessonPackageLoader`.
3. `smiling-sun` exposes Trace & Learn and each step satisfies authored trace support.
4. `friendly-owl` carries complete 1–5 Help Ladder coverage on every step.
5. `simple-rocket` is a Watch Then Draw lesson and all child turns remain replayable.
6. `easy-flower` contains grouped demonstrations with multiple stroke refs and orders after `smiling-sun` through authored prerequisites.
7. Catalog age coverage includes Little Artists, Creative Explorers and Growing Artists.
8. Existing Cute Cat remains present and unchanged as the regression baseline.
9. Exact-head Android CI is green before merge.

## Product/content QA

Beyond structural validity, review the four lessons for:
- coherent step boundaries;
- understandable pace at Extra Slow through Very Fast via the frozen engine pace model;
- useful help rather than answer-dumping;
- satisfying final silhouettes at phone scale;
- age-appropriate copy and complexity;
- safe replay/skip behavior;
- no teacher/guide contamination of child history;
- offline execution through generic product routes.

## Completion gate

P4.3 closes only when all four release packages and their assets/tests are merged, exact-head and merged-main CI are green, Cute Cat regression remains green, and representative content QA evidence is recorded. Physical/product usability claims must not be overstated if a real-device pass has not been performed.
