# P6.4 Release Report — Accessibility System V2

Status: **QA1 AUTOMATED GREEN / PHYSICAL ACCEPTANCE PENDING**  
Issue: #98 — OPEN  
Parent epic: #91  
PR: #99 — DRAFT  
Target milestone: `0.6.0-family-readiness`

## Scope delivered

P6.4 turns the app's accessibility-friendly foundations into a coherent Accessibility System V2 across critical child and parent flows while preserving the accepted safety, ownership, learning and artwork model.

Delivered implementation:
- device-local `AccessibilityPreferencesStore` with one real `reduceMotion` preference outside ChildProfile;
- deterministic system-font-scale layout policy: standard `<1.30`, large `>=1.30`, extra-large `>=1.60`;
- production Parent Zone → Accessibility & Audio surface;
- reduced-motion Parent Gate visual path with unchanged 2.5-second eligibility and existing accessible two-confirmation fallback;
- semantic separation of single-choice radio, multi-select checkbox and navigation cards;
- visible non-color selected cues;
- human-readable drawing/coloring color names and semantic selected state;
- targeted contrast hardening for active normal text without changing the art palette for contrast reasons;
- large-text/reachability hardening across Home, onboarding, Guided Lesson, Coloring, Free Draw and Gallery;
- honest direct-touch canvas semantics and explicit read-only Gallery artwork semantics;
- truthful handedness copy without speculative mirroring;
- teacher demonstration timing preserved as authored instructional content under Reduce motion;
- focused JVM and Compose/instrumentation-source coverage;
- no new account, cloud sync, network dependency, behavioral analytics upload, Android runtime permission or multi-profile migration.

Explicitly excluded:
- formal WCAG certification claim;
- claiming fully non-visual freehand geometry or spatial Fill equivalence;
- rewriting lesson teaching state machines to skip authored demonstrations;
- broad toolbar mirroring without an evidence-backed side obstruction;
- custom in-app text-size slider;
- P6.5 device/performance hardening;
- P6.6 destructive family-data/recovery controls;
- multi-profile migration or cloud accessibility-profile sync.

## Contract and stabilization evidence

- verified P6.4 baseline `56a5b99bee65bec8bb6acf687587a6ac7fc62cf7` / Android CI #622 GREEN;
- frozen contract head `7935348bfe3378b363cd7d5753c8f2e6b98d006c`;
- contract audit **74/74 PASS**;
- contract Android CI #623 GREEN;
- complete versionCode-29 implementation stabilization head `701d29e465a12f9e6d6f2a3331e82a8725fbd11a`;
- stabilization Android CI #647 / run `35067693656` **GREEN**.

## Immutable P6.4 QA1 executable

- executable source head `fec854c3321d1966dc05437c2c4a3651c2323ae1`;
- versionName `0.6.0-family-readiness-p6.4-qa1`;
- versionCode **30**;
- candidate Android CI #648 / run `35068180162` **GREEN**;
- frozen curriculum **24 lessons / 0 errors / exactly 6 reviewed warnings**;
- Android permission allowlist GREEN;
- exact debug/profile APK identity GREEN.

### Profile artifact
- artifact ID **10434518471**;
- archive digest `sha256:28978533088b03435343844018d99ec9bd0db0ef6ebf1929db4ef82a77731125`;
- APK size **16,426,590 bytes**;
- APK SHA256 `2727d18d8f21c0e60612f8134aeb06b502941e816ea5390c757007f5dc31acce`.

### Debug artifact
- artifact ID **10435236575**;
- archive digest `sha256:f0d4c65235c903ba4ea7e8ac8237451fce0bce367904f28a1c74c330f73c7a66`;
- APK size **20,724,127 bytes**;
- APK SHA256 `e7d1bb3b0dc1f2dc70e39ead44f9bfae0ad834db4fe511d2a2a44f8e8aa5965f`.

### Content-quality artifact
- artifact ID **10434204389**;
- archive digest `sha256:fa361b94242eaeb1dfd7bcad6b04951abee2c7d2e2afffc3b2d3daaaf0185236`.

CI packaged `RELEASE_IDENTITY.txt` records:
- `versionName=0.6.0-family-readiness-p6.4-qa1`;
- `versionCode=30`;
- workflow SHA `37cc44d635e2f39db7a070b2a302d71996edbc2c`;
- source head SHA `fec854c3321d1966dc05437c2c4a3651c2323ae1`.

Artifact ZIPs were downloaded independently. Both APK SHA256 values and byte sizes were recomputed and match CI's packaged `SHA256SUMS.txt` and `APK_SIZES.txt` exactly.

## Physical acceptance

Authoritative matrix: `docs/10-execution/P6_4_FINAL_QA.md`.

Current state:
- PASS **0/30**;
- FAIL **0/30**;
- NOT RUN **30/30**;
- test date **pending**;
- tester device/API **not provided and not inferred**;
- automated release blockers **none**;
- physical acceptance **pending**.

The exact physical candidate is profile artifact **10434518471**, built from executable head `fec854c...`. Later documentation-only heads and their CI rebuilds do not replace that binary.

## Closure path after physical acceptance

After all focused physical rows pass on the exact candidate:
1. update `P6_4_FINAL_QA.md` to 30/30 PASS and record only actually provided tester/device information;
2. update this release report and `PROJECT_STATUS.md`;
3. require Android CI GREEN on the final acceptance-documentation head;
4. mark PR #99 ready for review and squash-merge it;
5. verify merged-main Android CI GREEN on the exact squash merge commit;
6. close issue #98 completed;
7. activate P6.5 Device & Performance Hardening from that verified merged-main baseline.

Any executable change to P6.4 after the immutable QA1 candidate requires a new monotonic versionCode >30 and a fresh exact-binary QA cycle.
