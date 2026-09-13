# P3.6 Physical QA — Vertical Slice 0.3

**Issue:** #48  
**Target:** `0.3.0-vertical-slice`  
**Status:** **PHYSICAL QA COMPLETE — PASS**  
**Evidence rule:** automated/JVM/CI success is never relabeled as physical PASS.

## Candidate record

- Physical device: tester-owned Android phone; exact model/RAM/refresh-rate were not re-captured during P3.6 and are intentionally not inferred from earlier milestones.
- Executable candidate commit: `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`
- Version: `0.3.0-vertical-slice` / versionCode `13`
- Candidate CI: Android CI #307 / run `34706767213` — **GREEN**
- Profile artifact ID: `10301836886`
- Profile artifact name: `kids-drawing-0.3.0-vertical-slice-profile`
- APK filename: `Kids_Drawing_0.3.0_Vertical_Slice-profile.apk`
- APK byte size: `16080880`
- APK SHA-256: `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`
- Physical validation completed: 2026-09-13

### Superseded candidates

1. `670b0f81d716ca4c150d426f09ec5171f4cfbb2a` exposed a release-blocking guided-workspace layout defect: the primary `Done` action used a fill-max-size child, expanded vertically, and collapsed the weighted drawing canvas during the child turn.
2. `3ab93d7a57022e543009753d033c7f538fd12441` fixed guided drawing, but physical testing exposed release-blocking coloring defects: the coloring primary action could collapse the canvas, an ACTIVE coloring session was covered by a duplicate Gallery completion overlay, and coloring palette/tool settings leaked into the lesson drawing pencil through a shared tool engine.

The final executable candidate keeps the authoritative editable `DrawingDocument` shared while giving coloring independent tool state, preserves the canvas with compact controls, uses one completion path, and provides semantic Color With Me guidance without inventing unauthored fill regions.

## Required scenarios

