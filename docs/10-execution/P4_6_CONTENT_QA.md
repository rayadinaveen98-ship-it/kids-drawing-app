# P4.6 — Representative Content Set B + Cross-content QA

**Issue:** #63  
**PR:** #71  
**Branch:** `phase4/p4-6-content-set-b`  
**Candidate:** `0.4.0-content-studio-p4.6-qa1` / versionCode 18  
**Status:** Focused physical/product acceptance PASS on 2026-09-14; final exact-head documentation CI, PR merge, and merged-main verification remain.

## Automated implementation evidence

The nine-lesson implementation head `7928c712e65e180a1d97042c0ea5d131e61c65fb` passed Android CI #425 / run `34758790360` before QA1 version/artifact renaming.

Exact QA1 executable head `e96e452f41f545529712a35e9cf97553510252d8` passed Android CI #428 / run `34759214099`.

The exact QA1 run passed:
- committed JSON parsing;
- Drawing Engine AndroidX Ink boundary check;
- unit tests;
- lint;
- debug APK compile;
- instrumentation APK compile;
- profile APK compile;
- milestone permission allowlist;
- P4.6-specific evidence packaging and debug/profile artifact upload.

## Representative release set

| Lesson | Ages | Difficulty | Core proof |
| --- | --- | ---: | --- |
| Cute Cat | Creative, Growing | 1 | legacy all-mode lesson + freehand coloring |
| Smiling Sun | Little, Creative | 1 | trace-friendly beginner content |
| Friendly Owl | Creative, Growing | 2 | full Help Ladder |
| Simple Rocket | Creative, Growing | 2 | Watch Then Draw grouped demo |
| Easy Flower | Little, Creative, Growing | 2 | grouped multi-stroke construction |
| Little Fish | Little, Creative | 2 | prepared-region guided coloring |
| Hot Air Balloon | Little, Creative, Growing | 2 | richer 4-region guided coloring + multi-region step |
| Design Your Spaceship | Creative, Growing, Young | 3 | open-ended creative variation with no replica requirement |
| Fox Portrait | Growing, Young | 4 | older-child proportion/detail/observation |

Coverage after P4.6:
- all four age bands represented;
- difficulties 1–4 represented;
- First Shapes to Pictures, Animal Artist and Space Artist each have multiple meaningful lessons;
- Draw With Me and Watch Then Draw have multiple lessons;
- Trace & Learn remains represented by appropriate beginner content;
- prepared-region and legacy freehand coloring coexist;
- beginner high-assistance and older-child lighter-assistance patterns coexist;
- real open-ended creative choice exists without scoring against teacher geometry.

## New lesson quality review

### Hot Air Balloon
- 4 drawing steps, 10-minute estimate.
- Large envelope, panel lines, ropes/basket, optional sky details.
- Prepared regions: left panel, center panel, right panel, basket.
- Guided progression: center → both sides → basket.
- Suggested colors are not enforced.
- Region polygons are closed by runtime geometry semantics, non-degenerate and intentionally large enough for child taps.

### Fox Portrait
- 5 drawing steps, 14-minute estimate.
- Growing + Young only, difficulty 4.
- Focus: proportion, placement, balance, contour refinement, landmarks, texture and observation.
- No Trace & Learn dependency.
- Help uses hints/guides/anchors rather than mandatory tracing.
- Coloring intentionally disabled for r1 so the lesson stays focused on drawing craft.

### Design Your Spaceship
- 4 drawing steps, 12-minute estimate.
- Structured hull/cockpit/wings foundation followed by `make_it_yours` creative variation.
- Teacher examples are inspiration only.
- Creative child turn uses `manual_done`, `allowSkip=true`, and **empty `expectedStrokeRefs`**.
- No Trace & Learn mode, therefore no replica/trace contract applies to the creative step.

## Physical/product QA — exact QA1 APK

