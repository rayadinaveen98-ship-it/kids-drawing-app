# P5.2 — Content Production System V2 QA

**Parent epic:** #73  
**Issue:** #76  
**PR:** #77  
**Branch:** `phase5/p5-2-content-production-v2`  
**QA target:** `0.5.0-curriculum-expansion-p5.2-qa2` / versionCode 21  
**Status:** QA2 freeze in progress; physical/developer inspection pending

## 1. Scope

P5.2 proves that content can scale beyond the Phase-4 representative catalog without introducing a second lesson runtime or relying on manual inspection alone.

The quality system is read-only over the production `LessonCatalog`, `LessonPackageLoader`, validated runtime models and prepared-coloring geometry. The child-facing product remains the normal launcher. `ContentLabActivity` is a separate engineering inspection activity and has no child document/session persistence dependency.

## 2. Automated evidence already proven before QA freeze

Implementation baseline `1b63530dd3e466311921cebc5fa7c0497c57cb93` passed Android CI #465 / run `34806642996`.

Proven gates:
- committed JSON parsing — PASS;
- AndroidX Ink boundary — PASS;
- unit tests — PASS;
- lint — PASS;
- debug APK — PASS;
- instrumentation APK — PASS;
- profile APK — PASS;
- APK permission allowlist — PASS;
- P5.2 content-quality report verification — PASS;
- P5.2 content-quality report artifact upload — PASS.

Calibrated production-catalog report:
- release lessons: 9;
- quality errors: 0;
- quality warnings: 0;
- Little Artists coverage: 4;
- Creative Explorers coverage: 8;
- Growing Artists coverage: 7;
- Young Artists coverage: 2;
- difficulty 1/2/3/4 represented; difficulty 5 remains a Phase-5 curriculum gap;
- Watch Then Draw: 6/6 Phase-5 minimum already represented;
- coloring lessons: 3;
- prepared-coloring lessons: 2;
- Phase-5 catalog progress: 9/24.

Calibrated report artifact from CI #465: `10333610875`.

## 3. Automated content-quality contracts

The following are covered by deterministic JVM/production-catalog tests:

- production catalog diagnostics become release-blocking quality errors;
- accepted nine-lesson catalog produces zero release errors;
- coverage counts are deterministic across repeated analysis;
- Phase-5 lesson/age/difficulty/mode progress is reported without prematurely failing the nine-lesson baseline;
- excessive age-inappropriate step-count heuristic emits warning, not error;
- tiny expected child target emits warning;
- tiny teacher demonstration emits warning;
- duplicate teacher stroke references emit warning;
- duplicate expected child stroke references emit warning;
- grouped demonstration with fewer than two distinct strokes emits warning;
- Help Ladder authored out of ascending order emits warning;
- deliberately tiny prepared-color region emits warning;
- legitimate standalone/no-journey content can opt out of the standalone warning;
- prepared-coloring coverage is read from validated runtime packages;
- report JSON is parseable and deterministic;
- inspection repository exposes all nine release packages, preview/thumbnail SVG, localization, Help data and prepared-region data without mutation APIs.

Production `LessonPackageLoader` / `LessonCatalog` remain authoritative for structural failures such as malformed JSON/schema, unsafe asset paths, invalid IDs, duplicate IDs, missing stroke/guide references, out-of-canvas/invalid stroke geometry, unsupported content API, invalid Trace support, missing localization/assets, duplicate release lesson IDs, missing prerequisite lesson references and mathematically invalid prepared regions.

## 4. Candidate history and immutable artifact evidence

### QA1 — rejected before distribution

QA1 identity was `0.5.0-curriculum-expansion-p5.2-qa1`, versionCode 20, frozen at `07025d8cf35569179b1ee1e9443303bd92019093`.

Android CI #471 / run `34807204480` failed in `compileDebugAndroidTestKotlin`. The QA-freeze edit to `app/build.gradle.kts` had accidentally removed the existing Compose UI test dependencies:
- `androidTestImplementation(libs.compose.ui.test.junit4)`
- `debugImplementation(libs.compose.ui.test.manifest)`

