# P6.3 Progress Truth-Source Audit

Status: **FROZEN INPUT AUDIT**  
Issue: #96  
Parent epic: #91  
Branch: `phase6/p6-3-parent-progress`  
Baseline: `d518bd8fca3d45af6b33604e9f87f13798826142` — Phase-6 clean baseline after P6.2 closure, Android CI #605 GREEN

## Purpose

P6.3 replaces the Parent Zone Learning placeholder with a useful parent-facing progress/curriculum surface. This audit defines what the existing product can truthfully say **before** any new UI or storage is implemented.

The governing rule is simple:

> Parent Progress may project existing product truth, but it may not invent child history, infer ability, or silently turn advisory teaching signals into scores.

## 1. Existing local truth sources

### 1.1 Local adaptive state — completion and curriculum-exposure truth

Source:
- `app/src/main/java/com/navin/kidsdrawing/product/adaptive/LocalAdaptiveModels.kt`
- `LocalAdaptiveState`
- `AdaptiveEvent.LessonCompleted`
- `LocalAdaptiveReducer`
- `AtomicLocalAdaptiveStateStore`
- `LocalAdaptiveStateRepository`

Persisted fields relevant to P6.3:
- `completedLessons: List<AdaptiveLessonIdentity>`;
- `recentCompletions: List<AdaptiveLessonIdentity>`;
- `skillExposureCounts: Map<String, Int>`;
- `helpRequestCounts: Map<String, Int>`;
- bounded processed-event keys/revision metadata.

Important existing safety properties:
- local-only;
- bounded;
- deterministic reducer;
- versioned/corruption-tolerant storage;
- no artwork or raw stroke payload;
- no free-form child text;
- no account/cloud/device identifier;
- no scores, grades or inferred ability labels.

Completion semantics are concrete. `ProductGalleryRuntime.recordAdaptiveCompletion()` emits `AdaptiveEvent.LessonCompleted` only after `ArtworkCompletionResult.Saved`. Therefore the adaptive completed-lesson list represents a genuine lesson completion that crossed the product's successful save/completion boundary; it is not inferred from screen visits, step position or elapsed time.

Reducer semantics:
- `completedLessons` records each exact lesson identity once while within the current bound;
- `recentCompletions` is an ordered, bounded recent-completion sequence and can move a repeated completion back to the recent tail;
- `skillExposureCounts` increments on first completion of a lesson identity from authored lesson skill IDs;
- repeated completions do not inflate first-completion skill counts for the same identity;
- adaptive state has **no wall-clock completion timestamp**.

### 1.2 Gallery catalog — timestamped saved-artwork activity truth

Sources:
- `app/src/main/java/com/navin/kidsdrawing/gallery/domain/GalleryModels.kt`
- `GalleryArtworkRecord`
- `GalleryRepository.listArtwork()`

A Gallery record persists:
- entry/document identity;
- title;
- source (`LESSON` or `FREE_DRAW`);
- lesson ID/revision provenance for lesson artwork;
- completion kind (`DRAWING_ONLY` or `COLORED`);
- `completedAtEpochMillis`;
- preview status/reference.

Important semantics:
- Gallery timestamps are genuine saved-artwork timestamps;
- lesson artwork can be joined back to authored lesson metadata through lesson ID/revision;
- Free Draw activity remains clearly distinct from curriculum lessons;
- Gallery `Ready`, `Empty`, and `Unavailable` states already distinguish readable history from missing/corrupt index state;
- deleting a Gallery record does not mean the child never completed the lesson; adaptive completion and Gallery history are separate truths.

P6.3 may show exact dates/times for saved-artwork activity. It must **not** copy those timestamps onto adaptive completion entries that lack a matching Gallery record.

### 1.3 Authored lesson catalog — curriculum meaning

Source:
- `app/src/main/java/com/navin/kidsdrawing/lesson/model/LessonModels.kt`
- `LessonMetadata`

