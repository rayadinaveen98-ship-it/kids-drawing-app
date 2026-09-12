# P1.7 Physical Results — Samsung SM-A546E

**Device class:** M  
**Device:** Samsung SM-A546E  
**Android/API:** 36  
**Reported RAM:** 7436 MB  
**Heap class:** 256 MB  
**Display:** 120 Hz  
**Builds tested:** `0.0.6-p1.7-test`, `0.0.7-p1.7-fix-test`

## Confirmed PASS evidence

- Input dispatch upper-bound proxy: P95 2–3 ms, P99 3–4 ms across captured runs. Class-M gate: P95 <= 4 ms, P99 <= 8 ms.
- W2 save x20: observed P95 360 ms worst captured run. Class-M gate: <= 1000 ms.
- W2 load -> editable render x20: observed P95 500 ms. Class-M gate: <= 1500 ms.
- W3 5,000-operation document opens and remains editable without crash/OOM during the physical test.
- W3 memory observations remained below device limits in captured screenshots (roughly Java 22–38 MiB, native 84–115 MiB depending on workload state).

## Confirmed FAIL evidence

### W2 visible Undo/Redo

- Initial `0.0.6` result: P95 500 ms / P99 500 ms.
- First renderer projection-cache iteration in `0.0.7`: P95 192 ms / P99 240 ms.
- Class-M gate: P95 <= 50 ms.
- Status: **FAIL**, tracked by issue #23. The first cache iteration materially improved latency but is still not acceptable.

### W1 continuous interaction frame budget

`0.0.7` W1 physical run after workload-load reset:
- measured frame sample count: 35;
- native-refresh JankStats: 3 frames / 8.6%;
- 60 Hz-equivalent frames >16.7 ms: 27 / 77.1%;
- input upper-bound remained P95 3 ms / P99 3 ms.

The >16.7 ms rate is far above the Class-M <=3% gate. The result is therefore **FAIL** even though the screenshot did not expose the clipped frame P95/P99 values. Root cause investigation points to committed vector artwork being replayed during visible frames rather than flattened/cached.

## Measurement interpretation

The device is explicitly Class M under `docs/18_DRAWING_PERFORMANCE_GATES.md`. High-refresh native JankStats and the project's 60 Hz-equivalent >16.7 ms rate are recorded separately. The custom Phase-1 frame gate uses the 60 Hz-equivalent values while retaining native-refresh evidence for diagnosis.

## Required disposition before P1.7 closes

1. Replace repeated full committed-vector replay with a flattened/cached committed rendering path.
2. Keep the editable vector operation document authoritative; rasterization is projection-only.
3. Use recent checkpoints so visible Undo/Redo replays only a small tail.
4. Preserve erase-mask and clear semantics exactly.
5. Re-run W1 30-second continuous drawing and require <=3% frames >16.7 ms plus P95/P99 gates.
6. Re-run W2 Undo/Redo x20 and require P95 <=50 ms.
7. Keep prior input/save/load/W3 PASS evidence unless a regression is observed.
