# P6.4 Accessibility System V2 — Source Audit

Status: **FROZEN AUDIT INPUT**  
Issue: #98  
Parent epic: #91  
Verified branch baseline: `56a5b99bee65bec8bb6acf687587a6ac7fc62cf7` / Android CI #622 GREEN

## Purpose

This audit records the accessibility-relevant behavior that actually exists before P6.4 production changes. The contract must improve real gaps without falsely claiming that a direct-touch drawing product becomes fully non-visual or without changing accepted safety/learning/artwork semantics.

## Sources inspected

- `StudioTheme.kt`
- `StudioHomeScreen.kt`
- `OnboardingFlow.kt`
- `ParentZoneScreen.kt`
- `ParentProgressScreen.kt`
- `GuidedLessonScreen.kt`
- `DrawingSurface.kt`
- `TeacherPlaybackOverlay.kt`
- `ColoringWorkspaceScreen.kt`
- `FreeDrawScreen.kt`
- `GalleryScreens.kt`
- accepted P6.1/P6.2/P6.3 contracts and physical QA evidence

## 1. Existing strong foundation

1. Shared typography is defined in scalable `sp` units.
2. Age-aware design metrics already provide effective touch targets well above the 48dp baseline: roughly 54–68dp depending on age band/context.
3. Parent Gate, Parent Zone, onboarding, Parent Learning and several child flows already scroll/reflow instead of relying on fixed-height screens.
4. Parent Gate already has an accessible explicit two-step alternative to the timed hold.
5. Save/leave controls in guided lesson/coloring already provide descriptive semantics.
6. Guided lesson exposes a semantic message while drawing is paused for teacher demonstration.
7. Guided lesson pace control exposes current and next speed in its content description.
8. Gallery preview images include an artwork-specific content description.
9. Destructive Gallery/Free Draw operations already use explicit dialogs; P6.4 does not reopen their ownership rules.
10. Pencil/Eraser and coloring tool labels already include a visible `✓` selected cue in some workspaces.

## 2. Central accessibility architecture gap

There is no shared accessibility preference/policy layer today. In particular:
- no persisted reduced-motion preference exists;
- no common font-scale layout policy exists;
- no common selection-state semantics helper exists;
- no shared color-name semantics exists for palette controls;
- no systematic live-region/status announcement helper exists.

P6.4 should add a narrow, local, deterministic UI-accessibility layer rather than a telemetry/profile system.

## 3. Reduced motion audit

### Parent Gate
`ParentGateScreen` uses an `Animatable` plus linear `tween` over the same 2.5-second interval required by `ParentAccessSession`.

Contract implication:
- reduced motion may remove the animated progress sweep and use static/textual hold feedback;
- it **must not** shorten, bypass or change the 2.5-second adult-intent timing;
- accessible two-step confirmation remains available independently of reduced motion.

### Teacher playback
Teacher drawing playback is authored instructional content, not decorative transition motion. Automatically skipping/speeding it would alter accepted teaching behavior.

Contract implication:
- P6.4 reduced motion does not silently rewrite teacher playback timing or lesson state-machine semantics;
- decorative/transitional UI motion is reduced;
- instructional playback remains explicit content unless a future teaching-contract change defines a true static lesson alternative.

## 4. Contrast audit

Using the locked palette:
- `Ink900` on `Paper50` ≈ **15.2:1**;
- `Ink700` on `Paper100` ≈ **8.3:1**;
- `Ink500` on `Paper100` ≈ **4.34:1**;
- `Studio600` on white ≈ **5.45:1**;
- `Studio500` on white ≈ **4.0:1**.

Therefore palette accessibility cannot be claimed globally. Known ordinary-text usages of `Ink500`/similar low-contrast combinations need targeted correction where normal-text contrast should meet at least 4.5:1. Disabled/decorative states must remain distinguishable but are treated separately from active normal text.

Known review targets include Home secondary metadata/Grown-ups copy, onboarding progress/helper copy, guided-lesson secondary cues/startup copy and coloring startup text.

## 5. Home layout / large-text audit

- Home is scrollable, which is a strong foundation.
- Older-age layouts intentionally place Free Draw + Gallery side-by-side.
- Several supporting text blocks cap content at two lines.
- Current layout decisions are driven mainly by age/width, not system font scale.

Contract implication:
- system font scale is authoritative; P6.4 does not invent a second app text-size slider;
- large font scale must trigger stacked/reflow layouts where side-by-side controls risk clipping;
- critical explanatory text should not be silently truncated when it carries required meaning.

## 6. Onboarding / selection-state audit

- Onboarding itself is vertically scrollable and IME-aware.
- Most choices use shared `StudioChoiceCard`.
- `StudioChoiceCard` visually changes fill/border when selected but does not expose a generic selected-state semantic or visible text/check indicator.
- The same card is also reused as navigation in Parent Zone, so selection semantics cannot simply be applied to every card unconditionally.
- Handedness choices are side-by-side, creating a large-font reflow risk.
- Onboarding copy currently says important tools will be kept away from the drawing hand “when we can.” Current major drawing controls are predominantly below the canvas, and no central handedness-driven runtime layout policy was found.

