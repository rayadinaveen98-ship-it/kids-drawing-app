# 22 — Safety, Privacy, SDK & Release Review

**Status:** Phase 0.7 policy baseline  
**Review date:** 2026-09-11

This document is an engineering/product baseline, not legal advice. Public release still requires a final policy/legal review using then-current rules.

## 1. Current platform policy findings

### Google Play Families
For apps that include children in the target audience:
- Families Policy Requirements apply;
- app content accessible to children must be appropriate;
- Play Console target-audience, Data safety and content-rating answers must remain accurate;
- collection/transmission through app code and included SDKs counts toward the app's data practices;
- restricted device identifiers must not be transmitted from children or users of unknown age;
- solely child-targeted apps should not request AD_ID and must not transmit advertising ID;
- solely child-targeted apps may not request location permission or collect/use/transmit precise location;
- APIs/SDKs used in primarily child-directed services must be appropriate/approved for that use.

Official policy references reviewed:
- https://support.google.com/googleplay/android-developer/answer/9893335
- https://support.google.com/googleplay/android-developer/answer/11043825
- https://support.google.com/googleplay/android-developer/answer/9867159

### Android target SDK
Starting 2026-08-31, new apps and app updates submitted to Google Play must target Android 16 / API 36 or higher (standard Android apps).

Reference:
- https://developer.android.com/google/play/requirements/target-sdk

Engineering baseline for first Play-bound build:
- `targetSdk >= 36`
- compile with a compatible current SDK/toolchain
- minSdk selected from actual drawing/performance/device-market evidence, not convenience alone.

## 2. Product audience declaration direction

The product is genuinely designed for children, primarily approximately ages 4–12.

We must not falsely declare an adult/general audience merely to avoid Families requirements.

Before public launch:
- choose Play target age groups that match actual tested UI/content;
- ensure every selected band is intentionally supported;
- complete Target audience and content questionnaire accurately.

## 3. Data minimization baseline

Public V1 should require no child account.

Default local profile may store:
- nickname;
- selected age/age band;
- handedness;
- learning mode/pace;
- interests;
- progress;
- local artwork;
- local app preferences.

Do not request/store by default:
- legal full name;
- email;
- phone number;
- postal address;
- precise location;
- contacts;
- school;
- device advertising identifier;
- social graph;
- unrelated personal profile attributes.

## 4. Network baseline

Core drawing, lessons, coloring, Gallery and bundled content work offline.

### Alpha 0.1 / Art Lab
Prefer **no network permission/dependency at all** unless a concrete engineering need appears.

### Later V1 network features
If downloadable content, cloud backup, support or update metadata is added:
- each network purpose must be documented;
- transmitted fields must be enumerated;
- child-data necessity must be justified;
- privacy/Data safety declarations must be updated;
- service/SDK terms must permit child-directed use.

## 5. Android permission policy

### Not permitted in V1 without a new approved ADR/review
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- background location
- `READ_CONTACTS` / `WRITE_CONTACTS`
- SMS / Call Log permissions
- phone state/phone number access
- `AD_ID`
- `MANAGE_EXTERNAL_STORAGE`
- broad media read permissions merely to save/export drawings
- camera
- microphone
- Bluetooth/nearby-device permissions
- notification permission

The above are not currently needed for the core product.

### Storage
Use app-private/scoped storage for artwork and content.

For user-directed export/import:
- use Android Sharesheet / `FileProvider` / Storage Access Framework or Photo Picker where appropriate;
- do not ask for broad filesystem access.

### Audio output
Text-to-speech/audio playback requires no microphone permission.

### Future voice commands
Voice-command work is Later/Should Have. Microphone permission cannot be added casually; it requires:
- new privacy/policy review;
- explicit child/parent UX;
- clear purpose and just-in-time consent;
- on-device processing preference;
- retention/transmission contract;
- Play Data safety update.

## 6. Third-party SDK admission policy

No SDK enters the app merely because it is convenient.

Every non-AndroidX/non-Kotlin runtime dependency must have a lightweight SDK review recording:
- purpose;
- exact package/library and version;
- publisher;
- license;
- network behavior;
- identifiers/data accessed;
- permissions/manifest additions;
- child-directed/Families compatibility;
- security/update history;
- whether it can be replaced by platform code;
- removal/migration plan.

### Default-deny classes for child V1
Do not include without explicit policy review:
- ad SDKs;
- attribution SDKs;
- behavioral analytics SDKs;
- social/login SDKs;
- third-party chat SDKs;
- cloud AI SDKs;
- generic 'growth' SDKs;
- fingerprinting/device-identity libraries.

## 7. Analytics decision

### Alpha / internal builds
No third-party product analytics SDK required.

Use:
- local debug metrics;
- test logs;
- benchmark results;
- manually collected usability observations.

### Public V1
Default remains **no behavioral analytics** unless we identify a concrete quality question that cannot be answered safely otherwise.

