# P5.5 Execution Contract — Curriculum Expansion Set D

**Parent epic:** #73  
**Issue:** #82  
**Verified starting main:** `00c618cb9b16444f77e534431b3ca417e85a3e10`  
**Verified starting merged-main CI:** Android CI #514 / run `34834247565` — GREEN  
**Branch:** `phase5/p5-5-curriculum-set-d`  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Expected first distributed QA versionCode:** **24**  
**Status:** **LOCKED BEFORE CONTENT IMPLEMENTATION**

## 1. Purpose

P5.5 expands the accepted 14-lesson catalog to **20 production lessons** through six Set-D lessons focused on animals, vehicles/scenes and space.

This slice must prove curriculum depth and progression, not merely catalog growth. It reuses the frozen Phase-5 platform and content-production system; it does not reopen proven engines without a concrete defect and an explicit contract/ADR update.

Locked Set D:

1. `snail-garden@1`
2. `elephant-from-shapes@1`
3. `simple-car@1`
4. `sailboat-scene@1`
5. `planet-with-rings@1`
6. `friendly-alien@1`

No P5.6 people/character/perspective lesson may be substituted into this slice.

## 2. Frozen architecture / product baseline

P5.5 retains:

- production `LessonPackageLoader` and auto-discovered `LessonCatalog`;
- stable `(lessonId, revision)` identity;
- Drawing Engine / AndroidX Ink ownership boundary;
- `LessonSessionState` as the only teaching-state truth;
- Draw With Me / Watch Then Draw generic runtime behavior;
- authored Help Ladder semantics;
- ADR-008 generic open-authorship/Trace behavior;
- P5.3 `ProductLessonPresentationPolicy` as read-only Companion presentation;
- persistence/recovery/Gallery ownership and cross-lesson isolation;
- P5.2 `ContentQualityAnalyzer` + Content Lab as the production quality/inspection system;
- offline-first, account-free, ad-free child core with no behavioral analytics;
- no similarity scoring, grades, stars/XP/rankings, permanent ability labels or punitive streaks;
- no lesson-ID-specific product/runtime UI branches;
- teacher/help/reference overlays never enter child artwork history.

A Set-D content need is not permission to redesign an engine. Any proven contract defect requires explicit documentation before implementation changes.

## 3. Curriculum checkpoint after P5.5

Expected release catalog after P5.5: **20 lessons**.

Expected age-band coverage after the six additions, based on the accepted 14-lesson P5.4 catalog:

- `little_artists`: **8** suitable lessons;
- `creative_explorers`: **18**;
- `growing_artists`: **14**;
- `young_artists`: **6**.

This deliberately reaches the Phase-5 minimum Little/Creative/Growing coverage before P5.6. Young Artist coverage is completed by P5.6 Face/Body/Character/One-Point-Room work; do not dilute age eligibility merely to inflate counts early.

## 4. Journey / prerequisite contract

### 4.1 `journey.animal_artist`

Required Phase-5 sequence:

1. Little Fish
2. Snail Garden
3. Cute Cat
4. Friendly Owl
5. Elephant From Shapes
6. Fox Portrait

Set-D metadata:

- Snail Garden belongs to `journey.animal_artist` and requires `little-fish`.
- Elephant From Shapes belongs to `journey.animal_artist` and requires `friendly-owl`.
- retained legacy lessons are not rewritten merely to force a linear prerequisite chain; journey order expresses the full pedagogical progression.

### 4.2 Vehicles / scenes

- Simple Car is intentionally standalone in P5.5 and requires `shape-friends`.
- Sailboat Scene is intentionally standalone in P5.5 and requires `simple-car`.
- this creates a local construction → subject-in-environment progression without inventing a fake vehicle journey that P5.1 never approved.

### 4.3 `journey.space_artist`

Required Phase-5 sequence:

1. Planet With Rings
2. Simple Rocket
3. Friendly Alien
4. Design Your Spaceship

Set-D metadata:

- Planet With Rings belongs to `journey.space_artist` and has no prerequisite; it is the entry point.
- Friendly Alien belongs to `journey.space_artist` and requires `simple-rocket`.
- retained Simple Rocket / Design Your Spaceship are not rewritten merely to manufacture a stricter prerequisite chain.

