# P5.7 Execution Contract — Local Adaptive Teaching

**Parent epic:** #73  
**Issue:** #86  
**Verified starting main:** `91bc7584994224852d3d44749e78749d39ff954b`  
**Verified starting merged-main CI:** Android CI #552 / run `34849748403` — **GREEN**  
**Branch:** `phase5/p5-7-local-adaptive-teaching`  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Expected first distributed QA:** `0.5.0-curriculum-expansion-p5.7-qa1`, versionCode **26**  
**Status:** **LOCKED BEFORE IMPLEMENTATION**

## 1. Purpose

P5.7 adds a deterministic, offline adaptation layer that helps the existing studio choose a more useful fresh lesson and offer a more useful child-requested Help suggestion. It does **not** create a second lesson engine, an AI evaluator, an analytics system, or a child ability profile.

The same authored catalog + child profile + local adaptive state must always produce the same result. Adaptive policy remains explainable and bounded.

## 2. Frozen product ownership

P5.7 must preserve these accepted boundaries:

- `StudioPrimarySelectionPolicy` remains the authority for Home primary precedence: active coloring resume → drawing resume → fresh recommendation.
- `StudioRecommendationPolicy` remains the product recommendation surface; P5.7 may extend its inputs/policy but must not create competing UI-owned ranking truth.
- `ChildProfile` remains the source of explicit age, interests, teaching-mode and pace preferences.
- `LessonSessionState` remains the only teaching-state truth.
- `LessonSessionSnapshot` remains recovery/session truth; adaptive state never replaces or rewrites it.
- P5.3 Companion remains generic/read-only relative to session, Help, artwork and persistence.
- authored lesson metadata remains authoritative for age bands, prerequisites, difficulty, skills, journeys, categories and supported modes.
- teacher/help/reference overlays never enter child artwork.
- Drawing, Lesson, Coloring, Gallery and Free Draw ownership remain unchanged.

No lesson-ID-specific adaptive runtime/UI branches are allowed.

## 3. Owned adaptive layer

P5.7 introduces one owned local package under `product/adaptive`.

It may contain:

- immutable adaptive-state models;
- a versioned local store;
- an idempotent event reducer;
- a deterministic lesson-progression projection;
- explainable fresh-recommendation policy inputs/reasons;
- a deterministic child-controlled Help suggestion policy.

It must **not** contain artwork, raw strokes, rendered images, similarity metrics, grades, talent judgments, free-form behavioral logs, cloud identifiers or network clients.

The adaptive layer is advisory product state only. It cannot directly issue Lesson Engine commands or mutate artwork/history.

## 4. Local adaptive state contract

The first persisted format is `formatVersion = 1`.

State is bounded and designed around the current single local completed child profile. It may contain only:

- completed lesson identities/revisions needed for prerequisite/progression decisions;
- bounded first-completion skill exposure counts derived from authored lesson skill IDs;
- a bounded recent-completion list used only for fresh-pick repetition avoidance;
- bounded child-requested Help summaries keyed by authored skill/category/help-kind information;
- optional store revision/update metadata required only for atomic persistence/recovery.

### 4.1 No hidden child identity

P5.7 does not introduce account IDs, advertising IDs, device fingerprints or cloud/profile identifiers.

Because V1 currently owns one local profile, adaptive state belongs to that local profile lifecycle. If the completed child profile is replaced/reset incompatibly, the adaptive state must reset safely rather than attempt cross-profile inference.

### 4.2 Bounds

Implementation must lock finite limits before persistence:

- recent-completion history is small and bounded;
- counters use capped non-negative integers;
- unknown future enum/value data is rejected or safely ignored according to the store format contract;
- no unbounded event log is stored.

## 5. Adaptive events and idempotence

Adaptive state changes only from explicit local product events that already have product meaning.

Required event families:

1. `LessonCompleted`
   - only when a lesson is genuinely completed;
   - carries stable lesson identity/revision, session/document completion key, authored skill/category/journey metadata and difficulty needed by the reducer;
   - first-completion skill exposure must not double-count because of relaunch/recovery/repeated delivery.

2. `HelpRequested`
   - emitted only after the child explicitly requests authored Help/replay through the existing lesson product flow;
   - carries stable lesson identity plus bounded authored skill/category/help-kind context;
   - never carries strokes, timing traces, coordinates, correctness or quality estimates.

