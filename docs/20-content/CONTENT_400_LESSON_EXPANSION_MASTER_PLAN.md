# Content Expansion Master Plan — +100 Lessons Per Age Group

Status: **PLAN ONLY — no new lesson packages may be authored from this document until Stage 0 is accepted.**

Parent program: #116

Baseline source of truth: `main` at `2ea0dd25d57d7cb23c81d301748ee7c18f54b5af` (Content V2.5 accepted; merged-main Android CI #830 green).

## 1. Goal

Expand the accepted 36-lesson library by **100 new lessons for each age group**:

| Age group | Accepted V2.5 coverage | New target | Program-end coverage |
|---|---:|---:|---:|
| Little Artists | 11 | +100 | 111 |
| Creative Explorers | 21 | +100 | 121 |
| Growing Artists | 20 | +100 | 120 |
| Young Artists | 13 | +100 | 113 |

The expansion model is **100 subject families × 4 materially different age variants = 400 new distinct lessons**.

Program-end release catalog target: **436 distinct lessons**.

This is a breadth program, but lesson count never overrides product truth, visual quality, capability truth, performance, update safety or physical/device acceptance.

## 2. Stage model

Stage 0 adds no content. It makes large-scale authoring safe and deterministic. After Stage 0, ten controlled content stages add 10 families / 40 lessons each.

| Stage | Families | Little | Creative | Growing | Young | New lessons | Target catalog |
|---|---:|---:|---:|---:|---:|---:|---:|
| Stage 0 — Expansion Readiness | 0 | 0 | 0 | 0 | 0 | 0 | 36 |
| V2.6 | 10 | 10 | 10 | 10 | 10 | 40 | 76 |
| V2.7 | 10 | 10 | 10 | 10 | 10 | 40 | 116 |
| V2.8 | 10 | 10 | 10 | 10 | 10 | 40 | 156 |
| V2.9 | 10 | 10 | 10 | 10 | 10 | 40 | 196 |
| V3.0 | 10 | 10 | 10 | 10 | 10 | 40 | 236 |
| V3.1 | 10 | 10 | 10 | 10 | 10 | 40 | 276 |
| V3.2 | 10 | 10 | 10 | 10 | 10 | 40 | 316 |
| V3.3 | 10 | 10 | 10 | 10 | 10 | 40 | 356 |
| V3.4 | 10 | 10 | 10 | 10 | 10 | 40 | 396 |
| V3.5 | 10 | 10 | 10 | 10 | 10 | 40 | 436 |

No stage may borrow lessons from a later stage to hit a number. If a family fails quality or capability readiness, the stage remains incomplete until it is corrected or the master plan is explicitly revised.

## 3. Stage 0 — Expansion Readiness

Stage 0 is mandatory before V2.6 package authoring.

### 3.1 Curriculum and taxonomy readiness
- freeze this master plan and `CONTENT_400_LESSON_CURRICULUM.csv`;
- map every editorial category/family to existing registered runtime category/skill/journey/collection IDs where truthful;
- if a real taxonomy gap exists, change the taxonomy only through a reviewed contract — never invent IDs inside lesson packages;
- run duplicate/near-duplicate review against the accepted 36 lessons;
- freeze V2.6 exact package IDs, titles, age eligibility, difficulty, prerequisites/journeys and per-lesson capability matrix before authoring.

### 3.2 Batch authoring readiness
- preserve the normal Content Studio-compatible package format;
- automate only repetitive package plumbing, never fabricate capability truth;
- keep runtime package loading generic; no lesson-ID branches;
- build deterministic batch validation and family contact-sheet generation;
- ensure generated previews/thumbnails/strokes remain reviewable and editable source artifacts.

### 3.3 Scale/performance readiness
- Home/discovery must continue to consume Catalog Index V2 rather than parse full lesson packages for cards;
- establish repeatable synthetic catalog checks around 100 / 250 / 500 metadata entries;
- track catalog parse/discovery latency and APK size per stage;
- keep lesson media primarily compact JSON/vector/SVG where appropriate;
- add caching/incremental parsing only if measured data justifies it.

### 3.4 QA/update readiness
- preserve package `com.navin.kidsdrawing`;
- use the durable QA signer already frozen by V2.5;
- never reuse/decrease a delivered versionCode;
- each accepted stage must produce an installable signed profile APK and prove direct update over the previous accepted baseline without uninstalling;
- physical acceptance remains separate from automated/static claims.

Stage 0 is complete only when all four sections above have deterministic evidence and V2.6 can be authored without bypassing any frozen product contract.

## 4. Editorial breadth allocation

These are **planning/editorial categories**, not permission to create matching runtime taxonomy IDs.

| Editorial category | Families | Lessons |
|---|---:|---:|
| Drawing Basics | 6 | 24 |
| Animals | 20 | 80 |
| Nature | 8 | 32 |
| Vehicles | 10 | 40 |
| People & Characters | 10 | 40 |
| Food & Everyday Things | 10 | 40 |
| Fantasy & Magic | 8 | 32 |
| Space & Science | 6 | 24 |
| Places & Buildings | 6 | 24 |
| Stories & Scenes | 4 | 16 |
| Patterns & Decoration | 3 | 12 |
| Comics & Expression | 3 | 12 |
| Design & Invent | 3 | 12 |
| Culture & Celebrations | 3 | 12 |
| **Total** | **100** | **400** |

This mix deliberately spans fundamentals, living subjects, objects, scenes, design and sequential storytelling so the final library does not feel like an animal-only or object-only pack.

## 5. Age progression contract for every family

Every family produces four genuinely different lessons.

### Little Artists
- iconic/simple construction;
- large targets and forgiving placement;
- normally 3–5 major construction steps;
- simple silhouette readable at thumbnail size;
- Trace/coloring only when fully authored and truthful.

### Creative Explorers
- materially different pose/composition from Little;
- meaningful expression, pattern, customization or design choice;
- normally 5–7 major construction steps;
- should feel like drawing plus invention, not a decorated Little variant.

### Growing Artists
- structural proportion, overlap and controlled placement;
- materially different construction and silhouette;
- normally 6–9 major construction steps;
- introduces deliberate spatial/form thinking appropriate to the subject.

### Young Artists
- observational, perspective, mechanical/natural-form, depth or refined-contour demand;
- materially distinct final composition/silhouette;
- normally 8–12 major construction/detail steps;
- complexity must come from stronger drawing decisions, not decorative noise.

A family fails visual QA if the four previews can reasonably be described as “the same drawing with more details.”

## 6. Exact 100-family allocation by stage

The exact planned Little/Creative/Growing/Young lesson titles are frozen in `CONTENT_400_LESSON_CURRICULUM.csv`.

### V2.6 — +40 → 76 release lessons

`Line Adventure`, `Dog`, `Lion`, `Tree`, `Car`, `Friend Waving`, `Apple`, `Dragon`, `Ringed Planet`, `Cozy House`.

### V2.7 — +40 → 116 release lessons

`Shape Stack`, `Tiger`, `Panda`, `Mushroom`, `City Bus`, `Walking Person`, `Pizza`, `Unicorn`, `Moon Rover`, `Treehouse`.

### V2.8 — +40 → 156 release lessons

`Curves & Spirals`, `Giraffe`, `Zebra`, `Cactus`, `Train`, `Sitting Person`, `Ice Cream`, `Mermaid`, `Satellite`, `Lighthouse`.

### V2.9 — +40 → 196 release lessons

`Symmetry Emblem`, `Monkey`, `Rabbit`, `Leaf`, `Bicycle`, `Hairstyle Portrait`, `Cupcake`, `Wizard`, `Telescope`, `City Skyline`.

### V3.0 — +40 → 236 release lessons

`Form & Volume`, `Squirrel`, `Deer`, `Mountain`, `Motorcycle`, `Dancer`, `Fruit Bowl`, `Fairy`, `Comet`, `Bridge`.

### V3.1 — +40 → 276 release lessons

`Texture Sampler`, `Horse`, `Cow`, `Waterfall`, `Airplane`, `Athlete`, `Sandwich`, `Phoenix`, `Space Station`, `Cafe Storefront`.

### V3.2 — +40 → 316 release lessons

`Pig`, `Sheep`, `Rain Cloud`, `Helicopter`, `Musician`, `Mug`, `Friendly Monster`, `Rainy Day Scene`, `Mandala`, `Comic Reaction Panel`.

### V3.3 — +40 → 356 release lessons

`Frog`, `Crocodile`, `Seashell`, `Sailboat`, `Chef`, `Backpack`, `Magic Potion`, `Picnic Scene`, `Textile Pattern`, `Action Motion Panel`.

### V3.4 — +40 → 396 release lessons

`Dolphin`, `Whale`, `Tractor`, `Doctor`, `Sneaker`, `Beach Day Scene`, `Decorative Border`, `Three-Panel Mini Story`, `Gadget Concept`, `Festival Kite`.

### V3.5 — +40 → 436 release lessons

`Octopus`, `Penguin`, `Fire Truck`, `Builder`, `Teddy Bear`, `Night Camp Scene`, `Toy Invention`, `Dream Room`, `Decorative Diya Lamp`, `Rangoli / Kolam Pattern`.

## 7. Capability truth is stage-specific, not quota-driven

The program does **not** impose a percentage quota for Trace, Help or Coloring.

Before each stage is authored, its stage contract must declare the exact package truth for every lesson:

- `DRAW_WITH_ME`
- `WATCH_THEN_DRAW`
- `TRACE_AND_LEARN`
- authored Help availability/levels
- Trace readiness
- Coloring: `NONE`, `FREEHAND`, `PREPARED_ONLY`, or `GUIDED_PREPARED`
- Replay behavior
- strings/previews/thumbnails and any other authored assets

Rules:
- never advertise a mode/capability that the package cannot execute completely;
- never invent Trace geometry to make a metric look good;
- Help remains child-invoked/authored;
- no enforced suggested colors unless the contract/runtime genuinely supports them;
- no fake audio declaration;
- catalog/index remains discovery metadata; the runtime package remains capability truth.

## 8. Non-negotiable stage execution loop

Every content stage follows this order.

1. **Freeze stage target.** Confirm baseline catalog SHA/count and exact 10-family / 40-lesson matrix.
2. **Freeze stage contract.** Exact IDs, titles, age eligibility, difficulty, taxonomy, prerequisites/journeys, capability matrix and visual direction.
3. **Author packages.** Normal Content Studio-compatible structure only.
4. **Production-project index.** Regenerate Catalog Index V2 only from the production projector.
5. **Automated content gates.** Production loader, capability validator, canonical Content Studio round-trip, strings/assets, duplicates, taxonomy, prerequisite graph, diagnostics and committed-index drift.
6. **Static visual gate.** Generate family contact sheets for preview + thumbnail; compare all four age variants side by side.
7. **Functionality/options audit.** Re-run the full product matrix in Section 9; do not assume a content-only change cannot regress UI/routing.
8. **Scale/package audit.** Catalog performance, Home index-only behavior, APK size and permission diff.
9. **Exact-head CI.** Tests/lint/build/content gates fully green on the exact candidate SHA.
10. **Durably signed candidate.** Increasing versionCode, same QA signing identity, signature verified.
11. **Physical update/smoke.** Install over previous accepted APK without uninstall; exercise representative old + new lessons.
12. **Merge exact verified head.** No unverified commits after candidate evidence.
13. **Merged-main CI.** Must be fully green.
14. **Stage health report.** Record counts, warnings, capabilities, performance, APK/signing/update result, functionality/options result and known physical limitations.
15. **Only then open the next stage.**

## 9. Functionality and options recheck after every stage

The following matrix is mandatory after **every** +40 stage.

### Profile / age / discovery
- onboarding and age-band selection;
- existing-profile age change;
- age eligibility and filtering;
- Home discovery and lesson cards;
- category/journey/search surfaces that exist in the current product;
- no full package scan introduced for browsing.

### Resume / launch / learning modes
- resume precedence: coloring resume → drawing resume → fresh recommendation;
- lesson launch and mode chooser;
- Draw With Me;
- Watch Then Draw;
- Trace & Learn only when authored;
- Replay where supported;
- fallback behavior for lessons without optional capabilities.

### Help / Trace / Coloring
- every surfaced Help action resolves to authored output;
- Trace never appears without authored trace-ready geometry;
- coloring routing matches `NONE` / `FREEHAND` / `PREPARED_ONLY` / `GUIDED_PREPARED`;
- unsupported options remain hidden rather than failing after tap.

### Creation / persistence
- drawing save;
- Gallery;
- drawing resume;
- coloring save/resume;
- Free Draw;
- completion state and fresh recommendation behavior.

### Product safety / architecture
- offline correctness;
- no account/network/runtime-AI dependency;
- no new Android permission except an explicitly reviewed contract change;
- no lesson-ID-specific runtime production branch;
- local adaptive behavior remains bounded/deterministic/advisory;
- no grades, ranks, XP, punitive streaks or permanent ability labels.

### Usability smoke
- readable labels;
- sensible touch targets;
- no clipped/overlapping controls from larger catalogs;
- long lists remain navigable;
- representative Little/Creative/Growing/Young lesson paths remain understandable.

Each stage health report must mark every applicable row `PASS`, `FAIL`, or `NOT RUN` with reason. “Content-only change” is not an acceptable reason to skip the audit.

## 10. Automated acceptance gates

For the current release catalog after each stage:

- all release packages load through the production loader;
- all pass canonical Content Studio round-trip;
- all declared capabilities agree with authored assets;
- production-generated Catalog Index V2 entry count equals release count;
- committed index drift = 0;
- unknown taxonomy references = 0;
- duplicate logical identities = 0;
- prerequisite graph failures = 0;
- all required strings/previews/thumbnails resolve;
- no `authored_signal` regression;
- no unsupported enforced suggested colors;
- no runtime-audio claim unless explicitly supported;
- warnings are reviewed and explained; new content does not silently grow warning debt;
- test/lint/build green;
- permission diff reviewed;
- exact package/version identity verified.

Threshold-specific scale checks:
- before/at V2.7 (116): validate the ~100-entry scale target;
- before/at V3.1/V3.2 (276/316): validate the ~250-entry scale target;
- before V3.5 and at program completion: validate the ~500-entry synthetic target even though release count is 436.

## 11. APK/update contract

The accepted V2.5 physical-update baseline is versionCode 32. All user-delivered QA builds after it must:

- keep application ID `com.navin.kidsdrawing`;
- use a versionCode greater than every previously delivered Kids Drawing QA APK;
- use the same durable QA signing certificate;
- verify Android signing before delivery;
- install directly over the immediately previous accepted baseline without uninstalling;
- preserve app data unless a separately reviewed migration requires otherwise.

A signing mismatch, version downgrade, “App not installed” update failure or data-loss regression blocks stage acceptance.

## 12. Stop conditions

Stop the current stage and do not start the next one when any of these is true:

- capability drift or unsupported option is surfaced;
- new unexplained warnings/errors;
- catalog/index drift;
- unknown taxonomy or duplicate identity;
- prerequisite graph failure;
- weak age differentiation in preview or thumbnail;
- existing-functionality/options regression;
- Home/discovery begins loading all lesson packages;
- material catalog performance or APK-size regression without review;
- new unreviewed permission/network/account/runtime-AI behavior;
- signing/version/update failure;
- exact-head CI red;
- physical smoke has an unresolved blocker;
- merged-main CI red.

## 13. Program completion definition

The +100-per-age program is complete only when:

- 400 new distinct lessons are accepted;
- 100 new lessons were added to each age group;
- release catalog is 436;
- all 100 families satisfy the stronger cross-age differentiation rule;
- capability truth remains package-authored and validated;
- all accepted catalog/index/Content Studio invariants still hold at scale;
- functionality/options audits were completed at every stage;
- direct update behavior remained working across stage APKs;
- final 436-entry main is CI-green;
- final physical/device acceptance evidence is recorded separately and accurately.

Count alone never completes the program.
