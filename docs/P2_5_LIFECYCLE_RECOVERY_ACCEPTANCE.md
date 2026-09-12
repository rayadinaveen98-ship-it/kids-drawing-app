# P2.5 Acceptance Ledger

Issue: #33

Status: ACTIVE

The slice is complete only when all items below are backed by green exact-head CI and the resulting PR is merged.

- [ ] atomic lesson-session store with primary/backup recovery
- [ ] checksum/version validation and typed corrupt/unavailable outcomes
- [ ] safe semantic snapshot autosave coordinator
- [ ] child document reload before lesson-session restore
- [ ] process-recreation orchestration across all three teaching modes
- [ ] stale teacher/guide cleanup on restore
- [ ] deterministic overview/teacher restart normalization
- [ ] trace/help overlay recreation from authored content only
- [ ] lesson revision incompatibility handling without child-artwork loss
- [ ] teacher playback recoverable failure/retry contract
- [ ] drawing-complete/post-drawing lifecycle handoff
- [ ] lifecycle/recovery unit tests for Draw With Me, Watch Then Draw, Trace & Learn
- [ ] corruption/backup/revision mismatch tests
- [ ] debug + instrumentation + profile APK compile
- [ ] Android lint + Ink boundary + permission allowlist green

Physical Lesson Lab verification remains P2.6 unless a P2.5 defect requires targeted device evidence earlier.