If analytics is later proposed:
- favor aggregate/minimized event design;
- do not use advertising identifiers;
- avoid collecting child artwork/content in telemetry;
- document retention;
- review provider terms for child-directed use;
- perform Data safety/COPPA/local-law review before inclusion.

## 8. Crash reporting decision

Alpha can use local logs/Android Studio/GitHub CI without a remote crash SDK.

A remote crash service is not required for Phase 1.

Before adding one to public builds, review:
- automatic device identifiers;
- IP/network metadata;
- breadcrumb/user-input capture;
- custom-key content;
- retention;
- child-directed terms.

Never attach artwork, nickname, lesson free-text or child-created content to crash reports.

## 9. Advertising / monetization baseline

V1 architecture contains **no ads**.

Do not add:
- ad SDK;
- rewarded ads;
- interstitials;
- behavioral targeting;
- cross-promotion trackers.

If monetization is added later, favor parent-controlled paid entitlement/subscription design rather than advertising to children.

Purchases must use then-current Google Play Billing/policy requirements and should be surfaced from an adult/parent context, not pressured through child-facing companion language.

## 10. COPPA / child privacy direction

Because the service is child-directed and includes under-13 users, any future collection of personal information transmitted off-device must be evaluated under COPPA and other applicable child-privacy laws.

Current FTC baseline reviewed:
- COPPA requires covered child-directed online services to provide parental notice and obtain verifiable parental consent before collecting/using/disclosing covered personal information from children under 13, subject to the rule's details/exceptions;
- 2025 rule updates strengthened restrictions around children's data and third-party advertising;
- 2026 FTC policy statement addresses limited age-verification processing under stated conditions.

References:
- https://www.ftc.gov/news-events/topics/protecting-consumer-privacy-security/kids-privacy-coppa
- https://www.ftc.gov/news-events/news/press-releases/2025/01/ftc-finalizes-changes-childrens-privacy-rule-limiting-companies-ability-monetize-kids-data
- https://www.ftc.gov/news-events/news/press-releases/2026/02/ftc-issues-coppa-policy-statement-incentivize-use-age-verification-technologies-protect-children

Best engineering strategy remains: **do not collect what we do not need.**

## 11. Artwork privacy

Artwork is child-created content and must be treated as private by default.

V1 rules:
- save locally by default;
- no public gallery;
- no automatic upload;
- no face/object analysis;
- no training-data use;
- no hidden metadata sharing;
- outward export is a deliberate parent-gated/user-directed action;
- deleting artwork deletes the app-owned local editable document and derivatives subject to normal backup/platform behavior documented later.

## 12. External links

Child shell should contain no casual external web links.

External destinations such as:
- privacy policy;
- support site/email;
- licenses;
- future purchase/management pages

belong in Parent Zone and require the Parent Gate where appropriate.

## 13. Content safety

All bundled/recommended lesson content accessible to children must be reviewed for age appropriateness.

Content production QA should check:
- subject matter;
- text/narration;
- cultural sensitivity;
- frightening imagery;
- weapons/violence context;
- body/appearance messaging;
- unsafe imitation instructions;
- copyright/licensing provenance.

No user-generated public content exists in V1.

## 14. Dependency/manifest CI checks

Phase 1+ CI should eventually fail or flag when:
- an unexpected sensitive permission appears in merged manifest;
- `AD_ID` appears;
- dependency lock/version catalog changes without review;
- a known restricted SDK/package is introduced;
- release targetSdk drops below current project baseline.

Maintain a `THIRD_PARTY_NOTICES` / dependency inventory before public release.

## 15. Release channels

### Internal engineering builds
Purpose: engine development/QA.

Can contain:
- debug metrics;
- Art Lab;
- diagnostic menus;
- synthetic test lessons.

Must not contain real production secrets.

### Closed child/parent testing
Requires:
- safer debug surfaces;
- privacy explanation;
- parent-supervised recruitment/consent process appropriate to study design;
- no unnecessary telemetry.

### Public V1
Requires separate launch gate below.

## 16. Public launch compliance gate

Before Play production rollout, re-check the policies current on that date and complete at least:
- Google Play developer account/verifications required then;
- targetSdk requirement;
- Families target-audience declaration;
- Data safety form;
- privacy policy URL/content;
- IARC/content rating;
- app access/reviewer instructions if gated content exists;
- permissions declarations if any sensitive permission was introduced;
- SDK Families/child-directed compatibility review;
- billing/subscription policy if monetized;
- store listing age appropriateness;
- legal review for primary launch regions where needed;
- security/privacy review;
- accessibility QA;
- physical-device performance gate;
- content-license/provenance review.

Phase 0 completion does **not** claim those future public-release forms are already complete.

## 17. Policy-change rule

Platform and child-safety policy is versioned external reality.

Before any public release candidate:
1. re-open official Play/Families policy;
2. compare against this document;
3. record material changes in Git;
4. create ADR/issues for architecture-impacting policy changes;
5. do not rely on an old Phase 0 policy snapshot as final legal/compliance signoff.