# 06 — Technical Architecture

## Platform

Android first, native Kotlin + Jetpack Compose.

## Core design

Offline-first and modular.

Proposed modules:
- core:model
- core:drawing
- core:lesson
- core:coloring
- core:content
- core:companion
- core:learning
- core:audio
- core:database
- core:common
- design-system
- feature:onboarding
- feature:home
- feature:lesson
- feature:studio
- feature:coloring
- feature:gallery
- feature:journeys
- feature:parent
- benchmark

## Storage

- Room/SQLite for structured local data
- DataStore for preferences
- local filesystem for artwork, previews, lesson packages and assets

## Networking/backend

No backend dependency for Alpha. Supabase is a candidate only when cloud sync/accounts/remote catalog genuinely require it.

## Drawing stack

Native Android graphics/input APIs. Store strokes as structured data rather than flattened bitmaps as the authoritative representation.

## Quality

CI should build, lint, unit test and produce an installable debug APK at meaningful milestones.
