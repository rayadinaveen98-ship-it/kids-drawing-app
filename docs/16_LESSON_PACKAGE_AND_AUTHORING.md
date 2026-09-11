# 16 — Lesson Package & Authoring Contract

**Status:** Phase 0.4 content contract  
**Machine-readable schema:** `schemas/lesson.schema.json`  
**Validated example:** `examples/cute-cat.lesson.json`

## 1. Core rule

A lesson is **content interpreted by engines**, not application-screen code.

The Lesson Engine owns sequencing. The Drawing Engine owns stroke rendering/input. The Coloring Engine owns coloring behavior. The UI renders current state. A lesson package describes what to teach and what assets to use without knowing which Compose screen/component is showing it.

## 2. Source vs compiled package

We maintain two layers.

### Authoring source
Human-readable files used during content creation:

```text
lessons-src/<lesson-id>/
├── lesson.json
├── strokes.json
├── strings/
│   └── en.json
├── images/
│   ├── thumbnail.webp
│   └── preview.webp
├── coloring/
│   └── regions.svg
└── audio/                 # optional; locale subfolders later
```

### Compiled lesson package
Generated/validated content consumed by the app:

```text
lessons/<lesson-id>/
├── manifest.json
├── lesson.json
├── strokes.bin-or-json
├── strings/
├── images/
├── coloring/
├── audio/                 # optional
└── checksums.json
```

The exact runtime stroke encoding may become binary for performance later. Authoring data remains exportable/readable and the compiled format is generated rather than hand-edited.

## 3. IDs and versioning

### lessonId
Immutable stable ID, lowercase and namespaced by topic where useful.

Examples:
- `animals.cute-cat`
- `nature.sunflower`
- `space.simple-rocket`

A renamed display title does **not** change `lessonId`.

### schemaVersion
Defines the structure of the authoring lesson JSON. Initial version: `1.0`.

### revision
Monotonically increases when the content of a specific lesson changes.

### minimumContentApi
Minimum app content API required to interpret the package.

These are separate so a lesson can be revised without pretending the schema changed.

## 4. Logical canvas coordinates

Lesson geometry is authored in logical canvas units, never device pixels.

Each lesson declares a logical width/height, for example `1000 x 1000`. Teacher strokes, guides, anchors and coloring geometry reference the same coordinate space. Runtime scales/transforms that space to the actual drawing viewport.

This prevents content from being tied to one phone/tablet resolution.

## 5. Teacher stroke timing and pace

The content source stores **one normal teaching timing**.

A step may specify `normalDurationMs` for its demonstration. The engine applies the active pace profile at runtime.

Therefore we do **not** author five copies of the same stroke for Extra Slow / Slow / Normal / Fast / Very Fast.

Pace changes timing, pause density and narration strategy where authored/allowed; it never changes lesson geometry.

## 6. Drawing step contract

A drawing step contains:
- stable step ID;
- target skill IDs;
- teacher demonstration references;
- narration key where applicable;
- child-turn policy;
- allowed replay/skip behavior;
- expected/guide references where useful;
- authored Help Ladder entries;
- optional completion narration.

### V1 child completion policies

`manual_done`  
The child explicitly indicates they are ready to continue. This is the safest/default V1 policy for complex drawing.

`any_stroke`  
Useful only for simple interactions where making a mark is enough.

`authored_signal`  
Reserved for an engine-understood authored completion condition. It must not imply strict AI grading by default.

## 7. Help Ladder representation

Independent attempt is the default state and is not a help object.

Authored help levels map to:

1. `gentle_hint`
2. `visual_guide`
3. `direction_anchors`
4. `trace_path`
5. `assisted_success`

A lesson does not need every help level for every step. Authors include only useful assistance, but a critical step must have a viable progression to successful continuation.

Help references can point to guide strokes/anchors from the same logical coordinate system.

## 8. Narration and localization

Lesson JSON stores **string keys**, not child-facing copy embedded throughout engine code.

Example:
`lesson.cute_cat.step.head.demo`

Each supported locale provides a strings file. V1 can use Android TextToSpeech from localized text. Optional prerecorded audio can override/follow the same semantic keys later.

