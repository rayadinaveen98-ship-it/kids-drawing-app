# P5.8 Execution Contract — Cross-age Curriculum QA + 0.5 Release

**Parent epic:** #73  
**Issue:** #88  
**Verified starting main:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Verified starting merged-main CI:** Android CI #571 / run `34869966116` — **GREEN**  
**Branch:** `phase5/p5-8-cross-age-release`  
**Target final versionName:** `0.5.0-curriculum-expansion`  
**Final versionCode:** **RESERVED AFTER PRE-FREEZE GREEN; MUST BE >26**  
**Status:** **LOCKED BEFORE RELEASE IMPLEMENTATION**

## 1. Purpose

P5.8 is the final integrated validation and release gate for Phase 5. It does not add a parallel lesson system, new curriculum, new child profiling, or opportunistic product features. Its job is to prove that the complete 24-lesson offline product works coherently across all four age bands and to deliver one reproducible final `0.5.0-curriculum-expansion` APK with exact Git/CI/artifact/physical-QA evidence.

Phase 5 closes only after the exact final executable is automated-green, physically accepted, documented, squash-merged, and followed by green merged-main CI.

## 2. Frozen baseline

The P5.8 starting product is the P5.7 squash merge on `main`:

- commit `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`;
- Android CI #571 / run `34869966116` GREEN;
- 24 production lessons;
- P5.7 accepted profile candidate versionCode 26;
- P5.7 focused physical acceptance 45/45 PASS with no reported defects;
- content quality contract: 24 lessons / 0 errors / exactly 6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings.

The six reviewed warning lesson IDs remain:

- `ice-cream-shop`;
- `one-point-room`;
- `rainbow-weather`;
- `sailboat-scene`;
- `simple-car`;
- `tree-through-seasons`.

Any deliberate content change that changes this warning contract requires explicit review and updated evidence. P5.8 does not change it by default.

## 3. Scope discipline

P5.8 permits only:

1. integrated regression tests and deterministic release gates that prove already accepted product contracts;
2. final version/workflow/artifact-evidence changes after the pre-freeze gate is green;
3. narrowly scoped fixes for release-blocking defects discovered by P5.8 validation;
4. QA/evidence/status documentation.

P5.8 does **not** permit opportunistic curriculum expansion, visual redesign, engine replacement, new cloud/account/network dependencies, analytics, ads, new sensitive permissions, hidden child scoring, unrestricted generative AI, or lesson-ID-specific runtime UI branches.

A non-blocking product idea is deferred beyond Phase 5.

## 4. Frozen architecture and safety invariants

The following remain non-negotiable:

- `DrawingDocument` / Drawing Engine own editable artwork and history truth;
- `LessonSessionState` owns teaching-state truth;
- session snapshots/persistence own recovery truth;
- `ChildProfile` owns explicit age/interests/mode/pace preferences;
- `StudioPrimarySelectionPolicy` preserves coloring resume → drawing resume → fresh recommendation;
- `StudioRecommendationPolicy` remains the Home recommendation surface;
- P5.7 adaptive state is local, bounded, versioned, deterministic, explainable and advisory only;
- adaptation cannot unlock unmet prerequisites or override explicit child preferences silently;
- Companion remains read-only relative to artwork/session truth;
- Help remains child-initiated and authored;
- Trace is never invented;
- teacher/help/reference overlays never enter child artwork/history/persistence;
- coloring/fill cannot damage protected line art;
- Free Draw remains lesson-independent with correct Gallery provenance;
- Gallery copies remain isolated from protected working documents;
- core product remains offline-first, account-free, ad-free and without behavioral analytics;
- no raw artwork/strokes, ability labels, grades/scores/ranks/XP, punitive streaks, cloud child profiling or analytics upload enter adaptive state.

## 5. Pre-freeze automated release gate

Before assigning the final versionCode, exact-head CI must prove the complete integrated baseline.

### 5.1 Catalog and content

Automated coverage must prove:

- exactly 24 release lessons load through the production package path;
- zero content-quality errors;
- exactly six reviewed `NO_JOURNEY_MEMBERSHIP` warnings with the frozen lesson IDs;
- all lesson asset/localization/reference dependencies resolve;
- all four age bands have release content;
- declared teaching modes are executable by the generic lesson flow;
- journey membership/prerequisite references are valid;
- no invalid prerequisite becomes an adaptive fresh-primary recommendation.

