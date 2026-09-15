# P6.3 Parent Progress & Curriculum Visibility Contract

Status: **FROZEN CONTRACT**  
Issue: #96  
Parent epic: #91  
Target: Phase 6 / `0.6.0-family-readiness`  
Depends on: P6.2 complete, merge `38177435662f0b111b54138892ab65587b2d8abe`, merged-main CI #603 GREEN, clean baseline `d518bd8fca3d45af6b33604e9f87f13798826142` / CI #605 GREEN

## 1. Product objective

Parent Zone → Learning becomes a real local progress/curriculum surface that helps an adult answer:
- What has the child completed?
- What have they been exploring?
- What curriculum areas and authored skills have appeared in completed work?
- Is anything currently in progress?
- What journey step is available next?
- What artwork was recently saved?

It must answer those questions without grading, ranking, comparing or profiling the child.

## 2. Permanent P6.3 principles

1. **Projection, not analytics collection.** Use existing accepted local truth; do not add a behavioral analytics stream.
2. **Read-only to child truth.** Parent Progress cannot mark lessons complete, mutate artwork, change session truth or rewrite adaptive history.
3. **Descriptive, not evaluative.** Report authored curriculum exposure and product events, never inferred ability.
4. **Truth-source labels matter.** Completion, saved artwork, active work and recommendation/progression context remain distinct concepts.
5. **Partial truth is better than invented truth.** Missing/corrupt sources produce calm unavailable/empty states.
6. **Offline by construction.** No account, cloud sync, analytics upload or network dependency.
7. **Single-profile remains frozen for 0.6.** No profile-ID/multi-child migration.
8. **No new Android permission.**

## 3. Authoritative source hierarchy

### Completion truth
Use `LocalAdaptiveState.completedLessons` and `recentCompletions`.

A lesson is locally completed when the existing product completion boundary successfully saved the lesson artwork and recorded `AdaptiveEvent.LessonCompleted`.

### Current curriculum meaning
Join completion identities to current release `LessonCatalog` metadata.

Use authored:
- title/summary;
- category IDs;
- skill IDs;
- journey IDs;
- prerequisite lesson IDs;
- age-band metadata;
- tags where useful.

### Saved-artwork activity
Use Gallery catalog records and genuine `completedAtEpochMillis` timestamps.

### Active work
Use existing drawing/coloring resume stores and semantic state.

### Recommendation/progression context
Reuse accepted prerequisite/completion semantics. Do not introduce a second progression algorithm.

## 4. Required data model behavior

P6.3 shall implement a deterministic read-only Parent Progress model/projection.

At minimum it must represent:

### 4.1 Availability
Per-source state sufficient to distinguish:
- loaded/ready;
- genuinely empty/missing;
- unavailable/corrupt/incompatible.

Adaptive loading must gain a read-only typed outcome without changing `loadForPolicy()` behavior.

### 4.2 Summary
- current-catalog completed lesson count;
- current-catalog lesson count relevant to the projection;
- saved Gallery artwork count;
- active drawing/coloring work count;
- optional local completion count distinction when old/unresolved completion identities exist.

No summary field may be named or framed as score, mastery, rank, level, performance or ability.

### 4.3 Recent completions
- preserve genuine adaptive recent-completion ordering;
- resolve current title/metadata only when exact lesson ID + revision is available;
- no fabricated timestamp;
- repeated completion may appear as recent activity but must not inflate unique completed count.

### 4.4 Recent saved artwork
- newest-first Gallery ordering;
- title;
- lesson vs Free Draw;
- drawing-only vs colored;
- real saved timestamp;
- lesson provenance where present.

This list is “saved artwork activity,” not a substitute for all lesson completion history.

### 4.5 Skills practiced
Derive from authored skill IDs of resolved completed lessons.

Allowed framing:
- “Practiced curved lines”;
- “Curved lines appeared in 3 completed lessons.”

Forbidden framing:
- “Curved lines mastered 75%”;
- “Best skill”;
- “Weak skill”;
- ability/talent inference.

