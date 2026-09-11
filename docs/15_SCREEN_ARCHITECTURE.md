# 15 — V1 Screen & Navigation Architecture

**Status:** Phase 0.3 working contract  
**Scope:** Public V1 information architecture, not final visual styling.

## 1. Navigation principle

The app has three navigation contexts:

1. **Onboarding flow** — sequential setup before the first Home experience.
2. **Child shell** — stable non-creation navigation for Home, Learn, Create and Gallery.
3. **Creation workspace** — immersive full-screen guided drawing, coloring or Free Draw where child-shell navigation is hidden and exiting is deliberate/safe.

The Parent Zone is a separate gated graph and never becomes part of the child's normal navigation hierarchy.

## 2. App start/router behavior

### SYS-001 — Startup Router
Not a product destination. It resolves local state and routes safely.

Priority:
1. if onboarding is incomplete → resume the first incomplete onboarding step;
2. otherwise → Child Home;
3. if an unfinished creative session exists, Home surfaces **Continue Drawing** prominently rather than forcing immediate resume;
4. corrupt optional content/session data is isolated and must not prevent Home from opening.

A splash/brand presentation may wrap this router later but must not introduce unnecessary waiting.

## 3. Onboarding screen inventory

### ONB-001 — Welcome / Name
Purpose: friendly first contact and nickname entry.

Required:
- companion greeting;
- nickname field;
- obvious Continue action;
- no email, phone, account or full legal name request.

### ONB-002 — Age
Purpose: select age/age band used for initial experience policy.

Required:
- visual age selection;
- parent-friendly explanation available if needed;
- no full date-of-birth requirement.

### ONB-003 — Learning Style
Choices:
- Draw With Me;
- Watch Then Draw;
- Trace & Learn.

Each choice must be demonstrated visually, not only described with text.

### ONB-004 — Teaching Pace
Choices:
- Extra Slow;
- Slow;
- Normal;
- Fast;
- Very Fast.

A tiny animated preview may demonstrate pace. Adaptive/Match My Speed is not required for V1 onboarding.

### ONB-005 — Interests
Visual multi-select of favorite subject families such as animals, vehicles, space, nature, characters, food and fantasy.

Selection improves Home recommendations but never locks other content.

### ONB-006 — Drawing Setup
Purpose: physical/comfort preferences.

Required:
- left-handed / right-handed / not sure;
- narration on/off or simple sound preference;
- clear completion action.

Completion routes to Home and persists the local profile.

## 4. Child shell

The V1 child shell uses four stable primary destinations outside active creative sessions:

1. **Home**
2. **Learn**
3. **Create**
4. **Gallery**

The icons/routes remain conceptually stable across age bands. Labels, card density, explanatory text and visible secondary actions may adapt by age.

For younger children, label language can become more concrete (for example Create may visually read as Draw), but it must still route to the same conceptual destination.

### HOME-001 — Studio Lobby
Primary personalized landing screen.

Required sections/actions:
- Continue Drawing when relevant;
- recommended guided lesson;
- age/interests-based lesson row(s);
- Art Journey highlight;
- Free Draw shortcut;
- recent Gallery artwork;
- lightweight companion presence that does not dominate navigation.

Home must remain useful without network connectivity.

### LEARN-001 — Explore
Purpose: browse guided learning content.

Required:
- age-appropriate category cards;
- Art Journeys section;
- suitable difficulty/skill cues;
- no mandatory text search for V1 because the initial catalog is intentionally curated.

### LEARN-002 — Category Collection
Purpose: view lessons within one subject/category.

Required:
- visual lesson cards;
- progress/completed state where useful;
- age/difficulty filtering applied by policy;
- ability to view other suitable lessons without exposing inappropriate complexity.

### JOURNEY-001 — Art Journey Detail
Purpose: show a coherent skill progression.

Required:
- journey theme/title;
- sequence of lessons;
- current/next lesson;
- progress through journey;
- explanation of what the child will learn using age-appropriate language.

Journey membership never prevents opening a standalone suitable lesson elsewhere.

### CREATE-001 — Create Hub
Purpose: enter open-ended creation.

V1 required:
- prominent **Free Draw** action;
- optional small set of safe prompts/background starts only if content is ready;
- no pressure to consume a lesson before creating freely.

