# 03 — UX Architecture v0.2

## 1. UX principle

The product should feel like entering a calm art studio with a patient teacher, not opening a productivity dashboard. During creation, the canvas is always the hero and interface chrome recedes.

Age adaptation changes visible complexity, language, help defaults, and tool density—not merely recommendations.

## 2. Primary child destinations

### Home / Studio Lobby
Primary personalized landing area containing:
- Continue Drawing when relevant;
- recommended lessons;
- Art Journeys;
- categories/explore;
- Free Draw;
- Gallery;
- lightweight companion/profile entry.

### Learn / Explore
Browse lesson content by age-appropriate category, subject, skill, difficulty and journey membership.

### Art Journeys
Curated lesson sequences that teach a coherent progression rather than acting as another category grid.

### Free Draw Studio
Blank-canvas creative space with age-progressive tool complexity.

### Gallery
Personal collection of completed/saved artwork.

## 3. Parent Zone

Separated from child mode through a parent gate. Initial responsibilities:
- edit child/local profile preferences;
- manage audio/narration;
- safety/privacy information;
- parent-gated artwork export/share;
- local content/storage controls required by V1;
- app/version information;
- future purchase/account controls if those features are introduced.

The parent area should feel calm and informational rather than child-styled, while remaining visually consistent with the product.

## 4. Core V1 child journeys

### Journey A — First launch
1. Welcome from companion.
2. Enter nickname.
3. Choose age/age band.
4. Choose preferred learning style: Draw With Me / Watch Then Draw / Trace & Learn.
5. Choose initial pace.
6. Choose favorite subjects/interests.
7. Choose handedness.
8. Choose narration/audio preference.
9. Enter personalized Home.
10. Home prominently recommends a first suitable lesson and Free Draw.

Requirements:
- no account creation;
- no full birthday requirement;
- every step can be understood visually and/or through narration;
- interruption can recover without forcing complete restart.

### Journey B — Start and complete a guided drawing
1. Child chooses a lesson.
2. Lesson preview shows subject, approximate time, difficulty in child-friendly language, and supported modes.
3. Companion introduces the activity briefly.
4. Teacher demonstrates the first authored action.
5. Child attempts the step.
6. Child can pause, replay, change pace or request help.
7. Lesson progresses step by step.
8. Completion moment acknowledges the finished drawing without exaggerated scoring.
9. Child chooses Color With Me / Color Myself / Finish for Now.
10. Artwork is saved and appears in Gallery.

### Journey C — Child is stuck
1. Child selects Help or the lesson offers a gentle help prompt.
2. Current work remains untouched.
3. Help Ladder increases only as needed: hint → guide → anchors/direction → trace → assisted success.
4. Child can dismiss help and continue independently at any level.
5. Lesson continues from the same state.

There is no fail screen.

### Journey D — Coloring
1. Completed line art opens in Coloring.
2. Guided mode may demonstrate technique/area/order while preserving color choice unless color is the lesson objective.
3. Child may switch to self-coloring where allowed.
4. Undo/redo and eraser remain available.
5. Child saves or finishes.
6. Final artwork replaces/updates the Gallery presentation without losing the original lesson provenance.

### Journey E — Free Draw
1. Child taps Free Draw from Home.
2. Blank canvas opens immediately.
3. Visible tools are selected by age policy.
4. Child creates without lesson scoring or mandatory prompts.
5. Companion remains optional/minimal and does not interrupt creation.
6. Child saves artwork to Gallery or exits through a clear discard/save decision.

### Journey F — Resume interrupted work
1. App detects an incomplete lesson/artwork session.
2. Home surfaces Continue Drawing.
3. Opening restores canvas content, lesson step, selected pace/mode, and necessary tool state.
4. Child continues without replaying completed steps unless requested.

### Journey G — Gallery pride loop
1. Child opens Gallery.
2. Artwork appears as a visual collection rather than a data table.
3. Child can open a piece and see simple contextual information.
4. Outward sharing/export is not directly available to child mode without parent gate.
5. Future progress storytelling may highlight improvement without ranking one artwork as objectively better.

## 5. Parent journeys

### Parent Journey A — Change settings
1. Parent enters Parent Zone through gate.
2. Selects local profile/settings.
3. Changes age band, handedness, narration, pace default or other permitted preferences.
4. App explains when a change affects recommendations or tool complexity.
5. Settings persist locally.

### Parent Journey B — Export artwork
1. Child/parent selects export from Gallery.
2. Parent gate appears before leaving the protected app flow.
3. Parent chooses supported Android share/export destination.
4. Original local artwork remains intact if sharing is cancelled or fails.

### Parent Journey C — Understand safety/privacy
1. Parent enters Parent Zone.
2. Opens Safety & Privacy.
3. Sees plain-language summary of local storage, data collected, network/cloud behavior and permissions.
4. Any future optional cloud feature must be clearly separated from required core use.

## 6. Drawing lesson workspace hierarchy

Priority order:
1. Canvas.
2. Current teacher demonstration / child action.
3. Essential lesson controls: pause, replay, pace, help.
4. Companion presence/feedback.
5. Drawing tool controls required by the current authored step.
6. Secondary navigation/tools only when explicitly opened.

No permanent toolbar should cover useful drawing space merely because the engine supports more tools.

## 7. Age-adaptive presentation

### 4–5
- largest controls;
- strongest icon/narration reliance;
- minimal simultaneous choices;
- help visible and easy to invoke;
- tool drawer heavily curated.

### 6–7
- modestly richer navigation;
- more category choice;
- labels alongside icons where helpful;
- less persistent trace assistance.

### 8–9
- more lesson metadata and skill concepts;
- expanded studio controls;
- more independent learning flow.

### 10–12
- denser but still calm workspace;
- more technique vocabulary;
- selected advanced studio/lesson tools when useful.

## 8. Recovery and error philosophy

- Never erase artwork because a lesson/navigation error occurred.
- Auto-save working state at safe boundaries and/or incrementally where technically appropriate.
- Backgrounding the app must not silently restart the lesson.
- Invalid/corrupt optional content should be quarantined rather than breaking the whole library.
- Network failure must not interrupt bundled/core content.
- Destructive actions require clear confirmation appropriate to the surface.

## 9. Navigation rule

The child should never need to understand Android back-stack behavior to recover from a creative session. Every creation surface needs an intentional exit flow that makes Save / Continue / Discard consequences understandable.