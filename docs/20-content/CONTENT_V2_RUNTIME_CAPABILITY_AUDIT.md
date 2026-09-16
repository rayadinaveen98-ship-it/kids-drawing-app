# Content Library V2 — Runtime Capability Audit

Status: **ACTIVE / evidence-driven**  
Parent: #103  
Active slice: #104  
Draft PR: #107

This audit applies the frozen rule from `CONTENT_CAPABILITY_CONTRACT_V1.md`: a schema field is not a working lesson capability until authored data, runtime behavior, child-facing presentation and recovery agree.

## Proven child-facing capabilities

### Teaching modes — READY for current release catalog

The whole release catalog is now exercised through the real `LessonSessionEngine` for every mode each lesson advertises.

Evidence:
- capability-validation catalog gate: Android CI #700 GREEN;
- executable release lesson × advertised mode matrix: Android CI #701 GREEN.

The executable gate proves real start → teacher/overview playback → authored Help/Replay where available → child turns → drawing completion behavior for the current 24 release lessons.

### Replay — READY when authored

Replay is exposed only when `childTurn.allowReplay=true` and is exercised by the whole-catalog execution matrix.

### Trace & Learn — READY under the authored-geometry rule

Structured trace steps require authored `TRACE_PATH` geometry or authored expected geometry. Open-authorship exceptions remain explicit and bounded. Runtime may not invent trace paths.

### Post-drawing coloring availability — READY at product presentation boundary

A child-facing defect was found during this audit: the lesson workspace previously showed `Color with me` and `Color myself` after every drawing even when the selected lesson did not author coloring.

The product now derives the post-drawing controls from `ProductLessonRuntime.coloringAvailable` through `PostDrawingCapabilityPolicy`:
- authored coloring → coloring choices are shown;
- no authored coloring → coloring choices are hidden;
- `Finish for now` remains available in both cases;
- explanatory copy no longer promises unavailable coloring.

Exact corrected UI head: `3c9c982a75d4d87ea8b1617b82da2635ff080a92`  
Evidence: Android CI #705 FULL GREEN.

A deeper Lesson Engine defense-in-depth rejection for direct coloring commands on non-coloring content remains an audit follow-up; product runtime and child UI already reject/hide the unavailable capability.

## Authored text / narration audit

Release packages already author teacher-step, Help, completion and coloring narration keys. Catalog validation proves required default strings exist.

However, storing a key is not the same as presenting its lesson-specific message to the child. The production lesson companion historically relied mainly on generic age/mode copy.

Content V2 therefore adds:
- `LessonAuthoredTextPolicy` — pure mapping from accepted lesson/session state to the exact authored key that belongs in that state;
- `ProductLessonTextRepository` — local-only default-locale string resolver with safe null fallback;
- JVM coverage for teacher demonstrations, exact Help levels, final drawing completion, visual-only Help, normal child turns and mismatched state/content identity.

UI integration of the resolved lesson-specific text is the next implementation step. Generic age-aware guidance remains the fallback and is not removed.

Audio declarations remain **UNSUPPORTED AS A WORKING VOICE CAPABILITY** until playback, narration preference, lifecycle, accessibility and missing-audio behavior are implemented and tested end-to-end.

## Reserved / inert authoring fields

The following fields exist in the content model but must not be presented by future Content Studio as working controls merely because they exist.

### `childTurn.toolPreset` — RESERVED pending semantics

Current release content uses this field (for example Cute Cat authors `pencil-soft`), while the accepted lesson runtime currently leaves lesson drawing tools at product-owned defaults. Current `DrawingBrushPreset` values are `pencil.standard`, `crayon.standard` and `marker.standard`; no accepted mapping for `pencil-soft` has been defined.

Decision: do not guess a mapping and do not expose a Content Studio preset control until the preset vocabulary, child override rules, save/resume behavior and step-transition semantics are contracted and tested.

### `teacher.playAsGroup` — RESERVED pending semantics

Current release content uses this field, but `LessonTeacherSequenceFactory` currently sequences authored strokes deterministically with an inter-stroke gap and does not branch on `playAsGroup`.

Decision: do not invent grouping semantics. First quantify existing usage, then define what grouping changes visually/timing-wise and how Replay/pace/recovery preserve it.

### `suggestedColorRoles` — RESERVED metadata

Non-enforced suggestions may exist as metadata, but they are not currently a child-facing constraint or promise.

### `enforceSuggestedColors` — UNSUPPORTED_RUNTIME

`true` is release-blocked by `LessonCapabilityValidator` until accepted coloring runtime behavior exists.

### `authored_signal` completion — UNSUPPORTED_RUNTIME

Release-blocked until a separately accepted completion signal contract, persistence/recovery behavior and UI are implemented.

## Release-authoring inventory

`ProductionAuthoringCapabilityInventoryTest` now emits a deterministic CI report covering the current release catalog:
- `toolPreset` usage and distinct values;
- `playAsGroup` usage;
- teacher narration keys;
- completion narration keys;
- Help narration keys;
- coloring narration keys;
- suggested color roles;
- enforced suggested colors;
- audio assets.

The report is evidence for future semantics, not permission to assume that an inert field already works.

## No content-volume expansion yet

No new lesson has been added in this track.

The order remains:
1. capability truth;
2. runtime/presentation gaps closed or explicitly classified;
3. scalable catalog/index foundation (#105);
4. authoring workflow (#106);
5. deliberate lesson expansion only after those gates are green.
