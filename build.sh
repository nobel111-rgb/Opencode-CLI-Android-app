#!/usr/bin/env bash
set -e

echo "=========================================="
echo " Building OpenCode CLI Android APK"
echo "=========================================="

# 1. Ensure .env exists
if [ -f ".env.example" ] && [ ! -f ".env" ]; then
  cp .env.example .env
elif [ ! -f ".env" ]; then
  touch .env
fi

# 2. Setup debug keystore if needed
if [ -f "debug.keystore.base64" ] && [ ! -f "debug.keystore" ]; then
  base64 -d debug.keystore.base64 > debug.keystore 2>/dev/null || base64 --decode debug.keystore.base64 > debug.keystore 2>/dev/null || true
fi

if [ ! -f "debug.keystore" ] || [ ! -s "debug.keystore" ]; then
  echo "Generating debug.keystore..."
  keytool -genkeypair \
    -v \
    -keystore debug.keystore \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

# 3. Setup upload keystore for release if needed
export STORE_PASSWORD="${STORE_PASSWORD:-android}"
export KEY_PASSWORD="${KEY_PASSWORD:-android}"

if [ ! -f "my-upload-key.jks" ]; then
  echo "Generating upload key for release build..."
  keytool -genkeypair \
    -v \
    -keystore my-upload-key.jks \
    -storepass "$STORE_PASSWORD" \
    -alias upload \
    -keypass "$KEY_PASSWORD" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Release Key,O=OpenCode,C=US"
fi

export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"

# 4. Create root build output folder
mkdir -p build

echo "Compiling Debug & Release APKs..."
gradle assembleDebug assembleRelease --no-daemon --stacktrace -Dorg.gradle.configuration-cache=false

# 5. Copy built APKs to root build folder with clean names
echo "Copying APKs to ./build directory..."
cp -f app/build/outputs/apk/debug/*.apk build/OpenCode-CLI-debug.apk 2>/dev/null || cp -f app/build/outputs/apk/debug/* build/ 2>/dev/null || true
cp -f app/build/outputs/apk/release/*.apk build/OpenCode-CLI-release.apk 2>/dev/null || cp -f app/build/outputs/apk/release/* build/ 2>/dev/null || true

echo "=========================================="
echo " Build Completed Successfully!"
echo " Output files in ./build/ :"
ls -lh build/
echo "=========================================="
