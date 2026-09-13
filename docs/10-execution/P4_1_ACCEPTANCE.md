# P4.1 Acceptance Checklist

Issue #58 closes only when every item below is true.

- [ ] Three valid packages coexist through one catalog API in deterministic tests.
- [ ] Invalid package isolation is tested.
- [ ] Duplicate release lesson IDs are rejected deterministically.
- [ ] Queries by ID, age, category, skill, difficulty, journey and mode are tested.
- [ ] Missing declared assets are typed failures.
- [ ] Missing authored default-localization keys are typed failures.
- [ ] Draft/review packages are not child-facing catalog entries.
- [ ] Missing prerequisite lesson references are rejected.
- [ ] Studio Home loads its Phase 3 lesson through catalog projection.
- [ ] Product Lesson Runtime loads its default lesson through the catalog.
- [ ] Existing Cute Cat production package remains valid.
- [ ] No new network/account/analytics/sensitive permission.
- [ ] Exact final PR-head Android CI green.
- [ ] Review threads clean.
- [ ] PR merged before issue closure.