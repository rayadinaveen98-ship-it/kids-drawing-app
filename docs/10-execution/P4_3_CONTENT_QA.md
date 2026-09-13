# P4.3 Representative Content Set A — QA Record

**Issue:** #60  
**Milestone:** Phase 4 / `0.4.0-content-studio`  
**Status:** automated/content review in progress; physical product checks not yet claimed

## Set under review

| Lesson | Primary proof | Ages | Modes | Journey |
| --- | --- | --- | --- | --- |
| Smiling Sun | tracing-friendly first picture | Little / Creative | Trace & Learn, Draw With Me | First Shapes to Pictures |
| Friendly Owl | full authored Help Ladder | Creative / Growing | Draw With Me | Animal Artist |
| Simple Rocket | remember-then-draw teaching | Creative / Growing | Watch Then Draw | Space Artist |
| Easy Flower | grouped multi-stroke demonstration | Little / Creative / Growing | Draw With Me, Watch Then Draw | First Shapes to Pictures |
| Cute Cat r1 | frozen regression baseline | Creative / Growing | existing three modes | Animal Artist |

## Pre-CI authoring review

- [x] all four new lesson IDs/revisions are stable and unique;
- [x] all geometry remains inside the 1000×1000 logical canvas;
- [x] authored point timestamps are monotonic;
- [x] all teacher, expected-stroke and guide references resolve within each package;
- [x] every declared thumbnail/preview/default-localization asset is present;
- [x] child-facing strings are concise, patient and non-punitive;
- [x] Smiling Sun has explicit trace support on every step;
- [x] Friendly Owl has authored Help levels 1–5 on every step;
- [x] Simple Rocket uses Watch Then Draw with replayable child turns;
- [x] Easy Flower uses multi-stroke grouped demonstrations and declares Smiling Sun as prerequisite;
- [x] no new lesson enables prepared-region coloring before P4.5;
- [x] Cute Cat revision 1 is not modified by this slice.

## Automated gate

`RepresentativeContentSetATest` must load the real `src/main/assets/lessons` catalog and prove:
- no catalog diagnostics;
- five production lessons total (Cute Cat + four P4.3 lessons);
- strict direct package load success for every new lesson;
- required age/mode coverage;
- full Owl Help Ladder;
- Sun trace support;
- Rocket Watch Then Draw/replay contract;
- Flower prerequisite/grouped-demo contract.

Exact-head Android CI must be green before merge.

## Physical/product checks still required before claiming P4.3 complete

On the reference Android device, execute at minimum:
1. Little Artist → Smiling Sun → Trace & Learn from Home recommendation/discovery, including Help and replay.
2. Creative/Growing Artist → Friendly Owl → exercise Help levels progressively and complete the drawing.
3. Creative/Growing Artist → Simple Rocket → Watch Then Draw, replay each representative step, then complete.
4. Easy Flower → verify the grouped petal and stem/leaf demonstrations remain legible at Extra Slow, Normal and Very Fast; spot-check the other two paces unless an automated playback matrix already proves them.
5. Re-enter Cute Cat and confirm the existing Phase 3 start/resume path still works.
6. Confirm child history diagnostics remain free of teacher/guide ink during representative Trace/Help use.

Record results here as PASS/FAIL with device/API and exact tested commit. Until then, do not describe Trace & Learn or Watch Then Draw as physically verified P4.3 content.
