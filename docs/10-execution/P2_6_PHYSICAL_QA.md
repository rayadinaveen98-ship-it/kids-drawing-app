# P2.6 Physical QA — Lesson Engine 0.2

**Issue:** #34  
**Target:** `0.2.0-lesson-engine`  
**Status:** **PENDING-PHYSICAL** until executed on a real Android device.  
**Evidence rule:** automated/JVM/CI success must never be relabeled as physical PASS.

## Device record

- Device class: Class M preferred
- Device/model: PENDING
- Android/API: PENDING
- RAM: PENDING
- Refresh rate: PENDING
- APK commit: PENDING FINAL RELEASE COMMIT
- APK SHA-256: PENDING FINAL ARTIFACT
- Tester/date: PENDING

## Required scenarios

Record `PASS`, `FAIL`, or `BLOCKED` plus a concise observation for every row.

| # | Scenario | Result | Observation / evidence |
|---|---|---|---|
| 1 | Launch Lesson Lab and load bundled Cute Cat | PENDING | |
| 2 | Start Draw With Me | PENDING | |
| 3 | Start Watch Then Draw | PENDING | |
| 4 | Start Trace & Learn | PENDING | |
| 5 | EXTRA_SLOW 0.4× playback | PENDING | |
| 6 | SLOW 0.7× playback | PENDING | |
| 7 | NORMAL 1× playback | PENDING | |
| 8 | FAST 1.5× playback | PENDING | |
| 9 | VERY_FAST 2× playback | PENDING | |
| 10 | Draw With Me teacher demo → child stroke → next step | PENDING | |
| 11 | Watch overview Pause → Resume | PENDING | |
| 12 | Watch overview Skip → child pass | PENDING | |
| 13 | Trace guide visible during child turn | PENDING | |
| 14 | Trace/help guide does not increase child operation count | PENDING | |
| 15 | Replay during valid child turn | PENDING | |
| 16 | Help escalation skips missing authored levels correctly | PENDING | |
| 17 | Reduce Help | PENDING | |
| 18 | Dismiss Help | PENDING | |
| 19 | Invalid Skip rejected on non-skippable step | PENDING | |
| 20 | Valid Skip accepted on `face` step | PENDING | |
| 21 | Invalid Done/command path rejected without state corruption | PENDING | |
| 22 | Background → foreground preserves artwork/session | PENDING | |
| 23 | Process recreation/relaunch preserves child artwork/session progress | PENDING | |
| 24 | Recreated teacher request is fresh; stale callback cannot advance | PENDING | |
| 25 | Inject teacher playback failure → Retry → playback resumes | PENDING | |
| 26 | Drawing complete reaches post-drawing choice | PENDING | |
| 27 | Coloring handoff unavailable → returns to retryable choice | PENDING | |
| 28 | Post-drawing state survives recreation | PENDING | |
| 29 | Persisted child artwork contains zero TEACHER_GENERATED ink operations | PENDING | |
| 30 | Art Lab launcher still opens and draws | PENDING | |
| 31 | Quality Lab launcher still opens | PENDING | |
| 32 | No crash/deadlock during full matrix | PENDING | |

## Isolation audit

At least once in Trace & Learn and once after Help level 4:

- record visible `teacher/guide in child history` diagnostic;
- expected value: `0`;
- save/background/relaunch;
- confirm child operation history still contains only child-authored ink plus child erase/clear operations.

Result: **PENDING**.

## Lifecycle audit

Perform one recovery while:

1. teacher playback is active;
2. child turn is active after at least one committed stroke;
3. Help is active;
4. drawing is complete / post-drawing choice is active.

For each case, confirm child artwork is preserved and transient teacher/guide work is safely restarted/rehydrated rather than persisted as artwork.

Result: **PENDING**.

## Release decision

Physical milestone gate: **PENDING-PHYSICAL**.

Do not tag `v0.2.0-lesson-engine` as the verified final milestone until this sheet is updated with real-device evidence or an explicit milestone-approved hardware exception is documented in Git.
