# P5.3 — Companion / Teacher Experience V2 QA

**Parent epic:** #73  
**Issue:** #78  
**PR:** #79  
**Branch:** `phase5/p5-3-companion-teacher-v2`  
**QA target:** `0.5.0-curriculum-expansion-p5.3-qa1` / versionCode 22  
**Status:** QA1 exact binary physically accepted; final acceptance-doc CI / merge gates pending

## 1. Scope

P5.3 upgrades the companion/teacher presentation while preserving the existing Lesson Engine, session state, Help Ladder, teacher playback, artwork isolation and persistence as authoritative product truth.

The companion remains a pure presentation projection. It may vary wording and optional cues by the child profile age band and current lesson/session context, but it never owns progress, Help level, completion, artwork, scoring, or persistence.

## 2. Automated implementation evidence before QA freeze

Pure policy baseline:
- first policy CI #485 exposed one wording-test mismatch only;
- the Trace support phrase was refined to avoid scoring vocabulary rather than weakening the safety assertion;
- corrected pure-policy CI #486 / run `34810895376` — GREEN.

Integrated UI baseline:
- Guided Lesson supplies the existing product profile `AgeBand` to `ProductLessonPresentationPolicy`;
- secondary companion cues render inside the companion card;
- optional reflection renders only at the post-drawing boundary and never blocks Color/Finish actions;
- legacy lesson-specific `Cute Cat` / `save your cat` fallback copy was removed;
- existing lesson commands/control visibility were preserved;
- integrated CI #487 / run `34811189264` — GREEN.

## 3. Deterministic policy coverage

JVM tests cover:
- existing child-turn control visibility (Replay, Help, Done, Skip) remains authored/session-driven;
- non-skippable and `ANY_STROKE` semantics remain unchanged;
- teacher demonstration does not expose child completion controls;
- completion stays non-scoring and provides an optional reflection prompt;
- Little Artist and Young Artist use materially different tone without changing control semantics;
- open-choice detection is generic: `MANUAL_DONE + expectedStrokeRefs.isEmpty()`;
- open-choice copy avoids copy/match pressure;
- required-stroke steps do not accidentally use open-choice language;
- Trace & Learn uses guide/practice language with no punitive/scoring vocabulary;
- high Help remains non-punitive and age-aware across all four profile age bands;
- Watch Then Draw overview is distinct from per-step demonstration;
- reflection prompts differ by all four age bands;
- identical inputs produce identical presentation;
- every current `LessonSessionState` maps deterministically without crash.

## 4. QA1 immutable artifact evidence

- versionName: `0.5.0-curriculum-expansion-p5.3-qa1`
- versionCode: 22
- executable commit: `eef87b25478c6a30d6fefbd7580f75ded4eca3ab`
- exact-head Android CI: #493 / run `34811688427` — GREEN
- debug artifact ID: `10335460247`
- profile artifact ID: `10334873551`
- content-quality report artifact ID: `10334589314`
- profile artifact archive digest: `sha256:aa896ee0d42f491ccb85951f4f0c08e98c4c706a5f988c5ecfad417f6d0cc2a5`
- profile APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.3_QA1-profile.apk`
- profile APK size: `16,245,553 bytes`
- profile APK SHA-256: `bed4c00bce4629822b50c6523527a1ebad66428fc0977580501a20c77ad3e5de`
- independent local size/hash verification: MATCHED CI evidence
- content-quality report: 9 lessons / 0 errors / 0 warnings

## 5. Focused physical/product matrix — ACCEPTED 2026-09-14

User reported **“passed bro”** after being given this exact focused checklist for the exact QA1 v22 binary above. Device model / Android API level were not restated in the acceptance report and are therefore intentionally not inferred.

| # | Scenario | Status |
|---:|---|---|
| 1 | Normal app launches and Studio/Home remains usable; no new permissions/account/network requirement appears. | PASS |
| 2 | Little Artist guided lesson shows short, warm companion language and clearly distinguishes teacher turn from child turn. | PASS |
| 3 | Young Artist guided lesson uses concise studio/structure language and does not feel toddler-oriented. | PASS |
| 4 | Teacher demonstration blocks child drawing as before and does not expose Done/Help/Replay incorrectly. | PASS |
| 5 | Child turn enables drawing and preserves authored Replay/Help/Done/Skip availability. | PASS |
| 6 | Watch Then Draw full overview feels distinct from the later per-step teacher demonstration; `I’m ready` remains available only for the overview. | PASS |
| 7 | Replay gives another reference without blame/failure wording and returns to the correct child flow. | PASS |
| 8 | Request Help; companion explains support calmly and authored guide/help overlay appears without entering child artwork. | PASS |
| 9 | Increase to strong/Trace help where available; tracing is presented as normal practice, with Less Help / Hide Help still usable. | PASS |
| 10 | Trace & Learn child turn uses guide/practice language without score/accuracy/failure pressure. | PASS |
| 11 | Design Your Spaceship `Make It Yours` uses authorship/choice language and allows arbitrary details + Done without asking the child to copy/match the example. | PASS |
| 12 | A normal required-stroke manual step still gives structure/observation guidance rather than open-choice wording. | PASS |
| 13 | Completion celebrates authorship/process without stars, points, grades, accuracy or ranking language. | PASS |
| 14 | Optional reflection prompt is visible but does not block Color with me / Color myself / Finish for now. | PASS |
| 15 | Pause/resume keeps calm age-appropriate copy and resumes the correct session state. | PASS |
| 16 | Save & leave / reopen or background / return does not leave stale companion wording for the wrong session state. | PASS |
| 17 | No lesson-specific `Cute Cat`/`cat` fallback wording appears while using non-cat lessons. | PASS |
| 18 | Companion card/secondary cue remains readable without covering the drawing surface or essential controls on the test device. | PASS |
| 19 | Little Fish coloring, Free Draw and Gallery still open normally after guided-lesson use. | PASS |
| 20 | No crash, ANR, deadlock, lost drawing, or unexpected state reset during the focused pass. | PASS |

## 6. Acceptance boundary

This is a focused P5.3 teaching-presentation pass. It does not replace the later P5.8 full cross-age 0.5 release matrix.

Satisfied gates:
1. exact frozen QA1 CI green;
2. reproducible artifact ID/size/SHA evidence;
3. focused Little + Young + Watch/Help/Trace/open-choice/completion/device matrix accepted 20/20;
4. normal product regressions accepted.

Remaining gates:
5. final acceptance-doc exact-head CI green;
6. PR #79 ready and squash-merged;
7. merged-main CI green;
8. issue #78 closes completed.
