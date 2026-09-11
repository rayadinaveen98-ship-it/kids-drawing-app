# 21 — Visual System & Product Art Direction

**Status:** Phase 0.6 visual direction contract  
**Working direction name:** Premium Storybook Art Studio

## 1. Visual promise

The app should feel like entering a warm, premium children's art studio — playful enough for a 4-year-old, calm and tasteful enough that a 12-year-old does not feel they are using a toddler product.

The artwork/canvas is always the visual hero. UI supports creativity rather than competing with it.

## 2. What this is NOT

Avoid:
- rainbow-everywhere preschool dashboards;
- neon gaming UI;
- candy gradients on every surface;
- excessive badges, coins or streak chrome;
- glassmorphism that harms readability or is difficult to reproduce consistently in Compose;
- tiny adult-product controls scaled down for children;
- mascot covering canvas/content;
- overly childish bubble fonts throughout;
- heavy shadows and floating cards everywhere;
- decorative motion that never settles.

## 3. Core visual language

Keywords:
- warm
- tactile
- storybook
- artistic
- calm
- premium
- friendly
- handcrafted
- spacious
- expressive but controlled

The base environment is a lightly warm paper/studio surface rather than clinical pure white.

## 4. Color direction

### Foundation neutrals
- `Paper/50` — #FFFDF8 — primary light canvas/screen background
- `Paper/100` — #FAF6ED — secondary warm surface
- `Ink/900` — #242321 — primary text
- `Ink/700` — #4E4A45 — secondary text
- `Ink/500` — #79736C — muted metadata
- `Line/200` — #E8E0D4 — separators/borders when needed

### Brand / interaction
- `Studio/600` — #5C6F52 — calm moss-green primary action
- `Studio/500` — #718567 — standard brand accent
- `Studio/100` — #EAF0E5 — soft selected/background accent

### Creative accents
These are supporting accents, not equal-weight brand colors:
- `Sun/500` — #E9A94A — achievement/warm highlight
- `Coral/500` — #E47C68 — playful attention / selected creative item
- `Sky/500` — #6C9CB8 — cool informational accent
- `Lavender/500` — #9A83B8 — imagination/story accent

### Safety/status
Use standard accessible semantic color treatment but pair with text/icon:
- success — calm green
- caution — amber
- error — muted red, never aggressive alarm red for child-facing recoverable issues

## 5. Color discipline

A screen should normally have:
- one dominant neutral family;
- one primary brand accent;
- at most one or two supporting creative accents.

Lesson content artwork may be colorful. Product chrome should remain restrained so the child's drawing is the richest color object on screen.

## 6. Typography

### V1 primary typeface
Use **Nunito Sans** when bundled/available, with Android system sans fallback.

Reason:
- friendly rounded construction without becoming a novelty child font;
- highly readable at large and small sizes;
- mature enough for ages 10–12;
- free/open-font ecosystem suitability.

Do not use a decorative display typeface for body/UI text.

### Type hierarchy direction
- Display / onboarding moment: 32–40sp, bold/extra-bold, short copy only
- Screen title: 28–32sp, bold
- Section title: 20–24sp, bold/semi-bold
- Card title: 17–20sp, semi-bold/bold
- Body: 16–18sp for child-facing instructional content
- Metadata: 13–15sp, only for older age bands / parent zone
- Tool label: minimum comfortable child-readable size; icon-only permitted only for universally clear/reinforced tools

Younger age policy increases size and reduces simultaneous text rather than using a different font.

## 7. Shape language

### Corner radii
- small control: 12dp
- standard button/chip: 16dp
- card: 20dp
- large feature card/panel: 24dp
- hero/young-child feature surface: up to 28dp

Avoid turning every element into a pill. Pills are reserved for filters, compact status and pace/tool selectors.

## 8. Spacing scale

Base 4dp system:
- 4
- 8
- 12
- 16
- 20
- 24
- 32
- 40
- 48
- 64

Preferred content gutters:
- phone: 20–24dp
- tablet: 28–40dp depending on width

