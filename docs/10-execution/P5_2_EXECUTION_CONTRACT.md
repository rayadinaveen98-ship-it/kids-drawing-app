# P5.2 — Content Production System V2 Execution Contract

**Parent epic:** #73  
**Issue:** #76  
**Branch:** `phase5/p5-2-content-production-v2`  
**Baseline:** P5.1 merge `cea06e219290c82b9a1f8f8007069c61841bbc95` / merged-main CI #452 GREEN  
**Status:** implementation active

## 1. Purpose

Phase 5 expands the verified catalog from 9 to 24 production lessons. Before bulk authoring begins, the repository needs a content-production quality system that catches structural, coverage and likely usability mistakes early.

The system must **reuse production content truth**. It is not a CMS, cloud backend, subjective art grader, or parallel lesson engine.

## 2. Source-of-truth rule

Authoring/quality tooling must build on:
- `LessonPackageLoader` for package decoding and semantic validation;
- `LessonCatalog` for release package discovery, duplicate isolation, declared asset/localization integrity and cross-catalog prerequisite validation;
- `ColoringRegionValidator` for prepared-region geometry/reference validation;
- `LessonRuntimePackage` / existing lesson models for inspected package data;
- existing app render/domain utilities where previewing is added.

If authoring tooling disagrees with runtime truth, runtime/production contracts win and the tooling must be corrected.

No second JSON schema interpreter or independent lesson runtime is permitted.

## 3. Internal architecture

P5.2 is split into four layers.

### Layer A — Content quality domain
Pure Kotlin, Android-independent where practical.

Owns:
- catalog coverage model;
- per-lesson readiness model;
- severity/error/warning codes;
- deterministic structural/usability warning policies;
- stable text/JSON-friendly projections for CI/reporting.

Input is a production `LessonCatalogSnapshot` and its validated `LessonRuntimePackage`s.

### Layer B — Repository/catalog runner
Uses the same bundled content source/catalog discovery path as production/test infrastructure.

Owns:
- load all release lessons;
- run quality analyzer;
- fail on release-blocking errors;
- emit deterministic coverage/readiness output;
- expose reports to JVM tests/CI.

### Layer C — Authoring inspection UI
Developer-only/internal app surface. It must never mutate child artwork/session truth.

Owns visual inspection for a selected package:
- preview/thumbnail;
- logical canvas bounds;
- teacher strokes by step/group;
- guide/trace geometry;
- prepared coloring regions + IDs;
- modes/pace/step ordering;
- Help Ladder entries;
- strings/localization usage;
- readiness warnings/errors.

The inspection UI should reuse existing rendering/domain geometry where practical.

### Layer D — CI release gate
Runs catalog + quality checks on every PR/main build and publishes readable coverage/readiness evidence.

Existing Android gates remain mandatory.

## 4. Diagnostic severity

### ERROR
Release-blocking. Examples:
- catalog/package production diagnostic;
- duplicate/unstable identity;
- missing required asset/string/reference;
- unsupported mode/content API;
- invalid prepared-region geometry/reference;
- impossible cross-catalog prerequisite;
- geometry outside logical canvas where production contracts require in-bounds content;
- no meaningful drawing step/teacher construction for a guided release package.

### WARNING
Requires author review but does not automatically reject accepted legacy content. Examples:
- suspiciously high step count for target age;
- extremely small expected/trace/fill target relative to canvas and youngest supported age;
- missing journey membership where curriculum review may still allow a standalone lesson;
- Help distribution that looks inconsistent with age/difficulty;
- creative/open lesson with no obvious discretionary child turn;
- unusual mode/difficulty combination.

Warnings must never be silently dropped. The baseline 9-lesson catalog may legitimately have warnings; P5.2 must distinguish that from false release errors.

## 5. Coverage report contract

A deterministic report must include at minimum:
- total accepted release lessons;
- catalog diagnostic/error count;
- warning count;
- counts by `AgeBand`;
- counts by difficulty 1–5;
- counts by category ID;
- counts by skill ID;
- counts by journey ID;
- counts by `TeachingMode`;
- count with coloring enabled;
- count with prepared coloring regions;
- per-lesson summary: ID/revision/title/age/difficulty/categories/skills/journeys/modes/step count/coloring/prepared-regions/readiness result.

Ordering must be deterministic so CI diffs are meaningful.

## 6. Phase-5 target evaluation

The analyzer/report must be able to evaluate P5.1 coverage gates without hard-coding lesson IDs:
- total target: 24;
- Little Artists: 8+;
- Creative Explorers: 14+;
- Growing Artists: 14+;
- Young Artists: 10+;
- at least 3 difficulty-4 lessons;
- at least 1 difficulty-5 lesson;
- observation/Watch Then Draw target visibility;
- journey coverage visibility.

During P5.2 and early content expansion, unmet *future* 24-lesson targets are reported as milestone progress, not release errors against the existing 9-lesson baseline. P5.8 converts the final target into a release gate.

## 7. Deterministic geometry/usability warnings

P5.2 may add conservative warnings based on authored geometry. It must not claim subjective art quality.

Allowed deterministic checks include:
- authored stroke point outside canvas;
- zero/near-zero stroke extent;
- expected/trace geometry with very small bounding extent relative to canvas;
- prepared region area unusually small for youngest declared age;
- excessive meaningful drawing step count relative to age-band policy;
- empty teacher stroke group;
- repeated refs in a group/step where duplicates have no semantic purpose;
- Help entries referencing no usable guide for guide/trace kinds.

Thresholds must be documented and tested. Existing accepted content should be baseline-run before any warning becomes an ERROR.

## 8. Preview safety

The authoring inspection surface:
- is developer/internal only;
- does not create child session snapshots;
- does not write Gallery entries;
- does not mutate normal working documents;
- may render authored geometry read-only;
- must clearly label teacher/guide/trace/color-region layers;
- must be safe offline.

## 9. Testing requirements

Automated coverage must include:
- existing 9-lesson catalog baseline: zero quality ERROR beyond production catalog diagnostics (expected zero on accepted main content);
- deterministic coverage counts/report ordering;
- production catalog diagnostic → quality ERROR projection;
- excessive-step warning fixture;
- tiny-target warning fixture;
- no-journey warning behavior if enabled;
- prepared-region summary/coverage;
- no mutation of runtime package/document truth;
- negative fixtures for critical release errors already owned by production loader/catalog remain regression-green.

## 10. CI requirements

Before P5.2 closes:
- content quality unit tests run in normal Android CI;
- bundled 9-lesson catalog quality baseline runs automatically;
- human-readable report is visible in CI log and/or artifact;
- machine-readable report is available for later P5.8 gating;
- all existing JSON/Ink/unit/lint/debug/instrumentation/profile/permission gates stay green.

## 11. Incremental implementation order

1. Domain report/analyzer + tests.
2. Baseline runner against existing 9 release lessons.
3. Deterministic geometry/usability warnings + negative tests.
4. CI report/gate integration.
5. Internal inspection UI.
6. QA APK + developer/physical verification of inspection UI.
7. Exact-head CI → merge-main CI → close #76.

Do not start bulk P5.4–P5.6 lesson authoring before P5.2 acceptance.

## 12. Versioning

P5.2 is an internal production-system milestone. Do not bump the public `0.4.0-content-studio` app identity merely for pure tooling/domain work.

If an internal inspection UI APK is distributed for QA, assign a monotonic QA versionCode/versionName at that freeze point; never reuse a distributed versionCode.
