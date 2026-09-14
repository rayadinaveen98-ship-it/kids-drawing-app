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
**Current P5.7 state:** **BATCH A + B VERIFIED GREEN; BATCH C CHILD-CONTROLLED ADAPTIVE HELP IMPLEMENTED; BATCH-C CI PENDING**  
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
- real branch head `291587a767cd5eab99891d05feb711558af5e64d` before Batch C;
- adaptive fresh primary carries its explainable reason into the Home hero only;
- browse cards remain baseline/generic and resume progress copy still wins;
- Android CI #560 / run `34859897678`: **GREEN**.

## Batch C — child-controlled adaptive Help

Implemented for the next exact-head gate:
- pure `AdaptiveHelpSuggestionPolicy` runs only after a child Help request;
- choices are limited to the next authored Help entry or already-authored Replay;
- missing/corrupt/incompatible adaptive state preserves original `RequestHelp` order;
- Little Artist keeps authored Help order rather than adaptive replay substitution;
- bounded prior Help-kind/choice summaries may occasionally suggest Replay for older bands;
- replay choice is itself counted so the policy returns to authored Help instead of looping;
- no Trace can be invented; it is reachable only when the current step authors it;
- `ProductAdaptiveHelpCoordinator` dispatches existing Lesson Engine commands and records only accepted actions;
- Help persistence failure cannot turn an accepted lesson command into failure;
- existing Companion presentation remains read-only and reacts to the resulting Lesson Engine state;
- Help button remains the explicit child initiation point.

## Immediate gate

1. Batch-C exact-head Android CI must be GREEN;
2. after Batch C green, run focused deterministic/offline/privacy/adaptive-policy fixtures across all four age bands;
3. only then freeze `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode **26**;
4. exact APK evidence + physical acceptance follow before PR #87 can become ready.

## Frozen architecture invariants

- `StudioPrimarySelectionPolicy`: coloring resume → drawing resume → fresh recommendation;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- `ChildProfile` remains explicit profile truth;
- `LessonSessionState` remains teaching-state truth;
- Companion remains read-only;
- adaptive state is advisory/local/bounded and cannot mutate artwork/session truth;
- no raw artwork/strokes, ability labels, grades/scores/rank/XP, punitive streaks, cloud profiling, analytics upload or network dependency.
