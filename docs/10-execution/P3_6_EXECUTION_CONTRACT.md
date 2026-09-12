# P3.6 Execution Contract — End-to-End Vertical Slice + 0.3.0 Release

**Issue:** #48  
**Parent epic:** #42  
**Target:** `0.3.0-vertical-slice`  
**Version code:** 13

## Objective

Verify and package the first complete child-facing product journey as a physically tested Android milestone:

fresh install → onboarding → Studio Home recommendation → lesson preview → guided Cute Cat drawing → Help → drawing complete → coloring → completion → Gallery → restart/reopen.

P3.6 is a release/verification slice. It must not invent a second product architecture or casually reopen frozen Drawing/Lesson/Coloring engine contracts. Code changes are limited to release hardening and defects proven by QA.

## Source of truth

- GitHub repository is authoritative.
- P3.1–P3.5 merged product behavior is the implementation baseline.
- Engine truth remains below Compose presentation.
- `DrawingDocument` remains artwork truth; Gallery previews remain disposable derivatives.
- Automated/JVM/CI evidence must never be relabeled as physical-device evidence.

## Release candidate requirements

1. `versionName = 0.3.0-vertical-slice`.
2. `versionCode = 13`.
3. CI packages milestone artifacts using 0.3.0 Vertical Slice names, not stale 0.2 names.
4. Exact-head CI must pass unit tests, lint, debug APK, instrumentation APK compile, profile APK, permission allowlist, JSON validation and Ink-boundary verification.
5. The release-like profile APK is the physical QA candidate.
6. Candidate commit, CI run, artifact ID, byte size and SHA-256 are recorded before physical signoff.
7. Candidate code must not move after physical signoff except documentation-only evidence commits; any executable-code change invalidates the candidate and requires a new APK + relevant regression.

## Physical QA device

Primary device may reuse the proven Phase-2 Class-M device:

- Samsung SM-A546E;
- Android/API 36;
- ~7.4 GB RAM;
- 120 Hz.

If another device is used, record its exact model/API/RAM/refresh rate.

## Required physical matrix

### A. Fresh child journey

1. Fresh install/cleared app data opens production onboarding, not an engineering lab.
2. Complete all onboarding decisions and arrive at Studio Home.
3. Home presents a child-appropriate Cute Cat recommendation.
4. Lesson preview opens and Start Drawing enters the real guided workspace.
5. Teacher demonstration blocks child input while active.
6. Child turn accepts drawing and preserves child-authored operations.
7. Help can be requested and dismissed without contaminating artwork history.
8. Complete all Cute Cat drawing steps and reach post-drawing choice.
9. Choose coloring and enter the real coloring workspace.
10. Add at least two colors; color operations persist below protected line art.
11. Coloring eraser removes color without damaging line art.
12. Coloring Undo/Redo remains inside coloring history and cannot cross the protected drawing boundary.
13. Finish coloring reaches calm completion and creates a Gallery entry only after save succeeds.
14. Gallery shows the finished Cute Cat.
15. Gallery detail reopens the exact saved editable artwork.

### B. Restart/lifecycle durability

16. Onboarding draft survives background/process recreation before completion.
17. Lesson child-turn artwork/session survives background → foreground.
18. Lesson child-turn artwork/session survives force-stop/relaunch recovery.
19. Active coloring survives background → foreground.
20. Active coloring survives force-stop/relaunch and Home offers Continue Coloring.
21. Finished Gallery artwork survives force-stop/relaunch.
22. Reopened Gallery artwork after restart still contains drawing + color operations.

### C. Home/Gallery ownership rules

23. Save & leave from an unfinished lesson creates resumable work, not a completed Gallery item.
24. Continue Drawing resumes the real persisted lesson rather than starting fake Home progress.
25. Continue Coloring outranks starting a new recommendation while coloring is active.
26. Confirmed Gallery delete removes only the selected promoted artwork and never the active working lesson document.
27. Missing/failed preview falls back visually without deleting or reconstructing authoritative artwork from the preview.

### D. UI/accessibility policy spot checks

28. Age 4–5 policy: large controls/low density; no critical clipping in portrait.
29. Age 6–7 policy: controls remain comfortably tappable and unclipped.
30. Age 8–9 policy: workspace/Home remain readable and balanced.
31. Age 10–12 policy: denser layout remains readable and unclipped.
32. Increased system font scale does not hide the primary progression/exit controls.
33. Left/right-handed use is possible where applicable; no critical action is physically unreachable because of handedness.
34. Voice-off/reduced-motion style preferences degrade gracefully where those preferences are represented; no progression depends on audio or decorative animation.

### E. Offline/isolation/regression

35. Airplane mode: complete the production journey without network dependency.
36. Persisted child artwork contains zero teacher-generated/guide operations.
37. No drawing or coloring operation loss across the completed journey.
38. Art Lab still opens and accepts drawing.
39. Lesson Lab still opens for engineering regression.
40. Quality Lab still opens.
41. No crash/deadlock during the full physical matrix.

## Evidence policy

- A physical row is PASS only when the behavior is actually observed on-device.
- Existing engine automation may supplement a physical observation but cannot replace it.
- If a requirement genuinely cannot be exercised with available hardware/tooling, record a named exception with the exact automated evidence; do not silently convert it into PASS.
- Screenshots are preferred for lifecycle/recovery, isolation, clipping and Gallery persistence boundaries.

## Release completion

P3.6 is complete only when:

- all required executable code is frozen;
- exact-head CI is green;
- a release-like profile APK is physically verified;
- the physical matrix is complete with explicit exceptions only where unavoidable;
- implementation/QA report is committed;
- milestone evidence records version, candidate commit, CI run, artifact, APK byte size and SHA-256;
- engineering labs remain regression-green;
- Issue #48 and Epic #42 close only after the release gate passes;
- milestone tag target is the exact physically tested executable commit.
