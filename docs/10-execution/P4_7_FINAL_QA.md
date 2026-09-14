# P4.7 — Final `0.4.0-content-studio` QA Matrix

**Issue:** #64  
**Parent epic:** #57  
**Branch:** `phase4/p4-7-final-release`  
**Target versionName:** `0.4.0-content-studio`  
**Candidate versionCode:** 19  
**Status:** exact v19 final candidate physically/product accepted; repository closure pending

This is the authoritative Phase-4 physical/product release matrix. A green CI build is necessary but does not count as a physical PASS. Results below map only to the final checklist actually supplied to and accepted by the user. Failure-only fallback cases that were not deliberately induced remain identified as automated coverage rather than fabricated physical testing.

## Evidence header

- exact physically tested executable commit: `f3843365d39540de00fe08a008883c15abe75599`
- accepted documentation head: PENDING after this evidence commit set
- final merge commit: PENDING
- versionName/versionCode: `0.4.0-content-studio` / 19
- exact-candidate Android CI: #440 / run `34801122118` — GREEN
- merged-main Android CI: PENDING
- debug artifact: `10331472910` / `kids-drawing-0.4.0-content-studio-debug`
- profile artifact: `10331363240` / `kids-drawing-0.4.0-content-studio-profile`
- profile APK filename: `Kids_Drawing_0.4.0_Content_Studio-profile.apk`
- profile APK size: `16,196,353 bytes`
- profile APK SHA-256: `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`
- independent local profile hash/size verification: PASS; exact match with CI evidence
- device/model/API: not restated by the user in the final acceptance turn; never infer
- physical acceptance date: 2026-09-14
- user acceptance statement: **“final passed bro”** after being asked to run the complete ten-group final release checklist on this exact APK

## A. Fresh install, onboarding, Studio and discovery

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 1 | Clear app data / fresh launch | App starts cleanly with no stale artwork/session; onboarding appears | PASS — final checklist |
| 2 | Complete onboarding for age 4–5 | Profile completes; Little Artist presentation is readable and touch targets feel appropriately large | PASS — final checklist |
| 3 | Home/Studio after fresh profile | Multi-lesson Studio loads offline with no diagnostics/error state | PASS — final checklist |
| 4 | Catalog presence | All nine release lessons are discoverable through generic product routes | PASS — final checklist |
| 5 | Categories | Category browsing opens relevant lessons and back navigation is stable | PASS — final checklist |
| 6 | Art Journeys | First Shapes to Pictures, Animal Artist and Space Artist each expose meaningful multi-lesson sets | PASS — final checklist |
| 7 | Recommendation determinism | Returning to the same unchanged profile/state does not randomly reorder/recommend unrelated content | PASS — final checklist |
| 8 | Resume priority | An unfinished supported lesson resumes ahead of an unrelated new recommendation according to product policy | AUTOMATED PASS — deterministic resume policy is covered by the frozen P4.2 product tests; this exact failure mode was not separately forced in the supplied final physical checklist |
| 9 | Cross-lesson identity | Opening lesson A, returning, then opening lesson B shows B's preview/content with no A-specific art/copy | PASS — final checklist |

## B. P4.3 deferred representative-content regression

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 10 | Smiling Sun preview | Preview depicts Smiling Sun, never Cute Cat/another lesson | PASS — final checklist |
| 11 | Smiling Sun Trace & Learn | Trace path/help/replay work; child can complete the lesson | PASS — final checklist |
| 12 | Smiling Sun completion | No unsupported coloring choice appears; completion/Gallery path remains valid | PASS — final checklist |
| 13 | Friendly Owl preview + Draw With Me | Correct Owl artwork; lesson progresses normally | PASS — final checklist |
| 14 | Friendly Owl Help Ladder | Progressively exercise authored Help levels 1–5 without punitive copy or dead end | PASS — final checklist |
| 15 | Cumulative teacher construction | Earlier teacher parts remain faint while current Owl step is strong; child ink stays fully visible | PASS — final checklist |
| 16 | Replay construction safety | Replay does not duplicate/darken carried teacher references or contaminate child history | PASS — final checklist |
| 17 | Simple Rocket Watch Then Draw | Overview plays, clears for child turn, and remains replayable | PASS — final checklist |
| 18 | Watch Then Draw isolation | Full Rocket overview does not remain as a permanent tracing template | PASS — final checklist |
| 19 | Easy Flower grouped demo | Grouped multi-stroke demonstrations work at Extra Slow, Normal and Very Fast | PASS — final checklist |
| 20 | Set-A Gallery regression | Finish/reopen at least one non-Cute-Cat Set-A artwork from Gallery | PASS — final checklist |

