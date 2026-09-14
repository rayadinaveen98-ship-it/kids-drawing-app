# P5.1 — Curriculum & Teaching Contract

**Parent epic:** #73  
**Slice:** #74  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Status:** locked planning contract; no implementation yet

## 1. Milestone decision

Phase 5 targets **24 total production guided lessons** in `0.5.0-curriculum-expansion`.

This is intentionally the repository's existing hard public-V1 quality floor rather than the eventual 36-lesson target.

- Phase-4 release catalog: 9 lessons.
- Phase-5 additions: 15 lessons.
- Phase-5 release catalog: 24 lessons.
- Eventual public-V1 target remains 36 after 24-lesson quality/content-production proof.

Quantity never overrides final artwork quality, age fit, teaching clarity or reliability.

## 2. Frozen product/architecture baseline

Phase 5 does **not** reopen the verified 0.4 engines by default.

Frozen unless a concrete defect/ADR proves otherwise:
- AndroidX Ink drawing boundary;
- DrawingDocument operation/history model;
- Lesson Engine session/recovery semantics;
- catalog/package loading;
- generic content-driven teaching modes;
- Help Ladder semantics;
- Free Draw ownership/provenance;
- prepared-region coloring + protected line-art model;
- Gallery ownership/deletion isolation;
- offline-first/no-account core;
- no child behavioral analytics, ads or punitive scoring.

No lesson-ID-specific tutorial code is allowed.

## 3. Phase-5 curriculum principles

1. **Process before imitation.** Teacher demonstrations teach concepts/techniques; matching the teacher image is not the definition of success.
2. **Increasing authorship with age.** Guidance gradually decreases while observation, planning, variation, composition and personal decisions increase.
3. **Difficulty is content complexity, not child identity.** Never permanently label a child beginner/advanced.
4. **Help is child-controlled.** Requesting repeated help is not failure and never lowers a score/reward.
5. **Completion is not similarity grading.** No percent-match, stars for realism or visual ranking against a reference.
6. **Creative variation is required curriculum content.** Palette-only choice does not always count as meaningful variation.
7. **Older-child credibility matters.** 10–12 content must include real technique/proportion/perspective/character/composition work and a non-toddler tone.
8. **Local/cultural content needs specific sourcing/review.** No generic 'Indian style' imitation.
9. **Core journeys stay offline.** No cloud dependency is introduced to teach or recommend lessons.
10. **Every new lesson must earn its place.** It needs a defined age/skill/journey/content-quality purpose.

## 4. Age-band authoring contract

### 4.1 `little_artists` — approximately 4–5

Core learning emphasis:
- mark control;
- lines and loops;
- closed/basic shapes;
- simple shape combination;
- relative placement;
- playful color and detail choices.

Typical lesson policy:
- difficulty 1–2;
- usually 3–7 child turns;
- large simple geometry;
- one main construction idea at a time;
- Trace only where pedagogically justified;
- short/concrete companion language;
- frequent open choice without requiring representational accuracy.

### 4.2 `creative_explorers` — approximately 6–7

Core learning emphasis:
- fluent shape combination;
- spacing/symmetry/basic relative scale;
- simple overlap;
- repeated details/pattern;
- Watch Then Draw memory/observation;
- simple scene construction;
- intentional design/color choices.

Typical lesson policy:
- difficulty 1–3;
- usually 4–9 child turns;
- Draw With Me + Watch Then Draw common;
- Trace less central;
- each content cluster includes meaningful variation.

### 4.3 `growing_artists` — approximately 8–9

Core learning emphasis:
- proportion/placement;
- contour refinement;
- overlap and basic depth/volume cues;
- foreground/background and composition;
- texture/detail layering;
- character/scene choices;
- lightweight reflection/revision.

Typical lesson policy:
- difficulty 2–4;
- usually 5–12 child turns;
- construction logic instead of pixel copying;
- anchors/guides preferred before trace;
- substantial observation and creative variation.

### 4.4 `young_artists` — approximately 10–12

Core learning emphasis:
- deliberate proportion and contour;
- basic perspective/depth scale;
- shading/value foundations where suitable;
- face/body construction and pose;
- silhouette/character design;
- scene composition;
- planning/refinement/personal intent.

Typical lesson policy:
- difficulty 3–5;
- usually 5–14 child turns, never padded artificially;
- teacher as technique/reference, not tracing template;
- open design decisions common;
- optional intent/reflection prompts;
- companion language must feel mature enough for pre-teens.

