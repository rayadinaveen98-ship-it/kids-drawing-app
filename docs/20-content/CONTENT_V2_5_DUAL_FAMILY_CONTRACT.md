# Content Library V2.5 — Dual-Family Cross-Age Expansion Contract

Status: **FROZEN FOUNDATION — authoring gate active**

Parent: #103  
Implementation issue: #114  
Accepted baseline: `ed33bc42b69b7af2889c2e499bf79adde963d1f1`  
Accepted baseline catalog: 28 RELEASE lessons

## 1. Purpose

V2.5 is the second deliberate child-facing Content Library V2 expansion. It adds two contrasting four-age families — Sea Turtle and Robot — to prove that the accepted Content Studio / Lesson Engine / capability validator / Catalog Index V2 pipeline can scale from 28 to 36 release lessons without weakening per-lesson truth.

This is a controlled slice, not bulk generation.

## 2. Immutable product rules

1. A lesson may advertise only capabilities that are fully authored for that exact package.
2. Child runtime remains offline-first and deterministic.
3. No lesson-ID-specific runtime branch.
4. Metadata drives discovery; authored package assets drive teaching truth.
5. No runtime AI, account, network or new permission requirement.
6. Existing 28 accepted lesson packages are immutable in this slice.
7. Existing taxonomy only. No new category, skill, journey or collection ID.
8. Content Studio / production validators / Catalog Index V2 remain the release authority.
9. Physical/device acceptance is separate and must never be inferred from static review or CI.
10. P6.5 remains separate and untouched.

## 3. V2.4 pilot lesson carried forward

The accepted Butterfly family proved capability completeness and cross-age metadata, but static visual review found that its four previews retained a very similar overall silhouette/composition.

V2.5 therefore freezes this additional rule:

> **Age progression must be materially visible in construction, pose/composition, silhouette and observational demand — not merely the same drawing with more decoration.**

For each family:

- **Little Artists:** iconic read, large targets, few major forms, forgiving placement.
- **Creative Explorers:** materially changed pose/composition plus meaningful pattern/design choice.
- **Growing Artists:** structural proportion, overlap, controlled placement and a different construction strategy.
- **Young Artists:** observational/mechanical form demand, refined contour, depth/overlap/detail and a materially distinct final silhouette/composition.

The static acceptance review must compare all four previews side by side for each family before candidate merge.

## 4. Frozen family A — Sea Turtle

All Sea Turtle lessons use only existing category/journey vocabulary:

- categories: `animals`, `nature`
- journey: `journey.animal_artist`

### A1 — Little Artists

- lesson ID: `sea-turtle-big-shell`
- title: **Sea Turtle from Big Shapes**
- age band: `little_artists`
- difficulty: 1
- estimated minutes: 7
- content API: 2
- target drawing steps: 3
- taxonomy skills:
  - `shape.ellipse`
  - `shape.combine`
  - `line.curve`
  - `placement.relative`
- modes:
  - `draw_with_me`
  - `trace_and_learn`
- Help: required / complete
- Trace: real authored guides on every structured child turn
- coloring: `guided_prepared`
- coloring geometry: explicit prepared regions; suggested colors may exist but are never enforced
- prerequisite: none
- visual direction: **simple side-view turtle**; one oversized oval shell, small head, four large paddle-like flippers; centered composition; no perspective or texture burden.
- differentiation requirement: must read instantly as the simplest/iconic member of the family.

### A2 — Creative Explorers

- lesson ID: `sea-turtle-pattern-swim`
- title: **Sea Turtle Pattern Swim**
- age band: `creative_explorers`
- difficulty: 2
- estimated minutes: 10
- content API: 1
- target drawing steps: 4
- taxonomy skills:
  - `shape.combine`
  - `line.curve`
  - `pattern`
  - `creativity.variation`
  - `placement.relative`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
  - `trace_and_learn`
