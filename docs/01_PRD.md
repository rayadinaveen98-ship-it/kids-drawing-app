# 01 — Product Requirements Document (PRD) v0.2

**Status:** Phase 0 scope contract — in review/lock stage  
**Product:** Kids Drawing App (working name)  
**Primary platform:** Android  
**Primary audience:** Children approximately 4–12 years old  

## 1. Product statement

Kids Drawing App is an offline-first personal art teacher and creative studio for children. It teaches drawing and coloring interactively at the child's pace, adapts complexity to age and ability, offers patient assistance when the child is stuck, and preserves room for creative interpretation rather than grading every picture against one correct answer.

**Core promise:** Draw together with a teacher who never runs out of patience.

## 2. Problem

Children who want to learn drawing commonly face three incomplete experiences:
1. passive videos that do not wait, repeat intelligently, or respond to the child;
2. tracing/coloring products that entertain but teach little transferable drawing skill;
3. professional art applications with excessive tool and interface complexity for younger users.

The product bridges these gaps with a live draw-along lesson engine, age-progressive tools, a supportive companion, and a real creative studio.

## 3. Product goals

V1 must prove that:
- a child can begin and complete a drawing lesson with little adult instruction;
- the teacher can demonstrate real strokes at multiple paces and wait for the child;
- assistance can increase without shame or hard failure states;
- a finished drawing can transition naturally into coloring;
- the app remains fun when the child does not want a lesson through Free Draw;
- the interface meaningfully changes in complexity for different age bands;
- the complete core experience works offline;
- the foundation can scale to a large lesson library without hard-coded tutorial screens.

## 4. Explicit V1 non-goals

The first public V1 will not include:
- public child profiles or social feeds;
- comments, direct messaging, or stranger collaboration;
- advertisements or behavioral ad identifiers;
- competitive leaderboards or streak pressure;
- mandatory login or child account creation;
- unrestricted generative-AI chat;
- strict numerical drawing scores;
- advanced professional layers/masks/selection workflows;
- cloud sync as a requirement for core use;
- teacher/school administration systems;
- a marketplace.

## 5. Target users and experience bands

### Little Artists — 4–5
Very large controls, minimal reading, narration-led interaction, simple subjects, tracing/help available quickly, and a deliberately small visible tool set.

### Creative Explorers — 6–7
More independent guided drawing, shape construction, richer subject choice, basic coloring technique, and less default tracing.

### Growing Artists — 8–9
More detailed construction drawing, characters/scenery, introductory proportion/shading, and greater Free Draw control.

### Young Artists — 10–12
Perspective/faces/light-and-shadow foundations, more sophisticated lesson language, and selected advanced studio controls when useful.

Age is an initial policy signal, not a permanent skill label. Later learning signals may refine recommendations without changing the child's identity or presenting punitive rankings.

## 6. Core V1 journey

First launch → nickname → age → preferred learning style → preferred pace → interests → handedness → personalized home → choose/resume lesson → companion introduction → teacher demonstrates → child draws → optional help → lesson completion → guided/self coloring → artwork saved → gallery.

Free Draw remains directly available from Home and does not require completing lessons.

## 7. MUST HAVE — Public V1 contract

### 7.1 Onboarding and local profile
Must support:
- nickname rather than required legal name;
- age or age band without requiring full birth date;
- Draw With Me / Watch Then Draw / Trace & Learn preference;
- teaching pace preference;
- interest/category selection;
- left/right-handed preference;
- audio/narration preference;
- local persistence and later editing through parent/profile settings.

Acceptance criteria:
- a new user can reach Home without creating an online account;
- onboarding is recoverable after interruption;
- selections immediately influence recommendations or presentation where relevant;
- core setup remains functional with airplane mode enabled.

### 7.2 Personalized Home / Studio Lobby
Must provide:
- Continue Drawing when an incomplete session exists;
- age-appropriate recommended lessons;
- category browsing;
- entry to Art Journeys;
- entry to Free Draw;
- entry to Gallery;
- clear visual distinction between lesson content and free creation.