Authored metadata includes:
- age bands;
- difficulty;
- estimated minutes;
- category IDs;
- skill IDs;
- journey IDs;
- prerequisite lesson IDs;
- tags.

This is the authoritative source for descriptive parent curriculum language. P6.3 may say that a completed lesson **included/practiced** an authored skill or belonged to a category/journey. It may not convert that authored metadata into a prediction of child ability.

### 1.4 Lesson/coloring snapshots — active/resume truth only

Sources already consumed by `StudioHomeRepository`:
- `AtomicLessonSessionStore`;
- `AtomicColoringSessionStore`.

Current resume projections carry real `savedAtEpochMillis` values and semantic session state.

They are valid for wording such as:
- “Drawing in progress”;
- “Coloring in progress”;
- “Continue where you left off.”

They are **not historical completion records** and must not increase completed-lesson counts.

### 1.5 Existing Home/progression projection — prerequisite semantics

Sources:
- `StudioHomeRepository`;
- `StudioRecommendationPolicy`;
- `AdaptiveFreshRecommendationPolicy`.

The accepted progression rule already determines prerequisite eligibility from completed lesson IDs. P6.3 must reuse this semantic rather than create a second progression engine with different rules.

Browse/category/journey structures already expose:
- current release recommendations;
- categories;
- journeys;
- authored prerequisite ordering;
- active drawing/coloring resume context.

## 2. What P6.3 can truthfully derive

Without adding new persistent telemetry, a read-only parent projection can derive:

### Completion summary
- number of locally recorded completed lesson identities;
- number of those completions that resolve exactly into the current release catalog;
- recently completed lesson titles in genuine local completion order;
- repeated recent completion order without claiming a new unique completion.

### Curriculum context
For resolved completed lessons:
- categories explored;
- authored skills included/practiced;
- journeys encountered;
- journey lesson counts;
- completed lesson counts inside each current journey;
- next prerequisite-eligible lesson using existing completion semantics;
- current catalog lessons still available.

### Saved-artwork activity
From Gallery:
- saved artwork count;
- lesson vs Free Draw source;
- drawing-only vs colored completion kind;
- genuine saved timestamp;
- recent saved-artwork list.

### Active work
From existing session stores:
- active drawing resume;
- active coloring resume;
- saved-at timestamp and human-readable resume state.

## 3. What P6.3 must not derive

P6.3 must not generate or display:
- completion timestamps where only adaptive recent ordering exists;
- “time spent learning” from save timestamps;
- lesson/session visit counts unless explicitly persisted as a product truth later;
- ability/talent/IQ/readiness predictions;
- “behind/ahead for age” judgments;
- mastery percentages;
- grades/scores/ranks/XP;
- punitive streaks/inactivity warnings;
- sibling or peer comparison;
- raw stroke quality/performance metrics;
- inferred emotional/behavioral traits;
- hidden engagement scores;
- raw Help-request counts as a parent performance signal.

### Raw Help counts decision

`helpRequestCounts` remains an advisory teaching input and is **not parent-facing in P6.3**. Turning it into “needed help N times” would risk reframing a child-initiated support feature as a deficit metric.

## 4. Identity and catalog-evolution rules

Adaptive completion identity includes lesson ID + revision.

P6.3 must distinguish:
- **local completion truth** — all valid adaptive completed identities;
- **current curriculum completion** — completed identities that resolve exactly to the current release catalog;
- **prerequisite eligibility** — reuse the accepted product rule based on completed lesson IDs.

If an older completion cannot resolve into the current catalog:
- do not fabricate title/category/skill metadata;
- do not delete/reset it merely because the current catalog changed;
- do not count it as a current-catalog completed lesson;
- the projection may report a calm partial-history note if materially useful.

## 5. Missing/corrupt/incompatible data behavior

