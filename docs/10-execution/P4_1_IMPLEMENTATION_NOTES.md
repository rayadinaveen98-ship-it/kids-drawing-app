# P4.1 Implementation Notes

## Baseline

P4.1 starts from merged Phase 3 main `7aa53375ed4e0c248335ed26d93bc8cace72bdf2` and issue #58.

## Implemented boundary

- Added `LessonCatalogSource` discovery/existence contract while preserving `LessonPackageSource.readText` compatibility.
- `AndroidAssetLessonSource` now supports deterministic local asset discovery through `AssetManager`.
- Added `LessonCatalog` above the existing `LessonPackageLoader`.
- Catalog isolates invalid packages, rejects duplicate release lesson IDs, validates declared assets/default localization, verifies prerequisite references, exposes immutable metadata projections, and provides deterministic query methods.
- Only authored `release` packages are exposed to child-facing product projection.
- Three-package coexistence is proven with automated fixture packages using the real lesson JSON/stroke content shape; P4.1 intentionally does not ship low-quality placeholder lessons.
- `StudioHomeRepository` resolves its existing Phase 3 recommendation lesson through the catalog rather than a package-root constant.
- `ProductLessonRuntime` resolves its default lesson through the catalog before handing the package to the already-verified `LessonLabRuntimeCore`.

## Compatibility kept intentionally

P4.1 does **not** change the physically verified Phase 3 lesson session/document IDs. They remain `LessonLabRuntimeCore` compatibility identifiers until P4.2 introduces explicit multi-lesson product routing. This avoids turning content discovery into an unnecessary recovery migration.

## Deferred to later Phase 4 slices

- Canonical taxonomy migration for legacy Cute Cat metadata: P4.3 content authoring.
- Child-facing multi-lesson browsing/recommendation ranking: P4.2.
- Shipping the full seven-lesson representative set: P4.3/P4.6.
- Free Draw: P4.4.
- Prepared coloring-region fill: P4.5.

## Quality rule

Exact-head Android CI must be green before this slice is ready to merge. Automated catalog tests are evidence for catalog behavior only; they are not relabeled as physical child-content QA.