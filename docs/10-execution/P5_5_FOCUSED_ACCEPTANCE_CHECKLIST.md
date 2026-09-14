# P5.5 Focused Acceptance Checklist — Curriculum Expansion Set D

**Slice:** P5.5 / issue #82 / draft PR #83  
**QA candidate:** `0.5.0-curriculum-expansion-p5.5-qa1`, versionCode 24  
**Exact app/content QA commit:** `3a538b5f7c2db118a0006176b7093b0e22961f9b`  
**Binary source:** use only the exact release-like profile APK recorded in `P5_5_QA.md` after Android CI #525 succeeds.  

Do not infer PASS from automated tests. Record only what is actually observed. If any content/binary defect requires a change, invalidate QA1 and cut a new candidate/versionCode before retesting.

## A. Content Lab visual/content inspection — 18 checks

Mark each PASS / FAIL after interactive inspection.

1. Catalog / Content Lab shows **20 valid release lessons** with Set D present.
2. **Snail Garden** metadata, age/difficulty, preview and thumbnail are readable and appropriate.
3. Snail teacher/expected/help geometry is large and clear; `make_garden_yours` has no forced expected geometry.
4. **Elephant From Shapes** metadata, preview and thumbnail are credible for Creative/Growing/Young.
5. Elephant construction/proportion/help geometry is clear; `make_elephant_yours` is truly open.
6. **Simple Car** metadata, preview and thumbnail are readable and not over-detailed.
7. Car body/cabin/wheel construction is clear; `design_your_car` has no forced expected geometry.
8. **Sailboat Scene** metadata, preview and thumbnail communicate a subject + environment scene.
9. Sailboat horizon/depth/composition guides are useful; Watch Then Draw is appropriate; no Trace help appears; final scene turn is open.
10. **Planet With Rings** metadata, preview and thumbnail communicate centered form + ring overlap.
11. Planet front/back ring relationship reads clearly; final planet design/palette choice is optional authorship, not a correct-answer requirement.
12. **Friendly Alien** metadata, preview and thumbnail communicate silhouette/character construction.
13. Alien symmetry/parts guides are useful without scoring pressure; final personality turn is genuinely variable.
14. `journey.animal_artist` visibly contains the intended six-lesson progression: Little Fish, Snail Garden, Cute Cat, Friendly Owl, Elephant From Shapes, Fox Portrait.
15. `journey.space_artist` visibly contains Planet With Rings, Simple Rocket, Friendly Alien, Design Your Spaceship.
16. All six Set-D packages show resolved localization and useful Help references; no missing/broken assets or labels.
17. Content diagnostics match the accepted policy: **20 lessons / 0 errors / exactly five reviewed `NO_JOURNEY_MEMBERSHIP` warnings** for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car and Sailboat Scene only.
18. No visual/content defect is found that would require changing the QA binary/content.

**Content Lab result:** ___ / 18 PASS  
**Notes / failures:**

---

## B. Exact-profile physical-device acceptance — 36 checks

Install only the profile APK whose artifact ID, byte size and SHA-256 are recorded in `P5_5_QA.md` for exact QA commit `3a538b5f...`.

1. Exact profile APK installs/updates and launches successfully.
2. Studio/discovery shows **20 release lessons** and all six Set-D lessons can be opened.
3. Snail Garden Draw With Me starts and teacher/child turns are clear.
4. Snail Garden Watch Then Draw presents observation before drawing as expected.
5. Snail final shell/garden turn allows free authorship and completes without copying pressure.
6. Elephant Draw With Me construction flow works through body/head/ears/trunk/legs/face.
7. Elephant Watch Then Draw works and remains meaningfully distinct from per-step guided presentation.
8. Elephant Help gives useful construction/proportion support and never exposes Trace.
9. Elephant final variation turn accepts materially different details and completes normally.
10. Simple Car Draw With Me construction works with sensible body/wheel proportions.
11. Simple Car Watch Then Draw works normally.
12. Car final design turn allows different body/decals/context and completes without similarity grading.
13. Sailboat Draw With Me works through hull/sail/horizon/depth/composition.
14. Sailboat Watch Then Draw presents the whole scene first and then supports drawing.
15. Sailboat Help remains visual/conceptual; no Trace option/help appears.
16. Sailboat final scene turn allows different weather/land/birds/flags/details and completes normally.
17. Planet With Rings Draw With Me clearly teaches planet body then front/back ring overlap.
18. Planet Watch Then Draw works normally.
19. Planet Help is useful and no Trace appears.
20. Planet final world/palette/detail turn remains open with no enforced color answer.
21. Friendly Alien Draw With Me clearly builds silhouette → face → limbs/parts.
22. Friendly Alien Watch Then Draw works normally.
23. Alien Help supports balance/construction without scoring, Trace or copy pressure.
24. Alien final personality turn allows visibly different parts/expression/accessories/story and completes normally.
25. Little Artist profile presents Snail Garden with short, age-appropriate companion language.
26. Creative Explorer profile presents Set-D content clearly without excessive hand-holding.
27. Growing Artist profile presents construction/composition language with appropriate independence.
28. Young Artist profile presents Elephant/Sailboat/Alien in concise, non-toddler studio language.
29. Animal Artist discovery/progression includes all six intended members and Set-D entries are reachable normally.
30. Space Artist discovery/progression includes all four intended members and Planet is a valid journey entry.
31. Start a Set-D lesson, background/leave/reopen or relaunch, and verify resume/recovery preserves the expected lesson state/artwork.
32. Switch between two Set-D lessons and verify no strokes, Help state, step state or artwork leaks across lessons.
33. Save a completed Set-D drawing, open it from Gallery and verify the child artwork remains correct/editable with no teacher/help overlay contamination.
34. Regression smoke: open/complete representative prior content such as Cute Cat, Little Fish/Rainbow Weather, and Free Draw; no P5.5 regression is observed.
35. With Airplane Mode/offline and through a normal lifecycle transition (background/foreground and/or rotation where supported), core lesson/drawing/save flows remain functional.
36. No crash, ANR, deadlock, lost artwork, unexpected account/network requirement or new permission prompt occurs during the focused pass.

**Physical result:** ___ / 36 PASS  
**Device:** ____________________  
**Android/API:** ____________________  
**Notes / failures:**

---

## C. Acceptance rule

P5.5 may proceed to final acceptance-doc CI / PR ready / squash merge only when:

- Content Lab = **18/18 PASS**;
- physical exact-profile matrix = **36/36 PASS**;
- no binary/content-changing defect remains;
- actual device/API is recorded when supplied by the tester; never infer it;
- final acceptance evidence is committed and exact-head CI is green.