## 5. Phase-5 catalog — exact 24-lesson target

### Existing 0.4 lessons retained unchanged unless a concrete content defect is found

1. **Cute Cat** — baseline multi-mode lesson + legacy freehand coloring.
2. **Smiling Sun** — beginner Trace & Learn.
3. **Friendly Owl** — full Help Ladder.
4. **Simple Rocket** — Watch Then Draw.
5. **Easy Flower** — grouped demonstrations.
6. **Little Fish** — prepared-region guided coloring.
7. **Hot Air Balloon** — richer multi-region coloring.
8. **Design Your Spaceship** — open-ended creative variation.
9. **Fox Portrait** — older-child proportion/detail.

### Fifteen new Phase-5 lessons

#### Foundations / first pictures

10. **Happy Lines**
- primary bands: Little / Creative;
- difficulty: 1;
- core skills: `line.straight`, `line.curve`, `line.zigzag`, `line.loop`, `line.control`;
- modes: Draw With Me + selected Trace support;
- purpose: confidence/control before representational pressure;
- creative turn: turn favorite marks into a tiny invented picture/pattern.

11. **Shape Friends**
- primary bands: Little / Creative;
- difficulty: 1;
- core skills: `shape.circle`, `shape.square`, `shape.triangle`, `shape.combine`, `placement.relative`;
- modes: Draw With Me + selected Trace support;
- purpose: shapes become objects/characters;
- creative turn: choose which shapes become a face/creature/object.

#### Animals

12. **Snail Garden**
- primary bands: Little / Creative;
- difficulty: 2;
- skills: `line.curve`, `line.loop`, `shape.organic`, `overlap.basic`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: shell pattern + garden detail.

13. **Elephant From Shapes**
- primary bands: Creative / Growing / optional Young;
- difficulty: 3;
- skills: `shape.combine`, `scale.relative`, `proportion.basic`, `overlap.basic`, `contour.simple`;
- modes: Draw With Me + Watch Then Draw;
- purpose: bridge simple construction into larger-body proportion.

#### Nature / weather

14. **Rainbow Weather**
- primary bands: Little / Creative;
- difficulty: 2;
- skills: `line.curve`, `placement.spacing`, `color.palette_choice`, `composition.balance`;
- modes: Draw With Me;
- prepared coloring: optional large safe regions if authoring quality supports it;
- creative turn: choose weather details / sky mood.

15. **Tree Through Seasons**
- primary bands: Creative / Growing / Young;
- difficulty: 3;
- skills: `shape.organic`, `contour.simple`, `detail.layering`, `composition.balance`, `creativity.variation`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: child chooses season/story/details rather than copying one final tree.

#### Everyday

16. **Ice Cream Shop**
- primary bands: Creative / Growing;
- difficulty: 2;
- skills: `shape.combine`, `placement.relative`, `pattern`, `creativity.variation` where mapped to stable taxonomy IDs;
- modes: Draw With Me;
- creative turn: custom scoops/toppings/sign details;
- purpose: highly approachable designed-object variation.

#### Vehicles / scenes

17. **Simple Car**
- primary bands: Creative / Growing;
- difficulty: 2;
- skills: `shape.rectangle`, `shape.circle`, `placement.relative`, `proportion.basic`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: body shape/decals/context.

18. **Sailboat Scene**
- primary bands: Growing / Young;
- difficulty: 3;
- skills: `shape.triangle`, `overlap.basic`, `scene.foreground_background`, `composition.balance`, `scale.relative`;
- modes: Watch Then Draw + Draw With Me;
- purpose: first deliberate subject + environment composition.

#### Space

19. **Planet With Rings**
- primary bands: Creative / Growing;
- difficulty: 2;
- skills: `shape.circle`, `shape.ellipse`, `overlap.basic`, `composition.centering`, `color.palette_choice`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: ring/detail/moon choices.

20. **Friendly Alien**
- primary bands: Creative / Growing / Young;
- difficulty: 3;
- skills: `character.silhouette`, `placement.symmetry`, `creativity.variation`, `storytelling.character`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: body parts, expression, accessories and personality are intentionally variable.

#### People / Character Creator journey

21. **Face & Expressions**
- primary bands: Growing / Young;
- difficulty: 3;
- skills: `anatomy.face_basic`, `placement.symmetry`, `expression.face`, `proportion.basic`;
- modes: Draw With Me + Watch Then Draw;
- creative turn: expression choice and feature variation;
- help policy: construction anchors before trace; no full-face trace by default.

