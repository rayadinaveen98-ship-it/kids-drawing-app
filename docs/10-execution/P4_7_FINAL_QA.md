# P4.7 — Final `0.4.0-content-studio` QA Matrix

**Issue:** #64  
**Parent epic:** #57  
**Branch:** `phase4/p4-7-final-release`  
**Target versionName:** `0.4.0-content-studio`  
**Initial candidate versionCode:** 19  
**Status:** matrix locked; exact final candidate APK not yet frozen

This is the authoritative Phase-4 physical/product release matrix. A green CI build is necessary but does not count as a physical PASS. Record only what was actually tested on the exact final candidate APK.

## Evidence header — fill after candidate freeze

- exact executable commit: PENDING
- accepted documentation head: PENDING
- final merge commit: PENDING
- versionName/versionCode: `0.4.0-content-studio` / 19 initially
- exact-head Android CI: PENDING
- merged-main Android CI: PENDING
- debug artifact: PENDING
- profile artifact: PENDING
- profile APK size: PENDING
- profile APK SHA-256: PENDING
- device/model/API: PENDING — never infer
- physical acceptance date: PENDING

## A. Fresh install, onboarding, Studio and discovery

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 1 | Clear app data / fresh launch | App starts cleanly with no stale artwork/session; onboarding appears | NOT RUN |
| 2 | Complete onboarding for age 4–5 | Profile completes; Little Artist presentation is readable and touch targets feel appropriately large | NOT RUN |
| 3 | Home/Studio after fresh profile | Multi-lesson Studio loads offline with no diagnostics/error state | NOT RUN |
| 4 | Catalog presence | All nine release lessons are discoverable through generic product routes | NOT RUN |
| 5 | Categories | Category browsing opens relevant lessons and back navigation is stable | NOT RUN |
| 6 | Art Journeys | First Shapes to Pictures, Animal Artist and Space Artist each expose meaningful multi-lesson sets | NOT RUN |
| 7 | Recommendation determinism | Returning to the same unchanged profile/state does not randomly reorder/recommend unrelated content | NOT RUN |
| 8 | Resume priority | An unfinished supported lesson resumes ahead of an unrelated new recommendation according to product policy | NOT RUN |
| 9 | Cross-lesson identity | Opening lesson A, returning, then opening lesson B shows B's preview/content with no A-specific art/copy | NOT RUN |

## B. P4.3 deferred representative-content regression

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 10 | Smiling Sun preview | Preview depicts Smiling Sun, never Cute Cat/another lesson | NOT RUN |
| 11 | Smiling Sun Trace & Learn | Trace path/help/replay work; child can complete the lesson | NOT RUN |
| 12 | Smiling Sun completion | No unsupported coloring choice appears; completion/Gallery path remains valid | NOT RUN |
| 13 | Friendly Owl preview + Draw With Me | Correct Owl artwork; lesson progresses normally | NOT RUN |
| 14 | Friendly Owl Help Ladder | Progressively exercise authored Help levels 1–5 without punitive copy or dead end | NOT RUN |
| 15 | Cumulative teacher construction | Earlier teacher parts remain faint while current Owl step is strong; child ink stays fully visible | NOT RUN |
| 16 | Replay construction safety | Replay does not duplicate/darken carried teacher references or contaminate child history | NOT RUN |
| 17 | Simple Rocket Watch Then Draw | Overview plays, clears for child turn, and remains replayable | NOT RUN |
| 18 | Watch Then Draw isolation | Full Rocket overview does not remain as a permanent tracing template | NOT RUN |
| 19 | Easy Flower grouped demo | Grouped multi-stroke demonstrations work at Extra Slow, Normal and Very Fast | NOT RUN |
| 20 | Set-A Gallery regression | Finish/reopen at least one non-Cute-Cat Set-A artwork from Gallery | NOT RUN |

## C. P4.6 representative-content spot checks

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 21 | Hot Air Balloon drawing | Draw With Me completes with correct preview and no cross-lesson state leak | NOT RUN |
| 22 | Hot Air Balloon guided Fill | Center → both sides → basket; two-side step advances only after both side regions are filled | NOT RUN |
| 23 | Fox Portrait older-child flow | Draw With Me/Watch Then Draw pacing feels credible; no Trace UI appears | NOT RUN |
| 24 | Design Your Spaceship creative step | `make_it_yours` accepts original child details without replica matching | NOT RUN |

