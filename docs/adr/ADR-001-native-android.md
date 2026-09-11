# ADR-001 — Native Android First

**Status:** Accepted  
**Date:** 2026-09-11

## Decision
Use Kotlin + Jetpack Compose for the Android-first application.

## Reasons
- strong control over touch/stylus and graphics behavior
- Android is the first distribution target
- native lifecycle/performance integration
- no paid framework requirement
- reduces abstraction risk for the custom drawing engine

## Alternatives considered
Flutter, React Native, Unity.
