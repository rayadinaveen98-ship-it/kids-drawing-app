# P5.4 Focused Acceptance Checklist

**Slice:** P5.4 — Curriculum Expansion Set C #80  
**PR:** #81  
**QA candidate:** `0.5.0-curriculum-expansion-p5.4-qa1`  
**versionCode:** 23  
**Exact app/content commit:** `797d2219c4fe7f643d31f1ada42e08bacf7d105f`  
**Profile artifact:** `10342178179`  
**Profile APK:** `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.4_QA1-profile.apk`  
**Profile APK size:** `16,267,569 bytes`  
**Profile APK SHA-256:** `e220bb0ffc7a2a15e4cfedb9dd907a5850247e55611158cda2a29dc46c6d1200`  
**Status:** execution checklist only — do not mark PASS until actually performed

This checklist is intentionally narrower than P5.8 final 24-lesson release QA. It exists to accept or reject the exact Set-C QA1 candidate before P5.4 merge.

## A. Interactive Content Lab inspection

Record PASS/FAIL for every row. If a row cannot be inspected, record NOT RUN rather than inferring a result.

| # | Check | Result |
|---:|---|---|
| 1 | Content Lab opens through the engineering-only path and the production catalog reports 14 release lessons. | ⬜ |
| 2 | Happy Lines metadata shows intended Little + Creative eligibility, difficulty 1, foundations/line-control intent, and stable `happy-lines@1` identity. | ⬜ |
| 3 | Happy Lines preview/teacher/expected geometry stays inside the logical canvas, Trace support is visible only where authored, and the final open marks turn has no forced expected geometry. | ⬜ |
| 4 | Shape Friends metadata shows intended Little + Creative eligibility, difficulty 1, Happy Lines prerequisite, and stable `shape-friends@1` identity. | ⬜ |
| 5 | Shape Friends geometry visibly progresses from simple shapes to combined construction; Trace/reference support resolves; final friend-customization turn remains open authorship. | ⬜ |
| 6 | Rainbow Weather metadata shows intended Little + Creative eligibility, difficulty 2, Smiling Sun prerequisite, and stable `rainbow-weather@1` identity. | ⬜ |
| 7 | Rainbow Weather preview and step geometry are readable and exactly three large prepared coloring regions are visible/valid; suggested colors appear as suggestions rather than correctness requirements. | ⬜ |
| 8 | Tree Through Seasons metadata shows Creative + Growing + Young eligibility, difficulty 3, Easy Flower prerequisite, stable `tree-through-seasons@1`, Draw With Me + Watch Then Draw. | ⬜ |
| 9 | Tree Through Seasons teacher/reference geometry is readable, Help remains observational without Trace, and the final season/story step has no forced expected geometry. | ⬜ |
| 10 | Ice Cream Shop metadata shows Creative + Growing eligibility, difficulty 2, Shape Friends prerequisite, and stable `ice-cream-shop@1` identity. | ⬜ |
| 11 | Ice Cream Shop visibly progresses cone/base → scoops → sign → open customization; topping/sign ideas do not force a fixed match. | ⬜ |
| 12 | Localization strings/titles/instructions for all five lessons are resolved; no missing-key placeholders or broken labels appear. | ⬜ |
| 13 | Help levels/teacher refs/expected refs shown by Content Lab resolve without missing geometry or invalid references. | ⬜ |
| 14 | Diagnostics show 0 release errors; the only accepted warnings are `NO_JOURNEY_MEMBERSHIP` for Rainbow Weather, Tree Through Seasons and Ice Cream Shop. | ⬜ |
| 15 | No unexpected visual/content defect is found that requires changing the QA1 app/content binary. | ⬜ |

**Content Lab result:** `PENDING`  
**Reviewer:** `PENDING`  
**Date/device/emulator context if relevant:** `PENDING`

## B. Focused physical-device product acceptance

Install the exact profile APK identified above. Record the actual Android device/API used.

