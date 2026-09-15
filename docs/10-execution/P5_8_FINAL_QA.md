# P5.8 Final QA — Cross-age Curriculum + `0.5.0-curriculum-expansion`

**Issue:** #88  
**Parent epic:** #73  
**PR:** #90  
**Branch:** `phase5/p5-8-cross-age-release`  
**Verified base:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Final versionName:** `0.5.0-curriculum-expansion`  
**Final versionCode:** **27**  
**Physically tested executable commit:** `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`  
**Status:** **PHYSICAL ACCEPTANCE PASS — ACCEPTANCE-DOC CI / MERGE PENDING**

This is the authoritative P5.8 release matrix. The exact final profile APK produced from commit `7ca10ac918def4e5d9e22346dc98ab2d7bdda957` was the physical-test binary. No later rebuild may silently replace that accepted executable.

## A. Automated release gate — 32/32 PASS

| ID | Check | Evidence | Status |
|---|---|---|---|
| A01 | Exactly 24 production lessons load through release catalog | P5.8 integrated gate + content report | PASS |
| A02 | Content quality = 24 lessons / 0 errors / exactly 6 reviewed warnings | final content report | PASS |
| A03 | Reviewed warning IDs exactly match frozen six | production content gate | PASS |
| A04 | All four age bands have release content | integrated cross-age gate | PASS |
| A05 | Lesson assets/localization/references resolve | production validators | PASS |
| A06 | Journey references and prerequisites are structurally valid | integrated/existing tests | PASS |
| A07 | Draw With Me generic execution remains green | execution suite | PASS |
| A08 | Watch Then Draw generic execution remains green | execution suite | PASS |
| A09 | Authored Trace executes only where authored | execution/integrated tests | PASS |
| A10 | Help Ladder / Replay remains authored and bounded | help/adaptive tests | PASS |
| A11 | No automatic adaptive Help escalation | adaptive tests | PASS |
| A12 | Adaptive Help never invents Trace | adaptive tests | PASS |
| A13 | Companion remains read-only relative to session/artwork truth | companion tests | PASS |
| A14 | Recommendations deterministic for all four age bands | P5.8 integrated gate | PASS |
| A15 | Prerequisites remain authoritative for fresh-primary selection | integrated/adaptive tests | PASS |
| A16 | Journey continuation is deterministic | integrated/adaptive tests | PASS |
| A17 | Explicit interests remain influential | integrated/adaptive tests | PASS |
| A18 | New/underexposed skills can influence ordering without ability labels | integrated/adaptive tests | PASS |
| A19 | Completed/recent lessons do not dominate eligible fresh content | adaptive tests | PASS |
| A20 | Recommendation copy is non-judgmental across all age bands | integrated/adaptive tests | PASS |
| A21 | Resume precedence = coloring > drawing > fresh | primary-selection tests | PASS |
| A22 | Missing/corrupt/future adaptive state falls back safely | adaptive store/repository tests | PASS |
| A23 | Adaptive state remains bounded and privacy-safe | adaptive tests + structural check | PASS |
| A24 | Completion/help event delivery remains idempotent | adaptive reducer tests | PASS |
| A25 | Save & Leave / lesson recovery remains green | lifecycle/recovery tests | PASS |
| A26 | Gallery save/reopen/delete isolation remains green | Gallery tests | PASS |
| A27 | Prepared + legacy Coloring regression remains green | Coloring tests | PASS |
| A28 | Free Draw persistence/history/provenance remains green | Free Draw tests | PASS |
| A29 | Drawing Engine teacher/reference isolation remains green | engine/lesson tests | PASS |
| A30 | Android permission allowlist unchanged | CI APK inspection | PASS |
| A31 | Debug + instrumentation + profile APKs assemble | Android CI #580 | PASS |
| A32 | lint + all JVM tests pass | Android CI #580 | PASS |

## B. Final executable identity and immutable evidence

