# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Product
Android-first children's drawing/art-learning app built as a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

Development model: **Core-engine + vertical-slice** — Specify → build capability → test independently → integrate → test again → lock milestone → expand.

## Current state

- Phase 0 — COMPLETE
- Phase 1 / Drawing Engine 0.1 — COMPLETE and frozen
- Phase 2 / Lesson Engine 0.2 — COMPLETE and frozen
- Phase 3 / First Vertical Slice 0.3 — COMPLETE
- Phase 4 / Content & Studio Expansion 0.4 — FINAL RELEASE GATE ACTIVE
- P4.1 — COMPLETE
- P4.2 — COMPLETE
- P4.3 — implementation merged; issue #60 remains open only for final deferred physical coverage
- P4.4 — COMPLETE; deferred row-level Free Draw checks moved into P4.7
- P4.5 — COMPLETE; deferred coloring/lifecycle/offline/small-screen checks moved into P4.7
- P4.6 — COMPLETE; issue #63 closed; PR #71 squash merge `b811a149e44eadfee815f1f9896f2871e9f7e25d`; merged-main CI #433 GREEN
- Current slice: **P4.7 End-to-end QA + `0.4.0-content-studio` release (#64 / draft PR #72)**
- Active branch: `phase4/p4-7-final-release`
- Final candidate identity: `0.4.0-content-studio`, versionCode 19

## Final Phase-4 release contract

Read first:
- `docs/10-execution/P4_7_EXECUTION_CONTRACT.md`
- `docs/10-execution/P4_7_FINAL_QA.md`

P4.7 is **not** a new feature slice. Only release evidence, automated regression hardening and concrete release-blocking fixes are allowed.

Final matrix includes:
- fresh install/onboarding/profile and nine-lesson Studio;
- categories, Art Journeys and deterministic recommendations;
- Smiling Sun Trace, Friendly Owl Help 1–5/cumulative construction, Simple Rocket Watch Then Draw, Easy Flower grouped pace playback;
- Hot Air Balloon guided multi-region coloring, Fox Portrait, Design Your Spaceship creative step;
- complete row-level Free Draw tool/history/recovery/Gallery matrix;
- Little Fish/Hot Air Balloon prepared coloring, exact Undo boundary, lifecycle/process recreation and Gallery reopen;
- Cute Cat legacy freehand coloring;
- lesson/coloring/Free Draw process recreation;
- Gallery delete/source safety;
- full Airplane Mode core journeys;
- age 4–5 / 6–7 / 8–9 / 10–12 spot checks;
- larger font and handedness/voice/reduced motion where the capability is actually exposed;
- zero teacher/reference contamination, no line-art damage, no operation loss, no crash/ANR/deadlock.

## Final candidate rules

Initial candidate:
- versionName `0.4.0-content-studio`;
- versionCode 19;
- final workflow artifact names use `Kids_Drawing_0.4.0_Content_Studio-*`;
- exact physically tested APK must be the intended final delivered product binary.

If a materially changed APK is distributed after versionCode 19, increment to 20+; never reuse the distributed versionCode.

Acceptance documentation may create later docs-only commits, but always distinguish tested executable commit from documentation/merge commits.

## P4.6 evidence baseline

P4.6 final integration:
- merge `b811a149e44eadfee815f1f9896f2871e9f7e25d`;
- merged-main CI #433 / run `34800596778` GREEN;
- nine representative production lessons in catalog.

P4.6 QA1 physical evidence remains preserved in `P4_6_CONTENT_QA.md`; P4.7 retests only what is necessary for the complete release/deferred matrix.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons are structured content, never lesson-ID-specific tutorial code;
- UI cannot set arbitrary engine/session state;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help/reference overlays never become child artwork;
- coloring/fill remains structurally below protected line art;
- Free Draw is lesson-independent and uses explicit Gallery provenance;
- persistence stores editable operations, not screenshots;
- core product remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions;
- CI green is required before final release acceptance.

## Immediate continuation

1. Finish P4.7 final candidate freeze and exact-head CI.
2. Capture final debug/profile artifact IDs, profile APK bytes and SHA-256; independently verify them.
3. Materialize exact final profile APK for physical QA.
4. Execute `P4_7_FINAL_QA.md`; record PASS/FAIL only for actual checks.
5. If any release blocker appears, fix narrowly and increment versionCode for any changed distributed APK.
6. Commit accepted matrix + `P4_7_RELEASE_REPORT.md`.
7. Require exact-head acceptance-doc CI green.
8. Close P4.3 #60 only when its final rows pass.
9. Mark PR #72 ready, squash-merge, require merged-main CI green.
10. Close issue #64 and epic #57 only after final evidence supports closure.
11. Deliver the exact final profile APK; tag only if tooling supports creation and the tag is verified.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #64 + PR #72
5. `P4_7_EXECUTION_CONTRACT.md`
6. `P4_7_FINAL_QA.md`
7. issue #60 + `P4_3_CONTENT_QA.md`
8. `P4_4_FREE_DRAW_QA.md`
9. `P4_5_COLORING_QA.md`
10. relevant engine/content specifications.

Do not redesign proven foundations merely because a chat changes.
