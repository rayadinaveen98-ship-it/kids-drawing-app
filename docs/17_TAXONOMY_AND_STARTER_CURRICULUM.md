# 17 — Taxonomy & Starter Curriculum

**Status:** Phase 0.4 curriculum contract

## 1. Taxonomy principles

Content metadata must answer different questions independently:

- **Category** — what is being drawn?
- **Skill** — what is being practiced?
- **Difficulty** — how demanding is this lesson?
- **Age band** — which experience policies/users is this lesson appropriate for?
- **Journey** — does it belong to a deliberate learning progression?

Do not use one field as a substitute for another. A simple cat and an advanced cat can share the same category while targeting different difficulty/skill/age metadata.

## 2. Stable age-band IDs

- `little_artists` — approximately 4–5
- `creative_explorers` — approximately 6–7
- `growing_artists` — approximately 8–9
- `young_artists` — approximately 10–12

A lesson may support multiple bands.

## 3. Difficulty scale

Difficulty is content complexity, not child worth or permanent skill level.

### Difficulty 1 — First Marks
Very simple forms, few conceptual decisions, strong optional guidance.

### Difficulty 2 — Simple Construction
Several basic shapes/curves combined into a recognizable subject.

### Difficulty 3 — Developing
More parts, proportion/placement decisions, richer detail and less dependence on tracing.

### Difficulty 4 — Challenge
Technique-focused construction, more deliberate proportion/composition/shading or pose complexity.

### Difficulty 5 — Advanced Youth
The most demanding V1/later youth material: perspective, anatomy/detail, value, character/environment construction or other advanced concepts.

Difficulty does not directly dictate step count; authors use the fewest pedagogically sensible steps.

## 4. Category taxonomy

### `foundations`
Core mark-making and visual-building blocks.

Suggested subcategories:
- `foundations.lines`
- `foundations.shapes`
- `foundations.patterns`

### `animals`
Suggested subcategories:
- `animals.pets`
- `animals.wild`
- `animals.birds`
- `animals.ocean`
- `animals.insects`
- `animals.dinosaurs`

### `nature`
Suggested subcategories:
- `nature.plants`
- `nature.flowers`
- `nature.landscapes`
- `nature.weather`

### `everyday`
Suggested subcategories:
- `everyday.objects`
- `everyday.food`
- `everyday.toys`
- `everyday.clothing`

### `vehicles`
Suggested subcategories:
- `vehicles.land`
- `vehicles.air`
- `vehicles.water`

### `space`
Suggested subcategories:
- `space.planets`
- `space.rockets`
- `space.astronauts`
- `space.aliens`

### `people`
Suggested subcategories:
- `people.faces`
- `people.expressions`
- `people.bodies`
- `people.poses`

### `characters`
Suggested subcategories:
- `characters.cartoon`
- `characters.fantasy`
- `characters.robots`

### Later/expansion category families
- `buildings`
- `architecture`
- `comics`
- `fashion`
- `culture.folk_art`
- `seasonal`
- `story_scenes`

The taxonomy can expand, but released IDs should not be casually renamed because saved progress/recommendations may reference them.

## 5. Skill taxonomy

Skill IDs are intentionally reusable across subjects.

### Mark-making / control
- `line.straight`
- `line.curve`
- `line.zigzag`
- `line.loop`
- `line.control`
- `stroke.direction`
- `stroke.closure`

### Shape construction
- `shape.circle`
- `shape.ellipse`
- `shape.square`
- `shape.rectangle`
- `shape.triangle`
- `shape.organic`
- `shape.combine`

### Placement / proportion
- `placement.relative`
- `placement.symmetry`
- `placement.spacing`
- `proportion.basic`
- `scale.relative`

### Form / depth
- `overlap.basic`
- `contour.simple`
- `volume.basic`
- `perspective.one_point`
- `perspective.depth_scale`

### Composition / scene building
- `composition.centering`
- `composition.balance`
- `scene.foreground_background`

### People / character
- `anatomy.face_basic`
- `anatomy.body_basic`
- `expression.face`
- `pose.simple`
- `character.silhouette`
- `character.design`

### Detail / value
- `detail.layering`
- `texture.simple`
- `shading.light_dark`
- `shading.direction`

### Coloring
- `color.fill_control`
- `color.palette_choice`
- `color.warm_cool`
- `color.value`
- `color.blending_basic`

### Creativity / storytelling
- `creativity.variation`
- `creativity.imagination`
- `storytelling.scene`
- `storytelling.character`

Not every listed skill must appear in public V1 content. IDs define a scalable vocabulary.

