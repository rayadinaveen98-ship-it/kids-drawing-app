# P5.4 QA — Curriculum Expansion Set C

**Slice:** P5.4 — Curriculum Expansion Set C #80  
**PR:** #81  
**Branch:** `phase5/p5-4-curriculum-set-c`  
**QA candidate:** `0.5.0-curriculum-expansion-p5.4-qa1`  
**versionCode:** 23  
**Exact QA commit:** `797d2219c4fe7f643d31f1ada42e08bacf7d105f`  
**Status:** **AUTOMATED QA1 PASS / INTERACTIVE CONTENT LAB + PHYSICAL ACCEPTANCE PENDING**  
**Last updated:** 2026-09-14

## 1. Scope delivered

Set C adds five production lessons through the existing production `LessonPackageLoader` / `LessonCatalog` path:

1. `happy-lines@1`
2. `shape-friends@1`
3. `rainbow-weather@1`
4. `tree-through-seasons@1`
5. `ice-cream-shop@1`

The production catalog grows from 9 to **14 release lessons**.

The slice preserves the frozen Phase-5 teaching/product contract:

- no lesson-ID-specific product or runtime branches;
- no scores, grades, stars, ranks, XP, permanent ability labels, punitive streaks, or similarity judgement;
- offline/account-free/ad-free core remains unchanged;
- Companion V2 remains a deterministic read-only presentation layer over authoritative lesson/session state;
- open creative turns use authored teacher ideas without forced expected geometry.

## 2. Narrow engine compatibility fix

Set C exposed one concrete pre-existing Trace contract mismatch. The accepted generic rule is now:

- structured Trace & Learn steps remain traceable;
- an intentional open-authorship step is identified by `MANUAL_DONE + allowSkip + expectedStrokeRefs.isEmpty()` and receives no forced Trace overlay;
- an authored Trace guide is preferred when present;
- otherwise validated expected child geometry can provide the Trace overlay.

This is documented by `docs/adr/ADR-008-trace-open-authorship.md` and `docs/10-execution/P5_4_CONTRACT_CLARIFICATION_01_TRACE_OPEN_AUTHORSHIP.md`. It is not a lesson-ID special case or state-machine redesign.

## 3. Automated curriculum/content acceptance

### Happy Lines

- Little + Creative;
- difficulty 1;
- Draw With Me + selected Trace;
- line-control progression;
- final `make_marks_yours` turn has no expected geometry and preserves authorship.

### Shape Friends

- Little + Creative;
- difficulty 1;
- prerequisite Happy Lines;
- Draw With Me + selected Trace;
- shape construction and combined friend;
- final `make_friend_yours` turn has no expected geometry.

### Rainbow Weather

- Little + Creative;
- difficulty 2;
- Draw With Me;
- prerequisite Smiling Sun;
- exactly three large prepared rainbow coloring regions;
- suggested colors are not enforced;
- final weather-detail turn is open authorship.

### Tree Through Seasons

- Creative + Growing + Young;
- difficulty 3;
- Draw With Me + Watch Then Draw;
- prerequisite Easy Flower;
- no Trace Help;
- direction-anchor/visual support remains observational;
- final season/story turn is open authorship.

### Ice Cream Shop

- Creative + Growing;
- difficulty 2;
- Draw With Me;
- prerequisite Shape Friends;
- cone/base, scoop stack, shop sign construction;
- final topping/sign customization has no expected geometry.

## 4. Content-quality evidence

Exact QA1 CI report on commit `797d2219c4fe7f643d31f1ada42e08bacf7d105f`:

- release lessons: **14**;
- errors: **0**;
- warnings: **3**;
- reviewed warning code: `NO_JOURNEY_MEMBERSHIP` only;
- reviewed warning lesson IDs: exactly `rainbow-weather`, `tree-through-seasons`, `ice-cream-shop`;
- any other content-quality warning remains a gate failure.

The analyzer itself was not weakened. The three warnings are intentionally retained because the frozen P5.4 contract gives those lessons no journey membership and forbids inventing an unapproved journey merely to clear a warning. See `P5_4_CONTENT_QA.md`.

Reported coverage at QA1:

- Little Artists: 7 lessons;
- Creative Explorers: 13 lessons;
- Growing Artists: 9 lessons;
- Young Artists: 3 lessons;
- difficulty 1: 5;
- difficulty 2: 6;
- difficulty 3: 2;
- difficulty 4: 1;
- Draw With Me: 13;
- Watch Then Draw: 7;
- Trace & Learn: 4;
- coloring lessons: 4;
- prepared-coloring lessons: 3.

These are progress values toward the P5.1 final 24-lesson target, not final Phase-5 coverage claims.

## 5. Companion V2 Set-C integration

`P5_4SetCCompanionIntegrationTest` exercises the five real production open-authorship turns across Little / Creative / Growing / Young profiles.

