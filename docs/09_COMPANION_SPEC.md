# 09 — Companion V1 Behavior & Voice Contract

**Status:** Phase 0.6 implementation contract

## 1. Product role

The companion is the emotional teaching layer of the app, not decorative mascot art.

Its job is to make the child feel:
- safe to try;
- unhurried;
- noticed without being constantly judged;
- helped when stuck;
- proud of effort and progress;
- free to create differently from the example.

The companion must behave more like a patient art teacher sitting nearby than an arcade reward character.

## 2. Core behavior principles

1. **Quiet is a valid state.** The companion does not need to speak or animate continuously.
2. **Never shame.** No failure faces, red-X reactions, disappointment, sarcasm, comparison to other children, or language implying the child is bad at art.
3. **Do not overpraise every stroke.** Constant celebration becomes noise and makes meaningful praise feel fake.
4. **Praise process more than correctness.** Examples: effort, trying again, noticing shapes, using help, completing a step.
5. **Creative differences are allowed.** Unless a lesson explicitly teaches a constrained concept, the child's different colors/shapes are not called wrong.
6. **React to real product events.** Companion state comes from semantic engine signals, not random animation timers pretending to understand the child.
7. **The canvas wins.** The companion always yields space to artwork and critical controls.
8. **Voice is additive, never required for comprehension.** Every important instruction also has visual/textual support.

## 3. Semantic state model

Canonical V1 states:

```text
DORMANT
IDLE_PRESENT
GREETING
INTRODUCING_ACTIVITY
TEACHING
DEMONSTRATING
WATCHING_CHILD
WAITING_PATIENTLY
ENCOURAGING
HELPING
THINKING
CELEBRATING_STEP
CELEBRATING_ARTWORK
COLOR_COACHING
GENTLE_ERROR
PAUSED
GOODBYE
```

These are semantic states. Exact animation clips are presentation assets selected by Companion Engine.

### DORMANT
Companion is hidden or effectively inactive.

Used when:
- Free Draw child has chosen a distraction-free canvas;
- a screen has no meaningful companion purpose;
- reduced-stimulation policy requests minimal presence.

### IDLE_PRESENT
Quiet, low-motion presence. No unsolicited speech.

### GREETING
Warm first/return greeting. Brief and non-demanding.

### INTRODUCING_ACTIVITY
Explains what will happen next in child-friendly language.

### TEACHING
Delivers a concise instruction or concept.

### DEMONSTRATING
Visually attends to teacher drawing playback. Speech is minimal once the demonstration starts.

### WATCHING_CHILD
Quietly observes during the child's turn. This should be one of the most common states.

### WAITING_PATIENTLY
Used after meaningful inactivity. Communicates no pressure.

### ENCOURAGING
Gentle support triggered by authored/interaction signals, not every imperfect mark.

### HELPING
Coordinates with Help Ladder presentation.

### THINKING
Short transition for requests such as choosing help/color suggestion. Never used to fake AI reasoning for long periods.

### CELEBRATING_STEP
Small reaction for a meaningful step completion.

### CELEBRATING_ARTWORK
Larger but still tasteful celebration for completing drawing/coloring/artwork.

### COLOR_COACHING
Suggests coloring technique or palette ideas while preserving creative freedom.

### GENTLE_ERROR
Used for product/content failure. Language never blames child.

### PAUSED
Calm paused presence; animation can settle/rest.

### GOODBYE
Brief closure when saving/exiting a session.

## 4. Priority system

Higher-priority states can interrupt lower-priority presentation.

Suggested priority classes:

### P0 — Safety/system-critical presentation
- GENTLE_ERROR when an action requires child/parent attention
- parent-gate/system handoff visuals where relevant

### P1 — Direct child request
- HELPING
- requested replay/explanation response
- requested color/tool guidance

### P2 — Teaching progression
- INTRODUCING_ACTIVITY
- TEACHING
- DEMONSTRATING
- COLOR_COACHING when authored as required instruction

### P3 — Meaningful progress reaction
- CELEBRATING_ARTWORK
- CELEBRATING_STEP
- ENCOURAGING

### P4 — Ambient presence
- GREETING after initial moment
- WAITING_PATIENTLY
- WATCHING_CHILD
- IDLE_PRESENT
- PAUSED

Rules:
- higher priority may interrupt lower priority cleanly;
- lower priority waits/drops rather than queueing a long backlog;
- repeated identical low-priority signals coalesce;
- celebration cannot interrupt a direct Help request;
- optional chatter never interrupts required teaching.

## 5. Rate limiting / anti-annoyance rules

### During child drawing
Default behavior is silence.

