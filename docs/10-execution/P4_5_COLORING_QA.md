# P4.5 — Coloring V1 Expansion QA Record

**Status:** QA candidate preparation / physical acceptance pending  
**Issue:** #62  
**Draft PR:** #70  
**Branch:** `phase4/p4-5-coloring-expansion`  
**Candidate identity:** `0.4.0-content-studio-p4.5-qa1` / versionCode 17

This record separates automated evidence from physical/product evidence. A green CI build does not count as device PASS, and an informal smoke result does not imply every row below was exercised.

## 1. Scope under test

P4.5 expands the proven Phase-3 coloring path with:
- validated authored prepared regions;
- reversible prepared-region Fill operations in DrawingDocument schema 3;
- authored Color With Me progression;
- safe Color Myself Fill + freehand Brush/Eraser coexistence;
- protected line-art isolation;
- schema-1/2 document compatibility;
- lifecycle/recovery/Gallery preservation;
- production `little-fish` r1 content proving prepared regions;
- Cute Cat r1 legacy freehand regression.

## 2. Automated implementation evidence

Pre-candidate implementation baseline:
- commit: `a22cac0714f757857197ff00f67412a3ca9fe4b5`;
- Android CI #412 / run `34756288249`: **GREEN**;
- committed JSON parse: PASS;
- Drawing Engine Ink-boundary gate: PASS;
- JVM unit tests: PASS;
- lintDebug: PASS;
- debug APK compile: PASS;
- instrumentation APK compile: PASS;
- profile APK compile: PASS;
- permission allowlist: PASS.

Automated coverage includes:
- prepared-region structural/reference validation;
- invalid/degenerate/out-of-bounds/self-intersecting region rejection;
- legacy empty-region freehand compatibility;
- schema-3 fill history, recolor and coloring Undo/Redo boundary;
- schema-3 binary roundtrip plus schema-1/2 readability;
- prepared-region hit testing including boundary taps;
- multi-region guided progression requiring all authored regions;
- content-driven coloring presentation semantics;
- production catalog loading of Little Fish prepared coloring;
- Cute Cat legacy freehand and P4.3 content regressions.

## 3. Frozen QA1 executable evidence

Fill only after the final QA1 exact-head CI succeeds.

- executable commit: **PENDING**
- exact-head Android CI run: **PENDING**
- profile artifact ID: **PENDING**
- profile APK filename: `Kids_Drawing_0.4.0_Content_Studio_P4.5_QA1-profile.apk`
- profile APK size: **PENDING**
- profile APK SHA-256: **PENDING**
- debug artifact/SHA: **PENDING**

## 4. Physical/product QA matrix

Reference device/model/API: **PENDING USER TEST**

| # | Scenario | Expected result | Result |
|---|---|---|---|
| 1 | Fresh/open Studio → Little Fish → Draw With Me → finish drawing → Color With Me | Coloring opens with authored Little Fish guidance; no Cute Cat-specific copy | PENDING |
| 2 | Guided step 1, select Fill, tap outside body region | No artwork mutation; child gets safe retry feedback | PENDING |
| 3 | Guided step 1, Fill inside body | Body receives selected color; line art remains visible and untouched; guidance advances to step 2 | PENDING |
| 4 | Guided step 2, Fill tail only | Tail fills but guided step remains incomplete because fin is still required | PENDING |
| 5 | Guided step 2, Fill fin after tail | Step completes only after both tail + fin are filled | PENDING |
| 6 | Recolor an already filled region | New color replaces visible region color while earlier fill remains reversible history | PENDING |
| 7 | Undo after recolor, then Redo | Undo reveals previous region color; Redo restores newer color; line art is unchanged | PENDING |
| 8 | Switch Fill → Brush and draw color freely | Brush works through existing AndroidX Ink coloring path; prepared fills remain intact | PENDING |
| 9 | Switch to Eraser and erase coloring | Coloring pixels can be erased; protected child line art cannot be damaged | PENDING |
| 10 | Save & leave during partially completed guided coloring, then resume | Fill/freehand operations and correct guided step recover | PENDING |
| 11 | Background/process recreation during prepared coloring | Artwork recovers first; session resumes without lost fill/history or incompatible tool state | PENDING |
| 12 | Finish Little Fish coloring → Gallery | Gallery preview shows prepared fills beneath the child line art | PENDING |
| 13 | Reopen completed Little Fish artwork from Gallery | Saved artwork still shows all fills/freehand/line art correctly | PENDING |
| 14 | Little Fish → Color Myself | Fill can target any authored region in any order; Brush/Eraser remain available | PENDING |
| 15 | Cute Cat completion → Color With Me / Color Myself | Legacy freehand coloring still works; no deceptive Fill control appears | PENDING |
| 16 | Coloring Undo at boundary where last coloring op is removed | Undo stops before protected drawing/line-art history | PENDING |
| 17 | Airplane Mode / no network | Drawing, prepared Fill, coloring recovery and Gallery remain functional | PENDING |
| 18 | Small-screen usability / age-adaptive controls | Canvas remains usable; palette/tools/Finish remain reachable without obstructive layout | PENDING |

## 5. Release/merge gate

P4.5 is accepted only when:
1. candidate version is monotonic and frozen;
2. exact candidate head passes full Android CI;
3. profile APK artifact/size/SHA are recorded here;
4. the relevant physical matrix is executed and recorded honestly;
5. no prepared Fill can alter protected line-art history;
6. Cute Cat legacy coloring remains regression-green;
7. PR #70 is marked ready only after acceptance;
8. merged-main CI is green before issue #62 is closed.

Until those gates pass, PR #70 remains draft and P4.5 remains ACTIVE.
