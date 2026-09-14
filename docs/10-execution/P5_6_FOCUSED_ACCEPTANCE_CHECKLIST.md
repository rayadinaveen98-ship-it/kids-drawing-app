# P5.6 Focused Physical Acceptance Checklist — Curriculum Expansion Set E

**Slice:** P5.6 / issue #84 / PR #85  
**Candidate:** `0.5.0-curriculum-expansion-p5.6-qa1`  
**versionCode:** **25**  
**Exact QA1 head:** `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`  
**Automated gate:** Android CI #538 / run `34846235868` — **GREEN**  
**Required binary:** profile artifact `10348300909` only

## Exact profile APK

- filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`
- size: **16,311,900 bytes**
- SHA-256: `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

## Tester metadata

**Tester:** user-reported physical tester  
**Acceptance date:** 2026-09-14  
**Device model:** Not provided by tester  
**Android version / API:** Not provided by tester

## Physical acceptance — 36 checks

1. Exact profile APK installs with no unexpected permission prompt.
2. Installed build corresponds to P5.6 QA1 / versionCode 25.
3. Production discovery exposes 24 release lessons and all four Set-E lessons.
4. Growing profile sees Face, Body/Pose and Create Character; One-Point Room remains Young-only.
5. Young profile sees all four Set-E lessons with mature presentation.
6. Face Draw With Me completes end-to-end.
7. Face Watch Then Draw completes end-to-end.
8. Face Help remains conceptual/anchor based and exposes no Trace.
9. `choose_expression` supports authored divergence without copy pressure.
10. Body/Pose Draw With Me completes end-to-end.
11. Body/Pose Watch Then Draw completes end-to-end.
12. Body/Pose Help uses proportion/direction anchors and no Trace.
13. `make_pose_yours` accepts authored pose/action/accessory variation.
14. Create Your Character Draw With Me completes end-to-end.
15. Create Your Character Watch Then Draw completes end-to-end.
16. Character Help treats teacher art as technique/reference only; no Trace/scoring language.
17. `create_your_character` accepts authored silhouette/outfit/expression/accessories/story choices.
18. One-Point Room Draw With Me completes end-to-end as a credible older-child perspective lesson.
19. One-Point Room Watch Then Draw completes end-to-end.
20. Vanishing point behaves only as guide/anchor information, not a required tiny mark.
21. Convergence, near/far scale and furniture-depth guidance is usable at phone size.
22. `design_your_room` accepts authored purpose/layout/decor/furniture/story variation.
23. Growing Companion/help tone is technique/choice oriented rather than toddler praise.
24. Young Companion/help tone is concise, studio-oriented, non-scoring and non-punitive.
25. Character Creator progression works Face → Body/Pose → Create Character with prerequisites.
26. One-Point Room remains outside Character Creator and Sailboat prerequisite behaves correctly.
27. Save/exit/reopen Face restores correct session/artwork.
28. Save/exit/reopen One-Point Room restores correct session/artwork.
29. Background → foreground preserves session, Help state and child artwork.
30. App close/relaunch recovery resumes correctly without leaking teacher/help overlays into child artwork.
31. Completed Set-E artwork saves to Gallery and reopens correctly.
32. Cross-lesson artwork/history/help isolation holds.
33. Airplane Mode preserves discovery, teaching, Help, drawing, completion, save/reopen and Gallery.
34. Cute Cat and Little Fish regression smoke passes, including established Trace/coloring behavior where applicable.
35. Sailboat Scene plus Free Draw/Gallery regression smoke passes.
36. No crash, ANR, lost artwork, account/ad/network dependency, new sensitive permission, similarity score, grade/rank/XP or permanent ability label appears.

## Result

**Physical result: 36 / 36 PASS**  
**Failures / notes:** None reported by tester.  
**Device/API note:** not supplied and not inferred.

## Acceptance rule result

P5.6 physical acceptance is **ACCEPTED** on the exact profile APK above. No binary/content-changing defect was reported, so versionCode 25 remains the accepted P5.6 QA1 candidate.