## 5. Lesson contract — Snail Garden

- ID/revision: `snail-garden@1`
- age bands: Little Artists + Creative Explorers
- difficulty: 2
- target duration: 8 minutes
- categories: `animals`, `nature`, `nature.plants`
- skills: `line.curve`, `line.loop`, `shape.organic`, `overlap.basic`, `creativity.variation`
- journey: `journey.animal_artist`
- prerequisite: `little-fish`
- modes: Draw With Me + Watch Then Draw
- structured coloring: disabled
- Trace mode: not advertised

Required child-turn shape:

1. `shell_spiral` — large readable shell/spiral construction; expected geometry; Help 1 gentle hint + Help 2 visual guide.
2. `body_and_feelers` — grouped organic body + feelers; expected geometry; Help 1 + visual guide.
3. `garden_overlap` — simple leaf/ground/flower overlap behind or beside the snail; expected geometry; Help 1 + visual guide/direction support where useful.
4. `make_garden_yours` — shell pattern + garden-detail ideas only; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

The final turn must allow material divergence from the preview. Shell pattern is not similarity-graded.

## 6. Lesson contract — Elephant From Shapes

- ID/revision: `elephant-from-shapes@1`
- age bands: Creative Explorers + Growing Artists + Young Artists
- difficulty: 3
- target duration: 11 minutes
- categories: `animals`, `animals.wild`
- skills: `shape.combine`, `scale.relative`, `proportion.basic`, `overlap.basic`, `contour.simple`
- journey: `journey.animal_artist`
- prerequisite: `friendly-owl`
- modes: Draw With Me + Watch Then Draw
- structured coloring: disabled
- Trace mode: not advertised

Required child-turn shape:

1. `body_mass` — large body construction from simple forms; expected geometry; Help 1 + visual guide.
2. `head_ears_trunk` — head/ears/trunk as a coherent grouped construction; expected geometry; Help 1 + visual guide/direction anchors.
3. `legs_and_overlap` — legs/body overlap and relative scale; expected geometry; Help 1 + direction anchors.
4. `face_and_contour` — simple facial landmarks / contour refinement; expected geometry; Help remains observational rather than Trace.
5. `make_elephant_yours` — optional ear marks, trunk gesture, ground/scene/personality details; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

Growing/Young presentation must emphasize proportion/construction and must not sound toddler-oriented.

## 7. Lesson contract — Simple Car

- ID/revision: `simple-car@1`
- age bands: Creative Explorers + Growing Artists
- difficulty: 2
- target duration: 8 minutes
- categories: `vehicles`, `vehicles.land`
- skills: `shape.rectangle`, `shape.circle`, `placement.relative`, `proportion.basic`, `creativity.variation`
- journey: none in P5.5
- prerequisite: `shape-friends`
- modes: Draw With Me + Watch Then Draw
- structured coloring: disabled
- Trace mode: not advertised

Required child-turn shape:

1. `body_base` — simple body/base proportion; expected geometry; Help 1 + visual guide.
2. `cabin_and_wheels` — cabin + paired wheels with clear placement; expected geometry; Help 1 + visual guide/direction anchors.
3. `windows_and_details` — windows/lights/basic vehicle details; expected geometry but optional decorative detail is not required for completion.
4. `design_your_car` — body-shape/decals/context ideas only; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

The lesson teaches construction and relative proportion, not automotive realism.

## 8. Lesson contract — Sailboat Scene

- ID/revision: `sailboat-scene@1`
- age bands: Growing Artists + Young Artists
- difficulty: 3
- target duration: 11 minutes
- categories: `vehicles`, `vehicles.water`, `nature.landscapes`
- skills: `shape.triangle`, `overlap.basic`, `scene.foreground_background`, `composition.balance`, `scale.relative`
- journey: none in P5.5
- prerequisite: `simple-car`
- modes: Watch Then Draw + Draw With Me
- structured coloring: disabled
- Trace mode: not advertised

Required child-turn shape:

