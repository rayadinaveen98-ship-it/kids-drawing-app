# P6.2 Final QA — Parent Zone Foundation

Status: **QA1 AUTOMATED GREEN / PHYSICAL ACCEPTANCE PENDING**  
Issue: #94  
Parent epic: #91  
PR: #95  
Target milestone: `0.6.0-family-readiness`

## Immutable QA1 candidate

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

The profile APK above is the **only** P6.2 QA1 physical-acceptance binary unless a release-blocking executable defect requires a new versionCode. Later documentation-only commits do not replace it.

## Automated acceptance evidence

| ID | Gate | Status | Evidence / note |
|---|---|---|---|
| A01 | P6.1 contract dependency closed | PASS | #92 completed; PR #93 squash-merged; merged-main CI #591 GREEN |
| A02 | Parent access/session policy unit coverage | PASS | 2.5s hold, early release/cancel, accessible unlock, 5m expiry, >30s background invalidation, child return/external invalidation, remaining-time behavior |
| A03 | Profile editor transaction/validation unit coverage | PASS | normalization, validation, interests rule, supported defaults and abandoned-draft behavior |
| A04 | Protected Parent Zone product routing compiles/lints | PASS | exact QA1 CI #596 |
| A05 | Parent Zone six-section production Compose shell compiles/lints | PASS | exact QA1 CI #596 |
| A06 | Accessible Parent Gate Compose instrumentation coverage exists | COMPILES | Android test APK compiled in CI; no emulator execution is claimed |
| A07 | Phase-5 `ChildProfileStore` compatibility instrumentation coverage exists | COMPILES | Android test APK compiled in CI; runtime upgrade is covered physically by P02 |
| A08 | Phase-5 regression build matrix | PASS | unit tests + lint + debug APK + instrumentation APK + profile APK compile |
| A09 | Frozen curriculum quality | PASS | **24 lessons / 0 errors / exactly 6 reviewed warnings** |
| A10 | Android permission allowlist | PASS | no unexpected requested Android permission in debug/profile APKs |
| A11 | QA1 APK identity | PASS | package `com.navin.kidsdrawing`, versionCode 28, expected versionName |
| A12 | Immutable APK evidence | PASS | debug/profile artifacts, SHA256, sizes and source-head identity packaged by CI; hashes independently recomputed and matched |

## Focused physical acceptance matrix

Use the exact profile APK identified above. Do not substitute a later CI rebuild.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact QA1 profile APK and launch successfully | NOT RUN |
| P02 | When upgrading from accepted v27, existing child profile and existing artwork remain readable; no migration loss | NOT RUN |
| P03 | Child Home exposes a discoverable `Grown-ups` / Parent Zone entry without making it the primary child CTA | NOT RUN |
| P04 | Tap or release the primary adult-intent control before 2.5 seconds: Parent Zone does **not** unlock | NOT RUN |
| P05 | Hold the primary adult-intent control continuously for about 2.5 seconds: Parent Zone unlocks | NOT RUN |
| P06 | Gate Cancel / return path goes safely back to child mode and performs no profile mutation | NOT RUN |
| P07 | Accessible fallback is reachable; its first confirmation alone does not unlock Parent Zone | NOT RUN |
| P08 | Accessible second confirmation unlocks; cancelling the fallback leaves Parent Zone locked | NOT RUN |
| P09 | Parent Zone overview shows Family, Learning, Accessibility & Audio, Storage & Data, Safety & Privacy and About, with a child-mode return path | NOT RUN |
| P10 | Family shows the current nickname, age band and supported defaults from the existing local profile | NOT RUN |
| P11 | Valid nickname edit saves and normalized spacing is reflected after returning/reopening | NOT RUN |
| P12 | Blank/invalid/over-limit nickname cannot be saved | NOT RUN |
| P13 | Age-band edit saves and persists after returning/reopening | NOT RUN |
| P14 | Default teaching-mode and teaching-pace edits save and persist | NOT RUN |
| P15 | Interests edits persist and the profile cannot be saved with zero interests | NOT RUN |
| P16 | Handedness and narration-default edits save and persist | NOT RUN |
| P17 | Make unsaved profile changes then choose Cancel: previously accepted profile remains unchanged | NOT RUN |
| P18 | Learning section is descriptive/read-only for this slice and shows no grades, ranks, mastery %, XP/streak pressure or fake progress controls | NOT RUN |
| P19 | Accessibility & Audio truthfully shows current capability/deferral and does not expose fake unimplemented switches | NOT RUN |
| P20 | Storage & Data truthfully describes local storage/deferral and exposes no fake destructive control | NOT RUN |
| P21 | Safety & Privacy wording matches the product: offline core, no required account, no ads, no behavioral analytics upload, no new runtime permission | NOT RUN |
| P22 | About shows `0.6.0-family-readiness-p6.2-qa1` | NOT RUN |
| P23 | `Back to child mode` invalidates the adult session; reopening Parent Zone requires the gate again | NOT RUN |
| P24 | Brief background/foreground within 30 seconds keeps an otherwise-valid adult session; background longer than 30 seconds relocks | NOT RUN |
| P25 | Force-stop/process recreation/relaunch does not restore an unlocked Parent Zone | NOT RUN |
| P26 | Leave Parent Zone open until the 5-minute adult-session limit expires: it relocks | NOT RUN |
| P27 | Airplane Mode: gate, Parent Zone browsing and local profile edit/save continue to work | NOT RUN |
| P28 | Larger system text: Parent Zone/gate text reflows or scrolls without clipping critical actions | NOT RUN |
| P29 | Small-screen/reachability pass: critical actions remain reachable, usable and do not require precision/multi-finger/rapid gestures | NOT RUN |
| P30 | Regression smoke: child Home plus one lesson/drawing action and Gallery or Free Draw still work after Parent Zone/profile use | NOT RUN |

### Physical status

- PASS: **0 / 30**
- FAIL: **0 / 30**
- NOT RUN: **30 / 30**
- tester device/model/API: **not yet provided**
- release blockers from automated QA: **none**
- release decision: **PENDING PHYSICAL ACCEPTANCE**

## Closure rule

If all P01–P30 pass on the exact profile binary:
1. record actual device/API and results;
2. finalize the P6.2 release report and project status;
3. run documentation-only acceptance CI;
4. mark PR #95 ready and squash-merge;
5. verify merged-main Android CI GREEN;
6. close #94 completed;
7. begin P6.3 only from that verified merged-main baseline.

If a release-blocking executable defect is found, do **not** modify/reuse versionCode 28. Fix the defect, increment versionCode, cut a new QA candidate, and repeat exact-binary evidence + physical acceptance.
