# P4.7 / `0.4.0-content-studio` Release Report

**Issue:** #64 — CLOSED / completed  
**PR:** #72 — squash-merged  
**Parent epic:** #57 — CLOSED / completed  
**Status:** **RELEASE COMPLETE**

## Release identity

- versionName: `0.4.0-content-studio`
- versionCode: 19
- physically tested executable commit: `f3843365d39540de00fe08a008883c15abe75599`
- accepted documentation head before merge: `84771ba4a19692953bc79a1cf185c9a5d5c491b5`
- final squash merge commit: `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`
- final tag: **not created / not claimed**; connected repository tooling did not expose tag creation

The physically accepted product binary remains the exact v19 executable above. Later acceptance/closure commits are documentation/repository evidence only and do not replace that tested binary.

## Product delivered by Phase 4

Phase 4 expands the Phase-3 single vertical slice into an offline multi-lesson creative studio with:
- deterministic multi-package lesson catalog;
- Studio discovery, categories and Art Journeys;
- age-aware deterministic recommendations and safe resume routing;
- nine representative production lessons across all four age bands and difficulties 1–4;
- Trace & Learn, Draw With Me and Watch Then Draw;
- authored Help Ladder and grouped demonstrations;
- cumulative teacher construction references that remain presentation-only;
- Free Draw with Pencil/Crayon/Marker/Eraser, palette/size, editable history, recovery and Gallery provenance;
- prepared-region Fill with authored Color With Me progression plus Color Myself/freehand coloring;
- schema-3 reversible Fill history with schema-1/2 backward readability;
- protected line-art isolation;
- lesson/coloring/Free Draw Gallery reopen/delete safety;
- offline-first core product behavior.

## Representative release content

1. Cute Cat — legacy baseline + freehand coloring + all teaching modes.
2. Smiling Sun — beginner Trace & Learn.
3. Friendly Owl — full Help Ladder.
4. Simple Rocket — Watch Then Draw.
5. Easy Flower — grouped demonstration.
6. Little Fish — prepared-region guided coloring.
7. Hot Air Balloon — richer multi-region guided coloring.
8. Design Your Spaceship — open-ended creative variation.
9. Fox Portrait — older-child proportion/detail/observation.

## Automated release evidence

### Exact physically tested candidate

Commit `f3843365d39540de00fe08a008883c15abe75599`  
Android CI #440 / run `34801122118`: **GREEN**

Passed:
- committed JSON parsing;
- AndroidX Ink boundary;
- unit tests;
- lint;
- debug APK build;
- instrumentation APK build;
- profile APK build;
- permission allowlist;
- final artifact packaging/upload.

### Acceptance-documentation head

Commit `84771ba4a19692953bc79a1cf185c9a5d5c491b5`  
Android CI #445 / run `34801855617`: **GREEN**

This verified the final accepted QA matrix/release documentation without changing product behavior.

### Final merge to `main`

Squash merge `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`  
Android CI #446 / run `34802235303`: **GREEN**

The merged-main gate passed JSON/content parsing, Ink boundary, unit tests, lint, debug/instrumentation/profile APK builds, permission allowlist and final artifact packaging.

## APK evidence

- debug artifact from accepted candidate: `10331472910` / `kids-drawing-0.4.0-content-studio-debug`;
- profile artifact from accepted candidate: `10331363240` / `kids-drawing-0.4.0-content-studio-profile`;
- profile APK filename: `Kids_Drawing_0.4.0_Content_Studio-profile.apk`;
- profile APK size: `16,196,353 bytes`;
- profile APK SHA-256: `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- profile artifact archive digest: `sha256:1a539ec2952707eeb66e40b49322dc83f7a855b496f55e245ca0715459218bd1`;
- independent local hash/size verification: **PASS**, exact match with CI evidence.

## Physical/product QA

Authoritative matrix: `docs/10-execution/P4_7_FINAL_QA.md`.

- device/model/API: not restated by the user in the final acceptance turn; intentionally not inferred;
- physical acceptance date: 2026-09-14;
- acceptance statement: user reported **“final passed bro”** after the supplied complete ten-group final checklist;
- matrix result: **PASS**;
- P4.3 deferred Set-A coverage: PASS;
- P4.4 deferred Free Draw coverage: PASS;
- P4.5 deferred coloring coverage: PASS;
- P4.6 release-regression coverage: PASS;
- Airplane Mode: PASS;
- lifecycle/process recreation: PASS;
- age 4–5 / 6–7 / 8–9 / 10–12 spot checks: PASS;
- larger system font-scale: PASS;
- contamination/operation-loss/stability sweep: PASS;
- handedness / voice-off / reduced-motion product settings: NOT APPLICABLE because those settings are not exposed in this milestone.

Two cases are intentionally represented by automated evidence rather than fabricated physical failure injection:
- deterministic resume-priority policy — frozen P4.2 automated coverage;
- missing/incompatible runtime fallback / no-stranded-artwork behavior — frozen P2/P3 recovery coverage.

## Defects found during final release QA

No release-blocking defect was reported against the distributed v19 final candidate. No replacement APK was required; versionCode remains 19.

## Repository closure evidence

- P4.1 issue #58: closed;
- P4.2 issue #59: closed;
- P4.3 issue #60: **closed completed** after final deferred Set-A physical coverage;
- P4.4 issue #61: closed;
- P4.5 issue #62: closed;
- P4.6 issue #63: closed;
- P4.7 issue #64: **closed completed**;
- Phase-4 epic #57: **closed completed**;
- PR #72: squash-merged at `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`;
- merged-main Android CI #446 / run `34802235303`: **GREEN**.

## Known exceptions

- Exact device/model/API was not restated in the final acceptance turn and is not inferred.
- Handedness, voice-off and reduced-motion toggles are not exposed as product settings in this milestone; their conditional rows are N/A.
- No `v0.4.0-content-studio` tag is created or claimed because connected repository tooling did not expose tag creation.

## Release decision

# **PASS — `0.4.0-content-studio` RELEASE COMPLETE**

Phase 4 has met its exit target: the exact v19 candidate passed the final physical/product matrix and automated gate, acceptance evidence was committed, PR #72 merged cleanly, merged-main CI passed, all Phase-4 slice issues and the epic are closed, and the installable profile APK has reproducible size/SHA evidence.
