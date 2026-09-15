# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Parent epic:** #73  
**Latest completed slice:** P5.7 — Local Adaptive Teaching #86  
**Current slice:** P5.8 — Cross-age Curriculum QA + 0.5 Release #88  
**Active branch:** `phase5/p5-8-cross-age-release`  
**Draft PR:** #90  
**Current P5.8 state:** **BATCH B — FINAL RELEASE FREEZE / v27 CANDIDATE CI**  
**Current production catalog:** **24 release lessons**  
**Verified starting main:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Verified starting merged-main CI:** Android CI #571 / run `34869966116` — **GREEN**  
**Pre-freeze exact-head commit:** `dc5b3128dd4a3e3324b64ebe5d51c24dd37451fc`  
**Pre-freeze Android CI:** #579 / run `34926970514` — **GREEN**  
**Final versionName:** `0.5.0-curriculum-expansion`  
**Final candidate versionCode:** **27**  
**Last updated:** 2026-09-15

Git is authoritative when chat memory and repository state disagree.

## P5.7 closure — verified

- issue #86: **CLOSED / COMPLETED**;
- PR #87: **SQUASH-MERGED**;
- merge commit: `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`;
- merged-main Android CI #571 / run `34869966116`: **GREEN**;
- accepted binary commit: `e258632e83e83a39ac649855ea19592c2f5003ae`;
- accepted versionName `0.5.0-curriculum-expansion-p5.7-qa1`;
- accepted versionCode **26**;
- accepted profile SHA256 `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`;
- physical acceptance **45/45 PASS** on 2026-09-14;
- reported defects: **none**.

## P5.8 Batch A — verified GREEN

- execution contract + 52-row final physical matrix locked in Git;
- integrated real-catalog P5.8 release gate added;
- current-runner Android SDK setup defect fixed by removing the obsolete implicit `tools` package request and explicitly requesting `platform-tools`;
- pre-freeze commit `dc5b3128dd4a3e3324b64ebe5d51c24dd37451fc`;
- Android CI #579 / run `34926970514`: **GREEN**;
- unit tests, lint, debug/instrumentation/profile APK compile, content quality, permission allowlist and evidence packaging all passed;
- no P5.8 product defect discovered by the pre-freeze automated gate.

## P5.8 Batch B — active

The final executable identity is now permitted to freeze because Batch A passed.

Current actions:

1. set versionName exactly `0.5.0-curriculum-expansion`;
2. reserve monotonic versionCode **27**;
3. make CI enforce exact content quality **24 lessons / 0 errors / 6 warnings**;
4. verify versionName/versionCode directly from both final debug/profile APKs;
5. package final-named debug/profile artifacts with sizes, SHA256 and release identity evidence;
6. require the exact final-candidate head CI to be GREEN before physical distribution/acceptance.

After final-candidate CI is green:

1. capture immutable artifact IDs, byte sizes and SHA256;
2. independently verify the profile archive/APK where tooling permits;
3. physically test the exact v27 profile APK against `docs/10-execution/P5_8_FINAL_QA.md`;
4. any executable fix after distribution requires a new versionCode;
5. acceptance-doc CI → squash merge → merged-main CI;
6. close #88 and Phase-5 epic #73 and deliver the accepted final APK.

## Frozen Phase-5 release contract

- exactly **24 production lessons**;
- content quality **24 lessons / 0 errors / exactly 6 reviewed warnings** unless a deliberate reviewed content change updates the contract;
- all four age bands validated;
- `StudioPrimarySelectionPolicy`: coloring resume → drawing resume → fresh recommendation;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- `ChildProfile` remains explicit profile truth;
- `LessonSessionState` remains teaching-state truth;
- Companion remains read-only;
- Help remains child-invoked and authored;
- Trace is never invented;
- adaptive state stays advisory/local/bounded/deterministic and cannot mutate artwork/session truth;
- no raw artwork/strokes, ability labels, grades/scores/rank/XP, punitive streaks, cloud profiling, analytics upload or network dependency;
- final release requires exact Git/CI/APK hash evidence plus physical acceptance.
