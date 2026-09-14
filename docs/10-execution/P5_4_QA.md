# P5.4 QA — Curriculum Expansion Set C

**Slice:** P5.4 — Curriculum Expansion Set C #80  
**PR:** #81  
**Branch:** `phase5/p5-4-curriculum-set-c`  
**QA candidate:** `0.5.0-curriculum-expansion-p5.4-qa1`  
**versionCode:** 23  
**Exact app/content QA commit:** `797d2219c4fe7f643d31f1ada42e08bacf7d105f`  
**Status:** **ACCEPTED — AUTOMATED + CONTENT LAB + PHYSICAL QA PASS**  
**Last updated:** 2026-09-14

## 1. Scope delivered

P5.4 adds five production lessons through the existing `LessonPackageLoader` / `LessonCatalog` path:

1. `happy-lines@1`
2. `shape-friends@1`
3. `rainbow-weather@1`
4. `tree-through-seasons@1`
5. `ice-cream-shop@1`

The production catalog grows from 9 to **14 release lessons**.

Frozen Phase-5 boundaries remain intact: no lesson-ID runtime branches, no similarity scoring/grades/stars/rank/XP/permanent ability labels/cloud profiling, Companion V2 remains deterministic/read-only, and the offline/account-free/ad-free core is unchanged.

## 2. Generic Trace/open-authorship compatibility rule

Set C exposed one concrete pre-existing Trace contract mismatch. The accepted generic rule is:

- structured Trace & Learn steps remain traceable;
- an intentional open-authorship step is `MANUAL_DONE + allowSkip + expectedStrokeRefs.isEmpty()` and receives no forced Trace overlay;
- an authored Trace guide is preferred when present;
- otherwise validated expected child geometry can provide the Trace overlay.

This is documented in `docs/adr/ADR-008-trace-open-authorship.md` and `docs/10-execution/P5_4_CONTRACT_CLARIFICATION_01_TRACE_OPEN_AUTHORSHIP.md`.

## 3. Content-quality acceptance

Exact QA1 report:

- release lessons: **14**;
- errors: **0**;
- warnings: **3**;
- accepted warning code: `NO_JOURNEY_MEMBERSHIP` only;
- accepted warning lessons: `rainbow-weather`, `tree-through-seasons`, `ice-cream-shop` only.

Any other content-quality warning remains a failure. The analyzer was not weakened.

Coverage at QA1:

- Little Artists: 7 lessons;
- Creative Explorers: 13 lessons;
- Growing Artists: 9 lessons;
- Young Artists: 3 lessons;
- Draw With Me: 13;
- Watch Then Draw: 7;
- Trace & Learn: 4;
- coloring lessons: 4;
- prepared-coloring lessons: 3.

## 4. CI progression

- CI #503 — failed only on stale tests hardcoded to exactly 9 lessons.
- CI #506 / run `34829658746` — GREEN after expansion-safe regression fixes.
- CI #507 / run `34830243526` — GREEN for Rainbow Weather + Tree Through Seasons.
- CI #508 / run `34830695250` — GREEN for the complete 14-lesson Set C catalog.
- CI #509 / run `34831113980` — GREEN on exact frozen QA1 app/content commit.
- CI #510 / run `34832017058` — GREEN after QA/handoff synchronization.
- CI #511 / run `34832366705` — GREEN on the locked focused-acceptance checklist head before human acceptance recording.

## 5. Immutable QA1 APK evidence

### Release-like profile APK

- artifact: `kids-drawing-0.5.0-curriculum-expansion-p5.4-qa1-profile`
- artifact ID: `10342178179`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-profile.apk`
- size: **16,267,569 bytes**
- SHA-256: `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`

### Debug APK

- artifact ID: `10341973952`
- size: **20,480,506 bytes**
- SHA-256: `c47aa4167716ceb9123b683f23c546fb6460d5160ac80e060188db4038360e9d`

### Content-quality artifact

- artifact ID: `10342606597`

The Android permission allowlist passed; no unexpected sensitive permission was introduced.

## 6. Interactive Content Lab acceptance — PASS

On 2026-09-14 the tester reported the fixed P5.4 Content Lab acceptance matrix **all good**. The complete 15-row matrix is recorded in `P5_4_FOCUSED_ACCEPTANCE_CHECKLIST.md` as:

- **15/15 PASS**;
- all five Set-C lessons inspected;
- previews/step geometry/help/localization accepted;
- Rainbow prepared regions accepted;
- Tree confirmed without Trace;
- open-authorship turns accepted;
- diagnostics accepted with only the three reviewed standalone warnings;
- no binary-changing defect reported.

## 7. Focused physical-device acceptance — PASS

The tester reported the fixed 30-row physical matrix **all good** against the exact QA1 profile candidate.

Recorded result:

- **30/30 PASS**;
- 14-lesson discovery accepted;
- Happy Lines / Shape Friends / Rainbow Weather / Tree Through Seasons / Ice Cream Shop flows accepted;
- age-band Companion presentation accepted;
- save/reopen/session isolation accepted;
- Gallery accepted;
- Cute Cat / Little Fish / Free Draw regression smokes accepted;
- Airplane/offline core accepted;
- background/return accepted;
- no crash, ANR, deadlock, lost artwork, blocking overlay, unexpected permission/account/network requirement, or binary-changing defect reported.

Tester/device metadata:

- tester: `User / product tester`;
- date: `2026-09-14`;
- device model: `not provided`;
- Android version/API: `not provided`.

The missing device/API metadata is explicitly recorded rather than inferred. It does not change the reported functional pass result.

## 8. P5.4 acceptance decision

P5.4 QA1 is the accepted physical candidate. No new versionCode is required because no binary-changing defect was reported.

Remaining closure gates only:

1. final exact-head acceptance-document CI;
2. mark PR #81 ready for review;
3. squash-merge using the verified PR head SHA;
4. verify merged-main Android CI;
5. close issue #80 as completed;
6. freeze P5.4 and start P5.5 from verified `main`.
