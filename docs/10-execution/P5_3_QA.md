# P5.3 — Companion / Teacher Experience V2 QA

**Parent epic:** #73  
**Issue:** #78  
**PR:** #79  
**Branch:** `phase5/p5-3-companion-teacher-v2`  
**QA target:** `0.5.0-curriculum-expansion-p5.3-qa1` / versionCode 22  
**Status:** QA1 freeze in progress; physical/product acceptance pending

## 1. Scope

P5.3 upgrades the companion/teacher presentation while preserving the existing Lesson Engine, session state, Help Ladder, teacher playback, artwork isolation and persistence as authoritative product truth.

The companion remains a pure presentation projection. It may vary wording and optional cues by the child profile age band and current lesson/session context, but it never owns progress, Help level, completion, artwork, scoring, or persistence.

## 2. Automated implementation evidence before QA freeze

Pure policy baseline:
- first policy CI #485 exposed one wording-test mismatch only;
- the Trace support phrase was refined to avoid scoring vocabulary rather than weakening the safety assertion;
- corrected pure-policy CI #486 / run `34810895376` — GREEN.

Integrated UI baseline:
- Guided Lesson now supplies the existing product profile `AgeBand` to `ProductLessonPresentationPolicy`;
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

Fill only after exact frozen QA1 CI is green.

- versionName: `0.5.0-curriculum-expansion-p5.3-qa1`
- versionCode: 22
- executable commit: PENDING
- exact-head Android CI: PENDING
- debug artifact ID: PENDING
- profile artifact ID: PENDING
- content-quality report artifact ID: PENDING
- profile APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.3_QA1-profile.apk`
- profile APK size: PENDING
- profile APK SHA-256: PENDING
- independent local size/hash verification: PENDING

## 5. Focused physical/product matrix

Do not mark a row PASS unless it was actually exercised on the exact QA1 APK.

| # | Scenario | Status |
|---:|---|---|
| 1 | Normal app launches and Studio/Home remains usable; no new permissions/account/network requirement appears. | PENDING |
| 2 | Little Artist guided lesson shows short, warm companion language and clearly distinguishes teacher turn from child turn. | PENDING |
| 3 | Young Artist guided lesson uses concise studio/structure language and does not feel toddler-oriented. | PENDING |
| 4 | Teacher demonstration blocks child drawing as before and does not expose Done/Help/Replay incorrectly. | PENDING |
| 5 | Child turn enables drawing and preserves authored Replay/Help/Done/Skip availability. | PENDING |
| 6 | Watch Then Draw full overview feels distinct from the later per-step teacher demonstration; `I’m ready` remains available only for the overview. | PENDING |
| 7 | Replay gives another reference without blame/failure wording and returns to the correct child flow. | PENDING |
| 8 | Request Help; companion explains support calmly and authored guide/help overlay appears without entering child artwork. | PENDING |
| 9 | Increase to strong/Trace help where available; tracing is presented as normal practice, with Less Help / Hide Help still usable. | PENDING |
| 10 | Trace & Learn child turn uses guide/practice language without score/accuracy/failure pressure. | PENDING |
| 11 | Design Your Spaceship `Make It Yours` uses authorship/choice language and allows arbitrary details + Done without asking the child to copy/match the example. | PENDING |
| 12 | A normal required-stroke manual step still gives structure/observation guidance rather than open-choice wording. | PENDING |
| 13 | Completion celebrates authorship/process without stars, points, grades, accuracy or ranking language. | PENDING |
| 14 | Optional reflection prompt is visible but does not block Color with me / Color myself / Finish for now. | PENDING |
| 15 | Pause/resume keeps calm age-appropriate copy and resumes the correct session state. | PENDING |
| 16 | Save & leave / reopen or background / return does not leave stale companion wording for the wrong session state. | PENDING |
| 17 | No lesson-specific `Cute Cat`/`cat` fallback wording appears while using non-cat lessons. | PENDING |
| 18 | Companion card/secondary cue remains readable without covering the drawing surface or essential controls on the test device. | PENDING |
| 19 | Little Fish coloring, Free Draw and Gallery still open normally after guided-lesson use. | PENDING |
| 20 | No crash, ANR, deadlock, lost drawing, or unexpected state reset during the focused pass. | PENDING |

## 6. Acceptance boundary

This is a focused P5.3 teaching-presentation pass. It does not replace the later P5.8 full cross-age 0.5 release matrix.

P5.3 is complete only when:
1. exact frozen QA1 CI is green;
2. QA1 APK artifacts have reproducible ID/size/SHA evidence;
3. focused Little + Young + Watch/Help/Trace/open-choice/completion/device matrix is accepted;
4. normal product regressions are accepted;
5. QA/status/handoff docs record only evidence actually obtained;
6. final acceptance-doc exact-head CI is green;
7. PR #79 is ready and squash-merged;
8. merged-main CI is green;
9. issue #78 closes completed.
