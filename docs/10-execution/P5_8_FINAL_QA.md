# P5.8 Final QA — Cross-age Curriculum + `0.5.0-curriculum-expansion`

**Issue:** #88  
**Parent epic:** #73  
**Branch:** `phase5/p5-8-cross-age-release`  
**Verified base:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Target:** `0.5.0-curriculum-expansion`  
**Final versionCode:** **NOT YET RESERVED**  
**Status:** **PRE-FREEZE — AUTOMATED INTEGRATED GATE IN PROGRESS**

This document is the authoritative P5.8 release matrix. Do not mark rows PASS from inference or prior-phase acceptance alone. Automated evidence may satisfy rows explicitly identified as automated; physical rows require testing of the exact final profile APK.

## A. Automated release gate

| ID | Check | Evidence | Status |
|---|---|---|---|
| A01 | Exactly 24 production lessons load through release catalog | P5.8 integrated gate + content report | PENDING |
| A02 | Content quality = 24 lessons / 0 errors / exactly 6 reviewed warnings | content report + integrated gate | PENDING |
| A03 | Reviewed warning IDs exactly match frozen six | integrated gate | PENDING |
| A04 | All four age bands have release content | integrated gate | PENDING |
| A05 | Lesson assets/localization/references resolve | existing content validators | PENDING |
| A06 | Journey references and prerequisites are structurally valid | integrated/existing tests | PENDING |
| A07 | Draw With Me generic execution remains green | existing execution suite | PENDING |
| A08 | Watch Then Draw generic execution remains green | existing execution suite | PENDING |
| A09 | Authored Trace executes only where authored | existing + integrated gate | PENDING |
| A10 | Help Ladder / Replay remains authored and bounded | existing + adaptive tests | PENDING |
| A11 | No automatic adaptive Help escalation | adaptive tests | PENDING |
| A12 | Adaptive Help never invents Trace | adaptive tests | PENDING |
| A13 | Companion remains read-only relative to session/artwork truth | companion tests | PENDING |
| A14 | Recommendations deterministic for all four age bands | P5.8 integrated gate | PENDING |
| A15 | Prerequisites remain authoritative for fresh-primary selection | integrated/adaptive tests | PENDING |
| A16 | Journey continuation is deterministic | integrated/adaptive tests | PENDING |
| A17 | Explicit interests remain influential | integrated/adaptive tests | PENDING |
| A18 | New/underexposed skills can influence ordering without ability labels | integrated/adaptive tests | PENDING |
| A19 | Completed/recent lessons do not dominate eligible fresh content | adaptive tests | PENDING |
| A20 | Recommendation copy is non-judgmental across all age bands | integrated/adaptive tests | PENDING |
| A21 | Resume precedence = coloring > drawing > fresh | primary-selection tests | PENDING |
| A22 | Missing/corrupt/future adaptive state falls back safely | adaptive store/repository tests | PENDING |
| A23 | Adaptive state remains bounded and privacy-safe | adaptive tests + structural check | PENDING |
| A24 | Completion/help event delivery remains idempotent | adaptive reducer tests | PENDING |
| A25 | Save & Leave / lesson recovery remains green | lifecycle/recovery tests | PENDING |
| A26 | Gallery save/reopen/delete isolation remains green | Gallery tests | PENDING |
| A27 | Prepared + legacy Coloring regression remains green | Coloring tests | PENDING |
| A28 | Free Draw persistence/history/provenance remains green | Free Draw tests | PENDING |
| A29 | Drawing Engine teacher/reference isolation remains green | engine/lesson tests | PENDING |
| A30 | Android permission allowlist unchanged | CI APK inspection | PENDING |
| A31 | Debug + instrumentation + profile APKs assemble | CI | PENDING |
| A32 | lint + all JVM tests pass | CI | PENDING |

## B. Final executable identity and immutable evidence

Fill only after the pre-freeze automated gate is GREEN.

- final versionName: **PENDING**
- final versionCode: **PENDING (>26)**
- exact executable commit: **PENDING**
- final-candidate Android CI run: **PENDING**
- debug artifact ID/name: **PENDING**
- debug APK bytes: **PENDING**
- debug SHA256: **PENDING**
- profile artifact ID/name: **PENDING**
- profile APK bytes: **PENDING**
- profile SHA256: **PENDING**
- permission allowlist: **PENDING**
- content quality: **PENDING**

## C. Focused physical acceptance — exact final profile APK only

### C1. Install / onboarding / Studio

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P01 | Install exact final profile APK cleanly and launch | NOT RUN | |
| P02 | Fresh app-data onboarding completes without crash/blocker | NOT RUN | |
| P03 | Create 4–5 profile; Home/Studio usable and age-appropriate | NOT RUN | |
| P04 | Change/create 6–7 profile context; Studio usable | NOT RUN | |
| P05 | Change/create 8–9 profile context; Studio usable | NOT RUN | |
| P06 | Change/create 10–12 profile context; Studio usable | NOT RUN | |
| P07 | Catalog shows all expected discovery surfaces without blank/stranded state | NOT RUN | |
| P08 | Categories remain browsable independently of Home recommendation | NOT RUN | |
| P09 | Art Journeys remain browsable and coherent | NOT RUN | |