- Help: required / complete
- Trace: real authored guides on every structured child turn
- coloring: `freehand`
- prerequisite: none
- visual direction: **diagonal upward swimming pose**, shell tilted instead of horizontal, asymmetric visible flipper placement, authored shell pattern plus room for child-created pattern variation; optional bubble marks as composition accents.
- differentiation requirement: composition and pose must be visibly different from A1, not merely patterned A1.

### A3 — Growing Artists

- lesson ID: `sea-turtle-form-study`
- title: **Sea Turtle Form & Flippers**
- age band: `growing_artists`
- difficulty: 3
- estimated minutes: 13
- content API: 1
- target drawing steps: 4
- taxonomy skills:
  - `proportion.basic`
  - `overlap.basic`
  - `scale.relative`
  - `contour.simple`
  - `line.control`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
- Help: required / complete
- Trace: not advertised
- coloring: `none`
- prerequisite: none
- visual direction: **three-quarter swimming construction**; shell and head use unequal axes, near/far flippers differ in size and overlap, silhouette is intentionally less symmetrical than A1/A2.
- differentiation requirement: construction must teach relative scale and overlap, not decorative complexity.

### A4 — Young Artists

- lesson ID: `sea-turtle-observation-glide`
- title: **Observational Sea Turtle Glide**
- age band: `young_artists`
- difficulty: 4
- estimated minutes: 16
- content API: 1
- target drawing steps: 5
- taxonomy skills:
  - `observation`
  - `contour.refinement`
  - `proportion`
  - `detail.texture`
  - `overlap.basic`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
- Help: required / complete
- Trace: not advertised
- coloring: `none`
- prerequisite: none
- visual direction: **observational gliding turtle in oblique top/three-quarter view**; shell contour is not a perfect oval, head/flippers use natural taper, front/rear flippers overlap differently, shell scute/edge texture follows form.
- differentiation requirement: final silhouette, view angle and contour demand must be materially distinct from A1–A3.

## 5. Frozen family B — Robot

All Robot lessons use only existing category/journey vocabulary:

- categories: `characters`, `design`
- journey: `journey.character_creator`

### B1 — Little Artists

- lesson ID: `robot-big-shapes`
- title: **Friendly Robot from Big Shapes**
- age band: `little_artists`
- difficulty: 1
- estimated minutes: 7
- content API: 2
- target drawing steps: 3
- taxonomy skills:
  - `shape.rectangle`
  - `shape.circle`
  - `shape.combine`
  - `placement.relative`
- modes:
  - `draw_with_me`
  - `trace_and_learn`
- Help: required / complete
- Trace: real authored guides on every structured child turn
- coloring: `guided_prepared`
- coloring geometry: explicit prepared regions; no enforced suggested colors
- prerequisite: none
- visual direction: **front-facing friendly block robot**; one large body rectangle, smaller head, simple circular eyes/buttons and chunky limbs; centered, upright, large targets.
- differentiation requirement: simplest/iconic robot read with minimal joints and no depth requirement.

### B2 — Creative Explorers

- lesson ID: `robot-design-lab`
- title: **Robot Design Lab**
- age band: `creative_explorers`
- difficulty: 2
- estimated minutes: 10
- content API: 1
- target drawing steps: 4
- taxonomy skills:
  - `shape.combine`
  - `design.silhouette`
  - `design.variation`
  - `creativity.variation`
  - `expression.face`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
  - `trace_and_learn`
- Help: required / complete
- Trace: real authored guides on structured core construction; child-choice details remain optional and ungraded
- coloring: `freehand`
- prerequisite: none
- visual direction: **off-center action pose with one raised arm**, non-rectangular antenna/head choice and intentionally different limb proportions; child chooses face panel and accessory shapes.
- differentiation requirement: silhouette and pose must be visibly different from B1 before any decorative details are added.

### B3 — Growing Artists

- lesson ID: `robot-structure-study`
- title: **Robot Structure & Joints**
- age band: `growing_artists`
- difficulty: 3
- estimated minutes: 13
- content API: 1
- target drawing steps: 4
- taxonomy skills:
  - `proportion.basic`
  - `overlap.basic`
  - `line.control`
  - `detail.layering`
  - `design.silhouette`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
