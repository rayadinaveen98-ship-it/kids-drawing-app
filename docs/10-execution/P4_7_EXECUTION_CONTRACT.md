# P4.7 — End-to-end QA + `0.4.0-content-studio` Release — Execution Contract

**Issue:** #64  
**Parent epic:** #57  
**Branch:** `phase4/p4-7-final-release`  
**Verified base:** P4.6 squash merge `b811a149e44eadfee815f1f9896f2871e9f7e25d` with merged-main Android CI #433 / run `34800596778` GREEN  
**Target versionName:** `0.4.0-content-studio`  
**Initial final-candidate versionCode:** 19

## 1. Objective

P4.7 is the final Phase-4 release gate. It does **not** exist to add another feature slice. It exists to prove that the complete offline multi-lesson drawing studio works as one coherent Android product and to deliver a reproducible final APK.

Phase 4 closes only after:
- exact final candidate CI is green;
- the required physical/product matrix is genuinely completed;
- all still-deferred P4.3/P4.4/P4.5/P4.6 rows are resolved or explicitly documented as real hardware/tooling exceptions;
- any release-blocking defect found in the matrix is fixed and re-tested;
- final release evidence is committed;
- the release PR is clean and merged;
- merged-main CI is green;
- final installable profile APK evidence is preserved and delivered.

## 2. Scope discipline

P4.7 permits only:
1. final version/workflow/release-evidence changes;
2. automated regression hardening needed to prove an existing contract;
3. narrowly scoped fixes for defects discovered by the final matrix;
4. documentation/evidence updates.

P4.7 does **not** permit opportunistic feature expansion, visual redesign, curriculum expansion, engine rewrites, new accounts/network dependencies, analytics, ads or permissions.

If a new idea is not a release blocker for the frozen Phase-4 contract, defer it post-Phase-4.

## 3. Frozen product architecture

The following remain non-negotiable:
- `DrawingDocument` + `DrawingDocumentEngine` own editable artwork/history truth;
- UI never owns arbitrary drawing/lesson/session truth;
- lessons are structured/versioned content interpreted generically, with no lesson-ID-specific tutorial screen branching;
- AndroidX Ink remains behind owned drawing infrastructure boundaries;
- teacher/trace/help/reference overlays never enter child artwork/history/persistence;
- coloring/fill remains structurally below protected child line art;
- Free Draw remains lesson-independent and uses explicit `FREE_DRAW` Gallery provenance;
- Gallery copies remain independent from protected working documents;
- persistence stores editable operations, not screenshots;
- core drawing/teaching/catalog/Gallery flows remain offline-first;
- no mandatory child account, behavioral analytics, ads, sensitive permissions or paid critical-path dependency.

## 4. Final candidate identity

Initial final candidate:
- versionName: `0.4.0-content-studio`;
- versionCode: 19;
- profile/debug artifact names must use `0.4.0-content-studio` / `Final` naming, not P4.6 QA labels.

The binary tested physically must be the binary intended for final delivery. Do not physically accept an RC and silently ship a different product binary afterward.

If a **materially different APK** is produced after the versionCode-19 candidate has been distributed, increment to versionCode 20 (then 21, etc.). Never reuse a distributed versionCode for a changed binary.

Documentation-only acceptance commits may occur after the tested executable commit, but evidence must separately record:
- exact tested executable commit;
- exact accepted documentation head;
- final squash/merge commit;
- CI runs for each required gate.

## 5. Required automated release gate

Exact-head CI on the final candidate must pass:
- committed JSON parsing;
- AndroidX Ink boundary verification;
- all JVM/unit tests;
- lint;
- debug APK assembly;
- instrumentation APK assembly;
- profile APK assembly;
- milestone permission allowlist;
- final APK evidence packaging/upload.

Automated coverage must continue proving at minimum:
- all nine release lesson packages load without diagnostics;
- all declared assets/localization keys/references resolve;
- age/mode/journey/difficulty representative coverage remains intact;
- Trace content cannot omit required trace support;
- teacher/reference operations remain outside child history;
- Free Draw persistence/provenance/reset/delete-isolation contracts;
- prepared-region geometry validation;
- schema 3 Fill history/recolor/Undo/Redo and schema 1/2 backward readability;
- prepared Fill cannot mutate protected line art;
- guided multi-region coloring progression;
- legacy Cute Cat freehand coloring remains valid;
- Gallery rendering/reopen supports fills and Free Draw source semantics.

## 6. Physical/product release matrix

The authoritative matrix is `docs/10-execution/P4_7_FINAL_QA.md`.

It must cover the following groups:

### A. Fresh install + profile + Studio
- clean app-data start;
- onboarding/profile creation;
- nine-lesson Studio discovery;
- categories and Art Journeys;
- deterministic age/interests recommendations;
- all four age bands: 4–5, 6–7, 8–9, 10–12.

### B. Representative teaching modes/content
- Smiling Sun Trace & Learn + replay/help;
- Friendly Owl Help Ladder levels 1–5;
- Simple Rocket Watch Then Draw overview/replay;
- Easy Flower grouped playback at representative pace extremes;
- Hot Air Balloon guided coloring;
- Fox Portrait older-child proportion/detail flow;
- Design Your Spaceship open creative variation;
- Cute Cat Phase-3 all-mode/legacy regression.

