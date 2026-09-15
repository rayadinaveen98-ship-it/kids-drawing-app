# P6.2 Final QA — Parent Zone Foundation

Status: **PHYSICAL ACCEPTED — 30/30 PASS / CLOSURE ACTIVE**  
Issue: #94  
Parent epic: #91  
PR: #95  
Target milestone: `0.6.0-family-readiness`

## Immutable accepted QA1 candidate

- versionName: `0.6.0-family-readiness-p6.2-qa1`
- versionCode: **28**
- executable source head: `528467acdcfde9c4d6ea01959d57157376cb081d`
- Android CI: #596 / run `34940587740` — **GREEN**
- PR workflow SHA: `e523999099fd1ab190a65921d122f26eb5c81672`
- profile artifact ID: **10385266255**
- profile artifact archive digest: `sha256:11f77fb9f8225f0c01671abd2b112cf819d6aee49c2c3b89f5c2f7c5f82ee3fa`
- profile APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.2_QA1-profile.apk`
- profile APK size: **16,377,441 bytes**
- profile APK SHA256: `b438d69ef7cabd7e023c963602e08c887b73074e219df2d03355a308d4849402`
- debug artifact ID: **10384299888**
- debug artifact archive digest: `sha256:e73ccac77afc36387f5db4447ad4f63ef560de1492e9fa014ecce712263cabe0`
- debug APK size: **20,642,199 bytes**
- debug APK SHA256: `7c9a53ef5b3510a81965d3ec0443600e10c1f83ee5c41316797f3fcb9ac26e2d`
- content-quality artifact ID: **10384499464**
- content-quality artifact archive digest: `sha256:05e88209faf89861c5df40f594ae19b1086acd7d24956e6a817875ab7df4416a`

The profile APK above is the **physically accepted P6.2 QA1 binary**. Later documentation/merge commits do not replace it.

## Automated acceptance evidence

| ID | Gate | Status | Evidence / note |
|---|---|---|---|
| A01 | P6.1 contract dependency closed | PASS | #92 completed; PR #93 squash-merged; merged-main CI #591 GREEN |
| A02 | Parent access/session policy unit coverage | PASS | 2.5s hold, early release/cancel, accessible unlock, 5m expiry, >30s background invalidation, child return/external invalidation, remaining-time behavior |
| A03 | Profile editor transaction/validation unit coverage | PASS | normalization, validation, interests rule, supported defaults and abandoned-draft behavior |
| A04 | Protected Parent Zone product routing compiles/lints | PASS | exact QA1 CI #596 |
| A05 | Parent Zone six-section production Compose shell compiles/lints | PASS | exact QA1 CI #596 |
| A06 | Accessible Parent Gate Compose instrumentation coverage exists | COMPILES | Android test APK compiled in CI; runtime verified physically below |
| A07 | Phase-5 `ChildProfileStore` compatibility instrumentation coverage exists | COMPILES | Android test APK compiled in CI; upgrade/runtime verified physically below |
| A08 | Phase-5 regression build matrix | PASS | unit tests + lint + debug APK + instrumentation APK + profile APK compile |
| A09 | Frozen curriculum quality | PASS | **24 lessons / 0 errors / exactly 6 reviewed warnings** |
| A10 | Android permission allowlist | PASS | no unexpected requested Android permission in debug/profile APKs |
| A11 | QA1 APK identity | PASS | package `com.navin.kidsdrawing`, versionCode 28, expected versionName |
| A12 | Immutable APK evidence | PASS | debug/profile artifacts, SHA256, sizes and source-head identity packaged by CI; hashes independently recomputed and matched |

## Focused physical acceptance matrix

Tested on the exact profile APK identified above. User confirmed **all rows passed** on 2026-09-15 with no release-blocking defect reported.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact QA1 profile APK and launch successfully | PASS |
| P02 | Upgrade from accepted v27 preserves existing child profile/artwork readability with no migration loss | PASS |
| P03 | Child Home exposes a discoverable `Grown-ups` / Parent Zone entry without making it the primary child CTA | PASS |
| P04 | Release primary adult-intent control before 2.5 seconds does not unlock | PASS |
| P05 | Continuous ~2.5-second hold unlocks Parent Zone | PASS |
| P06 | Gate Cancel / return path safely returns to child mode without profile mutation | PASS |
| P07 | Accessible fallback is reachable; first confirmation alone does not unlock | PASS |
| P08 | Accessible second confirmation unlocks; cancelling leaves Parent Zone locked | PASS |
| P09 | Parent Zone overview shows all six contracted sections plus child-mode return | PASS |
| P10 | Family shows current nickname, age band and supported defaults from existing local profile | PASS |
| P11 | Valid nickname edit saves and normalized spacing persists after reopen | PASS |
| P12 | Blank/invalid/over-limit nickname cannot be saved | PASS |
| P13 | Age-band edit saves and persists | PASS |
| P14 | Default teaching-mode and teaching-pace edits save and persist | PASS |
| P15 | Interests edits persist and zero-interest profile cannot be saved | PASS |
| P16 | Handedness and narration-default edits save and persist | PASS |
| P17 | Cancel unsaved changes preserves previously accepted profile | PASS |
| P18 | Learning section is descriptive/read-only and contains no grades/ranks/mastery %/XP/streak pressure/fake progress controls | PASS |
| P19 | Accessibility & Audio wording is truthful and exposes no fake unimplemented switches | PASS |
| P20 | Storage & Data truthfully describes local storage/deferral and exposes no fake destructive control | PASS |
| P21 | Safety & Privacy matches implementation: offline core, no required account, no ads, no behavioral analytics upload, no new runtime permission | PASS |
| P22 | About shows `0.6.0-family-readiness-p6.2-qa1` | PASS |
| P23 | `Back to child mode` invalidates adult session; reopening requires gate again | PASS |
| P24 | <=30s background preserves valid session; >30s background relocks | PASS |
| P25 | Force-stop/process recreation/relaunch does not restore unlocked Parent Zone | PASS |
| P26 | 5-minute adult-session limit relocks Parent Zone | PASS |
| P27 | Airplane Mode: gate, Parent Zone browsing and local profile edit/save work | PASS |
| P28 | Larger system text reflows/scrolls without clipping critical actions | PASS |
| P29 | Small-screen/reachability: critical actions remain reachable and usable without precision/multi-finger/rapid gestures | PASS |
| P30 | Child Home + lesson/drawing + Gallery or Free Draw regression smoke remains functional after Parent Zone/profile use | PASS |

### Physical status

- PASS: **30 / 30**
- FAIL: **0 / 30**
- NOT RUN: **0 / 30**
- acceptance date: **2026-09-15**
- tester device/model/API: **not provided and not inferred**
- reported release blockers: **none**
- release decision: **ACCEPTED FOR P6.2 REPOSITORY CLOSURE**

## Closure rule

With P01–P30 physically accepted:
1. finalize release report/project status;
2. require acceptance-documentation CI GREEN;
3. mark PR #95 ready and squash-merge;
4. require merged-main Android CI GREEN;
5. close #94 completed;
6. begin P6.3 only from that verified merged-main baseline.

Any later executable change would require a new monotonic versionCode and a fresh exact-binary QA cycle. Documentation/merge commits do not replace the physically accepted executable above.
