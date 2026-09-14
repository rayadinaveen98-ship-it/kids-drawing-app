# P5.8 Execution Contract — Cross-age Curriculum QA + 0.5 Release

**Parent epic:** #73  
**Issue:** #88  
**Verified starting main:** `e4fb0ddd2053f6583b7c1457ee65e034d43707a8`  
**Verified starting merged-main CI:** Android CI #571 / run `34869966116` — **GREEN**  
**Branch:** `phase5/p5-8-final-release`  
**Target final milestone:** `0.5.0-curriculum-expansion`  
**Reserved final release versionCode:** **27**  
**Status:** **LOCKED BEFORE P5.8 IMPLEMENTATION/RELEASE CHANGES**

## 1. Purpose

P5.8 is the final Phase-5 integration, release-validation and acceptance gate. It does not add a parallel lesson engine, a new curriculum model or a new intelligence layer. It validates the complete accepted product built through P5.1–P5.7, fixes only concrete release-blocking defects, freezes the final `0.5.0-curriculum-expansion` binary, and closes Phase 5 only after exact automated + physical evidence is green.

## 2. Frozen integrated product baseline

P5.8 starts from merged P5.7 main and preserves:

- **24 production lessons**;
- four age bands: Little Artist, Creative Explorer, Growing Artist and Young Artist;
- Draw With Me, Watch Then Draw and authored Trace & Learn behavior;
- Help Ladder, replay and generic Companion behavior;
- journeys, prerequisites and full catalog discovery;
- coloring resume → drawing resume → fresh Home precedence;
- P5.7 local bounded deterministic adaptive recommendations and child-controlled adaptive Help;
- Drawing, Coloring, Gallery and Free Draw ownership boundaries;
- local/offline core behavior;
- no cloud child profile, analytics upload, artwork-quality scoring, hidden ability label, rank/grade/XP or network requirement.

The latest accepted P5.7 QA profile binary remains historical evidence only. P5.8 will produce a new final-release binary.

## 3. Content-quality contract

Unless a concrete reviewed content defect requires an intentional change, final Phase-5 content quality must remain:

- lesson count: **24**;
- error count: **0**;
- warning count: **6**;
- warning code: `NO_JOURNEY_MEMBERSHIP` only;
- reviewed warning lesson IDs exactly:
  - `ice-cream-shop`
  - `one-point-room`
  - `rainbow-weather`
  - `sailboat-scene`
  - `simple-car`
  - `tree-through-seasons`

Any unexpected warning/error or catalog count drift blocks final freeze.

## 4. Release validation matrix

Automated P5.8 gates must cover the integrated product, not only isolated policies.

### 4.1 Curriculum / age coverage

Prove:
- all 24 release lessons load through the strict production loader;
- all four age bands retain their accepted lesson coverage;
- final accepted coverage remains Little **8**, Creative **18**, Growing **17**, Young **10**;
- final difficulty distribution remains D1 **5**, D2 **9**, D3 **6**, D4 **3**, D5 **1**;
- all journey membership and prerequisite references resolve;
- the complete catalog remains discoverable even when prerequisite rules prevent an item from being the adaptive fresh primary.

### 4.2 Teaching modes / Help / Companion

Prove:
- Draw With Me remains executable generically;
- Watch Then Draw overview/child handoff remains executable generically;
- Trace remains available only where authored;
- open-authorship steps are never forced into Trace;
- Help remains explicitly child-invoked;
- adaptive Help may choose only authored Help or authored Replay;
- Replay cannot loop indefinitely;
- Companion remains read-only relative to artwork/session/persistence truth;
- teacher/help/reference overlays remain isolated from child artwork.

### 4.3 Home / progression / adaptation

Prove:
- coloring resume > drawing resume > fresh remains frozen;
- identical local state gives identical fresh recommendation ordering/reason copy;
- prerequisites cannot be bypassed by adaptation;
- journey continuation/new-skill/interest/preferred-mode/difficulty/repeat policy remains deterministic;
- fresh eligible work outranks completed repeat fallback;
- browse/category/journey views remain transparent/full-catalog;
- missing/corrupt/incompatible adaptive state safely falls back to the verified baseline behavior;
- adaptive state reset path remains deterministic and does not leak prior-profile history.

### 4.4 Lifecycle / recovery