- final versionName: `0.5.0-curriculum-expansion`
- final versionCode: **27**
- exact executable commit: `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`
- final-candidate Android CI: **#580 / run `34927419291` — GREEN**
- debug artifact ID/name: `10380690693` / `kids-drawing-0.5.0-curriculum-expansion-debug`
- debug APK bytes: **20,576,661**
- debug SHA256: `6b6fd08c0d20626a4f75e14baba244d1f929f6ccbd893e792eac7fff9472ae9c`
- profile artifact ID/name: `10380451787` / `kids-drawing-0.5.0-curriculum-expansion-profile`
- profile APK bytes: **16,344,669**
- profile SHA256: `ef6dace150ffd09d4a9b4cabf8558cdf0cc8d4b726d6e7f2c6901e6f57e133d9`
- content-quality artifact: `10380695707` / `kids-drawing-0.5.0-curriculum-expansion-content-quality`
- permission allowlist: **PASS**
- APK identity check: **PASS — versionName 0.5.0-curriculum-expansion / versionCode 27**
- content quality: **24 lessons / 0 errors / exactly 6 reviewed warnings**
- independent downloaded profile hash/size verification: **PASS; exact match with CI evidence**

## C. Focused physical acceptance — exact final profile APK — 52/52 PASS

The user completed the supplied final physical matrix and reported it done with no failed row or release blocker. Device model and Android/API were not supplied, so they are intentionally not inferred.

### C1. Install / onboarding / Studio
P01–P09: **PASS (9/9)** — install/launch, fresh onboarding, all four age-band contexts, discovery surfaces, categories and Art Journeys.

### C2. Teaching modes / representative curriculum
P10–P19: **PASS (10/10)** — Draw With Me, Watch Then Draw, authored Trace, non-Trace safety, Help Ladder, bounded replay/help, explicit pace/mode, teacher/reference isolation, younger-child and older-child representative lessons.

### C3. Adaptive Home / prerequisites / resume precedence
P20–P30: **PASS (11/11)** — fresh suggestion, deterministic reason, friendly copy, progression change, journey continuation, prerequisite authority, browse preservation, drawing/coloring resume precedence, child-requested authored Help, no automatic inferred-struggle Help.

### C4. Lifecycle / recovery
P31–P37: **PASS (7/7)** — Save & Leave, background/foreground, force-stop/relaunch, coloring recovery, Free Draw recovery, adaptive-state persistence and fresh-profile isolation.

### C5. Gallery / Coloring / Free Draw
P38–P45: **PASS (8/8)** — Gallery reopen/delete isolation, prepared coloring, protected line art, recolor/Undo/Redo, legacy/freehand coloring, Free Draw tools/history, provenance and deletion safety.

### C6. Offline / accessibility / stability
P46–P52: **PASS (7/7)** — Airplane Mode Studio/guided lesson/Coloring/Free Draw/Gallery, larger-font reachability, small-screen/reachability spot check and no crash/ANR/deadlock/persistent blank/unrecoverable navigation failure through the matrix.

## D. Physical acceptance summary

- acceptance date: **2026-09-15**
- tester-reported device model: **not provided / not inferred**
- tester-reported Android/API: **not provided / not inferred**
- passed: **52 / 52**
- failed: **0 / 52**
- not run: **0 / 52**
- release blockers: **none reported**

## E. Closure evidence

- physical-tested executable commit: `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`
- accepted documentation head: **this commit / pending CI SHA capture**
- acceptance-doc CI: **PENDING**
- PR: **#90 — draft until acceptance-doc CI green**
- squash merge commit: **PENDING**
- merged-main Android CI: **PENDING**
- issue #88 closure: **PENDING**
- Phase-5 epic #73 closure: **PENDING**

## Release decision

# **PHYSICAL QA PASS — 52/52**

The exact v27 profile candidate passed automated and physical acceptance. Remaining work is repository closure only: acceptance-doc CI green → PR #90 ready → squash merge → merged-main CI green → close #88 and #73.