22. **Simple Body & Pose**
- primary bands: Growing / Young;
- difficulty: 4;
- skills: `anatomy.body_basic`, `pose.simple`, `proportion.basic`, `character.silhouette`;
- modes: Draw With Me + Watch Then Draw;
- purpose: gesture/body construction without anatomy over-complexity;
- creative turn: pose/accessory variation.

23. **Create Your Character**
- primary bands: Growing / Young;
- difficulty: 4;
- skills: `character.design`, `character.silhouette`, `storytelling.character`, `creativity.imagination`, `creativity.variation`;
- mode: structured foundation + open `make_it_yours` style turn;
- purpose: synthesize face/body/expression/design skills into authored work;
- completion must never require reference similarity.

#### Advanced youth technique

24. **One-Point Room**
- primary band: Young; optional advanced Growing where policy allows;
- difficulty: 5;
- skills: `perspective.one_point`, `perspective.depth_scale`, `scene.foreground_background`, `composition.balance`, `scale.relative`;
- modes: Draw With Me + Watch Then Draw;
- purpose: credible advanced-youth technical lesson so 10–12 content is not just 'more details';
- help policy: conceptual anchors/guides and replay; trace is not default;
- creative turn: child designs room purpose/decor/furniture choices.

## 6. Required journey state at 0.5

### `journey.first_shapes_to_pictures`
Minimum sequence represented by 0.5:
1. Happy Lines
2. Shape Friends
3. Smiling Sun
4. Little Fish
5. Easy Flower
6. Cute Cat

Progression: marks → shapes → controlled repetition → combined recognizable pictures.

### `journey.animal_artist`
Minimum sequence represented by 0.5:
1. Little Fish
2. Snail Garden
3. Cute Cat
4. Friendly Owl
5. Elephant From Shapes
6. Fox Portrait

Progression: iconic/simple → overlap/scale → construction → proportion/detail/observation.

### `journey.space_artist`
Minimum sequence represented by 0.5:
1. Planet With Rings
2. Simple Rocket
3. Friendly Alien
4. Design Your Spaceship

Progression: simple space forms → constructed object → character variation → original designed vehicle.

### `journey.character_creator`
Minimum sequence represented by 0.5:
1. Face & Expressions
2. Simple Body & Pose
3. Create Your Character

This is a coherent Phase-5 foundation, not the final 36-lesson character curriculum. Additional hair/clothing/pose/story-scene lessons may be added later only after the 24-lesson system is proven.

## 7. Coverage gates for 0.5

Because lessons may support multiple bands, totals overlap.

Minimum release support counts across the 24 lessons:
- `little_artists`: **8+** suitable lessons;
- `creative_explorers`: **14+**;
- `growing_artists`: **14+**;
- `young_artists`: **10+**.

Difficulty coverage:
- difficulty 1 must remain meaningfully represented;
- difficulty 2–3 form the broad middle;
- at least 3 credible difficulty-4 experiences across the catalog;
- at least 1 difficulty-5 Young Artist lesson (`One-Point Room`).

Creative ownership coverage:
- at least **8 of 24** lessons must include a meaningful authored variation/creative decision beyond palette selection;
- at least **6 of 24** should exercise Watch Then Draw / observation-memory where pedagogically sensible;
- Trace is targeted support and must not become the default for older bands.

## 8. Teaching/companion contract for Phase 5

### 8.1 Turn-taking

Companion must make these states obvious without blocking the canvas:
- teacher demonstrating;
- child turn;
- optional help available;
- step complete/continue;
- lesson complete / continue creating.

### 8.2 Feedback

Prefer specific process/state acknowledgement:
- 'You finished the main shape.'
- 'You kept the two sides balanced.'
- 'Want to add your own detail?'
- 'Need a hint or want to try first?'

Avoid:
- 'Wrong.'
- 'Bad drawing.'
- similarity scores;
- comparative child ranking;
- empty praise on every stroke;
- pressure to work faster.

### 8.3 Optional reflection

For suitable Creative/Growing/Young lessons, completion may offer one lightweight optional prompt:
- favorite part;
- what they changed from the teacher;
- what the character is feeling/doing;
- what they would add next;
- what feels closer/farther/larger/smaller.

No written answer is required for lesson completion.

### 8.4 Help policy by maturity

- Little: strong visual assistance/trace may appear quickly after explicit Help request.
- Creative: visual guide and anchors before trace.
- Growing: direction/anchors/partial demonstration preferred.
- Young: conceptual hints, proportion/perspective anchors and replay preferred; full trace rare.

