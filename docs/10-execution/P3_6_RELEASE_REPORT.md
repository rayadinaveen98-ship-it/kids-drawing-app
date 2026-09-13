# P3.6 Release / Implementation Report — 0.3.0 Vertical Slice

## Release identity

- Target: `0.3.0-vertical-slice`
- versionCode: `13`
- Release PR: #56
- Executable candidate: `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`
- Candidate Android CI: #307 / run `34706767213` — GREEN
- Profile artifact ID: `10301836886`
- Profile artifact: `kids-drawing-0.3.0-vertical-slice-profile`
- APK: `Kids_Drawing_0.3.0_Vertical_Slice-profile.apk`
- APK size: `16080880` bytes
- APK SHA-256: `2f622c116813c7a5830109994c104d4bb1623dc41f57cb4f463c4d6e5e385961`

## Scope delivered

P3.6 closes the first complete production child journey built across P3.1–P3.5:

fresh setup → personalized Studio Home → Cute Cat recommendation → lesson preview → companion-led guided drawing → Help → post-drawing choice → real coloring → completion → local Gallery → restart-safe artwork reopen.

The milestone remains offline-first and local-only. It adds no account requirement, ads, behavioral analytics, network dependency, or sensitive permission.

## Release hardening completed in P3.6

### Guided workspace sizing defect

The first physical 0.3 candidate exposed a release-blocking child-turn layout bug: a primary `Done` button used a fill-max-size child and consumed the weighted drawing area. P3.6 fixed the primary action sizing without changing lesson/drawing engine truth.

### Coloring workspace hardening

The next physical candidate exposed a second release-blocking cluster:

- coloring controls could collapse the canvas;
- an ACTIVE coloring session could be covered by a duplicate Gallery completion overlay;
- coloring palette/tool settings shared the drawing tool engine and leaked color/width into a later drawing lesson;
- Color With Me was not sufficiently differentiated from Color Myself.

The final candidate fixes those issues by:

- preserving a usable coloring canvas with compact age-aware controls;
- using one active Finish Coloring transaction;
- sharing the authoritative editable DrawingDocument while isolating coloring tool state from lesson drawing tool state;
- adding semantic, persisted-stroke-driven Color With Me guidance without inventing region-fill assets.

## Architecture preserved

- UI does not own artwork/history/lesson truth.
- Teacher/help overlays remain non-authoritative and never become child artwork.
- Drawing and coloring share the same editable document truth but maintain separate tool state.
- Coloring undo/redo remains bounded to coloring operations and cannot cross into original line-art history.
- Gallery promotion creates a distinct stable Gallery document identity while preserving editable operations/provenance.
- Gallery previews are derived local PNGs only; missing previews use fallback UI and never reconstruct authoritative art.
- Active working lesson/coloring documents remain protected from Gallery deletion.

## Verification

### Automated / CI

Candidate CI #307 passed the complete Android gate for the executable candidate, including:

- JSON/spec validation;
- Drawing Engine Ink boundary check;
- JVM/unit tests;
- lint;
- debug APK build;
- instrumentation APK compilation;
- profile/release-like APK build;
- permission allowlist;
- milestone package/hash/size evidence;
- debug/profile artifact upload.

### Physical

All 41 P3.6 scenarios passed. Major coverage includes:

- fresh install/onboarding and onboarding process recovery;
- teacher-input gate, child-turn drawing, Help overlay isolation;
- complete Cute Cat drawing/coloring/completion/Gallery journey;
- color-line protection and coloring-only Undo/Redo;
- drawing/coloring background + force-stop recovery;
- Continue Drawing / Continue Coloring priority and exact state restore;
- Gallery completion, reopen, restart, deletion isolation, and missing-preview fallback;
- all four age-band layout spot checks and both handedness choices;
- increased font size, narration off, reduced animations;
- full Airplane Mode journey;
- zero teacher/help contamination and no lost operations;
- Art Lab, Lesson Lab, and Quality Lab regression smoke checks;
- no observed crash, freeze, dead screen, or deadlock across the matrix.

See `P3_6_PHYSICAL_QA.md` for row-level evidence.

## Scenario #27 controlled exception

Missing-preview fallback was physically induced with the debug APK produced from the **same executable source commit** because Android `run-as` cannot access a non-debuggable profile app's private data directory. The test removed only the derived `files/gallery-previews/*.png` file. The Gallery fallback appeared and the authoritative drawing/color document still reopened correctly. This is a tooling/build-visibility exception, not a product exception.

## Hardware evidence limitation

The physical test device's exact model/RAM/refresh-rate were not re-captured during P3.6. This report does not reuse earlier milestone hardware metadata as if it were newly observed. Physical product behavior was directly tested on the installed candidate; device metadata remains an explicit evidence limitation.

## Release gate

Physical QA: **PASS**.

Before final release administration is complete:

1. Require Android CI green on the exact final PR head containing only the final QA/report documentation commits after the already-verified executable candidate.
2. Ensure PR #56 has no unresolved review threads.
3. Mark PR ready and squash-merge guarded by the exact head SHA.
4. Verify merged-main CI green.
5. Close issue #48 and Phase 3 epic #42 when the merged-main gate is satisfied.
6. Create/verify Git tag `v0.3.0-vertical-slice` if repository tooling exposes tag creation; otherwise record that tag creation is the only administrative exception.

The executable release candidate itself remains `e54f8dabfd9a9f1a7dab5b382cedb73438f151cd`; later documentation-only commits do not change the APK under physical test.
