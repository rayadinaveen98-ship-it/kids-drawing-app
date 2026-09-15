# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Parent epic:** #73  
**Latest completed slice:** P5.7 — Local Adaptive Teaching #86  
**Current slice:** P5.8 — Cross-age Curriculum QA + 0.5 Release #88  
**Active branch:** `phase5/p5-8-cross-age-release`  
**Current P5.8 state:** **BATCH A — CONTRACT + INTEGRATED PRE-FREEZE RELEASE GATE**  
**Current production catalog:** **24 release lessons**  
**Verified starting main:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Verified starting merged-main CI:** Android CI #571 / run `34869966116` — **GREEN**  
**Target final versionName:** `0.5.0-curriculum-expansion`  
**Final versionCode:** **not reserved yet; must be >26 after pre-freeze GREEN**  
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

## P5.8 objective

P5.8 is the final Phase-5 integrated validation/release slice. It must prove the complete 24-lesson product across all four age bands and accepted product surfaces, then cut and physically accept the exact final `0.5.0-curriculum-expansion` APK.

Current order:

1. lock P5.8 execution contract and final QA matrix;
2. add/strengthen integrated automated cross-age release gate;
3. require exact-head pre-freeze CI GREEN;
4. only then set final versionName and a monotonic versionCode >26;
5. produce exact final debug/profile APK evidence;
6. physically accept the exact final profile APK;
7. acceptance-doc CI → squash merge → merged-main CI;
8. close #88 and Phase-5 epic #73 and deliver final APK.

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
