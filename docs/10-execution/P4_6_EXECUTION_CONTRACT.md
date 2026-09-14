# P4.6 — Representative Content Set B + Cross-content QA Execution Contract

**Issue:** #63  
**Parent epic:** #57  
**Branch:** `phase4/p4-6-content-set-b`  
**Base:** P4.5 squash merge `10b8f2f7a6a6b4579a8695ab4f602c2e4525aec5` with merged-main Android CI #420 green  
**Target:** complete the Phase-4 representative content set with three production lessons and prove cross-content quality before P4.7.

## 1. Non-negotiable direction

P4.6 is content-first. It must use the generic catalog, lesson, drawing, coloring, recommendation and Gallery systems already proven through P4.5.

- No lesson-ID-specific product/UI branching.
- Do not reopen frozen Drawing/Lesson Engine architecture without a concrete content-blocking defect.
- Do not create a second coloring or creative-mode runtime.
- Teacher/help/trace overlays never enter child artwork history.
- Prepared fill remains a schema-3 coloring operation below protected line art.
- Child creative choice must be real; do not pretend an authored replica is “open ended.”
- All lessons remain offline and package-driven.
- Default-locale strings must be complete for every authored key.
- Every release package must pass the production loader/validator with no diagnostics.

## 2. Baseline representative set

P4.5 main currently contains six release lessons:
1. `cute-cat` — Creative/Growing; animals/pets; Animal Artist; all three teaching modes; legacy freehand coloring.
2. `smiling-sun` — Little/Creative; foundations/weather; First Shapes to Pictures; trace-friendly.
3. `friendly-owl` — Creative/Growing; animals/birds; Animal Artist; full Help Ladder.
4. `simple-rocket` — Creative/Growing; space; Space Artist; Watch Then Draw grouped demo.
5. `easy-flower` — Little/Creative/Growing; nature/flowers; First Shapes to Pictures; grouped multi-stroke demo.
6. `little-fish` — Little/Creative; animals/ocean; First Shapes to Pictures; prepared-region Color With Me + Color Myself.

Current material gap: no credible Young Artist lesson and only one Space Artist lesson.

P4.6 adds all three packages below, taking the representative set to nine release lessons. The issue minimum is seven; do not cut the explicit three-package scope merely because the minimum would be exceeded.

## 3. Locked P4.6 lesson A — Hot Air Balloon

**ID:** `hot-air-balloon` r1  
**Purpose:** richer guided-region coloring proof using P4.5 Fill support  
**Age bands:** Little Artists + Creative Explorers + Growing Artists  
**Difficulty:** 2  
**Estimated time:** ~10 minutes  
**Categories:** travel / sky / objects  
**Journey:** `journey.first_shapes_to_pictures`  
**Teaching modes:** Draw With Me + Watch Then Draw  
**Coloring:** enabled; default guided; Color Myself remains available

Drawing structure:
1. balloon envelope — large clean oval/rounded contour;
2. panel lines — grouped internal construction lines;
3. ropes + basket — grouped simple straight/box construction;
4. small finishing details — optional cloud/accent details where appropriate.

Prepared coloring regions must be technically closed and comfortably tappable. Minimum region set:
- left balloon panel;
- center balloon panel;
- right balloon panel;
- basket.

Guided coloring progression:
1. center panel;
2. both side panels as one multi-region step;
3. basket.

Acceptance intent:
- proves prepared Fill with more regions than Little Fish;
- proves multi-region guided completion again through production content;
- allows reasonable child color choice;
- line art always remains protected.

## 4. Locked P4.6 lesson B — Fox Portrait

**ID:** `fox-portrait` r1  
**Purpose:** credible older-child detail/proportion lesson  
**Age bands:** Growing Artists + Young Artists  
**Difficulty:** 4  
**Estimated time:** ~14 minutes  
**Categories:** animals / wildlife / portrait  
**Journey:** `journey.animal_artist`  
**Teaching modes:** Draw With Me + Watch Then Draw  
**Coloring:** disabled in r1 unless existing product behavior makes optional coloring clearly valuable without weakening the lesson focus

Core skills:
- proportion;
- construction/placement;
- bilateral balance without demanding perfect symmetry;
- contour refinement;
- facial-feature placement;
- layered detail/texture;
- observation.

Drawing structure:
1. head construction and overall proportion;
2. muzzle/cheek placement;
3. ears and eye line;
4. eyes/nose/facial landmarks;
5. fur contour/detail accents.

Older-child presentation rules:
- no tracing requirement;
- Help Ladder should prioritize gentle hints, visual guides and direction anchors rather than infantilizing assistance;
- narration should use respectful craft language: compare distances, notice angles, refine shapes;
- no punitive scoring or “wrong” language;
- final result should visibly reward careful proportion/detail work.

## 5. Locked P4.6 lesson C — Design Your Spaceship

**ID:** `design-your-spaceship` r1  
**Purpose:** open-ended creative variation with meaningful child authorship  
**Age bands:** Creative Explorers + Growing Artists + Young Artists  
**Difficulty:** 3  
**Estimated time:** ~12 minutes  
**Categories:** space / design / imagination  
**Journey:** `journey.space_artist`  
**Teaching modes:** Draw With Me + Watch Then Draw  
**Coloring:** optional; do not add prepared coloring merely to inflate feature count