## D. Free Draw — complete deferred P4.4 matrix

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 25 | Direct Home → Free Draw | Real blank/recovered canvas opens with no lesson prerequisite | NOT RUN |
| 26 | Little Artist canvas space | Controls remain reachable; bounded/scrolling tray leaves meaningful canvas area | NOT RUN |
| 27 | Pencil Small/Medium/Large | Continuous input; size changes are visible; no unacceptable post-lift snap | NOT RUN |
| 28 | Crayon Small/Medium/Large | Visibly distinct committed result from Pencil/Marker and remains so after reopen | NOT RUN |
| 29 | Marker Small/Medium/Large | Bold/consistent result after commit and reopen | NOT RUN |
| 30 | Palette | Exercise all child-safe colors; drawing resumes safely after Eraser → color selection | NOT RUN |
| 31 | Eraser | Representative stroke portions erase while editable history remains intact | NOT RUN |
| 32 | Undo/Redo branching | Undo/Redo works across stroke + erase; new edit after Undo invalidates old Redo branch | NOT RUN |
| 33 | Clear cancel | Clear dialog Cancel/Keep produces zero mutation | NOT RUN |
| 34 | Clear confirm + Undo | Confirm clears; immediate Undo restores pre-clear artwork | NOT RUN |
| 35 | Save & leave | Artwork and current tool/color/size recover on return from Home | NOT RUN |
| 36 | Background/foreground | Active Free Draw survives app background/foreground without operation loss | NOT RUN |
| 37 | Force-stop/relaunch Free Draw | Saved editable art and tool state recover after process recreation | NOT RUN |
| 38 | Save Free Draw to Gallery | Completion/Gallery entry opens correct art with `FREE_DRAW` semantics | NOT RUN |
| 39 | Fresh working canvas after finish | New Free Draw working canvas is blank while Gallery copy remains | NOT RUN |
| 40 | Gallery delete isolation | Delete Free Draw Gallery copy; working canvas/unrelated lesson art remain safe | NOT RUN |
| 41 | Age adaptation | Compare younger vs older profile: density changes but core tool/palette capability does not | NOT RUN |

## E. Expanded coloring — complete deferred P4.5/P4.6 matrix

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 42 | Little Fish Color With Me | Body → tail+fin progression works; outside/disallowed taps do not mutate artwork | NOT RUN |
| 43 | Little Fish Color Myself | Prepared regions can be filled/recolored in any order; Brush/Eraser remain available | NOT RUN |
| 44 | Fill recolor Undo/Redo | Undo reveals prior region color; Redo restores newer color | NOT RUN |
| 45 | Exact coloring Undo boundary | Remove all coloring ops via Undo; next Undo cannot cross into protected drawing/line-art history | NOT RUN |
| 46 | Coloring Save & leave | Fills/freehand/guided progress recover after leaving/reopening | NOT RUN |
| 47 | Coloring background/foreground | Existing fills and freehand color remain intact | NOT RUN |
| 48 | Coloring force-stop/relaunch | Process recreation restores artwork/history before session state; no incompatible/stale Fill state | NOT RUN |
| 49 | Coloring Gallery | Finished prepared coloring renders correctly in Gallery and on reopen | NOT RUN |
| 50 | Cute Cat legacy coloring | Freehand Color With Me/Color Myself still works and prepared Fill is not deceptively exposed | NOT RUN |
| 51 | Protected line art | Fill/Brush/Eraser never damage child/protected line art | NOT RUN |

## F. Guided lesson lifecycle/recovery and state isolation

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 52 | Drawing background/foreground mid-step | Child artwork remains; teacher/session resumes safely without duplicate callback | NOT RUN |
| 53 | Drawing force-stop/relaunch mid-lesson | Editable child document restores before runtime; resume route is safe | NOT RUN |
| 54 | Cross-lesson switch after recovery | Opening another lesson cannot inherit previous child ink/session state | NOT RUN |
| 55 | Help/replay after recovery | Help Ladder and Replay remain usable after lifecycle recovery | NOT RUN |
| 56 | No stranded artwork | If runtime/content cannot resume normally, artwork remains recoverable rather than being silently lost | NOT RUN |