| # | Scenario | Result | Observation / evidence |
|---|---|---|---|
| 1 | Fresh install/cleared data opens production onboarding | PASS | Cleared-data launch opened production onboarding. |
| 2 | Complete onboarding → Studio Home | PASS | Completed onboarding reached Studio Home. |
| 3 | Home shows Cute Cat recommendation | PASS | Cute Cat recommendation present. |
| 4 | Lesson preview → Start Drawing → guided workspace | PASS | Preview and real guided workspace opened correctly. |
| 5 | Teacher demonstration blocks child input | PASS | Attempted input during teacher demo created no child stroke. |
| 6 | Child turn accepts drawing and preserves operations | PASS | Child turn accepted drawing; strokes persisted through subsequent steps/recovery checks. |
| 7 | Help request/dismiss keeps artwork isolated | PASS | Existing child art stayed intact; temporary help marks did not persist into child history. |
| 8 | Complete Cute Cat → post-drawing choice | PASS | Full drawing completion reached Color With Me / Color Myself / done choice boundary. |
| 9 | Enter real coloring workspace | PASS | Coloring workspace opened with visible canvas and compact usable controls. |
| 10 | Two+ colors persist below protected line art | PASS | Multiple colors were applied and preserved while line art remained above/protected. |
| 11 | Color eraser cannot damage line art | PASS | Eraser removed color only; black drawing remained intact. |
| 12 | Coloring Undo/Redo cannot cross drawing boundary | PASS | Repeated Undo/Redo affected only coloring history. |
| 13 | Finish coloring → completion → Gallery entry | PASS | Finish coloring saved and reached completion/Gallery flow. |
| 14 | Gallery shows completed Cute Cat | PASS | Completed artwork appeared in Gallery. |
| 15 | Gallery detail reopens exact saved editable art | PASS | Same drawing and colors reopened with no flattening/loss. |
| 16 | Onboarding draft survives background/process recreation | PASS | Partial onboarding resumed after background and force-stop/relaunch. |
| 17 | Lesson child-turn survives background → foreground | PASS | Unfinished child-turn state and strokes survived. |
| 18 | Lesson child-turn survives force-stop/relaunch | PASS | Continue Drawing restored same lesson position and strokes. |
| 19 | Active coloring survives background → foreground | PASS | Active coloring state and colors survived. |
| 20 | Active coloring survives force-stop/relaunch; Continue Coloring appears | PASS | Continue Coloring restored the same unfinished coloring. |
| 21 | Gallery artwork survives force-stop/relaunch | PASS | Completed Gallery artwork remained after force-stop. |
| 22 | Restarted Gallery reopen preserves drawing + color ops | PASS | Reopened artwork retained drawing and coloring operations exactly. |
| 23 | Save & leave creates resumable work, not Gallery completion | PASS | Unfinished lesson did not create a completed Gallery item. |
| 24 | Continue Drawing resumes persisted lesson truth | PASS | Continue Drawing resumed the same unfinished lesson/artwork. |
| 25 | Continue Coloring outranks new recommendation while active | PASS | Active Continue Coloring surfaced ahead of a new recommendation. |
| 26 | Confirmed Gallery delete cannot delete active working lesson doc | PASS | Deleting a completed Gallery item left active coloring untouched and recoverable. |
| 27 | Missing/failed preview uses safe fallback; artwork remains authoritative | PASS | Exact-source debug build was used only to remove the derived preview PNG via `run-as`; Gallery showed fallback while the authoritative drawing+colors still reopened correctly. |
| 28 | Age 4–5 layout spot check | PASS | Controls usable; no critical clipping/overlap. |
| 29 | Age 6–7 layout spot check | PASS | Controls usable; no critical clipping/overlap. |
| 30 | Age 8–9 layout spot check | PASS | Controls usable; no critical clipping/overlap. |
| 31 | Age 10–12 layout spot check | PASS | Controls usable; no critical clipping/overlap. |
| 32 | Increased system font scale: no critical-control clipping | PASS | Home/preview/drawing/coloring remained usable with increased font size. |
| 33 | Handedness/use-position check | PASS | Left- and right-handed choices remained usable without hidden/awkward critical controls. |
| 34 | Voice-off/reduced-motion graceful behavior where represented | PASS | On-screen guidance remained sufficient with narration off; reduced animations did not block progress. |
| 35 | Airplane mode full production journey | PASS | Full onboarding-to-Gallery journey completed offline. |
| 36 | Zero teacher/guide operations in persisted child artwork | PASS | Saved artwork contained no teacher/help overlay contamination. |
| 37 | No drawing/coloring operation loss | PASS | Drawing and coloring survived completion, restart, recovery, and Gallery reopen checks. |
| 38 | Art Lab regression | PASS | Drawing/tools/history/save-reload/teacher playback smoke-tested physically. |
| 39 | Lesson Lab regression | PASS | Start/help/pause-resume/save-recover smoke-tested; overlay isolation remained PASS. |
| 40 | Quality Lab regression | PASS | Quality harness opened and short benchmark/workload controls remained responsive; no repeat 30-minute soak required for this regression gate. |
| 41 | No crash/deadlock in full matrix | PASS | No crash, freeze, dead screen, or deadlock observed across the completed physical matrix. |

## Evidence notes

- Scenario #27 necessarily used the debug APK produced from the **same exact executable source commit** because Android `run-as` cannot access a non-debuggable profile app's private preview directory. Only the non-authoritative derived PNG was removed; the authoritative document remained untouched.
- The release-like profile APK remained the primary physical candidate for normal product-path testing.
- Candidate CI #307 already covered unit tests, lint, debug/profile build, instrumentation APK compilation, permission allowlist, package evidence, and artifact upload.
- Exact final PR-head CI must still be green after these documentation-only QA/report commits before merge.

## Release decision

Physical milestone gate: **PASS**.

The executable candidate `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd` is physically accepted for the `0.3.0-vertical-slice` milestone. Remaining release administration: commit final QA/report docs, require exact-final-head CI green, merge PR #56, verify merged-main CI, close #48 / Phase 3 epic when appropriate, and create/verify `v0.3.0-vertical-slice` if repository tooling permits tag creation.
