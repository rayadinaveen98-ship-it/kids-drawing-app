#!/usr/bin/env bash
set -euo pipefail

SOURCE_ROOT="app/src/main/java"
ALLOWED_SEGMENT="/drawing/infrastructure/"

violations="$({
  grep -R -n --include='*.kt' '^import androidx\.ink\.' "$SOURCE_ROOT" || true
} | grep -v "$ALLOWED_SEGMENT" || true)"

if [[ -n "$violations" ]]; then
  echo "AndroidX Ink import escaped the drawing infrastructure adapter boundary:"
  echo "$violations"
  exit 1
fi

echo "Ink boundary passed: AndroidX Ink imports are contained in drawing/infrastructure."
