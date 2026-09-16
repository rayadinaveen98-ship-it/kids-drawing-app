# Content Studio V1

`content-studio` is internal JVM authoring/release tooling. It is deliberately **not** an Android runtime dependency and must never be added to the app with `implementation(project(":content-studio"))` or `api(project(":content-studio"))`.

The module compiles the same pure lesson model/content validator sources used by the Android product, excluding the Android `AssetManager` adapter. This keeps package, capability, taxonomy, prerequisite and content-quality truth single-source without shipping Studio code in the child APK.

## Run the gate

```bash
./gradlew :content-studio:test
```

The normal repository `:app:testDebugUnitTest` gate also depends on this test task so Android CI receives Content Studio evidence automatically.

Studio tests read the accepted release corpus from `app/src/main/assets` and publish deterministic evidence into:

```text
app/build/reports/content-quality/content-studio-v1-evidence.json
app/build/reports/content-quality/content-studio-v1-health.txt
```

## Workflow

```text
accepted package / author draft
  -> ContentStudioDraft
  -> deterministic canonical export
  -> isolated ContentStudioStagedPackage
  -> production LessonPackageLoader
  -> LessonCapabilityValidator
  -> full LessonCatalog checks
  -> ContentQualityAnalyzer
  -> Catalog Index V2 projection + strict load
  -> READY or BLOCKED evidence
  -> complete promotion plan only when READY
```

A promotion plan always contains the full staged package, stale package-file deletions, and the regenerated `catalog/lesson-index-v2.json`. A BLOCKED candidate cannot invoke the mutation target.

## Capability truth

Authoring reports use only:

- `READY`
- `INCOMPLETE`
- `UNSUPPORTED_RUNTIME`
- `NOT_DECLARED`

Reserved legacy fields such as `childTurn.toolPreset`, `teacher.playAsGroup`, and non-enforced `suggestedColorRoles` are preserved during accepted-package round trips but are not presented as working controls.

`authored_signal`, `enforceSuggestedColors=true`, and working voice/audio capability remain release-blocked until separate runtime contracts are accepted.

## Release discipline

Do not add bulk lessons in this module milestone. Content Studio V1 exists to make later authoring deterministic, reviewable and capability-complete. The first cross-age pilot content family starts only after #106 is accepted on exact-candidate and merged-main CI.
