# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product milestone:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **PLANNING / P5.1 ACTIVE**  
**Active branch:** `phase5/p5-1-curriculum-contract`  
**Parent epic:** #73  
**Current slice:** P5.1 — Curriculum & Teaching Contract #74  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## Completed milestones

- Phase 0 — Product Foundation — COMPLETE.
- Phase 1 — Drawing Engine `0.1.0-art-lab`, versionCode 11 — COMPLETE/frozen. Verified tag `v0.1.0-art-lab` exists.
- Phase 2 — Lesson Engine `0.2.0-lesson-engine`, versionCode 12 — COMPLETE/frozen; physical matrix 32/32 PASS.
- Phase 3 — First Vertical Slice `0.3.0-vertical-slice`, versionCode 13 — COMPLETE; physical 41/41 PASS.
- Phase 4 — Content & Studio Expansion `0.4.0-content-studio`, versionCode 19 — COMPLETE.

No 0.2/0.3/0.4 tag is claimed unless separately verified. Phase 4 is frozen and must not be silently extended.

## Exact accepted 0.4 baseline

- physically tested executable: `f3843365d39540de00fe08a008883c15abe75599`;
- final Phase-4 squash merge: `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`;
- final closure `main` head: `e8215bedea6f8d55c0ca08ca4015a46062d45769`;
- accepted candidate CI #440 GREEN;
- merged-main CI #446 GREEN;
- closure-doc CI #450 GREEN;
- profile artifact `10331363240`;
- profile APK size `16,196,353 bytes`;
- SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- final physical/product checklist PASS.

## Phase 5 — ACTIVE PLANNING

Epic #73 targets `0.5.0-curriculum-expansion`.

Phase 5 does not begin by reopening engine internals. It begins by locking the age 4–12 curriculum, teacher/companion principles and content-production requirements.

### P5.1 — Curriculum & Teaching Contract — ACTIVE

Authoritative files:
- `docs/10-execution/P5_1_CURRICULUM_RESEARCH.md`
- `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`

Locked direction:
- 24 total production guided lessons for 0.5;
- 9 verified Phase-4 lessons retained;
- 15 purposeful new lessons;
- eventual 36-lesson public-V1 target remains later;
- all four age bands receive real progression, not cosmetic difficulty changes;
- guidance decreases and creative authorship increases with age;
- no similarity scoring, permanent ability labels, punitive streaks or cloud profiling;
- content remains structured/generic and offline-first.

Exact new lesson target:
- Happy Lines;
- Shape Friends;
- Snail Garden;
- Elephant From Shapes;
- Rainbow Weather;
- Tree Through Seasons;
- Ice Cream Shop;
- Simple Car;
- Sailboat Scene;
- Planet With Rings;
- Friendly Alien;
- Face & Expressions;
- Simple Body & Pose;
- Create Your Character;
- One-Point Room.

Phase-5 planned slices:
1. P5.1 — Curriculum & Teaching Contract.
2. P5.2 — Content Production System V2.
3. P5.3 — Companion / Teacher Experience V2.
4. P5.4 — Curriculum Expansion Set C.
5. P5.5 — Curriculum Expansion Set D.
6. P5.6 — Curriculum Expansion Set E.
7. P5.7 — Local Adaptive Teaching.
8. P5.8 — Cross-age curriculum QA + `0.5.0-curriculum-expansion` release.

## Frozen architecture invariants

- UI never owns artwork/history/lesson truth.
- Lessons are structured content interpreted generically; no lesson-ID-specific tutorial screens.
- UI cannot set arbitrary engine/session state.
- Teacher/trace/help/reference overlays never become child artwork.
- Persistence owns editable operations, not screenshots.
- AndroidX Ink remains behind owned drawing infrastructure boundaries.
- Coloring/fill remains structurally below protected line art.
- Free Draw remains lesson-independent with explicit provenance.
- Core product remains offline-first.
- No mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions in core milestones.
- No engine redesign without a concrete Phase-5 defect and explicit contract/ADR change.

## Immediate next action

1. Keep P5.1 research + curriculum contract on the dedicated planning branch.
2. Update ROADMAP/HANDOFF to the same state.
3. Open a draft P5.1 PR.
4. Require exact-head CI green.
5. Review/accept the contract and close #74 only after CI and repository evidence are clean.
6. Only then begin P5.2 Content Production System V2; do **not** start authoring all 15 lessons manually first.

## Continuation rule

Inspect in order:
1. `PROJECT_STATUS.md`
2. `docs/HANDOFF.md`
3. `ROADMAP.md`
4. epic #73
5. issue #74
6. `P5_1_CURRICULUM_RESEARCH.md`
7. `P5_1_CURRICULUM_CONTRACT.md`
8. Phase-4 release report only when baseline evidence is needed.
