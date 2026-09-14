# P5.7 QA — Local Adaptive Teaching

**Issue:** #86  
**PR:** #87  
**Branch:** `phase5/p5-7-local-adaptive-teaching`  
**Candidate:** `0.5.0-curriculum-expansion-p5.7-qa1`  
**versionCode:** **26**  
**Status:** **QA1 FROZEN — EXACT-HEAD CI / APK EVIDENCE / PHYSICAL ACCEPTANCE PENDING**

## 1. Pre-freeze evidence

The following gates were green before versionCode 26 was cut:

- Batch A adaptive-state foundation: Android CI #557 — GREEN.
- Batch B adaptive fresh recommendations: Android CI #558 — GREEN.
- Adaptive primary reason presentation: Android CI #560 / run `34859897678` — GREEN.
- Batch C child-controlled adaptive Help: Android CI #561 / run `34861064836` — GREEN.
- Cross-age deterministic pre-QA gate: Android CI #562 / run `34861549191` — GREEN.

The cross-age checkpoint head before the QA freeze was `c828d4bf801ddaf037b9b5b2b102588d7a0c6be8`.

## 2. QA1 freeze contract

QA1 is the first P5.7 distributed binary candidate and therefore uses:

- versionName `0.5.0-curriculum-expansion-p5.7-qa1`;
- versionCode **26**;
- P5.7-specific CI artifact names;
- debug APK for engineering inspection only;
- release-like `profile` APK as the only binary eligible for physical acceptance.

Any binary- or content-changing defect after this freeze invalidates QA1 and requires versionCode **>26** with new hashes/evidence. Documentation-only acceptance recording may remain on versionCode 26 if it does not alter the binary/content candidate.

## 3. Exact QA1 evidence

Populate only from the exact-head green CI run.

- QA1 freeze commit: `PENDING`
- Android CI run: `PENDING`
- content quality: expected **24 lessons / 0 errors / exactly 6 reviewed warnings**
- permission allowlist: `PENDING`
- debug artifact ID: `PENDING`
- debug APK size: `PENDING`
- debug SHA256: `PENDING`
- profile artifact ID: `PENDING`
- profile APK size: `PENDING`
- profile SHA256: `PENDING`

### Expected artifact names

