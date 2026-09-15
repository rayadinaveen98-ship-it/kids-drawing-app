# P6.3 Release Report — Parent Progress & Curriculum Visibility

Status: **PHYSICAL ACCEPTED — REPOSITORY CLOSURE ACTIVE**  
Issue: #96  
Parent epic: #91  
PR: #97  
Target milestone: `0.6.0-family-readiness`

## Scope delivered

P6.3 replaces the P6.2 Learning placeholder with a real protected parent-facing progress/curriculum surface derived only from accepted local product truth.

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

- clean P6.3 baseline: `d518bd8fca3d45af6b33604e9f87f13798826142`, Android CI #605 GREEN;
- contract/audit head: `cc2d9fde0678b86f6b808523b507912e6276340c`;
- contract audit: **64/64 PASS**;
- contract Android CI #606: **GREEN**;
- read-model implementation head: `0da51e9016dbf57a421e2f0df472896d000d4982`, CI #607 GREEN;
- fully wired Parent Learning head: `21a0a67a39273b4de4f5d392816ba4a722e66116`, CI #608 GREEN;
- route-hardening head: `f0f9f17cb11516a5660437289df37ef45aaae542`, CI #609 GREEN.

## Immutable physically accepted QA1 executable

- source head: `f155abc894d21b8cc09112a018fdf53ae25e4447`;
- versionName: `0.6.0-family-readiness-p6.3-qa1`;
- versionCode: **29**;
- Android CI #610 / run `34948654974`: **GREEN**;
- PR workflow SHA packaged by CI: `5fa02aa6b0655ec75624c6c1d47ebca7dbe27b0a`;
- frozen curriculum gate: **24 lessons / 0 errors / exactly 6 reviewed warnings**;
- Android permission allowlist: **GREEN**;
- exact APK identity gate: **GREEN**.

### Profile artifact

- artifact ID: **10387978868**;
- artifact archive digest: `sha256:c45a5a3e032a1748dd8b764c98696d3edf58fb1c6edccbe14652bf4fb79aa5d1`;
- APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.3_QA1-profile.apk`;
- size: **16,393,832 bytes**;
- SHA256: `93ccc4cbbf3be6b1d89e9d0094810dcf02a0654a410650752a3b8e47bf7d5b54`.

### Debug artifact

- artifact ID: **10388636830**;
- artifact archive digest: `sha256:a4de13445e372753121c3dfa735229b3f9e6b47c386138e28a2a788e2058a9a4`;
- APK size: **20,674,972 bytes**;
- APK SHA256: `a5fc5ce589b101b47e3e5885e7dd35be777595282a1cbb6d03def64e9d098c2e`.

### Content-quality artifact

- artifact ID: **10388931000**;
- artifact archive digest: `sha256:5dd7118a8eee6bf84acbfc5df8138d758467874ee54d7bd99e7c9f1d152ee556`.

Both profile and debug artifact ZIP digests were recomputed from downloaded CI archives and matched GitHub metadata. Both APK hashes and sizes were independently recomputed and matched `SHA256SUMS.txt` / `APK_SIZES.txt` packaged by CI.

## Physical QA status

Authoritative physical matrix: `docs/10-execution/P6_3_FINAL_QA.md`.

Accepted result on the exact profile APK above:
- physical PASS: **24/24**;
- physical FAIL: **0/24**;
- physical NOT RUN: **0/24**;
- acceptance date: **2026-09-15**;
- tester device/API: **not provided and not inferred**;
- release blockers: **none reported**.

The user confirmed all 24 focused checks passed, including upgrade preservation, protected Parent Learning access, truthful completion/artwork/in-progress separation, no fabricated completion dates, descriptive/non-judgmental language, Airplane Mode, large-text/small-screen handling and child-art regression smoke.

## Repository closure decision

**P6.3 is physically accepted and eligible for repository closure.**

Required remaining closure sequence:
1. require acceptance-documentation CI GREEN on the final documentation head;
2. mark PR #97 ready;
3. squash-merge using the exact accepted PR head;
4. require merged-main Android CI GREEN;
5. close #96 completed;
6. activate P6.4 only from that verified merged-main baseline.

Documentation and merge commits do not replace the exact physically accepted executable built from `f155abc...`. Any later executable modification requires a new monotonic versionCode and fresh QA binary.