Do not expose raw adaptive Help-request counts in P6.3.

### 4.6 Categories explored
Derive categories from resolved completed lessons. Counts are curriculum exposure counts, not quality metrics.

### 4.7 Journey progress
For each current journey where useful:
- authored journey title;
- total current journey lessons;
- number of current journey lessons completed;
- current completed lesson membership;
- next prerequisite-eligible incomplete lesson using accepted prerequisite semantics;
- calm “journey complete” state when all current journey lessons are completed.

Counts such as “2 of 4 lessons completed” are allowed. Percentages such as “50% mastered” are not.

### 4.8 Active work
Clearly separate:
- drawing in progress;
- coloring in progress;
- saved-at/resume timestamp where existing store supplies one.

Active work never counts as a completed lesson.

## 5. Parent Zone Learning UI contract

The P6.2 Learning placeholder must be replaced by a production Compose surface inside the existing protected Parent Zone session.

### Required sections

#### A. Header / context
- “Learning” heading;
- child nickname context where appropriate;
- explicit local-only/non-grading note available without burying critical content.

#### B. Calm summary
A compact summary with descriptive counts such as:
- completed lessons;
- saved artworks;
- active work.

No score dial, performance color grading, leaderboard, rank, percentage mastery or celebratory pressure.

#### C. Recent learning
- recently completed lessons in actual adaptive order;
- no timestamp if none exists;
- empty state when no completion history exists.

#### D. Curriculum explored
- categories explored;
- authored skills practiced/exposed;
- plain-language counts when useful;
- no ability interpretation.

#### E. Journeys
- journey lesson count/completed count;
- next available step when deterministically supported;
- completed state;
- prerequisite explanation in human language.

#### F. Saved artwork activity
- recent Gallery entries with real saved timestamp;
- clear distinction between lesson and Free Draw;
- this section may be omitted/collapsed if Gallery empty, but empty state must remain understandable.

#### G. In progress
- active drawing/coloring work when present;
- this is resume context, not completion history.

#### H. Privacy/data explanation
Plain language:
- progress is assembled from local activity on this device;
- no cloud parent dashboard/account in this release;
- no grading/ranking/comparison;
- deleting/resetting data behavior remains owned by later P6.6 controls.

## 6. Empty and degraded states

### Brand-new/no-history profile
Show:
- calm message that no lessons have been completed yet;
- current curriculum/journey availability where possible;
- no zero-score framing;
- no “behind” or inactivity language.

### Adaptive unavailable
If corrupt/incompatible:
- do not reset it merely to render Parent Progress;
- show a calm local-progress-unavailable note;
- still show Gallery/current curriculum/active work where available.

### Gallery unavailable
- preserve adaptive completion/curriculum sections;
- omit timestamped artwork history with a calm unavailable note;
- do not claim artwork was deleted.

### Catalog partial diagnostics
- only project valid current release lessons;
- no invented metadata for unavailable entries;
- progress view remains usable if safe partial data exists.

## 7. Privacy and safety contract

P6.3 must not:
- add network access;
- add account/login requirement;
- add analytics SDK;
- upload child activity;
- add raw artwork/strokes to progress state;
- add free-form parent notes about the child;
- create permanent ability labels;
- surface raw Help counts as deficiency evidence;
- infer emotion, diagnosis, cognition or talent;
- compare siblings/peers;
- introduce punitive streaks or inactivity alerts.

No new persistent progress store is allowed in P6.3 V1. A later need for timestamped lesson completion independent of Gallery requires a separately reviewed versioned-storage contract.

## 8. Determinism rules

Given the same:
- current release catalog;
- adaptive state;
- Gallery catalog;
- lesson/coloring resume state;
- child profile;

the Parent Progress model must be identical.

Ordering rules must be explicit:
- recent completion = adaptive stored order, newest logical item last internally then presented newest-first if UI chooses;
- Gallery = existing `newestFirst()` ordering;
- skills/categories = stable deterministic sort after aggregation;
- journeys = current authored/prerequisite order;
- active work = stable type/time ordering defined in tests.

