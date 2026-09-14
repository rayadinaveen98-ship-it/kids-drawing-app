# P5.5 QA — Curriculum Expansion Set D

**Slice:** P5.5 — Curriculum Expansion Set D #82  
**PR:** #83  
**Branch:** `phase5/p5-5-curriculum-set-d`  
**QA candidate:** `0.5.0-curriculum-expansion-p5.5-qa1`  
**versionCode:** 24  
**Exact app/content QA commit:** `3a538b5f7c2db118a0006176b7093b0e22961f9b`  
**Status:** **AUTOMATED QA GREEN — INTERACTIVE ACCEPTANCE PENDING**  
**Last updated:** 2026-09-14

## 1. Scope delivered

P5.5 adds six production lessons through the existing structured `LessonPackageLoader` / `LessonCatalog` path:

1. `snail-garden@1`
2. `elephant-from-shapes@1`
3. `simple-car@1`
4. `sailboat-scene@1`
5. `planet-with-rings@1`
6. `friendly-alien@1`

The production catalog grows from 14 to **20 release lessons**.

No lesson-ID-specific runtime path was added. Existing drawing/session/persistence/Companion boundaries remain intact. Set-D lessons use the generic open-authorship behavior established by ADR-008 and do not introduce similarity scoring, grades, stars/XP, ranking, permanent ability labels, cloud profiling, mandatory accounts, ads or network dependency.

## 2. Curriculum/journey result

Accepted journey state:

- `journey.animal_artist`: Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.
- `journey.space_artist`: Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.
- Simple Car and Sailboat Scene remain intentionally standalone.

During Set-D validation, retained metadata was corrected so Little Fish and Cute Cat use the canonical P5.1 Animal Artist membership expected by the frozen curriculum contract. No drawing geometry, lesson identity or saved-artwork contract changed.

## 3. Exact content-quality acceptance

Android CI #525 / run `34837734724` produced:

- release lessons: **20**;
- errors: **0**;
- warnings: **5**;
- accepted warning code: `NO_JOURNEY_MEMBERSHIP` only;
- accepted warning lessons only: `rainbow-weather`, `tree-through-seasons`, `ice-cream-shop`, `simple-car`, `sailboat-scene`.

Coverage at QA1:

- Little Artists: **8**;
- Creative Explorers: **18**;
- Growing Artists: **14**;
- Young Artists: **6**;
- Draw With Me: **19**;
- Watch Then Draw: **13**;
- Trace & Learn: **4**;
- coloring lessons: **4**;
- prepared-coloring lessons: **3**.

Journeys reported by the analyzer:

- `journey.animal_artist`: 6 members;
- `journey.first_shapes_to_pictures`: 7 members;
- `journey.space_artist`: 4 members.

## 4. CI progression

- CI #519 — exposed a retained Animal Artist metadata mismatch in the new Set-D journey gate.
- commit `c217346f...` corrected Little Fish/Cute Cat to canonical Animal Artist membership.
- CI #521 — exposed two retained LessonCatalog fixture assumptions tied to the old Cute Cat metadata shape.
- commit `d5b10aa1...` repaired only those fixture assumptions.
- CI #522 — GREEN for verified Animal Batch A + canonical journey metadata.
- CI #523 — GREEN for Simple Car + Sailboat Scene / 18-lesson checkpoint.
- CI #524 — GREEN for complete Set D / 20-lesson content and quality gate.
- CI #525 / run `34837734724` — GREEN on exact frozen QA1 app/content commit `3a538b5f7c2db118a0006176b7093b0e22961f9b`.

## 5. Immutable QA1 APK evidence

### Release-like profile APK — physical acceptance candidate

- artifact: `kids-drawing-0.5.0-curriculum-expansion-p5.5-qa1-profile`
- artifact ID: `10344519403`
- artifact archive size: `12,416,287 bytes`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.5_QA1-profile.apk`
- APK size: **16,293,898 bytes**
- APK SHA-256: `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`

### Debug APK

- artifact: `kids-drawing-0.5.0-curriculum-expansion-p5.5-qa1-debug`
- artifact ID: `10345315128`
- artifact archive size: `16,618,025 bytes`
- APK size: **20,508,319 bytes**
- APK SHA-256: `79fd638cefa5e1e46a337d2a35a720a1586139a7aa3c8b864b893e880095e307`

### Content-quality artifact

- artifact: `kids-drawing-p5.2-content-quality-report`
- artifact ID: `10344354743`
- artifact archive SHA-256: `73defb78fe44c483ddc0f68dbae8af613f9974de4bd876df222995a71abb3a4f`

The milestone permission allowlist passed for both debug and profile APKs. The only requested permission is the app-local `com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; no unexpected sensitive permission was introduced.

## 6. Interactive acceptance gate — pending

The fixed matrix is `docs/10-execution/P5_5_FOCUSED_ACCEPTANCE_CHECKLIST.md`.

Required before P5.5 closure:

- Content Lab: **18/18 PASS**;
- exact-profile physical acceptance: **36/36 PASS**;
- no binary/content-changing defect;
- actual device/API recorded when supplied by the tester; never infer it.

Automated CI does **not** substitute for these observations.

## 7. Current P5.5 decision

The QA1 binary is valid and frozen for interactive acceptance. Do not rebuild or increment versionCode unless a binary/content-changing defect is found.

P5.5 is **not yet merge-ready**. Remaining gates:

1. Content Lab 18/18 observed PASS;
2. exact-profile physical 36/36 observed PASS;
3. commit acceptance results;
4. exact-head acceptance-document CI GREEN;
5. mark PR #83 ready for review;
6. squash-merge the verified PR head;
7. verify merged-main Android CI GREEN;
8. close issue #82 completed and freeze P5.5 before starting P5.6.
