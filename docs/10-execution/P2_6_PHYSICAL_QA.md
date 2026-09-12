# P2.6 Physical QA — Lesson Engine 0.2

**Issue:** #34  
**Target:** `0.2.0-lesson-engine`  
**Status:** **PHYSICAL-QA-IN-PROGRESS** on the responsive hotfix candidate.  
**Evidence rule:** automated/JVM/CI success must never be relabeled as physical PASS.

## Device record

- Device class: Class M
- Device/model: Samsung SM-A546E
- Android/API: API 36
- RAM: ~7.4 GB
- Refresh rate: 120 Hz
- APK commit: `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`
- APK SHA-256: `656bee202bc71d5b487f287c6dc3a8b3f05f4cc50eea58c7477fd0a59534e109`
- Tester/date: user physical-device validation / 2026-09-12

## Physical UI checkpoint

Initial `0.2.0-lesson-engine` physical candidate exposed status-bar overlap and horizontally clipped controls in portrait. Hotfix PR #41 replaced overflow-prone horizontal control rows with adaptive grids, added safe system insets, and preserved a dedicated drawing canvas.

Replacement merged-main candidate verification:

- hotfix merge commit: `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`;
- merged-main Android CI #204 / run `34685747984`: **GREEN**;
- launch screenshot on Samsung SM-A546E: **PASS** for status-bar inset, portrait fit, initial scroll position, mode/pace visibility, reserved canvas, and no clipped horizontal controls;
- initial runtime isolation diagnostic: `teacher/guide in child history: 0 · PASS`.

Physical UI checkpoint result: **PASS**.

## Required scenarios

Record `PASS`, `FAIL`, or `BLOCKED` plus a concise observation for every row.

| # | Scenario | Result | Observation / evidence |
|---|---|---|---|
| 1 | Launch Lesson Lab and load bundled Cute Cat | PASS | Responsive merged-main APK launches on Samsung SM-A546E; `Loaded cute-cat r1`; initial state `No session`; no crash; isolation diagnostic `0 · PASS`. |
| 2 | Start Draw With Me | PASS | Started in Draw With Me on device; teacher playback completed and engine reached `AwaitingChild` on step `head`. |
| 3 | Start Watch Then Draw | PASS | Started Watch Then Draw on device at Normal 1.0×; full-lesson overview entered successfully and ultimately transitioned to the child pass without crash or state corruption. |
| 4 | Start Trace & Learn | PASS | Started Trace & Learn on device at Normal 1.0×; teacher playback completed and engine reached `AwaitingChild` on step `head` with the authored trace guide active. |
| 5 | EXTRA_SLOW 0.4× playback | PASS | Draw With Me was started at Extra Slow 0.4× on the physical device. After an injected playback failure and Retry, teacher playback resumed and completed successfully at the selected pace, returning to a healthy child-turn state. |
| 6 | SLOW 0.7× playback | PASS | Fresh Draw With Me run at Slow 0.7× completed teacher playback normally on the physical device and reached the child turn without crash or state corruption. |
| 7 | NORMAL 1× playback | PASS | Draw With Me at Normal 1.0× completed teacher playback on device; runtime reported `playback COMPLETED`. |
| 8 | FAST 1.5× playback | PASS | Fresh Draw With Me run at Fast 1.5× completed teacher playback normally on the physical device and reached the child turn without crash or state corruption. |
| 9 | VERY_FAST 2× playback | PASS | Fresh Draw With Me run at Very Fast 2.0× completed teacher playback normally on the physical device and reached the child turn without crash or state corruption. |
| 10 | Draw With Me teacher demo → child stroke → next step | PASS | On device, step `head` teacher demo completed, one child stroke committed (`operations 1 · child ink 1`), tapping Done advanced deterministically to step `ears`, next teacher playback completed, and the original child operation remained preserved. Isolation diagnostic stayed `teacher/guide in child history: 0 · PASS`. |
| 11 | Watch overview Pause → Resume | PASS | During active Watch Then Draw overview on device, Pause was accepted, playback held, Resume was accepted, and overview playback continued normally before Skip Overview. |
| 12 | Watch overview Skip → child pass | PASS | Skip Overview intentionally cancelled overview playback (`playback CANCELLED`) and transitioned deterministically to `AwaitingChild` on step `head` with `overview true`; child history remained empty and isolation stayed `0 · PASS`. |
| 13 | Trace guide visible during child turn | PASS | Trace & Learn child turn showed `guide TRACE_MODE` and a visible authored head trace on the canvas after teacher playback completed. |
| 14 | Trace/help guide does not increase child operation count | PASS | With the Trace guide visibly active, runtime remained `operations 0 · child ink 0` and `teacher/guide in child history: 0 · PASS`, physically proving the guide stays outside child artwork/history. |
| 15 | Replay during valid child turn | PASS | In Trace & Learn `AwaitingChild`, Replay re-ran teacher playback to `COMPLETED`, then restored the authored `TRACE_MODE` guide. Child history remained `operations 0 · child ink 0` and isolation stayed `0 · PASS`. |
| 16 | Help escalation skips missing authored levels correctly | PASS | Repeated Help+ on Trace & Learn advanced authored help to level 4. A further Help+ was deterministically rejected with `HELP_NOT_AVAILABLE: No higher authored help level is available for step head`; active guide was `...head-help-h4`. |
| 17 | Reduce Help | PASS | From Help level 4, Help− moved to the next lower authored level, level 1, skipping unavailable intermediate levels. Runtime remained `HelpActive`, child history stayed empty, and the base trace overlay was restored. |
| 18 | Dismiss Help | PASS | Dismiss Help returned the engine to `AwaitingChild · help 0` and restored the base `TRACE_MODE` guide without altering child history. |
| 19 | Invalid Skip rejected on non-skippable step | PASS | On `step 1: head`, Skip Step was rejected with `SKIP_NOT_ALLOWED`; state remained `AwaitingChild`, the Trace guide stayed active, child history stayed empty, and isolation remained `0 · PASS`. |
| 20 | Valid Skip accepted on `face` step | PASS | On the fresh Trace & Learn run, Skip Step was accepted on the authored skippable `face` step and advanced to `step 4: body_tail`; next teacher playback completed, base Trace guide appeared for `body_tail`, history stayed `operations 0 · child ink 0`, and isolation remained `0 · PASS`. |
| 21 | Invalid Done/command path rejected without state corruption | PASS | At `AwaitingChild · step 4: body_tail · help 0`, Dismiss Help was intentionally invoked with no active Help level. Device showed the expected `HELP_NOT_AVAILABLE` rejection; the step/session remained intact with no observed history corruption. |
| 22 | Background → foreground preserves artwork/session | PENDING | |
| 23 | Process recreation/relaunch preserves child artwork/session progress | PENDING | |
| 24 | Recreated teacher request is fresh; stale callback cannot advance | PENDING | |
| 25 | Inject teacher playback failure → Retry → playback resumes | PASS | During active Draw With Me teacher playback at Extra Slow 0.4×, Inject Playback Failure produced the expected recoverable failure state. Retry was accepted, teacher playback restarted/resumed, completed successfully, and the engine returned to `AwaitingChild` with history/isolation still healthy (`teacher/guide in child history: 0 · PASS`). |
| 26 | Drawing complete reaches post-drawing choice | PENDING | |
| 27 | Coloring handoff unavailable → returns to retryable choice | PENDING | |
| 28 | Post-drawing state survives recreation | PENDING | |
| 29 | Persisted child artwork contains zero TEACHER_GENERATED ink operations | PENDING | Live history remains isolated after Draw With Me, Watch Then Draw overview cancellation, Trace guide display, Replay, Help level 4, invalid Skip rejection, valid Skip advancement, invalid-command rejection, and failure/retry; persistence/relaunch proof still required. |
| 30 | Art Lab launcher still opens and draws | PENDING | |
| 31 | Quality Lab launcher still opens | PENDING | |
| 32 | No crash/deadlock during full matrix | PENDING | |

