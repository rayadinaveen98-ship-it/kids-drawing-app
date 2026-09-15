# P6.3 Acceptance Checklist — Parent Progress & Curriculum Visibility

Status: **CONTRACT AUDIT — 64/64 SPEC CHECKS PASS**  
Issue: #96  
Baseline: `d518bd8fca3d45af6b33604e9f87f13798826142` / Android CI #605 GREEN

This checklist validates the P6.3 contract itself before production implementation begins. Runtime/physical rows will be created after implementation and a monotonic QA APK exist.

## A. Truth-source boundary — 10/10

- [x] **A01** Completion truth is sourced from local adaptive `completedLessons`, not screen visits.
- [x] **A02** Recent completion ordering is sourced from adaptive `recentCompletions`.
- [x] **A03** Contract records that adaptive completion is emitted only after successful product save/completion boundary.
- [x] **A04** Contract records that adaptive completion has no wall-clock timestamp.
- [x] **A05** Timestamped saved-artwork activity is sourced only from Gallery `completedAtEpochMillis`.
- [x] **A06** Lesson/Free Draw Gallery provenance remains distinct.
- [x] **A07** Curriculum meaning comes from authored lesson metadata.
- [x] **A08** Drawing/coloring snapshots are explicitly active/resume truth, not historical completion truth.
- [x] **A09** Prerequisite eligibility reuses accepted completion semantics rather than a second progression engine.
- [x] **A10** P6.3 V1 adds no new persistent analytics/history database.

## B. Read-model semantics — 12/12

- [x] **A11** Read model distinguishes loaded, genuinely empty/missing and unavailable/corrupt sources.
- [x] **A12** Current-catalog completed count requires resolvable current lesson identity/revision.
- [x] **A13** Unresolved older completion identities are preserved without invented metadata.
- [x] **A14** Repeated recent completion cannot inflate unique completed count.
- [x] **A15** Recent completion order remains deterministic.
- [x] **A16** Skills are derived from authored skills on resolved completed lessons.
- [x] **A17** Categories are derived from authored categories on resolved completed lessons.
- [x] **A18** Journey completion counts use current authored journey membership.
- [x] **A19** Journey next-step eligibility uses accepted prerequisite completion semantics.
- [x] **A20** Gallery saved-artwork timestamp remains a saved-artwork timestamp and is not copied onto unrelated completion history.
- [x] **A21** Active drawing/coloring never increases completed count.
- [x] **A22** Same inputs must produce identical Parent Progress model and ordering.

## C. Parent Learning surface — 14/14

- [x] **A23** P6.2 Learning placeholder is replaced only inside the protected Parent Zone.
- [x] **A24** Surface includes a clear Learning heading/context.
- [x] **A25** Surface includes calm descriptive summary counts.
- [x] **A26** Surface includes recent completed lessons where available.
- [x] **A27** Recent completion UI does not fabricate dates when none exist.
- [x] **A28** Surface includes categories explored where supported.
- [x] **A29** Surface includes authored skills practiced/exposed where supported.
- [x] **A30** Surface includes journey lesson/completed counts where supported.
- [x] **A31** Surface includes next prerequisite-eligible journey step where supported.
- [x] **A32** Journey-complete state is supported without mastery language.
- [x] **A33** Surface includes recent saved-artwork activity with real timestamp where Gallery is available.
- [x] **A34** Lesson artwork and Free Draw activity are visually/textually distinguishable.
- [x] **A35** Surface includes clearly labelled active drawing/coloring work where present.
- [x] **A36** Surface includes local-only/non-grading privacy explanation and useful empty states.

## D. Child-safety / language boundary — 10/10

- [x] **A37** No grades.
- [x] **A38** No scores/ranks/leaderboards.
- [x] **A39** No mastery percentages.
- [x] **A40** No XP, punitive streaks or inactivity pressure.
- [x] **A41** No permanent ability/talent labels.
- [x] **A42** No age-based “ahead/behind” judgment.
- [x] **A43** No sibling/peer comparison.
- [x] **A44** No emotion, diagnosis, IQ/cognition or behavior inference.
- [x] **A45** Raw Help-request counts are explicitly excluded from parent-facing performance/progress UI.
- [x] **A46** No raw stroke/artwork quality metrics or hidden engagement score.

## E. Robustness, privacy and accessibility — 8/8

- [x] **A47** Adaptive missing and adaptive unavailable are distinguishable for the parent read model without changing recommendation fallback behavior.
- [x] **A48** Gallery Ready/Empty/Unavailable states remain independently usable.
- [x] **A49** One unavailable source cannot erase safe information from another source.
- [x] **A50** Parent Progress exposes no mutator for artwork/session/adaptive completion truth.
- [x] **A51** No raw artwork/strokes are copied into Parent Progress state.
- [x] **A52** No account, cloud sync, analytics upload, network dependency or new Android permission is introduced.
- [x] **A53** Large text / small screen / scrolling / semantic control expectations are explicit.
- [x] **A54** Completion/state information is available as text and not encoded only by color/chart position.

## F. Regression and release discipline — 10/10

- [x] **A55** Existing Parent Gate/session contract remains unchanged.
- [x] **A56** 0.6 remains single-profile; no profile-ID/multi-child migration in P6.3.
- [x] **A57** Drawing/Lesson/Coloring/Gallery/adaptive mutation semantics remain frozen unless a defect forces a contract amendment.
- [x] **A58** Existing unit/lint/debug/instrumentation/profile build gate must remain green.
- [x] **A59** Frozen curriculum quality gate remains 24 lessons / 0 errors / exactly 6 reviewed warnings unless separately approved content work changes it.
- [x] **A60** Android permission allowlist remains green.
- [x] **A61** Contract/docs work alone does not bump versionCode.
- [x] **A62** Production P6.3 code must be automated-green before reserving a monotonic versionCode >28.
- [x] **A63** Exact debug/profile APK evidence + SHA256 + physical QA are required before merge.
- [x] **A64** P6.3 closes only after acceptance-doc CI, squash merge and merged-main CI are green.

## Contract audit result

- Truth-source boundary: **10/10**
- Read-model semantics: **12/12**
- Parent Learning surface: **14/14**
- Safety/language boundary: **10/10**
- Robustness/privacy/accessibility: **8/8**
- Regression/release discipline: **10/10**
- **Total: 64/64 PASS**

## Implementation handoff

The first production implementation step after this contract is green is:
1. add typed read-only adaptive-state load semantics;
2. implement a pure Parent Progress projection with exhaustive unit tests;
3. implement the local Android repository adapter joining catalog/adaptive/Gallery/resume truth;
4. replace Parent Zone Learning placeholder with the production read-only Compose surface;
5. run full regression CI while keeping versionCode 28 until implementation stabilizes.
