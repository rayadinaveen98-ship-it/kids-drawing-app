#!/usr/bin/env bash
set -euo pipefail

HOME_FILE="app/src/main/java/com/navin/kidsdrawing/product/home/StudioHomeRepository.kt"
RUNTIME_FILE="app/src/main/java/com/navin/kidsdrawing/product/lesson/ProductLessonRuntime.kt"

for file in "$HOME_FILE" "$RUNTIME_FILE"; do
  if grep -Eq '^import com\.navin\.kidsdrawing\.lesson\.content\.LessonCatalog$|(^|[^A-Za-z0-9_])LessonCatalog\(' "$file"; then
    echo "Content V2.2 boundary failure: eager LessonCatalog usage returned in $file"
    exit 1
  fi
done

if grep -Eq 'LessonRuntimePackage|runtimePackage\(' "$HOME_FILE"; then
  echo "Content V2.2 boundary failure: Home discovery depends on full lesson packages."
  exit 1
fi

grep -F 'CatalogIndexV2Loader' "$HOME_FILE" > /dev/null
grep -F 'CatalogSelectedLessonLoader' "$RUNTIME_FILE" > /dev/null

echo "Catalog Index V2 boundary passed: Home is metadata-only and selected lesson execution is lazy."

bash scripts/verify-content-studio-boundary.sh