### 5.2 Teaching modes and Help

Automated coverage must preserve:

- Draw With Me execution;
- Watch Then Draw execution;
- authored Trace execution only where authored;
- authored Help Ladder / Replay behavior;
- no invented Trace;
- no automatic Help escalation;
- child-invoked adaptive Help remains inside current authored options;
- Companion remains read-only;
- teacher/reference overlays remain outside child artwork/history.

### 5.3 Cross-age recommendations and discovery

For all four age bands, tests must prove:

- deterministic recommendations for identical input;
- exact-age eligibility dominates fallback when valid exact-age content exists;
- prerequisites stay authoritative;
- journey continuation is deterministic;
- explicit interests still influence ranking;
- underexposed/new skill opportunity can influence ranking without an ability label;
- completed/recent content cannot dominate fresh uncompleted eligible content;
- recommendation reason copy is non-judgmental;
- browse/category/journey discovery remains available independent of the fresh-primary ranking;
- resume precedence remains coloring > drawing > fresh.

### 5.4 Lifecycle / recovery

Automated regression must cover the accepted persistence boundaries for:

- guided drawing save/relaunch/recovery;
- lesson session recovery;
- Save & Leave;
- process recreation/corruption-safe fallbacks where testable on JVM;
- drawing resume precedence;
- coloring resume precedence;
- adaptive-state missing/corrupt/future-format fallback;
- no duplicate completion/help state from replayed lifecycle delivery.

### 5.5 Studio surfaces

Automated regression must continue covering:

- Gallery save/reopen/delete isolation;
- prepared and legacy Coloring behavior;
- Free Draw persistence/history/provenance;
- catalog discovery/categories/journeys;
- profile-derived presentation logic.

### 5.6 Offline / permission / privacy

The release gate must prove structurally that:

- Android permission allowlist does not expand beyond the accepted app-generated dynamic-receiver permission;
- core production code has no newly introduced network dependency/path;
- adaptive persisted shape contains no raw stroke/artwork payload, score/rank/ability label or cloud identifier;
- Airplane Mode remains a required physical acceptance row.

## 6. Integrated P5.8 gate design

P5.8 adds one explicit integrated release-gate test suite rather than relying only on the existence of scattered slice tests.

The gate must aggregate stable production contracts and fail loudly if any release invariant regresses. It should prefer public/owned product APIs and content validators over duplicating implementation logic in the test.

At minimum it will assert:

1. the 24/0/6 content contract and exact reviewed warnings;
2. four-age-band catalog coverage;
3. cross-age deterministic adaptive recommendation properties;
4. resume precedence invariants;
5. authored teaching-mode/Trace/Help constraints across release content;
6. journey/prerequisite integrity;
7. adaptive-state privacy/bounds/fallback properties;
8. Gallery/Coloring/Free Draw core regression through existing dedicated suites.

Existing focused suites remain authoritative for engine details; P5.8 does not rewrite them.

## 7. Final candidate identity rule

Do **not** change the executable identity while the pre-freeze gate is being built.

After an exact P5.8 pre-freeze head is GREEN:

- set versionName exactly to `0.5.0-curriculum-expansion`;
- choose the next monotonic versionCode greater than 26 (normally **27** if no changed binary has already been distributed);
- update CI milestone/evidence artifact names from P5.7 QA labels to final `0.5.0-curriculum-expansion` labels;
- produce debug and release-like profile APKs from the exact same executable commit;
- record artifact IDs, byte sizes and SHA256 hashes.

If a materially changed APK is distributed after that candidate, increment versionCode again. Never reuse a distributed versionCode for changed executable bytes.

Documentation-only acceptance commits after the tested executable are allowed, but evidence must distinguish:

- exact physical-tested executable commit;
- accepted documentation head;
- squash merge commit;
- corresponding CI runs.

## 8. CI requirements

The final-candidate CI must pass:

- committed JSON parsing;
- Drawing Engine Ink-boundary verification;
- all JVM/unit tests including the P5.8 integrated release gate;
- lint;
- debug APK assembly;
- instrumentation APK assembly;
- profile APK assembly;
- P5.2 content-quality report with the frozen 24/0/6 result;
- milestone permission allowlist;
- final APK evidence packaging/upload.