Reducer requirements:

- deterministic;
- idempotent for duplicate delivery of the same logical completion/help event where an event key exists;
- tolerant of stale lesson revisions without corrupting current state;
- no clock-based ranking randomness;
- no implicit state changes merely because Home was opened.

## 6. Fresh recommendation eligibility

Resume precedence is evaluated **before** adaptive fresh ranking and is frozen.

For fresh adaptive primary suggestions:

- an authored prerequisite that is not satisfied prevents that lesson from becoming the adaptive primary fresh suggestion;
- browse/category/journey discovery remains transparent; P5.7 does not globally hide catalog content merely because a prerequisite is unmet;
- exact age-band content is preferred and is the normal adaptive pool;
- the existing safe age fallback behavior may be used only when no exact-age release candidate can satisfy the fresh-primary contract;
- unsupported teaching modes continue to use the existing authored-mode fallback rules;
- corrupt/missing adaptive state falls back to the verified P5.6 non-adaptive ranking safely.

Adaptation never unlocks invalid content.

## 7. Deterministic fresh-ranking contract

P5.7 uses a lexicographic policy, not an opaque numeric ability score.

After eligibility filtering, fresh candidates are ordered by these policy dimensions in order:

1. exact age fit;
2. coherent journey continuation when the preceding required work is completed and the next authored journey lesson is available;
3. useful underexposed/new authored skill opportunity;
4. explicit child-interest match;
5. preferred teaching-mode compatibility;
6. sensible difficulty proximity for the age band and recent completed curriculum context, without forced upward progression or demotion labels;
7. bounded recent-repeat avoidance;
8. stable `lessonId` then revision tie-break.

A completed lesson should not dominate fresh recommendations while uncompleted eligible content exists. Completed lessons remain available through browse/repeat and may re-enter only as an explicit fallback when no better fresh candidate exists.

The policy must remain deterministic for identical inputs.

## 8. Explainable recommendation reasons

Recommendation output may expose one or more stable reason codes such as:

- continue a journey;
- try a new skill;
- matches an explicit interest;
- good age fit;
- matches preferred lesson style/mode;
- repeat/familiar fallback.

Reasons are descriptions of policy facts. They must never say or imply:

- “you are weak/advanced”;
- “you failed”; 
- a score/rank/grade;
- hidden confidence/talent level;
- predicted artistic ability.

UI copy remains age-appropriate and non-judgmental.

## 9. Child-controlled adaptive Help

P5.7 may suggest which **existing authored Help/replay option** is likely to be most useful after the child asks for Help.

Rules:

- the child must initiate Help;
- adaptive policy cannot automatically open/escalate Help because of inferred struggle;
- authored Help Ladder bounds remain authoritative;
- Trace cannot be invented for a lesson/step that does not author it;
- the suggestion may use current step, current authored Help options, age band and bounded prior child-requested Help summaries;
- it may choose/recommend an authored gentle hint, visual guide, direction anchor, replay or other authored option that already exists;
- it cannot skip required lesson work, mark completion, change artwork, change pace/mode silently, or mutate session truth;
- if adaptive Help state is missing/corrupt, existing authored Help order is used unchanged.

Companion may explain the suggestion but remains read-only.

## 10. Persistence / corruption / migration

Adaptive persistence must be local and atomic/versioned.

Requirements:

- state survives process death/relaunch;
- missing state means empty/default adaptive state;
- corrupt state is quarantined/ignored and product falls back safely;
- unsupported future format is rejected safely without crashing;
- writes are atomic enough to avoid partial-state corruption;
- no migration may fabricate completion/help history;
- deleting/resetting the local child profile must have a deterministic adaptive-state reset path;
- no network access is introduced.

## 11. Privacy and child-safety boundary

P5.7 stores only the minimum bounded facts required for local adaptation.

Forbidden persisted data includes:

- stroke paths/coordinates;
- artwork images or document payloads;
- audio/video;
- free-form child text;
- duration/latency telemetry used as performance judgment;
- hidden engagement analytics;
- advertising/device identifiers;
- cloud sync tokens;
- inferred ability labels.

Adaptive state is not analytics and must never be uploaded by Phase 5.

## 12. Test contract

Automated tests must prove at minimum:

