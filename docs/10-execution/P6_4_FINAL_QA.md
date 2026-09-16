# P6.4 Final QA — Accessibility System V2

Status: **PHYSICAL ACCEPTED — 30/30 PASS / CLOSURE ACTIVE**  
Issue: #98  
Parent epic: #91  
PR: #99  
Target milestone: `0.6.0-family-readiness`

## Immutable physically accepted QA1 candidate

- versionName: `0.6.0-family-readiness-p6.4-qa1`
- versionCode: **30**
- executable source head: `fec854c3321d1966dc05437c2c4a3651c2323ae1`
- implementation stabilization head: `701d29e465a12f9e6d6f2a3331e82a8725fbd11a`
- stabilization Android CI: #647 / run `35067693656` — **GREEN**
- candidate Android CI: #648 / run `35068180162` — **GREEN**
- profile artifact ID: **10434518471**
- profile artifact archive digest: `sha256:28978533088b03435343844018d99ec9bd0db0ef6ebf1929db4ef82a77731125`
- profile APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.4_QA1-profile.apk`
- profile APK size: **16,426,590 bytes**
- profile APK SHA256: `2727d18d8f21c0e60612f8134aeb06b502941e816ea5390c757007f5dc31acce`
- debug artifact ID: **10435236575**
- debug APK size: **20,724,127 bytes**
- debug APK SHA256: `e7d1bb3b0dc1f2dc70e39ead44f9bfae0ad834db4fe511d2a2a44f8e8aa5965f`
- content-quality artifact ID: **10434204389**

The profile APK above is the **physically accepted P6.4 QA1 binary**. Later documentation or merge commits do not replace it. Profile/debug hashes and sizes were independently recomputed and matched CI evidence.

## Automated acceptance evidence

- P6.4 source audit + frozen contract: **74/74 PASS**; contract head `7935348b...`; CI #623 GREEN.
- Device-local Reduce motion preference, font-scale bands, Parent Accessibility surface and Parent Gate timing policy: PASS.
- Radio/checkbox/navigation semantics, named palette semantics, non-color selected cues and targeted contrast hardening: PASS.
- Home/onboarding/lesson/Coloring/Free Draw/Gallery large-text and reachability implementation: PASS.
- Honest direct-touch canvas/read-only Gallery semantics and truthful handedness behavior: PASS.
- Focused JVM coverage: PASS.
- Compose/instrumentation accessibility source: **COMPILES** in CI; CI does not run emulator instrumentation.
- Complete implementation stabilization: `701d29e...`, CI #647 GREEN.
- QA1 full regression build matrix: CI #648 GREEN.
- Frozen curriculum quality: **24 lessons / 0 errors / exactly 6 reviewed warnings**.
- Android permission allowlist: PASS.
- Exact APK identity/evidence packaging: PASS.
- No account/cloud/network dependency, new permission, multi-profile migration, grading/ranking or ownership rewrite.

## Focused physical acceptance matrix

All rows below were reported PASS by the user on the exact profile artifact **10434518471** on **2026-09-16**.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact v30 profile APK and launch | PASS |
| P02 | Upgrade preserves profile, Gallery, progress/resume and accepted local data | PASS |
| P03 | Normal motion: release Parent Gate before 2.5s does not unlock | PASS |
| P04 | Normal motion: continuous ~2.5s hold unlocks; accessible fallback works | PASS |
| P05 | Parent Zone → Accessibility & Audio production surface | PASS |
| P06 | Reduce motion ON persists across reopen/relaunch | PASS |
| P07 | Reduce motion OFF persists across reopen/relaunch | PASS |
| P08 | Reduce motion uses static/textual Parent Gate feedback | PASS |
| P09 | Reduced-motion gate still enforces 2.5s timing | PASS |
| P10 | Reduced-motion accessible two-confirmation fallback unchanged | PASS |
| P11 | Guided teacher demonstration is not silently skipped/accelerated | PASS |
| P12 | ~1.30× font: Home reflows and critical actions remain reachable | PASS |
| P13 | ~1.60×/large font: critical Home meaning remains visible | PASS |
| P14 | Large-text onboarding reflows; handedness copy remains truthful | PASS |
| P15 | Single-choice screen-reader state + visible non-color selected cue | PASS |
| P16 | Interests behave as multi-select/checkbox choices | PASS |
| P17 | Parent Zone section cards behave as navigation | PASS |
| P18 | Reduce motion is encountered as one understandable switch control | PASS |
| P19 | Guided Lesson large-text controls/actions remain reachable | PASS |
| P20 | Guided Lesson exposes meaningful progress beyond color alone | PASS |
| P21 | Guided drawing canvas/teacher pause semantics are understandable | PASS |
| P22 | Coloring large-text controls and named/selected palette semantics | PASS |
| P23 | Fill wording honestly describes spatial/direct-touch targeting | PASS |
| P24 | Free Draw large-text controls, named colors and selected cue | PASS |
| P25 | Gallery large-text/read-only semantics and delete behavior | PASS |
| P26 | Key supporting text contrast/state does not rely on color alone | PASS |
| P27 | Small-screen reachability/touch-target/gesture smoke | PASS |
| P28 | Airplane Mode critical child + parent flows remain functional | PASS |
| P29 | TalkBack traversal is understandable without noisy stroke/frame announcements | PASS |
| P30 | Drawing/Help/Trace/Coloring/Gallery/session regression smoke | PASS |

### Physical status

- PASS: **30 / 30**
- FAIL: **0 / 30**
- NOT RUN: **0 / 30**
- acceptance date: **2026-09-16**
- tester device/model/API: **not provided and not inferred**
- reported release blockers: **none**
- release decision: **ACCEPTED FOR P6.4 REPOSITORY CLOSURE**

## Closure rule

With P01–P30 physically accepted:
1. finalize release report;
2. require acceptance-documentation CI GREEN;
3. mark PR #99 ready and squash-merge;
4. require merged-main Android CI GREEN;
5. close #98 completed;
6. activate P6.5 #100 only from that verified merged-main baseline.

Any executable change after this accepted candidate requires a new monotonic versionCode (>30) and fresh exact-binary QA. Documentation/merge commits do not replace the accepted executable.