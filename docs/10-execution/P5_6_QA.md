# P5.6 QA — Curriculum Expansion Set E

**Slice:** P5.6 / issue #84 / draft PR #85  
**Candidate:** `0.5.0-curriculum-expansion-p5.6-qa1`  
**versionCode:** **25**  
**Exact QA1 app/content/workflow head:** `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`  
**Android CI:** #538 / run `34846235868` — **GREEN**  
**Date:** 2026-09-14

## Pre-freeze human gate

The required P5.6 Content Lab inspection passed **16/16** before versionCode 25 was cut. The user reported all checks good and no content-changing defect. Evidence is recorded in `P5_6_PRE_FREEZE_CONTENT_LAB_CHECKLIST.md`.

## Automated QA1 result — PASS

Exact-head CI #538 passed:
- committed JSON parsing;
- AndroidX Ink boundary verification;
- unit tests;
- lint;
- debug APK build;
- instrumentation APK build;
- release-like profile APK build;
- production content-quality gate;
- milestone APK permission allowlist;
- P5.6 QA1 evidence packaging/upload.

Verified curriculum state remains:
- **24 production lessons**;
- **0 quality errors**;
- exactly **6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings** only for Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene and One-Point Room;
- age coverage Little 8 / Creative 18 / Growing 17 / Young 10;
- difficulty D1 5 / D2 9 / D3 6 / D4 3 / D5 1;
- Draw With Me 23 / Watch Then Draw 17 / Trace & Learn 4;
- all Phase-5 curriculum coverage targets met.

Permission allowlist passed for debug and profile APKs. No new sensitive permission is accepted by this milestone.

## Immutable QA1 artifacts

### Release-like profile — required physical acceptance binary

- artifact ID: `10348300909`
- artifact name: `kids-drawing-0.5.0-curriculum-expansion-p5.6-qa1-profile`
- APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`
- APK size: **16,311,900 bytes**
- APK SHA-256: `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`
- workflow artifact archive digest: `sha256:5163926410d54a176aade5410a8b8c001c2d6e15325bf86287858a4a4d37f352`

### Debug

- artifact ID: `10347578783`
- artifact name: `kids-drawing-0.5.0-curriculum-expansion-p5.6-qa1-debug`
- APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-debug.apk`
- APK size: **20,527,518 bytes**
- APK SHA-256: `5e8eb2b9ff209798bdbb7d568326d4d4df3aff973821de2e14a333f2e12091e8`
- workflow artifact archive digest: `sha256:077b677175c71e804ccfc2fd5d9e8fe020be7ab7d7444671283479e256ba30d6`

### Content quality

- artifact ID: `10347409267`
- artifact name: `kids-drawing-p5.2-content-quality-report`
- workflow artifact archive digest: `sha256:6fde516b3d716cb36c6b2cd00764f54124f385e30c35548d0972eadd788bd5b8`

## Human physical acceptance

**Status: PENDING.**

Use only the exact profile APK above and `P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md`.

Required result: **36/36 PASS** with no binary/content-changing defect.

Do not infer physical acceptance from CI, Content Lab, emulator behavior or prior milestone QA. Device model and Android/API must not be invented; record them only if actually supplied by the tester.

Any binary/content-changing defect invalidates versionCode 25 and requires a new candidate/versionCode/evidence set before merge.

## Merge gate

P5.6 is not complete until:
1. exact-profile physical acceptance = 36/36 PASS;
2. genuine acceptance evidence is committed;
3. acceptance-doc exact-head CI is GREEN;
4. PR #85 is marked ready and squash-merged;
5. merged-main CI is GREEN;
6. issue #84 is closed completed.
