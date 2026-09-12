# P2.3 — Draw With Me Acceptance

Issue: #31  
Phase: 2 — Lesson Engine 0.2

## Execution loop

For `draw_with_me`, the authoritative Lesson Session Engine owns this loop:

1. select current authored step;
2. convert its validated teacher stroke references into a `TEACHER_GENERATED` Drawing Engine sequence;
3. emit a typed `TeacherPlaybackRequested` request with the selected `TeachingPace`;
4. wait for the matching runtime playback completion signal;
5. enter the child turn;
6. accept authored completion semantics (`manual_done` or `any_stroke` in P2.3);
7. emit step-complete/autosave semantics;
8. advance to the next step and launch its teacher request;
9. after the final child turn, enter `DrawingComplete`.

UI timers are not authoritative and cannot report teacher completion or committed child operations directly.

## Safety invariants

- Teacher requests carry unique request IDs; stale completion/failure callbacks are rejected.
- Teacher strokes are always `StrokeAuthorRole.TEACHER_GENERATED`.
- Teacher sequence creation has no child-document mutation API.
- Replay creates a new request and does not advance the step.
- Changing pace preserves the active teacher request and only changes playback clock pace.
- A child operation can satisfy `any_stroke` only after the Drawing Engine reports a committed operation for the session's child document.
- `manual_done` ignores child-operation signals for progression until Done is explicitly dispatched.
- Skip is accepted only when the authored step declares `allowSkip=true`.
- `authored_signal` remains intentionally unsupported in Lesson Engine 0.2 until a deterministic evaluator contract exists.

## Reference proof

The bundled `lessons/cute-cat` package must execute deterministically as:

`head teacher → child → ears teacher → child → face teacher → child → body_tail teacher → child → DrawingComplete`

Automated tests cover:
- all five teaching paces;
- full four-step Cute Cat progression;
- replay and stale callback rejection;
- valid/invalid skip;
- `manual_done` and `any_stroke`;
- child-document mismatch rejection;
- pace propagation during active playback;
- teacher playback failure as recoverable state;
- teacher-geometry conversion and child-document isolation.
