# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Product

Android-first children's drawing/art-learning app built as a patient personal art teacher.

**Product promise:** **Draw together with a teacher who never runs out of patience.**

## Current state

- Phase 0 — COMPLETE
- Phase 1 / Drawing Engine 0.1 — COMPLETE and frozen
- Phase 2 / Lesson Engine 0.2 — COMPLETE and frozen
- Phase 3 / First Vertical Slice 0.3 — COMPLETE
- Phase 4 / Content & Studio Expansion 0.4 — COMPLETE and frozen
- Phase 5 / Curriculum & Teaching Experience Expansion 0.5 — **PLANNING ACTIVE**
- Parent epic: #73
- Current slice: P5.1 Curriculum & Teaching Contract #74
- Active branch: `phase5/p5-1-curriculum-contract`
- Latest verified product release remains `0.4.0-content-studio`, versionCode 19

## Exact accepted 0.4 baseline

- physically tested executable `f3843365d39540de00fe08a008883c15abe75599`
- final squash merge `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`
- final closure `main` head `e8215bedea6f8d55c0ca08ca4015a46062d45769`
- accepted candidate CI #440 GREEN
- merged-main CI #446 GREEN
- closure-doc CI #450 GREEN
- profile artifact `10331363240`
- APK size `16,196,353 bytes`
- SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`
- final physical/product checklist PASS

No Phase-5 work may retroactively change or overclaim this release evidence.

## P5.1 source-of-truth files

Read:
- `docs/10-execution/P5_1_CURRICULUM_RESEARCH.md`
- `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`

The research uses the repository baseline plus authoritative guidance from NCERT NCF-SE 2023, National Core Arts Standards, NAEYC process/developmentally appropriate art guidance, UNESCO culture/arts education guidance and NEP principles.

## Locked Phase-5 direction

### Milestone target

`0.5.0-curriculum-expansion`

### Catalog target

- 24 total production guided lessons for 0.5;
- 9 existing Phase-4 lessons retained;
- 15 new lessons;
- eventual 36-lesson public-V1 target remains later, after the 24-lesson workflow/content quality is proven.

### New lessons

1. Happy Lines
2. Shape Friends
3. Snail Garden
4. Elephant From Shapes
5. Rainbow Weather
6. Tree Through Seasons
7. Ice Cream Shop
8. Simple Car
9. Sailboat Scene
10. Planet With Rings
11. Friendly Alien
12. Face & Expressions
13. Simple Body & Pose
14. Create Your Character
15. One-Point Room

### Age progression

- **4–5 / Little Artists:** marks, basic shapes, simple combination, large geometry, optional Trace only where justified, short concrete teacher language.
- **6–7 / Creative Explorers:** shape fluency, symmetry/spacing, simple overlap, Watch Then Draw, patterns and simple scenes, meaningful variation.
- **8–9 / Growing Artists:** proportion, contour, overlap/depth, foreground/background, texture/detail, character/scene choices, lightweight reflection.
- **10–12 / Young Artists:** deliberate proportion, basic perspective, value/shading foundations, face/body/pose, character design, composition, planning/refinement and non-toddler teacher tone.

### Teaching philosophy

- copying is a learning tool, not the curriculum;
- process and child ownership are first-class;
- creative authorship increases with age;
- no similarity scores, grades, leaderboards or permanent ability labels;
- Help remains child-controlled and never punitive;
- older lessons prefer conceptual/anchor help over tracing;
- optional Responding/Connecting prompts are brief and non-graded;
- culturally specific lessons require explicit sourcing/context/review and are not generic decorative imitation.

### Coverage gates

At 0.5 release:
- Little Artists: 8+ suitable lessons;
- Creative Explorers: 14+;
- Growing Artists: 14+;
- Young Artists: 10+;
- at least 3 credible difficulty-4 experiences;
- at least 1 difficulty-5 lesson (`One-Point Room`);
- at least 8 meaningful creative-choice lessons;
- at least 6 Watch Then Draw / observation-memory experiences where pedagogically appropriate.

## Phase-5 slice plan

1. **P5.1 — Curriculum & Teaching Contract** — active.
2. **P5.2 — Content Production System V2** — next after P5.1 acceptance.
3. **P5.3 — Companion / Teacher Experience V2**.
4. **P5.4 — Curriculum Expansion Set C**.
5. **P5.5 — Curriculum Expansion Set D**.
6. **P5.6 — Curriculum Expansion Set E**.
7. **P5.7 — Local Adaptive Teaching**.
8. **P5.8 — Cross-age curriculum QA + final 0.5 release**.

Do not skip P5.2 and manually author all 15 lessons first. The production/validation workflow must scale before the content catalog does.

## Frozen architecture constraints

- UI never owns artwork/history/lesson truth.
- lessons are structured content, never lesson-ID-specific tutorial code;
- UI cannot set arbitrary engine/session state;
- AndroidX Ink stays behind drawing infrastructure adapters;
- teacher/trace/help/reference overlays never become child artwork;
- coloring/fill remains structurally below protected line art;
- Free Draw is lesson-independent with explicit Gallery provenance;
- persistence stores editable operations, not screenshots;
- core product remains offline-first;
- no mandatory child account, ads, behavioral analytics, network dependency or sensitive permissions;
- engine changes require a concrete defect + explicit contract/ADR.

## Immediate continuation

1. Finish P5.1 roadmap/status/docs on branch.
2. Open draft P5.1 PR.
3. Require exact-head Android CI green.
4. Review and accept P5.1; merge to `main` and verify merged-main CI.
5. Close #74 only after merge-main is green.
6. Create/execute P5.2 Content Production System V2 from that verified baseline.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. epic #73
5. issue #74
6. `P5_1_CURRICULUM_RESEARCH.md`
7. `P5_1_CURRICULUM_CONTRACT.md`
8. `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`
9. `docs/05_CONTENT_ARCHITECTURE.md`

Do not reopen proven foundations merely because a chat changes.
