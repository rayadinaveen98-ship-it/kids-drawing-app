# P3.2 Execution Contract — Personalized Studio Lobby

**Issue:** #44  
**Parent epic:** #42  
**Target milestone:** `0.3.0-vertical-slice`

## Objective

Replace the P3.1 Home placeholder with the first production-realistic Studio Lobby while keeping Home as a read-only presentation layer over durable profile, authored lesson content, and Lesson Engine persistence truth.

The selected child journey for this slice is:

completed onboarding → personalized Studio Lobby → Continue Drawing when a resumable session exists, otherwise Draw Together → Picked for You → reserved Art Journey / Free Draw / Gallery / Grown-ups routes.

## Truth ownership

P3.2 does **not** create a Home-owned lesson/session state machine.

- `ChildProfileStore` owns completed local profile/preferences.
- bundled lesson package metadata owns lesson age bands, categories, tags, skills, journey membership, time, difficulty and supported modes.
- `AtomicLessonSessionStore` owns the persisted semantic lesson snapshot.
- `StudioHomeRepository` creates a read-only projection from those sources.
- `StudioHomeScreen` renders the resulting model and owns only ephemeral navigation/presentation state.

A corrupt/missing session snapshot does not become a guessed Continue Drawing state. A content-load failure does not destroy profile or artwork state.

## Continue Drawing rule

Continue Drawing is shown only when the stored semantic snapshot:

1. matches the bundled lesson ID and revision;
2. has a persisted mode and pace;
3. is not `READY`;
4. is not terminal `FINISHED`.

When present, Continue Drawing outranks starting a new recommendation in the primary hero. The projection carries the authoritative session ID, child-document ID, phase, mode, pace and authored step position for the P3.3 production workspace to consume.

P3.2 intentionally reads the physically verified P2 compatibility directory/IDs (`lesson-lab-sessions`, Cute Cat session/document IDs) so previously verified saved work remains discoverable. P3.3 may rename the Android wrapper/product surface, but must preserve or migrate these persistence identifiers explicitly rather than silently orphaning work.

## Recommendation policy

With the single bundled V1 reference lesson, recommendation is deterministic rather than pretending to provide a broad ranking system.

Priority of explanation:

1. child-interest match against authored categories/tags;
2. authored age-band match;
3. gentle starter fallback.

The child's preferred teaching mode is used when the lesson supports it; otherwise the first authored supported mode is selected. Preferred pace remains the child profile default.

The policy is pure Kotlin and separately tested so future lesson-library ranking can expand without moving recommendation truth into Compose.

## Age-adaptive Home presentation

### 4–5 — Little Artist
- spacious hero and route cards;
- no difficulty or skill metadata;
- secondary routes remain one-per-row;
- shortest greeting/copy.

### 6–7 — Creative Explorer
- spacious cards;
- difficulty may appear;
- skill vocabulary remains hidden;
- secondary routes remain one-per-row.

### 8–9 — Growing Artist
- balanced density;
- difficulty + compact skill context;
- secondary routes may use two columns.

### 10–12 — Young Artist
- compact-but-calm density;
- difficulty + skill/mode context;
- two-column secondary cards where useful.

These policies implement the frozen UX/visual contracts; they are not just recommendation differences.

## Visual hierarchy

1. short personalized greeting + compact studio/companion presence;
2. single large Continue Drawing / Draw Together hero;
3. Picked for You;
4. Art Journey and Free Draw;
5. Gallery;
6. quiet Grown-ups entry.

The screen must feel like a warm studio lobby, not a feed/dashboard. Product chrome stays restrained so authored/child artwork can become the richest visual content.

## Slice boundaries

P3.2 owns the Home projection and navigation intent, not the destination implementations.

- lesson preview / resume route is completed by P3.3;
- vertical-slice coloring by P3.4;
- Gallery by P3.5;
- full parent-zone implementation is outside this slice;
- Free Draw productization remains outside this vertical-slice milestone.

Placeholder destination surfaces must remain child-facing and must not expose internal phase/issue/version terminology.

## Offline and safety

- no network or account requirement;
- no new permissions;
- no behavioral analytics;
- no outward sharing from child Home;
- quiet Grown-ups route only; destructive/export behavior remains parent-owned later;
- failure to load a recommendation displays a calm recoverable Home state.

## Acceptance evidence

P3.2 is complete when:

- onboarding completion/relaunch reaches the real Studio Lobby;
- recommendation uses authored Cute Cat metadata and profile mode/pace;
- interest/age/starter recommendation behavior is deterministic and unit tested;
- real persisted semantic session state surfaces Continue Drawing;
- Continue Drawing outranks fresh-start recommendation;
- age-band presentation policies are test-covered;
- Home accessibility semantics cover primary lesson and Grown-ups actions;
- process recreation preserves current product route without moving session truth into UI;
- existing Drawing/Lesson/Quality labs and engine suites remain green;
- lint, unit tests, instrumentation APK compile, debug APK, profile APK and permission allowlist are green on the exact PR head.