No CI warning or skipped release assertion may be represented as a pass without explicit evidence.

## 9. Focused physical acceptance

The authoritative physical matrix is `docs/10-execution/P5_8_FINAL_QA.md`.

Physical acceptance targets high-value integrated behavior, not manual repetition of every unit assertion. It must include:

- fresh install/profile/Studio;
- all four age bands;
- representative lessons covering Draw With Me, Watch Then Draw, authored Trace, Help Ladder/Replay, prepared coloring and open creative work;
- deterministic/explainable Home behavior and browse/journey access;
- coloring resume → drawing resume → fresh precedence;
- child-invoked adaptive Help with no automatic escalation/invented Trace;
- Save & Leave/background/relaunch recovery;
- Gallery, Coloring and Free Draw smoke/regression;
- Airplane Mode core journey;
- accessibility/reachability spot checks;
- no crash/ANR/deadlock/unrecoverable blank state.

The exact final release-like profile APK must be the binary physically accepted.

## 10. Release evidence

Before merge, `P5_8_FINAL_QA.md` and `P5_8_RELEASE_REPORT.md` must record:

- final versionName/versionCode;
- exact physical-tested executable commit;
- exact accepted documentation head;
- final squash merge commit once known;
- pre-freeze, final-candidate, acceptance-doc and merged-main CI run numbers/IDs as applicable;
- debug/profile artifact IDs/names;
- debug/profile APK byte sizes and SHA256 hashes;
- tester-reported device/model/API if supplied, otherwise explicitly `not provided`;
- physical matrix PASS/FAIL/NOT RUN/real exception rows;
- defects found/fixed/retested;
- content quality 24/0/6 evidence;
- permission allowlist result;
- implementation/release summary.

Never infer missing device information.

## 11. Delivery batches

### Batch A — Contract + integrated gate

Deliver:
- this execution contract;
- final QA matrix scaffold;
- corrected project status for P5.7 completion / P5.8 activation;
- integrated P5.8 automated release-gate coverage.

Require exact-head CI GREEN before release freeze.

### Batch B — Final release freeze

Only after Batch A is green:
- set final versionName;
- reserve monotonic versionCode >26;
- rename/package final milestone artifacts;
- strengthen CI assertions to require the exact 24/0/6 content contract;
- produce exact debug/profile evidence.

Require exact-head final-candidate CI GREEN.

### Batch C — Exact binary evidence + physical acceptance

Deliver:
- download/hash/verify final profile artifact independently where tooling permits;
- execute focused physical matrix on exact final profile APK;
- fix only release blockers; any executable fix requires a new versionCode/evidence;
- record acceptance.

### Batch D — Acceptance + merge closure

Deliver:
- release report;
- acceptance documentation head CI GREEN;
- mark PR ready;
- squash merge exact accepted head;
- merged-main CI GREEN;
- close issue #88 and parent Phase-5 epic #73;
- update `PROJECT_STATUS.md` / roadmap to verified `0.5.0-curriculum-expansion` release;
- deliver exact final installable APK.

## 12. Exit sequence

1. Create P5.8 branch from verified P5.7 main.
2. Lock this contract + physical matrix before release implementation.
3. Add/strengthen integrated automated P5.8 release gate.
4. Require pre-freeze exact-head CI GREEN.
5. Set final `0.5.0-curriculum-expansion` identity with new versionCode >26 and final CI artifact names.
6. Require exact final-candidate CI GREEN.
7. Capture immutable APK IDs/sizes/SHA256.
8. Physically test the exact final release-like profile APK.
9. Fix/rebuild monotonically if any release blocker exists.
10. Commit physical acceptance + release report.
11. Require acceptance-doc exact-head CI GREEN.
12. Squash merge accepted head.
13. Require merged-main CI GREEN.
14. Close #88 and #73 only when evidence supports closure.
15. Deliver the exact final APK.

## 13. Definition of done

P5.8 and Phase 5 are complete only when the fully integrated `0.5.0-curriculum-expansion` product is automated-green across the frozen 24-lesson/four-age-band contract, the exact final profile APK is physically accepted, immutable Git/CI/APK evidence is recorded, the accepted branch is squash-merged, merged-main CI is green, and the final installable APK is delivered.