Rules:
- every release lesson has a complete default-locale strings file;
- engine events use semantic keys/state, not hard-coded English sentences;
- missing optional prerecorded audio falls back safely to text/TTS where supported;
- narration can be disabled without breaking the lesson.

## 9. Coloring contract

A lesson can set coloring disabled or enabled.

When enabled, the package can include:
- region geometry asset;
- guided coloring steps;
- region IDs;
- narration keys;
- suggested semantic color roles.

Suggested colors are not automatically enforced. `enforceSuggestedColors` is false by default and should be true only when the educational objective genuinely requires a specific color relationship/recognition task.

## 10. Assets

Required V1 source references:
- teacher stroke file;
- thumbnail;
- preview image;
- at least one strings locale.

Optional:
- coloring regions;
- prerecorded audio;
- additional guide/reference assets.

Child-facing lesson packages must not depend on arbitrary external URLs.

## 11. Package integrity

The content compiler/package task will eventually produce:
- package manifest;
- resolved file list;
- SHA-256 checksum per packaged asset;
- lesson/schema/revision metadata.

Bundled V1 lessons are compiled during the app build. Future downloadable packs use the same package contract and integrity metadata.

## 12. Validation gates

A lesson cannot move to `release` until automated validation confirms at least:
- JSON Schema validation succeeds;
- lesson ID and step IDs are unique in scope;
- every referenced stroke/guide/region exists;
- every required string key exists in the default locale;
- every referenced asset path exists and is relative/safe;
- age band and difficulty metadata are present;
- at least one teaching mode is supported;
- drawing steps are non-empty;
- coloring references resolve when coloring is enabled;
- package can load with no network access.

Content QA then verifies behavior beyond schema correctness.

## 13. Free authoring toolchain

Required development/content tooling remains ₹0.

### Teacher stroke creation
Preferred long-term workflow: a debug-only **Lesson Authoring Lab** built on our own Drawing Engine. An artist draws the teacher strokes using the exact engine data model, groups them into steps, and exports source stroke data.

This eliminates translation mismatch between an external drawing SDK and our runtime engine.

### Vector guides / coloring regions
- Inkscape for SVG/vector region preparation.

### Artwork / thumbnails / previews
- Krita and/or Inkscape.

### Metadata / narration / schema
- normal text/JSON editing in the repository;
- internal editor tooling later if authoring volume justifies it.

### Validation / packaging
- JSON Schema validation;
- repository scripts/Gradle tasks built from free/open tooling;
- CI validation once the app repository scaffold exists.

## 14. Lesson authoring workflow

1. **Choose learning objective** — subject, age bands, difficulty and target skills.
2. **Storyboard lesson** — define the smallest understandable drawing steps.
3. **Record master teacher strokes** — capture geometry/order/timing in the canonical coordinate space.
4. **Group strokes into steps** — author teacher demo and child-turn boundaries.
5. **Author Help Ladder** — create hints/guides/anchors/trace assets only where useful.
6. **Prepare coloring data** — only if the lesson supports coloring.
7. **Write narration/localizable copy** — concise, patient and age-appropriate.
8. **Prepare thumbnail/preview assets**.
9. **Validate schema and references**.
10. **Play-test all supported teaching modes**.
11. **Play-test all five pace profiles**.
12. **Age-band QA** — ensure visible complexity and narration fit intended users.
13. **Content review** — mark source `release` only after quality gates pass.
14. **Compile/package** for bundled or future downloadable distribution.

## 15. Content quality rule

A technically valid lesson is not automatically a good lesson.

Every release lesson must answer:
- What specific art skill is being practiced?
- Why are these step boundaries appropriate?
- Where can the child meaningfully make creative choices?
- Is the help sequence genuinely helpful instead of simply revealing the answer?
- Does the lesson remain understandable at the slowest pace without becoming irritating?
- Does the fastest pace remain legible?
- Is the completed artwork satisfying enough that a child would want it in their Gallery?

## 16. Schema validation record

The initial example `examples/cute-cat.lesson.json` was validated successfully against `schemas/lesson.schema.json` using a Draft 2020-12 JSON Schema validator before being committed.