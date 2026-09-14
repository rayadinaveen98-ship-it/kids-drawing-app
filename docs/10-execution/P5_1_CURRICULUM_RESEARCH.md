# P5.1 — Curriculum & Teaching Research Synthesis

**Parent epic:** #73  
**Slice:** #74  
**Target milestone:** `0.5.0-curriculum-expansion`  
**Research date:** 2026-09-14  
**Status:** research baseline for contract; not implementation

## 1. Purpose

Phase 4 proved that the app can teach materially different drawing experiences through generic engines. Phase 5 must now decide *what* should be taught across ages 4–12 and *how* guidance should change with development, without turning the product into a copy-the-demo worksheet or a scored school app.

This research combines the repository's frozen curriculum/content architecture with current authoritative arts-education guidance. It is used to derive product decisions, not to claim that the app is a formal school curriculum or standards-compliance product.

## 2. Existing repository baseline

The repository already freezes:
- four age bands: 4–5, 6–7, 8–9 and 10–12;
- independent category, skill, difficulty, age-band and journey metadata;
- difficulty 1–5 as content complexity, never child worth;
- reusable skill IDs covering mark-making, shapes, placement, proportion, depth, composition, people/character, detail/value, color and creativity;
- four starter journeys: First Shapes to Pictures, Animal Artist, Space Artist and Character Creator;
- a public-V1 target of 36 guided lessons with a hard quality floor of 24;
- content quality rules requiring real skill progression, meaningful creative decisions and stable source packages;
- structured lessons interpreted by engines rather than lesson-specific UI.

Phase 4 then proved the representative content types that had been prerequisites for scale: early Trace, full Help Ladder, Watch Then Draw, grouped demonstrations, prepared coloring, older-child proportion/detail and open creative variation.

## 3. Authoritative external guidance reviewed

### 3.1 NCERT — National Curriculum Framework for School Education 2023

Sources:
- https://ncf.ncert.gov.in/
- https://ncf.ncert.gov.in/webadmin/assets/b27f04eb-65af-467f-af12-105275251546
- https://www.education.gov.in/sites/upload_files/mhrd/files/ncf_2023.pdf

Relevant principles:
- Arts are a main curricular area rather than a decorative extracurricular activity.
- Art pedagogy should be **process-focused**: thinking, making, responding and appreciating matter more than producing a uniform final product.
- Student experience should be a starting point; discussion and multiple viewpoints matter.
- Variety, variation and interdisciplinary connections should be encouraged rather than rigid reproduction.
- Local arts and cultures should be meaningful starting points.
- As children mature, observation, sensitivity to detail, refinement and aesthetic judgement can become more sophisticated.
- Visual principles such as balance and proportion can be learned while recognising that aesthetic choices are contextual and cultural rather than universal rules of beauty.

### 3.2 National Core Arts Standards — Visual Arts PreK–8

Sources:
- https://www.nationalartsstandards.org/
- https://nationalartsstandards.org/content/visual-arts-introduction
- https://www.nationalartsstandards.org/content/national-core-arts-standards

Relevant principles:
- Visual-arts learning is broader than technical drawing; it blends **Creating, Presenting, Responding and Connecting**.
- Creating includes generating ideas, developing work and refining/completing work.
- Responding includes perceiving, interpreting and evaluating meaning.
- Connecting includes relating art to personal experience and external/cultural context.
- The visual-arts standards deliberately define grade-by-grade progressions from PreK through Grade 8.

Product implication: the app should remain drawing-first, but older-child lessons should increasingly include optional planning, choice, reflection and personal connection rather than only procedural imitation.

### 3.3 NAEYC — developmentally appropriate/process-art guidance

Sources:
- https://www.naeyc.org/resources/pubs/tyc/winter2023/what-will-we-make
- https://www.naeyc.org/resources/pubs/tyc/winter2023/art-story-and-process

Relevant principles:
- Guided/product-focused art can support some motor/executive skills.
- Process art is choice-driven and open-ended; it gives children room to explore materials, ideas and personal expression.
- Young children's marks and drawings can carry story/meaning even when adults do not see a polished representational product.

