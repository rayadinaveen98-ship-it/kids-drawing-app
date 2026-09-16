# Content Studio Export Contract V1

Status: **FROZEN FOUNDATION — implementation gate active**  
Parent: #103  
Active slice: #106  
Baseline: `74163710d43c15139aeaba8e14cdf43ce286b07a` / Android CI #746 GREEN

## 1. Purpose

Content Studio is internal authoring tooling for producing the same normal lesson packages consumed by the accepted child runtime. It is not a second lesson format, a child-facing feature, or a privileged release path.

The governing rule remains:

> A lesson may advertise or expose a capability only when that exact lesson has complete authored data and the accepted runtime can execute that capability end-to-end.

Content Studio may make authoring safer and faster, but it may never weaken `LessonPackageLoader`, `LessonCapabilityValidator`, taxonomy validation, content-quality gates, or Catalog Index V2 truth.

## 2. Architectural boundary

```text
Author draft
  ↓
Content Studio authoring model
  ↓
Deterministic canonical export into isolated staging
  ↓
Normal lesson package files
  ↓
Production LessonPackageLoader
  ↓
LessonCapabilityValidator
  ↓
Taxonomy / prerequisite graph validation
  ↓
Content quality + asset/reference checks
  ↓
Catalog Index V2 projection
  ↓
Release-catalog integration only when every blocking gate passes
```

The child app has no dependency on Content Studio classes, draft files, AI providers, editor state, or authoring history.

## 3. Normal package output — no parallel schema

A successful export produces the existing package shape under one lesson package root:

```text
lesson.json
strokes.json
strings/<locale>.json
thumbnail.svg
preview.svg
coloring-regions.json        # only when authored prepared regions exist
<audio files>                # reserved until voice capability is accepted
```

`lesson.json`, stroke catalogs and coloring-region catalogs must decode into the existing production models. Content Studio does not add runtime-only shadow fields to those files.

The exporter may maintain authoring-only drafts outside the release assets tree, but those files are not runtime inputs and must never be referenced by Catalog Index V2.

## 4. Authoring status vocabulary

Every authorable capability must be classified with the same four-state truth model introduced by Content Capability Contract V1:

- **READY** — runtime-supported, completely authored and production-valid;
- **INCOMPLETE** — runtime-supported but required authored data is missing or invalid;
- **UNSUPPORTED_RUNTIME** — modeled/schema field exists but accepted runtime does not implement the behavior;
- **NOT_DECLARED** — intentionally absent from this lesson.

Only READY capabilities may be exported as advertised release capabilities.

Studio UI/CLI/reporting must never convert INCOMPLETE or UNSUPPORTED_RUNTIME into a positive capability merely to make export succeed.

## 5. Runtime-backed authoring surface

V1 may author the following when all normal production requirements are satisfied:

### Lesson identity and metadata
- lesson ID and revision;
- draft/review/release status;
- minimum content API;
- age bands;
- difficulty and estimated minutes;
- registered category IDs;
- registered skill IDs;
- registered journey IDs;
- registered collection IDs/content-family ID when the accepted package/index model supports exporting them;
- prerequisite lesson IDs;
- tags;
- canvas dimensions/background role;
- supported teaching modes.

Taxonomy IDs are selected from the accepted registry. Studio must not silently create or normalize an unknown taxonomy ID during package export.

### Teacher geometry
- authored strokes with stable IDs;
- ordered points with logical x/y, monotonic `timeMs`, and pressure;
- authored guides referencing authored strokes;
- drawing steps with stable IDs;
- ordered teacher stroke references;
- teacher narration keys;
- teacher normal duration.

### Child turns
V1 release-authorable controls:
- `manual_done` completion;
- `any_stroke` completion;
- Replay permission;
- Skip permission;
- expected authored stroke references.

### Help ladder
V1 may author all accepted Help kinds:
- gentle hint;
- visual guide;
- direction anchors;
- trace path;
- assisted success.

The editor must enforce each kind's authored-output rule before READY status.

### Trace & Learn
Trace support is READY only when every structured step has accepted authored trace geometry through a TRACE_PATH guide or expected authored geometry, with the existing bounded open-authorship exception.

Studio must never synthesize a runtime trace path during export merely because Trace & Learn was selected.

### Strings and authored text
V1 authors default-locale strings and any explicitly supplied additional locale maps. All referenced title, summary, teacher, Help, completion and coloring text keys must resolve before release promotion.

### Coloring
V1 may author:
- no coloring;
- freehand coloring with empty region IDs;
- prepared/guided prepared coloring with real authored region geometry;
- coloring narration keys;
- non-enforced suggested color-role metadata.

Prepared region references must resolve to a real `coloring-regions.json` catalog and valid geometry.

