# P5.2 — Content Production System V2 QA

**Parent epic:** #73  
**Issue:** #76  
**PR:** #77  
**Branch:** `phase5/p5-2-content-production-v2`  
**Accepted QA target:** `0.5.0-curriculum-expansion-p5.2-qa2` / versionCode 21  
**Status:** QA2 automated + focused physical/developer acceptance PASS; final acceptance-doc CI and merge pending

## 1. Scope

P5.2 proves that content can scale beyond the Phase-4 representative catalog without introducing a second lesson runtime or relying on manual inspection alone.

The quality system is read-only over the production `LessonCatalog`, `LessonPackageLoader`, validated runtime models and prepared-coloring geometry. The child-facing product remains the normal launcher. `ContentLabActivity` is a separate engineering inspection activity and has no child document/session persistence dependency.

## 2. Automated evidence proven before QA freeze

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

Production/debug/profile Kotlin had compiled before the instrumentation-test compile failure. No QA1 APK was distributed or accepted. The dependency tail was restored from the last green implementation baseline, and the next candidate used a new versionCode as required.

### QA2 — accepted focused physical/developer candidate

- versionName: `0.5.0-curriculum-expansion-p5.2-qa2`
- versionCode: 21
- executable commit: `2f36834d110cb1076b953f519eee4a8dc6e2e19d`
- exact-head Android CI: #477 / run `34808016949` — GREEN
- debug artifact ID: `10333752707`
- profile artifact ID: `10333593024`
- content-quality report artifact ID: `10334126516`
- profile artifact archive digest: `sha256:9cf3a553b9d532b28725b6c1e08288bc5440aa7efcc1f8296a7d4f04340e551a`
- profile APK filename: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.2_QA2-profile.apk`
- profile APK size: `16,245,548 bytes`
- profile APK SHA-256: `725720ae1fbff232cbb56049d77b087d8214f959189269630dbbd9eeb0817cf6`
- independent local size/hash verification: PASS; local recomputation matched CI `APK_SIZES.txt` and `SHA256SUMS.txt`
- exact #477 content-quality report: 9 lessons / 0 errors / 0 warnings

Freeze audit from green implementation baseline `1b63530dd3e466311921cebc5fa7c0497c57cb93` to accepted QA2 head showed no Content Lab/analyzer/runtime source drift after the green implementation proof; changes were candidate/version/test-dependency restoration, CI packaging and QA/status documentation only.

## 5. Physical / developer Content Lab matrix

The user installed the exact QA2 v21 APK and, after receiving the full focused checklist covering all rows below, reported **“passed bro”** on 2026-09-14. Therefore these rows are recorded as focused physical/developer PASS against the exact accepted QA2 binary.

| # | Scenario | Status |
|---:|---|---|
| 1 | Normal app launcher still opens `ProductActivity`; Content Lab is not inserted into the child-facing Studio flow. | PASS |
| 2 | Launch `ContentLabActivity` explicitly (ADB or activity launcher) and it opens without crash. | PASS |
| 3 | Catalog summary reports 9 lessons, 0 errors and Phase-5 progress 9/24. | PASS |
| 4 | All 9 lessons can be selected; title, stable ID/revision, ages, difficulty, modes and skills are sensible. | PASS |
| 5 | Preview SVG renders for all 9 release lessons. | PASS |
| 6 | Thumbnail SVG renders for all 9 release lessons. | PASS |
| 7 | Step selectors work across representative early, normal, grouped, older-child and creative lessons without crash/stale geometry. | PASS |
| 8 | Teacher geometry changes with selected step and visually matches the authored subject. | PASS |
| 9 | Expected/Trace geometry is separately visible where authored and absent where intentionally open-ended. | PASS |
| 10 | Friendly Owl Help Ladder metadata/guide geometry is inspectable, including high help levels. | PASS |
| 11 | Little Fish prepared coloring regions are visible separately from strokes/guides. | PASS |
| 12 | Hot Air Balloon prepared coloring regions are visible separately from strokes/guides. | PASS |
| 13 | Design Your Spaceship `Make It Yours` shows no required expected replica strokes. | PASS |
| 14 | Default localization panel resolves the selected lesson/step semantic keys; no release-content `<missing>` values encountered. | PASS |
| 15 | Quality diagnostics panel is readable; accepted baseline content does not show release-blocking errors. | PASS |
| 16 | Using/switching Content Lab does not create or alter child drawing documents, lesson progress, coloring progress or Gallery entries. | PASS |
| 17 | Exit Content Lab and reopen normal product; previous child product state remains intact. | PASS |
| 18 | Content Lab works in Airplane Mode because inspection is bundled/local. | PASS |
| 19 | Larger Android system font scale remains scrollable/reachable enough for developer inspection. | PASS |
| 20 | No crash, ANR, deadlock or unrecoverable blank inspection surface during the focused pass. | PASS |

## 6. Focused product regression

The same user acceptance covered the requested normal-product smoke:
- Studio/Home loads normally — PASS;
- one guided lesson can start — PASS;
- Free Draw opens — PASS;
- Little Fish prepared coloring opens — PASS;
- Gallery opens — PASS;
- no new permissions/network/account requirement appeared — PASS.

Status: **PASS focused physical smoke**.

This is a focused P5.2 acceptance, not a substitute for the later P5.8 full cross-age 0.5 release matrix.

## 7. Exit rule

P5.2 is complete only when:
1. exact frozen QA2 CI is green — PASS;
2. QA2 APK/report artifacts have reproducible IDs, size and SHA evidence — PASS;
3. focused Content Lab device matrix is accepted — PASS;
4. normal product smoke is accepted — PASS;
5. QA/status/handoff docs record only evidence actually obtained — IN PROGRESS in acceptance-doc commit;
6. final exact-head acceptance CI is green — PENDING;
7. PR #77 is ready and merged — PENDING;
8. merged-main CI is green — PENDING;
9. issue #76 closes completed — PENDING.

Bulk P5.4–P5.6 lesson authoring remains blocked until this exit rule is satisfied.
