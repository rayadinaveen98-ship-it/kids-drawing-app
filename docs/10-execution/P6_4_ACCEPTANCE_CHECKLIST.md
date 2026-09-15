# P6.4 Acceptance Checklist — Accessibility System V2

Status: **CONTRACT AUDIT — 74/74 SPEC CHECKS PASS**  
Issue: #98  
Verified baseline: `56a5b99bee65bec8bb6acf687587a6ac7fc62cf7` / Android CI #622 GREEN

This checklist validates the frozen P6.4 audit/contract before production implementation. Runtime and physical rows will be created only after implementation is automated-green and a monotonic QA APK exists.

## A. Existing foundation / scope integrity — 8/8

- [x] **A01** Existing age-aware effective touch targets above 48dp are preserved.
- [x] **A02** Existing scalable `sp` typography is recognized as foundation rather than replaced with fixed pixels.
- [x] **A03** P6.4 is defined as targeted accessibility hardening, not visual redesign for its own sake.
- [x] **A04** Parent Gate/session timing and ownership rules remain frozen.
- [x] **A05** Drawing/Lesson/Coloring/Gallery/Free Draw/adaptive truth semantics remain frozen absent explicit defect amendment.
- [x] **A06** 0.6 remains single-profile.
- [x] **A07** Offline/account-free/ad-free architecture remains authoritative.
- [x] **A08** Direct-touch drawing limitations are documented honestly rather than hidden behind a false full-accessibility claim.

## B. Semantics / state / screen-reader contract — 12/12

- [x] **A09** Navigation cards and selectable cards are semantically distinguished.
- [x] **A10** Single-choice selections expose selected/not-selected state.
- [x] **A11** Multi-select interests remain distinguishable from single-choice selections.
- [x] **A12** Selected state has a non-color-only visible cue.
- [x] **A13** Tool controls expose current selected/enabled state in text/semantics.
- [x] **A14** Color controls expose human-readable color names.
- [x] **A15** Color controls expose selected state semantically.
- [x] **A16** Drawing/coloring canvases receive context-specific semantic descriptions.
- [x] **A17** Gallery artwork canvas is explicitly announced as read-only.
- [x] **A18** Meaningful asynchronous outcomes may use polite live-region semantics while noisy animation frames are excluded.
- [x] **A19** Important progress/state remains available in text rather than color/icon alone.
- [x] **A20** Natural top-to-bottom traversal is preferred; custom focus ordering is introduced only for a concrete defect.

## C. Reduced motion / timing safety — 10/10

- [x] **A21** A real device-local `reduceMotion` preference is defined.
- [x] **A22** Reduced motion is stored outside `ChildProfile` and contains no child learning/artwork data.
- [x] **A23** Missing/unreadable preference safely falls back without mutating product truth.
- [x] **A24** Parent Accessibility & Audio exposes the real reduced-motion control.
- [x] **A25** Reduced motion removes/simplifies Parent Gate visual motion only.
- [x] **A26** Parent Gate still requires the identical 2.5-second eligibility threshold.
- [x] **A27** Early hold release still cancels under reduced motion.
- [x] **A28** Accessible two-step Parent Gate fallback remains unchanged.
- [x] **A29** Decorative/transitional motion added in P6.4 has a static/immediate alternative.
- [x] **A30** Authored teacher demonstration is not silently skipped/accelerated as decorative motion.

## D. Font scale / layout / reachability — 10/10

- [x] **A31** Android/system font scale is authoritative; no duplicate app text-size slider is added.
- [x] **A32** Pure layout policy defines standard `<1.30`, large `>=1.30`, extra-large `>=1.60` bands.
- [x] **A33** Home secondary actions stack/reflow at large text where needed.
- [x] **A34** Required supporting text is not silently truncated at large text.
- [x] **A35** Onboarding choice/handedness layouts reflow when side-by-side presentation risks clipping.
- [x] **A36** Guided lesson action grids reduce columns at large text.
- [x] **A37** Guided post-drawing choices reflow at large text.
- [x] **A38** Coloring/Free Draw top bars and control rows may stack/reflow instead of clipping.
- [x] **A39** Gallery grid/detail/completion remain readable/reachable at large text.
- [x] **A40** Critical screens remain scrollable/reachable without precision gestures at extra-large text.

