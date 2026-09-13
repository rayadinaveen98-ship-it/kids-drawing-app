# P4.1 Execution Contract — Content Catalog Foundation

**Issue:** #58  
**Parent:** #57  
**Branch:** `phase4/p4-1-content-catalog-foundation`  
**Baseline:** merged Phase 3 commit `7aa53375ed4e0c248335ed26d93bc8cace72bdf2`

## Objective

Introduce a deterministic offline catalog above the already-verified lesson package loader so the product can discover and query multiple bundled lessons without reopening Drawing Engine or Lesson Engine semantics.

## Frozen boundaries

1. `LessonPackageLoader` remains the authoritative validator/decoder for one lesson package.
2. The catalog composes package-loader results; it does not duplicate lesson execution logic.
3. `DrawingDocument`, lesson session state, teacher overlays, recovery and coloring handoff remain engine-owned.
4. Child-facing UI never parses lesson JSON or owns catalog truth.
5. Catalog content is local/offline. P4.1 adds no network/account/analytics dependency or permission.
6. `cute-cat` must become an ordinary catalog entry at the content-loading boundary. P4.1 may retain the Phase 3 fixed session/document identifiers as an explicit compatibility adapter until P4.2 introduces multi-lesson product routing.
7. A malformed package must not make valid packages unavailable.
8. Duplicate release lesson IDs are rejected deterministically rather than silently choosing one package.
9. Product-facing catalog entries are immutable projections. Raw authored JSON is not exposed to UI.
10. Only `release` lesson packages are exposed as child-facing catalog entries. Draft/review content may validate but is not product-visible.

## Discovery contract

- Production root: `lessons` in Android assets.
- `LessonCatalogSource` extends the existing read contract with deterministic child listing and asset-existence checks.
- Android discovery uses `AssetManager.list` and sorts package names before loading.
- A child directory is considered a candidate package; `LessonPackageLoader` decides whether it is valid.
- Enumeration order from the platform is never trusted.

## Catalog projection

Every accepted entry exposes at least:
- stable lesson ID + revision;
- package root;
- resolved default-locale title + summary;
- age bands;
- difficulty and estimated duration;
- category, skill and journey IDs;
- supported teaching modes;
- thumbnail/preview asset paths.

Queries must be deterministic for:
- lesson ID;
- age band;
- category;
- skill;
- difficulty;
- journey;
- teaching mode.

Stable ordering is lesson ID, then revision, then package root.

## Asset/localization validation

Catalog acceptance additionally verifies declared package assets that the single-package runtime did not need to open during Phase 2:
- thumbnail exists;
- preview exists;
- every declared strings file exists and decodes as a string map;
- default `en` strings exist for release content;
- title/summary keys exist;
- all authored narration keys used by teacher/help/completion/coloring steps exist in default strings;
- declared audio and coloring-region files exist when present.

P4.1 validates reference presence and package integrity. Migration of legacy Cute Cat taxonomy IDs to the newer canonical taxonomy is a content-authoring task for P4.3, not an excuse to break the verified Phase 3 lesson here.

## Failure model

Catalog loading returns usable entries plus typed diagnostics. Required diagnostic families:
- catalog root unavailable;
- invalid package;
- duplicate lesson ID;
- missing declared asset;
- invalid localization;
- missing localization key.

A package failure is isolated. Duplicate colliding release packages are all excluded so behavior cannot depend on asset enumeration order.

## Three-package proof

P4.1 proves three-package coexistence with deterministic automated fixtures using real `LessonPackageLoader` content shapes. It must not ship low-quality placeholder lessons merely to satisfy a count. Production catalog breadth begins in P4.3.

## Product bridge

Before P4.1 closes:
- `StudioHomeRepository` must obtain the Phase 3 recommendation lesson through the catalog rather than calling a Cute-Cat package root directly;
- `ProductLessonRuntime` must obtain its default Phase 3 lesson through the catalog rather than hard-coding `LESSON_ROOT`;
- fixed Phase 3 recovery/session/document IDs may remain until P4.2 because changing their identity now would invalidate already-verified recovery compatibility without adding child value.

## Tests

Minimum automated coverage:
1. three valid packages enumerate successfully;
2. invalid fourth package is isolated and diagnostics are preserved;
3. duplicate release lesson IDs are rejected regardless discovery order;
4. deterministic output ordering;
5. each metadata query returns the correct subset/order;
6. missing thumbnail/preview/string asset is rejected;
7. missing title/summary/narration localization key is rejected;
8. non-release package is not exposed;
9. existing bundled Cute Cat package remains loadable through catalog-backed production adapters;
10. existing Lesson/Drawing/Coloring/Gallery tests remain green.

## Completion gate

P4.1 is complete only when:
- implementation and tests satisfy #58;
- no child-facing regression is introduced;
- permission allowlist remains unchanged;
- Android CI is green on the exact final PR head;
- review threads are clean;
- PR is merged before #58 is closed.