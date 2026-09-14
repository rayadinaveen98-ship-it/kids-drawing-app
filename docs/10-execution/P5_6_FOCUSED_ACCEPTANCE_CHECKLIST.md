# P5.6 Focused Physical Acceptance Checklist — Curriculum Expansion Set E

**Slice:** P5.6 / issue #84 / draft PR #85  
**Candidate:** `0.5.0-curriculum-expansion-p5.6-qa1`  
**versionCode:** **25**  
**Exact QA1 head:** `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`  
**Automated gate:** Android CI #538 / run `34846235868` — **GREEN**  
**Required binary:** profile artifact `10348300909` only

## Exact profile APK

- filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`
- size: **16,311,900 bytes**
- SHA-256: `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

Do not use the earlier pre-freeze engineering APK for this gate.

## Tester metadata

**Tester:** ____________________  
**Date:** ____________________  
**Device model:** ____________________  
**Android version / API:** ____________________

Do not invent missing tester/device information.

## Physical acceptance — 36 checks

1. Install the exact profile APK successfully with no unexpected permission prompt.
2. App reports/runs the P5.6 QA1 candidate corresponding to versionCode **25**; no stale P5.5 milestone identity is observed in the installed build metadata.
3. Production discovery exposes **24 release lessons** with all four Set-E lessons available under their intended age eligibility.
4. A Growing Artist profile can discover Face & Expressions, Simple Body & Pose and Create Your Character, but One-Point Room remains Young-only.
5. A Young Artist profile can discover all four Set-E lessons with mature, non-toddler presentation.
6. Face & Expressions — Draw With Me completes end-to-end with usable head/feature construction guidance.
7. Face & Expressions — Watch Then Draw completes end-to-end without losing session/artwork state.
8. Face Help progresses from conceptual hint to visual/anchor guidance and never exposes Trace.
9. Face `choose_expression` allows a visibly different authored expression and completes without similarity/copy pressure.
10. Simple Body & Pose — Draw With Me completes end-to-end with readable torso/pelvis/limb/pose construction.
11. Simple Body & Pose — Watch Then Draw completes end-to-end.
12. Body/Pose Help uses proportion/direction anchors, remains child-controlled and exposes no Trace.
13. Body `make_pose_yours` accepts a materially different pose/action/accessory choice without copy pressure.
14. Create Your Character — Draw With Me completes end-to-end.
15. Create Your Character — Watch Then Draw completes end-to-end.
16. Character Help treats the teacher character as technique/reference only and exposes no Trace or scoring language.
17. `create_your_character` accepts independently authored silhouette/outfit/expression/accessories/story choices without required similarity.
18. One-Point Room — Draw With Me completes end-to-end and reads as a credible older-child perspective lesson.
19. One-Point Room — Watch Then Draw completes end-to-end.
20. The vanishing point behaves only as guide/anchor information and is never a required tiny mark/precision tap.
21. Room convergence, near/far scale and furniture-depth guidance is understandable and usable at phone size.
22. `design_your_room` allows a materially different room purpose/layout/decor/furniture/story and completes without reference-matching pressure.
23. Growing Artist Companion/help wording across Set E is encouraging but technique/choice oriented rather than toddler praise.
24. Young Artist Companion/help wording is concise, studio-oriented, non-scoring and non-punitive.
25. Character Creator progression works in order: Face & Expressions → Simple Body & Pose → Create Your Character, with prerequisites behaving as authored.
26. One-Point Room remains outside Character Creator and its Sailboat Scene prerequisite behaves correctly.
27. Save/exit/reopen Face & Expressions restores the correct lesson/session/artwork without losing child marks.
28. Save/exit/reopen One-Point Room restores the correct lesson/session/artwork and preserves authored room work.
29. Background → foreground during a Set-E lesson preserves session state, Help state and child artwork.
30. App close/relaunch or equivalent lifecycle recovery resumes correctly without duplicating teacher/help overlays into child artwork.
31. Completed Set-E artwork saves to Gallery and reopens as expected.
32. Cross-lesson isolation holds: artwork/history/help state from one Set-E lesson never leaks into another lesson.
33. Airplane Mode: Set-E lesson discovery, teaching, Help, drawing, completion, save/reopen and Gallery remain functional without network dependency.
34. Representative prior-content regression smoke: Cute Cat and Little Fish still launch/draw/complete normally, including their established Trace/coloring behavior where applicable.
35. Representative advanced/prior-system regression smoke: Sailboat Scene plus Free Draw/Gallery continue to work and P5.6 changes do not alter their ownership/history behavior.
36. Across the full pass there is no crash, ANR, deadlock, lost artwork, unexpected account requirement, ad, behavioral-analytics dependency, network requirement, new sensitive permission, similarity score, grade/rank/XP or permanent ability label.

## Result

**Physical result:** ___ / 36 PASS  
**Failures / notes:**


## Acceptance rule

P5.6 physical acceptance is valid only when **36/36 PASS** is genuinely observed on the exact profile APK above.

If any binary/content-changing defect is found:
- keep PR #85 draft;
- do not merge;
- record the defect;
- fix it under the frozen contract;
- cut a new candidate with versionCode **>25**;
- regenerate immutable CI/artifact evidence;
- repeat the affected acceptance scope.

If all 36 pass, report the result truthfully. Final acceptance evidence must then be committed and pass exact-head CI before PR #85 can be marked ready.
