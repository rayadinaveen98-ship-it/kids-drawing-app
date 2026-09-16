# P6.5 Device & Performance Hardening — QA1 Release Report

Status: **QA1 BUILT / EXACT ARTIFACT VERIFIED / PHYSICAL QA PENDING**  
Issue: #100  
Parent epic: #91  
PR: #102  
Target milestone: `0.6.0-family-readiness`

## Scope delivered

P6.5 hardens the accepted family-readiness product without redefining Drawing/Lesson/Coloring/Gallery/adaptive ownership or adding cloud/network/account dependencies.

Delivered implementation includes:
- pure compact/expanded width and constrained/regular height geometry policy;
- targeted responsive/reachability hardening rather than device-model branches;
- off-main-thread Gallery preview decode with non-authoritative placeholder behavior;
- Quality Lab coloring-heavy stress/raster evidence extensions;
- local product timing evidence for profile, Home, Gallery, lesson, coloring and Free Draw operations;
- lifecycle/recreation hardening while preserving ephemeral Parent authorization;
- coloring persistence failure handling that keeps in-memory work retryable for expected I/O failures;
- strict durable coloring completion boundary so failed persistence cannot be reported as Gallery success;
- complete versionCode-30 stabilization before the monotonic QA bump.

## Frozen contract evidence

- baseline: `c49434ab6acd32e8153164e46f86a58ec09bca0a` / Android CI #655 GREEN;
- contract head: `bc91432f670e29e33f19fecc8b100424fdd382b0`;
- contract CI: #656 / run `35072570970` GREEN;
- specification audit: **80/80 PASS**;
- implementation stabilization head: `881f98f92690e2d9330e59ce54077cce12f33565`;
- stabilization CI: #693 / run `35079771772` GREEN.

## Immutable QA1 executable

- executable source head: `cbe31b2b24c43fe3a06ed60e0f917a270f67449f`
- versionName: `0.6.0-family-readiness-p6.5-qa1`
- versionCode: **31**
- candidate CI: #695 / run `35080612216` — **GREEN**
- CI embedded workflow SHA: `b9fabed82e2dc3521ec20be22d331ac20060413f`

### Profile artifact

- artifact ID: **10439774674**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.5-qa1-profile`
- archive size metadata: **12,567,933 bytes**
- archive digest: `sha256:a8b582bab46c3fb9b7c2e4bb28906312a7e9e04cab2ffbda1f81d9da6259406f`
- APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.5_QA1-profile.apk`
- APK size: **16,459,363 bytes**
- APK SHA256: `da939a0057391497cf0eacde524ac14409215d06f7ba573ec68920430138a51e`

### Debug artifact

- artifact ID: **10440550942**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.5-qa1-debug`
- archive size metadata: **16,876,557 bytes**
- archive digest: `sha256:b8cc5283eda19ee6513f2c4d6b64bb25226f5079c62cd3db88ecacc7646b1b6e`
- APK size: **20,773,409 bytes**
- APK SHA256: `969879fb515fa923c9995e794cfe49052dec530bcc871fe48223245722f33336`

### Content-quality artifact

- artifact ID: **10440232384**
- artifact name: `kids-drawing-0.6.0-family-readiness-p6.5-qa1-content-quality`
- archive size metadata: **4,257 bytes**
- archive digest: `sha256:0e3683d18ca07f4acb482a851e6069f364c2741b3c9b5fa916d5e1767210e402`

## Independent artifact verification

After downloading the #695 artifacts:
- profile ZIP SHA256 independently recomputed to `a8b582...9406f`, exactly matching GitHub artifact digest;
- debug ZIP SHA256 independently recomputed to `b8cc528...b1b6e`, exactly matching GitHub artifact digest;
- embedded `RELEASE_IDENTITY.txt` in both artifacts reports versionName `0.6.0-family-readiness-p6.5-qa1`, versionCode 31, source head `cbe31b2...` and workflow SHA `b9fabed...`;
- embedded `SHA256SUMS.txt` reports the exact debug/profile hashes above;
- embedded `APK_SIZES.txt` reports the exact debug/profile byte sizes above;
- both APK SHA256 values and byte sizes were independently recomputed and matched the embedded evidence.

## Automated candidate gates

Candidate CI #695 passed:
- Android/JDK/SDK setup;
- committed JSON parsing;
- Drawing Engine Ink boundary verification;
- unit tests;
- lint;
- debug APK compile;
- instrumentation APK compile;
- profile APK compile;
- frozen curriculum quality contract: 24 lessons / 0 errors / exactly 6 reviewed warnings;
- P6.5 content-quality artifact upload;
- Android permission allowlist;
- exact P6.5 QA1 v31 APK identity;
- P6.5 evidence packaging;
- debug/profile artifact upload.

Instrumentation APK compilation is not claimed as runtime instrumentation execution.

## Physical and hardware status

Focused core physical matrix in `P6_5_FINAL_QA.md`:
- PASS: **0 / 30**
- FAIL: **0 / 30**
- NOT RUN: **30 / 30**
- tester device/model/API/RAM: **not provided and not inferred**
- core physical release decision: **PENDING**

Hardware-specific numeric performance/API/tablet rows remain **PENDING-HARDWARE** until actually executed on identified hardware. CI and an unidentified physical tester device must not be used to manufacture Class L/M/S, API-band, cold-start or numeric product-timing PASS claims.

## Merge decision

**DO NOT MERGE YET.**

PR #102 stays draft until the exact profile artifact **10439774674** completes focused physical QA and the acceptance-documentation CI is green. Any executable change after physical QA starts requires a new monotonic versionCode and fresh exact-binary QA. Docs-only commits do not replace the immutable executable `cbe31b2...`.
