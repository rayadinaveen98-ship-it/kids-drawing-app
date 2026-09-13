# P4.6 — Representative Content Set B + Cross-content QA

**Issue:** #63  
**PR:** #71  
**Branch:** `phase4/p4-6-content-set-b`  
**Target candidate:** `0.4.0-content-studio-p4.6-qa1` / versionCode 18  
**Status:** QA candidate freeze in progress; physical/product rows remain unverified until run on the exact QA1 APK.

## Automated implementation evidence

The nine-lesson implementation head `7928c712e65e180a1d97042c0ea5d131e61c65fb` passed Android CI #425 / run `34758790360` before QA1 version/artifact renaming.

That run passed:
- committed JSON parsing;
- Drawing Engine AndroidX Ink boundary check;
- unit tests;
- lint;
- debug APK compile;
- instrumentation APK compile;
- profile APK compile;
- milestone permission allowlist.

This is implementation evidence only. The exact QA1 head created after version/workflow/docs changes still requires its own green CI before an APK is distributed.

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

Coverage intent after P4.6:
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

## Physical/product QA — exact QA1 APK required

Record device/model/API and exact APK evidence before marking PASS.

| # | Check | Status |
| ---: | --- | --- |
| 1 | Fresh launch/Home shows all expected P4.6 lessons through generic discovery | NOT RUN |
| 2 | Hot Air Balloon preview matches balloon artwork, not another lesson | NOT RUN |
| 3 | Hot Air Balloon Draw With Me completes drawing steps normally | NOT RUN |
| 4 | Hot Air Balloon guided coloring progresses center → both sides → basket | NOT RUN |
| 5 | Side-panel guided step accepts both authored regions and advances only when both are filled | NOT RUN |
| 6 | Hot Air Balloon Color Myself allows prepared-region recoloring; line art remains protected | NOT RUN |
| 7 | Hot Air Balloon coloring Undo/Redo behaves correctly | NOT RUN |
| 8 | Fox Portrait preview feels appropriate for older children | NOT RUN |
| 9 | Fox Portrait Draw With Me has usable pacing and proportion/detail guidance | NOT RUN |
| 10 | Fox Portrait Watch Then Draw replay works and no Trace UI appears | NOT RUN |
| 11 | Design Your Spaceship structured steps work in Draw With Me | NOT RUN |
| 12 | `make_it_yours` clearly invites choosing/combining/inventing rather than exact copying | NOT RUN |
| 13 | Creative step can complete without matching teacher accessory geometry | NOT RUN |
| 14 | Design Your Spaceship Watch Then Draw remains replayable | NOT RUN |
| 15 | Young Artist profile receives credible Fox/Spaceship discovery/recommendations | NOT RUN |
| 16 | Switching between new lessons does not leak child ink/session state | NOT RUN |
| 17 | Existing Smiling Sun/Friendly Owl/Simple Rocket/Easy Flower still open correctly | NOT RUN |
| 18 | Little Fish prepared Fill still works | NOT RUN |
| 19 | Cute Cat legacy freehand coloring still does not expose deceptive prepared Fill | NOT RUN |
| 20 | Free Draw remains independent and working | NOT RUN |
| 21 | Gallery promotion/reopen still preserves artwork/source provenance | NOT RUN |
| 22 | Airplane mode supports catalog/new lessons end-to-end | DEFERRED TO P4.7 unless explicitly rerun here |
| 23 | Process recreation/recovery across a P4.6 lesson | DEFERRED TO P4.7 unless explicitly rerun here |
| 24 | Small-screen/age-adaptive regression | DEFERRED TO P4.7 unless explicitly rerun here |

## QA1 evidence to fill after exact-head CI

- executable commit: PENDING
- Android CI run: PENDING
- debug artifact ID/name: PENDING
- profile artifact ID/name: PENDING
- profile APK size: PENDING
- profile APK SHA-256: PENDING
- physical device/API: PENDING
- user physical acceptance: PENDING

## Exit rule

Do not mark P4.6 complete or merge PR #71 until:
1. exact QA1 head CI is green;
2. exact profile APK evidence is recorded;
3. focused physical/product QA proves Hot Air Balloon coloring, Fox Portrait older-child experience and Spaceship creative variation;
4. any failures are fixed with a new monotonic versionCode if a materially different APK was already distributed;
5. PR is marked ready only after accepted evidence;
6. merged-main CI is green before issue #63 closes.
