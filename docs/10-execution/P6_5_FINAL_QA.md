# P6.5 Final QA — Device & Performance Hardening

Status: **PHYSICAL QA READY — 0/30 CORE PHYSICAL CHECKS RUN; HARDWARE-SPECIFIC NUMERIC GATES PENDING**  
Issue: #100  
Parent epic: #91  
PR: #102  
Target milestone: `0.6.0-family-readiness`

## Immutable QA1 candidate

- versionName: `0.6.0-family-readiness-p6.5-qa1`
- versionCode: **31**
- implementation stabilization head: `881f98f92690e2d9330e59ce54077cce12f33565`
- stabilization Android CI: #693 / run `35079771772` — **GREEN**
- executable source head: `cbe31b2b24c43fe3a06ed60e0f917a270f67449f`
- candidate Android CI: #695 / run `35080612216` — **GREEN**
- profile artifact ID: **10439774674**
- profile artifact archive digest: `sha256:a8b582bab46c3fb9b7c2e4bb28906312a7e9e04cab2ffbda1f81d9da6259406f`
- profile APK: `Kids_Drawing_0.6.0_Family_Readiness_P6.5_QA1-profile.apk`
- profile APK size: **16,459,363 bytes**
- profile APK SHA256: `da939a0057391497cf0eacde524ac14409215d06f7ba573ec68920430138a51e`
- debug artifact ID: **10440550942**
- debug artifact archive digest: `sha256:b8cc5283eda19ee6513f2c4d6b64bb25226f5079c62cd3db88ecacc7646b1b6e`
- debug APK size: **20,773,409 bytes**
- debug APK SHA256: `969879fb515fa923c9995e794cfe49052dec530bcc871fe48223245722f33336`
- content-quality artifact ID: **10440232384**
- content-quality artifact archive digest: `sha256:0e3683d18ca07f4acb482a851e6069f364c2741b3c9b5fa916d5e1767210e402`
- candidate workflow merge SHA recorded by CI: `b9fabed82e2dc3521ec20be22d331ac20060413f`

The profile APK above is the **only P6.5 QA1 physical target**. Later documentation commits do not replace it. Profile/debug ZIP digests, APK SHA256 values and byte sizes were independently recomputed after downloading the #695 artifacts and matched the CI evidence package.

## Automated acceptance evidence

- P6.5 frozen contract/checklist: **80/80 specification audit PASS**; contract CI #656 GREEN.
- Pure compact/expanded width and constrained/regular height geometry policy + boundary tests: PASS.
- Gallery preview decode moved off Compose/UI-thread synchronous decode path while placeholder/authoritative-artwork semantics remain separate: PASS.
- Targeted responsive hardening across constrained-height art controls and expanded-width content: PASS.
- Quality Lab coloring-heavy workloads, timing/raster evidence and existing drawing-performance infrastructure integration: PASS in automated source/build coverage; physical numeric hardware results are not inferred from CI.
- Product timing buckets cover profile resolution, Home load, Gallery list/reopen, lesson recovery/start, coloring recovery and Free Draw recovery using local monotonic timing only: PASS.
- Product timing wrapper tests cover suspend/blocking result preservation, sample recording and failure propagation: PASS.
- Coloring persistence hardening distinguishes routine recoverable I/O from strict durable completion: PASS.
- Routine coloring storage I/O failure keeps in-memory artwork open/retryable and exposes calm local retry state; unexpected non-I/O runtime failures are not swallowed: PASS.
- Gallery coloring completion uses a strict durable-save boundary, preventing false success on persistence failure: PASS.
- Full implementation stabilization on versionCode 30: `881f98f...`, Android CI #693 GREEN.
- QA1 full unit/lint/debug/instrumentation-compile/profile build matrix: Android CI #695 GREEN.
- Frozen curriculum quality: **24 lessons / 0 errors / exactly 6 reviewed warnings**.
- Android permission allowlist: PASS.
- Exact v31/P6.5 QA1 APK identity gate: PASS.
- Evidence packaging/upload: PASS.
- Android instrumentation source **COMPILES** in CI; CI does not run emulator/device instrumentation, so runtime instrumentation PASS is not claimed.
- No account/cloud/network dependency, behavioral analytics upload, new Android permission, device-model branch, ownership rewrite or speculative engine rewrite introduced.

## Focused core physical acceptance matrix

Run these checks on the exact profile artifact **10439774674**. Record the actual device/model/API/RAM only if known; never infer them.

