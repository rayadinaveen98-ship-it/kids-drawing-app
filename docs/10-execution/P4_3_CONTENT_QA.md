# P4.3 Representative Content Set A — QA Record

**Issue:** #60  
**Milestone:** Phase 4 / `0.4.0-content-studio`  
**Status:** physical QA round 1 FAILED; fixes implemented; QA2 candidate pending exact-head CI + device retest

## Set under review

| Lesson | Primary proof | Ages | Modes | Journey |
| --- | --- | --- | --- | --- |
| Smiling Sun | tracing-friendly first picture | Little / Creative | Trace & Learn, Draw With Me | First Shapes to Pictures |
| Friendly Owl | full authored Help Ladder | Creative / Growing | Draw With Me | Animal Artist |
| Simple Rocket | remember-then-draw teaching | Creative / Growing | Watch Then Draw | Space Artist |
| Easy Flower | grouped multi-stroke demonstration | Little / Creative / Growing | Draw With Me, Watch Then Draw | First Shapes to Pictures |
| Cute Cat r1 | frozen regression baseline | Creative / Growing | existing three modes | Animal Artist |

## Physical QA round 1 — REJECTED

Candidate:
- commit: `29c718a710481680274a591f99e7f93b04f36dab`;
- version: `0.4.0-content-studio-p4.3` / versionCode 14;
- CI: Android CI #328 / run `34746875883` GREEN;
- profile APK SHA-256: `67b238b8624c7f1cf5c7d5dfa2f97ae5a7497966b9a8e4f1c3c0c31f58c13694`.

Device review found two release-blocking presentation defects:
1. **FAIL — selected-lesson preview identity.** Every lesson preview displayed hard-coded Cute Cat art even though the selected runtime correctly executed the requested lesson.
2. **FAIL — cumulative construction context.** When the next Draw With Me teacher step loaded, the prior demonstrated part disappeared. Example: after the head step, the ears demonstration no longer retained the head as visual construction context.

Round-1 candidate is permanently rejected and must not be used as P4.3 acceptance evidence.

## QA2 fix contract

QA2 changes must prove:
- lesson preview visual comes from the selected lesson package's authored teacher geometry, with no Cute Cat singleton artwork path;
- completed earlier teacher parts carry into the next teacher sequence as a subtle reference (`0.16` opacity);
- the current teacher step remains full-strength while animating;
- Replay does not duplicate or progressively darken carried reference strokes;
- Watch Then Draw overview geometry is not carried into later step playback;
- teacher/reference strokes remain `TEACHER_GENERATED` presentation-only data and never enter child artwork/history/persistence;
- child strokes remain authoritative and fully visible;
- remaining Cat-specific generic workspace/completion copy is removed.

QA2 candidate identity:
- versionName: `0.4.0-content-studio-p4.3-qa2`;
- versionCode: 15;
- exact executable commit / CI / APK SHA: record after final exact-head CI succeeds.

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
- [x] P4.3 lessons cannot expose unsupported coloring choices;
- [x] `ProductColoringRuntime` rejects an unsupported coloring start before mutating the Lesson Engine handoff;
- [x] Cute Cat revision 1 retains its existing coloring path;
- [x] preview rendering now derives from selected-package teacher geometry;
- [x] cumulative teacher construction references remain overlay-only.

## Automated gate

Automated tests must prove:
- no catalog diagnostics and five production lessons total;
- strict package loading and required age/mode coverage;
- full Owl Help Ladder, Sun trace support and Rocket Watch Then Draw/replay;
- Flower grouped playback at all five pace profiles;
- preview geometry is selected-package-specific;
- completed teacher steps persist into the next non-overview sequence as faint construction context;
- Replay carries earlier context without duplicate current-step stroke IDs;
- overview geometry never contaminates later step playback;
- teacher/reference overlays remain outside child document/history truth.

Exact-head Android CI must be green before QA2 is installed.

## Physical/product checks required for QA2

On the reference Android device, execute at minimum:
1. Open Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower previews and confirm each preview depicts the selected lesson rather than Cute Cat.
2. Little Artist → Smiling Sun → Trace & Learn, including Help and replay.
3. Finish Smiling Sun and confirm Gallery-only completion — no unsupported coloring controls.
4. Creative/Growing Artist → Friendly Owl → exercise Help levels progressively and complete the drawing.
5. In Draw With Me, confirm cumulative construction: after Head, the head stays visible as a subtle reference while Ears are demonstrated; after Ears, earlier parts remain while the next part is taught.
6. Confirm carried reference is visibly lighter than the active teacher step and never replaces/hides the child's own strokes.
7. Creative/Growing Artist → Simple Rocket → Watch Then Draw; confirm the full overview does not remain as an always-visible tracing template.
8. Easy Flower → verify grouped demonstrations at Extra Slow, Normal and Very Fast.
9. Re-enter Cute Cat and confirm Phase 3 start/resume + coloring regression remains correct.
10. Confirm child history diagnostics remain free of teacher/guide/reference ink.
11. Finish and reopen at least one non-Cute-Cat artwork from Gallery.

Record results here as PASS/FAIL with device/API, exact tested commit, APK artifact name, size and SHA-256. Until the QA2 matrix passes, do not describe P4.3 as physically verified or merge PR #67.
