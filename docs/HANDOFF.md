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
- Phase 4 / Content & Studio Expansion 0.4 — ACTIVE
- P4.1 — COMPLETE
- P4.2 — COMPLETE
- P4.3 — implementation merged; issue #60 stays open for deferred broader physical/content matrix
- P4.4 — COMPLETE; issue #61 closed, PR #69 merged
- P4.5 — COMPLETE; issue #62 closed, PR #70 merged at `10b8f2f7a6a6b4579a8695ab4f602c2e4525aec5`, merged-main CI #420 GREEN
- Current slice: **P4.6 Representative Content Set B + Cross-content QA (#63 / draft PR #71)**
- Active branch: `phase4/p4-6-content-set-b`
- QA1 identity: `0.4.0-content-studio-p4.6-qa1`, versionCode 18

## Latest fully verified Android product milestone

`0.3.0-vertical-slice` / versionCode 13:
- executable `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`;
- Android CI #307 / run `34706767213` GREEN;
- profile artifact `10301836886`;
- SHA-256 `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`;
- physical 41/41 PASS.

Only repository tag `v0.1.0-art-lab` is known to exist. Do not claim 0.2/0.3 tags exist unless verified later.

## P4.6 implementation

Contract: `docs/10-execution/P4_6_EXECUTION_CONTRACT.md`  
QA: `docs/10-execution/P4_6_CONTENT_QA.md`

Nine release lessons are now represented in the branch. New P4.6 packages:
- **Hot Air Balloon** — Little/Creative/Growing; richer prepared-region guided coloring; four large regions; center → side pair → basket progression.
- **Fox Portrait** — Growing/Young; difficulty 4; proportion, placement, contour refinement, facial landmarks, texture and observation; no Trace dependency.
- **Design Your Spaceship** — Creative/Growing/Young; structured foundation plus a genuine `make_it_yours` step. Teacher examples are inspiration; child `expectedStrokeRefs` is empty so no replica is required.

Implementation head `7928c712e65e180a1d97042c0ea5d131e61c65fb` passed Android CI #425 / run `34758790360` across unit tests, lint, debug APK, instrumentation APK, profile APK and permission allowlist before QA1 version/evidence renaming.

QA1 freeze commit: `00a101f62e621e7ec7865920743b3a0d8b0c66c9`. It sets versionCode 18 and P4.6 artifact names. It requires exact-head CI before APK distribution or physical acceptance.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons are structured content, never lesson-ID-specific tutorial code;
- UI cannot set arbitrary engine/session state;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help overlays never become child artwork;
- coloring is structurally below protected line art;
- persistence stores editable operations, not screenshots;
- core drawing/teaching remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions in core milestones;
- critical-path development stays ₹0 where a professional free option exists;
- CI green is required before a slice is complete.

## Immediate continuation

1. Wait for exact-head CI on QA1 freeze `00a101f62e621e7ec7865920743b3a0d8b0c66c9`; fix only concrete failures.
2. Capture exact debug/profile artifact metadata, profile size and SHA-256.
3. Materialize the profile APK and give it to the user for focused physical P4.6 testing.
4. Test the three new experiences plus representative regressions listed in `P4_6_CONTENT_QA.md`.
5. Keep airplane/process-death/small-screen rows deferred to P4.7 unless they are actually rerun.
6. After user acceptance and final documentation: mark PR #71 ready, squash-merge, verify merged-main CI, close #63.
7. Begin P4.7 only after that gate.

## Resume protocol

Inspect in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. Phase 4 epic #57
5. issue #63 + PR #71 + `P4_6_EXECUTION_CONTRACT.md` + `P4_6_CONTENT_QA.md`
6. current P4.6 exact-head CI/artifact state
7. still-open P4.3 issue #60 / deferred P4.7 physical coverage
8. relevant Drawing/Coloring/Lesson specs.

Do not redesign proven Drawing/Lesson engine foundations merely because a chat changes.