The existing Help Ladder remains authoritative; this is an authoring-selection policy, not a new scoring system.

## 9. Content quality gate — every new lesson

A lesson cannot ship merely because schema validation passes.

### Structural gate
- package/schema/reference/localization/assets valid;
- stable ID/revision;
- age/category/skills/difficulty/journey metadata accurate;
- no unsupported mode advertised;
- no lesson-ID product branching.

### Teaching gate
- each step has a pedagogical purpose;
- no artificial step padding;
- teacher geometry clearly communicates intended construction;
- pace works across supported profiles;
- Help Ladder entries are useful rather than repetitive;
- child can recover/skip/retry safely.

### Age-fit gate
- touch/visual complexity appropriate;
- language tone appropriate;
- younger content not cognitively overloaded;
- older content not infantilized;
- independence increases appropriately.

### Creative-ownership gate
- where variation is promised, child can materially diverge from reference;
- completion does not require copying discretionary details;
- creative choices survive Save/Gallery/reopen.

### Artwork gate
- preview/teacher/final artwork is production quality, not test-fixture geometry;
- silhouettes/readability are strong at phone size;
- coloring regions, if used, are large/safe enough for target age;
- no reference/teacher overlay contaminates child document history.

### Reliability gate
- offline;
- lifecycle/recovery;
- cross-lesson isolation;
- Gallery reopen;
- no unexpected permission/account/network requirement;
- automated + physical acceptance evidence before milestone close.

## 10. Content Production System V2 requirements implied by P5.1

Before authoring all 15 lessons manually, P5.2 must improve tooling to catch expensive content defects earlier.

Minimum planned checks/tools:
- catalog-wide metadata/coverage report;
- lesson package preview rendering;
- teacher stroke/guide/trace visualization;
- prepared-coloring region visualization + hit-area warnings;
- missing string/reference/asset diagnostics;
- unsupported mode/content-API diagnostics;
- duplicate/stable-ID checks;
- target age/difficulty/journey coverage summary;
- optional warnings for suspiciously tiny child targets or excessive step counts;
- release-readiness report per lesson and whole catalog.

Tooling must validate content; it must not become a second runtime engine.

## 11. Local Adaptive Teaching — Phase-5 boundaries

P5.7 may use deterministic local signals such as:
- age band;
- stated interests;
- completed lesson/skill metadata;
- unfinished/resume state;
- repeated child-requested Help usage;
- recent difficulty mix.

Allowed outcomes:
- recommend a more guided next lesson;
- recommend practice of a skill through a different subject;
- suggest slightly greater independence/challenge;
- offer a creative/free-draw extension.

Not allowed:
- permanent ability labels;
- hidden cloud profiling;
- behavioral analytics upload;
- grades, rank, streak punishment or 'failure' state;
- automatic lowering of the child to easier content without choice;
- interpreting drawing appearance with ML to judge talent/quality.

## 12. Cultural-content policy

Phase 5's core 24 lessons remain broad/universal. Culturally specific/folk-art content can be researched for later expansion, but no such lesson ships merely as decorative imitation.

A future culturally specific package must document:
- named tradition/region/community;
- reliable source/reference;
- what is being taught (motif/process/context);
- age-appropriate context;
- review against stereotyping/mislabeling;
- rights/source suitability of reference assets.

## 13. Slice plan locked by this contract

- **P5.1** — Curriculum & Teaching Contract — this document/research.
- **P5.2** — Content Production System V2.
- **P5.3** — Companion / Teacher Experience V2.
- **P5.4** — Content Set C: Foundations + early/nature/everyday.
- **P5.5** — Content Set D: Animals + vehicles + space.
- **P5.6** — Content Set E: people/characters + advanced youth technique.
- **P5.7** — Local Adaptive Teaching.
- **P5.8** — Cross-age curriculum QA + `0.5.0-curriculum-expansion` release.

Actual lesson batching across P5.4–P5.6 may be balanced for QA efficiency, but the exact 24-lesson target and learning purposes above must not be silently changed.

## 14. P5.1 acceptance

P5.1 is complete only when:
1. research synthesis is committed;
2. this contract is committed;
3. Phase-5 epic and P5.1 issue exist in Git;
4. roadmap/status/handoff identify Phase 5 as active planning;
5. exact-head CI remains green;
6. any changes to the 24-lesson target or frozen teaching principles require an explicit Git decision/update, not chat-only drift.

No P5.2 implementation begins before this contract is accepted.