1. `hull_and_waterline` — readable hull + waterline; expected geometry; Help 1 + visual guide.
2. `mast_and_sail` — mast/triangular sail placement; expected geometry; Help 1 + direction anchors.
3. `horizon_and_depth` — horizon / water layers establishing foreground-background; expected geometry; Help remains conceptual/visual.
4. `balance_the_scene` — one or two simple environment anchors demonstrating scale/composition; expected geometry may cover only the structural anchors.
5. `make_scene_yours` — weather, distant land, birds, flags or other authored scene details; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

Watch Then Draw must present the whole scene as an observation target before the per-step reference path. No Trace Help.

## 9. Lesson contract — Planet With Rings

- ID/revision: `planet-with-rings@1`
- age bands: Creative Explorers + Growing Artists
- difficulty: 2
- target duration: 8 minutes
- categories: `space`, `space.planets`
- skills: `shape.circle`, `shape.ellipse`, `overlap.basic`, `composition.centering`, `color.palette_choice`
- journey: `journey.space_artist`
- prerequisite: none
- modes: Draw With Me + Watch Then Draw
- structured coloring: disabled in P5.5
- Trace mode: not advertised

Required child-turn shape:

1. `planet_body` — large centered planet form; expected geometry; Help 1 + visual guide.
2. `ring_overlap` — ring constructed as front/back arcs/ellipse relationship so overlap is visibly taught; expected geometry; Help 1 + visual guide.
3. `moons_and_surface` — small moon/surface marks as examples; structural expected geometry only where needed.
4. `design_your_planet` — ring thickness, surface patterns, moon count and palette/mood choices are authored; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

`color.palette_choice` is a creative decision, not a correct-answer gate. No suggested palette is enforced.

## 10. Lesson contract — Friendly Alien

- ID/revision: `friendly-alien@1`
- age bands: Creative Explorers + Growing Artists + Young Artists
- difficulty: 3
- target duration: 10 minutes
- categories: `space`, `space.aliens`, `characters`, `characters.cartoon`
- skills: `character.silhouette`, `placement.symmetry`, `creativity.variation`, `storytelling.character`
- journey: `journey.space_artist`
- prerequisite: `simple-rocket`
- modes: Draw With Me + Watch Then Draw
- structured coloring: disabled
- Trace mode: not advertised

Required child-turn shape:

1. `body_silhouette` — simple readable alien body/silhouette; expected geometry; Help 1 + visual guide.
2. `face_balance` — eyes/feature placement using symmetry as a tool rather than a score; expected geometry; Help 1 + visual guide/direction anchors.
3. `limbs_and_parts` — arms/legs/antennae or equivalent structural parts; expected geometry for the teacher example only.
4. `give_it_personality` — body-part count, expression, accessories, markings, name/story/personality are intentionally variable; `MANUAL_DONE`, `allowSkip=true`, `expectedStrokeRefs=[]`.

Completion must never require matching the teacher alien. Young-Artist wording must be concise/studio-oriented rather than toddler-ish.

## 11. Geometry / artwork rules

All six packages use a 1000×1000 logical canvas.

- primary subject geometry must preserve phone-size margins;
- teacher/expected/help geometry must stay within logical bounds;
- previews/thumbnails must be attractive finished examples consistent with the lesson construction but must not imply one required final answer;
- helper/reference geometry must never persist into child artwork;
- Little/Creative targets must remain large and uncluttered;
- Growing/Young lessons may be richer but must avoid tiny ornamental targets that do not teach the declared skill;
- no full Trace fallback is authored for Set D unless this contract is explicitly amended with pedagogical evidence.

## 12. Help / Companion rules

- Little/Creative: gentle hint → useful visual guide; direction anchors when they clarify motion/placement.
- Growing/Young: visual/conceptual anchors, replay and construction cues preferred; no default full Trace.
- Help levels must be monotonically stronger and resolve to real authored references.
- P5.3 Companion remains read-only and derives age-aware language from generic lesson/session semantics.
- open turns must produce choice/continue-creating language, not copy/match/scoring pressure.
- requesting Help never lowers a score or changes child identity.

## 13. Content-quality warning policy

The accepted P5.4 catalog already has exactly three reviewed `NO_JOURNEY_MEMBERSHIP` warnings:

- `rainbow-weather`
- `tree-through-seasons`
- `ice-cream-shop`

P5.5 intentionally adds two standalone lessons:

- `simple-car`
- `sailboat-scene`

