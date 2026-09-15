# P6.1 Scope Decision — Single Local Child Profile for 0.6

**Issue:** #92  
**Parent epic:** #91  
**Status:** **NORMATIVE ADDENDUM — SUPERSEDES CONFLICTING MULTI-PROFILE WORDING IN THE P6.1 CONTRACT**

## Decision

`0.6.0-family-readiness` will manage **one existing local child profile** through Parent Zone.

Phase 6 will **not** introduce multiple simultaneous child profiles or profile switching.

A future multi-child milestone must first define and execute a dedicated profile-identity/data-ownership migration across every profile-sensitive persistence domain before exposing additional profiles in UI.

## Why this is required

The accepted Phase-5 architecture is single-profile:
- `ChildProfile` has no stable profile identifier;
- `ChildProfileStore` persists one completed profile/draft in the single `child_profile` Preferences DataStore;
- accepted lesson/session, Gallery/artwork and adaptive state were proven under that single-profile product truth;
- Phase 5 explicitly froze those systems after physical acceptance.

Adding a second profile safely would therefore not be a Parent Zone UI feature. It would require a cross-cutting migration that introduces stable profile identity and ownership to profile-sensitive stores, existing records, recovery paths and deletion semantics.

Doing that silently inside P6.2 would violate the Phase-6 rule against reopening accepted foundations without an explicit migration contract and would create unacceptable risk of artwork/progress leakage or deletion across children.

## 0.6 Parent Zone profile scope

Allowed in 0.6:
- view the current local child profile;
- edit nickname;
- edit age/age band;
- edit supported stored defaults such as teaching mode, pace, interests, handedness and narration preference;
- explain the effect of age/default changes;
- reset supported preference/adaptive domains only when the relevant later slice implements the scoped operation safely;
- delete/reset current-profile/app-local data only under the destructive-action contracts implemented in P6.6;
- return safely to the existing child experience.

Not allowed in 0.6:
- create a second child profile;
- switch between multiple child profiles;
- duplicate/clone a child profile;
- show sibling comparisons;
- pretend existing artwork/session/adaptive records are profile-isolated when no stable profile ID exists.

## Superseded P6.1 wording

The following concepts in `P6_1_PARENT_ZONE_FAMILY_CONTROLS_CONTRACT.md` are narrowed by this addendum:
- “local child profiles” means the **current local child profile** for 0.6;
- the ownership-matrix row allowing **Create additional local profile** is **DEFERRED / NOT IN 0.6**;
- “cross-profile leakage prohibited” remains a permanent future requirement but is not claimed as a currently testable multi-profile capability;
- destructive-action references to Profile A vs Profile B become a **future multi-profile invariant**; current 0.6 operations must instead prove strict operation scope among artwork, profile preferences, sessions, adaptive state and app-global state for the one local profile;
- “per selected local profile” progress wording means the current local profile only.

All other Parent Gate, Parent Zone, progress-language, destructive-action, export, privacy and accessibility contracts remain unchanged.

## Future multi-profile start gate

Before multiple profiles may be implemented, a dedicated contract must inventory at minimum:
1. stable `profileId` model and migration of the existing single profile;
2. Gallery/artwork ownership;
3. drawing/lesson/coloring resumable-session ownership;
4. Free Draw working-state ownership;
5. adaptive-state ownership;
6. completion/journey/progress ownership;
7. deletion/recovery semantics;
8. migration rollback/corruption behavior;
9. upgrade tests from all supported existing app schemas;
10. physical QA proving no cross-profile display, mutation or deletion leakage.

Until that gate exists, single-profile is the authoritative product contract.

## P6.2 consequence

P6.2 Parent Zone Foundation should be narrow and safe:
- implement Parent Gate/session policy;
- add Parent Zone shell;
- allow editing the one existing `ChildProfile` through the accepted `ChildProfileStore` contract or a backward-compatible evolution of it;
- add adult settings/safety/about surfaces that are real and supported;
- do **not** add profile IDs or multi-profile persistence in P6.2.

This decision reduces hidden migration risk while delivering the actual V1 Parent Zone promised by the original PRD.