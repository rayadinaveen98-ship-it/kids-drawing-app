# P4.2 Execution Contract — Catalog Discovery, Journeys & Recommendations

**Issue:** #59  
**Parent:** #57  
**Depends on:** #58 (complete)  
**Branch:** `phase4/p4-2-catalog-discovery`  
**Baseline:** merged P4.1 main `c271ad8b519ec53d99fa18bf140aaf252a8d95c9`

## Objective

Turn the P4.1 offline catalog into a generic multi-lesson product surface: deterministic recommendations, available-content category/journey discovery, lesson selection and stable lesson-specific routing/recovery without lesson-ID special cases.

## Frozen principles

1. Catalog metadata owns discovery membership; UI must never hard-code lesson lists.
2. Resume work outranks fresh discovery/recommendations.
3. Recommendation ranking is deterministic and local. No ML/network/profile upload.
4. Easier content may remain visible to older children; age fit affects ranking/presentation rather than permanently hiding safe content.
5. Lesson execution still uses the verified Lesson Engine. This slice may parameterize storage/runtime identity only where required for correct multi-lesson routing; session semantics are not reopened.
6. Teacher/help overlays remain non-authoritative and never enter child artwork.
7. Product routes carry stable `(lessonId, revision)` identities, not file names or screen-owned package data.
8. `cute-cat` r1 must preserve Phase 3 persisted session/document IDs so upgrading to P4.2 does not strand verified 0.3 work.
9. New lessons receive deterministic lesson-specific session/document IDs.
10. Categories/journeys are shown only when installed release content exists for them.
11. Taxonomy presentation labels/icons may be mapped by taxonomy ID, but membership and lesson order come from catalog metadata.
12. P4.2 ships no placeholder production lessons; multi-item behavior is proven in deterministic tests until P4.3 adds real content.

## Runtime identity compatibility contract

`LessonLabRuntimeCore` currently owns one static Cute Cat identity. True multi-lesson routing requires an immutable runtime identity:

- `sessionId`
- `documentId`

The core accepts this identity as constructor state while retaining the legacy identity as its default for Lesson Lab/tests.

Product identity policy:
- `cute-cat` revision 1 → existing legacy Phase 3 IDs exactly;
- all other lesson/revision pairs → deterministic sanitized IDs derived from lesson ID + revision.

`newLessonDocument()` derives metadata from the loaded package and document ID from the runtime identity. Every recover/start/signal/handoff path must use the instance identity. No Lesson Engine state-transition rules change.

## Recommendation contract

Ranking inputs are explicit and testable. In descending significance:
1. exact age-band fit;
2. child-interest/category/tag match;
3. preferred teaching mode supported;
4. difficulty closeness to an age-appropriate target;
5. stable lesson ID/revision tie-break.

Resume state is not a ranking score. An active coloring session is primary; otherwise the newest compatible active drawing session is primary; only when no resumable work exists does the highest-ranked fresh recommendation become primary.

## Product models

Home projection exposes:
- ranked recommendations;
- primary recommendation;
- optional drawing resume tied to its lesson identity;
- optional coloring resume tied to its lesson identity;
- available categories with lesson counts;
- available journeys with lesson sequence/progress projection;
- safe content message for empty/partial catalogs.

## Discovery routes

P4.2 adds generic product routes for:
- category browser/list;
- journey browser/list;
- selected lesson preview/start.

A route selection stores stable lesson identity in saveable state. The selected lesson is resolved again from the catalog/runtime factory rather than storing a raw runtime package in UI state.

## Recovery scanning

Because persisted stores are keyed by IDs, Home checks deterministic identities for installed release catalog entries:
- load each lesson session ID and retain compatible active snapshots;
- load each corresponding coloring session ID and retain compatible active snapshots;
- coloring wins over drawing when both represent an active handoff chain;
- otherwise newest saved compatible session wins;
- corrupt/missing state for one lesson cannot poison other lessons.

## Visual/product contract

- No hard-coded `CuteCatPreview` for generic lesson cards.
- Until production thumbnail decoding is introduced, cards may use deterministic metadata-derived decorative art/icons; lesson identity/title must remain clear.
- Home remains a calm Studio Lobby, not a dense database grid.
- Younger ages receive fewer visible secondary options per section; older ages may see more compact cards.

## Tests

Minimum automated coverage:
1. legacy Cute Cat runtime identity remains byte-for-byte ID compatible;
2. non-Cute-Cat identities are deterministic and collision-safe for accepted lesson IDs/revisions;
3. runtime core uses instance identity for new document, start and recovery while legacy defaults keep all existing tests green;
4. deterministic ranking changes predictably for age, interests and teaching mode;
5. tie-break remains stable regardless catalog input order;
6. category/journey projections contain only installed content and no hard-coded membership;
7. active coloring outranks active drawing and fresh recommendation;
8. newest compatible drawing resume outranks fresh recommendation;
9. corrupt/missing one-lesson state does not suppress other content;
10. selected lesson identity resolves the matching catalog package/runtime;
11. current Phase 3 Cute Cat onboarding→lesson route remains regression-green.

## Completion gate

P4.2 is complete only when:
- #59 acceptance is implemented;
- exact final PR-head Android CI is green;
- review threads are clean;
- PR is merged;
- merged-main CI is green before #59 closes.