No randomization.

## 9. Catalog evolution and identity

For current-curriculum detail, exact lesson ID + revision must resolve to current catalog metadata.

Unresolved/older local completion identities:
- remain preserved in source state;
- do not acquire invented titles/skills/categories;
- do not count as current-catalog completion;
- may contribute to a calm “older local history exists” indicator if needed.

Prerequisite eligibility must continue using the same accepted lesson-ID completion semantics as the product recommendation policy unless a future migration explicitly changes that invariant.

## 10. Accessibility contract

Learning surface must:
- support large system text without clipping critical content;
- use vertical scrolling on compact screens;
- maintain >=48dp effective target for interactive controls;
- use semantic headings/content descriptions where practical;
- not rely on color alone to encode completion/state;
- avoid dense charts requiring precision interpretation;
- remain understandable in monochrome/high-contrast conditions;
- present counts and state in text.

## 11. Implementation boundary

Recommended production components:
- typed adaptive read result added behind existing local adaptive repository/store boundary;
- pure `ParentProgressProjection` / builder (exact name flexible);
- `ParentProgressRepository` Android/local adapter that loads catalog + accepted stores and returns a typed read model;
- production `ParentLearningScreen` or equivalent Compose surface;
- Parent Zone Learning route wired to that read-only model.

The read model exposes no save/delete/complete API.

Do not modify Drawing Engine, Lesson Engine, Coloring Engine, Gallery transaction semantics or adaptive reducer behavior unless a specific P6.3 defect proves necessary and the contract is amended first.

## 12. Automated test contract

Required focused tests include:
- empty adaptive + empty Gallery projection;
- completed current lesson aggregation;
- repeated recent completion does not inflate unique count;
- exact revision resolution;
- unresolved old completion identity behavior;
- authored skill/category aggregation;
- journey completed count + next prerequisite-eligible lesson;
- journey-complete state;
- adaptive missing vs unavailable distinction;
- Gallery ready/empty/unavailable handling;
- real Gallery timestamp preservation;
- Free Draw vs lesson activity distinction;
- active drawing and coloring remain in-progress, not completed;
- stable deterministic ordering;
- no raw Help-count exposure;
- no grade/rank/mastery/ability fields or copy in the model/UI;
- Parent Zone Learning render/navigation where practical;
- large-content/small-screen Compose behavior where practical;
- all existing unit/lint/APK/content/permission gates remain green.

## 13. QA/version contract

Do not bump versionCode simply for contract/docs work.

After P6.3 production implementation is automated-green:
- reserve a monotonic versionCode **>28**;
- use a Phase-6 P6.3 QA version name such as `0.6.0-family-readiness-p6.3-qa1`;
- package exact debug/profile APK evidence, sizes and SHA256;
- physically test the exact profile APK;
- record acceptance docs;
- acceptance-doc CI GREEN;
- mark PR ready and squash-merge;
- merged-main CI GREEN;
- close #96 completed.

Any executable change after physical acceptance requires a new versionCode and fresh exact-binary QA.

## 14. Physical acceptance focus

Future focused real-device QA must include:
- existing-progress profile;
- brand-new/empty profile;
- recent completions ordering;
- journey counts/next step;
- saved lesson artwork timestamp;
- Free Draw distinction;
- drawing/coloring in-progress distinction;
- relaunch persistence;
- Airplane Mode;
- large text;
- small-screen reachability;
- Parent Gate/session regression;
- child Home/lesson/Gallery regression.

## 15. Definition of done

P6.3 is complete only when:
1. Parent Learning is grounded in real local truth;
2. completion/artwork/active/recommendation semantics remain distinct;
3. no evaluative child scoring/profiling is introduced;
4. missing/corrupt data degrades honestly;
5. automated regression and product gates are green;
6. an exact monotonic QA APK passes focused physical acceptance;
7. acceptance evidence is committed;
8. PR is squash-merged;
9. merged-main CI is green;
10. issue #96 closes completed.
