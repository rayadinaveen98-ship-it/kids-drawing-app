# P5.6 Execution Contract — Curriculum Expansion Set E

**Parent epic:** #73  
**Issue:** #84  
**Verified starting main:** `6412e0e6cf346837b26e925cebc89662c27fba2c`  
**Verified starting merged-main CI:** Android CI #528 / run `34840361689` — GREEN  
**Branch:** `phase5/p5-6-curriculum-set-e`  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Expected first distributed QA:** `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode **25**  
**Status:** **LOCKED BEFORE CONTENT IMPLEMENTATION**

## 1. Purpose

P5.6 expands the accepted 20-lesson catalog to the frozen Phase-5 target of **24 production guided lessons** using the final four Set-E lessons:

1. `face-and-expressions@1`
2. `simple-body-and-pose@1`
3. `create-your-character@1`
4. `one-point-room@1`

Set E completes the Character Creator foundation and the credible older-child technique floor. It must not make easier lessons artificially all-ages merely to satisfy coverage numbers.

This slice reuses the verified Drawing Engine, Lesson Engine, content-production system, Companion V2, ADR-008 open-authorship behavior, persistence/Gallery ownership and offline child-safety boundaries. A content need is not permission to redesign an engine.

## 2. Frozen architecture / product baseline

P5.6 retains:

- production `LessonPackageLoader` and auto-discovered `LessonCatalog`;
- stable `(lessonId, revision)` identity;
- AndroidX Ink behind owned drawing infrastructure;
- `LessonSessionState` as the only teaching-state truth;
- generic Draw With Me / Watch Then Draw runtime semantics;
- authored Help Ladder semantics;
- ADR-008 generic open-authorship behavior;
- P5.3 Companion presentation as generic/read-only policy;
- persistence/recovery/Gallery ownership and cross-lesson isolation;
- P5.2 `ContentQualityAnalyzer` + Content Lab as release tooling;
- offline-first, account-free, ad-free child core with no behavioral analytics;
- no similarity scoring, grades, stars/XP/rank, permanent ability labels, punitive streaks or cloud child profiling;
- no lesson-ID-specific product/runtime branches;
- teacher/help/reference overlays never enter child artwork history.

Any proven engine/contract defect must be documented explicitly before implementation changes.

## 3. Final curriculum checkpoint after P5.6

Expected release catalog: **24 lessons**.

Expected age-band support counts:

- `little_artists`: **8**;
- `creative_explorers`: **18**;
- `growing_artists`: **17**;
- `young_artists`: **10**.

Expected difficulty distribution:

- difficulty 1: **5**;
- difficulty 2: **9**;
- difficulty 3: **6**;
- difficulty 4: **3**;
- difficulty 5: **1**.

This satisfies the P5.1 Phase-5 release floors, including at least three credible difficulty-4 experiences and one genuine difficulty-5 Young Artist lesson.

Do not broaden Set-E age eligibility merely to make the counters larger.

## 4. Journey / prerequisite contract

### 4.1 `journey.character_creator`

Required sequence:

1. Face & Expressions
2. Simple Body & Pose
3. Create Your Character

Locked prerequisites:

- Face & Expressions: no prerequisite; journey entry.
- Simple Body & Pose: requires `face-and-expressions`.
- Create Your Character: requires `simple-body-and-pose`.

The journey expresses construction → pose → authored synthesis. Completion is never reference similarity.

### 4.2 One-Point Room

- One-Point Room is intentionally **standalone** in P5.6.
- prerequisite: `sailboat-scene`.
- pedagogical bridge: Sailboat's foreground/background and scale work → explicit one-point perspective/depth scale.
- do not invent an unrelated journey merely to clear analyzer warnings.

## 5. Lesson contract — Face & Expressions

- identity: `face-and-expressions@1`
- age bands: Growing Artists + Young Artists
- difficulty: 3
- target duration: 10 minutes
- categories: `characters`, `portrait`
- skills: `anatomy.face_basic`, `placement.symmetry`, `expression.face`, `proportion.basic`, `creativity.variation`
- journey: `journey.character_creator`
- prerequisite: none
- modes: Draw With Me + Watch Then Draw
- coloring: disabled
- minimumContentApi: 1
- Trace mode/help: not advertised; no full-face Trace

Required drawing turns:

1. `head_construction` — large face/head construction; expected geometry; Help 1 gentle hint + Help 2 visual guide or Help 3 anchors.
2. `eyes_and_brows` — grouped eye/brow landmarks with symmetry as a placement tool, not a score; expected geometry; Help 1 + guide/anchors.
3. `nose_and_mouth` — readable central landmarks; expected geometry; Help 1 + visual guide.
4. `choose_expression` — teacher examples may show expression ideas, but child expression/features are authored; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

The final expression may materially diverge from the preview. Mature profiles must hear technique/choice language rather than toddler praise.

## 6. Lesson contract — Simple Body & Pose

- identity: `simple-body-and-pose@1`
- age bands: Growing Artists + Young Artists
- difficulty: 4
- target duration: 12 minutes
- categories: `characters`, `design`
- skills: `anatomy.body_basic`, `pose.simple`, `proportion.basic`, `character.silhouette`, `creativity.variation`
- journey: `journey.character_creator`
- prerequisite: `face-and-expressions`
- modes: Draw With Me + Watch Then Draw
- coloring: disabled
- minimumContentApi: 1
- Trace: not advertised/helped

Required drawing turns:

1. `torso_and_pelvis` — simple torso/pelvis masses; expected geometry; Help 1 + proportion anchors.
2. `arms_and_legs` — grouped limb construction; expected geometry; Help 1 + direction anchors.
3. `simple_pose` — structural pose/gesture relationship; expected geometry for the teacher example; Help conceptual/anchor based.
4. `contour_and_balance` — simple silhouette/contour refinement around construction; expected structural geometry; Help remains construction focused.
5. `make_pose_yours` — pose/accessory/action ideas only; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

This is gesture/body construction, not anatomy over-complexity. Full-body Trace is not allowed.

## 7. Lesson contract — Create Your Character

- identity: `create-your-character@1`
- age bands: Growing Artists + Young Artists
- difficulty: 4
- target duration: 13 minutes
- categories: `characters`, `design`
- skills: `character.design`, `character.silhouette`, `storytelling.character`, `creativity.imagination`, `creativity.variation`
- journey: `journey.character_creator`
- prerequisite: `simple-body-and-pose`
- modes: Draw With Me + Watch Then Draw
- coloring: disabled
- minimumContentApi: 1
- Trace: not advertised/helped

Required drawing turns:

1. `character_foundation` — simple construction/silhouette base; expected geometry; Help 1 + construction anchors.
2. `face_and_expression` — structural face/expression example; expected geometry for the demonstrated foundation only; Help remains conceptual/visual.
3. `outfit_and_shapes` — teacher example demonstrates clothing/shape language; structural expected refs allowed, but discretionary decoration is never required.
4. `create_your_character` — silhouette variations, outfit, expression, accessories, role/name/story/personality are authored choices; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

The teacher character is a technique reference, not a template. Completion must never require matching face, outfit, colors, body proportions beyond the child's own chosen construction, or discretionary details.

## 8. Lesson contract — One-Point Room

- identity: `one-point-room@1`
- age bands: Young Artists only
- difficulty: 5
- target duration: 15 minutes
- categories: `scenes`, `design`
- skills: `perspective.one_point`, `perspective.depth_scale`, `scene.foreground_background`, `composition.balance`, `scale.relative`
- journey: none in P5.6
- prerequisite: `sailboat-scene`
- modes: Draw With Me + Watch Then Draw
- coloring: disabled
- minimumContentApi: 1
- Trace: not advertised/helped

Required drawing turns:

1. `horizon_and_vanishing_point` — horizon is drawable expected geometry; vanishing point is a **guide/anchor**, not a tiny required child mark.
2. `room_edges` — converging structural lines toward the vanishing point; expected geometry; Help 1 + direction/perspective anchors.
3. `back_wall_and_openings` — back wall / doorway/window structural geometry; expected refs; Help conceptual/visual.
4. `furniture_depth` — one or two simple perspective boxes/furniture masses demonstrating depth scale; structural expected geometry only.
5. `design_your_room` — room purpose, decor, furniture choices, objects and story are authored; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

No Trace fallback. Perspective Help should explain convergence, near/far scale and anchor relationships rather than judge accuracy. The lesson must feel credible for pre-teens and avoid ornamental tiny targets.

## 9. Geometry / artwork rules

All Set-E packages use a 1000×1000 logical canvas.

- main construction geometry stays comfortably within phone margins;
- teacher/expected/help geometry stays within logical bounds;
- previews/thumbnails are attractive finished examples, not mandatory final answers;
- helpers/reference anchors never persist in child artwork;
- face/body targets remain readable at phone size;
- Young-Artist perspective anchors may be precise but must not depend on tiny taps/marks;
- no full Trace path is authored anywhere in Set E;
- final authored turns contain no required expected strokes.

## 10. Help / Companion policy

Growing/Young Set-E help follows P5.1 maturity policy:

- gentle conceptual hint first;
- visual construction guide or direction anchors next;
- replay/demonstration available where useful;
- Trace is not used;
- Help is child-controlled and non-punitive;
- open turns use authorship/choice language, never copy/match/accuracy pressure;
- Companion remains generic and read-only relative to session/help/artwork/persistence;
- Young Artist wording must be concise, studio-oriented and non-toddler.

## 11. Content-quality warning policy

Accepted P5.5 standalone warnings:

- `rainbow-weather`
- `tree-through-seasons`
- `ice-cream-shop`
- `simple-car`
- `sailboat-scene`

P5.6 intentionally adds one standalone release lesson:

- `one-point-room`

Therefore the final 24-lesson catalog may contain exactly **six** reviewed `NO_JOURNEY_MEMBERSHIP` warnings for those six IDs and no others.

This is not blanket suppression. Geometry/tiny-target/broken-reference/missing-localization/invalid-mode/help or any other warning/error remains a failure. Do not invent journey membership merely to make warning count zero.

## 12. Automated test contract

P5.6 tests must prove at minimum:

- catalog grows 20 → 24 through production discovery;
- loader diagnostics remain empty;
- all four IDs/revisions direct-load through strict production loader;
- metadata/age/difficulty/categories/skills/modes match this contract;
- Character Creator contains Face → Body → Create Character with locked prerequisites;
- One-Point Room remains standalone and requires Sailboat Scene;
- all assets/localization/teacher refs/expected refs/help guide refs resolve;
- all final authored turns use `MANUAL_DONE + allowSkip + expectedStrokeRefs=[]`;
- no Set-E package advertises Trace mode or contains `TRACE_PATH` Help;
- teacher/expected/help geometry remains inside canvas;
- generic Companion integration stays mature/non-punitive across Growing + Young profiles;
- final report = **24 lessons / 0 errors / exactly six reviewed standalone warnings**;
- final age counts = **8 / 18 / 17 / 10**;
- final difficulty counts = **5 / 9 / 6 / 3 / 1** for difficulties 1–5;
- P5.5 regression assertions become expansion-safe rather than hardcoding 20 as the forever catalog size.

## 13. Batch gates

### Batch A — Face & Expressions + Simple Body & Pose

Target catalog: **22 lessons**.

Require:
- both packages fully authored (lesson/strokes/strings/preview/thumbnail);
- Character Creator entry/progression tests;
- no Trace;
- open authorship tests;
- production quality errors = 0;
- known standalone warning policy preserved;
- exact-head Android CI GREEN before Batch B.

### Batch B — Create Your Character + One-Point Room

Target catalog: **24 lessons**.

Require:
- both packages fully authored;
- final Character Creator journey tests;
- One-Point perspective structure/help tests;
- generic Companion integration across all four;
- 24-lesson final age/difficulty/quality gates;
- exact-head Android CI GREEN before QA freeze.

## 14. Content Lab gate

Before QA freeze inspect all four Set-E lessons:

- metadata and stable identities;
- preview/thumbnail phone readability;
- every step and teacher/expected/help overlay;
- open-turn lack of forced expected geometry;
- Face symmetry/landmark guides;
- Body pose/proportion anchors;
- Character authored variation;
- One-Point horizon/vanishing-point/convergence/depth guides;
- localization resolution;
- diagnostics and exact warning policy;
- no visual/content defect requiring binary/content change.

## 15. QA / physical acceptance contract

After the full 24-lesson gate is green:

1. freeze `0.5.0-curriculum-expansion-p5.6-qa1`;
2. first distributed candidate uses **versionCode 25**; never reuse 24;
3. exact-head CI produces debug + release-like profile APK evidence;
4. record artifact IDs, APK byte sizes and SHA-256 hashes;
5. run focused Content Lab and exact-profile physical acceptance;
6. physical pass covers all four Set-E flows/modes/help/open authorship, mature age tone, Character Creator progression, One-Point perspective usability, save/reopen, Gallery, cross-lesson isolation, offline/Airplane Mode, lifecycle, and representative prior-content regression smoke;
7. any binary/content-changing defect invalidates the candidate and requires a new versionCode/evidence set;
8. final acceptance evidence must pass exact-head CI before merge.

## 16. Planned delivery order

1. this execution contract — first P5.6 branch commit;
2. continuation docs + draft PR;
3. contract/docs exact-head CI GREEN;
4. Batch A — Face + Body → 22 lessons → CI GREEN;
5. Batch B — Character + One-Point Room → 24 lessons → CI GREEN;
6. Content Lab inspection;
7. QA1 v25 freeze + immutable artifact evidence;
8. focused exact-profile physical acceptance;
9. acceptance docs + final CI;
10. PR ready → squash merge → merged-main CI;
11. close #84 and freeze P5.6 before P5.7 begins.

## 17. Definition of done

P5.6 is complete only when the production catalog reaches 24, final age/difficulty floors are satisfied by credible content, Character Creator is coherent, One-Point Room is a genuine Young-Artist D5 technique lesson, quality is 0 errors with only the six reviewed standalone warnings, exact v25 QA evidence is physically accepted, and the slice is squash-merged with merged-main CI green.