V1 recommendations may be deterministic rules based on age, interests, recent progress, and content metadata. Machine learning is not required.

### 7.3 Guided Drawing Lesson
Must support:
- structured lesson content interpreted by the Lesson Engine;
- animated teacher strokes rendered from stroke data;
- Draw With Me mode;
- Watch Then Draw mode;
- Trace & Learn mode;
- Extra Slow, Slow, Normal, Fast, and Very Fast playback;
- pace changes during a lesson without restarting;
- pause/resume;
- replay current demonstration;
- child drawing input while appropriate;
- persistent step/lesson progress;
- safe recovery after app backgrounding or interruption;
- controlled skip where the lesson author explicitly permits it.

V1 does **not** require strict shape-recognition grading. Step completion may use explicit child confirmation plus broad interaction signals. Assistance quality matters more than scoring accuracy.

Acceptance criteria:
- the same teacher stroke geometry replays consistently at every pace;
- changing pace changes timing, not geometry;
- the child can repeat a step without losing completed work;
- leaving and returning to a lesson does not corrupt the artwork or lesson state;
- no common path traps the child in a state that requires force-closing the app.

### 7.4 Adaptive Help Ladder
Must support an authored/help-state progression:
1. independent attempt;
2. gentle hint;
3. visual guide;
4. direction/anchor cues;
5. trace path;
6. assisted success.

V1 may trigger higher help levels through child request and simple lesson rules. Sophisticated automatic skill inference is not required for V1.

Acceptance criteria:
- requesting help never destroys existing child work;
- a child can always progress to an assisted completion path;
- UI language avoids wrong/fail/bad-style judgment;
- creative variation is allowed where exact geometry is not pedagogically necessary.

### 7.5 Coloring
After supported drawing lessons, the child must be offered:
- Color With Me;
- Color Myself;
- Finish for Now.

Coloring must include at minimum:
- child-safe color palette;
- pencil/crayon or brush-style coloring tool;
- fill tool for prepared closed regions where technically valid;
- eraser;
- size control appropriate to age;
- undo/redo;
- save/continue behavior.

Guided coloring can teach order, coverage, contrast, light/dark, and simple technique without forcing one exact color choice unless the lesson specifically teaches color recognition.

### 7.6 Free Draw Studio
Must provide:
- blank canvas;
- pencil;
- crayon;
- marker/basic brush;
- eraser;
- color selection;
- brush-size control;
- undo/redo;
- clear with confirmation;
- save artwork;
- age-appropriate visible tool density.

Advanced layers, selection tools, perspective grids, custom brushes, and professional compositing are later capabilities.

### 7.7 Companion
The companion must be a state-driven teaching participant with at least these semantic states available to the product:
- greeting;
- teaching;
- watching;
- waiting;
- encouraging;
- helping;
- thinking;
- celebrating;
- lesson complete.

V1 behavior may use curated lines plus Android TTS/prerecorded assets. The companion must not require a cloud language model.

Acceptance criteria:
- the companion never blocks critical drawing content or controls;
- visual expression and spoken/text feedback correspond to the same product state;
- narration can be muted;
- the companion can remain quietly present instead of speaking after every action.

### 7.8 Gallery
Must provide:
- saved artwork thumbnails;
- artwork title/date/basic lesson metadata where available;
- view artwork;
- safe delete with confirmation/parent policy as appropriate;
- parent-gated export/share outside the app;
- local storage by default.

### 7.9 Parent Zone / Safety Controls
Must be separated from child mode through an age-appropriate parent gate and include at least:
- profile/settings editing;
- audio/voice controls;
- export controls;
- privacy/safety information;
- content/storage management required by V1;
- app information and version.

Purchases are not required for V1. If monetization is introduced later, purchase and external-navigation actions remain parent-gated.

### 7.10 Offline behavior and persistence
Must remain usable offline for:
- onboarding/local profile;
- bundled lessons;
- guided drawing;
- coloring;
- Free Draw;
- Gallery;
- companion's required V1 behavior;
- progress persistence.

No core session may fail simply because a backend is unavailable.

