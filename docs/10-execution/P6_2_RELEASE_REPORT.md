# P6.2 Release Report — Parent Zone Foundation

Status: **PHYSICAL ACCEPTED — 30/30 PASS / REPOSITORY CLOSURE ACTIVE**  
Issue: #94  
Parent epic: #91  
PR: #95

## Scope delivered

P6.2 implements the first production Parent Zone foundation over the accepted single-profile Phase-5 product without reopening the accepted art/teaching engines.

Delivered:
- deterministic, testable Adult Intent Gate/session policy;
- 2.5-second primary hold gate;
- accessible two-confirmation fallback;
- memory-only adult session with 5-minute expiry;
- >30-second background invalidation plus child-return/external invalidation hooks;
- protected product routing from child Home into Parent Zone;
- Parent Zone shell: Family, Learning, Accessibility & Audio, Storage & Data, Safety & Privacy, About;
- transaction-style editing of the one existing local `ChildProfile`;
- atomic save through the existing Phase-5 `ChildProfileStore`;
- truthful offline/privacy/version surfaces;
- baseline Compose accessibility/reflow/scroll behavior;
- focused JVM + Android instrumentation coverage;
- no multi-profile/profile-ID migration;
- no new Android permission;
- no cloud account/sync, advertising, behavioral analytics upload, payments or public sharing.

## Dependency closure

P6.1 is complete:
- issue #92: CLOSED / COMPLETED;
- PR #93: squash-merged;
- P6.1 merge commit: `9e1323b3643106aee3e50134f1b971e8086998ed`;
- merged-main Android CI #591: GREEN.

P6.2 implementation/hardening baseline:
- head `8b836757fc6de9451848ea2fb937813c62c18ffe`;
- Android CI #595 / run `34938060413`: GREEN.

## Immutable physically accepted QA1 executable

- source head: `528467acdcfde9c4d6ea01959d57157376cb081d`
- commit message: `P6.2 cut v28 QA1 candidate`
- versionName: `0.6.0-family-readiness-p6.2-qa1`
- versionCode: **28**
- Android CI #596 / run `34940587740`: **GREEN**
- PR workflow SHA: `e523999099fd1ab190a65921d122f26eb5c81672`

CI #596 passed committed JSON parsing, Drawing Engine Ink boundary, unit tests, lint, debug/instrumentation/profile APK compile, frozen curriculum quality (**24 / 0 / 6**), permission allowlist, exact package/version identity and immutable APK evidence packaging/upload.

## Artifacts

### Release-like profile — physically accepted binary

- artifact ID: **10385266255**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.2-qa1-profile`
- archive digest: `sha256:11f77fb9f8225f0c01671abd2b112cf819d6aee49c2c3b89f5c2f7c5f82ee3fa`
- APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.2_QA1-profile.apk`
- APK size: **16,377,441 bytes**
- APK SHA256: `b438d69ef7cabd7e023c963602e08c887b73074e219df2d03355a308d4849402`

### Debug

- artifact ID: **10384299888**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.2-qa1-debug`
- archive digest: `sha256:e73ccac77afc36387f5db4447ad4f63ef560de1492e9fa014ecce712263cabe0`
- APK size: **20,642,199 bytes**
- APK SHA256: `7c9a53ef5b3510a81965d3ec0443600e10c1f83ee5c41316797f3fcb9ac26e2d`

### Content quality

- artifact ID: **10384499464**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.2-qa1-content-quality`
- archive digest: `sha256:05e88209faf89861c5df40f594ae19b1086acd7d24956e6a817875ab7df4416a`

The profile/debug APK SHA256 values were independently recomputed after downloading the workflow artifacts and matched the packaged `SHA256SUMS.txt` exactly.

## Testing truth

JVM unit suites run in CI and pass. Android instrumentation sources for Parent Gate Compose and existing `ChildProfileStore` compatibility compile into the instrumentation APK. Runtime behavior was then tested physically on the exact profile candidate.

Physical matrix: `docs/10-execution/P6_2_FINAL_QA.md`.

Physical result confirmed by the user on 2026-09-15:
- **30 / 30 PASS**
- **0 / 30 FAIL**
- **0 / 30 NOT RUN**
- device/model/API: **not provided and not inferred**
- release-blocking defect reported: **none**

## Release decision

**ACCEPTED FOR P6.2 REPOSITORY CLOSURE.**

The exact physically accepted executable remains artifact **10385266255** built from source head `528467ac...`. Documentation and merge commits after that executable head do not replace the tested binary.

Closure sequence:
1. acceptance-documentation CI GREEN;
2. mark PR #95 ready;
3. squash-merge PR #95;
4. merged-main Android CI GREEN;
5. close #94 completed;
6. begin P6.3 from the verified merged-main baseline.

If executable behavior changes later, versionCode 28 must not be reused; a new monotonic versionCode and exact-binary QA cycle are required.
