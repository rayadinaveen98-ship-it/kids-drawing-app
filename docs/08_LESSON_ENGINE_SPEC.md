# 08 — Lesson Engine V1 Contract

**Status:** Phase 0.5 implementation contract

## 1. Responsibility

Lesson Engine is the authoritative coordinator for a guided lesson session.

It owns:
- loading/validating a lesson package;
- selected teaching mode and pace;
- current drawing step;
- teacher demonstration orchestration;
- child-turn boundaries;
- Help Ladder level;
- pause/resume/replay transitions;
- skip permission;
- progress snapshot/resume;
- drawing-complete → coloring/finish transition;
- semantic events for companion/narration/UI.

It does **not** render strokes, draw UI, persist raw drawing points itself, or own companion animations.

## 2. Engine boundaries

```text
Feature UI
   │ commands / state
   ▼
Lesson Engine
   ├── Drawing Engine      teacher playback + child document/session
   ├── Narration Service   speak/stop semantic text
   ├── Companion Engine    semantic companion signals
   ├── Session Store       lesson progress snapshots
   └── Coloring Engine     handoff after drawing completion
```

Only stable product/domain commands and events cross boundaries. AndroidX Ink types never enter Lesson Engine.

## 3. Teaching mode semantics

### `draw_with_me`
Interactive alternating mode.

For every lesson step:
1. prepare step;
2. teacher demonstrates the current step;
3. child gets their turn;
4. child may replay/help/pause/change pace;
5. child confirms/completes the step;
6. continue to next step.

This is the default tutoring mode.

### `watch_then_draw`
Observation-first mode.

1. At lesson start, teacher plays a non-destructive overview demonstration of all lesson drawing steps in sequence.
2. Child then begins the drawing pass.
3. During the drawing pass, each step presents the child objective; teacher demonstration is available through Replay rather than automatically repeating unless the lesson policy requests it.
4. Help remains available.

The full preview uses teacher overlays only and never writes into the child artwork.

This interpretation preserves the user's original intent: **watch first, then draw**.

### `trace_and_learn`
High-assistance mode.

For every step:
1. teacher demonstrates the step;
2. child turn begins with the authored trace/guide visible when available;
3. child follows the guide;
4. Help Ladder can add direction/assisted-success behavior if needed;
5. guide remains an overlay and is never saved into child artwork.

If a lesson declares Trace & Learn support, required trace/guide references must validate for the steps that depend on them.

## 4. Session state model

Conceptual sealed state hierarchy:

```text
Uninitialized
Loading
Ready
OverviewDemonstrating            # watch_then_draw only
Drawing
  ├── PreparingStep
  ├── TeacherDemonstrating
  ├── AwaitingChild
  ├── HelpActive
  └── CompletingStep
Paused(previousStableState)
DrawingComplete
AwaitingPostDrawingChoice
HandingOffToColoring
Finished
RecoverableError
FatalContentError
```

`Drawing` also carries:
- step index / stable step ID;
- active Help Ladder level;
- selected mode;
- selected pace;
- replay status;
- child-turn progress signal if relevant.

A state transition is engine-owned. Screens observe state; screens do not set arbitrary states.

## 5. Commands

Conceptual commands accepted from UI/product:

### Session
- `LoadLesson(lessonId)`
- `StartLesson(mode, pace)`
- `Pause`
- `Resume`
- `SaveAndExit`

### During drawing
- `ReplayDemonstration`
- `SetPace(pace)`
- `RequestHelp`
- `ReduceHelp` / `DismissHelp` where allowed
- `MarkChildTurnDone`
- `SkipStep` only if authored as allowed

### Post drawing
- `ChooseColorWithMe`
- `ChooseColorMyself`
- `FinishForNow`

Invalid commands are rejected/ignored through typed result semantics; they never force impossible state transitions.

## 6. Events / effects

State is persistent/observable. One-shot side effects are emitted separately.

Examples:
- `NarrationRequested(key, priority)`
- `NarrationStopRequested`
- `CompanionSignal(TEACHING)`
- `CompanionSignal(WATCHING_CHILD)`
- `CompanionSignal(HELPING)`
- `CompanionSignal(CELEBRATING)`
- `TeacherPlaybackRequested(sequence, pace)`
- `TeacherPlaybackCancelRequested`
- `GuideOverlayRequested(...)`
- `GuideOverlayCleared`
- `AutosaveRequested`
- `OpenColoring(mode)`

UI transient effects such as haptic feedback may subscribe to semantic events but are not the source of session truth.

## 7. Teacher playback handshake

Lesson Engine requests playback from Drawing Engine and awaits a typed playback result/event:
- Started
- Progress where useful for UI
- Paused
- Resumed
- Completed
- Cancelled
- Failed

Lesson step state does not advance merely because a timer in the UI expired.

Changing pace delegates to Drawing Engine's playback clock without resetting teacher geometry.

## 8. Child-turn completion

V1 does not require strict AI scoring.

Supported policies from the lesson schema:

### `manual_done`
Default for meaningful drawing steps. Child presses a clear Done/Next control when ready.

### `any_stroke`
For simple early-child interactions where making a mark satisfies the interaction objective. Engine observes a Drawing Engine child-operation event.

### `authored_signal`
Reserved for a deterministic authored rule exposed by a future evaluator. V1 can leave this unused unless one simple rule is proven robust.

