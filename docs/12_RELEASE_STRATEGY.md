# 12 — Release & Version Strategy

**Status:** Phase 0.7 release contract

## 1. Milestone sequence

- `0.0.1-foundation` — Phase 0 product/architecture baseline
- `0.1.0-art-lab` — Drawing Engine / Art Lab 0.1
- `0.2.0-lesson-engine` — structured lesson engine proof
- `0.3.0-vertical-slice` — first complete child lesson journey
- `0.4.0-coloring-engine` — productionized guided/self coloring
- `0.5.0-companion-engine` — productionized companion integration
- `0.6.0-content-library` — content library/journeys
- `0.7.0-adaptive-learning` — recommendation/help adaptation
- `0.8.0-free-draw-studio` — age-progressive free studio
- `0.9.0-beta` — product beta / device hardening
- `1.0.0` — public V1

SemVer remains the external versioning direction. Internal pre-release qualifiers may add `-alpha.N`, `-rc.N`, or descriptive milestone suffixes where useful.

## 2. APK/AAB rule

Every meaningful Android development milestone should produce an installable APK when technically possible.

Internal milestones:
- debug/internal APK artifact required;
- build must come from a documented Git commit/tag;
- SHA-256 should be recorded for handoff/release evidence once release tooling is established.

Public Play releases:
- Android App Bundle (AAB) becomes the store artifact;
- APK can still be produced for internal/manual QA.

## 3. Phase 1 Art Lab release definition

Target version:

`0.1.0-art-lab`

Required release evidence:
- Git commit and tag;
- CI green for required checks;
- installable APK artifact;
- versionName/versionCode recorded;
- APK SHA-256 recorded;
- feature summary;
- test summary;
- physical-device performance result status;
- known issues linked to GitHub issues;
- no unresolved P0/P1 milestone-blocking defect.

## 4. Branch/release direction

Keep `main` as the authoritative integrated branch.

During active implementation:
- use focused feature branches where work is non-trivial;
- merge only after required checks/review;
- avoid long-lived divergent branches;
- milestone tags point to known-good commits.

The exact branching model may remain lightweight while the project is small; repository history and reproducibility matter more than ceremonial branch complexity.

## 5. VersionCode

Android `versionCode` is monotonically increasing and never reused for a published/distributed milestone that may need comparison/update testing.

Before first Android scaffold, choose a simple sequence and record it in project status/release notes.

## 6. Internal vs public quality

An internal milestone can ship with documented P2/P3 limitations if the milestone objective is still valid.

It cannot ship as complete with:
- known artwork/data corruption;
- persistent core-flow crash;
- child-safety/privacy bypass;
- falsely reported test evidence;
- missing required APK when build infrastructure is capable of producing it.

## 7. Public V1 release gate

`1.0.0` requires substantially more than engine milestone completion:
- then-current Google Play policy review;
- Families target-audience compliance;
- targetSdk requirement current at submission time;
- Data safety/privacy policy/store declarations;
- release signing and reproducible build procedure;
- complete permission/SDK inventory;
- billing review if monetized;
- accessibility/device QA;
- content/license provenance;
- parent-gate usability validation;
- product stability evidence;
- no unresolved launch-blocking P0/P1 defects.

## 8. Release notes format

Each milestone release notes should include:

```text
Version
VersionCode
Git commit/tag
Artifact(s)
Artifact SHA-256
What changed
What is proven
Tests/quality evidence
Known issues
Next milestone
```

## 9. Handoff rule

`PROJECT_STATUS.md` and `docs/HANDOFF.md` must point to the latest stable milestone and current active work before a major chat/development handoff.