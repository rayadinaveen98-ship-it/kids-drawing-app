# P4.1 CI Gates

P4.1 uses the repository Android CI as its merge gate.

Required before merge:
- committed JSON parse succeeds;
- Drawing Engine Ink boundary verification succeeds;
- JVM unit tests succeed, including `LessonCatalogTest`;
- Android lint succeeds;
- debug APK builds;
- instrumentation APK compiles;
- profile APK builds;
- permission allowlist remains unchanged and passes;
- exact final PR-head run is green;
- PR review threads are empty/resolved.

No physical QA is required for the catalog-only slice unless implementation changes expose a new child-facing behavior that automated/instrumented coverage cannot validate. P4.7 remains the Phase 4 physical milestone gate.