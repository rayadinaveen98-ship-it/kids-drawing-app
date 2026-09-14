# P5.6 Pre-Freeze Content Lab Checklist — Curriculum Expansion Set E

**Slice:** P5.6 / issue #84 / draft PR #85  
**Purpose:** required engineering-only Content Lab inspection before the first distributed P5.6 QA freeze.  
**Exact verified head:** `9244e776707826e613a0bec43a22f60edaae3ed0`  
**Automated gate:** Android CI #534 / run `34843509513` — **GREEN**  
**Verified quality:** **24 lessons / 0 errors / exactly 6 reviewed warnings**  

Do not infer PASS from automated tests. Record only observed Content Lab results. If a visual/content defect requires changing lesson content, fix it before versionCode 25 is cut.

## Engineering-only inspection binary

Use only the profile APK from CI #534 for this pre-freeze inspection:
- artifact ID `10346199935`
- APK size **16,311,900 bytes**
- SHA-256 `11d9459cbeefff3ff26a457b7a28b7e082ce08b55c8c8b70ce546444e24b4867`

The artifact/APK internal version still carries the inherited P5.5 QA1 label because the P5.6 v25 freeze is deliberately deferred until this checklist passes. This is **engineering inspection evidence only**, not a P5.5 acceptance binary and not yet P5.6 QA1.

Content Lab activity can be launched on a connected device with:
`adb shell am start -n com.navin.kidsdrawing/.ContentLabActivity`

## Content Lab inspection — 16 checks

1. Content Lab/catalog shows **24 valid release lessons** and all four Set-E lessons are present.
2. **Face & Expressions** metadata, Growing/Young eligibility, difficulty 3, preview and thumbnail are readable and mature.
3. Face head/eye-brow/nose-mouth construction geometry and Help guides are large/clear; no Trace help appears.
4. Face `choose_expression` is genuinely open: no expected child strokes, expression may visibly diverge from preview.
5. **Simple Body & Pose** metadata, Growing/Young eligibility, difficulty 4, preview and thumbnail are credible for older children.
6. Body torso/pelvis, limbs, pose and contour construction geometry is readable; proportion/direction anchors help without Trace.
7. Body `make_pose_yours` is genuinely open and does not require copying the teacher pose/accessories.
8. **Create Your Character** metadata, Growing/Young eligibility, difficulty 4, preview and thumbnail communicate authored character design rather than one required character.
9. Character foundation/face/outfit construction guides are useful; teacher example is clearly a technique reference and no Trace appears.
10. Character `create_your_character` is genuinely open with no expected strokes and meaningful silhouette/outfit/expression/story variation.
11. **One-Point Room** metadata, Young-only eligibility, difficulty 5, preview and thumbnail read as a credible one-point-perspective room.
12. Room horizon/convergence/back-wall/furniture depth overlays are readable at phone size; no tiny ornamental target is required.
13. The vanishing point appears only as Help/guide/anchor information; it is **not** a required expected child stroke/precision tap.
14. Room `design_your_room` is genuinely open; purpose/decor/furniture/story may diverge from the teacher room; no Trace appears.
15. `journey.character_creator` visibly contains Face & Expressions → Simple Body & Pose → Create Your Character, with One-Point Room intentionally standalone.
16. Diagnostics match the locked final policy: **24 lessons / 0 errors / exactly six reviewed `NO_JOURNEY_MEMBERSHIP` warnings** only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room; no missing localization/assets or other warning appears.

**Content Lab result:** ___ / 16 PASS  
**Inspector/date:** ____________________  
**Notes / failures:**

## Gate

P5.6 may freeze `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode 25 only after:
- all 16 checks PASS;
- no lesson/content change is required;
- the genuine observation is recorded.

If any content-changing defect is found, keep versionCode 24 and fix/re-run the 24-lesson gate first. Do not cut v25 early.
