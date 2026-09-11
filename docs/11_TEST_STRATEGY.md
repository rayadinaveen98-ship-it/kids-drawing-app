# 11 — Test Strategy

**Status:** Phase 0.7 reconciled quality contract

## 1. Principle

No engine or milestone is considered complete because it “looks like it works.”

Quality evidence is layered:
- unit tests;
- state-machine/contract tests;
- integration tests;
- UI tests where behavior is presentation-dependent;
- performance/benchmark tests;
- physical-device tests;
- persistence/recovery tests;
- accessibility/usability review.

## 2. Drawing Engine tests

Unit/contract coverage includes:
- document/operation serialization;
- coordinate transforms;
- brush/tool preset mapping;
- stroke commit lifecycle;
- command history;
- undo/redo branching;
- clear undo;
- role-aware erase semantics;
- save/load migration/defaulting;
- deterministic playback clock;
- all five pace profiles;
- pause/resume;
- mid-playback pace switch;
- teacher overlay separation.

Physical/performance gate is defined in `docs/18_DRAWING_PERFORMANCE_GATES.md` and is authoritative for Drawing Engine lock.

## 3. Lesson Engine tests

State-machine tests cover:
- Draw With Me;
- Watch Then Draw;
- Trace & Learn;
- five pace states;
- replay;
- pause/resume;
- Help Ladder escalation with missing optional levels;
- invalid/valid skip;
- child completion policies;
- drawing commit before step advance;
- drawing complete choices;
- coloring handoff;
- narration/companion failure fallback;
- process death/resume;
- incompatible/corrupt lesson recovery.

Illegal transitions must be rejected through contract tests, not only discovered through UI testing.

## 4. Coloring tests

Coverage includes:
- guided/self modes;
- authored region fill;
- replace fill + undo/redo;
- freehand color stroke;
- color eraser role protection;
- clear coloring undo;
- drawing→coloring atomic handoff;
- coloring init failure preserving line art;
- mixed fill/freehand persistence;
- completion/resume/process death.

## 5. Companion tests

Coverage includes:
- semantic priority interruption;
- rate-limit/coalescing rules;
- Help outranking optional celebration;
- voice failure/disabled fallback;
- reduced-motion presentation mapping;
- age-adaptive copy policy;
- safe-zone fallback states;
- lesson continuity when companion renderer fails.

Visual/manual QA additionally checks actual canvas obstruction on representative layouts.

## 6. Content validation

Every lesson package must pass pre-runtime validation for:
- schema version;
- required metadata;
- unique stable IDs;
- asset references;
- teaching-mode compatibility;
- trace/guide requirements;
- coloring region references;
- supported completion policies;
- narration keys;
- no orphan steps/assets.

Representative lessons are tested before bulk content production.

## 7. Persistence/recovery matrix

Test force-stop/process recreation at stable boundaries including:
- blank new artwork;
- during drawing after committed stroke;
- paused lesson;
- child turn;
- after help escalation;
- drawing complete before coloring choice;
- coloring active;
- artwork completed before preview generation;
- corrupt newest autosave with prior valid snapshot where recovery exists.

Artwork preservation takes priority over preserving an exact transient animation/audio frame.

## 8. UI/navigation tests

Test canonical routes from `docs/15_SCREEN_ARCHITECTURE.md`:
- onboarding resume/back;
- Home/Learn/Create/Gallery shell;
- safe exit from creative workspaces;
- Continue Drawing;
- empty Gallery;
- Parent Gate protection;
- adult-context timeout/relock;
- deep-link/direct-route gate bypass attempts;
- deletion/export confirmations.

## 9. Accessibility testing

At each product vertical slice, review:
- touch target sizing;
- font scaling;
- TalkBack labels/order for critical controls;
- non-color state cues;
- reduced motion;
- voice-off usability;
- left-handed layout;
- orientation/large-screen behavior where supported.

## 10. CI baseline

Every pull request/change set should progress toward:

```text
compile
→ unit/contract tests
→ Android lint
→ detekt/ktlint when configured
→ schema/content validation
→ debug APK assembly
```

Instrumentation/benchmark suites may run on dedicated workflows/emulators/physical devices because they are more expensive than JVM tests.

CI must not become dependent on paid cloud services to be green.

## 11. Phase 1 Art Lab acceptance evidence

Art Lab 0.1 is complete only when all applicable requirements below are met:
- app builds from clean checkout;
- CI compile/unit/lint checks green;
- installable debug/internal APK produced;
- Pencil/Eraser/color/width/undo/redo/clear/save/reload work;
- teacher test stroke can play at all five paces;
- playback pause/resume/replay works;
- lifecycle/background/restore preserves committed artwork;
- deterministic playback tests green;
- physical-device performance results recorded against `docs/18_DRAWING_PERFORMANCE_GATES.md`;
- known failures are documented rather than hidden;
- Git tag/release notes identify the milestone build.

A `PENDING-HARDWARE` performance result is allowed during early implementation when a required device class is not available, but Drawing Engine 0.1 cannot be called fully locked for public-product use until its required physical-device gate passes.

## 12. Vertical Slice acceptance evidence

The first complete lesson slice must prove:
- onboarding profile is saved;
- recommendation opens lesson;
- companion/voice can fail without blocking;
- teacher demo + child turn repeat correctly;
- help does not mutate child art;
- drawing completes and persists;
- guided/self coloring handoff works;
- finished artwork appears in Gallery;
- process death at representative boundaries does not lose committed artwork;
- no critical dead-end navigation;
- representative 4–5 and 10–12 age policies both remain usable.

## 13. Release defect policy

For milestone release:
- no known P0 data-loss/security/privacy defect;
- no known P1 defect preventing the milestone's core objective;
- P2/P3 known issues may ship internally only if documented with issue IDs and do not invalidate test results.

Severity direction:
- P0: artwork/data loss, child-safety/privacy bypass, corruption/security-critical issue;
- P1: core engine/lesson unusable, persistent crash, gate bypass;
- P2: major degraded behavior with workaround;
- P3: polish/minor/non-blocking defect.

## 14. Public V1 additional quality gate

Separate from internal milestone DoD, public release additionally requires:
- current platform-policy review;
- privacy/Data safety/store declarations;
- release signing/build reproducibility plan;
- physical low/mainstream/stylus device matrix results;
- accessibility review;
- parent-gate child/adult usability validation;
- content QA/licensing provenance;
- security/dependency/permission review;
- crash-free stability evidence from controlled testing that does not rely on invasive child telemetry.

## 15. Evidence rule

A test gate is only considered passed when evidence is recorded in Git/CI/release notes or a linked QA artifact. Verbal confirmation in chat is not evidence.