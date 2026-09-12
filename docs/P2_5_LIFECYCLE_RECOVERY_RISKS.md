# P2.5 Recovery Risk Register

- **Split-brain restore:** session progress restored before the referenced child document. Mitigation: child document must load first.
- **Stale runtime callbacks:** pre-recreation teacher request completes after a new session instance exists. Mitigation: request identity rejection + runtime cleanup.
- **Overlay persistence leak:** trace/help teacher strokes enter editable child history. Mitigation: overlays remain `TEACHER_GENERATED` and never use child commit APIs.
- **Transient snapshot lie:** mid-playback cursor is persisted as if stable. Mitigation: existing snapshot normalization + deterministic restart semantics.
- **Revision drift:** lesson content changes after a session is saved. Mitigation: exact lesson revision compatibility check; no silent migration in 0.2.
- **Primary-file corruption:** process death/interrupted write. Mitigation: atomic temp write + backup fallback + checksum.
- **Autosave amplification:** lesson session saved for every pointer event. Mitigation: event-driven semantic boundary saves only.
- **Child-art loss on session failure:** session file is corrupt or incompatible. Mitigation: drawing persistence remains independent and must remain recoverable even when session restore fails.
