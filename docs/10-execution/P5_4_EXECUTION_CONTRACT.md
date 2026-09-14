# P5.4 — Curriculum Expansion Set C Execution Contract

**Parent epic:** #73  
**Issue:** #80  
**Starting verified main:** `e3553414c591ae5def3d9016c1a63e9d1a350f39`  
**Branch:** `phase5/p5-4-curriculum-set-c`  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Expected first distributed QA:** versionCode 23  
**Status:** LOCKED BEFORE CONTENT IMPLEMENTATION

## 1. Purpose

P5.4 is the first Phase-5 production-content batch. It adds exactly five new release lessons using the already accepted P5.1 curriculum contract, P5.2 content-production/inspection system, and P5.3 Companion V2.

This slice is content production and content QA. It does **not** create a new lesson runtime, catalog parser, drawing engine, companion state machine, coloring engine, or adaptive teaching system.

Release-catalog target for this slice: **9 → 14 lessons**.

## 2. Frozen architecture

The following remain authoritative and unchanged unless a concrete content defect proves otherwise:
- `LessonPackageLoader` and lesson schema;
- `LessonCatalog` auto-discovery and stable `(lessonId, revision)` identity;
- Drawing Engine / AndroidX Ink ownership boundary;
- `LessonSessionState` and existing teaching modes;
- authored Help Ladder semantics;
- P5.3 `ProductLessonPresentationPolicy` as read-only companion presentation;
- prepared-coloring region validation/rendering and protected line art;
- persistence/recovery/Gallery ownership;
- P5.2 `ContentQualityAnalyzer` and Content Lab.

No lesson-ID-specific product UI or runtime branches are permitted.

## 3. Locked Set-C catalog

### 3.1 Happy Lines

**Identity:** `happy-lines` revision 1  
**Age bands:** Little Artists + Creative Explorers  
**Difficulty:** 1  
**Estimated time:** 6 minutes  
**Categories:** `foundations`, `foundations.lines`  
**Skills:** `line.straight`, `line.curve`, `line.zigzag`, `line.loop`, `line.control`  
**Journey:** `journey.first_shapes_to_pictures`  
**Prerequisites:** none  
**Modes:** Draw With Me + Trace & Learn  
**Coloring:** disabled

Locked child-turn sequence:
1. `straight_lines` — grouped teacher example of three large straight marks; expected straight refs; Help 1 gentle hint + Help 4 trace path.
2. `curves_and_waves` — two large curve/wave marks; expected refs; Help 1 gentle hint + Help 2 visual guide + Help 4 trace path.
3. `zigzag` — one large continuous zigzag; expected ref; Help 1 gentle hint + Help 4 trace path.
4. `loops` — one large continuous loop chain; expected ref; Help 1 gentle hint + Help 4 trace path.
5. `make_marks_yours` — teacher shows a few example marks as ideas; `MANUAL_DONE`, `allowSkip=true`, **no expected stroke refs**. Child may turn favorite marks into any tiny pattern/picture. No matching requirement.

Pedagogical rule: geometry is intentionally large and uncluttered. Trace support exists for foundational motor practice, not scoring or accuracy judgment.

### 3.2 Shape Friends

**Identity:** `shape-friends` revision 1  
**Age bands:** Little Artists + Creative Explorers  
**Difficulty:** 1  
**Estimated time:** 7 minutes  
**Categories:** `foundations`, `foundations.shapes`, `characters`  
**Skills:** `shape.circle`, `shape.square`, `shape.triangle`, `shape.combine`, `placement.relative`  
**Journey:** `journey.first_shapes_to_pictures`  
**Prerequisite:** `happy-lines`  
**Modes:** Draw With Me + Trace & Learn  
**Coloring:** disabled

Locked child-turn sequence:
1. `circle` — large circle; expected ref; Help 1 + Help 4 trace.
2. `square` — large rounded/simple square; expected ref; Help 1 + Help 4 trace.
3. `triangle` — large triangle; expected ref; Help 1 + Help 4 trace.
4. `build_friend` — grouped circle/square/triangle construction that demonstrates relative placement; expected construction refs; Help 1 + Help 2 visual guide.
5. `make_friend_yours` — optional eyes/mouth/arms/hat/object examples only; `MANUAL_DONE`, `allowSkip=true`, **no expected refs**.