Product implication: guided lessons are valuable, but especially for ages 4–7 they must preserve ownership. Trace/help is optional assistance, not the definition of success. Free Draw and creative variation are core learning experiences rather than side modes.

### 3.4 UNESCO — culture and arts education, including India context

Sources:
- https://www.unesco.org/en/articles/rhythms-learning-state-education-report-india-2024-culture-and-arts-education
- https://www.unesco.org/en/articles/state-education-report-india-2024
- https://www.unesco.org/en/articles/unesco-framework-culture-and-arts-education-implementation-guidance

Relevant principles:
- Culture and arts education supports creativity, critical thinking, empathy and cultural awareness.
- Inclusion, cultural diversity, co-creation and context-relevant learning are central.
- UNESCO's India report highlights alignment with NEP 2020 and NCF-SE 2023 and the importance of India's cultural diversity and local knowledge.
- Technology may improve accessibility, but should support rather than flatten cultural context.

Product implication: Phase 5 may begin preparing the taxonomy/content policy for Indian/local cultural art, but released cultural/folk-art lessons require respectful sourcing and review rather than generic style imitation.

### 3.5 NEP 2020

Source:
- https://www.uil.unesco.org/sites/default/files/medias/fichiers/2023/11/India_2020.pdf

Relevant principles:
- flexibility and learner choice;
- no harmful hierarchy between arts and other learning areas;
- creativity and critical thinking over rote learning;
- formative assessment for learning rather than exam/coaching culture;
- respect for diversity and local context.

Product implication: no grades, ability labels, leaderboards, punitive streaks or permanent 'beginner/advanced child' classification. Adaptation should guide options, not define identity.

## 4. Derived Phase-5 teaching principles

### 4.1 Technical guidance must decrease as authorship increases

The app should not use the same tutorial density for a 4-year-old and a 12-year-old.

- Younger lessons can teach marks/shapes explicitly, with large targets, fewer parts and stronger optional visual assistance.
- Middle ages should move toward construction, placement, overlap, texture, scene building and deliberate variation.
- Older-child content should use demonstrations as technique references while leaving proportion, composition, detail and design decisions increasingly to the child.

### 4.2 Copying is a tool, never the curriculum

A child may benefit from watching or following a teacher to learn a line, shape, proportion or sequence. But every curriculum band must also contain opportunities to:
- choose colors/details;
- vary shape or expression;
- invent additions;
- build a scene;
- reflect on what they made;
- create without matching a teacher image.

### 4.3 Process is first-class product truth

Success is not 'looks like the teacher.' Product success is:
- the child can understand the next action;
- input remains their own;
- help never destroys existing work;
- completion is possible through supported modes;
- the child has meaningful choices;
- work remains editable/recoverable;
- no punitive judgement is attached to style or realism.

### 4.4 Responding/connecting should be lightweight and optional

Phase 5 should not add school-style quizzes or written art criticism. Instead, appropriate lessons may offer short optional companion prompts such as:
- 'Which part is your favorite?'
- 'Want to make yours calm, silly, spooky or something else?'
- 'What could you add to show where this character lives?'
- 'Look at the two shapes — which feels bigger or closer?'

These support observation, reflection and connection without grading the answer.

### 4.5 Culture must be specific and respectful

Do not add 'Indian art style' as a generic decorative skin. Future folk/cultural content needs:
- named tradition/context;
- reliable sourcing;
- age-appropriate explanation;
- no flattening of distinct regional practices;
- review before release.

Phase 5's 24-lesson core can remain broadly universal while preparing this contract for later culturally specific expansion.

## 5. Age-band developmental product policy

These are app-authoring policies, not diagnoses or rigid developmental claims.

### Little Artists — approximately 4–5

Primary goals:
- confident mark-making;
- straight/curved/loop/zigzag control;
- open/closed shapes;
- combining 2–4 simple forms;
- relative placement in very simple pictures;
- color choice and playful variation;
- positive experience of 'I can make a picture.'