Structured foundation:
1. main hull/body;
2. cockpit/window placement;
3. wings/engines or other functional silhouette components.

Creative-variation step:
- teacher may demonstrate several examples such as alternate antenna, wing, engine, window or emblem ideas;
- the child is explicitly invited to choose, combine or invent their own details;
- the child turn must not require matching one authored accessory set;
- prefer `manual_done` with no replica requirement if the existing semantic validator supports it;
- `allowSkip=true` is acceptable for inspiration-only variation;
- if the current validator blocks an empty `expectedStrokeRefs` list for a non-trace creative step, make the smallest generic content-contract change required and prove it with tests. Do not special-case this lesson ID.

Creative narration rules:
- “Here are a few ideas” rather than “copy this exactly”;
- explicitly permit a different design;
- never score creative variation against teacher geometry.

## 6. Required representative-set coverage after P4.6

The final nine-lesson set must prove:
- all four age bands have credible experiences;
- Little Artist content is genuinely simpler and touch-friendly;
- Young Artist content is not merely a younger lesson with a metadata flag;
- difficulty levels span at least 1–4;
- at least three meaningful categories are represented;
- `journey.first_shapes_to_pictures`, `journey.animal_artist`, and `journey.space_artist` each have multiple meaningful lessons after P4.6;
- Draw With Me and Watch Then Draw have multiple packages;
- Trace & Learn remains represented by appropriate beginner content;
- prepared-region coloring and legacy freehand coloring both remain represented;
- Help Ladder coverage includes beginner high-assistance and older-child lighter-assistance patterns;
- at least one lesson includes genuine open-ended creative choice.

## 7. Cross-content QA requirements

Create a machine/test-backed representative-set matrix covering all release lessons and verify:
- unique stable lesson IDs/revisions;
- release status and loader success;
- title + summary localization keys exist;
- every narration/help/completion key referenced by content exists in default `en` strings;
- all stroke/guide/region references resolve;
- categories, skills, journeys and prerequisites resolve generically;
- no lesson is routed through product code by ID;
- catalog discovery returns all release lessons;
- recommendations can surface appropriate lessons by age without excluding Young Artists;
- preview art reflects the selected package;
- switching between lessons never leaks session/artwork state;
- coloring-enabled and drawing-only lessons coexist safely;
- legacy Cute Cat freehand coloring still exposes no deceptive Fill;
- Little Fish and Hot Air Balloon prepared Fill remain valid;
- Free Draw remains independent of lesson routing;
- Gallery promotion/reopen preserves source provenance and artwork operations.

## 8. Content-quality review requirements

For each P4.6 lesson record:
- pacing and total step count;
- teacher-demo duration reasonableness;
- child-turn clarity;
- help usefulness;
- final artwork legibility/appeal;
- age-band appropriateness;
- wording that is patient and non-punitive;
- creative freedom where promised;
- no visually tiny prepared-fill target;
- no unnecessary trace dependence for older children.

Create `docs/10-execution/P4_6_CONTENT_QA.md` with the coverage matrix and physical/product checks. Do not pre-mark physical rows as PASS.

## 9. Automated evidence minimum

Before QA APK freeze, automated tests must prove:
- all nine release packages load with no diagnostics;
- the three new package IDs/revisions/metadata are exact;
- all four age bands are represented, including at least two credible Young Artist entries if the creative spaceship also supports Young Artists as locked;
- three journeys are meaningfully populated with at least two release lessons each;
- Hot Air Balloon prepared regions and guided multi-region references are valid;
- Fox Portrait is Growing/Young, difficulty 4, and does not require trace support;
- Design Your Spaceship exposes a creative step without a replica requirement;
- default-locale key completeness across all release packages;
- prior P4.3/P4.5 content regression tests remain green;
- existing Android CI, lint, Ink-boundary and permission gates remain green.

## 10. QA/release identity

If a P4.6 installable candidate is distributed, use the next monotonic Android versionCode after P4.5 code 17.

Planned first candidate:
- versionName: `0.4.0-content-studio-p4.6-qa1`;
- versionCode: 18.

Any rebuilt/distributed replacement increments again; never reuse 18 after a materially different distributed candidate.

## 11. Exit gate

P4.6 closes only when:
1. all three locked production packages are authored and production-loadable;
2. representative-set coverage is at least nine lessons and satisfies age/journey/skill/mode requirements;
3. default-locale/reference completeness checks are green;
4. cross-content generic routing/recovery regressions are green;
5. exact-head Android CI is green;
6. a QA APK is produced with exact commit/run/artifact/size/SHA evidence when physical verification is needed;
7. physical/product QA confirms the new guided coloring, older-child proportion/detail experience and creative-variation experience are usable;
8. PR remains unmerged until evidence is recorded;
9. merged-main CI is green before issue #63 closes.

P4.7 begins only after this gate or an explicit documented exception.