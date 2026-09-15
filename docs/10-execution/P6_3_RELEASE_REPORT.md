# P6.3 Release Report — Parent Progress & Curriculum Visibility

Status: **COMPLETE / CLOSED**  
Issue: #96 — CLOSED / COMPLETED  
Parent epic: #91  
PR: #97 — SQUASH-MERGED  
Target milestone: `0.6.0-family-readiness`

## Scope delivered

P6.3 replaced the P6.2 Learning placeholder with a real protected parent-facing progress/curriculum surface derived only from accepted local product truth.

Delivered implementation:
- additive typed read-only adaptive-state load outcomes without changing recommendation fallback behavior;
- deterministic `ParentProgressProjection` over local completion, catalog, Gallery and resume state;
- local Android repository adapter joining those sources without a new analytics/history database;
- protected Parent Zone → Learning production Compose surface;
- descriptive completion/artwork summary;
- recent completion ordering without fabricated completion dates;
- curriculum areas explored and authored skills practiced;
- journey completed-of-total context plus next prerequisite-eligible step where available;
- real timestamped saved-artwork activity with Lesson vs Free Draw provenance;
- drawing/coloring in-progress context kept separate from completed history;
- partial/unavailable-source notices that do not mutate or erase underlying data;
- local-only/non-grading explanatory copy;
- focused JVM projection/read-path tests and Parent Learning Compose route coverage;
- no new Android permission, account, network dependency, cloud sync or behavioral analytics upload.

Explicitly excluded:
- grades/scores/ranks/leaderboards;
- mastery percentages;
- XP or punitive streaks;
- permanent ability/talent labels;
- ahead/behind or sibling/peer comparisons;
- parent-facing raw Help-request counts;
- stroke/artwork quality scoring;
- new persistent analytics/history store;
- multi-profile migration;
- P6.4 accessibility-system work;
- P6.5 device/performance hardening;
- P6.6 destructive family-data controls.

## Contract and stabilization evidence

- clean P6.3 baseline `d518bd8fca3d45af6b33604e9f87f13798826142`, CI #605 GREEN;
- contract/audit head `cc2d9fde0678b86f6b808523b507912e6276340c`;
- contract audit **64/64 PASS**;
- contract CI #606 GREEN;
- read-model implementation `0da51e9016dbf57a421e2f0df472896d000d4982`, CI #607 GREEN;
- wired Parent Learning `21a0a67a39273b4de4f5d392816ba4a722e66116`, CI #608 GREEN;
- route hardening `f0f9f17cb11516a5660437289df37ef45aaae542`, CI #609 GREEN.

## Immutable physically accepted QA1 executable

- source head `f155abc894d21b8cc09112a018fdf53ae25e4447`;
- versionName `0.6.0-family-readiness-p6.3-qa1`;
- versionCode **29**;
- candidate CI #610 / run `34948654974` GREEN;
- frozen curriculum **24 lessons / 0 errors / exactly 6 reviewed warnings**;
- Android permission allowlist GREEN;
- exact APK identity GREEN.

### Profile artifact
- artifact ID **10387978868**;
- archive digest `sha256:c45a5a3e032a1748dd8b764c98696d3edf58fb1c6edccbe14652bf4fb79aa5d1`;
- APK size **16,393,832 bytes**;
- SHA256 `93ccc4cbbf3be6b1d89e9d0094810dcf02a0654a410650752a3b8e47bf7d5b54`.

### Debug artifact
- artifact ID **10388636830**;
- archive digest `sha256:a4de13445e372753121c3dfa735229b3f9e6b47c386138e28a2a788e2058a9a4`;
- APK size **20,674,972 bytes**;
- SHA256 `a5fc5ce589b101b47e3e5885e7dd35be777595282a1cbb6d03def64e9d098c2e`.

### Content-quality artifact
- artifact ID **10388931000**;
- archive digest `sha256:5dd7118a8eee6bf84acbfc5df8138d758467874ee54d7bd99e7c9f1d152ee556`.

Artifact archive digests and APK hashes/sizes were independently recomputed and matched CI evidence.

## Physical acceptance

Authoritative matrix: `docs/10-execution/P6_3_FINAL_QA.md`.

- PASS **24/24**;
- FAIL **0/24**;
- NOT RUN **0/24**;
- acceptance date **2026-09-15**;
- tester device/API **not provided and not inferred**;
- reported blockers **none**.

## Repository closure evidence

- acceptance-documentation head `ab8df4d626136dea20c9fb3b00de203a527c455c`;
- acceptance-documentation CI #616 / run `34953626162` GREEN;
- PR #97 squash merge `fc81703b57419d14c9baf50a0fb4fb91554652f0`;
- merged-main Android CI #617 / run `34954128416` **GREEN**;
- issue #96 **CLOSED / COMPLETED**.

## Final decision

**P6.3 is COMPLETE.** P6.4 Accessibility System V2 is the active Phase-6 slice under issue #98.

The exact physically accepted P6.3 executable remains the profile APK built from `f155abc...` / artifact `10387978868`. Documentation, merge and later Phase-6 commits do not replace that tested binary. Any executable modification to the accepted P6.3 candidate would require a new monotonic versionCode and a fresh QA cycle.