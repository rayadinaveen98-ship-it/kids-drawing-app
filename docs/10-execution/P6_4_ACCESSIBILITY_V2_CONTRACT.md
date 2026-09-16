# P6.4 Accessibility System V2 — Frozen Contract

Status: **FROZEN CONTRACT — IMPLEMENTATION BLOCKED UNTIL CONTRACT CI GREEN**  
Issue: #98  
Parent epic: #91  
Verified baseline: `56a5b99bee65bec8bb6acf687587a6ac7fc62cf7` / Android CI #622 GREEN  
Audit: `docs/10-execution/P6_4_ACCESSIBILITY_V2_AUDIT.md`

## 1. Product goal

Accessibility V2 makes critical child and parent flows more understandable, reachable and robust for larger text, reduced-motion preference, screen readers and non-color-only state while preserving the accepted drawing/lesson/family safety model.

P6.4 is not a claim that freehand drawing or spatial fill becomes fully non-visual. It is a contract to make surrounding navigation, controls, instructions, status and state honest and accessible without pretending the direct-manipulation canvas has capabilities it does not.

## 2. Invariants that P6.4 may not change

1. Parent Gate still requires the accepted 2.5-second continuous hold or the accepted explicit two-confirmation fallback.
2. Reduced motion cannot shorten, bypass or weaken Parent Gate/session timing.
3. Parent session expiry/background/child-return/process-death invalidation stays unchanged.
4. Drawing, lesson, coloring, Gallery, Free Draw and adaptive truth/ownership semantics stay unchanged.
5. No automatic Help, invented Trace, grading/ranking/mastery scoring or ability labels are introduced.
6. 0.6 remains one local child profile.
7. Core remains offline-first, account-free and ad-free.
8. No new Android permission, cloud sync or behavioral analytics upload.
9. Critical effective touch targets remain >=48×48dp.
10. Critical state may not rely on color alone.

## 3. Accessibility preference model

P6.4 adds one real persisted app-level accessibility preference:

`reduceMotion: Boolean`

Rules:
- stored locally on-device outside `ChildProfile` because it is an app/UI accessibility preference, not child ability/profile truth;
- default `false` for backward compatibility;
- missing/unreadable preference fails safely to default behavior without mutating child/artwork/session data;
- contains no artwork, strokes, learning history, account/device identifier or analytics data;
- Parent Zone → Accessibility & Audio exposes the real setting;
- save is immediate and local because it is a simple UI preference, not a destructive child-data action;
- narration preference remains existing Family/profile behavior and is not duplicated as a second conflicting store.

P6.4 does **not** add a custom text-size slider. Android/system font scale is the authoritative text-size input.

## 4. Reduced-motion semantics

### 4.1 Parent Gate
Normal motion:
- existing visual hold-progress sweep may remain.

Reduced motion:
- remove the animated sweep;
- use static/textual hold state such as `Keep holding…` plus the existing duration instruction;
- still wait the same `ParentAccessSession.HOLD_DURATION_MILLIS` and call the same eligibility policy;
- release before eligibility still cancels;
- accessible confirmation path remains unchanged.

### 4.2 Decorative/transitional motion
Any P6.4-added decorative/transitional animation must consult the same preference and provide an immediate/static alternative.

### 4.3 Instructional teacher playback
Teacher demonstration is authored lesson content, not decorative UI motion. P6.4 does not silently skip, accelerate or rewrite teacher-session timing/state-machine semantics when reduced motion is enabled.

If a future product phase wants a true static teacher alternative, that requires a teaching-contract change with its own acceptance tests.

## 5. Font-scale / reflow policy

P6.4 introduces a pure deterministic layout policy derived from system `fontScale`.

Acceptance bands:
- standard: `< 1.30`;
- large text: `>= 1.30`;
- extra-large text: `>= 1.60`.

These are layout breakpoints, not user grades or profiles.

