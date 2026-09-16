# Content Capability Contract V1

Status: **FROZEN FOUNDATION — implementation gate active**  
Parent issue: #103  
Active slice: #104  
Branch: `content/library-v2-foundation`

## 1. Core product rule

A lesson may advertise or expose a capability **only when that exact lesson has complete authored data and the current runtime can execute that capability end-to-end**.

Schema validity alone is not enough.

A release lesson is capability-complete only when all five layers agree:
1. **declaration** — the package explicitly declares the capability;
2. **authorship** — the lesson contains the required lesson-specific strokes/guides/steps/text/assets;
3. **runtime** — the accepted engine implements the behavior;
4. **presentation** — the child-facing UI exposes the behavior truthfully and only when available;
5. **recovery** — save/resume/recreation do not turn the capability into a different behavior.

No generic fallback may make an unsupported capability look supported.

## 2. Existing teaching-mode semantics are authoritative

### Draw With Me
Runtime behavior:
- teacher demonstrates the current authored step;
- child receives a turn for that step;
- next step repeats the same teacher → child rhythm.

Release requirements:
- every step has at least one valid teacher stroke reference;
- all teacher stroke references resolve to valid authored geometry;
- child completion policy is implemented by the current Lesson Engine;
- Replay appears only when `allowReplay=true`.

### Watch Then Draw
Runtime behavior:
- one overview plays all authored lesson steps in order;
- child then works through the same step structure;
- later steps do not silently inject a new teacher demonstration;
- Replay remains step-specific when that step allows it.

Release requirements:
- every step contributes valid teacher geometry to the overview;
- overview construction must be deterministic;
- child completion policy is implemented;
- skipping the overview never changes lesson ownership or authored step order.

### Trace & Learn
Runtime behavior:
- teacher demonstrates each step;
- structured child turns receive an authored trace overlay;
- trace geometry comes from an authored `TRACE_PATH` guide or authored expected geometry;
- an intentional open-authorship step may omit trace geometry only when it is explicitly manual-done + skippable + has no expected stroke references.

Release requirements:
- every structured step satisfies the authored trace-source rule;
- no trace geometry is invented by runtime heuristics;
- trace guides resolve to real authored strokes;
- trace remains a non-authoritative visual guide and never writes teacher geometry into child artwork.

## 3. Completion-policy support

Current runtime support:
- `manual_done` — supported;
- `any_stroke` — supported;
- `authored_signal` — **not implemented by Lesson Engine 0.2**.

Therefore a release lesson using `authored_signal` is invalid until a separately accepted runtime contract implements the signal, persistence, recovery, tests and UI behavior.

Draft/review authoring may reserve future fields, but unsupported behavior cannot enter the release catalog.

## 4. Help Ladder capability completeness

A Help entry must produce a real child-facing authored output.

### `gentle_hint`
Required:
- an authored narration/text key.

A `gentle_hint` with neither text/narration nor geometry is invalid.

### `visual_guide`
Required:
- one or more authored guide references resolving to real guide geometry.

Narration is optional.

### `direction_anchors`
Required:
- one or more authored guide references resolving to real guide geometry.

Narration is optional.

### `trace_path`
Required:
- one or more authored guide references resolving to real guide geometry.

A `trace_path` Help entry with empty guide refs is invalid even if Trace & Learn could fall back to expected geometry; the Help entry itself declared a trace path and must therefore own a real path.

### `assisted_success`
Required:
- at least one real authored output: narration/text and/or guide geometry.

Help remains child-initiated and authored. The validator must never generate missing Help content.

## 5. Coloring capability completeness

Coloring is available only when `coloring.enabled=true` in the selected lesson.

Release requirements already preserved:
- enabled coloring has a default mode and at least one step;
- prepared region IDs require an authored coloring-region asset;
- all region IDs resolve to real authored regions;
- freehand coloring may intentionally use empty region IDs;
- fill availability is derived from valid prepared regions, never invented.

### Reserved coloring controls
The data model currently includes `suggestedColorRoles` and `enforceSuggestedColors`.

The accepted product runtime does not currently enforce authored color roles as a completion/interaction rule. Therefore:
- `enforceSuggestedColors=true` is **not a valid release capability** until runtime enforcement is separately implemented and accepted;
- authoring tools must not advertise this as working merely because the schema contains the field;
- non-enforced suggestion metadata may remain reserved, but it cannot be described to authors or children as active behavior until the product consumes it truthfully.

## 6. Narration and audio

Text/narration keys used by teacher steps, Help and coloring must be valid authored identifiers and resolve through the lesson string assets before a content release is accepted.

Audio file declarations are not automatically equivalent to working voice narration. A future authoring surface may expose lesson audio only after product runtime playback, profile preference, lifecycle, accessibility and missing-audio fallback are proven end-to-end.

Until then, Content Library V2 must not claim that declaring `assets.audio` alone creates a functioning lesson capability.

## 7. Asset truth

For release lessons:
- teacher strokes are authoritative authored teaching geometry;
- guides are authored visual support geometry;
- thumbnails/previews are non-authoritative discovery assets;
- coloring regions are authored prepared-fill geometry;
- child artwork remains child-owned and is never replaced by teacher/reference geometry.

No lesson-ID-specific runtime branch is permitted.

## 8. Capability status model for authoring tools

Each lesson capability will eventually have one of four authoring statuses:
- **READY** — authored + runtime-supported + validated;
- **INCOMPLETE** — runtime-supported but required lesson-specific authored data is missing;
- **UNSUPPORTED_RUNTIME** — schema/model field exists but accepted runtime does not implement the behavior;
- **NOT_DECLARED** — lesson intentionally does not offer the capability.

Only **READY** capabilities may appear in the released lesson index as available options.

## 9. Validation behavior

Release validation must fail deterministically for at least:
- unsupported completion policy;
- declared Trace & Learn without step-complete authored trace support;
- Help kinds that declare a visual/trace behavior with no guide geometry;
- a gentle hint with no authored narration/text;
- assisted success with no authored output at all;
- unsupported enforced-color behavior;
- missing references or unsafe assets;
- impossible mode declarations.

Diagnostics must identify the exact lesson path/step/field.

## 10. Compatibility rule

The first implementation of this contract must be tested against the complete existing release catalog before it becomes a production gate.

If the stricter validator finds accepted legacy content that violates this contract:
1. record the exact lesson and field;
2. decide whether the content or contract is wrong;
3. fix the authored lesson when the runtime behavior is clearly unsupported/incomplete;
4. do not weaken the rule merely to keep a green test;
5. never bulk-edit lesson semantics without focused review.

## 11. Definition of done for #104

#104 is complete only when:
- this contract is committed;
- current runtime semantics have been audited against it;
- capability diagnostics exist in production validation code;
- negative JVM tests cover every new failure class;
- all existing release lessons have been run through the stricter gate;
- any legacy exceptions are explicit and reviewed, not silent;
- CI is green;
- no new lesson has been added merely to increase count.
