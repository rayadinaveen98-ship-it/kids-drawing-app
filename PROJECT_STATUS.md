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
**Current P5.7 state:** **BATCH A VERIFIED GREEN; BATCH B IMPLEMENTED; ADAPTIVE PRIMARY REASON PATCH APPLIED; EXACT-HEAD CI PENDING**  
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
- contract/docs head `19e043eda7f2046df18cec6d1bd8fee02f4b33b9`.
- contract/docs Android CI #553 / run `34851042117`: **GREEN**.

## P5.7 Batch A

Batch A adaptive-state foundation commit: `2b14ad149ce054284b81a1a6d95327054f9f629f`.

Implemented under `product/adaptive`:
- bounded/versioned local adaptive-state model;
- completed lesson identities/revisions;
- first-completion skill exposure counts;
- bounded recent-completion history;
- bounded authored Help-context request counters;
- bounded idempotency keys;
- explicit `LessonCompleted` + child-requested `HelpRequested` events;
- deterministic/idempotent pure reducer;
- atomic local store with backup recovery;
- explicit Missing / Corrupt / Incompatible outcomes;
- deterministic reset path for profile replacement;
- codec/privacy tests proving no artwork/stroke/score/ability/device/cloud payload shape.

Additional foundation safety fix:
- incompatible future-format primary state never falls back to and overwrites an older backup;
- fix commit `95ac76dac95f663484cfc357c5673bd7b8ab424d`;
- Android CI #557: **GREEN**.

## P5.7 Batch B

Adaptive fresh-recommendation implementation commit: `76f6b64f540e579f94af4265cc8084638a7055d2`.

Implemented:
- prerequisite-safe adaptive fresh-primary eligibility;
- exact-age fresh-first pool with safe fresh fallback before repeats;
- deterministic journey continuation / underexposed-skill / interest / mode / difficulty / repeat ordering;
- successful Gallery lesson completion emits bounded `LessonCompleted` advisory events;
- adaptive persistence failure cannot fail Gallery completion;
- browse/category/journey projections remain baseline/full-catalog;
- resume precedence remains coloring → drawing → fresh;
- Android CI #558: **GREEN**.

Adaptive reason presentation patch:
- primary fresh recommendation now carries the adaptive explanation copy into the existing Home hero;
- browse cards remain baseline/generic;
- resume progress copy still outranks adaptive copy;
- JVM regression tests cover adaptive-visible, baseline-generic and blank-fallback behavior;
- exact-head CI required before Batch C.

## Immediate gate

1. adaptive-reason patch exact-head Android CI must be GREEN;
2. only then begin Batch C child-controlled adaptive Help;
3. Batch C must attach to genuine child `RequestHelp` flow and may only suggest existing authored Help/replay options;
4. versionCode **26** remains reserved for the first P5.7 distributed QA freeze.

## Frozen architecture invariants

- `StudioPrimarySelectionPolicy`: coloring resume → drawing resume → fresh recommendation;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- `ChildProfile` remains explicit profile truth;
- `LessonSessionState` remains teaching-state truth;
- Companion remains read-only;
- adaptive state is advisory/local/bounded and cannot mutate artwork/session truth;
- no raw artwork/strokes, ability labels, grades/scores/rank/XP, punitive streaks, cloud profiling, analytics upload or network dependency.
