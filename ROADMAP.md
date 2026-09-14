# Product Roadmap

## Phase 0 — Product Foundation — COMPLETE
Product/UX/content/engine/companion/safety/quality/release contracts locked.

## Phase 1 — Art Lab / Drawing Engine 0.1 — COMPLETE
Low-latency native drawing, editable operation history, tools, Undo/Redo/Clear, atomic persistence/recovery and deterministic teacher playback proven.

## Phase 2 — Lesson Engine 0.2 — COMPLETE
Structured lesson loading/validation, all three teaching modes, Help Ladder, five paces, lifecycle/process recovery and 32/32 physical QA proven.

## Phase 3 — First Vertical Slice 0.3 — COMPLETE
`0.3.0-vertical-slice`, versionCode 13. Production onboarding → lesson → coloring → Gallery journey physically passed 41/41 scenarios.

## Phase 4 — Content & Studio Expansion 0.4 — ACTIVE

**Milestone target:** `0.4.0-content-studio`  
**Epic:** #57

Goal: scale the verified product into a reusable offline multi-lesson art-learning studio without reopening proven engine foundations.

### P4.1 — Content Catalog Foundation — COMPLETE
Deterministic offline multi-package discovery, validation, typed diagnostics, metadata queries and generic catalog loading.

### P4.2 — Discovery, Art Journeys & Recommendations — COMPLETE
Multi-lesson Studio, deterministic recommendations, category/journey discovery, stable lesson identity and cross-lesson recovery.

### P4.3 — Representative Content Set A — IMPLEMENTATION MERGED / PHYSICAL MATRIX OPEN
Smiling Sun, Friendly Owl, Simple Rocket and Easy Flower are integrated. PR #67 merged at `99767b71a8e4ea20b6d587e4951c822a386019e5`; merged-main CI #342 green. Issue #60 remains open because its broader physical/content matrix was not fully rerun. Finish that coverage in P4.7.

### P4.4 — Free Draw Studio V1 Core — COMPLETE
Issue #61 / PR #69. Squash merge `46c5954fd829e8f64cb752a58c732e895b8e3855`; merged-main CI #379 green. QA1 versionCode 16 received positive user physical smoke. Exhaustive deferred P4.4 rows remain part of P4.7 regression.

### P4.5 — Coloring V1 Expansion — COMPLETE
Issue #62 / PR #70. Squash merge `10b8f2f7a6a6b4579a8695ab4f602c2e4525aec5`; merged-main CI #420 GREEN.

QA1 `0.4.0-content-studio-p4.5-qa1`, versionCode 17, executable `240007b6161ebfefd09252efa844e4d18808a7f0`, profile artifact `10317956245`, size `16,183,450 bytes`, SHA-256 `cf45fedc129523c8c9d3784e3d6ea70d0319dc1b8f9236430b85c099075394d6`. Focused physical/product checklist user-reported PASS. Deferred process-death, exact undo-boundary, airplane-mode and small-screen rows remain for P4.7.

### P4.6 — Representative Content Set B + Cross-content QA — ACCEPTED / MERGE VERIFICATION PENDING
Issue #63 / PR #71 / branch `phase4/p4-6-content-set-b`.

P4.6 expands the representative catalog to nine production lessons with:
- **Hot Air Balloon** — richer guided-region coloring;
- **Fox Portrait** — credible older-child detail/proportion experience;
- **Design Your Spaceship** — genuine open-ended creative variation.

Coverage now includes:
- all four age bands;
- difficulty 1–4;
- multiple lessons in First Shapes to Pictures, Animal Artist and Space Artist;
- repeated Draw With Me/Watch Then Draw coverage plus beginner Trace;
- prepared-region coloring and legacy freehand coloring;
- beginner high-assistance and older-child lighter-assistance patterns;
- a child-authored creative step with no replica requirement.

QA1 evidence:
- `0.4.0-content-studio-p4.6-qa1`, versionCode 18;
- executable `e96e452f41f545529712a35e9cf97553510252d8`;
- CI #428 / run `34759214099` GREEN;
- profile artifact `10318307419`;
- size `16,196,362 bytes`;
- SHA-256 `e207d006893747a91a0c8dd6935d7764417fc77532a992a6d1120ec8bd613a4e`;
- supplied focused physical/product checklist user-reported PASS on 2026-09-14.

Deferred to P4.7 rather than overclaimed: Set-A focused regression, airplane mode, process recreation on P4.6 content, small-screen/age-adaptive regression, and prior deferred Phase-4 physical rows.

Remaining gate: exact-head CI after acceptance documentation → ready/merge PR #71 → merged-main CI → close #63.

### P4.7 — End-to-end QA + `0.4.0-content-studio` Release — NEXT
This is the final Phase-4 slice. It must complete the full offline/lifecycle/accessibility/device regression across catalog discovery, all representative lesson modes, Help Ladder, Free Draw, expanded coloring, Gallery, recommendations/journeys and recovery.

P4.7 must explicitly absorb all still-deferred P4.3/P4.4/P4.5/P4.6 physical rows, produce an exact final candidate with monotonic versionCode, keep CI green, record reproducible APK evidence, deliver the final verified `0.4.0-content-studio` APK, and close the remaining Phase-4 issues only when evidence genuinely supports it.

## Post-Phase-4 direction
- companion expression/voice polish;
- expand representative curriculum toward public V1 24–36 lessons;
- adaptive local recommendations/help without punitive scoring;
- Parent Zone / parent-controlled export/settings;
- broader accessibility/device hardening and Beta;
- V1.0 only after product, privacy/safety, content and store-release gates pass.

## Permanent delivery rule
Every meaningful Android milestone should produce an installable APK when technically possible, tied to an exact Git commit and reproducible CI/release evidence.