Contract implication:
- distinguish navigation cards from selectable cards;
- selectable cards expose selected state semantically and visually beyond color/border;
- multi-select interests must remain distinguishable from single-choice selections;
- handedness copy must describe real current support, not imply an unimplemented mirroring system;
- current tool layout is effectively hand-neutral, so no speculative mirroring is required in P6.4.

## 7. Parent Zone / Parent Learning audit

- Parent Gate primary control already has Button role + descriptive hold instruction.
- Parent Gate fallback is an explicit, reachable two-step path.
- Parent Zone/Parent Learning use scrollable surfaces and text-based state.
- `Accessibility & Audio` is intentionally informational today and does not expose fake switches.

Contract implication:
- P6.4 may turn this section into a real reduced-motion control plus truthful narration information;
- accessibility preference must be device-local and not part of child ability/profile scoring;
- parent safety/session boundaries remain unchanged.

## 8. Guided lesson audit

Strengths:
- Save & leave semantics exist;
- paused teacher overlay has a semantic explanation;
- pace control has useful dynamic description;
- tool selection includes visible `✓` for Pencil/Eraser.

Gaps:
- `DrawingSurface` itself has no shared semantic description;
- teacher progress/companion changes have no systematic state/live-region policy;
- action-grid columns are chosen by width, not font scale;
- post-drawing Color With Me / Color Myself actions remain side-by-side regardless of font scale;
- some secondary active text uses `Ink500`.

## 9. Coloring audit

Strengths:
- Save & leave semantics exist;
- fill overlay describes the tap-to-fill interaction;
- color palette currently says `Color N, selected` when selected;
- Brush/Fill/Eraser use visible `✓` selected labels.

Gaps:
- numbered color descriptions are less useful than human-readable color names;
- color selection is mainly border-based visually;
- drawing canvas shares the same missing generic semantic boundary;
- Fill is inherently spatial/direct-touch. P6.4 must improve instructions/state but must **not falsely claim equivalent non-visual operation** where the interaction requires locating a prepared drawing region.

## 10. Free Draw audit

Strengths:
- tool tray scrolls;
- critical actions use large age-aware targets;
- clear operation requires confirmation;
- selected tool/button style is visually distinguishable.

Gaps:
- every color swatch currently announces only `Drawing color`;
- selected swatch is indicated by border only and lacks selected semantics;
- top bar contains Save & leave, title/subtitle and Save to Gallery in one row, which risks clipping at large font scales;
- tool/color/size rows are column-count driven without a shared font-scale policy;
- direct drawing canvas has no generic semantic description.

## 11. Gallery audit

Strengths:
- Gallery is a scrollable adaptive grid;
- preview images have artwork-specific descriptions;
- delete uses an explicit dialog.

Gaps:
- read-only artwork canvas has no semantic label/state;
- adaptive grid min cell size is width-driven, not explicitly font-scale-aware;
- loading/error status does not use a shared announcement policy.

## 12. Direct-touch canvas boundary

`DrawingSurface` wraps an Android View and is the low-latency direct-touch drawing surface. P6.4 should add meaningful canvas role/description and ensure surrounding controls/instructions are understandable.

However:
- freehand drawing and spatial fill remain direct-manipulation creative interactions;
- P6.4 does not claim full non-visual equivalence for drawing geometry;
- screen-reader support must not hijack or mutate stroke/artwork truth;
- this limitation must be documented honestly in Parent Accessibility copy/QA rather than hidden.

## 13. Left-handed audit decision

The stored handedness preference remains useful profile context, but the inspected core workspaces place controls mainly above/below the canvas rather than as persistent right-edge overlays. No broad mirror operation is justified by current evidence.

P6.4 decision input:
- preserve handedness data;
- keep neutral centered/below-canvas controls unless a concrete side-specific obstacle exists;
- make onboarding/parent copy truthful about current neutral layout;
- do not mirror Back/navigation/security semantics.

## 14. Privacy / architecture invariants

Accessibility V2 must add no:
- account;
- cloud sync;
- analytics upload;
- behavioral child profiling;
- new Android permission;
- child ability classification;
- multi-profile migration.

A reduced-motion preference, if persisted, is app-local UI configuration only and must contain no artwork/session/learning data.

## Audit conclusion

P6.4 is a **targeted system-quality phase**, not a visual redesign. The highest-value changes are:
1. local reduced-motion policy + real Parent Zone control;
2. shared font-scale/reflow policy;
3. explicit selection/state semantics + non-color-only cues;
4. human-readable palette semantics;
5. canvas/status descriptions without overclaiming non-visual drawing equivalence;
6. targeted contrast fixes;
7. truthful handedness messaging;
8. focused Compose/JVM accessibility tests plus exact physical QA.