Companion should not speak more frequently than necessary. Initial V1 policy:
- no unsolicited praise for every stroke;
- after a step begins, allow a quiet observation window before any encouragement;
- one waiting/encouragement prompt should not repeat until there is new meaningful interaction or a substantially longer inactivity period;
- repeated undo/erase does not trigger repeated commentary;
- rapid step completions can use visual micro-reactions without full spoken lines.

Exact time thresholds remain configurable product policy and are tuned through child testing rather than hard-coded into lesson data.

## 6. Waiting behavior

The companion must never communicate impatience.

Good examples:
- “Take your time. I’m right here.”
- “No hurry.”
- “Want to see that part again?”

Avoid:
- countdowns;
- repeated “Are you done?”;
- sighing/tapping/impatient animation;
- pressure language;
- automatic step advancement merely because time passed.

## 7. Encouragement language

Preferred patterns:
- “Nice trying!”
- “You found the curve.”
- “That’s your version — cool.”
- “Want a little hint?”
- “You kept going.”
- “Great, we finished this part.”

Avoid blanket superlatives every time:
- “Perfect!”
- “Amazing!”
- “Best drawing ever!”

Use strong celebration for meaningful milestones so it remains believable.

## 8. Help behavior

Help Ladder remains owned by Lesson Engine. Companion presents the semantic transition.

Example mapping:
- Hint → verbal/visual clue;
- Guide → companion points attention toward the guide area;
- Direction/Anchors → concise explanation of start/direction;
- Trace → reassuring instruction that tracing is a normal way to learn;
- Assisted Success → supportive transition onward without calling it a failure.

Never say “You couldn’t do it, so I’ll do it for you.”

Preferred: “Let’s do this one together.”

## 9. Placement rules

### Global rule
Companion owns no fixed screen corner unconditionally.

Layout provides a **Companion Safe Zone** calculated around:
- current artwork bounds/lesson focus bounds;
- tool rail;
- primary action controls;
- hand-dominance UI mirroring;
- system bars/insets.

If no safe area exists, companion reduces size, collapses to a portrait/bubble, or disappears temporarily.

### Home / Explore
Companion can be medium/large and expressive because artwork interaction is not being occluded.

### Lesson Preview
Companion may introduce the subject but should not compete with the lesson preview art.

### Guided Drawing Workspace
Companion must be compact.

Preferred placements:
- outside primary canvas where tablet layout permits;
- small anchored edge position;
- temporary head/portrait bubble for speech;
- automatically relocate when authored teacher stroke/focus region overlaps the current safe zone.

The character never sits over the current teacher stroke or likely child target area.

### Coloring Workspace
Same canvas-safe rule. When suggesting a color, palette emphasis should occur near palette UI rather than the character physically covering the artwork.

### Free Draw
Default = quiet/minimal presence. Child can use the studio without being coached continuously.

## 10. Companion visual scale states

Presentation layer supports conceptually:
- `FULL` — onboarding/Home/activity introductions;
- `COMPACT` — lesson/coloring workspace;
- `BUBBLE` — speech/reaction only;
- `HIDDEN` — distraction-free or no safe placement.

Semantic state and scale are separate. Example: `HELPING + BUBBLE` is valid.

## 11. Animation rules

V1 character animation should prioritize expressive economy over hundreds of clips.

Minimum useful motion library:
- breathe/idle;
- wave/greet;
- look/point toward canvas;
- teaching gesture;
- watch/lean-in;
- gentle nod;
- encourage;
- help/point;
- think;
- small celebrate;
- large celebrate;
- color/palette gesture;
- gentle error/confused-but-calm;
- pause/rest;
- goodbye.

Rules:
- loops must be subtle enough for long drawing sessions;
- no constant bouncing/flashing;
- animation must not convey disappointment;
- semantic state can select among several cosmetic variants later without changing engine behavior;
- decorative animation failure cannot block lesson progression.

## 12. V1 voice personality

Voice characteristics:
- warm;
- calm;
- patient;
- conversational;
- short sentences;
- clear pronunciation;
- never baby-talk by default;
- never teacher-authoritarian;
- no exaggerated YouTube-kids shouting tone.

The same companion can adapt language complexity by age without becoming a different personality.

### Ages 4–5
- shortest phrases;
- concrete words;
- one instruction at a time;
- more visual pointing/repetition;
- avoid art jargon unless immediately explained.

### Ages 6–7
- concise explanations;
- basic shape/line vocabulary;
- friendly choices.

### Ages 8–9
- introduce technique language gradually;
- slightly less repetition;
- acknowledge independent problem-solving.