Authoring policy:
- difficulty mostly 1–2;
- typically 3–7 meaningful child turns;
- one main construction idea at a time;
- large/simple geometry;
- Trace may be offered for selected foundational lessons, never mandatory;
- frequent optional creative/color choice;
- companion language short, concrete and non-evaluative.

### Creative Explorers — approximately 6–7

Primary goals:
- combine shapes fluently;
- spacing, symmetry and basic relative scale;
- simple overlap;
- repeated patterns/details;
- Watch Then Draw memory/observation;
- first simple scenes;
- intentional color and design choices.

Authoring policy:
- difficulty mostly 1–3;
- typically 4–9 child turns;
- Draw With Me and Watch Then Draw both common;
- Trace becomes less central;
- every content cluster should include visible variation/open choice.

### Growing Artists — approximately 8–9

Primary goals:
- proportion and placement;
- contour refinement;
- overlapping forms and simple volume/depth cues;
- composition and foreground/background;
- texture/detail layering;
- character/scene decisions;
- simple reflection/revision.

Authoring policy:
- difficulty mostly 2–4;
- typically 5–12 child turns;
- demonstrations explain construction logic, not exact pixels;
- Help should increasingly use anchors/guides rather than tracing;
- creative variation and observation should occupy a substantial part of the lesson.

### Young Artists — approximately 10–12

Primary goals:
- deliberate proportion;
- stronger contour/shape design;
- one-point perspective/basic depth scale;
- value/shading foundations where suitable;
- face/body construction;
- pose/silhouette/character design;
- scene composition;
- planning, revision and personal visual decisions.

Authoring policy:
- difficulty mostly 3–5;
- typically 5–14 child turns, using the fewest pedagogically sensible steps;
- teacher demonstrations are references, not tracing templates;
- open-ended design decisions should be common;
- companion tone must not feel toddler-oriented;
- optional reflection may ask about intent, composition or next refinement rather than praise-only copy.

## 6. Help and feedback policy

Retain the frozen Help Ladder but tune authored help by age/difficulty:
- 4–5: hints may quickly become strong visual/trace support if the child requests it.
- 6–7: visual guides/anchors before trace.
- 8–9: direction, construction anchors and partial demonstrations preferred.
- 10–12: conceptual hints, proportion/placement guides and replay preferred; full trace should be rare unless the technique specifically requires it.

Companion feedback must:
- notice progress/state rather than judge beauty;
- avoid 'wrong', 'bad', scores or comparison to other children;
- avoid overusing empty 'perfect/amazing' praise;
- encourage agency: 'Want help?', 'Try another version?', 'Add your own detail?';
- make skipping/retrying safe;
- never punish slow work or repeated help use.

## 7. Curriculum construction rules derived from research

1. Every new lesson must teach or exercise named skills beyond its subject theme.
2. No age band may be served only by recolored/simplified versions of another band's content.
3. Every journey needs an observable progression in construction/observation/creative independence.
4. At least one-third of the Phase-5 catalog should contain a meaningful child-authored variation/choice beyond palette selection.
5. At least one-quarter should support Watch Then Draw or observation/memory behavior where pedagogically sensible.
6. Trace remains a targeted support, not a catalog-wide default.
7. Older-child content must include credible technical growth (proportion, contour, composition, perspective, character design), not merely more decorative details.
8. Free Draw remains a curriculum companion: lessons should sometimes invite children to continue an idea freely after guided completion.
9. Completion is not a quality grade; no star score, percent similarity or permanent skill rank is introduced.
10. Cultural/folk-art lessons are deferred until sourcing/review standards are separately satisfied.

## 8. Research conclusion

The professional next step is not to produce 27 more copy-along lessons. Phase 5 should reach a **24-lesson high-quality curriculum floor** first, with deliberate age progression and increasing creative independence. The eventual 36-lesson public-V1 target remains valid, but the additional 12 should be produced only after the 24-lesson curriculum and content-production workflow are physically proven.

P5.1 therefore locks the next contract around:
- 24 total release lessons for 0.5 (9 existing + 15 new);
- deeper age/skill progression;
- stronger creative ownership;
- a more context-aware patient teacher;
- repeatable content authoring/QA before scale to 36.
