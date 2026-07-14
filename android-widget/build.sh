#!/usr/bin/env bash
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
BUILD="$HERE/build"
ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
R8_JAR="$BUILD/tools/r8.jar"
KEYSTORE="${HOME}/.tolu-cc-widget.keystore"

rm -rf "$BUILD/gen" "$BUILD/classes" "$BUILD/dex"
mkdir -p "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" "$BUILD/tools"

if [[ ! -f "$R8_JAR" ]]; then
  curl -fsSL "https://storage.googleapis.com/r8-releases/raw/8.7.18/r8.jar" -o "$R8_JAR"
fi

aapt package -f -m \
  -J "$BUILD/gen" \
  -M "$HERE/AndroidManifest.xml" \
  -S "$HERE/res" \
  -I "$ANDROID_JAR" \
  -F "$BUILD/tolu-cc-resources.apk"

mapfile -d '' SOURCES < <(find "$BUILD/gen" "$HERE/src" -name '*.java' -print0)
javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" \
  -d "$BUILD/classes" \
  "${SOURCES[@]}"

jar cf "$BUILD/classes.jar" -C "$BUILD/classes" .
java -cp "$R8_JAR" com.android.tools.r8.D8 \
  --min-api 23 \
  --lib "$ANDROID_JAR" \
  --output "$BUILD/dex" \
  "$BUILD/classes.jar"

cp "$BUILD/tolu-cc-resources.apk" "$BUILD/tolu-cc-unsigned.apk"
(cd "$BUILD/dex" && aapt add "$BUILD/tolu-cc-unsigned.apk" classes.dex >/dev/null)
zipalign -f 4 "$BUILD/tolu-cc-unsigned.apk" "$BUILD/tolu-cc-aligned.apk"

if [[ ! -f "$KEYSTORE" ]]; then
  keytool -genkeypair -keystore "$KEYSTORE" -storepass tolucc92 -keypass tolucc92 \
    -alias tolucc -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Tolu's CC, O=Tolu Ashton Kehinde, C=GB" >/dev/null 2>&1
fi

apksigner sign --ks "$KEYSTORE" --ks-pass pass:tolucc92 --key-pass pass:tolucc92 \
  --out "$BUILD/Tolus-CC.apk" "$BUILD/tolu-cc-aligned.apk"
apksigner verify --verbose "$BUILD/Tolus-CC.apk"
echo "$BUILD/Tolus-CC.apk"
