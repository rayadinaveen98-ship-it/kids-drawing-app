# P4.7 / `0.4.0-content-studio` Release Report

**Issue:** #64  
**PR:** #72  
**Branch:** `phase4/p4-7-final-release`  
**Status:** exact v19 release candidate accepted; repository closure pending

## Release identity

- versionName: `0.4.0-content-studio`
- versionCode: 19
- physically tested executable commit: `f3843365d39540de00fe08a008883c15abe75599`
- accepted documentation head: PENDING after final acceptance/status documentation commits
- final squash/merge commit: PENDING
- final tag: not created; repository tooling available in this session exposes no tag-creation action, so no tag is claimed

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

Exact final candidate commit: `f3843365d39540de00fe08a008883c15abe75599`.

Android CI #440 / run `34801122118`: **GREEN**.
- committed JSON parsing: PASS;
- AndroidX Ink boundary: PASS;
- unit tests: PASS;
- lint: PASS;
- debug APK build: PASS;
- instrumentation APK build: PASS;
- profile APK build: PASS;
- permission allowlist: PASS;
- final artifact packaging/upload: PASS.

The P4.7 compare against verified P4.6 main contained no product/runtime behavior changes: only final version identity, CI artifact naming and release/QA/status documentation. Thus the accepted v19 binary is the exact final product candidate rather than a new feature build.

## APK evidence

- debug artifact: `10331472910` / `kids-drawing-0.4.0-content-studio-debug`;
- profile artifact: `10331363240` / `kids-drawing-0.4.0-content-studio-profile`;
- profile APK filename: `Kids_Drawing_0.4.0_Content_Studio-profile.apk`;
- profile APK size: `16,196,353 bytes`;
- profile APK SHA-256: `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- independent local hash/size verification: PASS, exact match with CI evidence;
- profile artifact archive digest: `sha256:1a539ec2952707eeb66e40b49322dc83f7a855b496f55e245ca0715459218bd1`.

## Physical/product QA

Authoritative matrix: `docs/10-execution/P4_7_FINAL_QA.md`.

- device/model/API: not restated by the user in the final acceptance turn; never infer;
- physical acceptance date: 2026-09-14;
- acceptance statement: user reported **“final passed bro”** after the complete ten-group final checklist;
- matrix result: **PHYSICAL / PRODUCT / AUTOMATED RELEASE CANDIDATE PASS**;
- P4.3 deferred Set-A coverage: PASS in final checklist;
- P4.4 deferred Free Draw coverage: PASS in final checklist;
- P4.5 deferred coloring coverage: PASS in final checklist;
- P4.6 release-regression coverage: PASS in final checklist;
- Airplane Mode: PASS in final checklist;
- lifecycle/process recreation: PASS in final checklist;
- all four age-band spot checks: PASS;
- larger system font-scale spot check: PASS;
- handedness / voice-off / reduced-motion product settings: NOT APPLICABLE because they are not exposed in this milestone;
- contamination/operation-loss checks: PASS;
- stability result: PASS — no crash, ANR, deadlock or unrecoverable blank state reported in the completed final checklist.

Two cases are intentionally not misrepresented as physical injection:
- resume-priority policy is backed by frozen deterministic P4.2 automated coverage;
- missing/incompatible runtime fallback/no-stranded-artwork behavior is backed by frozen P2/P3 recovery tests.

## Defects found during final release QA

No release-blocking defect was reported against the distributed v19 final candidate. No replacement binary was required, so versionCode remains 19.

## Known exceptions

- Exact device/model/API was not restated in the final acceptance turn and is intentionally not inferred.
- Handedness, voice-off and reduced-motion toggles are not exposed as product settings in this milestone; their conditional physical rows are N/A.
- No release tag is created or claimed because available repository tooling does not expose tag creation.

## Repository closure evidence

- P4.3 issue #60 closure: PENDING after final acceptance-doc exact-head CI;
- P4.7 issue #64 closure: PENDING after merge-main green;
- Phase-4 epic #57 closure: PENDING after #64 closes;
- PR #72 ready/merge: PENDING after final acceptance-doc exact-head CI;
- merged-main Android CI: PENDING.

## Release decision

**EXACT V19 RELEASE CANDIDATE ACCEPTED.**

`0.4.0-content-studio` has passed the required physical/product matrix and exact-candidate CI. Only repository closure gates remain: acceptance-documentation exact-head CI, deferred P4.3 issue closure, PR #72 merge, merged-main CI, and final issue/epic closure. These documentation/repository steps do not replace the physically accepted executable `f3843365d39540de00fe08a008883c15abe75599` or its verified APK hash.
