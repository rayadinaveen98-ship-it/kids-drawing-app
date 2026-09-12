# P3.3 Execution Contract — Production Guided Lesson Workspace

**Issue:** #45  
**Parent epic:** #42  
**Target milestone:** `0.3.0-vertical-slice`

## Objective

Replace the product lesson placeholders with a child-facing lesson preview and canvas-first guided workspace while preserving the physically verified Lesson Engine 0.2 + Drawing Engine 0.1 as the only lesson/artwork authorities.

Primary vertical-slice path:

Studio Lobby → Cute Cat preview → companion introduction → real teacher demonstration → child turn → Replay / Pace / Help → step progression → drawing completion → post-drawing choice.

All three verified teaching modes remain available because Cute Cat authors all three. Draw With Me remains the reference path for end-to-end QA.

## Runtime ownership

`ProductLessonRuntime` is an Android/product adapter over `LessonLabRuntimeCore`; it is **not** a new engine.

It deliberately reuses:
- the same bundled lesson package and loader;
- the same DrawingDocumentEngine / DrawingToolEngine;
- the same TeacherPlaybackSession;
- the same LessonSessionEngine;
- the same autosave and recovery coordinators;
- the same guide-overlay isolation model;
- the same persisted document/session directories and semantic IDs already physically verified in P2.

No Lesson Lab state is copied into product UI. Engineering diagnostics/lab controls remain separate.

## Preview contract

The production preview presents:
- authored lesson title/summary/time/difficulty/step count;
- profile-preferred teaching mode and pace as defaults;
- only authored supported teaching modes;
- all five verified teacher paces;
- compact companion introduction;
- one obvious Start Drawing action.

Changing preview mode/pace is presentation/input configuration. The session is not created until the child starts.

## Workspace hierarchy

Priority is frozen as:
1. canvas;
2. current teacher/guide action;
3. essential controls — Pause/Resume, Replay, Pace, Help;
4. compact companion presentation;
5. lesson-required child tool controls;
6. save/leave navigation.

The canvas receives the largest flexible region. No engineering diagnostics or invalid-command buttons appear in child UI.

## Child input gate

The low-level Drawing Surface remains mounted for rendering continuity, but product interaction is blocked unless the semantic lesson state is:
- `AwaitingChild`; or
- `HelpActive`.

Teacher demonstration, overview, paused, transition, error, and post-drawing phases consume input above the surface. This prevents child artwork from being committed while the teacher owns the turn.

Teacher and guide strokes continue to render only through `TeacherPlaybackOverlay` and never enter DrawingDocument history.

## Semantic companion

P3.3 introduces presentation states derived from Lesson Engine truth rather than timers:
- INTRODUCING_ACTIVITY;
- DEMONSTRATING;
- WATCHING_CHILD;
- HELPING;
- PAUSED;
- CELEBRATING_ARTWORK;
- GENTLE_ERROR;
- IDLE_PRESENT.

The presentation layer may change text/art later without changing engine state. Quiet child-turn copy is intentional. No per-stroke praise, shame, scores, streaks or failure faces.

## Valid controls only

Unlike Lesson Lab, product UI hides invalid engineering commands.

Examples:
- Replay only during a child turn;
- Skip Step only when authored `allowSkip` is true;
- Skip Overview only during Watch Then Draw overview;
- Help reduction/dismiss only while Help is active;
- Retry only for recoverable engine error;
- Done only during a child turn.

Typed engine rejection remains authoritative as a final safety boundary.

## Help

Help Ladder remains engine-owned. Product UI only presents semantic help level changes and the overlay produced by the verified guide factory.

Help copy must normalize assistance:
- hints are gentle;
- visual/anchor support is optional scaffolding;
- tracing is described as a normal way to learn;
- reducing/dismissing help is always available when authored state permits.

## Save, exit and lifecycle

Internal product “Save & leave” uses the runtime’s explicit document/session save while the semantic session remains resumable; product routing then returns Home. It does not mark a still-active creative session terminal merely because the child left the screen.

While the guided workspace is visible, Android `ON_STOP` invokes runtime background persistence.

On re-entry/process recreation:
- product route/mode/pace presentation state is saveable;
- runtime recovery loads the child document first, then semantic session, then recreates transient teacher/guide work;
- resume requests never silently create a new lesson if the saved session is absent;
- fresh start only occurs through the preview/start path.

## Post-drawing boundary

P3.3 may present the already-verified post-drawing choices, but P3.4 owns the real production coloring implementation and final drawing→coloring handoff. P3.3 must not fake a coloring canvas.

`Finish for now` remains a valid engine-owned terminal choice. Gallery ownership remains P3.5.

## Accessibility / age presentation

- essential controls use child-sized minimum targets;
- critical actions carry semantics/content descriptions;
- younger profiles inherit the larger Studio design language from P3.1/P3.2;
- companion remains compact in workspace and never owns the canvas;
- no instruction relies on voice alone.

## Acceptance evidence

P3.3 is complete when:
- Home fresh recommendation opens preview and starts real engine session;
- Home Continue Drawing recovers the persisted real session;
- profile mode/pace defaults populate preview;
- all authored modes remain selectable;
- teacher playback is frame-driven by the existing TeacherPlaybackSession;
- child strokes commit only during child/help turns;
- teacher/guide overlays remain isolated from child history;
- Help / Replay / Pace / Pause / Resume map to real engine commands;
- safe leave/background/relaunch preserves child work;
- companion presentation is semantic and non-blocking;
- policy tests cover valid-control exposure and help/companion mapping;
- prior engine/lab regression suites stay green;
- exact-head CI passes unit tests, lint, debug APK, instrumentation APK compile, profile APK and permission allowlist.
