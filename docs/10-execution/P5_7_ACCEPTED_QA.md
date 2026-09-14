# P5.7 Accepted QA — Local Adaptive Teaching

**Issue:** #86  
**PR:** #87  
**Branch:** `phase5/p5-7-local-adaptive-teaching`  
**Candidate:** `0.5.0-curriculum-expansion-p5.7-qa1`  
**versionCode:** **26**  
**Accepted binary commit:** `e258632e83e83a39ac649855ea19592c2f5003ae`  
**Status:** **PHYSICALLY ACCEPTED — FINAL ACCEPTANCE CI PENDING**

## Exact QA1 evidence

- Android CI #563 / run `34866700627`: **GREEN**.
- content quality: **24 lessons / 0 errors / exactly 6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings**.
- reviewed warning IDs: `ice-cream-shop`, `one-point-room`, `rainbow-weather`, `sailboat-scene`, `simple-car`, `tree-through-seasons`.
- permission allowlist: **PASS**.
- debug artifact ID: `10357282156`.
- debug APK size: `20,576,669` bytes.
- debug SHA256: `08f9cdecb451f60d93db54d1ad6a45b1c3e98d90e7f40137123396159ee62b84`.
- profile artifact ID: `10357367072`.
- profile APK size: `16,344,676` bytes.
- profile SHA256: `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`.

## Physical acceptance

**Acceptance date:** 2026-09-14  
**Tester result:** **45/45 PASS**  
**Tester-reported device model:** **Not provided**  
**Tester-reported Android/API:** **Not provided**  
**Reported defects:** **None**

The tester reported all focused P5.7 acceptance checks as good on the exact profile APK above. No device model or API level was inferred.

### Accepted areas

- install / startup / Airplane Mode;
- all four age bands receive sensible deterministic fresh suggestions;
- repeated Home launches stay stable for unchanged state;
- adaptive Home reason copy is friendly and non-judgmental;
- browse/category/journey discovery remains visible;
- coloring resume → drawing resume → fresh precedence remains intact;
- unmet prerequisites cannot become adaptive primary fresh suggestions;
- journey/progression/completion/interest behavior is sensible;
- Help never opens automatically;
- adaptive Help remains child-invoked and limited to authored Help/Replay;
- Little Artist keeps authored Help order;
- Replay does not loop forever;
- Trace is never invented;
- Help overlays remain outside artwork;
- adaptive state survives relaunch and app-data reset returns safely to default behavior;
- prior adaptive history does not leak into a fresh local profile lifecycle;
- Draw With Me, Watch Then Draw, authored Trace, recovery, Gallery, Coloring and Free Draw remain functional;
- all 24 production lessons remain discoverable.

## Acceptance integrity

The accepted APK is still the immutable QA1 binary from commit `e258632e83e83a39ac649855ea19592c2f5003ae`. This acceptance record is documentation-only. Any later binary/content change invalidates this acceptance and requires versionCode >26 with new evidence.

## Remaining closure gate

1. commit acceptance docs only;
2. acceptance-head Android CI must be GREEN;
3. mark PR #87 ready;
4. squash-merge exact acceptance head;
5. merged-main Android CI must be GREEN;
6. close #86 completed;
7. begin P5.8 from that verified main.
