# P5.8 / `0.5.0-curriculum-expansion` Release Report

**Issue:** #88 — CLOSED / COMPLETED  
**PR:** #90 — SQUASH-MERGED  
**Parent epic:** #73 — CLOSED / COMPLETED  
**Status:** **RELEASE COMPLETE**

## Release identity

- versionName: `0.5.0-curriculum-expansion`
- versionCode: **27**
- physically tested executable commit: `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`
- final-candidate Android CI: **#580 / run `34927419291` — GREEN**
- acceptance-documentation head: `f299a77573fcd47ffd72b6ecee223b4d0610234b`
- acceptance-documentation Android CI: **#584 / run `34934075313` — GREEN**
- final squash merge commit: `fbc118343dde860a9784d7a47356eb6a7fff73e1`
- merged-main Android CI: **#585 / run `34934427237` — GREEN**
- final tag: not created / not claimed.

The exact physically accepted product binary remains the executable at `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`. Acceptance documentation and the squash merge are repository evidence and do not replace that tested binary.

## Phase-5 product delivered

Phase 5 turns the 0.4 Studio foundation into a deeper offline art-learning product with:
- a frozen 24-lesson curriculum across all four age bands;
- professional content-production validation and Content Lab workflow;
- strengthened generic Companion/Teacher presentation;
- foundations, nature/everyday, animals, vehicles/space, character/people and older-child technique content;
- deterministic local adaptive recommendations using explicit profile/progress/help signals;
- explainable non-judgmental recommendation reasons;
- prerequisite-safe journey progression;
- coloring resume → drawing resume → fresh recommendation precedence;
- child-requested adaptive Help constrained to authored Help/Replay choices;
- no invented Trace, automatic Help, grades/ranks/XP, permanent ability labels, cloud profiling or network dependency;
- preserved Gallery, Coloring, Free Draw, lifecycle/recovery and offline behavior.

## Automated release evidence

### Pre-freeze gate

Commit `dc5b3128dd4a3e3324b64ebe5d51c24dd37451fc`  
Android CI #579 / run `34926970514`: **GREEN**.

This gate validated the integrated production catalog and cross-age/adaptive release contract before reserving the final versionCode.

### Exact final candidate

Commit `7ca10ac918def4e5d9e22346dc98ab2d7bdda957`  
Android CI #580 / run `34927419291`: **GREEN**.

Passed:
- committed JSON/content parsing;
- AndroidX Ink boundary;
- all JVM tests including P5.8 integrated real-catalog gate;
- lint;
- debug APK build;
- instrumentation APK build;
- release-like profile APK build;
- exact content quality contract **24 lessons / 0 errors / 6 reviewed warnings**;
- permission allowlist;
- exact APK identity check for `0.5.0-curriculum-expansion` / versionCode 27;
- final artifact packaging/upload.

### Acceptance documentation

Commit `f299a77573fcd47ffd72b6ecee223b4d0610234b`  
Android CI #584 / run `34934075313`: **GREEN**.

This revalidated the accepted product while recording final physical evidence and the Phase-6 roadmap. It introduced no replacement product binary.

### Final merge to `main`

Squash merge `fbc118343dde860a9784d7a47356eb6a7fff73e1`  
Android CI #585 / run `34934427237`: **GREEN**.

The merged-main gate again passed JSON/content parsing, Ink boundary, all JVM tests, lint, debug/instrumentation/profile APK builds, exact 24/0/6 content quality, permission allowlist, final APK identity and artifact packaging.

## APK evidence

- debug artifact: `10380690693` / `kids-drawing-0.5.0-curriculum-expansion-debug`;
- debug APK size: `20,576,661 bytes`;
- debug APK SHA-256: `6b6fd08c0d20626a4f75e14baba244d1f929f6ccbd893e792eac7fff9472ae9c`;
- profile artifact: `10380451787` / `kids-drawing-0.5.0-curriculum-expansion-profile`;
- profile APK size: `16,344,669 bytes`;
- profile APK SHA-256: `ef6dace150ffd09d4a9b4cabf8558cdf0cc8d4b726d6e7f2c6901e6f57e133d9`;
- content-quality artifact: `10380695707`;
- independent downloaded profile hash/size verification: **PASS**, exact match with CI evidence.

## Physical/product QA

Authoritative matrix: `docs/10-execution/P5_8_FINAL_QA.md`.

- acceptance date: **2026-09-15**;
- exact tested binary: final v27 profile artifact above;
- result: **52/52 PASS**;
- failed: **0**;
- not run: **0**;
- release blockers: **none reported**;
- tester device/model/API: **not provided and not inferred**.

Coverage includes all four age bands, discovery/journeys, all accepted teaching modes, authored Trace/Help behavior, adaptive Home/help, prerequisite authority, resume precedence, lifecycle/recovery, Gallery/Coloring/Free Draw, Airplane Mode, larger-font/small-screen reachability and stability.

## Repository closure evidence

- P5.8 issue #88: **CLOSED / COMPLETED**;
- Phase-5 epic #73: **CLOSED / COMPLETED**;
- PR #90: **SQUASH-MERGED** at `fbc118343dde860a9784d7a47356eb6a7fff73e1`;
- merged-main Android CI #585 / run `34934427237`: **GREEN**;
- Phase 6 continues under epic #91 with P6.1 #92.

## Release decision

# **PASS — `0.5.0-curriculum-expansion` RELEASE COMPLETE**

Phase 5 has met its exit target: the exact v27 candidate passed the final automated and physical matrices, immutable APK evidence was verified, acceptance documentation passed CI, PR #90 squash-merged cleanly, merged-main CI passed, and both P5.8 and the Phase-5 epic are closed completed.