## 6. Onboarding interest policy

Interest choices should be driven by content that actually exists in the installed catalog.

V1 should not ask a child to choose an interest such as Fantasy and then offer almost no appropriate lessons.

Recommended V1 interest families:
- Animals
- Nature
- Vehicles
- Space
- Food / Everyday Things
- Characters

Additional interests become selectable when catalog depth justifies them.

## 7. Starter Art Journeys

Public V1 requires at least three coherent journeys. The planned baseline uses four so all age bands have meaningful progression.

### Journey A — `journey.first_shapes_to_pictures`
**Primary audience:** Little Artists / early Creative Explorers  
**Goal:** discover that lines and shapes can become recognizable pictures.

Indicative progression:
1. Happy Lines — straight, curve, zigzag and loop marks.
2. Shape Friends — circle, square, triangle and simple combination.
3. Smiling Sun — circle + controlled rays.
4. Little Fish — ellipse/curve + triangle + simple detail.
5. Easy Flower — circle/curves + repeated petals.
6. Simple Cat — circles/triangles + relative placement.

Core skills:
`line.control`, `line.curve`, `shape.circle`, `shape.triangle`, `shape.combine`, `placement.relative`.

### Journey B — `journey.animal_artist`
**Primary audience:** Creative Explorers / Growing Artists  
**Goal:** learn how simple construction shapes build increasingly complex animals.

Indicative progression:
1. Fish
2. Snail
3. Cat
4. Dog
5. Owl
6. Elephant
7. Lion
8. Horse

Progression adds symmetry, overlapping forms, relative scale and detail layering.

### Journey C — `journey.space_artist`
**Primary audience:** Creative Explorers / Growing Artists  
**Goal:** move from iconic shapes toward small imaginative scenes.

Indicative progression:
1. Star & Moon
2. Planet With Rings
3. Rocket
4. Friendly Alien
5. Astronaut
6. Moon Landscape
7. Design Your Own Planet

Core skills include shape combination, symmetry, scene placement and creative variation.

### Journey D — `journey.character_creator`
**Primary audience:** Growing Artists / Young Artists  
**Goal:** build an original stylized character from understandable components.

Indicative progression:
1. Face Shape & Guidelines
2. Eyes / Nose / Mouth Basics
3. Expressions
4. Hair Shapes
5. Simple Body Construction
6. Clothes & Accessories
7. Simple Poses
8. Create Your Own Character

Core skills include face placement, symmetry, expression, proportion, silhouette and creative variation.

## 8. Public V1 content allocation target

Target catalog: **36 guided lessons**. Hard release floor remains **24** if quality is substantially higher and all age/interest promises remain credible.

Planned target allocation:

| Category family | Target lessons |
| --- | ---: |
| Foundations | 6 |
| Animals | 8 |
| Nature | 5 |
| Everyday / Food / Toys | 4 |
| Vehicles | 4 |
| Space | 4 |
| People / Characters | 5 |
| **Total** | **36** |

Lessons can appear in more than one journey/category context without being duplicated as separate content packages.

## 9. Age-band coverage target

Because lesson suitability overlaps, totals below are not additive.

At public V1 target, aim for at least:
- 8 lessons suitable for `little_artists`;
- 16 lessons suitable for `creative_explorers`;
- 18 lessons suitable for `growing_artists`;
- 16 lessons suitable for `young_artists`.

Older children can still access easier content when they choose; age policy primarily controls recommendations, presentation and tool/help defaults rather than hiding all simpler subjects.

## 10. Curriculum quality rules

- Every journey must have a visible skill progression, not merely a theme.
- A lesson can belong to multiple contexts but has one stable source package.
- Every release lesson declares at least one meaningful target skill.
- Difficulty changes lesson demands, not reward value.
- Recommendations should mix practice with interest-driven choice; do not turn Home into a rigid school syllabus.
- At least one open-ended creative decision should be preserved where pedagogically appropriate.
- Cultural/folk-art content added later requires respectful sourcing/review rather than generic visual imitation.

## 11. Content-production priority

Before producing all 36 lessons, create and validate a **representative content set** that exercises the platform:

1. very simple tracing-friendly lesson for Little Artists;
2. normal Draw With Me animal lesson with full Help Ladder;
3. Watch Then Draw lesson;
4. lesson with multi-stroke grouped demonstrations;
5. lesson with guided coloring regions;
6. older-child lesson with more detail/proportion;
7. open-ended creative variation lesson.

If those content types cannot be authored cleanly with the schema/engines, expanding the catalog stops until the architecture is corrected.