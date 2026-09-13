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
- [x] product completion now derives coloring availability from authored lesson content, so P4.3 lessons cannot expose unsupported coloring choices;
- [x] `ProductColoringRuntime` also rejects an unsupported coloring start before mutating the Lesson Engine handoff;
- [x] Cute Cat revision 1 is not modified by this slice and retains its existing coloring path.

## Automated gate

`RepresentativeContentSetATest` must load the real `src/main/assets/lessons` catalog and prove:
- no catalog diagnostics;
- five production lessons total (Cute Cat + four P4.3 lessons);
- strict direct package load success for every new lesson;
- required age/mode coverage;
- full Owl Help Ladder;
- Sun trace support;
- Rocket Watch Then Draw/replay contract;
- Flower prerequisite/grouped-demo contract;
- Flower grouped teacher sequence contains every authored stroke and completes/replays at all five pace profiles;
- P4.3 lessons keep coloring disabled while Cute Cat keeps its existing enabled regression path.

Exact-head Android CI must be green before merge.

## Physical/product checks still required before claiming P4.3 complete

On the reference Android device, execute at minimum:
1. Little Artist → Smiling Sun → Trace & Learn from Home recommendation/discovery, including Help and replay.
2. Finish Smiling Sun and confirm the completion UI offers Gallery completion only — no unsupported `Color with me` / `Color myself` controls.
3. Creative/Growing Artist → Friendly Owl → exercise Help levels progressively and complete the drawing; confirm Gallery-only completion.
4. Creative/Growing Artist → Simple Rocket → Watch Then Draw, replay each representative step, then complete; confirm Gallery-only completion.
5. Easy Flower → verify the grouped petal and stem/leaf demonstrations remain legible at Extra Slow, Normal and Very Fast. Functional completion/replay at all five paces is automated; device review is for presentation/legibility.
6. Re-enter Cute Cat and confirm the existing Phase 3 start/resume path still works and still offers its existing coloring choices after drawing completion.
7. Confirm child history diagnostics remain free of teacher/guide ink during representative Trace/Help use.
8. Finish and reopen at least one non-Cute-Cat artwork from Gallery to prove lesson-specific document identity and generic completion routing.

Record results here as PASS/FAIL with device/API and exact tested commit. Until then, do not describe Trace & Learn or Watch Then Draw as physically verified P4.3 content.