- Help: required / complete
- Trace: not advertised
- coloring: `none`
- prerequisite: none
- visual direction: **three-quarter standing robot**; torso/hip masses overlap, near/far limbs differ in width, joints connect as separate forms, feet anchor with controlled spacing.
- differentiation requirement: structural mass/joint logic must drive the lesson rather than extra decoration.

### B4 — Young Artists

- lesson ID: `robot-mechanical-observation`
- title: **Mechanical Robot Observation**
- age band: `young_artists`
- difficulty: 4
- estimated minutes: 16
- content API: 1
- target drawing steps: 5
- taxonomy skills:
  - `observation`
  - `contour.refinement`
  - `proportion`
  - `detail.texture`
  - `design.silhouette`
- modes:
  - `draw_with_me`
  - `watch_then_draw`
- Help: required / complete
- Trace: not advertised
- coloring: `none`
- prerequisite: none
- visual direction: **mechanical three-quarter/crouched service-robot pose** with tapered torso, overlapping shoulder/forearm forms, unequal near/far limbs and restrained panel/seam texture following form.
- differentiation requirement: view angle, body ratio and silhouette must be materially different from B1–B3 and demand observation of connected forms.

## 6. Frozen capability matrix

| Lesson | Age | Difficulty | Draw | Watch | Trace | Help | Coloring | API |
|---|---|---:|---|---|---|---|---|---:|
| sea-turtle-big-shell | Little | 1 | READY | — | READY | READY | GUIDED_PREPARED | 2 |
| sea-turtle-pattern-swim | Creative | 2 | READY | READY | READY | READY | FREEHAND | 1 |
| sea-turtle-form-study | Growing | 3 | READY | READY | — | READY | NONE | 1 |
| sea-turtle-observation-glide | Young | 4 | READY | READY | — | READY | NONE | 1 |
| robot-big-shapes | Little | 1 | READY | — | READY | READY | GUIDED_PREPARED | 2 |
| robot-design-lab | Creative | 2 | READY | READY | READY | READY | FREEHAND | 1 |
| robot-structure-study | Growing | 3 | READY | READY | — | READY | NONE | 1 |
| robot-mechanical-observation | Young | 4 | READY | READY | — | READY | NONE | 1 |

`toolPreset` and `playAsGroup` remain RESERVED preserve-only fields and are not capability readiness. `suggestedColorRoles` remains non-enforced metadata. `enforceSuggestedColors=true`, `authored_signal`, and runtime voice/audio capability remain release-blocked/unsupported under the existing frozen capability contract.

## 7. Asset / geometry contract

Every package must contain the normal release package files required by Content Studio / LessonPackageLoader:

- `lesson.json`
- `strokes.json`
- `strings/en.json`
- `thumbnail.svg`
- `preview.svg`
- `coloring-regions.json` only where prepared coloring is authored

Geometry requirements:

- all authored coordinates remain within the accepted drawing bounds;
- stroke timing is monotonic;
- child-turn expected stroke references resolve;
- Trace guides are genuine authored teaching geometry, not aliases to unsupported signals;
- Little targets must remain large and forgiving;
- prepared coloring regions must be non-self-intersecting and comfortably usable;
- visual differentiation must be visible in the actual preview geometry, not only described in metadata.

## 8. Expected aggregate health after V2.5

If all eight packages validate exactly as frozen:

- RELEASE lessons: **36**
- content errors: **0**
- accepted warnings: **exactly 6** (legacy warnings only; all 8 new lessons 0 warnings)
- Help-ready: **36**
- Trace-ready: **10**
- coloring:
  - NONE: **26**
  - FREEHAND: **4**
  - PREPARED_ONLY: **0**
  - GUIDED_PREPARED: **6**
- mode coverage:
  - Draw With Me: **35**
  - Watch Then Draw: **26**
  - Trace & Learn: **10**
