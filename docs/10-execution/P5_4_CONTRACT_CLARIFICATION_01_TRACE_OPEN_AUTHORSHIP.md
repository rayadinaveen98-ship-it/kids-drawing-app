# P5.4 Contract Clarification 01 — Trace + Open Authorship

**Parent execution contract:** `P5_4_EXECUTION_CONTRACT.md`  
**Issue:** #80  
**PR:** #81  
**ADR:** `docs/adr/ADR-008-trace-open-authorship.md`  
**Date:** 2026-09-14

## Why this clarification exists

The locked Set-C contract deliberately combines Trace & Learn foundational practice with final true-authorship turns in Happy Lines and Shape Friends. Those final turns are `MANUAL_DONE`, skippable, and have no required expected strokes. The pre-P5.4 validation/runtime path treated Trace support as if every turn had to remain traceable. Implementing the locked content exposed that assumption before any Set-C package was accepted.

This clarification resolves only that concrete mismatch. The original P5.4 execution contract remains authoritative in every other respect.

## Clarified rule

For a lesson that supports `TRACE_AND_LEARN`:

### Structured child turns
A structured turn must provide an authored `TRACE_PATH` Help guide or non-empty `expectedStrokeRefs`. Runtime automatic Trace prefers the authored Trace guide and otherwise uses expected geometry.

### Intentional open-authorship turns
A turn is intentionally open only when `completionPolicy == MANUAL_DONE`, `allowSkip == true`, and `expectedStrokeRefs` is empty. Such a turn is valid inside a Trace-capable lesson and receives **no automatic Trace overlay**. It remains a normal child turn with existing Done/Skip/session semantics.

### Help precedence
An explicitly selected Help entry with guide refs continues to render its Help overlay. Help does not silently change completion, scoring, artwork, or session truth.

## Frozen boundaries preserved

This clarification does not add a schema field, lesson-ID runtime branch, session state, completion policy, scoring, similarity grading, adaptive profiling, or drawing/history ownership change. P5.3 companion semantics remain unchanged. Structured Trace validation is not weakened. Trace/Help overlays remain read-only `TEACHER_GENERATED` geometry.

## Implementation evidence

- `ecf46d80e5c4e4812b5510aa45a82d115be01d3e` — validator accepts only intentional skippable open-authorship turns without Trace geometry.
- `4327ab102fc0220fe7568e557594fda6b66a44d0` — session runtime yields automatic Trace on those open turns.
- `4650d160558fde9c73887fa1c826ce2f73889eef` — automatic Trace falls back to expected geometry when no authored Trace guide exists.

## Required verification before Set-C Batch 1 is accepted

- Happy Lines and Shape Friends load through `LessonPackageLoader`.
- Expanded release catalog contains 11 lessons at the Batch-1 checkpoint.
- ContentQualityAnalyzer reports 0 release errors and target 0 warnings.
- Happy Lines structured turns receive Trace; `make_marks_yours` does not.
- Shape Friends circle/square/triangle receive Trace; `build_friend` receives expected-geometry Trace fallback; `make_friend_yours` does not.
- Existing Set-B nine-lesson coverage remains present as a retained subset rather than an immutable total-count assertion.
- Existing malformed structured Trace fixtures remain rejected.

No other P5.4 contract term changes.