- `kids-drawing-0.5.0-curriculum-expansion-p5.7-qa1-debug`
- `kids-drawing-0.5.0-curriculum-expansion-p5.7-qa1-profile`
- profile APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.7_QA1-profile.apk`

## 4. Automated acceptance requirements

QA1 exact-head CI must prove:

- unit tests pass;
- lint passes;
- debug / instrumentation / profile APKs compile;
- AndroidX Ink ownership boundary passes;
- production content remains 24 lessons with no content errors and only the six reviewed standalone-journey warnings;
- no Android permission beyond the existing app-local dynamic-receiver permission is requested;
- deterministic recommendation fixtures pass across all four age bands;
- resume precedence remains coloring → drawing → fresh;
- unmet prerequisites cannot become adaptive primary fresh suggestions;
- completed lessons do not dominate while eligible fresh lessons exist;
- adaptive recommendation reasons remain non-judgmental;
- child Help remains explicitly initiated;
- adaptive Help cannot invent Trace or exceed authored Help/replay choices;
- missing/corrupt/incompatible adaptive state falls back safely;
- reducer/store remain bounded and idempotent;
- no artwork/strokes/score/ability/cloud/device payload is introduced.

## 5. Focused physical acceptance — exact profile APK only

Record device model/API exactly as reported by the tester. Do not infer them.

**Acceptance date:** `PENDING`  
**Tester-reported device model:** `PENDING`  
**Tester-reported Android/API:** `PENDING`

### A. Install / startup / offline

- [ ] A1 QA1 profile APK installs successfully.
- [ ] A2 app launches without crash.
- [ ] A3 existing child profile loads normally.
- [ ] A4 Airplane Mode: Home opens and recommendations remain available.
- [ ] A5 Airplane Mode: guided lesson starts and works end-to-end.
- [ ] A6 Airplane Mode: Gallery, Coloring and Free Draw remain usable.

### B. Fresh adaptive recommendations

For each age band, use a clean/local profile state appropriate to the existing product flow and verify the primary recommendation is sensible, stable and explainable.

- [ ] B1 Little Artist receives a sensible fresh suggestion.
- [ ] B2 Creative Explorer receives a sensible fresh suggestion.
- [ ] B3 Growing Artist receives a sensible fresh suggestion.
- [ ] B4 Young Artist receives a sensible fresh suggestion.
- [ ] B5 unchanged state + repeated Home launches keeps the same primary suggestion.
- [ ] B6 primary hero explanation is friendly/non-judgmental.
- [ ] B7 browse/category/journey cards remain visible and are not globally hidden by adaptive eligibility.

### C. Resume precedence

- [ ] C1 create an in-progress drawing lesson, leave, return Home: drawing resume outranks fresh adaptation.
- [ ] C2 create an active coloring session, leave, return Home: coloring resume outranks drawing/fresh.
- [ ] C3 resume hero shows resume-progress copy rather than adaptive-reason copy.
- [ ] C4 completing/exiting resume state returns Home to a sensible fresh recommendation.

### D. Progression / prerequisites / interests

- [ ] D1 an unmet-prerequisite lesson never becomes the fresh primary.
- [ ] D2 completing prerequisite work allows contracted journey continuation to influence the fresh primary.
- [ ] D3 completed lessons do not dominate while eligible fresh lessons remain.
- [ ] D4 a completed lesson remains available through browse/repeat surfaces.
- [ ] D5 changing an explicit interest through supported profile/onboarding flow changes suitable fresh recommendations where catalog content permits.
- [ ] D6 no UI text describes the child as weak/advanced, scored, ranked, graded or talented.

### E. Child-controlled adaptive Help

- [ ] E1 no Help opens automatically while the child is drawing.
- [ ] E2 tapping Help invokes only an authored Help/replay option.
- [ ] E3 Little Artist follows authored Help order without adaptive replay substitution.
- [ ] E4 for an older age band, repeated prior authored Help context may suggest Replay only when Replay is authored.
- [ ] E5 after an adaptive Replay choice, a later Help request can return to authored Help rather than looping Replay forever.
- [ ] E6 a step without authored Trace never receives Trace.
- [ ] E7 Help overlays remain outside the child artwork.
- [ ] E8 Less help / Hide help continue to work normally.

### F. Persistence / fallback / profile lifecycle

- [ ] F1 adaptive recommendation state survives ordinary app relaunch.
- [ ] F2 lesson completion influences later fresh recommendation only after genuine completion.
- [ ] F3 merely opening Home does not create visible progression changes.
- [ ] F4 clearing/resetting app data returns to empty/default adaptive behavior safely.
- [ ] F5 after a fresh local profile lifecycle, previous adaptive history is not visible/leaked.
- [ ] F6 no network connection is required to rebuild useful Home/Help behavior.

### G. P5.6 regression smoke

- [ ] G1 Draw With Me still works.
- [ ] G2 Watch Then Draw still works.
- [ ] G3 Trace & Learn still works only where authored.
- [ ] G4 Save & leave / recovery works.
- [ ] G5 lesson completion reaches Gallery correctly.
- [ ] G6 prepared Coloring works and child artwork remains isolated from overlays.
- [ ] G7 Free Draw remains lesson-independent and saves with correct provenance.
- [ ] G8 all 24 production lessons remain discoverable through expected surfaces.

**Physical result:** `PENDING`  
**Passed:** `PENDING / 45`  
**Failed:** `PENDING / 45`

## 6. Acceptance rule

P5.7 cannot be marked complete, PR #87 cannot be made ready, and issue #86 cannot close until:

1. the exact versionCode 26 QA1 head has green CI;
2. exact profile APK artifact ID, size and SHA256 are recorded;
3. the tester physically accepts that exact profile APK;
4. acceptance documentation is committed without changing the accepted binary;
5. acceptance-head CI is green;
6. PR #87 is squash-merged;
7. merged-main CI is green.
