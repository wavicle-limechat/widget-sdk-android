#!/bin/bash

# Test script for Local SDK Demo
# This script demonstrates how to build and test the local-sdk-demo module

echo "🚀 Testing Local SDK Demo Module"
echo "================================="

# Check if we're in the right directory
if [ ! -f "../build.gradle.kts" ]; then
    echo "❌ Error: This script must be run from the local-sdk-demo directory"
    echo "   Current directory: $(pwd)"
    echo "   Expected to be in: local-sdk-demo/"
    exit 1
fi

echo "✅ Current directory: $(pwd)"
echo "✅ Parent project detected"

# Clean previous builds
echo ""
echo "🧹 Cleaning previous builds..."
../gradlew clean

# Build the local-sdk-demo module
echo ""
echo "🔨 Building local-sdk-demo module..."
../gradlew :local-sdk-demo:assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed!"
    exit 1
fi

# Check the generated APK
APK_PATH="build/outputs/apk/debug/local-sdk-demo-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "✅ APK generated: $APK_PATH"
    echo "   Size: $(du -h "$APK_PATH" | cut -f1)"
else
    echo "❌ APK not found at expected location"
    exit 1
fi

# Show module dependencies
echo ""
echo "📋 Module Dependencies:"
echo "   - widget-sdk (local module)"
echo "   - androidx.core:core-ktx:1.12.0"
echo "   - androidx.appcompat:appcompat:1.6.1"

# Show key differences from published SDK demo
echo ""
echo "🔍 Key Differences from Published SDK Demo:"
echo "   ✅ Uses local widget-sdk module instead of JitPack dependency"
echo "   ✅ Immediate testing of SDK changes"
echo "   ✅ No publishing delays for development"
echo "   ✅ Full source access for debugging"

echo ""
echo "🎯 Next Steps:"
echo "   1. Install on device: ../gradlew :local-sdk-demo:installDebug"
echo "   2. Run on device: ../gradlew :local-sdk-demo:installDebug"
echo "   3. Check logs for 'LOCAL SDK DEMO' messages"
echo "   4. Verify widget button appears in bottom-right corner"
echo "   5. Test full widget functionality"

echo ""
echo "✨ Local SDK Demo is ready for testing!"
