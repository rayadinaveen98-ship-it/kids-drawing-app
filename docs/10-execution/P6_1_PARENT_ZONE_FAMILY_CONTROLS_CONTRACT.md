# P6.1 — Parent Zone & Family Controls Contract

**Issue:** #92  
**Parent epic:** #91  
**Target milestone:** `0.6.0-family-readiness`  
**Baseline:** Phase-5 release `0.5.0-curriculum-expansion`, versionCode 27  
**Baseline merge:** `fbc118343dde860a9784d7a47356eb6a7fff73e1`  
**Baseline merged-main CI:** Android CI #585 / run `34934427237` — GREEN  
**Contract status:** **FROZEN FOR P6.2 IMPLEMENTATION**

## 1. Purpose

Phase 6 adds family-management capability without turning a child-first offline art product into an account dashboard or surveillance product.

The Parent Zone exists to separate adult-only management, external navigation/export, privacy information and higher-impact local-data actions from ordinary child creative flows.

It is not an identity system. It does not claim to prove who the adult is, and it does not collect or persist a parent password, child secret, birth date, email address, phone number or online account credential.

## 2. Frozen principles

1. **Child creativity remains primary.** Parent controls must not make the child experience feel monitored, graded or permission-heavy.
2. **Adult-only surfaces are explicit.** Profile administration, bulk/reset data controls, outward export/share and external navigation do not sit directly in child mode.
3. **The gate is honest.** It deters casual/accidental child entry; it is not presented as high-assurance authentication.
4. **High-impact actions require their own confirmation.** A currently open Parent Zone never turns destructive actions into one-tap operations.
5. **Offline remains complete.** Parent Zone, gate, profiles, settings, progress summaries and local-data controls require no network or account.
6. **Progress is descriptive, not evaluative.** No grades, scores, rankings, mastery percentages, XP, streak pressure, permanent ability labels or comparisons between children.
7. **Artwork is protected.** Profile/settings/data operations must have explicit scope and must not silently damage unrelated artwork or sessions.
8. **Accepted child choices remain meaningful.** A default set by an adult may shape future entry state, but an explicit supported choice made by the child in an active creative/teaching flow is not silently overwritten.
9. **Accessibility applies to adults too.** Parent Gate and Parent Zone cannot depend on tiny targets, precision gestures, color alone, rapid timing or inaccessible text layouts.
10. **No engine redesign by convenience.** Drawing/Lesson/Coloring/Gallery/Free Draw/adaptive internals remain frozen unless a concrete defect proves a contract insufficient.

## 3. Parent Gate contract

### 3.1 Threat model

The gate protects against:
- accidental child entry into adult settings;
- casual tapping into export/external-navigation surfaces;
- accidental initiation of profile-wide or app-wide destructive actions.

The gate does **not** claim to protect against:
- a motivated older child who has observed the adult flow repeatedly;
- a person with unrestricted access to the unlocked Android device;
- device compromise or OS-level privilege.

UI/privacy copy must never call this gate a password, identity verification or secure authentication.

### 3.2 Gate mechanism — v0.6

The primary gate is an **Adult Intent Gate**:

1. Adult selects **Parent Zone** or another adult-only entry point.
2. A calm full-screen/interstitial gate explains that the next area changes family settings or can leave the child-safe flow.
3. The primary control reads **Hold to enter Parent Zone** (or context-specific equivalent).
4. The user must press and hold one large accessible control continuously for **2.5 seconds**.
5. A visible progress indicator and semantic accessibility announcement communicate progress.
6. Releasing early cancels progress without penalty.
7. Successful hold opens the requested adult surface.

Why this mechanism is frozen:
- fully offline;
- no arithmetic or literacy puzzle pretending to authenticate identity;
- no stored secret for a child to discover;
- accessible with one large control and no precision gesture sequence;
- clearly an intentionality barrier rather than a security claim.

### 3.3 Accessibility fallback

If accessibility services/input modality make long-press timing impractical, the gate must expose an equivalent **accessible confirmation path** consisting of two explicit large actions:

`Continue to adult controls` → `Yes, open Parent Zone`.

The fallback may require an additional confirmation screen, but it must not require rapid tapping, multi-finger gestures, drag precision, hidden controls or arithmetic.

### 3.4 Gate session

A successful gate creates an **in-memory Parent Zone session** only.

Session rules:
- valid while Parent Zone remains foreground, up to **5 minutes of continuous adult-area use**;
- returning to child Home/Studio invalidates the session immediately;
- process death invalidates the session;
- app background longer than **30 seconds** invalidates the session;
- opening another app through Android share/external navigation invalidates the session before returning;
- the token is not persisted to DataStore/files/database;
- no secret/challenge answer is stored.

