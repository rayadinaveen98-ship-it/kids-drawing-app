# P4.4 — Free Draw Studio V1 Core QA

**Issue:** #61  
**PR:** #69  
**Branch:** `phase4/p4-4-free-draw-studio`  
**Candidate identity:** `0.4.0-content-studio-p4.4-qa1` / versionCode 16  
**Status:** exact-head automated gate green; user-reported physical smoke is positive; exhaustive 18-scenario matrix remains partially unrecorded

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

## Automated gate — PASS

Exact-head Android CI is green on the frozen QA1 executable commit:
- exact candidate commit: `e24033862b4a653edbbc9f722b4c1560bf96a925`;
- Android CI run number: #377;
- GitHub Actions run ID: `34753682674`;
- committed JSON parsing: PASS;
- Drawing Engine AndroidX Ink boundary verification: PASS;
- `testDebugUnitTest`: PASS;
- `lintDebug`: PASS;
- debug APK assembly: PASS;
- instrumentation APK assembly: PASS;
- profile APK assembly: PASS;
- APK permission allowlist: PASS;
- P4.4 QA1 evidence packaging and artifact upload: PASS.

The first attempted frozen gate (#376) correctly failed two reset tests. Root cause was split clock ownership between Free Draw document mutations and reset candidate timestamps, causing the atomic store's stale-save protection to reject the reset blank. The production fix unified clock ownership and ensures reset timestamps cannot regress behind the active document. The tests were preserved and #377 passed.

P4.4-specific automated coverage includes:
- first-open blank working-document creation with null lesson provenance;
- durable recovery from the atomic document store;
- Pencil/Crayon/Marker persisted preset normalization;
- tool-state persistence;
- Undo/Redo persistence;
- confirmation-only Clear and undoable Clear history;
- post-Gallery working-canvas reset behavior;
- failure injection proving a failed post-Gallery blank save does not replace in-memory/durable working truth;
- Free Draw Gallery source/provenance validation;
- Free Draw Gallery reopen;
- Gallery-copy deletion isolation from the working canvas;
- age presentation policy keeps the same palette/capabilities while adapting density;
- bounded age-adaptive control-tray policy.

## Physical/product QA

Record PASS/FAIL with device model, Android/API version, exact commit, APK SHA-256 and observations. The user installed QA1 and reported the experience as **“looking good”** after being asked to exercise the Free Draw path. This is recorded as a positive physical smoke result only; individual matrix rows below are not silently converted to PASS when the conversation did not explicitly confirm them.

1. **Direct entry** — From Home, open Free Draw with no lesson prerequisite. A usable blank/recovered canvas must appear. — smoke-positive, row-level PASS not separately recorded.
2. **Canvas space** — On the target phone, especially Little Artist presentation, the drawing canvas must remain meaningfully visible. The lower tool tray must scroll internally when needed instead of pushing the canvas off-screen. — smoke-positive, row-level PASS not separately recorded.
3. **Pencil** — Draw several strokes at Small/Medium/Large. Stroke input must feel continuous; committed/reconciled strokes must not show an unacceptable visible snap. — smoke-positive, no defect reported.
4. **Crayon** — Draw several colors/sizes. It must be visibly distinguishable from Marker/Pencil after commit and after reopen. — smoke-positive, row-level PASS not separately recorded.
5. **Marker** — Draw several colors/sizes. It must remain bold and consistent after commit/reopen. — smoke-positive, row-level PASS not separately recorded.
6. **Eraser** — Erase representative portions of multiple strokes. Existing drawing history must remain editable. — not separately recorded.
7. **Palette** — Exercise all child-safe palette colors; changing color while Eraser is selected must return safely to the current drawing preset. — not separately recorded.
8. **Undo/Redo** — Exercise across stroke, erase and Clear operations. Redo must invalidate after a new branch edit as owned by the Drawing Engine. — not separately recorded.
9. **Clear safety** — Tap Clear and cancel: no mutation. Confirm Clear: canvas clears. Immediately Undo: pre-clear artwork returns. — not separately recorded.
10. **Save & leave** — Draw, change brush/color/size, save & leave, return from Home. Editable artwork and tool selection must recover. — smoke-positive, row-level PASS not separately recorded.
11. **Lifecycle recovery** — Background/foreground during active work; artwork remains. Then terminate/relaunch the app after a saved edit and verify recovery. — not separately recorded.
12. **Gallery finish** — Save a non-empty Free Draw artwork to Gallery. Completion screen opens; Gallery card opens the authoritative editable document rendered read-only. — smoke-positive, row-level PASS not separately recorded.
13. **Fresh working canvas after finish** — Return to Free Draw after successful Gallery completion. The working canvas should be blank while the Gallery copy remains intact. — smoke-positive, row-level PASS not separately recorded.
14. **Gallery delete isolation** — Delete the finished Gallery copy. Free Draw working state and unrelated lesson Gallery art must remain safe. — not separately recorded.
15. **Age adaptation** — Compare Little Artist and an older profile if practical: younger controls are larger/less dense, older controls more compact; tool/palette capability is unchanged. — not separately recorded.
16. **Offline** — Repeat direct entry, drawing, save & leave, Gallery save/reopen in Airplane Mode. — not separately recorded.
17. **Regression** — Start/resume a bundled guided lesson, exercise Help/teacher playback, and verify the existing Cute Cat coloring path still starts. Free Draw must not have altered lesson/session semantics. — not separately recorded in this QA1 pass; automated regression is green.
18. **No contamination/crash** — No teacher/guide operations appear in Free Draw history; no crash, ANR or deadlock observed during the matrix. — no crash/major defect reported in physical smoke; contamination is structurally/automatically protected.

## QA evidence fields

- Exact candidate commit: `e24033862b4a653edbbc9f722b4c1560bf96a925`
- Android CI run: #377 / `34753682674` — PASS
- Profile artifact ID: `10315674512`
- Profile artifact archive digest: `sha256:d838ea420ff3c5f1cecfbf0615070c122f8e2cf321ae7b8d252888efef49a6bd`
- Profile APK filename: `Kids_Drawing_0.4.0_Content_Studio_P4.4_QA1-profile.apk`
- Profile APK size: `16,162,897 bytes`
- Profile APK SHA-256: `cae971fd3e084ac45566ef4f5098741209b07575391c7f3cc989e30e41a4db43`
- Device / API: not explicitly reported in this QA1 conversation
- Physical result: POSITIVE SMOKE — user reported “looking good”; exhaustive row-level matrix is not claimed

## Exit rule / disposition

Do not convert unperformed physical checks into fabricated PASS evidence. The QA1 candidate is automated-green and has positive user physical smoke. The remaining explicit row-level physical checks stay visible and must be completed in the broader Phase 4 end-to-end physical regression no later than P4.7. This document intentionally distinguishes implementation/CI acceptance from exhaustive device-matrix evidence.
