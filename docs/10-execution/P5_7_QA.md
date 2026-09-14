# P5.7 QA — Local Adaptive Teaching

**Issue:** #86  
**PR:** #87  
**Branch:** `phase5/p5-7-local-adaptive-teaching`  
**Candidate:** `0.5.0-curriculum-expansion-p5.7-qa1`  
**versionCode:** **26**  
**Status:** **PHYSICALLY ACCEPTED — FINAL ACCEPTANCE CI PENDING**

## Verified automated evidence

- QA1 binary commit: `e258632e83e83a39ac649855ea19592c2f5003ae`.
- Android CI #563 / run `34866700627`: **GREEN**.
- content quality: **24 lessons / 0 errors / exactly 6 reviewed `NO_JOURNEY_MEMBERSHIP` warnings**.
- permission allowlist: **PASS**.
- debug artifact ID: `10357282156`.
- debug APK size: `20,576,669` bytes.
- debug SHA256: `08f9cdecb451f60d93db54d1ad6a45b1c3e98d90e7f40137123396159ee62b84`.
- profile artifact ID: `10357367072`.
- profile APK size: `16,344,676` bytes.
- profile SHA256: `126b0c2d742a4c22b82239ded0502baf5119cf35a89883391ac68239c37b14d0`.

Reviewed warning IDs:
- `ice-cream-shop`
- `one-point-room`
- `rainbow-weather`
- `sailboat-scene`
- `simple-car`
- `tree-through-seasons`

## Physical acceptance

**Acceptance date:** 2026-09-14  
**Tester-reported device model:** **Not provided**  
**Tester-reported Android/API:** **Not provided**  
**Physical result:** **PASS**  
**Passed:** **45 / 45**  
**Failed:** **0 / 45**

The tester reported all focused P5.7 checks as good on the exact release-like profile APK above. Device model/API were not supplied and were not inferred.

Accepted coverage includes:
- install/startup and Airplane Mode;
- sensible/stable fresh suggestions across all four age bands;
- friendly explainable adaptive reason copy;
- unchanged browse/category/journey visibility;
- coloring resume → drawing resume → fresh precedence;
- prerequisite, journey, completion and interest behavior;
- no automatic Help;
- child-invoked authored Help/Replay only;
- no invented Trace and no Replay loop;
- overlay/artwork isolation;
- adaptive-state relaunch/default/reset safety;
- fresh profile lifecycle does not expose old adaptive history;
- Draw With Me, Watch Then Draw, authored Trace, Save & Leave/recovery, Gallery, Coloring and Free Draw regression smoke;
- all 24 lessons remain discoverable.

## Acceptance integrity

The accepted APK remains the immutable binary from commit `e258632e83e83a39ac649855ea19592c2f5003ae`. Documentation-only acceptance recording does not alter the accepted candidate. Any later binary/content change requires versionCode >26 and new evidence.

## Remaining closure gate

1. acceptance documentation exact-head CI GREEN;
2. mark PR #87 ready;
3. squash-merge exact acceptance head;
4. merged-main CI GREEN;
5. close issue #86 completed;
6. start P5.8 from that verified main.
