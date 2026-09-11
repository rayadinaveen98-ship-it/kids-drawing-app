# 08 — Lesson Engine Specification (Initial)

## Responsibilities

- load structured lesson content
- manage lesson/step state
- coordinate teacher stroke playback
- wait for child action
- expose replay/pause/speed controls
- trigger hint/help states
- coordinate narration hooks
- coordinate companion events
- persist/resume progress safely
- transition to coloring/completion

## Separation rule

Lesson content describes what should happen. UI screens render state. The Lesson Engine owns lesson sequencing and must not be embedded as ad-hoc screen code.
