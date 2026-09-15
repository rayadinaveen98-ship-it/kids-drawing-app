# P6.1 Acceptance Checklist — Parent Zone & Family Controls Contract

**Issue:** #92  
**Parent epic:** #91  
**Target:** `0.6.0-family-readiness`  
**Type:** specification/contract gate — no production Parent Zone implementation in P6.1.

## A. Parent Gate

- [x] A01 — Gate purpose is casual/accidental child-access deterrence, not identity authentication.
- [x] A02 — Gate requires no parent/child password, email, birth date, account or network.
- [x] A03 — Primary Adult Intent Gate behavior is frozen: one large 2.5-second hold control.
- [x] A04 — Accessible non-long-press confirmation fallback is frozen.
- [x] A05 — Cancel/failed gate mutates no product data.
- [x] A06 — Gate has no lockout, punishment or escalating challenge.
- [x] A07 — In-memory session validity and invalidation rules are frozen.
- [x] A08 — Returning to child mode/process death/background timeout/external navigation invalidates appropriately.
- [x] A09 — Protected entry points are enumerated.

## B. Parent Zone information architecture

- [x] B01 — Family section defined.
- [x] B02 — Learning section defined.
- [x] B03 — Accessibility & Audio section defined.
- [x] B04 — Storage & Data section defined.
- [x] B05 — Safety & Privacy section defined.
- [x] B06 — About section defined.
- [x] B07 — Adult presentation direction is calm/informational and still uses product design system.

## C. Parent / child ownership

- [x] C01 — First-profile onboarding remains child-friendly/local/account-free.
- [x] C02 — Additional profile creation/edit administration is adult-gated.
- [x] C03 — Age/age-band post-onboarding changes are parent-owned.
- [x] C04 — Default mode/pace can be adult-managed without overriding explicit active child choice.
- [x] C05 — Help/Replay remains child-controlled.
- [x] C06 — Creative drawing/color/tool choices remain child-owned.
- [x] C07 — Export/share is adult-gated.
- [x] C08 — Bulk/profile/family destructive actions are adult-only.
- [x] C09 — Existing accepted single-artwork Gallery delete semantics are not silently removed by P6.1.
- [x] C10 — Cross-profile data leakage is explicitly prohibited.

## D. Progress visibility

- [x] D01 — Allowed descriptive progress fields are enumerated.
- [x] D02 — Completed lessons/journeys/skills encountered may be shown locally.
- [x] D03 — Resumable work and neutral recent activity may be shown.
- [x] D04 — Grades/accuracy/mastery percentages are prohibited.
- [x] D05 — Rank/leaderboard/XP/streak pressure is prohibited.
- [x] D06 — Permanent ability/talent labels are prohibited.
- [x] D07 — Comparison between children/profiles is prohibited.
- [x] D08 — Hidden numeric ability scoring is prohibited even if not rendered.
- [x] D09 — Neutral preferred/forbidden copy examples are frozen.

## E. Destructive actions and data isolation

- [x] E01 — Every destructive action requires explicit scope.
- [x] E02 — Profile/family-wide deletion requires stronger just-in-time confirmation.
- [x] E03 — Reset profile preferences preserves artwork.
- [x] E04 — Reset adaptive state preserves artwork/session/completion records.
- [x] E05 — Profile A actions may not mutate Profile B data.
- [x] E06 — Partial/corrupt data cannot broaden deletion scope.
- [x] E07 — Process-death/failure behavior must not silently cause cross-profile loss.
- [x] E08 — Post-delete product must not retain stranded references.
- [x] E09 — App-family-wide deletion is distinct from profile deletion.

## F. Export / external navigation

- [x] F01 — No direct ungated child export/share path.
- [x] F02 — Export has adult gate + just-in-time explanation/confirmation.
- [x] F03 — Android Sharesheet/system destination flow is the intended outward boundary.
- [x] F04 — Cancel/failure leaves original local artwork unchanged.
- [x] F05 — No silent background upload/default social destination.
- [x] F06 — Broad storage permission is not acceptable when scoped platform sharing is sufficient.
- [x] F07 — External navigation invalidates Parent Zone session appropriately.

## G. Privacy / network / permissions

- [x] G01 — Parent Zone remains usable in Airplane Mode.
- [x] G02 — No account/cloud dependency for 0.6.
- [x] G03 — No behavioral analytics upload.
- [x] G04 — No advertising identifier/SDK requirement.
- [x] G05 — No external child-profile/raw-artwork upload by default.
- [x] G06 — Any future sensitive permission requires explicit reviewed justification.
- [x] G07 — Safety & Privacy copy must reflect actual implementation behavior.
- [x] G08 — Local-data/uninstall implications must be explained honestly.

## H. Accessibility / device baseline

- [x] H01 — Critical targets aim for at least 48×48 dp effective touch area.
- [x] H02 — Gate/destructive controls require semantics/roles.
- [x] H03 — Critical state cannot depend on color alone.
- [x] H04 — Larger text may reflow/scroll rather than clip.
- [x] H05 — Small screens must scroll instead of hiding critical controls.
- [x] H06 — No precision/multi-finger/shake/rapid-tap gate requirement.
- [x] H07 — Reduced motion will be honored when exposed in P6.4.
- [x] H08 — Tablet/phone layout adaptation cannot change ownership/security semantics.

## I. Architecture boundaries

- [x] I01 — Parent gate/session state is not stored in `ChildProfile` or adaptive teaching state.
- [x] I02 — UI cannot invent gate bypass rules.
- [x] I03 — Scoped mutation belongs behind repositories/services, not ad-hoc UI store access.
- [x] I04 — Parent progress read models cannot write inferred ability state.
- [x] I05 — Export remains read-only-copy semantics.
- [x] I06 — Accepted Drawing/Lesson/Coloring/Gallery/Free Draw/adaptive internals remain frozen absent explicit defect/ADR.

## J. P6.2 implementation readiness

- [x] J01 — P6.2 can implement gate behavior without inventing product policy.
- [x] J02 — P6.2 can build Parent Zone shell without inventing IA.
- [x] J03 — P6.2 can implement local profile administration with frozen ownership/isolation rules.
- [x] J04 — P6.2 knows which adult defaults may be exposed and how active child choice behaves.
- [x] J05 — P6.2 knows what is deliberately deferred to P6.3/P6.4/P6.6.
- [x] J06 — No Phase-5 engine/product invariant needs reopening for P6.1.

## Contract result

**66/66 contract checks PASS.**

P6.1 is specification-complete when this checklist and `P6_1_PARENT_ZONE_FAMILY_CONTROLS_CONTRACT.md` are CI-green, reviewed against existing PRD/UX contracts, merged to `main`, and issue #92 is closed completed.

P6.1 does **not** claim physical product QA because no Parent Zone production UI is implemented in this slice. Physical acceptance begins with P6.2 implementation.