## G. Gallery safety

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 57 | Lesson drawing completion → Gallery reopen | Correct editable source is rendered read-only; artwork intact | NOT RUN |
| 58 | Colored lesson completion → Gallery reopen | Fills/freehand/line art render together correctly | NOT RUN |
| 59 | Delete lesson Gallery entry | Only selected Gallery copy is removed; unrelated entries remain | NOT RUN |
| 60 | Delete Free Draw Gallery entry | Protected working Free Draw document remains untouched | NOT RUN |
| 61 | Gallery after app restart | Entries and previews remain available and open correctly | NOT RUN |

## H. Full offline / Airplane Mode

Enable Airplane Mode before this section and do not disable it until row 67.

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 62 | Offline Studio launch | Catalog/Home/Journeys/recommendations remain usable | NOT RUN |
| 63 | Offline guided lesson | Start and complete a representative drawing lesson | NOT RUN |
| 64 | Offline prepared coloring | Fill/recolor/save/reopen work without network | NOT RUN |
| 65 | Offline Free Draw | Draw, Save & leave, reopen, finish to Gallery | NOT RUN |
| 66 | Offline Gallery | Save/reopen lesson and Free Draw entries | NOT RUN |
| 67 | No hidden dependency | No account/network blocker prevents core art-learning journey | NOT RUN |

## I. Age bands, accessibility and presentation

Use profile/settings/system controls available in the build. Mark a row NOT APPLICABLE only when the stated capability is genuinely not exposed in this milestone and document why.

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 68 | Age 4–5 spot check | Large/reduced-density controls; beginner recommendations; canvas remains usable | NOT RUN |
| 69 | Age 6–7 spot check | Creative Explorer content/presentation feels appropriate | NOT RUN |
| 70 | Age 8–9 spot check | Growing Artist sees credible intermediate content including Owl/Fox/Spaceship where eligible | NOT RUN |
| 71 | Age 10–12 spot check | Young Artist receives mature-enough Fox/Spaceship experience; UI does not feel toddler-only | NOT RUN |
| 72 | Larger system font scale | Critical controls/text remain reachable/readable without blocking canvas/Finish actions | NOT RUN |
| 73 | Handedness setting if exposed | Layout follows selected handedness without moving artwork truth or hiding controls | NOT RUN |
| 74 | Voice off if exposed | Lessons remain fully usable visually with voice disabled | NOT RUN |
| 75 | Reduced motion if represented | Product remains usable and state-correct with reduced-motion behavior | NOT RUN |

## J. Integrity, contamination and stability

| # | Scenario | Expected result | Result |
| ---: | --- | --- | --- |
| 76 | Teacher/reference contamination | Teacher/trace/help/cumulative reference strokes never become child artwork/history | NOT RUN |
| 77 | Coloring integrity | Fill/freehand color operations never mutate protected line-art history | NOT RUN |
| 78 | Operation durability | No tested child drawing/color/fill operation disappears after save/reopen/process recreation | NOT RUN |
| 79 | No duplicate operations | Replay/recovery does not duplicate child/teacher operations or progressively darken references | NOT RUN |
| 80 | No cross-source deletion | Gallery delete cannot erase unrelated lesson/Free Draw working truth | NOT RUN |
| 81 | Stability sweep | No crash, ANR, deadlock or unrecoverable blank state across the completed matrix | NOT RUN |

## K. Automated/lab/release gates

| # | Gate | Expected result | Result |
| ---: | --- | --- | --- |
| 82 | Exact-head unit tests | PASS | PENDING |
| 83 | Exact-head lint | PASS | PENDING |
| 84 | Exact-head debug/instrumentation/profile APK build | PASS | PENDING |
| 85 | AndroidX Ink boundary verification | PASS | PENDING |
| 86 | Permission allowlist | PASS | PENDING |
| 87 | Nine-lesson catalog/content regression | PASS | PENDING |
| 88 | Art/Lesson/Quality automated regression suites | PASS where implemented in repository test/lab coverage | PENDING |
| 89 | Independent profile APK hash/size verification | Matches CI evidence exactly | PENDING |

## Release decision

Final result: **PENDING**.

Do not mark this matrix PASS merely because previous slices passed focused checks. P4.7 exists specifically to finish the deferred physical evidence and verify the complete product on the exact final candidate.

If any row reveals a release-blocking defect:
1. record FAIL with observation;
2. fix narrowly;
3. increment versionCode if a changed APK is distributed;
4. rerun relevant automated gate;
5. rerun the failed row plus reasonable neighboring regressions;
6. only then replace FAIL with a documented retest PASS.