### Discovery artwork
V1 may accept or generate deterministic thumbnail/preview SVG derivatives. These are discovery assets only; they never become teacher geometry or child artwork.

## 6. Reserved / unsupported controls

The Studio must explicitly prevent these modeled fields from being mistaken for working release controls:

### `childTurn.toolPreset` — RESERVED
Current runtime does not implement an accepted authored-preset mapping/override contract. Existing legacy values may be preserved when round-tripping an accepted package, but V1 must not present tool preset as a functioning new authoring choice.

### `teacher.playAsGroup` — RESERVED
Current runtime does not implement accepted grouping semantics. Existing values may be preserved during lossless import/round-trip, but V1 must not claim grouping affects playback.

### `suggestedColorRoles` — RESERVED METADATA
May be authored/preserved only as non-enforced metadata. Studio wording must not imply a child constraint.

### `enforceSuggestedColors=true` — UNSUPPORTED_RUNTIME
Release export is blocked while true.

### `authored_signal` completion — UNSUPPORTED_RUNTIME
Release export is blocked until a separately accepted runtime/persistence/UI contract exists.

### lesson audio / working voice narration — UNSUPPORTED AS A CAPABILITY
Audio declarations do not become READY voice support until runtime playback, narration preference, lifecycle/recovery, accessibility and missing-audio behavior are separately accepted. V1 must not advertise voice readiness from file presence alone.

## 7. Draft versus release behavior

Content Studio may save incomplete internal drafts.

Draft persistence and release promotion are different operations:
- **save draft** may retain incomplete editor state outside runtime release assets;
- **validate export** renders a candidate normal package into isolated staging and runs all deterministic gates;
- **promote release** is permitted only when the staged candidate has zero blocking diagnostics.

An invalid candidate must not partially overwrite the release package, Catalog Index V2, strings, strokes, preview, thumbnail, or coloring regions.

## 8. Deterministic export rules

Given identical authoring input and exporter version, canonical package output must be byte-stable except for explicitly versioned/generated assets whose deterministic inputs changed.

Required rules:
- UTF-8 text;
- stable JSON formatting;
- stable field ordering defined by serializers/exporters;
- authored list ordering is preserved where order has product meaning;
- map keys are serialized deterministically;
- no timestamps, random IDs or machine paths in runtime package output;
- no hidden environment-dependent defaults;
- normalized relative asset paths only;
- export never mutates authored geometry to make validation pass;
- IDs are explicit and stable; renaming is an author decision, not an exporter side effect.

## 9. Geometry rules

Teacher strokes and prepared coloring regions use the lesson's logical canvas coordinates.

Before READY/export:
- stroke points stay inside canvas bounds;
- each stroke has at least two points;
- stroke timestamps are monotonic;
- pressure stays within accepted bounds;
- guide references resolve;
- prepared region polygons satisfy the accepted coloring-region validator;
- all referenced IDs are unique and resolve.

Import helpers may transform external source geometry into logical coordinates, but the transformation is explicit and deterministic. Runtime export contains only the final accepted geometry.

## 10. Text/key rules

The Studio owns human-friendly editing but exports stable string IDs.

Release validation must prove:
- title/summary keys resolve in the required default locale;
- every authored teacher narration key resolves;
- every authored Help narration key resolves;
- every authored completion narration key resolves;
- every authored coloring narration key resolves;
- a visual-only Help item remains visual-only and does not receive invented text;
- missing optional translations do not masquerade as translated content.

Key generation, when offered, must be deterministic from explicit lesson/step intent and remain editable before export.

## 11. Preview and thumbnail contract

V1 may generate SVG discovery assets from explicitly authored reference geometry or accept reviewed SVG assets.

Generated derivatives must:
- use deterministic logical bounds/viewBox;
- contain no remote resources, scripts, external file references or executable content;
- remain non-authoritative discovery art;
- be separately reviewable from teaching geometry;
- pass existing safe-path and asset checks.

A missing/invalid required thumbnail or preview blocks release promotion.

## 12. Validation pipeline

A release candidate must pass, in order:

1. authoring-model structural diagnostics;
2. deterministic canonical serialization into isolated staging;
3. the production `LessonPackageLoader` against staged files;
4. `LessonCapabilityValidator`;
5. taxonomy registry/reference validation;
6. prerequisite/release-graph validation in the proposed catalog context;
7. required string/asset reference checks;
8. `ContentQualityAnalyzer` / frozen content-quality contract;
9. Catalog Index V2 projection for the candidate;
10. aggregate index health/drift checks for the proposed release set.

The Studio must report exact field/step/asset diagnostics. It must not catch a production failure and replace it with a generic success/warning.