A fresh gate is therefore required when the adult returns later.

### 3.5 Contextual adult-only entry points

Gate required before:
- Parent Zone root;
- post-onboarding profile administration that changes age/age-band or deletes a profile;
- export/share outside the app;
- external links from protected/safety/about surfaces;
- profile-wide reset/delete operations;
- app-local family-data reset;
- future purchase/account/cloud actions if introduced after separate scope approval.

A gate session may cover ordinary navigation inside Parent Zone, but destructive/export actions still follow the just-in-time rules below.

### 3.6 Cancel/failure behavior

At every gate:
- Back/Cancel returns to the exact safe originating child/adult-read-only surface where practical;
- no data mutation occurs before successful gate completion;
- failed/abandoned gate attempts do not affect recommendation/adaptive state;
- there is no lockout, punishment or escalating challenge;
- no analytics event is uploaded.

## 4. Parent Zone information architecture

P6.2 must implement a calm adult-facing shell with these top-level destinations:

1. **Family** — local child profiles and profile defaults.
2. **Learning** — local descriptive activity/progress and curriculum context.
3. **Accessibility & Audio** — adult-managed defaults and accessibility preferences that the product actually supports.
4. **Storage & Data** — scoped local-data/recovery controls and storage explanation.
5. **Safety & Privacy** — plain-language local storage/network/permission behavior.
6. **About** — app version, release information and approved external/legal links behind adult boundary where applicable.

The adult area should be visually quieter and denser than child mode but remain part of the same design system.

## 5. Parent/child ownership matrix

| Capability / data | Child surface | Parent Zone | Frozen ownership rule |
|---|---|---|---|
| Create first local profile during onboarding | Allowed | — | Child-friendly setup remains valid; no account required. |
| Create additional local profile | No direct child admin | Allowed | Parent-gated family administration. |
| Edit nickname | No admin edit | Allowed | Parent-owned after onboarding. |
| Edit age / age band | No | Allowed | Parent-owned because it changes curriculum/tool policy. |
| Delete local child profile | No | Allowed + destructive confirmation | Must be scoped and explain artwork/state consequences. |
| Default learning style | May choose during onboarding/lesson where supported | Allowed as default | Child’s explicit active-session supported choice wins. |
| Default teaching pace | May choose/change during lesson | Allowed as default | Parent default cannot silently override an active child choice. |
| Interests/categories | Child may browse/choose interests | Parent may edit profile defaults | Both may influence recommendations; no hidden lockout. |
| Handedness default | Child may select where exposed | Parent may edit default | Presentation preference, not a permanent ability label. |
| Narration/audio default | Child may mute/unmute where supported | Parent may set default | Child may silence audio in active flow; parent cannot force continuous narration. |
| Request Help / Replay | Allowed | No runtime control | Child-controlled; adaptive Help remains authored and voluntary. |
| Drawing/color/tool choices | Allowed | No runtime control | Creative authorship belongs to child. |
| Browse lessons/categories/journeys | Allowed | Learning view may summarize | Parent Zone must not hide curriculum based on inferred ability. |
| View Gallery | Allowed | May view family/profile gallery context | Cross-profile leakage prohibited. |
| Delete one artwork item | Preserve accepted current Gallery policy | May perform with confirmation | P6.1 does not silently remove an already accepted child capability; bulk/profile deletion remains adult-only. |
| Export/share artwork outside app | No ungated export | Allowed after gate + just-in-time confirmation | Original local artwork stays intact on cancel/failure. |
| View descriptive progress | Lightweight child context only | Allowed | Parent view is descriptive, never grading/comparison. |
| Reset adaptive recommendation history | No | Allowed + destructive confirmation | Must not delete artwork/lesson history unless separately selected. |
| Reset profile settings to defaults | No | Allowed + confirmation | Must not delete artwork. |
| Delete all profile-local data | No | Allowed + strongest confirmation | Exact scope and counts/consequences shown before commit. |
| Delete all app-local family data | No | Allowed + strongest confirmation | Separate from ordinary profile deletion; no accidental one-tap path. |
| External web/legal/store link | No ungated navigation from child mode | Allowed after adult boundary | Leaving app must be explicit. |

## 6. Progress visibility contract

### 6.1 Allowed parent-facing information

Learning may show, per selected local profile:
- completed lesson count and named completed lessons;
- currently resumable lesson/coloring work;
- Art Journeys started/completed/next eligible step;
- skills **encountered/practiced** based on lesson metadata;
- recent activity dates in a neutral history view;
- recent/saved artwork thumbnails where Gallery ownership already permits them;
- interests/preferences explicitly stored in the child profile;
- optional explanatory recommendation text already produced by accepted deterministic local policy.