| # | Scenario | Result |
|---:|---|---|
| 1 | App installs/launches normally; no unexpected permission, account, sign-in or network requirement appears. | ⬜ |
| 2 | Studio/product discovery exposes 14 release lessons without duplicate/missing Set-C entries. | ⬜ |
| 3 | Happy Lines starts from the normal product path and teacher/child turns are clear. | ⬜ |
| 4 | Happy Lines selected Trace/strong Help appears correctly, does not contaminate child artwork, and can be reduced/hidden where expected. | ⬜ |
| 5 | Happy Lines final open marks turn accepts the child’s own marks and Done without copy/match pressure. | ⬜ |
| 6 | Shape Friends normal flow builds simple shapes into the combined friend without state leakage from Happy Lines. | ⬜ |
| 7 | Shape Friends Trace/reference support works on structured construction and the final open friend turn accepts arbitrary authored details. | ⬜ |
| 8 | Rainbow Weather drawing flow completes normally and enters its prepared coloring experience. | ⬜ |
| 9 | Rainbow prepared coloring exposes exactly three broad usable regions; fills stay below protected line art. | ⬜ |
| 10 | Rainbow recolor, Undo and Redo work correctly without losing line art or corrupting another region. | ⬜ |
| 11 | Rainbow final weather-detail/open-choice behavior does not demand matching the sample or suggested colors. | ⬜ |
| 12 | Tree Through Seasons completes in Draw With Me with clear teacher/child turns. | ⬜ |
| 13 | Tree Through Seasons completes in Watch Then Draw; whole-overview presentation remains distinct from later per-step reference. | ⬜ |
| 14 | Tree Help works without exposing Trace; no Trace control/overlay appears for Tree. | ⬜ |
| 15 | Tree final season/story variation accepts authored details with age-respectful Companion language. | ⬜ |
| 16 | Ice Cream Shop builds cone/base → scoops → sign correctly and final topping/sign customization is genuinely open. | ⬜ |
| 17 | Representative Little Artist presentation on Set C is short/warm without changing lesson semantics. | ⬜ |
| 18 | Representative Creative Explorer presentation uses technique + choice language appropriately. | ⬜ |
| 19 | Representative Growing Artist presentation on Tree/Ice Cream remains respectful and not toddler-oriented. | ⬜ |
| 20 | Representative Young Artist presentation on Tree is concise/studio-oriented and not toddler-ish. | ⬜ |
| 21 | Save & leave / reopen resumes a representative new lesson at the correct session state with artwork intact. | ⬜ |
| 22 | Completing/reopening a second new lesson proves cross-lesson session/artwork isolation; no prior lesson artwork/help state leaks in. | ⬜ |
| 23 | Completed Set-C work appears/reopens through Gallery as expected. | ⬜ |
| 24 | Existing Cute Cat smoke: opens and guided flow remains usable. | ⬜ |
| 25 | Existing Little Fish smoke: guided flow + coloring still work normally. | ⬜ |
| 26 | Free Draw smoke: open/draw/Undo/Redo/save/reopen remains lesson-independent. | ⬜ |
| 27 | Airplane Mode: normal core Studio → lesson → save/Gallery journey works without network dependency. | ⬜ |
| 28 | Background/return or pause/resume on a Set-C lesson does not reset, deadlock or show stale Companion state. | ⬜ |
| 29 | No crash, ANR, deadlock, lost artwork, unexpected state reset or unreadable blocking overlay occurs during the focused pass. | ⬜ |
| 30 | Overall candidate is acceptable for P5.4 merge with no binary-changing defect. | ⬜ |

**Physical result:** `PENDING / 0 of 30 recorded`  
**Device model:** `PENDING`  
**Android version/API:** `PENDING`  
**Tester:** `PENDING`  
**Date:** `PENDING`

## C. Acceptance rule

- PASS requires all mandatory rows above to be performed and accepted.
- A cosmetic or content defect must be recorded, triaged and classified; do not silently waive it.
- Any defect requiring app/content modification invalidates QA1 as the final physical candidate. Cut a new versionCode/QA candidate and regenerate exact CI/artifact evidence.
- If all rows pass without binary-changing defects, update `P5_4_QA.md` with the real Content Lab + physical evidence, run final exact-head acceptance-doc CI, then proceed to PR #81 merge gates.
