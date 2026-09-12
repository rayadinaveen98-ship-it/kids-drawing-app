# P2.5 Lifecycle / Recovery Test Matrix

This matrix is the acceptance evidence target for Issue #33.

| Scenario | Draw With Me | Watch Then Draw | Trace & Learn | Expected recovery |
|---|---:|---:|---:|---|
| Background during child turn | Required | Required | Required | Persist semantic session + child document; restore same child turn |
| Background during teacher step | Required | n/a after overview | Required | Normalize to safe pre-teacher state and restart deterministic teacher request |
| Background during overview | n/a | Required | n/a | Normalize/restart overview; never resume a stale request identity |
| Background with Help active | Required | Required | Required | Restore help level and recreate only the correct transient guide |
| Process death after completed step | Required | Required | Required | Reload child document first; restore next semantic step |
| Process death while guide visible | Required where applicable | Required where applicable | Required | Guide not persisted; recreate from authored content after restore |
| Corrupt primary session file | Required | Required | Required | Recover backup when valid; never mutate child artwork |
| Corrupt primary + backup | Required | Required | Required | Typed unavailable/corrupt-session outcome; child drawing remains loadable |
| Lesson revision mismatch | Required | Required | Required | Typed incompatibility; no silent progress coercion |
| Stale teacher callback after restore | Required | Required | Required | Reject callback deterministically |
| Playback failure | Required | Required | Required | Recoverable error/retry contract, or explicit terminal outcome if content is invalid |
| Drawing complete recreation | Required | Required | Required | Restore drawing-complete/post-drawing decision state safely |

No P2.5 acceptance case is allowed to persist screenshots as editable truth or copy teacher/guide strokes into the child document.