### 6.2 Forbidden interpretations

Do not show or persist:
- drawing score or accuracy percentage;
- “mastery %”, IQ, talent, level or ability tier;
- ranks/leaderboards;
- XP, points or pressure-oriented streaks;
- predicted future ability;
- “behind/ahead” labels;
- comparison between siblings/profiles;
- a hidden numeric ability score merely because it is not rendered;
- negative language inferred from Help usage.

### 6.3 Language rules

Preferred patterns:
- “Practiced circles and curved lines”
- “Completed 6 drawing lessons”
- “Explored animals, nature and characters”
- “Next in Character Creator: Simple Body & Pose”
- “Asked for help in this lesson” only when that fact is genuinely useful and phrased neutrally; aggregate help counts should not become a performance metric.

Forbidden patterns:
- “Weak at proportions”
- “Only 42% mastered”
- “Below age level”
- “Needs improvement compared with…”
- “3-day streak lost”
- “Top artist / rank #…”

Progress copy describes **activity, exposure and available next opportunities**, never intelligence or worth.

## 7. Settings ownership contract

Parent Zone may own persistent defaults for supported features, including:
- local profile administration;
- age/age-band;
- handedness default;
- narration/audio default;
- default learning style/pace where product currently supports a default;
- accessibility defaults introduced by P6.4;
- local data/recovery controls;
- future outward sharing/export policy toggles only if deliberately implemented.

Rules:
- do not expose a toggle for a capability the runtime does not actually support;
- changing a default must explain material effects such as age-band curriculum/tool density;
- a parent default applies on future entry, not by silently mutating an active child session;
- safety/accessibility invariants may override presentation only where explicitly documented.

## 8. Destructive-action safety matrix

| Action | Gate needed | Extra confirmation | Scope guarantee |
|---|---:|---:|---|
| Delete one Gallery item under existing accepted Gallery flow | Existing policy | Yes | Only selected Gallery/artwork record and its defined owned payload. |
| Reset profile preferences to defaults | Yes | Yes | Profile preference fields only; artwork/sessions/history preserved. |
| Reset adaptive recommendation/help history | Yes | Yes | Adaptive state only; artwork/session/completion records preserved. |
| Delete one local profile | Yes | Strong confirmation | Only selected profile-owned data per implementation inventory; unrelated profiles untouched. |
| Delete all data for selected profile | Yes | Strong confirmation + explicit consequence summary | Selected profile scope only; unrelated profiles untouched. |
| Delete all app-local family data | Yes | Strongest confirmation + explicit family-wide scope | App-owned local data only; operation must be atomic/best-effort recoverable and never silently partial without reporting. |
| Export/share | Yes | Just-in-time destination confirmation | Read-only copy outward; original local artwork unchanged. |
| External navigation | Yes/adult session | Explicit leave-app action | No local-data mutation merely by opening link. |

### Strong confirmation rules

For profile/family-wide deletion:
- show the profile name or “all family data” scope in plain language;
- show which categories are affected: profile settings, progress/adaptive data, sessions and artwork only if that action actually includes them;
- do not preselect optional deletion categories silently;
- primary destructive control uses clear verb such as **Delete profile data**;
- Cancel is equally reachable;
- mutation begins only after confirmation;
- operation result reports success/failure without claiming deletion of data that was not actually removed.

## 9. Data isolation and deletion semantics

P6.2/P6.6 implementation must inventory actual persistence ownership before deleting anything.

Required invariants:
- every profile-owned record has a stable local profile association or an explicitly documented global scope;
- operations on Profile A cannot delete/modify Profile B artwork, adaptive state or sessions;
- resetting adaptive state cannot remove child artwork;
- deleting Gallery presentation cannot corrupt protected working state unless the existing ownership contract explicitly makes it the same object;
- partial/corrupt data must fall back safely rather than broadening deletion scope;
- process death during destructive mutation must not result in silent cross-profile loss;
- post-action Home/Gallery cannot strand references to deleted data.

## 10. Export/share boundary

Outward export/share remains adult-gated.

Flow:
1. Child/parent chooses an export affordance.
2. Adult Intent Gate is satisfied if no valid Parent Zone session exists.
3. App shows a just-in-time summary: artwork being shared and that Android will hand a copy to another app/service.
4. Adult chooses **Continue to share**.
5. Android Sharesheet/system destination chooser opens.
6. Parent session is invalidated before/when control leaves the app.
7. Cancel/failure leaves the original artwork and local state unchanged.

Rules:
- no silent background upload;
- no default social destination;
- no child-facing direct external share button bypass;
- URI/file permission must be temporary and limited to the chosen Android share flow when implemented;
- export implementation cannot require broad storage permission when Android scoped/file-provider mechanisms suffice.