- age-band coverage:
  - Little Artists: **11**
  - Creative Explorers: **21**
  - Growing Artists: **20**
  - Young Artists: **13**
- difficulty coverage:
  - 1: **8**
  - 2: **12**
  - 3: **9**
  - 4: **6**
  - 5: **1**
- `journey.animal_artist`: **14**
- Content Studio canonical import/export/production reload: **36/36**
- Catalog Index V2 projected entries: **36**
- committed index drift: **0**
- unknown taxonomy refs: **0**
- duplicate identities: **0**
- prerequisite graph failures: **0**
- blocked `authored_signal`: **0**
- blocked enforced suggested colors: **0**
- audio declarations: **0**

If production validators prove any expected aggregate false, stop and correct the contract or authored package truth before release. Do not weaken the validator to fit these numbers.

## 9. Deterministic tests required

V2.5 must add/extend tests proving:

1. exactly the eight frozen new IDs are present;
2. each maps to exactly one intended age band and difficulty;
3. category/skill/journey refs are existing registered taxonomy only;
4. modes/Help/Trace/coloring match the frozen capability matrix;
5. Little prepared-color packages have valid region geometry and content API 2;
6. Creative freehand packages contain exactly the supported freehand behavior, not ambiguous multiple regionless guided steps;
7. Growing/Young packages do not accidentally advertise Trace/coloring;
8. no new reserved/release-blocked fields or audio declarations;
9. all eight import/export/reload through Content Studio deterministically;
10. projected Catalog Index V2 truth equals package truth;
11. current catalog health is 36 with exactly 6 legacy warnings;
12. historical Phase-5 and V2.4 evidence remains scoped to its accepted cohort rather than rewritten to 36;
13. no existing accepted lesson package is edited.

## 10. Static visual review gate

Before marking the candidate ready:

- inspect all eight `preview.svg` and `thumbnail.svg` assets;
- compare each four-age family side by side;
- confirm different pose/view/composition/silhouette, not merely added details;
- confirm Little assets remain large/readable;
- confirm Creative assets visibly introduce choice/pattern/design;
- confirm Growing assets visually teach structure/overlap/proportion;
- confirm Young assets show the intended observational/mechanical demand;
- record the review in #114 / PR evidence.

This is a static authoring review only. It does not replace physical child/device validation.

## 11. Catalog promotion / release discipline

- Do not hand-author final index capability truth.
- Regenerate the 36-entry Catalog Index V2 from the production projector.
- Promotion remains all-or-nothing and requires READY production validation, explicit revision decision, RELEASE status and regenerated index.
- No partial candidate may mutate the accepted release catalog.
- Exact candidate Android CI must be fully green before merge.
- Merge only the verified head SHA.
- Resulting `main` Android CI must be fully green before #114 closes.

## 12. Scope boundaries

V2.5 does **not**:

- add a third family;
- add taxonomy;
- edit the accepted 28 package roots;
- change production child-runtime behavior;
- change app permissions/accounts/network behavior;
- implement/claim P6.5 hardware acceptance;
- add runtime AI/audio;
- redefine release identity merely to produce an APK.

A new meaningful content QA/release identity may be planned only after this real 8-lesson expansion is merged-main green and a release/physical-QA strategy is explicitly frozen.

## 13. Definition of Done

V2.5 is complete only when:

- the eight frozen packages exist as RELEASE content;
- catalog contains exactly 36 release lessons;
- all 36 production packages load and validate;
- Content Studio round-trip is 36/36;
- all eight new lessons are 0-error / 0-warning;
- aggregate warning count remains exactly 6;
- exact capability matrix matches authored truth;
- static cross-age visual differentiation gate passes for both families;
- existing 28 accepted packages are unchanged;
- deterministic generated index has 36 entries and drift 0;
- no taxonomy/runtime/P6.5 boundary violation exists;
- exact candidate Android CI is green;
- merged-main Android CI is green.
