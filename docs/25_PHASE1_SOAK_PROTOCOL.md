# 25 — Phase 1 30-Minute Soak Protocol

**Applies to:** `0.1.0-art-lab` quality validation  
**Contract:** `docs/18_DRAWING_PERFORMANCE_GATES.md`

Purpose: exercise drawing, history, persistence, teacher playback and lifecycle boundaries long enough to expose leaks, corruption, stale saves or cumulative state bugs.

## Preconditions

Record before starting:
- APK version/versionCode and commit SHA;
- device make/model;
- Android API/version;
- RAM or memory class if known;
- display refresh rate;
- input method: finger / active stylus;
- battery/thermal state notes;
- starting artwork/document state.

Start with a clean Art Lab document unless testing recovery from an existing document.

## Required 30-minute workload

The aggregate run must include at least:
- continuous/mixed drawing for the full session;
- approximately 500 completed drawing/erase operations;
- 500 Undo/Redo operations across the run;
- 100 save requests;
- 100 teacher Play/Pause/Resume/pace-change operations;
- repeated background/foreground transitions;
- configuration/rotation changes only where supported by the current Art Lab surface.

Use automation where available. Manual activity may be used for visual/input checks, but counts must be recorded rather than guessed.

## Suggested checkpoints

### 0–5 minutes — warmup / normal drawing
- reset performance counters;
- draw short, medium and long strokes;
- mix Pencil/Eraser, widths and colors;
- confirm no missing/duplicate marks;
- run teacher demo at Normal and Extra Slow.

### 5–10 minutes — history pressure
- perform repeated Undo/Redo batches;
- use Clear then Undo;
- continue drawing after Undo and confirm redo invalidation is correct;
- record any visible pause or ordering error.

### 10–15 minutes — persistence pressure
- issue repeated Save requests while continuing edits between groups;
- background then foreground the app several times;
- Reload and verify the durable state is coherent;
- force-close/reopen once if the test plan permits.

### 15–20 minutes — teacher playback pressure
- cycle all five paces;
- repeatedly Pause/Resume and Replay;
- change pace mid-stroke;
- verify teacher overlay never increments child document history.

### 20–25 minutes — heavy document
- load W2 (2,000 operations / 250,000 samples);
- interact with the canvas and collect frame/jank measurements;
- measure Save and Reload;
- run large Undo/Redo batches;
- if testing on a Class M device, open W3 and verify it remains editable without OOM.

### 25–30 minutes — mixed abuse / cooldown evidence
- return to mixed manual drawing;
- repeat background/foreground;
- Save, Reload, Undo, Redo, teacher playback and erase operations;
- stop active interaction for a short idle period;
- capture final performance/memory observations.

## PASS gate

All are required:
- zero crashes;
- zero corrupted documents;
- zero lost committed operations;
- no teacher overlay contamination of child history;
- no stuck input/transient stroke after cancellation/lifecycle changes;
- no observed unbounded memory growth;
- final Save → Reload returns coherent editable artwork.

A visible performance problem that does not corrupt data still requires a GitHub issue and measured evidence before the run can be called fully PASS.

## Evidence record template

```text
Date/time:
APK/versionCode:
Commit SHA:
Device model:
Android version/API:
Device class (L/M/S or UNCLASSIFIED):
RAM/memory class:
Refresh rate:
Input: finger / stylus model:

Duration completed:
Approx drawing/erase operations:
Undo/Redo operations:
Save requests:
Teacher control operations:
Background/foreground transitions:

Frame sample count:
Frame P95:
Frame P99:
Jank rate:
Input processing P95/P99 or proxy:
W2 Save samples/results:
W2 Load samples/results:
W2 Undo/Redo samples/results:
W3 open/edit result:

Start memory observation:
Mid memory observation:
End/idle memory observation:

Crashes: 0 / details
Corruption: 0 / details
Lost committed operations: 0 / details
Visual/input defects:
GitHub issues opened:

FINAL STATUS: PASS / FAIL / PENDING-HARDWARE
Tester notes:
```

## Status discipline

Never replace missing evidence with “felt fine.” If a device, stylus, profiler or external latency measurement is unavailable, record the specific gate as `PENDING-HARDWARE` and state why.