Pedagogical rule: first three turns practice shape closure; the fourth demonstrates combination; the fifth proves authorship. The child is never required to reproduce discretionary face/details.

### 3.3 Rainbow Weather

**Identity:** `rainbow-weather` revision 1  
**Age bands:** Little Artists + Creative Explorers  
**Difficulty:** 2  
**Estimated time:** 8 minutes  
**Categories:** `nature`, `nature.weather`, `scenes`  
**Skills:** `line.curve`, `placement.spacing`, `color.palette_choice`, `composition.balance`  
**Journey:** none in P5.4; do not invent an unapproved journey  
**Prerequisite:** `smiling-sun`  
**Modes:** Draw With Me  
**Coloring:** enabled, guided by default, prepared regions

Locked child-turn sequence:
1. `rainbow_arcs` — three large nested arcs played as one coherent group; expected refs; Help 1 + Help 2 visual guide.
2. `clouds` — two simple cloud contours/cluster outlines; expected refs; Help 1 + Help 2 visual guide.
3. `weather_details` — teacher shows rain/sun/sparkle examples, but child chooses which weather details to add; `MANUAL_DONE`, `allowSkip=true`, **no expected refs**.

Prepared-coloring contract:
- exactly three broad rainbow-band regions: `rainbow-outer-region`, `rainbow-middle-region`, `rainbow-inner-region`;
- optional cloud region is **not** introduced in this slice; keep the prepared region set simple and large;
- coloring steps proceed outer → middle → inner;
- suggested colors are optional roles only, never enforced;
- all prepared polygons must pass existing geometry validation and remain comfortably tappable for Little Artists.

Pedagogical rule: this lesson teaches spacing/balance and simple scene choice. It must not become a fixed “color the rainbow correctly” exercise.

### 3.4 Tree Through Seasons

**Identity:** `tree-through-seasons` revision 1  
**Age bands:** Creative Explorers + Growing Artists + Young Artists  
**Difficulty:** 3  
**Estimated time:** 11 minutes  
**Categories:** `nature`, `nature.trees`, `scenes`  
**Skills:** `shape.organic`, `contour.simple`, `detail.layering`, `composition.balance`, `creativity.variation`  
**Journey:** none in P5.4  
**Prerequisite:** `easy-flower`  
**Modes:** Draw With Me + Watch Then Draw  
**Coloring:** disabled

Locked child-turn sequence:
1. `trunk` — two organic contour strokes defining the trunk; expected refs; Help 1 gentle hint + Help 2 visual guide.
2. `branches` — grouped branch structure; expected refs; Help 1 + Help 3 direction anchors.
3. `canopy` — grouped loose canopy masses; expected refs; Help 1 + Help 2 visual guide.
4. `season_story` — teacher shows a small set of seasonal-detail ideas (leaves/blossoms/snow/bare twigs/ground marks) as references; `MANUAL_DONE`, `allowSkip=true`, **no expected refs**.

No Trace Help is authored for this lesson. Growing/Young use observation, replay, visual guides and direction anchors instead.

Pedagogical rule: the final tree can materially diverge from the teacher. Season/story/detail choice is the curriculum objective, not reference similarity.

### 3.5 Ice Cream Shop

**Identity:** `ice-cream-shop` revision 1  
**Age bands:** Creative Explorers + Growing Artists  
**Difficulty:** 2  
**Estimated time:** 9 minutes  
**Categories:** `everyday`, `food`, `design`  
**Skills:** `shape.combine`, `placement.relative`, `pattern`, `creativity.variation`  
**Journey:** none in P5.4  
**Prerequisite:** `shape-friends`  
**Modes:** Draw With Me  
**Coloring:** disabled

Locked child-turn sequence:
1. `cone_or_cup` — teacher demonstrates one simple cone/cup base construction; expected refs; Help 1 + Help 2 visual guide.
2. `scoops` — grouped stacked scoop contours; expected refs; Help 1 + Help 2 visual guide.
3. `shop_sign` — simple sign panel + support/detail; expected refs; Help 1 gentle hint.
4. `make_it_yours` — teacher shows topping/sign/sprinkle/cherry/wafer examples only; `MANUAL_DONE`, `allowSkip=true`, **no expected refs**.

Pedagogical rule: toppings, number/type of decorative marks, and sign details are authored choices. Completion never depends on copying those idea strokes.

## 4. Artwork and geometry contract

All five lessons use the existing 1000×1000 logical canvas.