The Lesson Engine may provide gentle reminders based on time/interaction policy but must not fabricate a fail state.

## 9. Help Ladder state machine

Independent attempt is help level 0.

`RequestHelp` advances to the next available authored level for the current step:
1. gentle hint;
2. visual guide;
3. direction/anchors;
4. trace path;
5. assisted success.

Rules:
- missing levels are skipped safely;
- requesting help never clears child artwork;
- replay is independent from help level;
- guide overlays are non-destructive;
- assisted success must still permit the child to continue rather than dead-end;
- Help state is persisted for resume where displaying the wrong assistance after restart would confuse the child.

## 10. Pause behavior

Pause freezes:
- lesson progression;
- teacher playback virtual time;
- lesson-owned delay timers;
- narration as defined by narration policy.

Pause does **not** discard child document state.

Resume returns to the previous stable session state rather than guessing a new step.

## 11. Pace changes

Selected pace is session state and persists for the lesson.

During teacher playback:
- `SetPace` updates Drawing Engine's virtual playback clock multiplier;
- current progress position is preserved.

Between teacher strokes/steps:
- Lesson Engine uses pace-profile teaching cadence parameters rather than multiplying every UI delay mechanically.

Narration should not be sped to cartoonish/unintelligible rates merely because visual drawing pace is Very Fast. Narration policy can shorten/skip optional explanation at faster profiles while preserving necessary instruction.

## 12. Watch-Then-Draw overview

The overview is a sequence of non-destructive teacher demonstrations across all steps.

Requirements:
- child document remains empty/unmodified;
- overview can be paused/resumed;
- child can skip the remainder of the overview and begin drawing;
- completed overview does not mark lesson steps completed;
- replaying one step later uses the same canonical source.

## 13. Persistence snapshot

Persist at stable transitions and after meaningful child document changes through the session store/drawing autosave.

`LessonSessionSnapshot` conceptually stores:
- session ID;
- lesson ID + revision;
- child document ID;
- teaching mode;
- pace;
- high-level state/phase;
- current step ID/index;
- current help level;
- whether overview completed;
- drawing/coloring handoff state;
- timestamps needed for product metadata, not UI timers.

Do not persist an unsafe half-transition such as 'step advanced but document operation not committed'. Cross-engine handoffs must define atomic ordering.

## 14. Resume rules

On restore:
1. validate lesson ID/revision compatibility;
2. load the child drawing document;
3. load session snapshot;
4. normalize transient states into a safe stable state;
5. clear stale teacher/guide overlays;
6. restore needed guide/help state;
7. return to child turn or restart the relevant demonstration safely.

We do not attempt to resume in the middle of an exact audio phoneme or half-rendered transient frame. Teacher playback may restart the current demonstration if exact continuation cannot be restored safely; child artwork must still be preserved.

## 15. Lesson revision compatibility

An in-progress session records the lesson revision.

Bundled content should not replace a lesson revision in a way that makes active sessions impossible to resume.

Initial policy:
- keep the referenced bundled lesson revision available for existing active sessions where practical;
- if a revision is incompatible/missing, preserve child artwork and offer safe restart/finish behavior rather than corrupting data;
- future downloadable content migration needs an explicit content migration policy.

## 16. Error model

### Recoverable
Examples:
- transient narration failure;
- teacher overlay render/playback failure where retry is possible;
- optional asset missing but fallback exists.

Behavior:
- child artwork stays intact;
- engine exposes Retry / Continue Without Optional Feature where pedagogically safe.

### Fatal content error
Examples:
- invalid core step references;
- corrupted lesson package that cannot be interpreted.

Behavior:
- save child document/session if any work exists;
- return through a safe child-friendly exit;
- record technical diagnostics;
- quarantine the bad optional/downloaded lesson from normal recommendations until repaired where applicable.

No error screen should blame the child.

## 17. Engine invariants

Always true:
- exactly one authoritative current lesson step in an active drawing session;
- child document operations never live only in UI state;
- teacher/trace overlays are never persisted as child artwork;
- session state never advances beyond a required Drawing Engine commit;
- unsupported Skip cannot bypass authored requirements;
- help level never decreases/increases accidentally due to recomposition;
- switching screen orientation/recomposition does not restart the lesson engine;
- offline operation has no server dependency.

## 18. Test matrix

Automated state-machine tests must cover at least:
- all three teaching modes;
- all five paces;
- pause/resume from every pausable state;
- replay during every child step;
- help progression with missing intermediate levels;
- invalid skip;
- valid skip;
- child Done with/without prior marks according to policy;
- background/restore at each stable state;
- teacher playback failure/retry;
- narration failure fallback;
- drawing complete choices;
- coloring handoff failure/retry;
- incompatible lesson revision recovery;
- process recreation with saved session.

Property/state tests should reject illegal transitions rather than rely only on happy-path UI tests.

## 19. V1 acceptance gate

Lesson Engine contract is satisfied when:
- same lesson source can execute in all declared supported modes;
- UI contains no sequencing timers/state machine truth;
- teacher playback and child turns cannot race into incorrect step progression;
- pause/replay/pace/help work from the required states;
- session restoration preserves artwork/progress;
- fatal content failure cannot corrupt or strand child artwork;
- state-machine automated tests are green.