### Adaptive state
`LocalAdaptiveStateRepository.loadForPolicy()` currently collapses missing/corrupt/incompatible state to `null`, which is appropriate for recommendation fallback but not expressive enough for a parent progress surface.

P6.3 implementation should add a **read-only typed load result** (name may vary) that can distinguish at least:
- `Loaded(state)`;
- `Missing` — normal new/no-history state;
- `Unavailable` — corrupt or incompatible local progress state.

This addition must not change existing recommendation behavior or mutation semantics.

### Gallery
Reuse existing `GalleryListResult`:
- Ready;
- Empty;
- Unavailable.

### Catalog
Use current `LessonCatalog` snapshot and diagnostics. Invalid/unavailable lessons are never invented into progress.

### Partial truth
One unavailable source must not destroy the rest of the parent view. Examples:
- adaptive unavailable + Gallery ready → show saved-artwork activity and a calm completion-history-unavailable note;
- Gallery unavailable + adaptive ready → show completion/curriculum view and omit timestamped artwork history;
- no adaptive history + empty Gallery → genuine new-profile empty state.

## 6. Persistence decision

### Decision: no new P6.3 persistent analytics/history store

P6.3 V1 will build a dedicated read-only Parent Progress projection over accepted stores. It will **not** introduce a parallel analytics database, event stream or behavioral telemetry log.

Rationale:
- adaptive state already provides completion identity/order and curriculum exposure;
- Gallery already provides genuine saved-artwork timestamps;
- lesson/coloring stores already provide active/resume truth;
- catalog metadata already provides curriculum meaning;
- a new analytics store would duplicate authority and increase privacy/migration risk without being necessary for the P6.3 product goal.

A future requirement for timestamped lesson completion independent of Gallery would require a separate explicit contract/versioned migration. P6.3 does not silently add it.

## 7. Recommended P6.3 read model

Create a dedicated read-only projection such as `ParentProgressRepository` + pure model builder. Naming is implementation detail; the boundaries are not.

Suggested projection groups:
- `summary` — completed current lessons, saved artwork count, active-work count;
- `recentCompletions` — ordered resolved lesson titles, no fabricated timestamps;
- `recentSavedArtwork` — timestamped Gallery records with source/kind;
- `skillsPracticed` — authored skill IDs/titles derived from completed current lessons, expressed as exposure/practice, never mastery;
- `categoriesExplored` — authored category coverage from completed current lessons;
- `journeys` — total current lessons, completed current lessons, prerequisite-safe next available lesson;
- `activeWork` — drawing/coloring resume context;
- `dataAvailability` — source-specific ready/empty/unavailable state.

The projection must contain no raw artwork/strokes and must expose no mutator for lesson/gallery/adaptive truth.

## 8. UI truth boundaries

Safe examples:
- “4 completed lessons”
- “Recently completed: Friendly Cat”
- “Explored animals and shapes”
- “Curved lines appeared in 3 completed lessons”
- “2 of 4 lessons completed in this journey”
- “Next available: …”
- “Saved a colored lesson artwork on 15 September”
- “One drawing is still in progress”

Unsafe examples:
- “85% mastered”
- “Advanced artist”
- “Needs more help”
- “Behind for age”
- “Only practiced twice this week”
- “Top skill” when based on frequency as a quality judgment
- “Completed yesterday” if only adaptive order exists without a timestamp.

## 9. Frozen implementation direction

P6.3 implementation shall:
1. add a typed read-only adaptive-load path without changing policy behavior;
2. build a deterministic pure Parent Progress projection from current truth stores;
3. replace Parent Zone → Learning placeholder with the production read-only surface;
4. use no new persistent analytics/history store;
5. surface partial/unavailable data honestly;
6. preserve Parent Gate/session rules from P6.2;
7. preserve all Phase-5/P6.2 regression, content and permission gates;
8. cut a new monotonic QA version only after implementation CI is green.

This audit is the authoritative data-truth input for `P6_3_PARENT_PROGRESS_CONTRACT.md`.
