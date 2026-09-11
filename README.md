# Kids Drawing App — Product & Engineering Repository

> Repository: `rayadinaveen98-ship-it/kids-drawing-app`
>
> Working product/brand name is not yet final.

Kids Drawing is an Android-first, offline-first creative learning product for children. Its core proposition is a patient personal art teacher that adapts lessons, pacing, assistance, tools, and content to a child's age and ability.

## Product loop

Learn → Watch → Draw Together → Practice → Color → Create Freely → Tell Stories → Grow

## Current phase

**Phase 0 — Product Foundation**

See [PROJECT_STATUS.md](PROJECT_STATUS.md) for the exact continuation point and [docs/HANDOFF.md](docs/HANDOFF.md) for chat/developer handoff.

## Core principles

- Child-first safety and privacy
- No mandatory account for V1
- Offline-first core experience
- Android-first native implementation
- Custom drawing engine; no paid drawing SDK
- Structured lessons rather than hard-coded tutorials
- Patient teaching; never punish imperfect drawing
- Age-adaptive UI, curriculum, and assistance
- Free/open tooling wherever practical
- Decisions and continuation state live in Git, not only chat

## Planned stack

- Kotlin
- Jetpack Compose
- Android native graphics APIs
- Room / SQLite
- DataStore
- Kotlin Coroutines + Flow
- kotlinx.serialization
- Hilt
- Android TextToSpeech initially
- Lottie/Compose animation for companion presentation
- GitHub + GitHub Actions
- Krita + Inkscape + Blender for art/content production
- Figma or Penpot for product design
- Supabase only later if cloud features are justified

## Repository rule

If losing a decision would hurt development, it belongs in Git.
