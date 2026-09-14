# P5.3 — Companion / Teacher Experience V2 Execution Contract

**Parent epic:** #73  
**Issue:** #78  
**Branch:** `phase5/p5-3-companion-teacher-v2`  
**Base:** verified P5.2 squash merge `c0e4c3708fd94102773d6438cbf401162815a9fc`, merged-main CI #482 GREEN  
**Target:** Phase-5 intermediate QA; first distributed candidate versionCode 22

## 1. Purpose

P5.3 upgrades the proven lesson companion from mostly static generic copy into a clearer, age-appropriate, context-aware patient art teacher.

This slice changes **presentation**, not teaching truth. `LessonSessionState`, validated lesson content, teacher playback, Help Ladder, child drawing document and persistence remain authoritative.

## 2. Non-negotiable architecture boundary

The companion/presentation layer may **derive** UI from:
- current `LessonSessionState`;
- validated `LessonRuntimePackage`;
- product profile `com.navin.kidsdrawing.product.profile.AgeBand`;
- current teaching mode/pace already contained in session context;
- authored current-step metadata such as completion policy, replay/skip/help availability and expected-stroke presence;
- lesson difficulty and generic skill/category metadata when useful for wording.

It must never:
- own or mutate progress;
- advance a step;
- change Help level;
- infer completion;
- write artwork/history;
- mutate lesson/coloring/Free Draw/Gallery persistence;
- score geometry or quality;
- create a second session state machine;
- branch on lesson ID.

Existing session commands remain the only path for Pause/Resume/Replay/Help/Less Help/Hide Help/Skip/Done/Retry.

## 3. Age-band source

Companion tone uses the **product profile age band** (`product.profile.AgeBand`):
- `LITTLE_ARTIST` — ages 4–5;
- `CREATIVE_EXPLORER` — ages 6–7;
- `GROWING_ARTIST` — ages 8–9;
- `YOUNG_ARTIST` — ages 10–12.

Do not confuse this with `lesson.model.AgeBand`, which belongs to package eligibility metadata. Package age coverage does not replace the child profile's selected age band.

## 4. Presentation model V2

`LessonWorkspacePresentation` remains presentation-only. P5.3 may extend it with fields such as:
- `turnLabel` / accessibility cue;
- optional `secondaryCue`;
- optional `reflectionPrompt`;
- presentation emphasis/tone token if required by Compose.

The existing control-visibility fields remain derived from the same session/step contracts. Any deliberate behavior change to a control requires a test proving the engine contract remains authoritative.

## 5. Turn-taking contract

Every major state must communicate what is happening **now**:

### Ready / introducing
- calm start;
- no urgency;
- age-appropriate wording.

### Watch Then Draw overview
- explicitly communicates that the child is watching the whole drawing first;
- remains distinct from a per-step demonstration;
- `I’m ready` remains governed by existing overview skip contract.

### Teacher demonstration / preparing step
- identifies teacher turn;
- asks child to observe a relevant generic cue, never to judge correctness;
- canvas input remains blocked exactly as before.

### Child turn
- clearly identifies child turn;
- may mention observation, shape/placement, or personal choice based on generic step semantics;
- manual/open-ended steps with no required expected strokes must emphasize authorship rather than copying.

### Help active
- Help Ladder remains authored content/geometry truth;
- companion explains the current amount of support without implying failure;
- increasing Help is always child-requested through existing command;
- Less Help / Hide Help remain available under existing contract;
- tracing is normalized as a legitimate learning tool.

### Paused / recovery / error
- artwork safety language may be used only where persistence contract supports it;
- no blame/failure language.

### Completion
- celebrate authorship/effort/process, not accuracy;
- no “perfect”, “correct”, talent claims, grades, stars, XP, streaks or ranks;
- optional non-blocking reflection/creative-extension prompt may be present;
- Save/Color/Finish actions must remain primary and reachable.

## 6. Age-aware tone contract

### Little Artist (4–5)
- shortest sentences;
- concrete verbs: watch, draw, look, try;
- warm and reassuring;
- one idea at a time.

### Creative Explorer (6–7)
- simple technique language plus explicit permission to make choices;
- avoid baby talk.

### Growing Artist (8–9)
- respectful craft/observation language;
- can mention proportion, placement, contour, overlap, texture or composition only when generically supported by lesson/step metadata/copy context;
- no grades.

### Young Artist (10–12)
- concise studio language;
- observation/planning/refinement framing;
- never toddler-ish celebration or excessive cheerleading.

Age changes tone and cue density only. It must not alter session semantics, available Help level, required strokes, completion policy, or difficulty.

## 7. Deterministic context rules