The user installed the exact profile APK from QA1 head `e96e452f41f545529712a35e9cf97553510252d8` and reported the supplied focused P4.6 checklist **PASS** on 2026-09-14. Device/model/API were not re-stated in that acceptance, so this record does not invent them.

| # | Check | Status |
| ---: | --- | --- |
| 1 | Home/Studio shows the three P4.6 lessons through generic discovery | PASS — user reported |
| 2 | Hot Air Balloon preview matches balloon artwork | PASS — user reported |
| 3 | Hot Air Balloon Draw With Me completes drawing steps normally | PASS — user reported |
| 4 | Hot Air Balloon guided coloring progresses center → both sides → basket | PASS — user reported |
| 5 | Side-panel step advances only after both authored side regions are filled | PASS — user reported |
| 6 | Hot Air Balloon Color Myself supports prepared-region recoloring and protected line art | PASS — user reported |
| 7 | Hot Air Balloon coloring Undo/Redo behaves correctly | PASS — user reported |
| 8 | Fox Portrait preview feels appropriate for older children | PASS — user reported |
| 9 | Fox Portrait Draw With Me pacing/proportion/detail guidance is usable | PASS — user reported |
| 10 | Fox Portrait Watch Then Draw replay works and no Trace UI appears | PASS — user reported |
| 11 | Design Your Spaceship structured Draw With Me steps work | PASS — user reported |
| 12 | `make_it_yours` communicates choosing/combining/inventing rather than exact copying | PASS — user reported |
| 13 | Creative step completes without matching teacher accessory geometry | PASS — user reported |
| 14 | Design Your Spaceship Watch Then Draw remains replayable | PASS — user reported |
| 15 | Young Artist discovery makes Fox Portrait / Design Your Spaceship available appropriately | PASS — user reported |
| 16 | Switching between new lessons does not leak child ink/session state | PASS — user reported |
| 17 | Smiling Sun/Friendly Owl/Simple Rocket/Easy Flower focused physical regression | NOT RUN in supplied P4.6 checklist; defer to P4.7 |
| 18 | Little Fish prepared Fill still works | PASS — user reported |
| 19 | Cute Cat legacy freehand coloring still avoids deceptive prepared Fill | PASS — user reported |
| 20 | Free Draw remains independent and working | PASS — user reported |
| 21 | Gallery promotion/reopen preserves artwork/source provenance for a new lesson | PASS — user reported |
| 22 | Airplane mode supports catalog/new lessons end-to-end | DEFERRED TO P4.7 |
| 23 | Process recreation/recovery across a P4.6 lesson | DEFERRED TO P4.7 |
| 24 | Small-screen/age-adaptive regression | DEFERRED TO P4.7 |

## QA1 evidence

- executable commit: `e96e452f41f545529712a35e9cf97553510252d8`
- versionName: `0.4.0-content-studio-p4.6-qa1`
- versionCode: `18`
- Android CI: #428 / run `34759214099` — GREEN
- debug artifact: `10318167923` / `kids-drawing-0.4.0-content-studio-p4.6-qa1-debug`
- profile artifact: `10318307419` / `kids-drawing-0.4.0-content-studio-p4.6-qa1-profile`
- profile APK size: `16,196,362 bytes`
- profile APK SHA-256: `e207d006893747a91a0c8dd6935d7764417fc77532a992a6d1120ec8bd613a4e`
- physical device/API: not re-stated by user in this acceptance
- user physical acceptance: focused supplied checklist PASS on 2026-09-14

## Exit rule / current gate

P4.6 product acceptance is satisfied for the focused QA1 scope. Remaining repository gates before issue #63 can close:
1. exact-head CI after acceptance-documentation commits must be green;
2. mark PR #71 ready;
3. squash-merge PR #71;
4. require merged-main CI green;
5. close issue #63 completed;
6. move deferred rows 17 and 22–24 into P4.7 final regression rather than overclaiming them here.