## 11. Privacy / network / permissions contract

For Phase 6:
- Parent Zone works in Airplane Mode;
- no required parent or child online account;
- no cloud sync dependency;
- no behavioral analytics upload;
- no advertising SDK/identifier;
- no external child profile upload;
- no raw artwork upload by default;
- no new sensitive Android permission without explicit reviewed need and updated permission allowlist/evidence;
- privacy copy must be generated from actual implemented behavior, not marketing language;
- third-party SDK admission remains conservative and cannot silently expand data collection.

Safety & Privacy should plainly state, when true:
- profiles/progress/artwork are stored locally on the device;
- bundled core learning works offline;
- what Android permissions are used and why;
- what outward share/export does;
- that uninstall/device data clearing may remove local-only data unless a separately implemented backup/export mechanism exists.

## 12. Accessibility contract for Parent Zone and gate

Minimum Phase-6 requirements:
- all critical touch targets target at least **48×48 dp** effective size;
- gate and destructive actions have semantic labels/roles;
- focus order follows visual/task order;
- state/progress is not conveyed by color alone;
- critical controls remain reachable at larger system font scales targeted by P6.4;
- text may wrap/reflow; it must not be clipped behind fixed-height controls;
- important screens scroll rather than truncate on small displays;
- no gate/action requires multi-finger, drag precision, shake or rapid-tap timing;
- reduced-motion preference must be respected once P6.4 exposes it;
- landscape/tablet adaptation may change layout but not ownership/security semantics.

## 13. Parent Gate state model

States:

`ChildSafe` → `GatePresented` → `GateHolding` → `ParentSessionActive`

Additional transitions:
- `GatePresented` + Cancel/Back → `ChildSafe/origin`
- `GateHolding` + release before 2.5s → `GatePresented`
- `GateHolding` + hold complete → `ParentSessionActive`
- accessibility fallback confirm path → `ParentSessionActive`
- `ParentSessionActive` + child-mode exit → `ChildSafe`
- `ParentSessionActive` + process death → `ChildSafe`
- `ParentSessionActive` + background >30s → `ChildSafe`
- `ParentSessionActive` + 5-minute adult-session expiry → `GatePresented` on next protected action
- `ParentSessionActive` + external share/navigation → invalidate before/when leaving app

A destructive action introduces a separate state:

`ParentSessionActive` → `DestructiveConfirmation` → `ExecutingScopedMutation` → `Result`

Cancelling at `DestructiveConfirmation` performs no mutation.

## 14. Architecture boundaries for P6.2+

Recommended implementation ownership:
- `ParentGatePolicy` / session state owns gate timing/session validity;
- Parent Zone UI consumes that state but does not invent bypass rules;
- repositories/services own scoped profile/data mutations and return explicit results;
- UI does not reach directly into unrelated persistence stores to assemble deletes;
- progress read models are derived from accepted local product truth and do not write inferred ability state;
- export boundary uses Android platform sharing abstractions and read-only copy semantics;
- parent/session state is not stored in `ChildProfile` or adaptive teaching state.

Exact package/class names may vary in P6.2, but these ownership boundaries are frozen.

## 15. P6.1 acceptance requirements

P6.1 is accepted only when all of the following are true:
- Parent Gate purpose/threat model is explicit and non-deceptive;
- exact v0.6 gate behavior and accessibility fallback are frozen;
- session invalidation rules are frozen;
- Parent Zone IA is frozen;
- parent/child ownership matrix is frozen;
- progress allowed/forbidden language is frozen;
- destructive-action matrix and isolation invariants are frozen;
- outward export/share boundary is frozen;
- offline/privacy/network/permission rules are frozen;
- accessibility baseline is frozen;
- P6.2 can implement the contract without inventing product/security policy;
- Phase-5 product foundations remain unchanged.

## 16. P6.1 non-goals

- no Parent Zone production UI implementation;
- no new persistence deletion implementation;
- no export/share implementation;
- no cloud auth/account/sync;
- no payments/subscriptions;
- no public sharing/social features;
- no new curriculum production;
- no engine redesign.

## 17. Handoff to P6.2

P6.2 should implement the narrow foundation first:
1. in-memory Parent Gate state/policy with deterministic tests;
2. Parent Zone shell and protected routing;
3. local profile-management read/write path with isolation tests;
4. supported adult-owned settings defaults only;
5. Safety & Privacy/About shell based on actual behavior;
6. no bulk destructive data controls until P6.6 unless needed to prove the P6.2 profile-deletion contract safely.

P6.2 must produce an installable QA APK with exact CI evidence and focused physical acceptance before closure.