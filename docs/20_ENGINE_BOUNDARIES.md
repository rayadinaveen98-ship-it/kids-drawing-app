# 20 — Cross-Engine Boundaries & Handoff Contract

**Status:** Phase 0.5 implementation contract

## 1. Purpose

This document prevents product logic from leaking into feature screens and prevents the core engines from becoming mutually coupled through implementation details.

The governing rule is:

> UI presents state and emits commands. Engines own authoritative product state, sequencing, persistence semantics and recovery behavior.

## 2. Engine ownership

### Drawing Engine
Owns:
- editable drawing document;
- ink/color stroke operation storage substrate;
- drawing tools/brush resolution;
- low-latency pointer/stylus integration;
- operation history;
- document persistence;
- deterministic teacher stroke playback;
- document-space transforms;
- role-aware erase behavior.

Does not own:
- lesson step progression;
- child encouragement;
- guided coloring progression;
- app navigation.

### Lesson Engine
Owns:
- current lesson session;
- teaching mode;
- pace selection;
- drawing step progression;
- teacher-demo orchestration;
- child-turn boundaries;
- Help Ladder;
- drawing→coloring/finish decisions;
- lesson session persistence/recovery.

Does not own:
- raw stroke geometry;
- low-level rendering;
- companion animation implementation;
- coloring operation storage.

### Coloring Engine
Owns:
- coloring session;
- freehand coloring semantics;
- authored region fills;
- guided-coloring step progression;
- coloring tool/color state;
- coloring-phase persistence/recovery;
- completion signal.

Does not own:
- line-art lesson progression;
- screen navigation;
- companion rendering;
- public sharing/export.

### Companion Engine
Owns:
- semantic companion state;
- animation/expression selection;
- interruption/priority policy for companion reactions;
- optional voice/narration presentation rules where delegated.

Does not own:
- lesson truth;
- drawing truth;
- coloring truth;
- progression decisions.

### Narration Service
Owns:
- local TTS/prerecorded playback execution;
- queue/priority/stop behavior;
- capability/fallback reporting.

Does not own lesson timing truth.

### Session Store
Owns:
- versioned lesson/coloring session snapshots;
- atomic read/write semantics;
- compatible migration/defaulting.

Does not duplicate the drawing document.

## 3. Shared domain identifiers

Cross-engine calls use stable owned identifiers/value objects, not Android framework or AndroidX Ink implementation types.

Examples:
- `DocumentId`
- `LessonId`
- `LessonRevision`
- `SessionId`
- `StepId`
- `ColorRegionId`
- `ToolPresetId`
- `PlaybackSequenceId`

Ink/graphics implementation objects remain inside `core:drawing` adapters.

## 4. State/event pattern

Each engine exposes:
- observable durable state (`StateFlow` directionally);
- typed commands/methods;
- typed one-shot semantic effects/events where needed.

Feature screens must not reconstruct engine state by combining unrelated booleans/timers.

Forbidden example:

```text
UI starts a 2.3 second timer
→ assumes teacher stroke finished
→ advances lesson step
```

Required example:

```text
Lesson Engine requests teacher playback
→ Drawing Engine emits typed completion result
→ Lesson Engine transitions state
→ UI observes new state
```

## 5. Drawing Engine ↔ Lesson Engine contract

Lesson Engine may request:
- load/attach child document;
- start teacher playback(sequence, pace);
- pause/resume/cancel/replay teacher playback;
- change playback pace;
- show/clear non-destructive guide overlays through an owned overlay contract;
- observe child document mutation signals relevant to completion policy;
- request/confirm document save.

Drawing Engine reports:
- document ready/error;
- child operation committed;
- playback started/progress/paused/resumed/completed/cancelled/failed;
- persistence success/failure;
- renderer/input capability diagnostics where necessary.

Lesson Engine never imports raw Ink stroke types.

## 6. Lesson Engine ↔ Coloring Engine contract

Lesson Engine initiates coloring only after completed drawing persistence is confirmed.

Request contains conceptually:
- child document ID;
- lesson ID/revision;
- coloring mode (`guided` or `self`);
- validated coloring metadata reference.

Coloring Engine reports:
- Ready;
- RecoverableError;
- FatalColoringContentError;
- Completed;
- SaveAndExitCompleted.

Lesson Engine remains the owner of the overall lesson session completion semantics. Coloring Engine owns the coloring sub-session.

## 7. Drawing Engine ↔ Coloring Engine contract

Coloring Engine uses Drawing Engine through a coloring-specific facade rather than reaching into renderer internals.