Prove:
- Save & Leave restores the exact owned lesson/document state safely;
- process/activity recreation does not corrupt child artwork or teaching state;
- active coloring resume remains higher priority than drawing resume;
- recovery never fabricates completion/adaptive events;
- completion/help event idempotence survives repeated delivery/relaunch.

### 4.5 Gallery / Coloring / Free Draw

Prove:
- successful guided completion still reaches Gallery correctly;
- Coloring starts/resumes without contaminating drawing ownership;
- Free Draw remains lesson-independent and has explicit Gallery provenance;
- adaptive persistence failure cannot cause Gallery save failure;
- no teacher/help/reference overlay is persisted into child artwork.

### 4.6 Offline / permission / privacy

Prove:
- core app flows work in Airplane Mode;
- no new network client/dependency is required for core use;
- Android permission allowlist does not expand beyond the accepted app-owned dynamic receiver permission;
- persisted adaptive state contains no stroke coordinates, artwork payloads, audio/video, free-form child text, device/advertising IDs, score/rank/grade/ability labels or unbounded behavioral event log.

## 5. P5.8 defect policy

P5.8 is validation-first.

Allowed code/content changes are only:
- concrete defects exposed by integrated automated or physical QA;
- release metadata/workflow/evidence changes required to cut the final milestone;
- test coverage needed to prove the final integrated contract.

Do not perform speculative refactors, visual redesigns, new curriculum expansion or unrelated features during P5.8.

Any binary/content-changing fix after the final release candidate is frozen invalidates that candidate and requires a versionCode greater than the frozen one plus new evidence and physical acceptance.

## 6. Pre-freeze gate

Before versionCode 27 is spent:

- P5.8 contract/docs checkpoint CI must be GREEN;
- add/run the integrated cross-age Phase-5 regression gate;
- preserve content quality at 24 / 0 / 6 reviewed warnings;
- permission allowlist must pass;
- no release-blocking automated defect may remain.

VersionCode **27** is reserved for the first final `0.5.0-curriculum-expansion` release candidate and must not be used for intermediate contract/test-only checkpoints.

## 7. Final release freeze

When the pre-freeze gate is green, cut exactly:

- versionName: **`0.5.0-curriculum-expansion`**;
- versionCode: **27**;
- P5.8/final-release-specific CI artifact names;
- exact debug/profile release evidence;
- final acceptance checklist/evidence document.

The profile build remains the release-like physically accepted APK unless the repository deliberately adds a separately signed production-release pipeline. Do not pretend debug or unsigned binaries are store production signatures.

## 8. Final physical acceptance scope

The exact frozen profile APK must physically prove at minimum:

- install/startup and onboarding/profile path;
- all four age bands receive sensible Home presentation;
- all 24 lessons remain discoverable;
- representative lessons across every age band and teaching mode launch correctly;
- journey/prerequisite behavior is coherent;
- resume precedence remains coloring > drawing > fresh;
- adaptive fresh suggestions are stable/explainable;
- Help stays child-initiated/authored and Trace is never invented;
- Save & Leave/relaunch/recovery works;
- Gallery, Coloring and Free Draw smoke pass;
- Airplane Mode works end-to-end;
- no crash, data-loss, unexpected permission or network requirement appears.

The focused matrix must be committed before acceptance. Tester device/API must be recorded if provided; otherwise explicitly record `Not provided by tester` rather than infer it.

## 9. Final evidence contract

Final release evidence must record:

- exact release commit SHA;
- exact CI run number + run ID;
- exact content-quality artifact ID;
- debug artifact ID, APK size and SHA256;
- profile artifact ID, APK size and SHA256;
- final versionName/versionCode;
- content-quality summary;
- permission result;
- tester-reported physical result and device/API metadata;
- any reviewed exceptions/warnings.

Documentation-only acceptance recording may follow the frozen binary without invalidating it.

## 10. Phase-5 closure

P5.8 is complete only after:

1. final release candidate automated CI GREEN;
2. exact final profile APK physically accepted;
3. acceptance evidence committed;
4. acceptance-doc exact-head CI GREEN;
5. P5.8 PR marked ready and squash-merged;
6. merged-main Android CI GREEN;
7. issue #88 closed completed;
8. Phase-5 parent epic/status/roadmap updated to COMPLETE;
9. `0.5.0-curriculum-expansion` becomes the latest fully verified product milestone.

No Phase-6 work starts before merged-main P5.8 is green.