# P6.4 Transition Note

P6.4 issue #98 is activated only after P6.3 completed its full acceptance and repository-closure sequence.

Verified dependency evidence:
- P6.3 issue #96: CLOSED / COMPLETED;
- P6.3 PR #97: squash-merged;
- merge: `fc81703b57419d14c9baf50a0fb4fb91554652f0`;
- merged-main Android CI #617 / run `34954128416`: GREEN;
- physically accepted P6.3 executable remains `f155abc894d21b8cc09112a018fdf53ae25e4447`, artifact `10387978868`, versionCode 29, physical QA 24/24 PASS.

P6.4 must not reinterpret the merge/docs builds as the physically accepted P6.3 binary. Accessibility work begins from the verified repository baseline while preserving all accepted P6.3 product/data/safety semantics.