Production/debug/profile Kotlin had compiled before the instrumentation-test compile failure. No QA1 APK was distributed or accepted. The dependency tail was restored from the last green implementation baseline, and the next candidate uses a new versionCode as required.

### QA2 — current candidate

Fill only after the exact frozen QA2 head is green.

- versionName: `0.5.0-curriculum-expansion-p5.2-qa2`
- versionCode: 21
- executable commit: PENDING
- exact-head Android CI: PENDING
- debug artifact ID: PENDING
- profile artifact ID: PENDING
- content-quality report artifact ID: PENDING
- profile APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.2_QA2-profile.apk`
- profile APK size: PENDING
- profile APK SHA-256: PENDING
- independent local size/hash verification: PENDING

## 5. Physical / developer Content Lab matrix

Do not mark a row PASS unless it was actually exercised on the QA2 APK.

| # | Scenario | Status |
|---:|---|---|
| 1 | Normal app launcher still opens `ProductActivity`; Content Lab is not inserted into the child-facing Studio flow. | PENDING |
| 2 | Launch `ContentLabActivity` explicitly (ADB or activity launcher) and it opens without crash. | PENDING |
| 3 | Catalog summary reports 9 lessons, 0 errors and Phase-5 progress 9/24. | PENDING |
| 4 | All 9 lessons can be selected; title, stable ID/revision, ages, difficulty, modes and skills are sensible. | PENDING |
| 5 | Preview SVG renders for all 9 release lessons. | PENDING |
| 6 | Thumbnail SVG renders for all 9 release lessons. | PENDING |
| 7 | Step selectors work across representative early, normal, grouped, older-child and creative lessons without crash/stale geometry. | PENDING |
| 8 | Teacher geometry changes with selected step and visually matches the authored subject. | PENDING |
| 9 | Expected/Trace geometry is separately visible where authored and absent where intentionally open-ended. | PENDING |
| 10 | Friendly Owl Help Ladder metadata/guide geometry is inspectable, including high help levels. | PENDING |
| 11 | Little Fish prepared coloring regions are visible separately from strokes/guides. | PENDING |
| 12 | Hot Air Balloon prepared coloring regions are visible separately from strokes/guides. | PENDING |
| 13 | Design Your Spaceship `Make It Yours` shows no required expected replica strokes. | PENDING |
| 14 | Default localization panel resolves the selected lesson/step semantic keys; no release-content `<missing>` values encountered. | PENDING |
| 15 | Quality diagnostics panel is readable; accepted baseline content does not show release-blocking errors. | PENDING |
| 16 | Using/switching Content Lab does not create or alter child drawing documents, lesson progress, coloring progress or Gallery entries. | PENDING |
| 17 | Exit Content Lab and reopen normal product; previous child product state remains intact. | PENDING |
| 18 | Content Lab works in Airplane Mode because inspection is bundled/local. | PENDING |
| 19 | Larger Android system font scale remains scrollable/reachable enough for developer inspection. | PENDING |
| 20 | No crash, ANR, deadlock or unrecoverable blank inspection surface during the focused pass. | PENDING |

## 6. Focused product regression

P5.2 is engineering tooling, but its APK still contains the production app. Before acceptance, spot-check:
- Studio/Home loads normally;
- one guided lesson can start;
- Free Draw opens;
- Little Fish prepared coloring opens;
- Gallery opens;
- no new permissions/network/account requirement appeared.

Status: PENDING physical smoke.

## 7. Exit rule

P5.2 is complete only when:
1. exact frozen QA2 CI is green;
2. QA2 APK/report artifacts have reproducible IDs, size and SHA evidence;
3. focused Content Lab device matrix is accepted;
4. normal product smoke is accepted;
5. QA/status/handoff docs record only evidence actually obtained;
6. final exact-head acceptance CI is green;
7. PR #77 is ready and merged;
8. merged-main CI is green;
9. issue #76 closes completed.

Bulk P5.4–P5.6 lesson authoring remains blocked until this exit rule is satisfied.