## Isolation audit

At least once in Trace & Learn and once after Help level 4:

- record visible `teacher/guide in child history` diagnostic;
- expected value: `0`;
- save/background/relaunch;
- confirm child operation history still contains only child-authored ink plus child erase/clear operations.

Current evidence:
- launch/idle diagnostic: `0 · PASS`;
- Draw With Me after one committed child stroke: `operations 1 · child ink 1`, `teacher/guide in child history: 0 · PASS`;
- Watch Then Draw after Skip Overview: `operations 0 · child ink 0`, `teacher/guide in child history: 0 · PASS`;
- Trace & Learn with a visible `TRACE_MODE` guide: `operations 0 · child ink 0`, `teacher/guide in child history: 0 · PASS`;
- Trace & Learn Replay completed and rehydrated the trace guide with history still `operations 0 · child ink 0`, isolation `0 · PASS`;
- Help level 4 displayed `guide HELP` / authored `...head-help-h4` while history remained `operations 0 · child ink 0` and isolation stayed `0 · PASS`;
- Help− returned to authored level 1 and Dismiss Help returned to base trace mode, both with history still isolated;
- invalid Skip rejection on `head` and valid Skip advancement from `face` to `body_tail` both preserved `operations 0 · child ink 0` and isolation `0 · PASS`;
- invalid-command rejection at `body_tail` preserved the active lesson state without observed history corruption;
- injected teacher-playback failure followed by Retry returned to a healthy child turn with isolation still `0 · PASS`;
- all five teaching paces have now completed physically on device;
- save/relaunch isolation proof remains **PENDING**.

## Lifecycle audit

Perform one recovery while:

1. teacher playback is active;
2. child turn is active after at least one committed stroke;
3. Help is active;
4. drawing is complete / post-drawing choice is active.

For each case, confirm child artwork is preserved and transient teacher/guide work is safely restarted/rehydrated rather than persisted as artwork.

Result: **PENDING**.

## Release decision

Physical milestone gate: **IN PROGRESS**.

Do not tag `v0.2.0-lesson-engine` as the verified final milestone until this sheet is updated with real-device evidence or an explicit milestone-approved hardware exception is documented in Git.
