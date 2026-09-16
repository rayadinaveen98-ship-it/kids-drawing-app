#!/usr/bin/env bash
set -euo pipefail

APP_MAIN="app/src/main"
APP_BUILD="app/build.gradle.kts"
STUDIO_BUILD="content-studio/build.gradle.kts"
STUDIO_SRC="content-studio/src/main/kotlin/com/navin/kidsdrawing/lesson/authoring"

if [[ -d "$APP_MAIN/java/com/navin/kidsdrawing/lesson/authoring" ]]; then
  echo "Content V2.3 boundary failure: Studio authoring source is present under Android app main."
  exit 1
fi

if grep -R -E '^import com\.navin\.kidsdrawing\.lesson\.authoring\.' "$APP_MAIN" >/dev/null 2>&1; then
  echo "Content V2.3 boundary failure: Android production source imports Studio tooling."
  exit 1
fi

if grep -E '(^|[[:space:]])(implementation|api)\(project\(\":content-studio\"\)\)' "$APP_BUILD" >/dev/null 2>&1; then
  echo "Content V2.3 boundary failure: Android APK has a production dependency on :content-studio."
  exit 1
fi

test -d "$STUDIO_SRC"
grep -F 'kotlin.exclude("com/navin/kidsdrawing/lesson/content/AndroidAssetLessonSource.kt")' "$STUDIO_BUILD" >/dev/null
grep -F 'include(":content-studio")' settings.gradle.kts >/dev/null

echo "Content Studio boundary passed: authoring tooling is isolated from the child APK."
