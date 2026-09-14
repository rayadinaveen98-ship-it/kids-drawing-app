# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Parent epic:** #73  
**Latest completed slice:** P5.6 — Curriculum Expansion Set E #84  
**Current slice:** P5.7 — Local Adaptive Teaching #86  
**Active branch:** `phase5/p5-7-local-adaptive-teaching`  
**Draft PR:** #87  
**Current P5.7 state:** **BATCH A/B/C + CROSS-AGE PRE-QA VERIFIED GREEN; QA1 VERSIONCODE 26 FROZEN; EXACT-HEAD CI / PHYSICAL ACCEPTANCE PENDING**  
**Current production catalog:** **24 release lessons**  
**Verified starting main:** `91bc7584994224852d3d44749e78749d39ff954b`  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## P5.6 frozen baseline

- PR #85 squash-merged at `91bc7584994224852d3d44749e78749d39ff954b`.
- merged-main Android CI #552 / run `34849748403`: **GREEN**.
- issue #84 closed completed.
- catalog remains **24 production lessons**.
- P5.6 Content Lab 16/16 PASS + exact-profile physical 36/36 PASS.

## P5.7 contract

- issue #86.
- branch `phase5/p5-7-local-adaptive-teaching`.
- draft PR #87.
- contract-first commit `5225e82bcae4afae7a0d5dc65bacd1a5054d4b19`.
- contract/docs Android CI #553 / run `34851042117`: **GREEN**.

## Batch A — adaptive state foundation

- foundation commit `2b14ad149ce054284b81a1a6d95327054f9f629f`;
- bounded/versioned local state + deterministic/idempotent reducer + atomic store + corruption/reset/privacy tests;
- future-format preservation fix `95ac76dac95f663484cfc357c5673bd7b8ab424d`;
- Android CI #557: **GREEN**.

## Batch B — progression-aware fresh recommendations

- implementation commit `76f6b64f540e579f94af4265cc8084638a7055d2`;
- prerequisite-safe adaptive fresh primary;
- exact-age fresh-first with safe fresh fallback before repeats;
- deterministic journey / new-skill / interest / mode / difficulty / repeat ordering;
- successful Gallery lesson completion records bounded completion events;
- browse/category/journey remain baseline/full-catalog;
- resume precedence remains coloring → drawing → fresh;
- Android CI #558: **GREEN**.

Adaptive primary-reason completion:
- branch head `291587a767cd5eab99891d05feb711558af5e64d` before Batch C;
- adaptive fresh primary carries its explainable reason into the Home hero only;
- browse cards remain baseline/generic and resume progress copy still wins;
- Android CI #560 / run `34859897678`: **GREEN**.

## Batch C — child-controlled adaptive Help

- implementation commit `803286d95191c5a955b0ad5f82d09b993363400a`;
- adaptive Help runs only after explicit child Help action;
- choices are limited to the next authored Help entry or already-authored Replay;
- missing/corrupt/incompatible adaptive state preserves original `RequestHelp` order;
- Little Artist keeps authored Help order;
- bounded older-band history may choose authored Replay without replay loops;
- Trace cannot be invented;
- Lesson Engine remains teaching-state authority and Companion remains read-only;
- Android CI #561 / run `34861064836`: **GREEN**.

## Cross-age pre-QA gate

- test checkpoint `c828d4bf801ddaf037b9b5b2b102588d7a0c6be8`;
- deterministic fixtures cover all four age bands and non-judgmental reason copy;
- Android CI #562 / run `34861549191`: **GREEN**.

## QA1 freeze

The contract now permits the first distributed P5.7 candidate:

- versionName `0.5.0-curriculum-expansion-p5.7-qa1`;
- versionCode **26**;
- P5.7-specific CI debug/profile artifact packaging;
- exact release-like profile APK is the only binary eligible for physical acceptance;
- `docs/10-execution/P5_7_QA.md` owns the exact evidence and focused physical checklist.

Any binary/content-changing defect after this freeze requires versionCode **>26** and new evidence.

## Immediate gate

1. QA1 versionCode 26 exact-head Android CI must be GREEN;
2. capture exact debug/profile artifact IDs, sizes and SHA256 values from that run;
3. distribute the exact profile APK for physical acceptance;
4. do not mark acceptance until the tester reports results for that exact binary;
5. after physical PASS, commit acceptance evidence, require acceptance-head CI GREEN, ready PR #87, squash merge, then require merged-main CI GREEN before closing #86.

## Frozen architecture invariants

- `StudioPrimarySelectionPolicy`: coloring resume → drawing resume → fresh recommendation;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- `ChildProfile` remains explicit profile truth;
- `LessonSessionState` remains teaching-state truth;
- Companion remains read-only;
- adaptive state is advisory/local/bounded and cannot mutate artwork/session truth;
- no raw artwork/strokes, ability labels, grades/scores/rank/XP, punitive streaks, cloud profiling, analytics upload or network dependency.