- Primary subject geometry stays comfortably inside the canvas with generous phone-size margins.
- Little-targeted expected/trace geometry must use large paths and avoid tiny targets.
- Teacher strokes must read clearly at phone size and should not be fixture-like scribbles.
- Grouped demos are used only when the grouped strokes form one understandable construction unit.
- Idea/example strokes used on open turns are teacher/reference-only and are never required expected refs.
- Preview/thumbnail SVGs should show a finished, attractive example while remaining consistent with authored lesson geometry.
- Reference/teacher/help geometry is never persisted into child artwork.

## 5. Help contract

Set C deliberately exercises three support styles:
- selected Trace for Happy Lines / Shape Friends foundational motor-shape practice;
- visual guides for spacing/placement/construction;
- direction anchors for Tree branches.

Help levels remain monotonically stronger. No lesson silently changes Help based on child performance. Companion wording remains P5.3-derived and non-punitive.

## 6. Coloring contract

Only Rainbow Weather introduces new prepared coloring in P5.4.

- `minimumContentApi` must be 2 for Rainbow Weather because it declares prepared coloring regions.
- the other four lessons remain `minimumContentApi` 1 unless an existing production contract requires otherwise;
- prepared fills remain under protected line art;
- region polygons must validate and be large enough for younger children;
- color suggestions are optional and never correctness constraints.

## 7. Catalog / progression contract

Expected release catalog after Set C: **14 lessons**.

`journey.first_shapes_to_pictures` gains:
1. Happy Lines
2. Shape Friends
3. Smiling Sun
4. Little Fish
5. Easy Flower
6. Cute Cat

P5.4 does not alter existing lesson identities/revisions merely to force ordering. Prerequisites provide the intended progression:
- Shape Friends → Happy Lines
- Rainbow Weather → Smiling Sun
- Tree Through Seasons → Easy Flower
- Ice Cream Shop → Shape Friends

No existing lesson content is rewritten unless a concrete integration defect is demonstrated.

## 8. Test and content-quality gates

Before QA freeze:
- every new package loads through production `LessonPackageLoader`;
- all five appear as release entries in `LessonCatalog`;
- catalog total = 14;
- P5.2 quality report error count = 0;
- warnings must be reviewed and either fixed or explicitly documented; target is 0 warnings;
- stable identities/revisions are asserted;
- prerequisites resolve;
- all declared assets/localization keys resolve;
- all teacher/expected/help refs resolve;
- open creative turns are asserted to have empty `expectedStrokeRefs`;
- Happy Lines/Shape Friends Trace refs resolve;
- Rainbow prepared regions validate and coloring step refs resolve;
- Tree has no Trace Help and supports Watch Then Draw;
- representative cross-age Companion V2 presentation remains generic for new packages.

Content Lab inspection is required for all five lessons before physical product acceptance.

## 9. Physical QA boundary

The focused P5.4 device pass must exercise at minimum:
- Studio discovery/catalog growth to 14 release lessons;
- Happy Lines foundational Draw With Me + Trace Help + open final turn;
- Shape Friends Trace + combined-shape construction + open final turn;
- Rainbow Weather drawing + prepared guided coloring + recolor/Undo/Redo/protected line art;
- Tree Through Seasons Draw With Me + Watch Then Draw + Help without Trace + open seasonal variation;
- Ice Cream Shop construction + open customization;
- Little + Creative + Growing/Young presentation where supported;
- Save/reopen/resume and cross-lesson isolation on representative new lessons;
- Gallery reopen;
- existing Cute Cat, Little Fish, Free Draw smoke;
- Airplane Mode core journey;
- no crash/ANR/deadlock/lost artwork or unexpected permission/account/network requirement.

This focused pass does not replace P5.8 full 24-lesson release QA.

## 10. Delivery gates

P5.4 is complete only after:
1. contract committed before content behavior;
2. five production packages implemented exactly under this contract;
3. deterministic tests + P5.2 report show 14 lessons / 0 release errors;
4. Content Lab inspection evidence recorded;
5. exact-head CI green;
6. QA APK uses versionCode 23+ and has exact artifact ID/size/SHA evidence;
7. focused physical acceptance is recorded only for checks actually performed;
8. final acceptance-doc CI green;
9. draft PR becomes ready and squash-merges;
10. merged-main CI green;
11. issue #80 closes completed.

Never reuse distributed versionCode 22.
