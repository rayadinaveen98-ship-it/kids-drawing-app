# 11 — Test Strategy

## Test layers

- unit tests for models, serialization, history and engine state
- drawing-engine deterministic tests
- lifecycle/persistence tests
- Compose/UI integration tests
- performance benchmarks
- device testing across low/mid/high Android hardware
- finger and stylus tests
- accessibility/usability tests appropriate to target ages

## Drawing stress cases

- rapid short strokes
- long continuous strokes
- hundreds/thousands of strokes
- repeated undo/redo
- pause/replay switching
- app background/foreground
- interrupted save/load
- multi-touch interference
- orientation/configuration changes where supported
- memory pressure

## Philosophy

A feature is not 'done' because it worked once manually. Each milestone has explicit acceptance gates.
