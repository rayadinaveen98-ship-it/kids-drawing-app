# P2.5 Lifecycle Recovery Architecture

## Ownership

P2.5 connects two already separate sources of truth without merging them:

- `DrawingDocumentEngine` + drawing persistence own editable child artwork.
- `LessonSessionEngine` + lesson-session persistence own semantic teaching progress.

A recovery coordinator may orchestrate both, but it may not duplicate or reinterpret their truth.

## Restore ordering

1. Load and validate the lesson package revision.
2. Load the persisted lesson-session envelope.
3. Use its `childDocumentId` to load the editable child drawing document.
4. Only after the child document is available, call `LessonSessionEngine.restore(...)`.
5. Clear any stale runtime teacher/guide overlays.
6. Convert restore normalization into deterministic runtime actions:
   - restart overview when required;
   - restart the current teacher step when required;
   - return to the child turn with the correct authored trace/help overlay;
   - restore drawing-complete/post-drawing states without replaying completed child work.

## Persistence boundary

The session store persists the existing semantic `LessonSessionSnapshot`; it does not persist renderer objects, playback cursors, AndroidX Ink runtime objects, guide bitmaps, or teacher overlay frames.

The storage envelope adds only integrity/version metadata required for safe file recovery.

## Failure policy

- Valid primary -> use primary.
- Invalid/missing primary + valid backup -> recover backup.
- Invalid primary + invalid backup -> return a typed corrupt/unavailable result.
- Snapshot format/lesson ID/revision/step incompatibility -> preserve the child document and surface typed session incompatibility.
- Teacher callback from a pre-recreation request -> reject as stale.
- Teacher playback runtime failure -> transition through the existing recoverable-error model and expose retry/restart behavior.
- Authored content inconsistency discovered after validation -> explicit fatal-content path; never invent drawing/session progress.

## Autosave policy

Session snapshots are persisted at safe semantic boundaries, including:

- accepted Start;
- child-turn entry after teacher/overview completion;
- help-level changes;
- step completion;
- drawing completion;
- explicit Save & Exit;
- lifecycle background request when the current state can produce a safe snapshot.

High-frequency pointer input never triggers lesson-session file writes; drawing autosave remains independently responsible for child drawing operations.