Automated assertions verify:

- Done remains available;
- authored optional Skip remains available;
- wording supports choice/authorship;
- presentation does not introduce copy/match pressure;
- presentation does not expose score/points/stars/XP/rank/grade/accuracy language;
- behavior is derived generically from lesson/session semantics rather than lesson IDs.

## 6. CI progression

- CI #503 — failed only on three historical tests hardcoded to exactly 9 production lessons; no runtime/content parser failure.
- CI #506 / run `34829658746` — GREEN after making historical coverage tests expansion-safe.
- CI #507 / run `34830243526` — GREEN for Rainbow Weather + Tree Through Seasons batch.
- CI #508 / run `34830695250` — GREEN for the complete 14-lesson Set C catalog including Ice Cream Shop and Companion integration.
- **CI #509 / run `34831113980` — GREEN on exact frozen QA1 commit `797d2219c4fe7f643d31f1ada42e08bacf7d105f`.**

QA1 #509 passed:

- JSON parsing;
- AndroidX Ink boundary verification;
- JVM unit tests;
- lint;
- debug APK compile;
- instrumentation APK compile;
- release-like profile APK compile;
- P5.2 content-quality report verification;
- Android permission allowlist;
- P5.4 QA1 evidence packaging and artifact upload.

## 7. Immutable QA1 APK evidence

### Release-like profile APK

- artifact name: `kids-drawing-0.5.0-curriculum-expansion-p5.4-qa1-profile`
- artifact ID: `10342178179`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-profile.apk`
- APK size: **16,267,569 bytes**
- SHA-256: `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`

### Debug APK

- artifact name: `kids-drawing-0.5.0-curriculum-expansion-p5.4-qa1-debug`
- artifact ID: `10341973952`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-debug.apk`
- APK size: **20,480,506 bytes**
- SHA-256: `c47aa4167716ceb9123b683f23c546fb6460d5160ac80e060188db4038360e9d`

### Content-quality artifact

- artifact name: `kids-drawing-p5.2-content-quality-report`
- artifact ID: `10342606597`

All three artifacts were generated from workflow run `34831113980` whose head SHA is the exact QA1 commit above.

## 8. Permission evidence

Both debug and profile milestone APKs passed the existing allowlist. The only requested/generated permission observed by the gate was:

`com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`

No unexpected sensitive permission was introduced by P5.4.

## 9. Content Lab acceptance

### Automated inspection-model coverage — PASS

Production catalog/package tests verify that the expanded catalog is discoverable through the existing inspection repository and strict production loader, and that package preview/thumbnail/strings/quality metadata remain available.

### Interactive Content Lab visual inspection — PENDING

This gate requires real inspection of all five new lessons in Content Lab. It has **not** been claimed complete by automated tests.

Required review:

- metadata and age/difficulty/category/skills;
- preview/thumbnail quality;
- step order and help progression;
- teacher/reference geometry;
- Rainbow prepared regions;
- open-authorship turns;
- absence of unintended Trace on Tree;
- no unexpected diagnostics beyond the three reviewed standalone warnings.

## 10. Focused physical-device acceptance — PENDING

The QA1 profile APK has been produced, but physical acceptance has not yet been claimed.

Required focused matrix:

- 14-lesson product discovery;
- Happy Lines complete flow including selected Trace and open final turn;
- Shape Friends complete flow including construction Trace fallback and open final turn;
- Rainbow Weather drawing + prepared coloring, recolor, Undo/Redo, protected line art;
- Tree Through Seasons in Draw With Me and Watch Then Draw, Help without Trace, open season/story turn;
- Ice Cream Shop construction + open topping/sign customization;
- age-band presentation checks across Little / Creative / Growing / Young;
- save/reopen/session isolation;
- Gallery completion visibility;
- Cute Cat / Little Fish / Free Draw regression smoke;
- Airplane/offline core smoke;
- no crash, ANR, lost artwork, unexpected permission, account requirement, or network dependency.

Device model/API and pass counts must be recorded from the actual physical run; they are intentionally not inferred here.

## 11. Remaining release gates

1. Perform interactive Content Lab inspection for all five Set-C lessons and record results.
2. Install the exact profile artifact `10342178179` and perform the focused physical matrix above.
3. Record the exact physical candidate identity, device/API, pass/fail matrix, and any defects.
4. If defects require code/content changes, cut a new QA candidate/versionCode and repeat exact-head evidence.
5. If acceptance passes, synchronize final P5.4 acceptance docs.
6. Run final exact-head acceptance-doc CI.
7. Mark PR #81 ready and squash-merge only after all P5.4 gates pass.
8. Verify merged-main Android CI on the exact squash merge.
9. Close issue #80 completed only after merged-main green.

Until those gates pass, P5.4 remains an accepted **automated QA candidate**, not a merged/completed slice.
