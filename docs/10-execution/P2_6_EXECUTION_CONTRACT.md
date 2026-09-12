# P2.6 Execution Contract — Lesson Lab, Physical Verification + `0.2.0-lesson-engine`

**Issue:** #34  
**Parent epic:** #28  
**Starting branch:** `phase2/lesson-lab-release`  
**Starting baseline:** fresh `main` after P2.5 merge and status/handoff synchronization  
**Target milestone:** `0.2.0-lesson-engine`

## Objective

Expose the completed Lesson Engine 0.2 through a real internal Android Lesson Lab, verify its integration with the frozen Drawing Engine and authored Cute Cat package, collect automated and physical evidence, and package a reproducible tagged milestone APK.

## Non-negotiable boundaries

- The Lesson Lab is an engineering shell, not a second source of product truth.
- `LessonSessionEngine` owns lesson state and legal transitions.
- `DrawingDocumentEngine` owns child artwork/history.
- `TeacherPlaybackSession`/Drawing Engine own teacher playback runtime.
- Teacher/trace/help overlays never enter child document operations.
- Recovery restores child artwork before reactivating transient lesson runtime.
- The Lab must use the real bundled `lessons/cute-cat` package; no fake lesson state.
- All five frozen `TeachingPace` values and all three `TeachingMode` values must be selectable.
- Core verification must remain offline and permission-minimal.
- Existing Phase 1 Art Lab/Quality Lab functionality must not regress.

## Implementation slices

### P2.6-A — Lesson Lab integration runtime

Create an app-owned internal integration controller that wires:
- `LessonPackageLoader` + `AndroidAssetLessonSource`;
- `LessonSessionEngine`;
- `DrawingDocumentEngine`;
- `DrawingToolEngine`;
- `TeacherPlaybackSession`;
- `AtomicDrawingDocumentStore`;
- `AtomicLessonSessionStore`;
- `LessonSessionAutosaveCoordinator`;
- `LessonRecoveryCoordinator`.

The controller must translate typed Lesson Engine events into runtime effects without allowing Compose/UI to invent session state.

### P2.6-B — Lesson Lab Android surface

Provide a dedicated `LessonLabActivity` launcher with:
- mode selector: Draw With Me / Watch Then Draw / Trace & Learn;
- five-pace selector;
- New/Start/Recover/Save & Exit controls;
- Pause/Resume, Replay, Help, Reduce Help, Dismiss Help, Skip/Done controls gated by engine rejection rather than UI truth;
- real DrawingSurface child input;
- real teacher playback overlay;
- static trace/help guide overlay;
- visible session state, step, help, overview, playback request, document-operation count and persistence/recovery status;
- explicit teacher/guide/child operation-isolation diagnostic;
- failure injection for teacher playback to prove Retry path;
- post-drawing choices/handoff diagnostics without pretending Coloring Engine 0.4 exists.

### P2.6-C — Automated integration verification

Required tests include:
- all 3 modes × all 5 paces can start through the integration runtime;
- teacher request events are executed and completed back into Lesson Engine;
- committed child strokes enter child history and send typed completion signals;
- guide/teacher strokes do not enter child history;
- pause/resume/replay/help/skip paths remain engine-owned;
- lifecycle autosave and child-document-first recovery work through real stores;
- stale teacher callback rejection survives recreation;
- corrupt newest session falls back to backup where available;
- teacher failure → Retry uses a fresh request;
- post-drawing handoff failure returns to retryable choice;
- no existing Art Lab/Quality Lab tests regress.

### P2.6-D — Milestone packaging

Before tagging:
- set `versionName = "0.2.0-lesson-engine"`;
- monotonically advance `versionCode` from 11;
- exact-head Android CI green;
- debug/internal and release-like profile APK artifacts produced;
- permission allowlist green;
- APK SHA-256 and size recorded;
- release notes committed under `docs/releases/0.2.0-lesson-engine.md`;
- `PROJECT_STATUS.md` and `docs/HANDOFF.md` updated;
- tag points at the verified milestone commit, not an earlier implementation head.

## Physical verification matrix

Use the available Class-M Android device unless a different device is explicitly documented.

Required physical scenarios:
1. launch Lesson Lab and load Cute Cat;
2. start each of the three modes;
3. verify all five pace selections execute without crash/deadlock;
4. Draw With Me: teacher demo → child stroke → next step;
5. Watch Then Draw: overview pause/resume/skip and child pass;
6. Trace & Learn: trace guide visible while child operation count excludes guide strokes;
7. Replay during a valid child turn;
8. Help escalation, Reduce Help and Dismiss Help;
9. valid/invalid Skip/Done behavior;
10. background/foreground while active;
11. process recreation/relaunch with preserved child artwork/session progress;
12. injected teacher playback failure → Retry;
13. drawing-complete post-choice state survives recreation;
14. no teacher/guide operations appear in persisted child artwork.

If tooling cannot programmatically perform a required real-device action from CI, the result must remain **PENDING-PHYSICAL** until recorded from the device. Automated/emulator success must never be relabeled as physical PASS.

## Merge/release gates

P2.6 implementation PR may merge only when:
- exact-head CI is green;
- no unresolved review threads/change requests;
- Lesson Lab uses real engines/content/stores;
- automated acceptance matrix passes;
- no P0/P1 milestone blocker is known.

The final `0.2.0-lesson-engine` milestone may be tagged only when:
- merged `main` CI is green;
- required physical evidence is recorded or explicitly blocked as permitted by the milestone contract;
- installable milestone APK exists;
- checksum/version/commit evidence is recorded;
- release notes and handoff are synchronized.

## Definition of done

A tagged `0.2.0-lesson-engine` build can load Cute Cat, exercise all three teaching modes at all five paces, preserve committed child artwork across lifecycle/process restoration, keep teacher/trace/help overlays isolated from artwork history, and reproduce its build/test evidence from Git + CI.
