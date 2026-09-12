# P3.1 Execution Contract — Product Shell + Onboarding

**Issue:** #43  
**Parent epic:** #42  
**Target milestone:** `0.3.0-vertical-slice`

## Objective

Create the first production-facing child surface over the frozen engine foundation without weakening the Drawing/Lesson Engine contracts.

P3.1 owns:
- the production launcher;
- Studio design tokens/components;
- startup routing;
- local child-profile preferences;
- interruption-safe onboarding;
- a production Home shell used as the handoff target for P3.2.

P3.1 does **not** own lesson truth, artwork truth, recommendation truth, coloring truth, Gallery truth, or companion animation implementation.

## Frozen product/design inputs

Implementation must follow:
- `docs/01_PRD.md`;
- `docs/03_UX_ARCHITECTURE.md`;
- `docs/06_TECH_ARCHITECTURE.md`;
- `docs/20_ENGINE_BOUNDARIES.md`;
- `docs/21_VISUAL_SYSTEM.md`.

The selected visual direction remains **Premium Storybook Art Studio**.

## Onboarding decisions

One decision per screen:
1. nickname;
2. age band;
3. preferred teaching mode;
4. preferred teaching pace;
5. interests;
6. handedness;
7. narration preference.

No full birthday, email, phone number, account, precise location, or network connection is required.

## Persistence

Profile/preferences use Android DataStore, matching the frozen technical architecture.

Requirements:
- save after each completed onboarding decision;
- persist current onboarding step;
- restart/recreation resumes from durable draft state;
- final profile is marked complete only when every required field validates;
- startup router chooses Onboarding vs Home from durable profile state rather than UI booleans.

## Production launcher rule

`ProductActivity` becomes the only normal launcher activity.

Engineering labs remain explicit activities for regression/debug use:
- `MainActivity` — Art Lab;
- `LessonLabActivity` — Lesson Lab;
- `QualityLabActivity` — Quality Lab.

They must not be deleted or silently rewritten into product state owners.

## Visual requirements

- warm paper background;
- restrained Studio green as primary action;
- large, calm controls;
- one question/decision per onboarding page;
- no rainbow dashboard, engagement badges, streaks, or neon gaming language;
- system sans fallback is allowed until Nunito Sans is deliberately bundled;
- system bars/insets must be respected;
- child-facing text must remain readable under normal font scaling.

## Age policy

P3.1 establishes one shared design system with age-density policy hooks. It must not fork into four unrelated themes.

Initial policy mapping:
- 4–5: XL targets, most whitespace, least metadata;
- 6–7: large targets, icon + text support;
- 8–9: standard-large targets, richer text allowed;
- 10–12: mature but still child-safe density.

P3.2+ may consume these policy hooks more deeply.

## Failure/recovery behavior

- storage failure must not erase an already loaded profile;
- incomplete/invalid profile data routes safely back to onboarding;
- no onboarding failure may touch drawing/lesson persistence;
- offline/airplane mode must not change behavior.

## Tests

At minimum:
- profile completeness validation;
- nickname normalization/validation;
- enum/default recovery from missing preference values;
- onboarding step bounds/defaulting;
- fresh-start routing;
- completed-profile routing;
- existing Drawing/Lesson engine unit/instrumentation suites remain green through CI.

## Acceptance gate

P3.1 is complete only when:
- fresh install launches the product onboarding surface;
- all seven decisions can be completed without network/account creation;
- interruption/relaunch preserves completed decisions and current step;
- final profile persists and routes to the product Home shell;
- the Home shell is not an engineering lab;
- Art Lab, Lesson Lab, and Quality Lab remain explicitly launchable;
- CI is green on the exact PR head;
- no engine architecture invariant is weakened.