Young-child screens use more whitespace and fewer objects, not simply larger versions of dense screens.

## 9. Depth and borders

Default UI should feel mostly flat/tactile rather than floating.

Use:
- surface contrast first;
- subtle 1dp warm border where separation is needed;
- low, soft elevation only for interactive floating toolbars/temporary sheets;
- stronger depth rarely for modal focus.

Canvas/workspace must not look like a stack of Material cards.

## 10. Iconography

Direction:
- rounded, simple, solid/outlined family with consistent stroke weight;
- tools may use recognisable physical metaphors (pencil, crayon, eraser, palette);
- labels accompany less-obvious icons for younger children;
- no thin 1px adult-dashboard icons for child actions.

## 11. Illustration direction

Lesson thumbnails and category art:
- hand-drawn or painterly-vector hybrid;
- clean silhouette/readability;
- slight imperfection/texture is welcome;
- avoid generic clip-art stock feel;
- avoid hyper-detailed 3D rendering for basic lesson catalog cards;
- subject should remain visually understandable at thumbnail scale.

## 12. Companion art direction

V1 companion should be:
- 2D;
- simple enough to animate economically;
- appealing across 4–12 rather than baby-proportioned;
- expressive through eyes, brows, mouth, head angle and arms;
- able to hold/use pencil, brush, palette and eraser props;
- designed with compact/speech-bubble crops in mind;
- visually related to Studio green + warm neutral palette but not monochromatic.

Avoid giant head/tiny body preschool styling. A stylized animal or small imaginative creature is preferable to a human teacher for broad cultural neutrality and animation simplicity.

Final species/name is a branding decision, not required for engine implementation.

## 13. Motion principles

Motion should communicate:
- continuity;
- cause/effect;
- teaching focus;
- gentle delight.

Typical UI motion:
- 150–220ms small control response;
- 220–320ms card/sheet transitions;
- slower companion/story moments only when they do not delay child control.

No critical drawing input waits for decorative animation.

Respect system reduced-motion preference.

## 14. Child shell visual structure

Primary destinations:
- Home
- Learn
- Create
- Gallery

Navigation:
- large, calm, clearly separated targets;
- selected state uses Studio tint/fill + icon/text emphasis;
- no red notification badges for engagement pressure;
- tablet layouts may use a rail when it materially improves canvas/content space.

## 15. Home — selected direction

Home should feel like a studio lobby, not a content-feed app.

### Top area
- friendly short greeting + companion presence;
- no huge app logo taking vertical space after onboarding;
- parent-zone entry visually quiet and protected.

### Primary hero
A single large **Continue / Draw Together** studio card:
- warm illustrated lesson art;
- title;
- one progress/context line;
- large action affordance;
- companion may appear integrated at card edge.

### Below
- “Picked for you” lesson row;
- Art Journey feature card;
- Free Draw studio card;
- recent artwork strip.

Card density adapts by age: fewer/larger for 4–5; more efficient for older children.

## 16. Onboarding — selected direction

One decision per screen.

Visual structure:
- generous paper background;
- companion/illustration on one side/top;
- one strong question;
- 2–6 large visual choice cards;
- one obvious Continue action;
- progress represented softly, not as a stressful numbered test.

Example Age screen should not resemble a form. Use large age/age-band choices with friendly art cues.

Learning-style cards must visually demonstrate Draw With Me / Watch Then Draw / Trace & Learn.

## 17. Guided Drawing Workspace — selected direction

The workspace is intentionally calmer than Home.

### Priority
1. artwork canvas
2. current demonstration/guide
3. essential lesson controls
4. tools required by this lesson
5. compact companion

### Layout
Phone landscape/tablet preferred during drawing when product testing supports it; architecture must remain responsive.

- warm-neutral outer workspace
- bright/paper canvas region
- compact tool rail on dominant-hand-opposite side where practical
- pace/pause/help/replay grouped away from brush tools
- companion in safe-zone compact/bubble mode
- current instruction displayed in short sentence/bubble outside the actual target drawing region

