# Content Library V2.2 — Catalog Index & Taxonomy Contract

Status: **FROZEN FOR IMPLEMENTATION**  
Parent: #103  
Active slice: #105  
Verified baseline: `b772fd750b5e67eb74c42f39b7505d8f3d16941e` / merged-main Android CI #714 GREEN

## 1. Purpose

Catalog Index V2 separates **child discovery metadata** from **full teaching-package execution data** so the library can grow from 24 lessons toward 100/250/500 entries without Home, browsing, filtering, recommendations or lesson cards loading every stroke catalog, Help payload and coloring geometry.

The accepted Lesson Engine and `LessonPackageLoader` remain the execution authority. Catalog Index V2 is a validated projection of release content, never a competing source of teaching truth.

## 2. Non-negotiable boundaries

1. Child discovery must not need to decode all full `LessonRuntimePackage` objects.
2. Selecting/executing a lesson still loads and validates its full lesson package before teaching begins.
3. Catalog capability fields are generated from validated lesson/runtime truth; they are not independently hand-authored promises.
4. An index entry cannot make an unsupported option visible.
5. Missing, duplicate, malformed or stale index data fails closed for the affected entry/build; runtime must not invent metadata.
6. Runtime code must not branch on individual lesson IDs.
7. The child app remains offline-first and deterministic.
8. No new account, network or Android permission is introduced.
9. #105 adds no bulk lesson expansion.

## 3. Runtime architecture

```text
Bundled catalog/lesson-index-v2.json
        ↓
CatalogIndexV2Loader
        ↓
CatalogIndexV2Snapshot
        ↓
Home / filters / categories / journeys / collections / recommendations
        ↓
selected lesson identity + packageRef
        ↓
LessonPackageLoader.load(packageRef)
        ↓
existing Lesson Engine / Drawing / Help / Coloring / persistence
```

The index is intentionally small enough to load as one deterministic local asset. Full teacher strokes, guide geometry, coloring regions and child state are prohibited from the index.

## 4. Index identity and versioning

Top-level index fields:

```text
schemaVersion
contentApi
entries[]
```

Each entry is identified by `(lessonId, revision)` and contains:

```text
lessonId
revision
status
minimumContentApi
packageRef

title
titleKey
summary
summaryKey

ageBands[]
difficulty
estimatedMinutes
categoryIds[]
skillIds[]
journeyIds[]
collectionIds[]
prerequisiteLessonIds[]
tags[]
contentFamilyId?

supportedModes[]
capabilitySummary
thumbnailRef
previewRef
```

Release runtime exposes only `status=release` entries.

`packageRef` is the relative package root used by the full package loader. `thumbnailRef` and `previewRef` are lightweight asset references suitable for discovery surfaces.

## 5. Capability summary

`capabilitySummary` is generated from a package that already passes the V2.1 capability-completeness contract.

```text
teachingModes[]
helpAvailable
traceReady
coloring: NONE | FREEHAND | PREPARED | GUIDED_PREPARED
voiceAudio: NOT_SUPPORTED | READY
```

Rules:
- `teachingModes` must exactly match validated supported modes.
- `helpAvailable=true` only when at least one executable authored Help entry exists.
- `traceReady=true` only when Trace & Learn is advertised and every structured trace step satisfies the V2.1 authored-geometry rule; otherwise false.
- coloring classification is derived from accepted coloring content/geometry semantics, never manually typed into the index.
- `voiceAudio` remains `NOT_SUPPORTED` until an accepted end-to-end voice playback contract exists, even if package audio files are present.

## 6. Generated-data / drift rule

The checked-in bundled index is generated evidence, not an authoring surface.

CI must:
1. load the complete release catalog through the existing full package validator;
2. apply `LessonCapabilityValidator`;
3. project deterministic Catalog Index V2 entries;
4. serialize them with stable ordering;
5. compare the generated result with the committed `catalog/lesson-index-v2.json`;
6. fail when the committed index is missing, malformed or stale.

This makes the full package the source of truth while keeping production discovery lightweight.

## 7. Taxonomy contract

### Age bands
Existing accepted bands remain unchanged:
- `little_artists` — 4–5;
- `creative_explorers` — 6–7;
- `growing_artists` — 8–9;
- `young_artists` — 10–12.

Age is eligibility/presentation metadata, not an ability label.

