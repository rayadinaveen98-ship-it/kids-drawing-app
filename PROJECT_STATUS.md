# Project Status

**Working project:** Kids Drawing App  
**Authoritative repository:** `rayadinaveen98-ship-it/kids-drawing-app`  
**Latest fully verified product release:** `0.4.0-content-studio`, versionCode 19  
**Current phase:** Phase 5 — Curriculum & Teaching Experience Expansion / `0.5.0-curriculum-expansion` — **ACTIVE**  
**Parent epic:** #73  
**Latest completed slice:** P5.5 — Curriculum Expansion Set D #82  
**Current slice:** P5.6 — Curriculum Expansion Set E #84  
**Active branch:** `phase5/p5-6-curriculum-set-e`  
**Draft PR:** #85  
**Current P5.6 state:** **24 LESSONS COMPLETE; PRE-FREEZE CONTENT LAB 16/16 PASS; QA1 v25 AUTOMATED CI GREEN; PHYSICAL ACCEPTANCE PENDING**  
**Current production catalog:** **24 release lessons**  
**QA1 candidate:** `0.5.0-curriculum-expansion-p5.6-qa1`, versionCode **25**  
**Exact QA1 binary head:** `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`  
**Last updated:** 2026-09-14

Git is authoritative when chat memory and repository state disagree.

## P5.6 verified state

- Set E delivered: Face & Expressions, Simple Body & Pose, Create Your Character, One-Point Room.
- Production catalog: **24**.
- Quality: **24 lessons / 0 errors / exactly 6 reviewed standalone warnings**.
- Age coverage: Little 8 / Creative 18 / Growing 17 / Young 10.
- Difficulty: D1 5 / D2 9 / D3 6 / D4 3 / D5 1.
- Character Creator: Face → Body/Pose → Create Character.
- One-Point Room standalone with Sailboat Scene prerequisite; vanishing point guide-only.
- Content Lab: **16/16 PASS**.
- QA1 exact head: `9a8a0424d5f9b0bcb17b28bc4e64c0a8714c6ecb`.
- QA1 CI #538 / run `34846235868`: **GREEN**.

## Immutable QA1 acceptance binary

Profile artifact `10348300909`  
`Kids_Drawing_0.5.0_Curriculum_Expansion_P5.6_QA1-profile.apk`  
Size: **16,311,900 bytes**  
SHA-256: `0abb9da43315d118c67b9adf004753aed622dda992c95bce26eddc3727bf04b5`

Debug artifact `10347578783`, 20,527,518 bytes, SHA-256 `5e8eb2b9ff209798bdbb7d568326d4d4df3aff973821de2e14a333f2e12091e8`.

Content-quality artifact `10347409267`.

## Immediate gate

Physical acceptance is **PENDING 36/36** using `docs/10-execution/P5_6_FOCUSED_ACCEPTANCE_CHECKLIST.md` and only the exact profile APK above.

After genuine 36/36 PASS:
1. commit acceptance evidence without inventing device/API metadata;
2. exact-head acceptance-doc CI GREEN;
3. mark PR #85 ready;
4. squash merge;
5. merged-main CI GREEN;
6. close #84 and freeze P5.6;
7. start P5.7 from verified post-P5.6 main.

Any binary/content-changing defect invalidates versionCode 25 and requires a new versionCode/evidence set.

## Frozen architecture invariants

- generic structured lessons only; no lesson-ID-specific runtime UI;
- `LessonSessionState` remains teaching-state truth;
- Companion remains read-only relative to Help/completion/artwork/persistence;
- teacher/help/reference overlays never enter child artwork;
- AndroidX Ink stays behind owned drawing infrastructure;
- offline/account-free/ad-free core remains unchanged;
- no similarity scoring, grades, rank, stars/XP, permanent ability labels, punitive streaks or cloud child profiling.
