# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE / final slice.
- Parent epic: #73.
- P5.1–P5.7 — COMPLETE/frozen.
- P5.8 Cross-age Curriculum QA + 0.5 Release #88 — **ACTIVE**.
- branch: `phase5/p5-8-final-release`.
- production catalog: **24 release lessons**.
- verified starting main: `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`.
- merged-main Android CI #571 / run `34869966116`: **GREEN**.

## P5.7 frozen baseline

- PR #87 squash-merged at `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`.
- issue #86 closed completed.
- QA1 versionCode 26 physically accepted 45/45.
- profile artifact `10357367072`.
- profile SHA256 `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`.

## P5.8 authoritative contract

Read `docs/10-execution/P5_8_EXECUTION_CONTRACT.md`.

Contract-first commit:
- `0bb87cb845b9ae713a4cefcf46b55a57fdce6148`.

Target:
- final versionName `0.5.0-curriculum-expansion`;
- versionCode **27 reserved for final freeze only**.

## Final integrated contract

P5.8 validates rather than expands:
- all 24 lessons;
- all four age bands;
- journeys, prerequisites and full discovery;
- Draw With Me, Watch Then Draw and authored Trace;
- Help Ladder, Replay and generic Companion;
- P5.7 local adaptive recommendation + child-controlled adaptive Help;
- coloring resume → drawing resume → fresh precedence;
- lifecycle, process recovery and Save & Leave;
- Gallery, Coloring and Free Draw;
- Airplane Mode/offline core;
- privacy and permission boundaries;
- content quality 24 / 0 errors / six reviewed warnings.

Accepted coverage remains:
- Little 8;
- Creative 18;
- Growing 17;
- Young 10;
- difficulty D1/D2/D3/D4/D5 = 5/9/6/3/1.

## Immediate continuation

1. open draft PR for P5.8;
2. require contract/docs head CI GREEN;
3. implement only the integrated final regression matrix needed by the P5.8 contract;
4. require pre-freeze head CI GREEN;
5. cut `0.5.0-curriculum-expansion`, versionCode 27;
6. capture immutable APK/content/permission evidence;
7. physically test the exact final profile APK;
8. acceptance docs + exact-head CI;
9. ready → squash merge → merged-main CI;
10. close #88 and mark Phase 5 COMPLETE.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #88
5. `docs/10-execution/P5_8_EXECUTION_CONTRACT.md`
6. P5.8 PR once opened.
