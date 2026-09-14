# Project Handoff

**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
Git is authoritative when chat and repository state disagree.

## Current state

- Phase 0–4 — COMPLETE/frozen.
- Phase 5 / `0.5.0-curriculum-expansion` — ACTIVE.
- Parent epic: #73.
- P5.1–P5.6 — COMPLETE/frozen.
- P5.7 Local Adaptive Teaching #86 — **implementation + QA1 + physical acceptance complete; merge closure pending**.
- branch: `phase5/p5-7-local-adaptive-teaching`.
- PR #87 remains draft until acceptance-head CI is GREEN.
- production catalog remains **24 release lessons**.

## P5.7 accepted QA

Exact binary candidate:
- commit `e258632e83e83a39ac649855ea19592c2f5003ae`;
- `0.5.0-curriculum-expansion-p5.7-qa1`;
- versionCode **26**;
- Android CI #563 / run `34866700627`: **GREEN**;
- content quality **24 / 0 errors / 6 reviewed warnings**;
- permission allowlist **PASS**;
- profile artifact `10357367072`;
- profile APK `16,344,676` bytes;
- profile SHA256 `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`.

Physical acceptance:
- date: 2026-09-14;
- **45/45 PASS**;
- device model/API: **not provided by tester**;
- reported defects: none.

Acceptance evidence lives in:
- `docs/10-execution/P5_7_QA.md`;
- `docs/10-execution/P5_7_ACCEPTED_QA.md`.

## P5.7 frozen product behavior

- Home resume precedence stays coloring → drawing → fresh.
- Fresh adaptive recommendations are deterministic, prerequisite-safe and explainable.
- Browse/category/journey discovery stays transparent/full-catalog.
- Adaptive state is local, versioned, bounded, corruption-safe and idempotent.
- Child Help is still explicitly initiated and can only use existing authored Help/Replay.
- Trace is never invented.
- Companion remains read-only relative to artwork/session truth.
- No raw artwork/strokes, score/grade/rank/ability labels, analytics upload, cloud child profile or network dependency.

## Immediate continuation

1. require exact-head Android CI GREEN on the acceptance-doc state;
2. mark PR #87 ready;
3. squash merge exact acceptance head;
4. require merged-main Android CI GREEN;
5. close issue #86 completed;
6. start P5.8 from that exact verified main with a contract-first commit.

## P5.8 target

P5.8 is the final Phase-5 cross-age release gate for `0.5.0-curriculum-expansion` across:
- all 24 lessons;
- all four age bands;
- journeys/prerequisites;
- Companion + authored Help;
- local adaptive teaching;
- offline behavior;
- lifecycle/recovery;
- Gallery/Coloring/Free Draw;
- content-quality + permission boundaries;
- exact final release APK evidence and physical acceptance.

## Resume protocol

Read in order:
1. `PROJECT_STATUS.md`
2. this file
3. `ROADMAP.md`
4. issue #86 / PR #87 until P5.7 merge closure completes
5. `docs/10-execution/P5_7_QA.md`
6. after merge, P5.8 issue/contract becomes authoritative.