### Ages 10–12
- more mature tone;
- less mascot chatter;
- real art vocabulary such as proportion, highlight, shadow, contour when appropriate;
- avoid language that feels preschool-oriented.

## 13. Narration/voice technical policy

V1 critical path requires no paid/cloud voice API.

Priority:
1. bundled prerecorded voice for high-value signature lines where available;
2. Android local/system TextToSpeech for scalable authored narration when suitable voices are available;
3. on-screen text + visual cue fallback if voice is unavailable/disabled.

No lesson state may depend on cloud TTS response.

Narration Service reports capability and completion/failure, but Lesson Engine does not use assumed audio duration as its state-machine clock.

## 14. Voice interruption policy

Priority examples:
- direct Help response interrupts optional chatter;
- required instruction can stop prior ambient line;
- optional celebration waits/drops if the next teaching instruction is ready;
- Pause stops lesson-owned narration cleanly;
- replay can replay authored instruction only when pedagogically useful, not automatically every time.

Never queue multiple stale praise lines after rapid child actions.

## 15. Child-safety language rules

Companion must not:
- ask for secrets, address, phone, school, precise location or unrelated personal information;
- encourage leaving the app or contacting strangers;
- create parasocial exclusivity such as “I’m your only friend”;
- guilt the child for leaving or missing sessions;
- use body/appearance judgments;
- shame mistakes;
- imply real sentience/emotions in ways designed to manipulate attachment;
- make purchases/subscription pressure child-facing.

Companion may use the locally stored nickname in normal art-teaching context.

## 16. Error language

System problem language is calm and ownership-neutral.

Examples:
- “That part didn’t load. Your drawing is safe.”
- “The voice isn’t working right now, but we can keep drawing.”
- “I couldn’t show that guide. Want to try again?”

Never:
- “You did something wrong.”
- “You broke it.”

## 17. Reduced motion / accessibility

When Reduce Motion is active or animation capability is constrained:
- replace large movement with expression/pose changes;
- avoid bouncing/translations;
- preserve semantic communication through voice/text/iconography;
- celebration can use a static happy pose and lightweight non-flashing feedback;
- no critical instruction depends only on animation.

Voice-off mode remains fully usable.

## 18. Companion Engine API direction

Conceptual interface:

```text
CompanionEngine
  observeState(): StateFlow<CompanionPresentationState>
  submit(signal: CompanionSignal)
  setContext(screenContext, safeZones, agePolicy)
  setVoiceEnabled(enabled)
  setMotionPreference(preference)
  dismissOptionalSpeech()
```

`CompanionSignal` comes from product semantics, for example:
- `ActivityStarted`
- `TeachingInstruction`
- `TeacherDemoStarted`
- `ChildTurnStarted`
- `ChildRequestedHelp`
- `MeaningfulStepCompleted`
- `ArtworkCompleted`
- `ColorTip`
- `RecoverableProblem`
- `SessionPaused`
- `SessionEnding`

## 19. Companion presentation state

Conceptually includes:
- semantic state;
- priority;
- scale mode;
- placement/safe-zone result;
- selected animation/pose ID;
- optional speech payload;
- optional speech bubble text;
- interruptibility;
- accessibility adaptation flags.

Exact asset/animation IDs remain presentation implementation, not Lesson Engine API.

## 20. Test matrix

Automated/unit tests should cover:
- priority interruption;
- repeated low-priority signal coalescing;
- optional chatter dropping;
- Help interrupting celebration;
- voice disabled fallback;
- voice failure fallback;
- reduced motion state mapping;
- age-policy copy selection;
- safe-zone fallback FULL → COMPACT → BUBBLE → HIDDEN;
- error signal never producing blame copy;
- Free Draw default quiet policy;
- lesson state continuing when animation/voice fails.

Visual/device testing covers companion occlusion against representative lesson focus regions and left/right-handed layouts.

## 21. V1 acceptance gate

Companion contract is satisfied when:
- semantic state is driven by product events;
- companion never owns lesson progression;
- no critical action requires companion voice/animation;
- canvas-safe placement rules prevent critical obstruction;
- rate limiting prevents repetitive chatter;
- age 10–12 experience does not feel preschool-oriented;
- voice has a ₹0/offline-capable fallback path;
- reduced-motion/voice-off modes remain coherent;
- failure of companion/voice cannot block drawing or coloring;
- child-safety language rules are enforced in authored copy review.

## 22. Explicitly later

- unrestricted conversational AI companion;
- cloud LLM dependency;
- open-ended voice chat;
- emotional-state inference from microphone/camera;
- child relationship scoring;
- multiple selectable companion personalities;
- complex procedural full-body animation system;
- personalized generative dialogue.