### C2. Teaching modes / representative curriculum

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P10 | Representative Draw With Me lesson starts and completes | NOT RUN | |
| P11 | Representative Watch Then Draw overview/replay/draw flow works | NOT RUN | |
| P12 | Authored Trace lesson exposes Trace correctly | NOT RUN | |
| P13 | Non-Trace content never invents a Trace action | NOT RUN | |
| P14 | Representative Help Ladder progresses only after child requests Help | NOT RUN | |
| P15 | Replay/help remains bounded and does not loop/auto-escalate | NOT RUN | |
| P16 | Pace/mode explicit child choice is respected | NOT RUN | |
| P17 | Teacher/reference overlays do not become child artwork | NOT RUN | |
| P18 | Representative younger-child lesson remains reachable/understandable | NOT RUN | |
| P19 | Representative older-child technique/open-creative lesson remains usable | NOT RUN | |

### C3. Adaptive Home / prerequisites / resume precedence

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P20 | Fresh suggestion is sensible for a clean profile | NOT RUN | |
| P21 | Reopening Home with same state gives stable suggestion/reason | NOT RUN | |
| P22 | Recommendation reason copy is friendly/non-judgmental | NOT RUN | |
| P23 | Completing work changes later fresh suggestions coherently | NOT RUN | |
| P24 | Journey continuation behaves coherently after prerequisite completion | NOT RUN | |
| P25 | Unmet-prerequisite lesson does not become fresh-primary suggestion | NOT RUN | |
| P26 | Browse still exposes discoverable curriculum without adaptive hiding | NOT RUN | |
| P27 | Active drawing resume outranks fresh suggestion | NOT RUN | |
| P28 | Active coloring resume outranks drawing resume and fresh suggestion | NOT RUN | |
| P29 | Child-requested adaptive Help chooses only an authored current option | NOT RUN | |
| P30 | No automatic Help appears solely because of inferred struggle/history | NOT RUN | |

### C4. Lifecycle / recovery

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P31 | Save & Leave guided drawing restores correctly | NOT RUN | |
| P32 | Background/foreground guided lesson keeps session/artwork coherent | NOT RUN | |
| P33 | Force-stop/relaunch guided drawing recovers without duplicate/stale work | NOT RUN | |
| P34 | Coloring save/leave/relaunch restores expected state | NOT RUN | |
| P35 | Free Draw save/leave/relaunch restores expected state | NOT RUN | |
| P36 | Adaptive recommendation/help history survives ordinary relaunch | NOT RUN | |
| P37 | Fresh/reset profile does not expose incompatible old adaptive history | NOT RUN | |

### C5. Gallery / Coloring / Free Draw

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P38 | Completed lesson artwork appears in Gallery and reopens | NOT RUN | |
| P39 | Deleting a Gallery entry does not damage unrelated artwork | NOT RUN | |
| P40 | Prepared guided coloring works and protected line art remains intact | NOT RUN | |
| P41 | Coloring recolor + Undo/Redo behaves correctly | NOT RUN | |
| P42 | Legacy/freehand coloring path remains usable where applicable | NOT RUN | |
| P43 | Free Draw tools/canvas/Undo/Redo basic flow works | NOT RUN | |
| P44 | Free Draw finish saves correct Gallery provenance and fresh canvas behavior | NOT RUN | |
| P45 | Deleting Free Draw Gallery copy does not corrupt protected working state | NOT RUN | |

### C6. Offline / accessibility / stability

| ID | Physical check | Status | Notes |
|---|---|---|---|
| P46 | Enable Airplane Mode; Studio/catalog/Home remain usable | NOT RUN | |
| P47 | In Airplane Mode, start/continue/complete representative guided lesson | NOT RUN | |
| P48 | In Airplane Mode, Coloring works | NOT RUN | |
| P49 | In Airplane Mode, Free Draw + Gallery save/reopen work | NOT RUN | |
| P50 | Larger system font does not make critical controls unreachable | NOT RUN | |
| P51 | Small-screen/reachability spot check keeps critical controls usable | NOT RUN | |
| P52 | No crash, ANR, deadlock, persistent blank state or unrecoverable navigation failure through matrix | NOT RUN | |

## D. Physical acceptance summary

- acceptance date: **PENDING**
- tester-reported device model: **PENDING / do not infer**
- tester-reported Android/API: **PENDING / do not infer**
- passed: **0 / 52**
- failed: **0 / 52**
- not run: **52 / 52**
- release blockers: **PENDING**

## E. Closure evidence

- physical-tested executable commit: **PENDING**
- accepted documentation head: **PENDING**
- acceptance-doc CI: **PENDING**
- PR: **PENDING**
- squash merge commit: **PENDING**
- merged-main Android CI: **PENDING**
- issue #88 closure: **PENDING**
- Phase-5 epic #73 closure: **PENDING**

## Acceptance rule

P5.8 cannot close from automated evidence alone. The exact final release-like profile APK must pass the focused physical matrix with no unresolved release blocker, followed by acceptance-doc CI, squash merge, and green merged-main CI.