### C. Free Draw
Absorb the still-unrecorded P4.4 rows:
- direct entry/canvas space;
- Pencil/Crayon/Marker differentiation;
- palette/size/Eraser;
- Undo/Redo branch invalidation;
- confirmation-only Clear + immediate Undo;
- Save & leave recovery;
- background/foreground and force-stop/relaunch recovery;
- Gallery finish/reopen;
- fresh working canvas after finish;
- Gallery-delete isolation;
- age-adaptive density without capability loss;
- offline journey;
- no contamination/crash/deadlock.

### D. Expanded coloring
Absorb P4.5/P4.6 deferred rows:
- Little Fish guided Fill + Color Myself;
- Hot Air Balloon multi-region guided fill;
- recolor + Undo/Redo;
- exact coloring Undo boundary cannot cross protected drawing history;
- save/leave/reopen;
- background/process recreation while fills exist;
- Gallery render/reopen;
- Cute Cat freehand path has no deceptive Fill;
- small-screen reachability;
- offline prepared-coloring journey.

### E. Lifecycle/recovery
- guided drawing background/foreground;
- guided drawing force-stop/relaunch recovery;
- coloring process recreation;
- Free Draw process recreation;
- no operation loss or duplicate/stale callbacks;
- missing/incompatible lesson fallback remains safe where practicable without corrupting artwork.

### F. Gallery safety
- lesson drawing/coloring completion reopen;
- Free Draw completion reopen;
- delete lesson Gallery entry safely;
- delete Free Draw Gallery copy without deleting protected working canvas;
- unrelated artwork remains unaffected;
- fallback behavior does not strand editable artwork.

### G. Offline
With Airplane Mode enabled, prove:
- Studio/catalog/recommendations available;
- start/complete a guided lesson;
- prepared coloring works;
- Free Draw works;
- Gallery save/reopen works;
- no hidden network/account dependency blocks core use.

### H. Age/accessibility/settings
Spot-check:
- age 4–5 presentation;
- age 6–7 presentation;
- age 8–9 presentation;
- age 10–12 presentation;
- handedness behavior if exposed in the product build;
- larger system font scale;
- voice-off behavior if exposed;
- reduced-motion behavior where represented;
- controls remain reachable and canvas remains usable.

### I. Integrity/stability
- no teacher/guide/reference ink enters child history;
- no child drawing/color/fill operation loss after tested lifecycle transitions;
- no cross-lesson state leak;
- no line-art damage from coloring/fill;
- no crash, ANR, deadlock or unrecoverable blank state through the final matrix.

## 7. Deferred evidence absorbed by P4.7

P4.7 is explicitly responsible for resolving:
- P4.3 issue #60 broader physical/content matrix, including Set-A preview identity, Trace, Help Ladder, Watch Then Draw, grouped pace playback, cumulative teacher construction and Gallery regression;
- P4.4 row-level Free Draw matrix that previously had only positive smoke evidence;
- P4.5 process-death prepared-coloring sequence, exact Undo boundary, airplane mode and small-screen coverage;
- P4.6 Set-A focused regression, airplane mode, process recreation and small-screen/age-adaptive coverage.

Do not close issue #60 until the relevant final P4.7 rows genuinely pass.

## 8. Release evidence

Before merge/closure, record in `P4_7_FINAL_QA.md` and `P4_7_RELEASE_REPORT.md`:
- final versionName/versionCode;
- exact physical-tested executable commit;
- exact accepted documentation head;
- final merge commit;
- exact-head and merged-main CI run numbers/IDs;
- debug/profile artifact IDs/names;
- profile APK byte size;
- profile APK SHA-256;
- device/model/API actually reported for physical QA (never infer if absent);
- physical QA matrix with PASS/FAIL/NOT RUN/real exception;
- representative content coverage matrix;
- defects found/fixed/retested;
- implementation/release summary;
- actual hardware/tooling exceptions only.

## 9. Tag rule

Create/tag `v0.4.0-content-studio` **only if** an available GitHub tool safely supports creating the repository tag after the final merge. If not, do not claim a tag exists. Tag absence does not invalidate the release when all other evidence is complete.

## 10. Exit sequence

1. Create P4.7 branch from verified P4.6 merge.
2. Commit this contract + final QA matrix.
3. Set final candidate identity and CI artifact names.
4. Require exact-head automated gate green.
5. Download/profile-hash exact final candidate independently.
6. Run final physical matrix on that exact APK.
7. Fix/rebuild with monotonic versionCode if any release blocker exists.
8. Commit physical evidence + release report.
9. Require exact-head CI green on acceptance documentation head.
10. Close P4.3 issue #60 only if its deferred rows are now satisfied.
11. Mark final release PR ready and squash-merge.
12. Require merged-main CI green.
13. Close P4.7 issue #64 and Phase-4 epic #57 only after evidence supports both.
14. Deliver the exact final profile APK to the user.
15. Create `v0.4.0-content-studio` tag only if tooling safely supports it and verify it before claiming it.
