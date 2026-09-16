# P6.4 Final QA — Accessibility System V2

Status: **QA1 AUTOMATED GREEN / PHYSICAL ACCEPTANCE PENDING**  
Issue: #98  
Parent epic: #91  
PR: #99  
Target milestone: `0.6.0-family-readiness`

## Immutable P6.4 QA1 candidate

- versionName: `0.6.0-family-readiness-p6.4-qa1`
- versionCode: **30**
- executable source head: `fec854c3321d1966dc05437c2c4a3651c2323ae1`
- implementation stabilization head: `701d29e465a12f9e6d6f2a3331e82a8725fbd11a`
- stabilization Android CI: #647 / run `35067693656` — **GREEN**
- candidate Android CI: #648 / run `35068180162` — **GREEN**
- PR workflow SHA packaged by CI: `37cc44d635e2f39db7a070b2a302d71996edbc2c`
- profile artifact ID: **10434518471**
- profile artifact archive digest: `sha256:28978533088b03435343844018d99ec9bd0db0ef6ebf1929db4ef82a77731125`
- profile APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.4_QA1-profile.apk`
- profile APK size: **16,426,590 bytes**
- profile APK SHA256: `2727d18d8f21c0e60612f8134aeb06b502941e816ea5390c757007f5dc31acce`
- debug artifact ID: **10435236575**
- debug artifact archive digest: `sha256:f0d4c65235c903ba4ea7e8ac8237451fce0bce367904f28a1c74c330f73c7a66`
- debug APK size: **20,724,127 bytes**
- debug APK SHA256: `e7d1bb3b0dc1f2dc70e39ead44f9bfae0ad834db4fe511d2a2a44f8e8aa5965f`
- content-quality artifact ID: **10434204389**
- content-quality artifact archive digest: `sha256:fa361b94242eaeb1dfd7bcad6b04951abee2c7d2e2afffc3b2d3daaaf0185236`

The profile APK above is the **only physical P6.4 QA1 candidate**. Later documentation commits or CI rebuilds do not replace it. Both profile/debug APK hashes and byte sizes were independently recomputed from downloaded CI artifacts and matched `SHA256SUMS.txt` / `APK_SIZES.txt` exactly.

## Automated acceptance evidence

| ID | Gate | Status | Evidence / note |
|---|---|---|---|
| A01 | P6.4 source audit + frozen accessibility contract | PASS | contract head `7935348bfe3378b363cd7d5753c8f2e6b98d006c`; **74/74** contract checks |
| A02 | Contract CI | PASS | Android CI #623 GREEN before production implementation |
| A03 | Device-local accessibility preference model | PASS | `AccessibilityPreferencesStore`; `reduceMotion` outside ChildProfile; offline/local-only |
| A04 | Font-scale policy | PASS | deterministic standard `<1.30`, large `>=1.30`, extra-large `>=1.60` unit coverage |
| A05 | Parent Accessibility & Audio production surface | PASS | real Reduce motion control, Android text-size guidance, existing narration ownership retained |
| A06 | Parent Gate timing safety | PASS | same `ParentAccessSession.HOLD_DURATION_MILLIS`; reduced motion changes visual feedback only |
| A07 | Selection semantics architecture | PASS | single-choice radio / multi-select checkbox / navigation semantics separated; visible non-color cue |
| A08 | Palette semantics | PASS | deterministic human-readable color names, selected semantics, visible selected cue; unknown fallback `Custom color` |
| A09 | Targeted contrast hardening | PASS | active `Ink500` / `Studio500` tokens corrected; art palette itself not distorted for text contrast |
| A10 | Major-flow font-scale reflow | PASS | Home, onboarding, lesson, Coloring, Free Draw, Gallery changes compile/lint on exact candidate |
| A11 | Honest canvas semantics | PASS | lesson/free-draw/coloring direct-touch descriptions; Gallery explicitly read-only; no false non-visual-equivalence claim |
| A12 | Focused JVM tests | PASS | accessibility policy/color naming and existing regression unit suite pass |
| A13 | Focused Compose/instrumentation coverage | COMPILES | Parent Accessibility, Reduce-motion gate, choice semantics and local preference round-trip tests compile in CI; CI does not run an emulator |
| A14 | Complete implementation stabilization gate | PASS | exact versionCode-29 head `701d29e...`, Android CI #647 GREEN |
| A15 | Full QA1 regression build matrix | PASS | unit tests + lint + debug APK + instrumentation APK + profile APK compile in CI #648 |
| A16 | Frozen curriculum quality | PASS | **24 lessons / 0 errors / exactly 6 reviewed warnings** |
| A17 | Android permission allowlist | PASS | no unexpected requested Android permission in debug/profile APKs |
| A18 | QA1 APK identity | PASS | package `com.navin.kidsdrawing`, versionCode 30, expected P6.4 QA1 versionName |
| A19 | Immutable artifact evidence | PASS | exact source-head identity, profile/debug sizes + SHA256 packaged by CI and independently verified |
| A20 | Scope/safety boundary | PASS | no account/cloud/network dependency, new permission, multi-profile migration, grading/ranking or drawing/session ownership rewrite |

## Focused physical acceptance matrix

Run every row on the **exact profile APK / artifact 10434518471** identified above. Do not substitute a later docs-head rebuild.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact v30 profile APK and launch successfully | NOT RUN |
| P02 | Upgrade from accepted v29 preserves existing child profile, Gallery artwork, progress/resume state and accepted local data | NOT RUN |
| P03 | Normal motion: release Parent Gate hold before 2.5s — must not unlock | NOT RUN |
| P04 | Normal motion: continuous Parent Gate hold for about 2.5s unlocks; accepted accessible two-confirmation fallback still works | NOT RUN |
| P05 | Parent Zone → Accessibility & Audio opens a real production surface with Reduce motion, Android text-size guidance and narration information | NOT RUN |
| P06 | Turn Reduce motion ON; leave/reopen Parent Accessibility and relaunch app — setting remains ON locally | NOT RUN |
| P07 | Turn Reduce motion OFF; leave/reopen/relaunch — setting remains OFF locally | NOT RUN |
| P08 | Reduce motion ON: Parent Gate uses static/textual hold feedback instead of animated sweep | NOT RUN |
| P09 | Reduce motion ON: release hold before 2.5s still does not unlock; continuous 2.5s hold still unlocks | NOT RUN |
| P10 | Reduce motion ON: accessible two-confirmation Parent Gate fallback remains available and unchanged | NOT RUN |
| P11 | Reduce motion ON during a guided lesson: authored teacher demonstration is not silently skipped or accelerated | NOT RUN |
| P12 | System font scale around 1.30×: Home remains readable; Free Draw/Gallery secondary actions stack/reflow and Grown-ups remains reachable | NOT RUN |
| P13 | System font scale around 1.60×/largest practical size: Home required recommendation/supporting text wraps instead of losing critical meaning | NOT RUN |
| P14 | Large text onboarding: choice cards remain reachable; handedness choices stack/reflow without clipping and copy does not promise fake toolbar mirroring | NOT RUN |
| P15 | With TalkBack/screen reader, single-choice cards expose selected/not-selected state and selected choices have a visible non-color `✓ Selected` cue | NOT RUN |
| P16 | Interests behave as multi-select/checkbox-style choices rather than single-choice radio semantics; multiple interests can remain selected | NOT RUN |
| P17 | Parent Zone section cards behave as navigation and do not announce meaningless `Not selected` state | NOT RUN |
| P18 | Parent Accessibility Reduce motion row is encountered as one understandable switch control, not duplicate nested switch nodes | NOT RUN |
| P19 | Guided Lesson at large/extra-large text: essential action grid/tools/post-drawing choices reduce columns or stack; critical actions remain reachable | NOT RUN |
| P20 | Guided Lesson exposes meaningful step/progress information beyond progress-bar color alone | NOT RUN |
| P21 | Guided Lesson drawing canvas is described honestly as touch/stylus freehand; teacher-demonstration pause state remains understandable | NOT RUN |
| P22 | Coloring at large text: top bar/tools/actions remain usable; palette announces human-readable color names and selected color has semantic + visible non-color state | NOT RUN |
| P23 | Coloring Fill wording makes clear that targeting a prepared region is spatial/direct-touch; app does not imply equivalent non-visual region targeting | NOT RUN |
| P24 | Free Draw at large text: top bar/tools/size/undo-redo-clear controls remain reachable; colors have names and selected state/cue | NOT RUN |
| P25 | Gallery at large text remains readable; saved artwork preview/detail is announced as read-only and deletion semantics remain unchanged | NOT RUN |
| P26 | Inspect key active supporting text on Home/onboarding/lesson/Parent/Coloring surfaces: readable contrast with no state conveyed by color alone | NOT RUN |
| P27 | Small-screen/reachability smoke: critical controls remain reachable with effective targets >=48dp and no mandatory precision, multi-finger, shake or rapid-tap gesture | NOT RUN |
| P28 | Airplane Mode: Parent accessibility setting, Parent Gate/Zone, Home, guided lesson, Coloring, Free Draw and Gallery core flows remain functional | NOT RUN |
| P29 | TalkBack traversal across Parent Zone/Home/onboarding/lesson tools/Coloring/Free Draw/Gallery is understandable and does not noisily announce every teacher stroke/animation frame | NOT RUN |
| P30 | Regression smoke after accessibility use: drawing/erase/undo/save, lesson Help behavior, Trace truth, coloring, Gallery ownership and child/adult session boundaries remain unchanged | NOT RUN |

### Physical status

- PASS: **0 / 30**
- FAIL: **0 / 30**
- NOT RUN: **30 / 30**
- physical test date: **pending**
- tester device/model/API: **not provided and not inferred**
- reported release blockers: **none from automated QA; physical acceptance pending**
- release decision: **PENDING PHYSICAL ACCEPTANCE**

## Closure rule

P6.4 can close only after P01–P30 are physically accepted on the exact profile APK above. Then:
1. record physical results and final release decision;
2. finalize release report/project status;
3. require acceptance-documentation CI GREEN;
4. mark PR #99 ready and squash-merge;
5. require merged-main Android CI GREEN on the exact squash merge;
6. close #98 completed;
7. begin P6.5 only from that verified merged-main baseline.

Any executable change after this candidate requires a new monotonic versionCode (>30), a new exact artifact/hash record and a fresh physical QA cycle. Documentation-only commits do not replace the executable candidate.