Required behavior at large/extra-large text:
- side-by-side action cards that can clip must stack vertically;
- action grids reduce columns where required for readable labels;
- critical labels/instructions must wrap rather than disappear behind `maxLines` truncation;
- top bars may reflow/stack while preserving action meaning;
- screens remain scrollable where total content grows;
- critical actions stay reachable without precision gestures;
- canvas size may shrink to preserve controls, but controls may not overlap the canvas or system insets.

Specific target surfaces:
- Home Free Draw/Gallery secondary actions;
- Home recommendation/supporting copy;
- onboarding handedness/choice layouts;
- Parent Gate/Zone/Learning;
- guided lesson action grid + post-drawing choices;
- coloring top bar/tools/actions;
- Free Draw top bar/tool/color/size controls;
- Gallery grid/detail/completion screens.

## 6. Shared selectable-state semantics

`StudioChoiceCard` or its replacement must distinguish **navigation** from **selectable** use.

Selectable controls must expose:
- selected/not-selected semantic state;
- an appropriate role/state description where practical;
- a visible non-color-only selected cue (`✓`, `Selected`, or equivalent);
- text that remains understandable without relying on border/fill color.

Multi-select interests must remain semantically distinguishable from single-choice age/mode/pace/handedness/narration controls.

Navigation cards must not announce a meaningless `Not selected` state.

## 7. Tool/palette semantics

### 7.1 Drawing tools
Pencil, eraser, brush, fill, size and related controls must expose their current selected/enabled state in text/semantics, not only style.

### 7.2 Color palettes
Palette controls must expose human-readable color names plus selected state.

For the existing six-color teaching palette, expected names are equivalent to:
- coral/red;
- orange;
- yellow;
- green;
- blue;
- purple.

Free Draw palette values must use deterministic human-readable names; unknown future values may fall back to a stable `Custom color` description rather than raw meaningless `Drawing color`.

Selected color must have a non-color-only visual cue in addition to border/fill styling.

## 8. Canvas semantics and honesty boundary

`DrawingSurface` gains a product-owned optional semantic description/state boundary. Callers provide context-specific text such as:
- `Drawing canvas. Touch and drag to draw.`
- `Coloring canvas. Touch and drag with the selected tool.`
- `Saved artwork preview. Read only.`

Rules:
- semantics never mutate artwork;
- no AndroidX Ink type leaks into product UI contracts;
- read-only Gallery canvas must be announced as read-only;
- teacher overlay remains presentation-only;
- P6.4 does not claim that TalkBack can create arbitrary freehand geometry or locate spatial fill regions non-visually;
- surrounding controls/instructions/status must remain navigable even when the direct canvas interaction itself is not equivalent under touch exploration.

## 9. Screen-reader status and focus contract

Major screens use natural visual/top-to-bottom Compose traversal unless a concrete defect requires custom traversal.

Required semantic/status behavior:
- primary actions have understandable names/roles;
- selectable/toggled controls expose current state;
- meaningful async outcomes (save failure, recovery message, completed save, temporarily unavailable data) are represented in text and may use polite live-region semantics where this avoids silent state changes;
- do not announce animation frames, every teacher stroke, or other noisy transient updates;
- progress indicators must have adjacent text/state meaning when progress is important;
- dialogs remain modal and use their existing explicit action labels;
- no essential state is encoded only by an icon/color.

## 10. Contrast contract

P6.4 uses an internal accessibility acceptance target modeled on WCAG contrast guidance; it is **not a certification claim**.

Targets:
- active normal text: >=4.5:1 against its background;
- large/bold text where applicable: >=3:1;
- critical non-text boundaries/focus/state indicators: >=3:1 where color carries information;
- disabled/decorative content is reviewed separately but may not make enabled/disabled state ambiguous.

Known low-contrast active normal-text usages using `Ink500`/`Studio500` must move to an accepted stronger semantic color (normally `Ink700/Ink600` or `Studio600`) unless measurement proves the specific usage meets target.

