#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
VERSION='9.4.1'
CACHE="$ROOT/.gradle-local"
mkdir -p "$CACHE"
ZIP="$CACHE/gradle-$VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
if [ ! -f "$CACHE/gradle-$VERSION/bin/gradle" ]; then
  echo "Downloading Gradle $VERSION..."
  [ -f "$ZIP" ] || curl --fail --location --retry 3 "$URL" -o "$ZIP"
  EXPECTED="$(curl --fail --location "$URL.sha256" | tr -d '\r\n')"
  ACTUAL="$(shasum -a 256 "$ZIP" | awk '{print $1}')"
  [ "$EXPECTED" = "$ACTUAL" ] || { rm -f "$ZIP"; echo 'Gradle SHA-256 mismatch' >&2; exit 1; }
  unzip -q -o "$ZIP" -d "$CACHE"
fi
mkdir -p "$CACHE/wrapper-setup"
printf '%s\n' 'rootProject.name = "wrapper-setup"' > "$CACHE/wrapper-setup/settings.gradle.kts"
"$CACHE/gradle-$VERSION/bin/gradle" -p "$CACHE/wrapper-setup" wrapper --gradle-version "$VERSION" --distribution-type bin
cp "$CACHE/wrapper-setup/gradle/wrapper/gradle-wrapper.jar" "$ROOT/gradle/wrapper/"
cp "$CACHE/wrapper-setup/gradlew" "$ROOT/gradlew"
cp "$CACHE/wrapper-setup/gradlew.bat" "$ROOT/gradlew.bat"
chmod +x "$ROOT/gradlew"
echo 'Ready. Open the project folder in Android Studio.'
