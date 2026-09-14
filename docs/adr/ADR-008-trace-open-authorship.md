# ADR-008 — Trace & Learn yields to intentional open authorship

**Status:** Accepted for P5.4  
**Issue:** #80  
**PR:** #81  
**Date:** 2026-09-14

## Context

P5.4 locks **Happy Lines** and **Shape Friends** as lessons that support `TRACE_AND_LEARN` for foundational practice while ending with an explicitly open creative turn. Those final turns use the already-established authorship signal:

- `completionPolicy = MANUAL_DONE`;
- `allowSkip = true`;
- `expectedStrokeRefs` is empty.

The pre-P5.4 Trace contract assumed every child turn in a Trace-capable lesson had traceable geometry. That assumption caused two concrete integration defects:

1. package validation rejected a valid open-authorship turn because it had no trace/expected geometry;
2. the session runtime attempted to create an automatic Trace overlay for every child turn, including the open turn.

A second mismatch also existed: package validation already considered `expectedStrokeRefs` a valid structured Trace source, while the runtime could render automatic Trace only from an authored `TRACE_PATH` guide. Set C's `build_friend` step intentionally has expected construction geometry plus visual Help rather than a dedicated Trace guide.

These are concrete Set-C contract failures. They do not justify a new lesson runtime, state machine, schema, or lesson-ID branch.

## Decision

1. **Structured Trace turns remain strict.** A normal child turn in a lesson supporting `TRACE_AND_LEARN` must have either an authored `TRACE_PATH` Help entry with guide geometry or non-empty `expectedStrokeRefs` usable as the automatic Trace source.
2. **Intentional open authorship is the only geometry-free Trace exception.** It requires `MANUAL_DONE`, `allowSkip = true`, and empty `expectedStrokeRefs`.
3. **Trace mode yields on that open turn.** No automatic trace overlay is requested; existing Done/Skip semantics remain.
4. **Automatic Trace resolution is generic.** Prefer an authored `TRACE_PATH`; otherwise render `expectedStrokeRefs` as the read-only Trace overlay.
5. **Explicit Help remains authoritative.** Help with guide refs takes precedence over automatic Trace; reducing/dismissing Help returns to automatic Trace where applicable.
6. **Overlay isolation is unchanged.** Trace/Help geometry remains `TEACHER_GENERATED` presentation geometry and never enters child artwork/history.
7. No scoring, similarity judgement, lesson-ID-specific behavior, hidden adaptation, or new persistence/session truth is introduced.

## Consequences

- Happy Lines and Shape Friends retain foundational Trace practice without forcing final creative choices.
- `build_friend` and future structured steps can use validated expected geometry without duplicating a dedicated Trace guide.
- Accidentally missing Trace geometry on structured turns remains a validation error.
- The schema and session-state model remain unchanged.

## Verification

P5.4 automated coverage must preserve malformed structured Trace rejection, load both packages, show Trace on structured turns, prove `build_friend` expected-geometry fallback, prove both final open turns have no automatic Trace overlay, and confirm normal completion/artwork isolation.