## C. P4.6 representative-content spot checks

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 21 | Hot Air Balloon drawing | Draw With Me completes with correct preview and no cross-lesson state leak | PASS — final checklist |
| 22 | Hot Air Balloon guided Fill | Center → both sides → basket; two-side step advances only after both side regions are filled | PASS — final checklist |
| 23 | Fox Portrait older-child flow | Draw With Me/Watch Then Draw pacing feels credible; no Trace UI appears | PASS — final checklist |
| 24 | Design Your Spaceship creative step | `make_it_yours` accepts original child details without replica matching | PASS — final checklist |

## D. Free Draw — complete deferred P4.4 matrix

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 25 | Direct Home → Free Draw | Real blank/recovered canvas opens with no lesson prerequisite | PASS — final checklist |
| 26 | Little Artist canvas space | Controls remain reachable; bounded/scrolling tray leaves meaningful canvas area | PASS — final checklist |
| 27 | Pencil Small/Medium/Large | Continuous input; size changes are visible; no unacceptable post-lift snap | PASS — final checklist |
| 28 | Crayon Small/Medium/Large | Visibly distinct committed result from Pencil/Marker and remains so after reopen | PASS — final checklist |
| 29 | Marker Small/Medium/Large | Bold/consistent result after commit and reopen | PASS — final checklist |
| 30 | Palette | Exercise child-safe colors; drawing resumes safely after Eraser → color selection | PASS — final checklist |
| 31 | Eraser | Representative stroke portions erase while editable history remains intact | PASS — final checklist |
| 32 | Undo/Redo branching | Undo/Redo works across stroke + erase; new edit after Undo invalidates old Redo branch | PASS — final checklist |
| 33 | Clear cancel | Clear dialog Cancel/Keep produces zero mutation | PASS — final checklist |
| 34 | Clear confirm + Undo | Confirm clears; immediate Undo restores pre-clear artwork | PASS — final checklist |
| 35 | Save & leave | Artwork and current tool/color/size recover on return from Home | PASS — final checklist |
| 36 | Background/foreground | Active Free Draw survives app background/foreground without operation loss | PASS — final checklist |
| 37 | Force-stop/relaunch Free Draw | Saved editable art and tool state recover after process recreation | PASS — final checklist |
| 38 | Save Free Draw to Gallery | Completion/Gallery entry opens correct art with `FREE_DRAW` semantics | PASS — final checklist |
| 39 | Fresh working canvas after finish | New Free Draw working canvas is blank while Gallery copy remains | PASS — final checklist |
| 40 | Gallery delete isolation | Delete Free Draw Gallery copy; working canvas/unrelated lesson art remain safe | PASS — final checklist |
| 41 | Age adaptation | Compare younger vs older profile: density changes but core tool/palette capability does not | PASS — final checklist |

## E. Expanded coloring — complete deferred P4.5/P4.6 matrix

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 42 | Little Fish Color With Me | Body → tail+fin progression works; outside/disallowed taps do not mutate artwork | PASS — final checklist |
| 43 | Little Fish Color Myself | Prepared regions can be filled/recolored in any order; Brush/Eraser remain available | PASS — final checklist |
| 44 | Fill recolor Undo/Redo | Undo reveals prior region color; Redo restores newer color | PASS — final checklist |
| 45 | Exact coloring Undo boundary | Remove all coloring ops via Undo; next Undo cannot cross into protected drawing/line-art history | PASS — final checklist |
| 46 | Coloring Save & leave | Fills/freehand/guided progress recover after leaving/reopening | PASS — final checklist |
| 47 | Coloring background/foreground | Existing fills and freehand color remain intact | PASS — final checklist |
| 48 | Coloring force-stop/relaunch | Process recreation restores artwork/history before session state; no incompatible/stale Fill state | PASS — final checklist |
| 49 | Coloring Gallery | Finished prepared coloring renders correctly in Gallery and on reopen | PASS — final checklist |
| 50 | Cute Cat legacy coloring | Freehand Color With Me/Color Myself still works and prepared Fill is not deceptively exposed | PASS — final checklist |
| 51 | Protected line art | Fill/Brush/Eraser never damage child/protected line art | PASS — final checklist |

## F. Guided lesson lifecycle/recovery and state isolation

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 52 | Drawing background/foreground mid-step | Child artwork remains; teacher/session resumes safely without duplicate callback | PASS — final checklist |
| 53 | Drawing force-stop/relaunch mid-lesson | Editable child document restores before runtime; resume route is safe | PASS — final checklist |
| 54 | Cross-lesson switch after recovery | Opening another lesson cannot inherit previous child ink/session state | PASS — final checklist |
| 55 | Help/replay after recovery | Help Ladder and Replay remain usable after lifecycle recovery | PASS — final checklist |
| 56 | No stranded artwork | If runtime/content cannot resume normally, artwork remains recoverable rather than being silently lost | AUTOMATED PASS — failure/fallback contract remains covered by frozen P2/P3 recovery tests; missing/incompatible content was not intentionally injected into the supplied physical checklist |

