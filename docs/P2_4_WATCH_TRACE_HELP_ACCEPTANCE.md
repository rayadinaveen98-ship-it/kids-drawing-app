# P2.4 — Watch Then Draw, Trace & Learn, and Help Ladder Acceptance

Issue: #32  
PR: #38  
Exact hardened branch head: `d36d773b2e7a75735723fa6bd0dec1979d686e05`  
Android CI: run #146 / `34681054424` — GREEN  
Merged `main` commit: `16828eee0988b8143559857a892c7d927dfc1c0d`

## Accepted behavior

- Watch Then Draw performs one deterministic, non-destructive full-lesson overview before the child drawing pass.
- Overview pause/resume/pace/skip controls stay attached to one typed playback request identity.
- Watch Then Draw does not automatically replay the teacher between child steps; replay is explicit and step-scoped.
- Trace & Learn executes the authored teacher step and then displays the authored trace guide.
- Trace/help guides are product-owned overlay requests made only from `TEACHER_GENERATED` strokes.
- Replay clears the current guide and restores the correct trace/help guide after playback.
- Help Ladder advances only through authored levels, skips missing levels, supports Reduce Help and Dismiss Help, and restores the base trace guide in Trace mode.
- Step completion clears active guides before progression.
- The bundled Cute Cat lesson completes through all three teaching modes from one lesson source.
- A real `DrawingDocumentEngine` rejects guide strokes from child history, preserving child-artwork ownership.

## Verification

Exact-head Android CI #146 passed:

- JVM/unit tests;
- Android lint;
- debug APK compile;
- instrumentation APK compile;
- profile APK compile;
- Ink boundary verification;
- APK permission allowlist;
- debug and profile artifact uploads.

P2.4 is complete. P2.5 is the next Phase 2 slice.
