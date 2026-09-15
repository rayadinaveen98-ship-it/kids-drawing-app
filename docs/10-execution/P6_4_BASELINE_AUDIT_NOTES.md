# P6.4 Baseline Audit Notes — Accessibility System V2

Status: **READ-ONLY BASELINE AUDIT — NOT THE FROZEN CONTRACT**  
Issue: #98  
Parent epic: #91  
Verified dependency baseline: P6.3 merge `fc81703b57419d14c9baf50a0fb4fb91554652f0`, merged-main Android CI #617 GREEN

These notes capture verified source observations before the P6.4 contract is frozen. They do not authorize implementation beyond audit/contract work.

## Existing accessibility foundation

- Shared `StudioTheme` already uses scalable `sp` typography.
- Age-aware design metrics already provide effective minimum touch targets above the 48dp baseline: approximately 54–68dp depending on age band/context.
- Critical Parent Gate and Parent Zone screens already use scrolling/reflow patterns and physically passed prior large-text/small-screen checks.
- Parent Gate already provides an explicit accessible two-step fallback in addition to the primary hold interaction.
- Several major Home/Parent controls already expose text labels or content descriptions.

## Verified gaps / risks

### Reduced motion
- There is no centralized reduced-motion/accessibility policy yet.
- Parent Gate uses visual hold-progress animation over the existing 2.5-second policy interval.
- P6.4 may simplify/static-replace visual motion but **must not shorten, bypass or otherwise change the 2.5-second Adult Intent Gate timing or adult-session security semantics**.

### Contrast
A source-level palette calculation shows that contrast must be audited per real usage rather than globally assumed:
- `Ink900` on `Paper50` ≈ 15.2:1;
- `Ink700` on `Paper100` ≈ 8.3:1;
- `Ink500` on `Paper100` ≈ 4.34:1;
- `Studio600` on white ≈ 5.45:1;
- `Studio500` on white ≈ 4.0:1.

The latter normal-text combinations are below a 4.5:1 target and require usage-level review/fix where they carry ordinary text or critical state.

### Large text / layout
- Older-age Home layouts place Free Draw and Gallery side-by-side in a row.
- Several supporting labels/cards cap text at two lines.
- P6.4 must adapt layout based on actual font scale/reachability rather than age alone.

### State semantics / non-color-only state
- Selectable profile/default-choice cards use fill/border styling for selected state and need explicit selected semantics plus a visible non-color-only selected cue.
- P6.4 must audit toggle/current/selected state announcements across critical flows.

### Screen reader / focus
- Existing semantics are uneven rather than centrally governed.
- Major child and parent flows need an ordered audit for names, roles, state descriptions, traversal/focus behavior and meaningful status announcements where practical.

### Left-handed/layout behavior
- The existing profile already stores handedness, but P6.4 must inspect the real drawing/tool layouts before deciding whether any tool placement should adapt.
- Do not mirror security/ownership/navigation semantics merely because the profile is left-handed.

## Frozen inherited constraints entering the contract

- critical effective targets remain >=48×48dp;
- no color-only critical state;
- large text/small screens keep critical actions reachable through reflow/scroll;
- no mandatory multi-finger, precision, shake or rapid-tap critical interaction;
- reduced motion cannot weaken Parent Gate timing/session rules;
- accessibility behavior cannot change child/adult ownership boundaries;
- no account/cloud/network dependency/new Android permission;
- accepted Drawing/Lesson/Coloring/Gallery/adaptive truth semantics remain unchanged absent a concrete defect + explicit contract amendment;
- 0.6 remains single-profile;
- versionCode stays 29 through contract and initial implementation stabilization; reserve >29 only after the complete implementation is automated-green.

## Next contract work

Before production changes, freeze:
1. `P6_4_ACCESSIBILITY_V2_AUDIT.md`;
2. `P6_4_ACCESSIBILITY_V2_CONTRACT.md`;
3. `P6_4_ACCEPTANCE_CHECKLIST.md`.

The contract must define which preferences are local/persisted, reduced-motion semantics, font-scale breakpoints/reflow rules, contrast acceptance rules, selected/toggled/current state semantics, major screen-reader/focus flows, left-handed evidence boundaries, regression invariants and exact QA discipline.