## G. Gallery safety

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 57 | Lesson drawing completion → Gallery reopen | Correct editable source is rendered read-only; artwork intact | PASS — final checklist |
| 58 | Colored lesson completion → Gallery reopen | Fills/freehand/line art render together correctly | PASS — final checklist |
| 59 | Delete lesson Gallery entry | Only selected Gallery copy is removed; unrelated entries remain | PASS — final checklist |
| 60 | Delete Free Draw Gallery entry | Protected working Free Draw document remains untouched | PASS — final checklist |
| 61 | Gallery after app restart | Entries and previews remain available and open correctly | PASS — final checklist |

## H. Full offline / Airplane Mode

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 62 | Offline Studio launch | Catalog/Home/Journeys/recommendations remain usable | PASS — final checklist |
| 63 | Offline guided lesson | Start and complete a representative drawing lesson | PASS — final checklist |
| 64 | Offline prepared coloring | Fill/recolor/save/reopen work without network | PASS — final checklist |
| 65 | Offline Free Draw | Draw, Save & leave, reopen, finish to Gallery | PASS — final checklist |
| 66 | Offline Gallery | Save/reopen lesson and Free Draw entries | PASS — final checklist |
| 67 | No hidden dependency | No account/network blocker prevents core art-learning journey | PASS — final checklist |

## I. Age bands, accessibility and presentation

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 68 | Age 4–5 spot check | Large/reduced-density controls; beginner recommendations; canvas remains usable | PASS — final checklist |
| 69 | Age 6–7 spot check | Creative Explorer content/presentation feels appropriate | PASS — final checklist |
| 70 | Age 8–9 spot check | Growing Artist sees credible intermediate content including Owl/Fox/Spaceship where eligible | PASS — final checklist |
| 71 | Age 10–12 spot check | Young Artist receives mature-enough Fox/Spaceship experience; UI does not feel toddler-only | PASS — final checklist |
| 72 | Larger system font scale | Critical controls/text remain reachable/readable without blocking canvas/Finish actions | PASS — final checklist |
| 73 | Handedness setting if exposed | Layout follows selected handedness without moving artwork truth or hiding controls | NOT APPLICABLE — no handedness control is exposed in `0.4.0-content-studio` |
| 74 | Voice off if exposed | Lessons remain fully usable visually with voice disabled | NOT APPLICABLE — no product voice toggle is exposed in this milestone |
| 75 | Reduced motion if represented | Product remains usable and state-correct with reduced-motion behavior | NOT APPLICABLE — no reduced-motion product setting is exposed in this milestone |

## J. Integrity, contamination and stability

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 76 | Teacher/reference contamination | Teacher/trace/help/cumulative reference strokes never become child artwork/history | PASS — final integrity sweep |
| 77 | Coloring integrity | Fill/freehand color operations never mutate protected line-art history | PASS — final integrity sweep |
| 78 | Operation durability | No tested child drawing/color/fill operation disappears after save/reopen/process recreation | PASS — final integrity sweep |
| 79 | No duplicate operations | Replay/recovery does not duplicate child/teacher operations or progressively darken references | PASS — final integrity sweep |
| 80 | No cross-source deletion | Gallery delete cannot erase unrelated lesson/Free Draw working truth | PASS — final integrity sweep |
| 81 | Stability sweep | No crash, ANR, deadlock or unrecoverable blank state across the completed matrix | PASS — final integrity sweep |

## K. Automated/lab/release gates

| # | Gate | Expected result | Result |
| ---: | --- | --- | --- |
| 82 | Exact-head unit tests | PASS | PASS — CI #440 |
| 83 | Exact-head lint | PASS | PASS — CI #440 |
| 84 | Exact-head debug/instrumentation/profile APK build | PASS | PASS — CI #440 |
| 85 | AndroidX Ink boundary verification | PASS | PASS — CI #440 |
| 86 | Permission allowlist | PASS | PASS — CI #440 |
| 87 | Nine-lesson catalog/content regression | PASS | PASS — CI #440 test/content gate |
| 88 | Art/Lesson/Quality automated regression suites | PASS where implemented in repository test/lab coverage | PASS — CI #440 full repository test gate |
| 89 | Independent profile APK hash/size verification | Matches CI evidence exactly | PASS — `16,196,353 bytes`; SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59` |

## Release decision

**PHYSICAL / PRODUCT / AUTOMATED RELEASE CANDIDATE PASS.**

The exact v19 candidate `f3843365d39540de00fe08a008883c15abe75599` passed the supplied final physical/product checklist and exact-candidate automated gate. Rows 8 and 56 are explicitly retained as automated failure/policy coverage rather than fabricated physical injection. Rows 73–75 are genuinely not applicable because those settings are not exposed in this milestone.

Remaining repository-only closure gates:
1. commit this acceptance evidence and complete the release report;
2. require exact-head CI green for the resulting documentation head;
3. close deferred P4.3 issue #60;
4. mark PR #72 ready and squash-merge;
5. require merged-main CI green;
6. close P4.7 issue #64 and Phase-4 epic #57;
7. record final merge/closure evidence without changing the physically accepted product binary.