If the hub adds no value at implementation time, the Create shell destination may route directly to Free Draw and expose prompts from there. That decision is UI-level, not an engine dependency.

### GALLERY-001 — My Art Gallery
Purpose: visual ownership/history.

Required:
- artwork thumbnail collection;
- recent-first default;
- clear opening of an artwork;
- empty state that invites creation rather than displaying a sterile error.

### GALLERY-002 — Artwork Detail
Required:
- full artwork view;
- simple title/date/context metadata;
- lesson provenance when relevant;
- parent-gated export/share entry;
- safe deletion entry;
- no public like/comment/social controls.

## 5. Guided lesson flow

### LESSON-001 — Lesson Preview
Reached from Home, Explore, Category or Journey.

Required:
- subject preview;
- child-friendly difficulty cue;
- approximate duration when useful;
- selected/default learning mode;
- selected/default pace;
- Start Drawing;
- mode/pace adjustment without returning to global settings.

For younger ages, metadata should be primarily visual and narration-friendly.

### LESSON-002 — Guided Drawing Workspace
Immersive creation surface; child-shell navigation is hidden.

Visual/interaction priority:
1. canvas;
2. current teacher demonstration/child action;
3. essential controls;
4. companion;
5. authored tool controls;
6. optional secondary controls.

Required persistent/quick-access actions:
- Pause;
- Replay current demonstration;
- Pace;
- Help.

Required behavior:
- child artwork survives replay/help/pace changes;
- step/session state is recoverable;
- lesson UI does not expose unrelated Free Draw/pro tools;
- left-handed policy may mirror controls where useful.

### LESSON-003 — Drawing Complete
Purpose: acknowledge completion and choose next action.

Primary actions:
- Color With Me;
- Color Myself;
- Finish for Now.

No numerical accuracy score is shown.

Completion must create a recoverable artwork record before transitioning onward.

## 6. Coloring flow

### COLOR-001 — Coloring Workspace
Immersive creation surface; child shell hidden.

Modes:
- guided coloring;
- self coloring.

Required:
- age-appropriate palette;
- supported brush/coloring tools;
- fill where lesson regions support it;
- eraser;
- size control where relevant;
- undo/redo;
- switch from guided to self-color where the lesson permits;
- safe exit/save behavior.

The companion may teach technique but must not insist on one color unless color recognition/theory is the lesson objective.

### COLOR-002 — Artwork Finished
Can reuse a shared completion presentation rather than becoming a visually unique full screen if implementation is cleaner.

Required outcomes:
- final artwork saved;
- View in Gallery;
- Back Home;
- optional next suitable lesson recommendation.

## 7. Free Draw flow

### FREE-001 — Free Draw Workspace
Immersive full-screen creation surface.

Required tools:
- pencil;
- crayon;
- marker/basic brush;
- eraser;
- color;
- size;
- undo/redo;
- clear;
- save.

Tool visibility adapts by age policy. The engine can support more capabilities than are visible.

The companion should default to quiet presence or be absent from the canvas unless invited/meaningfully useful.

## 8. Parent Zone

### PARENT-001 — Parent Gate
Used before:
- entering Parent Zone;
- outward artwork export/share;
- future purchase/external-link actions.

The gate must be understandable to an adult but not trivially bypassed by accidental child taps. Exact mechanism is defined during safety implementation review.

### PARENT-002 — Parent Hub
Required destinations:
- Child Preferences;
- Audio / Narration;
- Safety & Privacy;
- Storage / Content where applicable;
- About / Version.

### PARENT-003 — Child Preferences
Editable local profile settings:
- nickname;
- age/age band;
- handedness;
- default learning mode;
- default pace;
- interests;
- audio preference.

Changes that affect complexity/recommendations should be explained clearly.

### PARENT-004 — Safety & Privacy
Plain-language explanation of:
- what data is stored locally;
- permissions;
- network/cloud behavior;
- outward sharing controls;
- absence/presence of optional future services.

### PARENT-005 — Storage & Content
Only required to expose controls actually needed by V1. May include local storage usage and future downloaded-pack management.

### PARENT-006 — About / Version
Shows app version, build information useful for support, acknowledgements/legal links as required, and future support/contact entry if added.

