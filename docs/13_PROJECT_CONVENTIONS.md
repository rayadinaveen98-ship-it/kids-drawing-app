# 13 — Project Conventions

## Git

Preferred commit style:
- feat(drawing): ...
- feat(lesson): ...
- fix(canvas): ...
- test(...): ...
- docs(...): ...
- perf(...): ...
- refactor(...): ...

## Branches

- main: stable integration/release line
- feature/<scope>: focused development
- fix/<scope>: bug fixes
- spike/<scope>: disposable/experimental technical validation where needed

Avoid long-lived branch complexity unless the team size requires it.

## Source of truth

- Product status: PROJECT_STATUS.md
- Continuation/handoff: docs/HANDOFF.md
- Planned work: ROADMAP.md + GitHub issues
- Decisions: docs/adr/
- Release history: CHANGELOG.md + GitHub Releases

## Definition of done

A milestone must have implementation, tests, updated docs/status, and a build artifact when relevant. Known limitations must be recorded rather than silently carried forward.
