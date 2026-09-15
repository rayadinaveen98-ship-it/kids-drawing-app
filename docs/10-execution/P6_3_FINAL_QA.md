# P6.3 Final QA — Parent Progress & Curriculum Visibility

Status: **PHYSICAL ACCEPTED — 24/24 PASS / CLOSURE ACTIVE**  
Issue: #96  
Parent epic: #91  
PR: #97  
Target milestone: `0.6.0-family-readiness`

## Immutable accepted QA1 candidate

- versionName: `0.6.0-family-readiness-p6.3-qa1`
- versionCode: **29**
- executable source head: `f155abc894d21b8cc09112a018fdf53ae25e4447`
- Android CI: #610 / run `34948654974` — **GREEN**
- PR workflow SHA packaged by CI: `5fa02aa6b0655ec75624c6c1d47ebca7dbe27b0a`
- profile artifact ID: **10387978868**
- profile artifact archive digest: `sha256:c45a5a3e032a1748dd8b764c98696d3edf58fb1c6edccbe14652bf4fb79aa5d1`
- profile APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.3_QA1-profile.apk`
- profile APK size: **16,393,832 bytes**
- profile APK SHA256: `93ccc4cbbf3be6b1d89e9d0094810dcf02a0654a410650752a3b8e47bf7d5b54`
- debug artifact ID: **10388636830**
- debug artifact archive digest: `sha256:a4de13445e372753121c3dfa735229b3f9e6b47c386138e28a2a788e2058a9a4`
- debug APK size: **20,674,972 bytes**
- debug APK SHA256: `a5fc5ce589b101b47e3e5885e7dd35be777595282a1cbb6d03def64e9d098c2e`
- content-quality artifact ID: **10388931000**
- content-quality artifact archive digest: `sha256:5dd7118a8eee6bf84acbfc5df8138d758467874ee54d7bd99e7c9f1d152ee556`

The profile APK above is the **physically accepted P6.3 QA1 binary**. Later documentation or merge commits do not replace it. The profile/debug artifact ZIP digests and both APK SHA256/size values were independently recomputed and matched CI-packaged evidence.

## Automated acceptance evidence

| ID | Gate | Status | Evidence / note |
|---|---|---|---|
| A01 | P6.3 truth-source/contract audit | PASS | 64/64; contract head `cc2d9fde...`; CI #606 GREEN |
| A02 | Typed read-only adaptive-state outcome | PASS | additive parent read path; recommendation `loadForPolicy()` behavior preserved |
| A03 | Missing/corrupt/incompatible adaptive-state semantics | PASS | focused JVM tests; unavailable history is surfaced, not rewritten or fabricated |
| A04 | Pure deterministic Parent Progress projection | PASS | focused JVM coverage for counts/order/categories/skills/journeys/artwork/in-progress/partial data |
| A05 | No new analytics/history persistence | PASS | P6.3 projects accepted existing local truth only |
| A06 | Parent Progress Android repository adapter | PASS | catalog + adaptive + Gallery + resume sources compile/lint on exact candidate |
| A07 | P6.2 Learning placeholder replaced inside protected Parent Zone only | PASS | wired implementation CI #608 GREEN |
| A08 | Parent Learning Compose route coverage exists | COMPILES | instrumentation APK compiles in CI #609/#610; runtime verified physically below |
| A09 | Full regression build matrix | PASS | unit tests + lint + debug APK + instrumentation APK + profile APK compile |
| A10 | Frozen curriculum quality | PASS | **24 lessons / 0 errors / exactly 6 reviewed warnings** |
| A11 | Android permission allowlist | PASS | no unexpected requested Android permission in debug/profile APKs |
| A12 | QA1 APK identity | PASS | package `com.navin.kidsdrawing`, versionCode 29, expected versionName |
| A13 | Immutable artifact evidence | PASS | source-head identity, debug/profile sizes + SHA256 packaged by CI and independently verified |
| A14 | Safety/language boundary | PASS | no grades/scores/ranks/mastery %, XP/streak pressure, ability labels, comparison or parent-facing Help counts |

## Focused physical acceptance matrix

Tested on the exact profile APK identified above. User confirmed **all rows passed** on 2026-09-15 with no release-blocking defect reported.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact v29 profile APK and launch successfully | PASS |
| P02 | Upgrade from accepted v28 preserves the existing local child profile, Gallery artwork and readable learning state | PASS |
| P03 | Parent Zone still requires the accepted adult-intent gate; P6.3 does not create a child-facing Learning bypass | PASS |
| P04 | Parent Zone overview → `Learning` opens the real progress screen rather than the old placeholder | PASS |
| P05 | Learning shows `Local and descriptive` explanation and makes clear that it does not grade/rank/compare/predict ability or upload behavioral analytics | PASS |
| P06 | Summary cards show `Lessons completed` and `Artworks saved` with values that match local activity rather than invented progress | PASS |
| P07 | Curriculum card reports the locally available curriculum and explicitly says completion is not mastery | PASS |
| P08 | If completed lesson history exists, `Recently completed` shows genuine stored recent ordering | PASS |
| P09 | Recently completed lessons do **not** display fabricated wall-clock completion dates; the explanatory no-date note is visible | PASS |
| P10 | `Curriculum areas explored` appears from completed authored lesson categories when supported | PASS |
| P11 | `Authored skills practiced` appears from authored skill exposure when supported and contains no quality/ability judgment | PASS |
| P12 | Journey cards show completed-of-total lesson counts using descriptive wording only | PASS |
| P13 | Journey card shows a sensible `Next available` lesson when prerequisites make one eligible, or omits it when none is available | PASS |
| P14 | Start/save an unfinished guided lesson; `In progress` shows it separately as drawing work and completed lesson count does not increase | PASS |
| P15 | Where a resumable coloring session exists, `In progress` labels it as coloring rather than historical completion | PASS |
| P16 | `Recent saved artwork` displays a genuine local date/time sourced from saved Gallery activity | PASS |
| P17 | Saved lesson work is labelled `Lesson artwork`; Free Draw saves are labelled `Free Draw artwork` | PASS |
| P18 | Completing/saving a new guided lesson and reopening Learning updates completion/artwork information without manual online sync | PASS |
| P19 | Learning contains no grades, scores, ranks, mastery percentages, XP, punitive streaks, permanent ability labels, sibling/peer comparisons or raw Help-request counts | PASS |
| P20 | Back from Learning returns to Parent Zone safely; other Parent Zone sections and profile editing remain usable | PASS |
| P21 | Airplane Mode: Parent Zone → Learning loads available local progress/artwork/curriculum information without network dependency | PASS |
| P22 | Larger system text reflows/scrolls without clipping the heading, Back action, summary cards or later sections | PASS |
| P23 | Small-screen/reachability: all Learning sections remain scrollable/readable and critical controls remain usable | PASS |
| P24 | Child Home + guided lesson/drawing + Gallery/Free Draw regression smoke remains functional after using Parent Learning | PASS |

### Physical status

- PASS: **24 / 24**
- FAIL: **0 / 24**
- NOT RUN: **0 / 24**
- acceptance date: **2026-09-15**
- tester device/model/API: **not provided and not inferred**
- reported release blockers: **none**
- release decision: **ACCEPTED FOR P6.3 REPOSITORY CLOSURE**

## Closure rule

With P01–P24 physically accepted:
1. finalize release report/project status;
2. require acceptance-documentation CI GREEN;
3. mark PR #97 ready and squash-merge;
4. require merged-main Android CI GREEN;
5. close #96 completed;
6. begin P6.4 only from that verified merged-main baseline.

Any executable change after this candidate requires a new monotonic versionCode and fresh exact-binary QA. Documentation/merge commits do not replace the physically accepted executable above.