Do not change the art/drawing palette merely to satisfy text contrast; text/control labeling carries the accessibility semantics for color choices.

## 11. Parent Accessibility & Audio surface

P6.4 replaces the placeholder-only accessibility section with a truthful production surface containing:
- `Reduce motion` real switch/control;
- explanation that it simplifies UI motion without weakening Parent Gate timing;
- current narration default shown as information, with Family remaining the owner of that profile setting;
- concise note that system text size is respected automatically;
- honest direct-touch drawing limitation language if needed.

No fake high-contrast switch, screen-reader switch or text-size slider is added.

## 12. Handedness decision

Current major drawing controls are centered/above/below canvas and no broad side-specific obstruction was found.

P6.4 therefore:
- preserves stored handedness;
- keeps current neutral control layout unless a concrete side-specific obstacle is found during implementation/physical QA;
- updates onboarding/parent copy so it does not promise an unimplemented mirrored toolbar;
- never mirrors Back/navigation/Parent Gate/security semantics.

A future side-toolbar design must explicitly consume handedness and be tested separately.

## 13. Critical-flow implementation scope

P6.4 implementation must cover, at minimum:
1. shared accessibility preference store/policy;
2. Parent Accessibility & Audio production control;
3. Parent Gate reduced-motion visual path;
4. shared selection semantics/non-color cue in choice cards;
5. Home large-text reflow + low-contrast active text review;
6. onboarding selection semantics/reflow + truthful handedness copy;
7. Guided Lesson canvas semantics, large-text action reflow and important status semantics;
8. Coloring canvas/palette/tool semantics + reflow;
9. Free Draw canvas/palette/tool semantics + reflow;
10. Gallery read-only canvas semantics + large-text/reflow review;
11. targeted contrast corrections on the inspected critical paths.

## 14. Automated acceptance expectations

Before any QA version bump:
- accessibility preference store/default/round-trip tests;
- reduced-motion Parent Gate policy/UI coverage proving timing is unchanged;
- pure font-scale layout-policy unit tests at standard/large/extra-large bands;
- shared selection semantics Compose tests;
- palette naming/selected-state tests where practical;
- Parent Accessibility control Compose/instrumentation compile coverage;
- Home/critical route semantics/layout coverage where practical;
- all existing unit/lint/debug/instrumentation/profile compilation stays green;
- frozen curriculum remains 24/0/6;
- permission allowlist remains unchanged;
- versionCode remains 29 until full P6.4 implementation is automated-green.

## 15. Physical QA expectations

The eventual monotonic P6.4 QA APK must verify on a real device:
- upgrade preservation from v29;
- normal and reduced-motion Parent Gate behavior with identical hold threshold;
- accessibility preference persistence/relaunch;
- large and extra-large system font behavior on critical screens;
- screen-reader labels/state on Parent Zone, Home, onboarding, lesson tools, coloring/free-draw tools and Gallery;
- non-color-only selected states;
- contrast/readability on inspected text/control states;
- Airplane Mode;
- child-art regression smoke;
- no safety/ownership/session regression.

Tester device/API must be recorded only if actually provided.

## 16. Non-goals

- claiming formal WCAG certification;
- making freehand geometry creation fully non-visual;
- changing lesson teaching state machines to skip authored teacher playback;
- P6.5 device/performance stress matrix;
- P6.6 destructive family data/recovery controls;
- multi-profile migration;
- cloud accessibility-profile sync;
- visual redesign unrelated to accessibility defects.

## 17. Release discipline

- contract/audit work does not bump versionCode;
- implementation stabilization stays versionCode **29**;
- reserve versionCode >29 only after the complete P6.4 implementation is automated-green;
- exact debug/profile APK IDs, sizes and SHA256 evidence are required before physical QA;
- any executable change after physical acceptance requires a new monotonic versionCode and fresh QA;
- PR remains draft until physical acceptance;
- acceptance-doc CI → squash merge → merged-main CI → close #98.
