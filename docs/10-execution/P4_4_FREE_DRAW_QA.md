# P4.4 — Free Draw Studio V1 Core QA

**Issue:** #61  
**PR:** #69  
**Branch:** `phase4/p4-4-free-draw-studio`  
**Candidate identity:** `0.4.0-content-studio-p4.4-qa1` / versionCode 16  
**Status:** automated/CI verification in progress; physical product QA not yet claimed

## Acceptance mapping

P4.4 exists to replace the Free Draw placeholder with a real offline creative studio without introducing a second drawing system or a Lesson Engine dependency.

Implemented boundaries:
- Home routes directly to a production Free Draw workspace;
- authoritative editable artwork remains a `DrawingDocument` with operation history;
- working identity is stable: `free-draw-working-v1`;
- working artwork uses the same atomic drawing-document store as the rest of the product;
- no lesson ID/revision is attached to Free Draw artwork;
- Pencil, Crayon and Marker are product-owned persisted brush presets, with Eraser remaining the existing structural erase tool;
- tool/color/size selection is persisted separately from artwork history;
- Undo/Redo/Clear are owned by the verified `DrawingDocumentEngine`;
- Clear requires explicit confirmation and remains undoable after confirmation;
- Save & leave persists the working artwork without creating a Gallery completion;
- Gallery completion uses `GalleryArtworkSource.FREE_DRAW` and promotes to a distinct Gallery document identity;
- successful Gallery promotion resets the Free Draw working document only after the fresh blank document is durably saved;
- deleting a Gallery copy cannot delete the protected Free Draw working document;
- Gallery browsing/reopen/delete no longer requires an active lesson runtime;
- age adaptation changes control density/presentation only, never document truth or available core capabilities;
- the control tray is age-bounded and internally scrollable so small screens retain a meaningful drawing canvas;
- no network/account/analytics permission was added.

## Automated gate

The exact-head CI for the final QA1 commit must pass all existing Android CI gates:
- committed JSON parsing;
- Drawing Engine AndroidX Ink boundary verification;
- `testDebugUnitTest`;
- `lintDebug`;
- debug APK assembly;
- instrumentation APK assembly;
- profile APK assembly;
- APK permission allowlist;
- P4.4 QA1 evidence packaging and artifact upload.

P4.4-specific automated coverage includes:
- first-open blank working-document creation with null lesson provenance;
- durable recovery from the atomic document store;
- Pencil/Crayon/Marker persisted preset normalization;
- tool-state persistence;
- Undo/Redo persistence;
- confirmation-only Clear and undoable Clear history;
- post-Gallery working-canvas reset behavior;
- Free Draw Gallery source/provenance validation;
- Free Draw Gallery reopen;
- Gallery-copy deletion isolation from the working canvas;
- age presentation policy keeps the same palette/capabilities while adapting density;
- bounded age-adaptive control-tray policy.

## Physical/product QA — required before PR #69 is treated as complete

Record PASS/FAIL with device model, Android/API version, exact commit, APK SHA-256 and observations.

1. **Direct entry** — From Home, open Free Draw with no lesson prerequisite. A usable blank/recovered canvas must appear.
2. **Canvas space** — On the target phone, especially Little Artist presentation, the drawing canvas must remain meaningfully visible. The lower tool tray must scroll internally when needed instead of pushing the canvas off-screen.
3. **Pencil** — Draw several strokes at Small/Medium/Large. Stroke input must feel continuous; committed/reconciled strokes must not show an unacceptable visible snap.
4. **Crayon** — Draw several colors/sizes. It must be visibly distinguishable from Marker/Pencil after commit and after reopen.
5. **Marker** — Draw several colors/sizes. It must remain bold and consistent after commit/reopen.
6. **Eraser** — Erase representative portions of multiple strokes. Existing drawing history must remain editable.
7. **Palette** — Exercise all child-safe palette colors; changing color while Eraser is selected must return safely to the current drawing preset.
8. **Undo/Redo** — Exercise across stroke, erase and Clear operations. Redo must invalidate after a new branch edit as owned by the Drawing Engine.
9. **Clear safety** — Tap Clear and cancel: no mutation. Confirm Clear: canvas clears. Immediately Undo: pre-clear artwork returns.
10. **Save & leave** — Draw, change brush/color/size, save & leave, return from Home. Editable artwork and tool selection must recover.
11. **Lifecycle recovery** — Background/foreground during active work; artwork remains. Then terminate/relaunch the app after a saved edit and verify recovery.
12. **Gallery finish** — Save a non-empty Free Draw artwork to Gallery. Completion screen opens; Gallery card opens the authoritative editable document rendered read-only.
13. **Fresh working canvas after finish** — Return to Free Draw after successful Gallery completion. The working canvas should be blank while the Gallery copy remains intact.
14. **Gallery delete isolation** — Delete the finished Gallery copy. Free Draw working state and unrelated lesson Gallery art must remain safe.
15. **Age adaptation** — Compare Little Artist and an older profile if practical: younger controls are larger/less dense, older controls more compact; tool/palette capability is unchanged.
16. **Offline** — Repeat direct entry, drawing, save & leave, Gallery save/reopen in Airplane Mode.
17. **Regression** — Start/resume a bundled guided lesson, exercise Help/teacher playback, and verify the existing Cute Cat coloring path still starts. Free Draw must not have altered lesson/session semantics.
18. **No contamination/crash** — No teacher/guide operations appear in Free Draw history; no crash, ANR or deadlock observed during the matrix.

## QA evidence fields

- Exact candidate commit: **TBD after final exact-head freeze**
- Android CI run: **TBD**
- Profile artifact ID: **TBD**
- Profile APK filename: `Kids_Drawing_0.4.0_Content_Studio_P4.4_QA1-profile.apk`
- Profile APK size: **TBD**
- Profile APK SHA-256: **TBD**
- Device / API: **TBD when physically tested**
- Physical result: **NOT YET CLAIMED**

## Exit rule

Do not mark #61 complete, mark PR #69 ready, or merge merely because the APK compiles. P4.4 closes only after the frozen QA candidate has exact-head green CI and the required physical/product matrix is recorded without overstating unperformed checks. Broader cross-content/device coverage can still be repeated in P4.7, but P4.4's own Free Draw acceptance must be demonstrated first.
