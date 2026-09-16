# P6.4 Release Report — Accessibility System V2

Status: **PHYSICAL ACCEPTED / REPOSITORY CLOSURE ACTIVE**  
Issue: #98 — OPEN pending merged-main closure  
Parent epic: #91  
PR: #99 — DRAFT pending acceptance-doc CI  
Target milestone: `0.6.0-family-readiness`

## Scope delivered

P6.4 turns the app's accessibility-friendly foundations into a coherent Accessibility System V2 across critical child and parent flows while preserving accepted safety, ownership, learning and artwork semantics.

Delivered:
- device-local `AccessibilityPreferencesStore` with persistent `reduceMotion` outside ChildProfile;
- deterministic system-font-scale layout policy: standard `<1.30`, large `>=1.30`, extra-large `>=1.60`;
- production Parent Zone → Accessibility & Audio surface;
- reduced-motion Parent Gate visual path with unchanged 2.5-second eligibility and accessible fallback;
- radio / checkbox / navigation semantic separation and visible non-color selected cues;
- human-readable drawing/coloring color names + semantic selected state;
- targeted text-contrast hardening;
- large-text/reachability hardening across Home, onboarding, Guided Lesson, Coloring, Free Draw and Gallery;
- honest direct-touch canvas semantics and read-only Gallery semantics;
- truthful handedness copy without speculative mirroring;
- authored teacher timing preserved under Reduce motion;
- focused JVM + Compose/instrumentation-source accessibility coverage;
- no account/cloud/network dependency, new permission, behavioral analytics upload or multi-profile migration.

## Contract and stabilization evidence

- verified baseline `56a5b99bee65bec8bb6acf687587a6ac7fc62cf7` / CI #622 GREEN;
- contract head `7935348bfe3378b363cd7d5753c8f2e6b98d006c`;
- contract audit **74/74 PASS**;
- contract CI #623 GREEN;
- complete versionCode-29 implementation stabilization head `701d29e465a12f9e6d6f2a3331e82a8725fbd11a`;
- stabilization CI #647 / run `35067693656` **GREEN**.

## Immutable physically accepted QA1 executable

- executable source head `fec854c3321d1966dc05437c2c4a3651c2323ae1`;
- versionName `0.6.0-family-readiness-p6.4-qa1`;
- versionCode **30**;
- candidate CI #648 / run `35068180162` **GREEN**;
- frozen curriculum **24 lessons / 0 errors / exactly 6 reviewed warnings**;
- Android permission allowlist GREEN;
- exact APK identity/evidence packaging GREEN.

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

Artifact ZIPs were independently downloaded; APK byte sizes and SHA256 values matched CI-packaged evidence.

## Physical acceptance

Authoritative matrix: `docs/10-execution/P6_4_FINAL_QA.md`.

- PASS **30/30**;
- FAIL **0/30**;
- NOT RUN **0/30**;
- acceptance date **2026-09-16**;
- tester device/model/API **not provided and not inferred**;
- reported blockers **none**.

The exact accepted physical candidate remains profile artifact **10434518471** from executable head `fec854c...`. Later documentation, merge or future phase commits do not replace that tested binary.

## Repository closure path

1. require Android CI GREEN on the final acceptance-documentation head;
2. mark PR #99 ready and squash-merge;
3. verify merged-main Android CI GREEN on the exact squash merge commit;
4. close #98 completed;
5. activate P6.5 Device & Performance Hardening issue #100 from that verified main baseline.

Any executable modification to the accepted P6.4 candidate requires a new monotonic versionCode >30 and fresh physical QA.