| ID | Physical check | Status |
|---|---|---|
| P01 | Install/upgrade exact v31 profile APK and launch successfully | NOT RUN |
| P02 | Upgrade from accepted v30 preserves child profile, Gallery, progress and resumable work | NOT RUN |
| P03 | Fresh launch/recreated Activity never restores an already-unlocked Parent session | NOT RUN |
| P04 | Parent Zone route after recreation shows Adult Intent Gate until unlocked again | NOT RUN |
| P05 | Parent session still expires at five minutes and background >30s invalidates it | NOT RUN |
| P06 | Home loads and remains usable in Airplane Mode | NOT RUN |
| P07 | Home/primary navigation has no critical clipping or unreachable action in the available geometry | NOT RUN |
| P08 | Guided Lesson opens and committed drawing remains editable after background/foreground | NOT RUN |
| P09 | Guided Lesson recreation/reopen restores persistent session/artwork truth rather than guessed UI state | NOT RUN |
| P10 | Recreation does not convert in-progress lesson work into completion | NOT RUN |
| P11 | Repeated drawing undo/redo/save smoke preserves operation truth and remains responsive | NOT RUN |
| P12 | Coloring starts from a valid completed drawing and protected line art remains intact | NOT RUN |
| P13 | Coloring brush/erase/prepared fill smoke remains editable and responsive | NOT RUN |
| P14 | Coloring background/reopen restores the last known-good persisted state | NOT RUN |
| P15 | Coloring Save & leave returns only after a successful durable save | NOT RUN |
| P16 | If a storage-save failure is encountered, workspace remains open with calm retry copy and no false success | NOT RUN |
| P17 | Finish coloring promotes exactly one durable Gallery artwork; retry does not duplicate completion | NOT RUN |
| P18 | Free Draw committed work survives background/reopen and remains editable | NOT RUN |
| P19 | Gallery list scrolls with stable preview/placeholder behavior and no obvious main-thread decode freeze | NOT RUN |
| P20 | Gallery artwork reopen loads authoritative editable document independently of preview availability | NOT RUN |
| P21 | Gallery delete/return behavior remains correct and does not affect unrelated artwork truth | NOT RUN |
| P22 | Activity/navigation recreation does not duplicate Gallery entries or adaptive completion | NOT RUN |
| P23 | Available constrained-height/small-screen art controls remain reachable without making canvas unusable | NOT RUN |
| P24 | P6.4 large-text, Reduce motion, selection semantics and critical touch-target behavior remain intact | NOT RUN |
| P25 | TalkBack/readable semantics smoke remains understandable without noisy stroke/frame announcements | NOT RUN |
| P26 | Repeated Home → Lesson/Coloring/Free Draw/Gallery navigation loops complete without crash or obvious cumulative degradation | NOT RUN |
| P27 | 30-minute sustained use/soak completes with repeated drawing/coloring interaction | NOT RUN |
| P28 | Soak includes repeated undo/redo and save/load/reopen cycles | NOT RUN |
| P29 | Soak includes background/foreground cycles and Parent-session invalidation checks | NOT RUN |
| P30 | Final soak result: zero crash, zero corrupt known-good document, zero lost committed operation | NOT RUN |

### Core physical status

- PASS: **0 / 30**
- FAIL: **0 / 30**
- NOT RUN: **30 / 30**
- physical acceptance date: **not yet recorded**
- tester device/model/API/RAM: **not provided and not inferred**
- release blockers: **physical core QA pending**
- decision: **DO NOT MERGE YET**

## Hardware/API/performance evidence matrix

These gates require actual identified hardware/tooling. They are intentionally not counted as PASS from CI or from an unidentified tester device.

| Evidence | Status | Required note |
|---|---|---|
| Class L drawing W1/W2 numeric input/frame/save/load/undo gates | PENDING-HARDWARE | Requires identified Class L device/profile build measurement |
| Class M drawing W1/W2 numeric input/frame/save/load/undo gates | PENDING-HARDWARE | Requires identified Class M device/profile build measurement |
| Class S stylus-specific quality/performance evidence | PENDING-HARDWARE | Required before differentiated stylus-quality claim |
| API Band A (23–28) runtime compatibility | PENDING-HARDWARE | No runtime execution claimed unless actually run |
| API Band B (29–32) runtime compatibility | PENDING-HARDWARE | No runtime execution claimed unless actually run |
| API Band C (33–35) runtime compatibility | PENDING-HARDWARE | No runtime execution claimed unless actually run |
| API Band D (36) runtime compatibility | PENDING-HARDWARE | No runtime execution claimed unless actually run |
| Cold-start numeric timing (`am start -W`/Macrobenchmark or equivalent) | PENDING-HARDWARE/TOOLING | P6.5 defines no universal cross-device threshold |
| Product-level physical timing median/P95 evidence | PENDING-HARDWARE | Requires identified device/profile build and repeated measurements |
| Expanded-width/tablet physical layout matrix | PENDING-HARDWARE unless actually available | Geometry policy is automated-tested; physical tablet claim requires hardware |

If the available tester device later supplies model/API/RAM and satisfies one of these rows, update only the evidence actually measured. Leave all unavailable classes/bands pending.

## Closure rule

P6.5 remains open until:
1. the exact QA1 profile APK receives the focused core physical acceptance;
2. hardware-specific unavailable rows remain honestly pending, while any available hardware results are recorded exactly;
3. acceptance/release docs are finalized with no executable changes;
4. acceptance-documentation CI is GREEN;
5. PR #102 is marked ready and squash-merged;
6. merged-main Android CI is GREEN;
7. issue #100 is closed completed;
8. P6.6 starts only from the verified merged-main baseline.

Any executable change after physical QA starts requires a new monotonic versionCode (>31) and a fresh exact-binary physical cycle. Documentation-only commits do not replace `cbe31b2...` / artifact **10439774674**.
