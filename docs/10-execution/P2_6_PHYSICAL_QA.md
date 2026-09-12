# P2.6 Physical QA — Lesson Engine 0.2

**Issue:** #34  
**Target:** `0.2.0-lesson-engine`  
**Status:** **PHYSICAL-QA-PASS**  
**Evidence rule:** automated/JVM/CI success is not relabeled as physical PASS; physical rows below are based on the 2026-09-12 Samsung device validation.

## Device record

- Device class: Class M
- Device/model: Samsung SM-A546E
- Android/API: API 36
- RAM: ~7.4 GB
- Refresh rate: 120 Hz
- Verified APK commit: `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`
- APK SHA-256: `656bee202bc71d5b487f287c6dc3a8b3f05f4cc50eea58c7477fd0a59534e109`
- Tester/date: user physical-device validation / 2026-09-12

## Physical UI checkpoint

The first `0.2.0-lesson-engine` candidate exposed status-bar overlap and horizontally clipped controls in portrait. Hotfix PR #41 replaced overflow-prone horizontal rows with adaptive grids, added safe system insets, and preserved a dedicated drawing canvas.

Replacement candidate verification:

- hotfix merge commit: `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`;
- merged-main Android CI #204 / run `34685747984`: **GREEN**;
- portrait fit, system insets, initial scroll position, mode/pace visibility, reserved canvas and control clipping: **PASS**;
- initial isolation diagnostic: `teacher/guide in child history: 0 · PASS`.

## Required scenarios

| # | Scenario | Result | Observation / evidence |
|---|---|---|---|
| 1 | Launch Lesson Lab and load bundled Cute Cat | PASS | `Loaded cute-cat r1`; no crash; isolation `0 · PASS`. |
| 2 | Start Draw With Me | PASS | Teacher playback completed and reached `AwaitingChild` on `head`. |
| 3 | Start Watch Then Draw | PASS | Overview entered and transitioned correctly to child pass. |
| 4 | Start Trace & Learn | PASS | Teacher playback completed; `AwaitingChild` with authored trace guide. |
| 5 | EXTRA_SLOW 0.4× playback | PASS | Completed physically; also recovered successfully after injected failure/retry. |
| 6 | SLOW 0.7× playback | PASS | Fresh run completed normally. |
| 7 | NORMAL 1× playback | PASS | Teacher playback reported `COMPLETED`. |
| 8 | FAST 1.5× playback | PASS | Fresh run completed normally. |
| 9 | VERY_FAST 2× playback | PASS | Fresh run completed normally. |
| 10 | Draw With Me teacher demo → child stroke → next step | PASS | One child stroke committed; Done advanced to `ears`; artwork preserved; isolation `0 · PASS`. |
| 11 | Watch overview Pause → Resume | PASS | Pause held playback; Resume continued normally. |
| 12 | Watch overview Skip → child pass | PASS | Skip Overview cancelled overview playback and entered `AwaitingChild` without history corruption. |
| 13 | Trace guide visible during child turn | PASS | `guide TRACE_MODE` visible during child turn. |
| 14 | Trace/help guide does not increase child operation count | PASS | Visible guide with `operations 0 · child ink 0`; isolation `0 · PASS`. |
| 15 | Replay during valid child turn | PASS | Replay completed and restored Trace guide; history remained isolated. |
| 16 | Help escalation skips missing authored levels correctly | PASS | Help escalated to authored level 4; further Help+ rejected with `HELP_NOT_AVAILABLE`. |
| 17 | Reduce Help | PASS | Help− moved 4 → 1, correctly skipping unavailable authored levels. |
| 18 | Dismiss Help | PASS | Returned to `AwaitingChild · help 0` and restored base Trace guide. |
| 19 | Invalid Skip rejected on non-skippable step | PASS | `head` rejected Skip with `SKIP_NOT_ALLOWED`; state/history intact. |
| 20 | Valid Skip accepted on `face` step | PASS | Skip on authored skippable `face` advanced to `body_tail`; isolation remained `0 · PASS`. |
| 21 | Invalid Done/command path rejected without state corruption | PASS | Dismiss Help at help 0 produced expected `HELP_NOT_AVAILABLE`; lesson state stayed intact. |
| 22 | Background → foreground preserves artwork/session | PASS | Child stroke and active lesson state survived normal Home/Recents background cycle. |
| 23 | Process recreation/relaunch preserves child artwork/session progress | PASS | After Home → Force stop → launcher reopen, persisted lesson progress and child artwork were recovered. |
| 24 | Recreated teacher request is fresh; stale callback cannot advance | PASS | Before recreation teacher request was `...r1:g0:head:teacher:1`; after force-stop/relaunch recovery it became `...r1:g1:head:teacher:1` with `Recovered restart_current_teacher_demonstration`. Fresh-generation proof is physical; stale-request rejection remains additionally covered by the engine test suite. |
| 25 | Inject teacher playback failure → Retry → playback resumes | PASS | Injected active-playback failure entered recoverable state; Retry restarted playback and returned to healthy child turn. |
| 26 | Drawing complete reaches post-drawing choice | PASS | Completing final drawing step reached the post-drawing completion flow. |
| 27 | Coloring handoff unavailable → returns to retryable choice | PASS | `Color With Me` followed by simulated unavailable coloring returned to a retryable post-drawing choice rather than dead-ending. |
| 28 | Post-drawing state survives recreation | PASS | Post-drawing choice survived Home → Force stop → relaunch recovery. |
| 29 | Persisted child artwork contains zero TEACHER_GENERATED ink operations | PASS | After persisted/relaunch recovery, diagnostic remained `teacher/guide in child history: 0 · PASS`; teacher/guide overlays did not enter child operation history. |
| 30 | Art Lab launcher still opens and draws | PASS | Art Lab opened and accepted physical drawing strokes without crash. |
| 31 | Quality Lab launcher still opens | PASS | Quality Lab opened and was usable without crash. |
| 32 | No crash/deadlock during full matrix | PASS | Full physical matrix completed across modes, five paces, help, replay, invalid commands, lifecycle recovery, failure/retry, handoff, Art Lab and Quality Lab with no observed crash or deadlock. |

## Isolation audit

**PASS.** Physical evidence repeatedly showed `teacher/guide in child history: 0 · PASS` across launch, Draw With Me, Watch Then Draw, Trace & Learn, Replay, Help level 4, invalid/valid Skip, failure/retry, process recreation, post-drawing recovery and the final fresh-generation recovery. Persisted child history remained child-authored only.

## Lifecycle audit

All four required recovery positions passed physically:

1. **Teacher playback active:** force-stop/relaunch restored teacher work with a fresh runtime generation (`g0 → g1`) and restarted the current teacher demonstration.
2. **Child turn after committed stroke:** normal backgrounding and process recreation preserved the child artwork and lesson progress.
3. **Help active:** force-stop/relaunch restored `HelpActive` and rehydrated the guide without contaminating child history.
4. **Drawing complete / post-drawing choice:** force-stop/relaunch restored the retryable post-drawing state.

Lifecycle audit result: **PASS**.

## Release decision

Physical milestone gate: **PASS**.

The Samsung SM-A546E/API 36 physical matrix is complete with all 32 required rows passing and no observed crash/deadlock. The verified APK remains the responsive candidate built from `7a33edc6a5c37fe5bea93d3729f8a1eabf88e8df`, SHA-256 `656bee202bc71d5b487f287c6dc3a8b3f05f4cc50eea58c7477fd0a59534e109`.

P2.6 may proceed to final milestone documentation, CI confirmation, tag/release and Issue #34 closure.