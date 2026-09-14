# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — **COMPLETE**

**Released milestone:** `0.4.0-content-studio` / versionCode 19  
**Epic:** #57 — CLOSED completed  
**Final release issue:** #64 — CLOSED completed  
**Final PR:** #72 — squash-merged at `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4`  
**Merged-main CI:** #446 / run `34802235303` — GREEN

Phase 4 successfully scaled the verified single vertical slice into a reusable offline multi-lesson kids art-learning studio.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Multi-lesson Studio, deterministic recommendations, category/journey discovery, stable lesson identity and cross-lesson recovery.

### P4.3 — Representative Content Set A — COMPLETE
Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower execute through generic content/runtime paths. Deferred physical Set-A coverage passed in the final P4.7 candidate; issue #60 is closed completed.

### P4.4 — Free Draw Studio V1 Core — COMPLETE
Production Free Draw with Pencil/Crayon/Marker/Eraser, palette/size, editable history, Clear safety, recovery, age-adaptive presentation and Gallery provenance. Full deferred row-level physical matrix passed in P4.7.

### P4.5 — Coloring V1 Expansion — COMPLETE
Prepared-region Fill, authored Color With Me, Color Myself, reversible schema-3 fill history, older-document readability, protected line art and Gallery rendering. Deferred lifecycle/offline/exact-Undo rows passed in P4.7.

### P4.6 — Representative Content Set B + Cross-content QA — COMPLETE
Hot Air Balloon, Fox Portrait and Design Your Spaceship expanded the representative catalog to nine production lessons across all four age bands and difficulty levels 1–4.

### P4.7 — End-to-end QA + `0.4.0-content-studio` Release — COMPLETE

Exact physically accepted binary:
- versionName `0.4.0-content-studio`;
- versionCode 19;
- executable `f3843365d39540de00fe08a008883c15abe75599`;
- candidate Android CI #440 / run `34801122118` GREEN;
- profile artifact `10331363240`;
- profile APK size `16,196,353 bytes`;
- profile SHA-256 `f7a8f9a177d8edc6244796f7c08ff4fd32ad64eeb26e90b7ba00bb68fc9daa59`;
- independent local hash/size verification PASS;
- final supplied physical/product checklist user-reported PASS on 2026-09-14.

Final QA proved:
- fresh install/onboarding/profile and nine-lesson Studio;
- categories, Art Journeys and deterministic recommendations;
- Trace & Learn, Draw With Me, Watch Then Draw, Help Ladder and grouped playback;
- cumulative teacher construction without child-art contamination;
- complete Free Draw tools/history/recovery/Gallery matrix;
- prepared + legacy coloring, exact Undo boundary and protected line art;
- lesson/coloring/Free Draw lifecycle and force-stop recovery;
- Gallery reopen/delete/source isolation;
- Airplane Mode complete core journeys;
- age 4–5 / 6–7 / 8–9 / 10–12 plus larger system-font spot checks;
- operation durability, no cross-source deletion and stability sweep.

Handedness, voice-off and reduced-motion setting rows are N/A because those product controls are not exposed in 0.4. Deterministic resume priority and missing/incompatible-content fallback remain backed by frozen automated coverage rather than fabricated device failure injection.

Acceptance-documentation head `84771ba4a19692953bc79a1cf185c9a5d5c491b5` passed CI #445. Final merge `9f9a21b7d77b2471e1c4e1a7035ce7ec258f35a4` passed merged-main CI #446.

No `v0.4.0-content-studio` tag is claimed because connected repository tooling did not expose tag creation.

## Post-Phase-4 direction — NOT YET LOCKED AS A BUILD MILESTONE

Potential next work:
- companion expression/voice polish;
- expand representative curriculum toward public V1 24–36 lessons;
- adaptive local recommendations/help without punitive scoring;
- Parent Zone / parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

Before any of this is implemented, create and lock the next milestone/epic in Git. Do not silently extend Phase 4 after its verified release.

## Permanent delivery rule
Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