Needed capabilities:
- attach existing editable document;
- commit color stroke operation;
- commit/replace region fill operation;
- commit color erase operation;
- undo/redo current document operation;
- persist document;
- observe document mutation/persistence state.

Drawing Engine enforces role-aware operation semantics so Coloring Engine cannot accidentally erase immutable lesson overlays or protected final line art.

## 8. Lesson/Coloring ↔ Companion contract

Product engines emit semantic signals only, such as:
- `WELCOME`
- `TEACHING`
- `WATCHING_CHILD`
- `WAITING_PATIENTLY`
- `HELPING`
- `ENCOURAGING`
- `CELEBRATING_STEP`
- `CELEBRATING_ARTWORK`
- `GENTLE_ERROR`
- `PAUSED`

Companion Engine decides the exact animation/expression/voice treatment.

No engine waits for a decorative animation to finish unless an authored pedagogical beat explicitly requires acknowledgement through a typed contract.

## 9. Narration coordination

Lesson/Coloring engines emit semantic narration requests with keys/text roles and priority.

Narration must report execution/failure independently.

Rules:
- missing TTS/voice capability cannot corrupt lesson state;
- required instruction always has readable/visual fallback;
- optional companion chatter cannot block progression;
- pause can stop/freeze lesson-owned narration according to policy;
- UI never uses audio duration as authoritative lesson timing.

## 10. Atomic handoff rules

### Drawing step commit
Order:
1. Drawing Engine commits operation.
2. Drawing Engine acknowledges commit.
3. Lesson Engine updates step/session state.
4. Session snapshot persists.

Never persist `step complete` before required document mutation is committed.

### Drawing → coloring
Order:
1. final drawing operation confirmed;
2. document save confirmed or durable autosave boundary confirmed;
3. session snapshot = drawing complete;
4. post-drawing choice recorded;
5. Coloring Engine initializes existing document;
6. coloring phase snapshot committed;
7. UI navigates into ready coloring workspace.

### Coloring → artwork complete
Order:
1. pending color operation committed;
2. document save confirmed;
3. Coloring Engine reports complete;
4. Lesson Engine/session marks final completion;
5. Gallery metadata/preview generation scheduled;
6. completion UI shown.

Preview-image generation is not part of the atomic artwork truth.

## 11. Process death / recovery ownership

On cold restore:
- Startup Router asks session repository for resumable activity metadata;
- Drawing Engine owns document recovery;
- Lesson Engine owns guided-session recovery;
- Coloring Engine owns coloring sub-session recovery;
- transient companion/audio/render overlays are rebuilt, not persisted as truth.

If engine snapshots disagree, preservation of the editable child artwork has highest priority. Session progression may safely roll back to the last stable step; artwork must not be discarded to force metadata consistency.

## 12. Failure containment

A failure in one engine must not automatically cascade into destructive failure elsewhere.

Examples:
- narration fails → visual lesson continues;
- companion animation fails → lesson continues;
- guided coloring metadata fails → completed line art remains intact;
- teacher playback fails → retry or safe child turn where pedagogically allowed;
- preview generation fails → artwork still exists;
- optional downloaded lesson is corrupt → quarantine that lesson, not the app/database.

## 13. UI contract

Feature UI may own ephemeral presentation state only, for example:
- whether a bottom sheet is currently expanded;
- scroll position;
- local animation transition progress;
- focus state;
- one-frame pointer decoration.

Feature UI must not own:
- current authoritative lesson step;
- artwork operation history;
- teacher playback time;
- coloring progression;
- persisted Help Ladder level;
- save truth;
- completion truth.

## 14. Repository/module direction

Directional dependency target:

```text
core:model
   ↑
core:drawing       core:content
   ↑                  ↑
core:coloring      core:lesson
        \             /
         \           /
          feature modules
```

Exact Gradle modules may be introduced progressively to avoid premature module overhead, but dependency direction must be preserved.

Companion/audio/session abstractions live in core modules and are injected through interfaces rather than instantiated directly in feature composables.

## 15. Contract-test requirements

Before public V1, integration tests cover at least:
- lesson requests teacher playback and advances only on completion;
- drawing commit precedes lesson step commit;
- drawing completion safely initializes guided coloring;
- coloring init failure preserves drawing and session recovery;
- color operations persist in the shared document;
- role-aware erase protection;
- save/exit/resume across drawing and coloring;
- process death during handoff boundaries;
- companion/narration failure does not stall core progression;
- preview/gallery failure does not lose artwork.

## 16. Phase 0.5 exit rule

Phase 0.5 is complete only when the Drawing, Lesson and Coloring contracts plus this boundary document are precise enough that Art Lab and the first vertical slice can be implemented without putting sequencing, persistence or engine truth into UI code.