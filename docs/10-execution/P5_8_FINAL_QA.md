# P5.8 Final 0.5 Physical Acceptance — `0.5.0-curriculum-expansion`

**Issue:** #88  
**PR:** #89  
**Branch:** `phase5/p5-8-final-release`  
**Final versionName:** `0.5.0-curriculum-expansion`  
**Reserved versionCode:** **27**  
**Status:** **PENDING FINAL FREEZE / PHYSICAL ACCEPTANCE**

## Immutable binary evidence

Populate only after the exact final-release CI succeeds.

- release commit: **PENDING**
- Android CI run: **PENDING**
- content-quality artifact: **PENDING**
- debug artifact: **PENDING**
- debug APK size: **PENDING**
- debug SHA256: **PENDING**
- profile artifact: **PENDING**
- profile APK size: **PENDING**
- profile SHA256: **PENDING**
- permission allowlist: **PENDING**
- content quality: expected **24 lessons / 0 errors / 6 reviewed warnings**

## Tester metadata

- acceptance date: **PENDING**
- tester-reported device model: **PENDING**
- tester-reported Android/API: **PENDING**

Do not infer missing device/API information. If the tester does not provide it, record `Not provided by tester`.

## Final physical matrix — 50 checks

### A. Install / startup / profile — 1–5

- [ ] 1. Exact final profile APK installs successfully.
- [ ] 2. App launches without crash or blocking error.
- [ ] 3. Existing completed profile opens Home correctly.
- [ ] 4. Fresh app-data/onboarding path can produce a valid child profile.
- [ ] 5. No unexpected Android runtime permission prompt appears during normal core use.

### B. Four age bands / Home / discovery — 6–13

- [ ] 6. Little Artist Home renders a sensible primary suggestion and usable controls.
- [ ] 7. Creative Explorer Home renders a sensible primary suggestion and usable controls.
- [ ] 8. Growing Artist Home renders a sensible primary suggestion and usable controls.
- [ ] 9. Young Artist Home renders a sensible primary suggestion and usable controls.
- [ ] 10. Reopening Home with unchanged local state keeps the same deterministic fresh suggestion.
- [ ] 11. Adaptive reason copy is friendly/explainable and contains no score/rank/ability language.
- [ ] 12. Browse/category/journey discovery remains visible rather than being hidden by prerequisite gating.
- [ ] 13. All 24 production lessons remain discoverable somewhere in the product.

### C. Journey / prerequisite / adaptive progression — 14–20

- [ ] 14. A lesson with an unmet prerequisite does not become the adaptive fresh primary.
- [ ] 15. Completing a prerequisite can make the next authored journey lesson eligible coherently.
- [ ] 16. Character Creator progression remains Face → Body/Pose → Create Your Character.
- [ ] 17. Animal Artist progression remains coherent through its authored members.
- [ ] 18. Space Artist progression remains coherent through its authored members.
- [ ] 19. Changing an explicit child interest can transparently influence a fresh suggestion.
- [ ] 20. Completed familiar content does not dominate while eligible fresh work remains.

### D. Resume precedence / lifecycle — 21–27

- [ ] 21. Active drawing resume outranks a fresh adaptive suggestion.
- [ ] 22. Active coloring resume outranks drawing resume and fresh suggestion.
- [ ] 23. Save & Leave preserves the drawing and returns to Home safely.
- [ ] 24. Reopening the saved lesson restores the correct lesson/step/document state.
- [ ] 25. Background/foreground lifecycle does not lose committed child artwork.
- [ ] 26. Relaunch after process death/recreation recovers safely where recovery is expected.
- [ ] 27. Recovery does not create duplicate visible completion/progression behavior.

### E. Teaching modes / Companion / Help — 28–37

- [ ] 28. Representative Draw With Me lesson launches and teacher demonstration works.
- [ ] 29. Representative Watch Then Draw lesson launches, overview works, and child handoff works.
- [ ] 30. Representative authored Trace & Learn lesson exposes Trace correctly.
- [ ] 31. A lesson/step without authored Trace never invents a Trace option.
- [ ] 32. An open-authorship final turn remains free/open rather than becoming a forced copy/Trace task.
- [ ] 33. Help does not open or escalate automatically without a child tap.
- [ ] 34. Child Help tap yields only an authored Help level or authored Replay.
- [ ] 35. Repeated adaptive Help does not get stuck in an endless Replay loop.
- [ ] 36. Less Help / Hide Help controls return toward independent work correctly.
- [ ] 37. Companion language remains supportive/non-scoring and follows Lesson Engine state.

### F. Artwork isolation / Gallery / Coloring / Free Draw — 38–44

- [ ] 38. Teacher demonstration overlay never becomes part of saved child artwork.
- [ ] 39. Help/reference/Trace guide overlays never become part of saved child artwork.
- [ ] 40. Successful guided drawing completion can be saved to Gallery.
- [ ] 41. Gallery item reopens/displays with correct provenance.
- [ ] 42. Coloring can start from supported lesson completion and remain distinct from drawing ownership.
- [ ] 43. Saved coloring can resume safely when resumable work exists.
- [ ] 44. Free Draw remains lesson-independent and saves with explicit Free Draw provenance.

### G. Offline / privacy / stability — 45–50

- [ ] 45. Enable Airplane Mode and cold-launch the app successfully.
- [ ] 46. Home/catalog/lesson launch still work in Airplane Mode.
- [ ] 47. Drawing + Help + Save/Recovery work in Airplane Mode.
- [ ] 48. Gallery/Coloring/Free Draw core flows work without a network requirement.
- [ ] 49. No visible child ability score, grade, rank, XP, punitive streak or talent judgment appears.
- [ ] 50. Final smoke: no crash, corrupted artwork, blocking error or unexpected network/permission dependency during the matrix.

## Acceptance result

- passed: **PENDING / 50**
- failed: **PENDING / 50**
- overall: **PENDING**
- reported defects: **PENDING**

## Integrity rule

Physical acceptance is valid only for the exact final profile APK whose commit, artifact ID, size and SHA256 are recorded above. Any binary/content-changing defect fix invalidates this candidate and requires versionCode >27 plus new automated and physical evidence.