### Categories
Categories answer **what do I want to draw?** Primary vocabulary:
- Drawing Basics
- Animals
- Nature
- Vehicles & Machines
- People & Characters
- Food & Everyday Things
- Fantasy & Magic
- Space & Science
- Places & Buildings
- Stories & Scenes
- Patterns & Decoration
- Comics & Expression
- Design & Invent
- Culture & Celebrations

Existing narrower IDs may remain as secondary taxonomy aliases during migration; #105 must report unknown/unregistered IDs rather than silently discarding them.

### Skills
Skills answer **what artistic idea does this practice?** Initial families:
- line control
- curves
- basic shapes
- shape construction
- placement
- symmetry
- proportion
- overlap/depth
- texture/pattern
- facial expression
- pose/gesture
- simple perspective
- composition
- visual storytelling

### Journeys
Journeys are authored learning sequences. Prerequisites may order a journey but must not turn broad creative browsing into a locked progression system.

### Collections
Collections are editorial/fun groupings. They never alter lesson identity, completion truth or prerequisites.

### Content families
A content family groups related subjects across age/depth while preserving distinct lesson IDs. It is discovery/authoring metadata only; runtime never special-cases a family ID.

`collectionIds` and `contentFamilyId` are optional additive metadata. Existing packages remain valid with empty/null values.

## 8. Taxonomy registry

V2.2 introduces a deterministic taxonomy registry for categories, skills, journeys, collections and content families. The registry owns display labels and editorial metadata; lesson packages/index entries own only stable IDs.

Validation must report:
- missing taxonomy references;
- duplicate taxonomy IDs;
- prerequisite references to absent release lessons;
- prerequisite cycles;
- journey ordering inconsistencies where applicable.

Taxonomy validation must not rewrite lesson data or infer permanent child ability.

## 9. Query behavior

Catalog snapshot queries must be deterministic and pure. Required query dimensions:
- lesson ID;
- age band;
- category;
- skill;
- difficulty;
- journey;
- collection;
- content family;
- supported teaching mode;
- Help availability;
- Trace readiness;
- coloring capability.

Stable ordering is `(lessonId, revision, packageRef)` unless a higher-level product policy explicitly supplies another deterministic sort.

## 10. Lazy package loading

Catalog Index V2 stores no `LessonRuntimePackage` map.

A separate selected-package resolver loads the full package only for:
- lesson preview when deeper package data is required;
- lesson execution;
- explicit validation/tooling;
- a future proven prefetch policy.

Existing callers that require a full package must transition through lesson identity/packageRef, not through an eager in-memory catalog of all packages.

## 11. Scale acceptance

Pure JVM tests must construct synthetic valid index snapshots at:
- 100 entries;
- 250 entries;
- 500 entries.

Tests prove deterministic decode/filter/query behavior and absence of full-package dependencies. Wall-clock microbenchmark thresholds are not release gates in V2.2 because CI hardware is variable; structural laziness and correctness are the gate.

## 12. Content-health evidence

V2.2 extends release evidence with deterministic counts for:
- total release entries;
- age-band coverage;
- categories;
- skills;
- difficulty distribution;
- supported modes;
- Help coverage;
- Trace readiness;
- coloring capability;
- journey sizes;
- collection sizes;
- content-family coverage;
- missing/unknown taxonomy references;
- duplicate identities;
- prerequisite graph failures;
- committed-index drift.

Coverage gaps are reported honestly; they are not hidden by recommendation logic.

## 13. Migration compatibility

The current 24 release lessons must project to Catalog Index V2 without changing their teaching behavior, lesson IDs, revisions, completion truth or artwork ownership.

Optional `collectionIds` / `contentFamilyId` additions are backward compatible and may remain empty during the first migration. Existing category/skill/journey IDs are inventoried before any vocabulary normalization.

## 14. Definition of done for #105

#105 is complete only when:
- this contract is implemented without weakening V2.1;
- one bundled metadata-only V2 index is consumed by production discovery;
- production discovery no longer requires eager full-package loading;
- selected lessons still load/validate through `LessonPackageLoader` before execution;
- committed index and generated full-package projection match exactly in CI;
- taxonomy registry/reference/graph validation is deterministic;
- 100/250/500 synthetic scale tests pass;
- current 24 release lessons retain equivalent discovery eligibility and lesson execution behavior;
- content-health evidence is produced in CI;
- Android CI is green on the exact candidate and on merged main;
- no new lessons are added under #105.

Only then may #106 Content Studio begin.