## E. Contrast / non-color-only state — 10/10

- [x] **A41** Contract does not claim the whole palette is universally accessible.
- [x] **A42** Active normal-text target is >=4.5:1.
- [x] **A43** Large/bold text target where applicable is >=3:1.
- [x] **A44** Critical non-text state/boundary target is >=3:1 where color carries information.
- [x] **A45** Known active `Ink500` low-contrast usages are targeted for stronger semantic colors.
- [x] **A46** Known active `Studio500` normal-text usages require measurement/fix before acceptance.
- [x] **A47** Disabled/decorative content is reviewed separately without making enabled state ambiguous.
- [x] **A48** Palette colors themselves are not changed merely to satisfy text contrast.
- [x] **A49** Color-choice meaning is carried by text/semantics in addition to swatch color.
- [x] **A50** Selected/toggled/current state cannot rely only on fill/border hue.

## F. Canvas / tool / handedness boundary — 8/8

- [x] **A51** `DrawingSurface` keeps AndroidX Ink behind the existing product-owned boundary.
- [x] **A52** Canvas semantics cannot mutate strokes/artwork/session truth.
- [x] **A53** Freehand geometry is explicitly treated as direct-touch creative interaction, not falsely claimed as full non-visual operation.
- [x] **A54** Spatial Fill gets clearer semantics/status without falsely claiming non-visual region targeting.
- [x] **A55** Teacher overlay remains presentation-only and does not acquire ownership semantics.
- [x] **A56** Existing handedness data is preserved.
- [x] **A57** No broad toolbar mirroring is required without a real side-specific obstruction.
- [x] **A58** Onboarding/parent handedness copy must describe current neutral support truthfully.

## G. Architecture / privacy / settings — 8/8

- [x] **A59** Reduced-motion preference is local-only app UI configuration.
- [x] **A60** No new account is introduced.
- [x] **A61** No cloud sync dependency is introduced.
- [x] **A62** No behavioral analytics upload is introduced.
- [x] **A63** No new Android permission is introduced.
- [x] **A64** No child ability/profile scoring is introduced.
- [x] **A65** Narration remains owned by the existing Family/profile setting rather than duplicated.
- [x] **A66** Accessibility & Audio exposes only implemented controls; no fake high-contrast/screen-reader/text-size switches.

## H. Regression / release discipline — 8/8

- [x] **A67** Contract/audit work does not bump versionCode.
- [x] **A68** Implementation stabilization remains versionCode 29.
- [x] **A69** VersionCode >29 is reserved only after full P6.4 implementation is automated-green.
- [x] **A70** Full existing unit/lint/debug/instrumentation/profile build gate remains required.
- [x] **A71** Frozen curriculum quality remains 24 lessons / 0 errors / exactly 6 reviewed warnings absent separately approved content work.
- [x] **A72** Android permission allowlist remains required.
- [x] **A73** Exact debug/profile artifacts, sizes, SHA256 and focused physical accessibility QA are required before merge.
- [x] **A74** P6.4 closes only after acceptance-doc CI, squash merge and merged-main CI are green.

## Contract audit result

- Foundation / scope integrity: **8/8**
- Semantics / state / screen-reader: **12/12**
- Reduced motion / timing safety: **10/10**
- Font scale / layout / reachability: **10/10**
- Contrast / non-color-only state: **10/10**
- Canvas / tool / handedness: **8/8**
- Architecture / privacy / settings: **8/8**
- Regression / release discipline: **8/8**
- **Total: 74/74 PASS**

## Implementation handoff after contract CI GREEN

1. add local accessibility preference store/model and pure font-scale layout policy;
2. replace Parent Accessibility placeholder with real reduced-motion control;
3. harden Parent Gate reduced-motion visuals without touching timing policy;
4. add shared selection semantics/non-color cue + color naming semantics;
5. apply targeted Home/onboarding/lesson/coloring/Free Draw/Gallery reflow/semantics/contrast fixes;
6. add focused JVM/Compose coverage;
7. run full regression CI while remaining versionCode 29;
8. only then reserve the monotonic P6.4 QA version.