## 8. SHOULD HAVE — include only if Must-Haves are already excellent

- Match My Speed / adaptive pace recommendation;
- Draw From Memory challenge;
- gentle achievements based on skill exploration/creation rather than streak pressure;
- downloadable offline lesson packs;
- basic parent learning-progress summaries;
- additional companion cosmetic reactions/idle animations;
- accessibility refinements beyond mandatory baseline;
- basic stroke similarity used only for optional supportive hints, never punitive grading.

Should-Have work must not delay V1 if it reduces stability, lesson quality, drawing latency, or safety.

## 9. LATER product expansion

- Story Drawing and illustrated story creation;
- private parent/child collaborative drawing;
- multiple selectable companions;
- advanced layers, selections, blending, symmetry, perspective guides and transformations;
- character/environment design curricula;
- on-device voice commands;
- parent accounts and optional cloud backup/sync;
- cross-device profiles;
- safe generative creativity tools after a separate child-safety review;
- teacher/school mode;
- web/iPad/iOS expansion after Android product fit is proven.

## 10. Public V1 content target

Quality outranks raw catalog size.

Target:
- **30–40 high-quality guided drawing lessons**;
- hard release floor of **24 complete guided lessons** if every lesson meets quality gates;
- coverage across all four primary experience bands;
- at least **6 meaningful subject categories**;
- at least **3 starter Art Journeys** with coherent skill progression;
- every guided lesson that promises coloring must include tested coloring data/content;
- a small curated set of Free Draw backgrounds/prompts may be included but is not required.

The app must be architected for hundreds/thousands of future lessons without app-screen duplication.

## 11. Experience quality requirements

### Teaching tone
Patient, calm, specific, encouraging, and non-judgmental. Do not overpraise every stroke and do not manufacture urgency.

### Interface
Canvas is the visual priority during creation. Secondary navigation and tools should recede while drawing.

### Age adaptation
A 4-year-old and a 12-year-old must not receive merely different lesson recommendations; visible tool density, language, help defaults, and lesson complexity must also differ.

### No manipulation
No forced daily streaks, loot-box mechanics, competitive rankings, or artificial loss-aversion loops are required for engagement.

## 12. V1 safety/privacy requirements

- no public profile;
- no open social communication;
- no precise location requirement;
- no behavioral advertising;
- no mandatory child email/phone/account;
- nickname/age band preferred over unnecessary identity data;
- artwork local by default;
- external sharing/export behind parent control;
- third-party SDK footprint kept minimal;
- microphone access absent from V1 unless a separately reviewed feature requires it.

Formal store/legal compliance review remains a pre-public-release gate and is separate from this engineering PRD.

## 13. Engineering quality requirements

V1 is not releasable merely because screens function. It must satisfy the project test strategy and milestone gates, including:
- drawing responsiveness on reference low/mid/high Android hardware;
- no routine lost/corrupt strokes;
- deterministic stroke save/load/replay;
- reliable interruption/background restoration;
- bounded memory usage in realistic long sessions;
- stable undo/redo;
- offline functional testing;
- usability testing appropriate to multiple age bands;
- installable APK from the release pipeline.

Precise drawing latency/frame-time thresholds are defined and validated in the Drawing Engine performance specification rather than guessed in this PRD.

## 14. V1 product success criteria

V1 succeeds when:
- a child can understand how to begin without a parent teaching the interface;
- the child can complete a lesson at a comfortable pace;
- asking for help feels like teaching rather than failure recovery;
- the child can turn the same drawing into a colored finished artwork;
- Free Draw is useful enough to return to even without consuming lesson content;
- the Gallery creates visible ownership/pride in previous work;
- a parent can understand the app's safety model and control outward sharing;
- core use remains valuable offline;
- adding a new lesson is primarily a content operation, not new feature code.

## 15. Scope-change rule

Any new feature proposed for V1 must answer all three questions:
1. Which V1 goal does it materially improve?
2. Why can the first public release not succeed without it?
3. What existing scope, schedule, complexity, or quality budget pays for it?

If those answers are weak, the feature moves to Should Have or Later.