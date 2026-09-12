# P2.5 — Lifecycle Recovery Execution Plan

Issue: #33  
Parent epic: #28  
Depends on completed #31 and #32.

## Objective

Make lesson sessions survive background/foreground transitions and process recreation without corrupting child artwork or resuming transient teacher/guide state unsafely.

## Locked implementation order

1. Define the persisted lesson-session envelope and typed session-store result model.
2. Implement an atomic file-backed lesson session store with checksum/version validation and backup recovery.
3. Add an autosave coordinator driven only by Lesson Engine safe-transition events.
4. Restore the child drawing document before restoring lesson-session state.
5. Normalize transient teacher/overview/guide phases on restore and emit explicit runtime cleanup/restart actions.
6. Handle incompatible lesson revisions as typed recovery outcomes rather than silently coercing progress.
7. Add teacher playback retry/failure recovery and fatal-content termination behavior at the engine boundary.
8. Lock drawing-complete/post-drawing handoff semantics.
9. Prove all three teaching modes through background/foreground and process-recreation tests.

## Non-negotiable invariants

- The UI never becomes the source of lesson/session truth.
- Child drawing operations and lesson session progress remain separately owned and separately persisted.
- Child document reload completes before lesson-session restoration.
- Teacher/trace/help overlays are always transient and are never persisted into the child document.
- Restore may restart a deterministic teacher/overview demonstration, but it must never pretend a transient overlay was safely resumed mid-frame.
- Snapshot/lesson revision mismatch is explicit and recoverable; no silent migration in Lesson Engine 0.2.
- Persistence remains offline and requires no new sensitive permission or network dependency.

## Acceptance target

Across Draw With Me, Watch Then Draw, and Trace & Learn, background/foreground and process recreation preserve child artwork and semantic lesson progress. Transient teacher/trace state is cleared or deterministically restarted, stale callbacks are rejected, and incompatible/corrupt persistence resolves through typed recovery paths without child-artwork loss.