P5.3 copy is deterministic. Do not use random praise pools.

Allowed generic signals:
- session semantic state;
- teaching mode;
- current step completion policy;
- whether expected child stroke refs are empty/non-empty;
- whether replay/help/skip exist;
- current Help level;
- lesson difficulty;
- objective skill IDs where wording is generic and safe.

Not allowed:
- stroke similarity/geometry quality;
- elapsed time as performance judgement;
- number of failed attempts as a child ability label;
- hidden behavioral profile;
- lesson ID or title special cases.

## 8. Open-ended/manual step rule

A child turn is treated as **open-choice presentation** when its authored contract supports manual completion and does not require expected replica strokes.

For such a turn:
- never say “copy”, “match”, “make it the same”, or equivalent;
- explicitly permit choosing/changing/inventing details in age-appropriate language;
- `Done` and `Skip` visibility still come only from authored step/session contracts.

This rule must cover existing `Design Your Spaceship` Make It Yours generically without checking its lesson ID.

## 9. Help presentation rule

Help wording must be selected from Help level + age band, while the actual guide geometry/content continues to come from the authored Help entry/runtime.

Required semantic progression:
- low Help: small cue / where to look;
- medium Help: stronger visual map/direction;
- high Help: direct guided/tracing support normalized as practice.

Skipped authored Help levels are valid; P5.3 must not assume contiguous levels.

## 10. Reflection / extension rule

Completion may expose one optional presentation-only prompt. It must:
- be non-blocking;
- require no answer/storage;
- not change completion state;
- be short and age-appropriate;
- focus on noticing a choice, imagining a variation, or what the child may want to add next time.

No diary/profile persistence is added in P5.3.

## 11. Compose integration rule

`GuidedLessonScreen` remains a thin renderer/dispatcher:
- pass product age band into the policy;
- render any new secondary/reflection cue without covering canvas or essential controls;
- preserve existing drawing-input blocking during teacher turns;
- preserve existing action dispatch to session commands;
- preserve lifecycle/recovery flow;
- no state duplicated in Compose solely for companion logic.

Generic polish allowed in this slice includes removing existing hardcoded lesson-specific copy such as “save your cat” from the generic post-drawing boundary.

## 12. Automated acceptance

Before QA freeze, tests must prove at minimum:

1. existing control-visibility contracts remain intact for AwaitingChild, HelpActive and TeacherDemonstrating;
2. all `LessonSessionState` variants map without crash and deterministically;
3. Little vs Young child-turn wording is materially age-appropriate;
4. Little vs Young Help wording is materially age-appropriate while preserving Help controls;
5. Watch Then Draw overview is distinct from step demonstration;
6. manual/open-choice step with empty expected refs uses authorship language and no copy/match language;
7. required-stroke step does not accidentally become open-choice presentation;
8. high Help/tracing wording is non-punitive across age bands;
9. completion gives optional reflection but never scoring language;
10. repeated calls with identical inputs produce identical presentation;
11. policy has no mutation dependency on document/session persistence;
12. generic post-drawing UI contains no lesson-specific “cat” copy;
13. all existing unit/lint/debug/instrumentation/profile/permission/content-quality gates remain green.

## 13. Physical QA target

First distributed P5.3 candidate must use versionCode **22** unless another distributed candidate already consumed it.

Focused physical pass must include at least:
- Little Artist representative Draw With Me flow: teacher turn → child turn → Help → completion;
- Young Artist representative lesson: tone is concise/non-toddler;
- Watch Then Draw overview vs child turn;
- Friendly Owl multi-level Help;
- Design Your Spaceship open-ended `Make It Yours` authorship wording;
- completion/reflection does not block Color/Finish actions;
- background/reopen retains correct companion state;
- normal coloring, Free Draw and Gallery smoke;
- no crash/ANR/stale companion copy.

## 14. Non-goals

- production voice/TTS system;
- animated companion asset overhaul;
- new lesson content batch;
- local adaptive recommendation/help engine (P5.7);
- Parent Zone;
- cloud/generative companion;
- Drawing/Lesson Engine redesign;
- similarity or art-quality scoring.

## 15. Exit rule

P5.3 completes only when:
1. this contract is committed before behavior changes;
2. pure deterministic presentation V2 + tests are green;
3. Compose integration remains thin and existing engine/session contracts stay intact;
4. exact QA candidate CI is green;
5. exact APK/report evidence is reproducible;
6. focused physical pass is accepted;
7. final acceptance-doc CI is green;
8. PR is squash-merged;
9. merged-main CI is green;
10. issue #78 closes completed.

P5.4 bulk curriculum production does not start until P5.3 is formally accepted.