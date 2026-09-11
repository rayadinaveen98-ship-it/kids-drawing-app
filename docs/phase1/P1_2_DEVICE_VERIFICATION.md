# P1.2 DrawingSurface — Physical Device Verification

**Issue:** #9  
**Purpose:** collect the hardware evidence that CI cannot prove for the low-latency DrawingSurface.  
**Status values:** `PASS`, `FAIL`, `PENDING-HARDWARE`, `NOT-APPLICABLE` with rationale.

This checklist does not replace `docs/18_DRAWING_PERFORMANCE_GATES.md`. It is the smaller P1.2 acceptance pass required before Issue #9 can be closed.

## Build under test

Record before testing:
- Git commit SHA:
- CI run ID:
- APK SHA-256:
- versionName / versionCode:

Do not mix observations from different APKs under one result record.

## Device record

For each physical device record:
- model:
- Android version / API:
- RAM if known:
- display refresh rate if known:
- input: finger / active stylus / both:
- stylus model if applicable:
- device class: L / M / S per `docs/18_DRAWING_PERFORMANCE_GATES.md`:

## A. Finger authoring

### A1 — Normal repeated strokes
1. Open Art Lab.
2. Draw at least 100 separate short/medium strokes across the paper.
3. Compare visible strokes with the Art Lab committed-stroke counter.
4. Confirm no normal stroke disappears during wet → committed handoff.

Result: `PENDING-HARDWARE`

### A2 — Rapid continuous input
1. Draw rapidly for at least 30 seconds using curves, corners and direction changes.
2. Watch for visible gaps, flicker, long stalls, duplicated wet/committed strokes or a stuck active stroke.
3. Record the observed handoff metric range shown by Art Lab.

Result: `PENDING-HARDWARE`

## B. Multi-pointer safety

### B1 — Secondary pointer must not draw
1. Start drawing with the primary finger.
2. While the primary stroke is active, touch the surface with a second finger.
3. Move/lift the second finger while continuing the primary stroke.
4. Confirm the second pointer creates no independent mark.

Result: `PENDING-HARDWARE`

### B2 — Primary pointer lifecycle after multitouch
1. Start a primary stroke.
2. Add a second finger.
3. Lift the primary drawing finger before the secondary finger.
4. Confirm the primary stroke terminates cleanly and the surface accepts a fresh new stroke afterward.

Result: `PENDING-HARDWARE`

## C. Cancellation / interruption

Exercise practical interruption paths available on the device while a stroke is active, such as system gesture/interception or other UI interruption that causes Android to cancel the pointer stream.

Confirm:
- no partial stroke becomes a committed owned record after cancellation;
- the next stroke starts normally;
- the Art Lab does not remain in a stuck active-tool state.

Result: `PENDING-HARDWARE`

## D. Viewport/document coordinates

Automated unit tests already verify logical coordinate round trips under portrait/landscape-sized viewports and letterboxing. On device, additionally resize the drawing viewport where the OS/device supports a non-destructive resize path (for example split-screen/window resizing) and confirm existing committed strokes remain aligned with the paper.

Full Activity recreation persistence is intentionally owned by later document/persistence milestones and is not falsely claimed by P1.2.

Result: `PENDING-HARDWARE`

## E. Stylus — only when Class S hardware is available

### E1 — Stylus authoring
- draw at least 50 strokes;
- confirm stable wet → committed handoff;
- confirm finger/palm contacts during an active stylus stroke do not create independent marks under the current single-primary-pointer policy.

Result: `PENDING-HARDWARE`

### E2 — Pressure metadata
- draw very light and firm strokes where hardware supports pressure;
- confirm Art Lab's pressure metric changes meaningfully;
- record representative observed values.

Result: `PENDING-HARDWARE`

### E3 — Tilt/orientation capture
The owned stroke record preserves tilt/orientation values supplied by the platform. Detailed brush-response validation belongs to the later Class S performance/correctness gate.

Result: `PENDING-HARDWARE`

### E4 — Inverted stylus
P1.2 intentionally does **not** convert `TOOL_TYPE_ERASER` into black ink. Product eraser semantics arrive in the owned erase-operation slice. Confirm an inverted stylus does not create a black drawing stroke.

Result: `PENDING-HARDWARE`

## P1.2 close rule

Issue #9 can close only when:
- CI/compiler/lint/unit-test/permission/Ink-boundary gates are green for the exact build under test;
- at least the available finger-device tests above pass on physical hardware;
- stylus-only items are either passed on Class S hardware or explicitly remain `PENDING-HARDWARE` without claiming a differentiated stylus experience;
- failures are fixed or tracked with a concrete follow-up rather than waived informally.
