# P3.6 Physical QA — Vertical Slice 0.3

**Issue:** #48  
**Target:** `0.3.0-vertical-slice`  
**Status:** **PENDING PHYSICAL QA**  
**Evidence rule:** automated/JVM/CI success is never relabeled as physical PASS.

## Candidate record

- Device class/model: PENDING
- Android/API: PENDING
- RAM: PENDING
- Refresh rate: PENDING
- Candidate commit: PENDING
- CI run: PENDING
- Profile artifact ID: PENDING
- APK byte size: PENDING
- APK SHA-256: PENDING
- Tester/date: PENDING

## Required scenarios

| # | Scenario | Result | Observation / evidence |
|---|---|---|---|
| 1 | Fresh install/cleared data opens production onboarding | PENDING | |
| 2 | Complete onboarding → Studio Home | PENDING | |
| 3 | Home shows Cute Cat recommendation | PENDING | |
| 4 | Lesson preview → Start Drawing → guided workspace | PENDING | |
| 5 | Teacher demonstration blocks child input | PENDING | |
| 6 | Child turn accepts drawing and preserves operations | PENDING | |
| 7 | Help request/dismiss keeps artwork isolated | PENDING | |
| 8 | Complete Cute Cat → post-drawing choice | PENDING | |
| 9 | Enter real coloring workspace | PENDING | |
| 10 | Two+ colors persist below protected line art | PENDING | |
| 11 | Color eraser cannot damage line art | PENDING | |
| 12 | Coloring Undo/Redo cannot cross drawing boundary | PENDING | |
| 13 | Finish coloring → completion → Gallery entry | PENDING | |
| 14 | Gallery shows completed Cute Cat | PENDING | |
| 15 | Gallery detail reopens exact saved editable art | PENDING | |
| 16 | Onboarding draft survives background/process recreation | PENDING | |
| 17 | Lesson child-turn survives background → foreground | PENDING | |
| 18 | Lesson child-turn survives force-stop/relaunch | PENDING | |
| 19 | Active coloring survives background → foreground | PENDING | |
| 20 | Active coloring survives force-stop/relaunch; Continue Coloring appears | PENDING | |
| 21 | Gallery artwork survives force-stop/relaunch | PENDING | |
| 22 | Restarted Gallery reopen preserves drawing + color ops | PENDING | |
| 23 | Save & leave creates resumable work, not Gallery completion | PENDING | |
| 24 | Continue Drawing resumes persisted lesson truth | PENDING | |
| 25 | Continue Coloring outranks new recommendation while active | PENDING | |
| 26 | Confirmed Gallery delete cannot delete active working lesson doc | PENDING | |
| 27 | Missing/failed preview uses safe fallback; artwork remains authoritative | PENDING | |
| 28 | Age 4–5 layout spot check | PENDING | |
| 29 | Age 6–7 layout spot check | PENDING | |
| 30 | Age 8–9 layout spot check | PENDING | |
| 31 | Age 10–12 layout spot check | PENDING | |
| 32 | Increased system font scale: no critical-control clipping | PENDING | |
| 33 | Handedness/use-position check | PENDING | |
| 34 | Voice-off/reduced-motion graceful behavior where represented | PENDING | |
| 35 | Airplane mode full production journey | PENDING | |
| 36 | Zero teacher/guide operations in persisted child artwork | PENDING | |
| 37 | No drawing/coloring operation loss | PENDING | |
| 38 | Art Lab regression | PENDING | |
| 39 | Lesson Lab regression | PENDING | |
| 40 | Quality Lab regression | PENDING | |
| 41 | No crash/deadlock in full matrix | PENDING | |

## Release decision

Physical milestone gate: **PENDING**.

Do not close #48, close Epic #42, or declare `0.3.0-vertical-slice` released until this matrix and release evidence are complete.
