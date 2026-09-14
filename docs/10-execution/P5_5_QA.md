# P5.5 QA — Curriculum Expansion Set D

**Slice:** P5.5 — Curriculum Expansion Set D #82  
**PR:** #83  
**Branch:** `phase5/p5-5-curriculum-set-d`  
**QA candidate:** `0.5.0-curriculum-expansion-p5.5-qa1`  
**versionCode:** 24  
**Exact app/content QA commit:** `3a538b5f7c2db118a0006176b7093b0e22961f9b`  
**Status:** **ACCEPTED — AUTOMATED + CONTENT LAB + PHYSICAL QA PASS**  
**Last updated:** 2026-09-14

## 1. Scope delivered

P5.5 adds six production lessons through the generic `LessonPackageLoader` / `LessonCatalog` path:

1. `snail-garden@1`
2. `elephant-from-shapes@1`
3. `simple-car@1`
4. `sailboat-scene@1`
5. `planet-with-rings@1`
6. `friendly-alien@1`

The catalog grows from 14 to **20 release lessons**. Frozen architecture and safety boundaries remain intact: no lesson-ID-specific runtime path, no scoring/rank/stars/XP/permanent ability labels/cloud profiling, and no mandatory account/network dependency.

## 2. Curriculum result

- `journey.animal_artist`: Little Fish → Snail Garden → Cute Cat → Friendly Owl → Elephant From Shapes → Fox Portrait.
- `journey.space_artist`: Planet With Rings → Simple Rocket → Friendly Alien → Design Your Spaceship.
- Simple Car and Sailboat Scene remain intentionally standalone.

Set-D validation also corrected retained Little Fish/Cute Cat journey metadata to canonical P5.1 IDs. This was metadata/test-fixture compatibility only; lesson identity, drawing geometry and saved-artwork contracts were unchanged.

## 3. Automated quality acceptance

Exact QA1 Android CI #525 / run `34837734724`:

- release lessons: **20**;
- errors: **0**;
- warnings: **5**;
- warning code: `NO_JOURNEY_MEMBERSHIP` only;
- reviewed warning lessons only: Rainbow Weather, Tree Through Seasons, Ice Cream Shop, Simple Car, Sailboat Scene.

Coverage:
- Little Artists **8**;
- Creative Explorers **18**;
- Growing Artists **14**;
- Young Artists **6**;
- Draw With Me **19**;
- Watch Then Draw **13**;
- Trace & Learn **4**;
- coloring **4**;
- prepared coloring **3**.

The QA evidence/checklist synchronization commit `8f67896c08f413d4dc81c611234e5897da055d7c` also passed Android CI #526.

## 4. Immutable QA1 binary evidence

### Release-like profile APK

- artifact ID: `10344519403`
- APK: `Kids_Drawing_0.5.0_Curriculum_Expansion_P5.5_QA1-profile.apk`
- APK size: **16,293,898 bytes**
- SHA-256: `9e23562b5bea4f2b406a48a1b64e339887f91926ad8789deaa160bbc93301629`

### Debug APK

- artifact ID: `10345315128`
- APK size: **20,508,319 bytes**
- SHA-256: `79fd638cefa5e1e46a337d2a35a720a1586139a7aa3c8b864b893e880095e307`

### Content-quality artifact

- artifact ID: `10344354743`
- artifact archive SHA-256: `73defb78fe44c483ddc0f68dbae8af613f9974de4bd876df222995a71abb3a4f`

Permission allowlist passed for debug/profile APKs. The only requested permission is the app-local `com.navin.kidsdrawing.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

## 5. Interactive Content Lab acceptance — PASS

On 2026-09-14, after receiving the exact QA1 binary and fixed 18-row checklist, the tester reported **“all good”** for the requested Content Lab pass.

Recorded result:
- **18/18 PASS**;
- all six Set-D packages accepted;
- previews/thumbnails/geometry/help/localization accepted;
- Animal Artist and Space Artist progressions accepted;
- open-authorship behavior accepted;
- diagnostics accepted at 20 lessons / 0 errors / exactly five reviewed warnings;
- no content/binary-changing defect reported.

## 6. Focused physical-device acceptance — PASS

The tester reported the fixed exact-profile 36-row physical matrix **all good** against QA1.

Recorded result:
- **36/36 PASS**;
- all six Set-D lesson flows accepted;
- Draw With Me / Watch Then Draw distinctions accepted;
- no unintended Trace on older Set-D content;
- open creative turns accepted;
- age-band Companion presentation accepted;
- Animal/Space journey discovery accepted;
- resume/recovery, cross-lesson isolation and Gallery accepted;
- prior-content/Free Draw regression smoke accepted;
- Airplane Mode/lifecycle accepted;
- no crash, ANR, deadlock, lost artwork, unexpected account/network requirement, permission prompt or binary-changing defect reported.

Tester metadata:
- tester: `User / product tester`;
- date: `2026-09-14`;
- device model: `not provided`;
- Android version/API: `not provided`.

Device/API are explicitly left unknown rather than inferred.

## 7. Acceptance decision

P5.5 QA1 is the accepted physical candidate. No new versionCode is required because no binary/content-changing defect was reported.

Remaining closure gates only:
1. final exact-head acceptance-document CI;
2. mark PR #83 ready for review;
3. squash-merge the verified PR head;
4. verify merged-main Android CI;
5. close #82 completed;
6. freeze P5.5 and start P5.6 only from verified `main`.