## 9. Overlays, sheets and drawers — not standalone destinations

These should not become extra navigation screens unless implementation/accessibility requires it:

- UI-OVERLAY-001 — Pace Picker
- UI-OVERLAY-002 — Help Ladder / Help Choice
- UI-DRAWER-001 — Drawing Tools
- UI-DRAWER-002 — Color Palette / Tool Options
- UI-SHEET-001 — Safe Exit
- UI-SHEET-002 — Destructive Clear/Delete Confirmation
- UI-SHEET-003 — Lesson Pause State
- UI-SHEET-004 — Save/Discard decision when required

Keeping them as transient UI preserves the child's sense of remaining in the same artwork/session.

## 10. Safe exit/back behavior

### Onboarding
Back returns to the previous onboarding step without losing completed selections.

### Child shell
Each primary destination retains reasonable local state. Back from secondary detail surfaces returns to its source. Back from Home follows normal Android app-exit behavior.

### Guided Drawing / Coloring / Free Draw
System back or explicit close **must never immediately discard work**.

Open Safe Exit with context-appropriate actions:
- Keep Drawing;
- Save & Exit / Save for Later;
- Discard, followed by clear confirmation when actual work would be lost.

Guided lessons default to preservation/resume rather than discard.

### Completion
Once an artwork is committed as complete, Back must not silently revert it to an unfinished state.

### Parent Zone
Normal hierarchical back navigation; exiting returns to the protected child context that launched the gate where practical.

## 11. Resume/recovery contract

An unfinished creative session can store enough state to restore:
- artwork/document identifier;
- lesson identifier when relevant;
- lesson step;
- selected teaching mode;
- pace;
- current coloring/drawing phase;
- required tool state;
- child stroke document.

Home surfaces a single clear Continue Drawing entry. The app does not force resume on startup because the child may want a different activity.

## 12. Age-adaptive UI rules

### 4–5
- largest touch targets and visual cards;
- minimal simultaneous choices;
- narration/icon-first communication;
- Help highly visible;
- heavily curated tool drawer;
- avoid dense metadata and nested menus.

### 6–7
- labels plus icons;
- richer category browsing;
- moderate tool expansion;
- less default trace assistance.

### 8–9
- skill concepts and more lesson context;
- expanded studio controls;
- more independent navigation.

### 10–12
- denser but calm information hierarchy;
- technique vocabulary;
- selected advanced controls when justified by task.

Across all ages:
- touch targets should meet Android accessibility guidance;
- color must not be the only way to convey critical state;
- left-handed mode should prevent dominant-hand occlusion where practical;
- critical lesson actions should never require hidden gestures.

## 13. No-dead-end rules

Every V1 surface must provide a safe path forward or back:
- onboarding can move backward and resume;
- lesson preview can return to source;
- guided workspace can help, pause or save/exit;
- coloring can save/exit;
- Free Draw can save or intentionally discard;
- empty Gallery points to creation;
- Parent Gate can cancel safely;
- Parent Zone always returns to child context;
- optional content/network failure cannot strand core navigation.

## 14. Stable V1 screen list

Canonical screen IDs for implementation/tests:

- SYS-001 Startup Router
- ONB-001 Welcome / Name
- ONB-002 Age
- ONB-003 Learning Style
- ONB-004 Teaching Pace
- ONB-005 Interests
- ONB-006 Drawing Setup
- HOME-001 Studio Lobby
- LEARN-001 Explore
- LEARN-002 Category Collection
- JOURNEY-001 Art Journey Detail
- CREATE-001 Create Hub (may collapse to Free Draw route)
- GALLERY-001 My Art Gallery
- GALLERY-002 Artwork Detail
- LESSON-001 Lesson Preview
- LESSON-002 Guided Drawing Workspace
- LESSON-003 Drawing Complete
- COLOR-001 Coloring Workspace
- COLOR-002 Artwork Finished / shared completion surface
- FREE-001 Free Draw Workspace
- PARENT-001 Parent Gate
- PARENT-002 Parent Hub
- PARENT-003 Child Preferences
- PARENT-004 Safety & Privacy
- PARENT-005 Storage & Content
- PARENT-006 About / Version

This inventory defines information architecture only. Final layouts/components are selected during the visual-design workstream.