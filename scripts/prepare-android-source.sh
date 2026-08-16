#!/usr/bin/env bash
# Extract an uploaded Android source archive into the repository's android directory.
set -euo pipefail

: "${ARCHIVE:?ARCHIVE must point to the uploaded source archive}"
DEST_DIR="${DEST_DIR:-android}"
WORK_DIR="${RUNNER_TEMP:-/tmp}/myipcheck-source"
PRESERVED_RELEASES="$WORK_DIR/preserved-releases"

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR/unpacked"

case "$ARCHIVE" in
  *.zip)
    unzip -q "$ARCHIVE" -d "$WORK_DIR/unpacked"
    ;;
  *.tar.gz|*.tgz)
    tar -xzf "$ARCHIVE" -C "$WORK_DIR/unpacked"
    ;;
  *.tar)
    tar -xf "$ARCHIVE" -C "$WORK_DIR/unpacked"
    ;;
  *)
    echo "Unsupported archive format: $ARCHIVE" >&2
    exit 2
    ;;
esac

# A source archive commonly contains one top-level folder. If so, use it as the project root.
shopt -s dotglob nullglob
ROOT="$WORK_DIR/unpacked"
ENTRIES=("$ROOT"/*)
if [[ "${#ENTRIES[@]}" -eq 1 && -d "${ENTRIES[0]}" ]]; then
  ROOT="${ENTRIES[0]}"
fi

# If the archive has a wrapper folder, locate the first Android Gradle project below it.
if [[ ! -f "$ROOT/settings.gradle.kts" && ! -f "$ROOT/settings.gradle" ]]; then
  SETTINGS_FILE="$(find "$ROOT" -maxdepth 3 -type f \( -name settings.gradle.kts -o -name settings.gradle \) -print -quit)"
  if [[ -z "$SETTINGS_FILE" ]]; then
    echo "No Android Gradle project (settings.gradle[.kts]) found in $ARCHIVE" >&2
    exit 3
  fi
  ROOT="$(dirname "$SETTINGS_FILE")"
fi

if [[ ! -f "$ROOT/app/build.gradle.kts" && ! -f "$ROOT/app/build.gradle" ]]; then
  echo "No app module found under $ROOT" >&2
  exit 4
fi

# Keep the current APK copy in the repository even when the new source archive does not contain releases/.
if [[ -d "$DEST_DIR/releases" ]]; then
  mkdir -p "$PRESERVED_RELEASES"
  cp -a "$DEST_DIR/releases/." "$PRESERVED_RELEASES/"
fi

rm -rf "$DEST_DIR"
mkdir -p "$DEST_DIR"
cp -a "$ROOT"/. "$DEST_DIR"/
if [[ -d "$PRESERVED_RELEASES" ]]; then
  mkdir -p "$DEST_DIR/releases"
  cp -a "$PRESERVED_RELEASES/." "$DEST_DIR/releases/"
fi

# Never commit machine-specific state or stale build outputs from an uploaded archive.
rm -rf "$DEST_DIR/.gradle" "$DEST_DIR/app/build" "$DEST_DIR/local.properties"

printf 'Prepared Android project at %s\n' "$DEST_DIR"
printf 'Gradle settings: %s\n' "$DEST_DIR/settings.gradle.kts"