Avoid permanent top/bottom bars that consume large portions of canvas.

## 18. Coloring Workspace

Keep the same spatial grammar as guided drawing so the child does not feel transferred into another app.

Differences:
- palette becomes visually prominent;
- coloring tools replace unnecessary drawing tools;
- guided coloring can softly highlight the active authored region without flashing or aggressive outlines;
- line art remains high-contrast enough above color fills.

## 19. Free Draw Studio

More tool-forward but still calm.

Younger bands:
- visible pencil/crayon/marker/eraser + colors + undo;
- advanced controls hidden.

Older bands:
- denser expandable tool shelf;
- size/opacity where supported;
- future layers/guides slots can exist architecturally but not as disabled clutter.

Companion is hidden or quiet by default.

## 20. Gallery

Gallery should feel like a child's personal art wall.

- large artwork thumbnails with paper/mat framing feel;
- minimal metadata;
- no social counts;
- milestones can be celebrated through curation (“Your first animal”, “Look how your cats changed”) rather than competitive ranking.

## 21. Parent Zone

Parent Zone intentionally shifts slightly more mature:
- denser information hierarchy;
- smaller but still accessible controls;
- same color/token system;
- companion mostly absent;
- clear privacy/safety language.

This separation prevents child UI from becoming settings-heavy.

## 22. Age-adaptive density tokens

### Little Artists — 4–5
- XL touch targets
- 1 major decision/section at a time
- 2–4 visible choices when possible
- large preview art
- minimal metadata
- companion FULL/COMPACT more often

### Creative Explorers — 6–7
- large targets
- 3–6 choices comfortably
- text+icon labels
- moderate catalog density

### Growing Artists — 8–9
- standard-large targets
- richer lesson metadata
- more compact browsing
- companion less dominant

### Young Artists — 10–12
- mature spacing/density
- technique labels and compact metadata
- reduced mascot scale/chatter
- still avoids adult productivity UI

## 23. Accessibility baseline

- target WCAG-appropriate text/background contrast;
- do not rely on color alone for state;
- Android minimum touch-target guidance is the floor, not the goal for younger bands;
- voice instructions always have visual/text equivalents;
- support system font scaling without clipping critical controls;
- reduced motion respected;
- left-handed layouts considered at workspace level;
- important actions use recognizable icon + text where ambiguity exists.

## 24. Compose implementation rule

The visual system must translate cleanly into reusable Compose tokens/components:

```text
StudioTheme
  ├── StudioColors
  ├── StudioTypography
  ├── StudioSpacing
  ├── StudioShapes
  ├── StudioElevation
  └── StudioMotion
```

Age policy decorates component sizing/density; it should not create four unrelated design systems.

## 25. Initial component inventory

- StudioPrimaryButton
- StudioSecondaryButton
- ChoiceCard
- LessonCard
- JourneyCard
- ArtworkCard
- ToolButton
- ColorSwatch
- PaceSelector
- CompanionBubble
- InstructionBubble
- ProgressPill
- StudioNavigation
- SafeExitSheet
- ParentSettingRow

## 26. Figma workspace

Design file created for this workstream:

`Kids Drawing App — Phase 0.6 Visual System`

File key: `2lGC11EPu2tjgrpYivJ8hf`

The file was successfully created and inspected as an empty design file. Further MCP canvas generation was blocked by the current Figma Starter-plan MCP call limit on 2026-09-11. This is a tooling quota, not a product blocker.

When the free tool-call window permits, the first three production-realistic frames to build are:
1. ONB-003 Learning Style
2. HOME-001 Studio Lobby
3. LESSON-002 Guided Drawing Workspace

Those frames must implement this document rather than invent a separate visual language.

## 27. Visual direction decision

**Selected direction: Premium Storybook Art Studio.**

This direction is locked as the Phase 0 design target unless real child/parent usability testing exposes a specific problem. Visual polish may evolve, but the principles above remain the baseline.