Therefore the final 20-lesson P5.5 catalog may contain exactly **five** reviewed `NO_JOURNEY_MEMBERSHIP` warnings for those five lesson IDs and no others.

This is not blanket warning suppression. Any geometry, tiny-target, duplicate-reference, broken-help, missing-localization, invalid-mode, or other warning/error remains a failure. If the analyzer behavior differs, inspect the cause; do not invent journeys merely to make the warning counter zero.

## 14. Automated test contract

P5.5 tests must prove at minimum:

- catalog grows 14 → 20 and loader diagnostics remain 0;
- all six stable IDs/revisions load through production code;
- declared age bands/difficulties/categories/skills/modes match this contract;
- Snail + Elephant are in `journey.animal_artist` with the locked prerequisites;
- Planet + Friendly Alien are in `journey.space_artist` with the locked prerequisites;
- Car + Sailboat have no invented journey and their prerequisite chain is correct;
- all localization/assets/teacher refs/expected refs/help guide refs resolve;
- all open creative turns use `MANUAL_DONE + allowSkip + expectedStrokeRefs=[]`;
- no Set-D package advertises Trace mode;
- Sailboat contains Draw With Me + Watch Then Draw and no Trace help;
- expected/teacher/help geometry stays in canvas;
- Companion integration remains generic across Set-D production packages and supported age profiles;
- final quality report = 20 lessons / 0 errors / only the five reviewed standalone warnings described above;
- historical P4/P5.4 lesson coverage is retained as subset/regression assertions rather than fixed old catalog counts.

## 15. Content Lab gate

Before QA freeze, inspect all six Set-D lessons in engineering-only Content Lab:

- title/summary/age/difficulty/category/skills/journey/prerequisite/modes;
- preview + thumbnail readability;
- every drawing step;
- teacher/expected/help geometry overlays;
- open-turn lack of forced expected geometry;
- localization resolution;
- diagnostics;
- Watch Then Draw / Help intent;
- no visual/content defect requiring binary/content change.

## 16. QA / physical acceptance contract

After all six lessons and final 20-lesson gates are green:

1. freeze `0.5.0-curriculum-expansion-p5.5-qa1` unless a concrete reason requires a later QA suffix;
2. use **versionCode 24** for the first distributed P5.5 QA candidate; never reuse 23;
3. exact-head CI must produce debug + release-like profile APK evidence;
4. record artifact IDs, APK byte sizes and SHA-256 hashes;
5. run focused physical acceptance on the exact profile binary;
6. cover discovery, all six Set-D flows, Watch Then Draw, Help, open authorship, age-tone presentation, journey/prerequisite behavior, save/reopen, Gallery, cross-lesson isolation, offline/Airplane Mode, lifecycle, and representative P5.4/P4 regression smoke;
7. any binary/content-changing defect invalidates that QA candidate and requires a new versionCode/evidence set;
8. final acceptance evidence must pass exact-head CI before merge.

## 17. Planned delivery order

1. execution contract + continuation docs;
2. Animal Batch A — Snail Garden + Elephant From Shapes → deterministic tests → quality gate → CI;
3. Vehicle/Scene Batch B — Simple Car + Sailboat Scene → tests → reviewed-warning gate → CI;
4. Space Batch C — Planet With Rings + Friendly Alien → journey tests + full Set-D Companion integration → CI;
5. full 20-lesson content-quality/coverage gate;
6. Content Lab inspection evidence;
7. versionCode-24 QA freeze + exact APK evidence;
8. focused physical acceptance;
9. acceptance-doc CI;
10. PR ready → squash merge with expected head SHA → merged-main CI;
11. close #82 completed only after merged-main green.

## 18. Definition of done

P5.5 is complete only when:

- all six locked Set-D lessons ship as production packages;
- catalog count is 20;
- animal + space journeys reflect the P5.1 progression;
- vehicle/scene standalone intent is explicitly reviewed rather than hidden;
- P5.2 quality/Content Lab gates pass;
- Companion remains generic/read-only;
- no engine or safety invariant regresses;
- exact automated + Content Lab + physical QA evidence exists for versionCode 24 or its legitimate successor;
- the slice is squash-merged to `main`;
- merged-main Android CI is green;
- issue #82 is closed completed only after that merged-main proof.