- identical profile/catalog/adaptive-state input yields identical ranking and Help suggestion output;
- resume precedence remains coloring > drawing > fresh recommendation;
- unmet prerequisite content cannot become adaptive primary fresh suggestion;
- exact-age candidates dominate safe fallback candidates when eligible exact-age content exists;
- journey continuation behaves deterministically;
- underexposed/new skills can influence fresh ordering without ability labels;
- explicit interests still influence recommendations;
- recent-repeat avoidance is bounded and deterministic;
- completed lessons do not dominate while eligible uncompleted lessons exist;
- fallback to the P5.6 baseline recommendation policy occurs for missing/corrupt/incompatible adaptive state;
- completion events are idempotent for skill exposure;
- adaptive-state bounds/counter caps hold;
- HelpRequested stores no stroke/quality data;
- adaptive Help never fires without child request;
- Help suggestion never exceeds authored options or invents Trace;
- Companion remains read-only;
- Airplane/offline operation remains complete;
- existing 24-lesson content quality stays 24 / 0 errors / six reviewed warnings;
- permission allowlist does not expand.

## 13. Delivery batches

### Batch A — Adaptive state foundation

Deliver:
- `product/adaptive` models;
- versioned local store;
- idempotent reducer;
- completion/help event contracts;
- corruption/default/reset behavior;
- unit tests for bounds, determinism, idempotence and privacy shape.

No Home ranking behavior changes before Batch A exact-head CI is GREEN.

### Batch B — Progression-aware fresh recommendations

Deliver:
- adaptive projection into Home;
- prerequisite-aware fresh-primary eligibility;
- journey/new-skill/interest/mode/difficulty/repeat lexicographic ordering;
- explainable reason codes/copy projection;
- preserve StudioPrimarySelectionPolicy resume precedence;
- baseline fallback for missing/corrupt adaptive state;
- regression tests across all age bands.

Require exact-head CI GREEN before Help integration.

### Batch C — Child-controlled adaptive Help

Deliver:
- generic Help suggestion policy over existing authored Help options;
- integration with existing product/Companion presentation without changing session ownership;
- HelpRequested reducer integration;
- no automatic escalation;
- fallback to authored Help order;
- cross-age/maturity regression tests.

Require exact-head CI GREEN before QA freeze.

## 14. QA / milestone contract

Before QA freeze:

- run deterministic adaptive-policy fixtures across all four age bands;
- verify resume precedence, prerequisite gating, journey continuation, new-skill/interest ordering, repeat avoidance and baseline fallback;
- verify Help remains child-controlled and authored;
- verify adaptive store corruption/reset paths;
- verify the app is fully usable in Airplane Mode;
- verify no new sensitive permission/network dependency.

First distributed candidate:
- versionName `0.5.0-curriculum-expansion-p5.7-qa1`;
- versionCode **26**;
- never reuse versionCode 25;
- versionCode is cut only at QA freeze, not for contract/intermediate batches.

Any binary-changing defect after QA freeze invalidates the candidate and requires versionCode >26 with new evidence.

## 15. Physical acceptance scope

The accepted QA profile APK must physically prove:

- all four age bands receive sensible deterministic fresh suggestions;
- active resume continues to outrank adaptation;
- explicit interest changes affect fresh suggestions transparently;
- completed/prerequisite/journey state affects fresh suggestions as contracted;
- repeated Home launches with unchanged state remain stable;
- Help stays child-invoked and authored;
- adaptive state survives relaunch and corruption fallback is safe;
- profile reset/replacement does not leak old adaptive state;
- Airplane Mode works end-to-end;
- P5.6 lesson, drawing, Gallery, coloring and Free Draw behavior does not regress.

## 16. Definition of done

P5.7 is complete only when:

- the adaptive layer is owned, local, bounded, deterministic and versioned;
- no artwork/strokes/ability labels are persisted;
- Home resume precedence is unchanged;
- fresh recommendations are progression-aware, prerequisite-safe and explainable;
- adaptive Help remains child-controlled and limited to authored options;
- missing/corrupt state falls back safely;
- offline/privacy/permission gates pass;
- exact-head CI is green for each batch;
- an exact versionCode 26+ QA APK is physically accepted;
- acceptance evidence passes CI;
- the slice is squash-merged and merged-main CI is green before issue #86 closes.