## 13. Atomic promotion

Promotion is a transaction from validated staging into the release content tree.

Requirements:
- all candidate files are known before promotion begins;
- destination package identity is explicit;
- replacement of an existing release lesson requires an explicit revision decision;
- failure before commit leaves the previous release package untouched;
- no catalog/index update may point at an uncommitted or invalid package;
- after promotion, the complete release catalog is revalidated and the aggregate Catalog Index V2 is regenerated/compared;
- rollback means restoring the previous complete package/index state, never mixing revisions.

Git remains the durable review/audit boundary for accepted repository content.

## 14. Import / round-trip compatibility

Content Studio V1 must be able to import the current accepted release-package shape for inspection/round-trip testing.

For fields classified RESERVED but present in legacy content (`toolPreset`, `playAsGroup`, non-enforced suggested roles), import/export must preserve their values unless the author explicitly edits the lesson through a future accepted migration.

Round-tripping must not silently:
- change lesson ID/revision;
- reorder product-significant lists;
- drop valid authored Help/trace/coloring content;
- invent capabilities;
- alter teacher geometry;
- change string values/keys;
- rewrite release status.

## 15. AI assistance boundary

AI may propose draft text, step decomposition, geometry ideas, tags or Help ideas inside an authoring workflow.

AI output:
- is draft material only;
- has no direct release-catalog write path;
- receives no exemption from normal validation;
- must be reviewable/editable before export;
- cannot fabricate runtime support for a reserved capability;
- cannot generate child-facing content dynamically at runtime.

The Content Studio foundation must work completely without any paid/external AI service.

## 16. Evidence produced by V1

For every validated export candidate, tooling must be able to produce deterministic evidence containing at least:
- lesson ID/revision/status;
- package file manifest;
- package-loader result;
- capability status summary;
- taxonomy/reference diagnostics;
- string/reference diagnostics;
- stroke/guide/region counts;
- supported modes;
- Help readiness;
- Trace readiness;
- coloring capability;
- reserved/unsupported-field usage;
- content-quality diagnostics;
- projected Catalog Index V2 entry or reason projection is blocked.

Machine-readable evidence is authoritative; a human-readable summary may be generated from it.

## 17. Testing requirements

Pure JVM tests must cover at least:
- deterministic export of a minimal valid draft;
- same input → byte-identical output;
- import → export round-trip of representative accepted packages;
- invalid IDs/refs/paths blocked;
- missing string key blocked;
- unsupported `authored_signal` blocked;
- `enforceSuggestedColors=true` blocked;
- Trace declaration without step-complete authored trace support blocked;
- Help kind without required authored output blocked;
- prepared coloring reference without region geometry blocked;
- reserved fields preserved but never reported READY;
- taxonomy unknown reference blocked;
- candidate cannot partially promote when any blocking diagnostic exists;
- projected index capability summary matches production V2 truth.

Integration coverage must validate at least one current real release package through the Studio import/export pipeline and then through the normal production loader/capability gate.

## 18. V1 implementation order

1. freeze this contract;
2. implement pure authoring-domain/status/diagnostic model;
3. implement deterministic canonical package serializer/exporter to an abstract staging sink;
4. implement production-validation adapter over staged output;
5. implement import/round-trip path for accepted packages;
6. implement export evidence report;
7. implement safe atomic repository/filesystem promotion adapter;
8. wire CI gates and representative real-package fixtures;
9. only then consider a richer visual editor surface;
10. after #106 is accepted, author the first cross-age pilot content family under a separate reviewed slice.

## 19. Non-goals for #106 foundation

#106 does not:
- add bulk release lessons;
- replace Lesson Engine, Drawing Engine or Coloring Engine;
- change child-facing teaching behavior merely to simplify authoring;
- make every lesson support every mode;
- implement reserved tool/group/color/audio semantics;
- add child-facing generative AI;
- add account/cloud/network requirements;
- bypass production validators;
- treat lesson count as a success metric.

## 20. Definition of done for #106

#106 is complete only when:
- this contract is implemented without weakening V2.1/V2.2;
- Studio can import/inspect the accepted package shape;
- Studio can deterministically export a normal package into isolated staging;
- a staged export passes the exact production package/capability gates before promotion;
- capability statuses truthfully distinguish READY / INCOMPLETE / UNSUPPORTED_RUNTIME / NOT_DECLARED;
- reserved legacy fields round-trip without being advertised as working controls;
- export evidence is deterministic and machine-readable;
- invalid candidates cannot partially mutate release content;
- representative current release packages pass round-trip/integration coverage;
- CI is green on the exact candidate and on merged main;
- no bulk lesson expansion occurs under #106.

Only after this is green may the first four-age-band pilot content family begin.