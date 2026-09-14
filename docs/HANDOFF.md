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
- Phase 5 / Curriculum & Teaching Experience Expansion 0.5 — ACTIVE
- Parent epic: #73
- P5.1 #74 — COMPLETE; PR #75 merged; merged-main CI #452 GREEN
- Current slice: **P5.2 Content Production System V2 #76**
- Draft PR: #77
- Active branch: `phase5/p5-2-content-production-v2`
- Latest fully verified product release remains **`0.4.0-content-studio`, versionCode 19**
- P5.2 QA1 identity: `0.5.0-curriculum-expansion-p5.2-qa1`, versionCode 20 — candidate evidence/physical acceptance pending

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

## P5.1 — COMPLETE curriculum contract

Authoritative files:
- `docs/10-execution/P5_1_CURRICULUM_RESEARCH.md`
- `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`

Locked target:
- 24 total production guided lessons for 0.5;
- 9 existing Phase-4 lessons retained;
- 15 new lessons;
- 36 remains later public-V1 target;
- Little/Creative/Growing/Young progression is real rather than cosmetic;
- creative authorship increases with age/difficulty;
- no similarity scores, grades, leaderboards, permanent ability labels or cloud profiling;
- Help remains child-controlled/non-punitive;
- culturally specific content requires sourcing/context/review.

## P5.2 — ACTIVE

### Purpose

Scale content production safely before authoring 15 additional lessons. Tooling must consume the real production content model and never become a second lesson runtime.

### Implemented quality/reporting system

- `ContentQualityAnalyzer` consumes `LessonCatalogSnapshot` and validated runtime packages.
- Production catalog/load diagnostics are release errors.
- Conservative authoring/usability rules are warnings only.
- Deterministic text + JSON reports include lessons, ages, difficulty, categories, skills, journeys, modes, coloring/prepared-region coverage and Phase-5 target progress.
- CI verifies report existence/JSON/errorCount and uploads `kids-drawing-p5.2-content-quality-report` for 30 days.
- Negative tests cover structural-error projection plus suspicious tiny geometry, step count, duplicate refs, grouped-demo misuse, non-monotonic Help ordering and tiny prepared regions.
- Production structural truth remains `LessonPackageLoader`, `LessonCatalog`, schema and `ColoringRegionValidator`.

### Calibrated accepted baseline

CI #465 / run `34806642996`: GREEN.

Calibrated report artifact `10333610875` proves:
- 9 release lessons;
- 0 quality errors;
- 0 quality warnings;
- Little 4 / Creative 8 / Growing 7 / Young 2;
- difficulty 1–4 present, difficulty 5 still future Phase-5 work;
- Watch Then Draw 6/6 target minimum already present;
- 3 coloring lessons, 2 prepared-coloring lessons;
- Phase-5 catalog progress 9/24.

### Read-only Content Lab

`ContentInspectionRepository` and `ContentLabActivity` are implemented.

The lab:
- loads the real bundled `LessonCatalog`;
- selects any release lesson;
- displays real bundled preview/thumbnail SVGs;
- displays lesson identity/age/difficulty/modes/skills/journeys;
- selects authored drawing steps;
- visualizes teacher strokes, expected/trace geometry, Help guide geometry and prepared-color regions separately;
- shows step/Help/coloring contract metadata;
- shows default semantic localization keys/values;
- shows analyzer diagnostics/Phase-5 progress.

Safety boundary:
- separate engineering activity;
- `ProductActivity` remains sole MAIN/LAUNCHER;
- no child document/session persistence dependency;
- no mutation API into child artwork/progress/Gallery;
- local bundled assets only.

Complete implementation baseline including Content Lab passed CI #463. Calibrated analyzer baseline passed CI #465.

## P5.2 QA1 freeze

QA file: `docs/10-execution/P5_2_QA.md`.

Candidate identity:
- versionName `0.5.0-curriculum-expansion-p5.2-qa1`;
- versionCode 20;
- dedicated QA1 debug/profile artifact filenames/names configured;
- exact candidate commit/CI/artifact IDs/SHA: PENDING final exact-head green run;
- Content Lab physical/developer matrix: PENDING;
- normal product smoke: PENDING.

Launch engineering lab after installing QA1:
`adb shell am start -n com.navin.kidsdrawing/.ContentLabActivity`

Do not add a child-facing navigation entry merely to make the engineering lab easier to open.

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

1. Freeze exact P5.2 QA1 head after docs synchronization.
2. Require exact-head CI green including P5.2 report + QA1 APK packaging.
3. Fetch content-report/debug/profile artifact IDs.
4. Download profile APK, independently verify size/SHA against CI evidence.
5. Hand exact APK to user; run `P5_2_QA.md` focused Content Lab matrix + normal product smoke.
6. Record only checks actually reported.
7. After acceptance: docs-only evidence commit + exact-head CI → PR #77 ready/squash merge → merged-main CI → close #76.
8. Continue to P5.3 only after P5.2 is formally closed. Bulk P5.4–P5.6 lesson authoring remains blocked until then.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #76 / PR #77
5. `docs/10-execution/P5_2_EXECUTION_CONTRACT.md`
6. `docs/10-execution/P5_2_QA.md`
7. `docs/10-execution/P5_1_CURRICULUM_CONTRACT.md`
8. `docs/17_TAXONOMY_AND_STARTER_CURRICULUM.md`
9. Phase-4 release report only when baseline evidence is needed.

Do not reopen proven foundations merely because a chat changes.
