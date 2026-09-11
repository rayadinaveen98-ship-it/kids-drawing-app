# 23 — Parent Gate & Adult-Only Action Contract

**Status:** Phase 0.7 implementation contract

## 1. Purpose

The Parent Gate separates child creative flows from adult-only settings and outward actions.

It is a friction boundary, not an identity system and not a substitute for legal parental consent when law requires verifiable parental consent for data collection.

## 2. Actions requiring the gate in V1

Required before:
- entering Parent Zone;
- exporting/sharing artwork outside the app;
- opening external web/email destinations;
- changing privacy/safety settings;
- deleting all child/profile data;
- future purchase/subscription management;
- future cloud/account enablement.

Normal child actions do **not** require the gate:
- drawing;
- coloring;
- Free Draw;
- browsing child-safe lessons;
- opening local Gallery artwork;
- deleting one artwork only if the product chooses a child-safe confirmation flow and usability testing supports it. Parent-only deletion remains the safer default for younger bands.

## 3. Gate design principle

The gate should be easy for a normal adult and resistant to accidental/rote child taps.

Do not use:
- a tiny hidden hotspot;
- a fixed 4-digit code printed on screen;
- a trivial “Are you a parent? Yes” button;
- a challenge dependent on private personal information;
- CAPTCHAs that send unnecessary child/device data to third parties.

## 4. V1 gate mechanism

Recommended V1 local gate:

1. Adult taps Parent Zone / outward action.
2. Calm screen explains: **“Grown-up check”** and why the action is protected.
3. Generate one randomized adult-readable instruction/challenge from a local set.
4. Require completion before continuing.

Example challenge families:
- “Hold the two marked shapes for 2 seconds.”
- “Drag the paintbrush to the palette, then tap Continue.”
- a simple randomized arithmetic prompt intended for an adult, with numbers/wording chosen so it is not a reusable fixed answer.

The exact V1 mechanism must be usability-tested with adults and children before public release.

For initial implementation, prefer a randomized local multi-step interaction over external age-verification services because no server/data collection is required.

## 5. Security expectations

The Parent Gate is not designed to resist a determined older child with unlimited observation. Its purpose is to prevent accidental child access and casual bypass.

Higher-risk future actions such as account creation, paid purchases or legally significant parental consent may require platform authentication or a separate verifiable-consent flow.

Never describe the basic Parent Gate as COPPA verifiable parental consent.

## 6. Session behavior

After successful gate completion:
- unlock the specific adult context for a short in-app session;
- do not make the child-facing shell globally unlocked indefinitely;
- leaving Parent Zone returns to protected child context;
- app restart clears gate-unlocked state;
- device/process recreation should default toward locked unless an active protected operation must safely complete.

Suggested V1 adult-session timeout: short and configurable, e.g. several minutes of active Parent Zone use. Exact duration is a product constant tested later.

## 7. Export/share flow

Artwork export is parent-gated because it transfers child-created content outside the protected app environment.

Order:
1. child/adult selects export/share from Artwork Detail;
2. Parent Gate if not already in an active adult context;
3. show export preview and plain-language destination warning;
4. user intentionally invokes Android Sharesheet / user-selected save destination;
5. app shares only the selected derivative/file;
6. no automatic contact/social upload.

The app does not preselect social-network recipients.

## 8. External links

After gate success, external links should:
- clearly name destination/purpose;
- use normal system browser/email intent;
- not embed unrestricted web browsing inside child shell;
- not pass child nickname/artwork unless the adult explicitly chose an export action requiring it.

## 9. Purchases later

No purchase UI is required for Alpha.

When monetization is added:
- purchase management originates from Parent Zone;
- child companion never pressures payment;
- Google Play Billing/family purchase controls are respected;
- entitlement restoration and parental messaging are designed separately;
- current Play policy is re-reviewed before implementation.

## 10. Privacy settings

Parent Zone can expose:
- child nickname/age band/preferences;
- local storage/artwork management;
- narration/audio settings;
- privacy explanation;
- optional future network/cloud features;
- data deletion/reset;
- app version/licenses.

Changing age band should warn that lesson recommendations/UI complexity may change.

## 11. Data deletion

High-impact deletion actions require:
- active Parent Gate;
- clear scope explanation;
- secondary destructive confirmation;
- no dark patterns.

Examples:
- delete one child profile;
- erase all artwork;
- reset app.

Undo/recovery can be provided where technically feasible, but the UI must not imply deletion happened if local files remain intentionally retained.

## 12. Accessibility

Gate must remain completable by adults with:
- larger font settings;
- reduced motion;
- screen reader where feasible;
- motor limitations.

Do not rely solely on color, tiny drag targets or rapid timed gestures.

Provide an alternate accessible adult verification path if the primary gesture challenge is inaccessible.

## 13. Failure/cancel behavior

- Cancel always returns safely to child context.
- Incorrect attempt gives neutral feedback; no ridicule.
- repeated attempts may vary challenge rather than increasing emotional pressure.
- gate failure never affects artwork/session data.

## 14. Test requirements

Test:
- gate appears for every protected entry point;
- direct deep-link/navigation attempts cannot bypass gate;
- screen rotation/process recreation does not accidentally unlock;
- app restart locks again;
- cancel returns safely;
- successful adult context can perform intended action;
- export sends only selected file;
- accessibility alternative works;
- younger child usability testing measures accidental/bypass rate before public release.

## 15. V1 acceptance gate

Parent Gate contract is satisfied when:
- all adult-only routes are protected through one reusable gate service;
- no child-facing feature needs the gate for normal creative use;
- protected state cannot be enabled by a single obvious child tap;
- gate is entirely local for V1;
- export/external-link actions are deliberate and transparent;
- restart/process recovery defaults safely;
- accessibility path exists;
- gate is not misrepresented as legal verifiable parental consent.