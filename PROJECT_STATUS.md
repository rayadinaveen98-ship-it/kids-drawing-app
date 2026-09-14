# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE / FINAL SLICE**  
**Parent epic:** #73  
**Latest completed slice:** P5.7 — Local Adaptive Teaching #86  
**Current slice:** P5.8 — Cross-age Curriculum QA + 0.5 Release #88  
**Active branch:** `phase5/p5-8-final-release`  
**Draft PR:** #89  
**Current P5.8 state:** **CONTRACT/DOCS VERIFIED GREEN; INTEGRATED PRE-FREEZE REGRESSION GATE NEXT**  
**Current production catalog:** **24 release lessons**  
**Verified starting main:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Verified starting merged-main CI:** Android CI #571 / run `34869966116` — **GREEN**  
**P5.8 contract/docs CI:** Android CI #572 / run `34870784247` — **GREEN**  
**Reserved final release versionCode:** **27**  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## P5.7 frozen baseline

- PR #87 squash-merged into `main` at `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`.
- merged-main Android CI #571 / run `34869966116`: **GREEN**.
- issue #86: **closed completed**.
- accepted P5.7 QA1: `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode 26.
- P5.7 physical acceptance: **45/45 PASS**.
- profile artifact: `10357367072`.
- profile SHA256: `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`.

## P5.8 contract

- issue #88.
- branch `phase5/p5-8-final-release`.
- draft PR #89.
- contract-first commit `0bb87cb845b9ae713a4cefcf46b55a57fdce6148`.
- contract/docs head CI #572 / run `34870784247`: **GREEN**.
- target final milestone: `0.5.0-curriculum-expansion`.
- versionCode **27** is reserved for the first final-release freeze only.

## Final integrated baseline to preserve

- 24 production lessons.
- age coverage: Little 8 / Creative 18 / Growing 17 / Young 10.
- difficulty: D1 5 / D2 9 / D3 6 / D4 3 / D5 1.
- content quality: 24 / 0 errors / exactly six reviewed `NO_JOURNEY_MEMBERSHIP` warnings.
- Draw With Me, Watch Then Draw, authored Trace, Help Ladder, Replay and Companion.
- journeys/prerequisites/full-catalog discovery.
- coloring resume → drawing resume → fresh precedence.
- deterministic local adaptive recommendations and child-controlled adaptive Help.
- lifecycle/recovery, Gallery, Coloring and Free Draw.
- offline core, unchanged permission allowlist, no cloud/analytics/ability-scoring boundary changes.

## Immediate gate

1. promote the integrated P5.8 release regression test checkpoint;
2. require its exact-head CI GREEN;
3. only then cut final `0.5.0-curriculum-expansion`, versionCode **27**;
4. capture exact APK evidence and perform final physical acceptance;
5. acceptance CI → squash merge → merged-main CI → close #88 and Phase 5.

## Frozen architecture invariants

- UI never owns artwork/history/lesson truth.
- `LessonSessionState` remains teaching-state truth.
- Companion remains read-only.
- overlays never enter child artwork.
- adaptive state is local/bounded/advisory and never stores raw artwork/strokes or ability labels.
- no network dependency for core use.
