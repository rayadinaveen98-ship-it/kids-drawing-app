# P5.6 QA — Curriculum Expansion Set E

**Slice:** P5.6 / issue #84 / PR #85  
**Candidate:** `0.5.0-curriculum-expansion-p5.6-qa1`  
**versionCode:** **25**  
**Exact QA1 app/content/workflow head:** `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`  
**Android CI:** #538 / run `34846235868` — **GREEN**  
**Date:** 2026-09-14

## Pre-freeze human gate

Content Lab passed **16/16** before versionCode 25 was cut. No content-changing defect was reported.

## Automated QA1 result — PASS

CI #538 passed committed JSON parsing, Ink-boundary verification, unit tests, lint, debug/profile/instrumentation APK builds, production content-quality gate, permission allowlist, and P5.6 QA1 evidence packaging.

Verified curriculum state:
- **24 production lessons**;
- **0 quality errors**;
- exactly **6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings** only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room;
- age coverage Little 8 / Creative 18 / Growing 17 / Young 10;
- difficulty D1 5 / D2 9 / D3 6 / D4 3 / D5 1;
- Draw With Me 23 / Watch Then Draw 17 / Trace & Learn 4;
- all Phase-5 curriculum coverage targets met.

## Immutable QA1 artifacts

### Release-like profile — accepted physical binary
- artifact ID: `10348300909`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`
- size: **16,311,900 bytes**
- SHA-256: `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

### Debug
- artifact ID: `10347578783`
- size: **20,527,518 bytes**
- SHA-256: `5e8eb2b9ff209798bdbb7d568326d4d4df3aff973821de2e14a333f2e12091e8`

### Content quality
- artifact ID: `10347409267`

## Human physical acceptance — PASS

The user reported **36/36 PASS** on the exact profile APK above on 2026-09-14. No binary/content-changing defect was reported.

- Device model: **Not provided by tester**.
- Android/API: **Not provided by tester**.
- No device metadata is inferred.

Authoritative checklist: `P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md`.

## Merge gate

P5.6 may proceed to merge only after this acceptance evidence commit itself passes exact-head Android CI. Then PR #85 may be marked ready, squash-merged, merged-main CI verified green, and issue #84 closed completed.
