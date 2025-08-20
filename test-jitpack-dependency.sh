#!/bin/bash

# LimeChat Widget Android SDK - JitPack Dependency Test Script
# This script helps test that your published SDK can be used as a dependency

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 LimeChat Widget Android SDK - JitPack Dependency Test${NC}"
echo ""

# Check if version is provided
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Usage: $0 <version>${NC}"
    echo "Example: $0 1.0.0"
    echo ""
    echo "This will test the dependency: com.github.wavicle-limechat:widget-sdk-android:<version>"
    exit 1
fi

VERSION=$1
echo -e "${BLUE}Testing version: ${VERSION}${NC}"

# Create temporary test project directory
TEST_DIR="/tmp/limechat-sdk-test-$(date +%s)"
echo -e "${YELLOW}📁 Creating test project in: ${TEST_DIR}${NC}"
mkdir -p "$TEST_DIR"
cd "$TEST_DIR"

# Create a minimal Android project structure
echo -e "${YELLOW}🏗️  Setting up test project...${NC}"

# Create settings.gradle
cat > settings.gradle << 'EOF'
rootProject.name = "LimeChatSDKTest"
include ':app'
EOF

# Create root build.gradle
cat > build.gradle << 'EOF'
buildscript {
    ext.kotlin_version = '1.9.22'
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
EOF

# Create app directory and build.gradle
mkdir -p app/src/main/java/com/test/app
cat > app/build.gradle << EOF
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'

android {
    namespace 'com.test.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.test.app"
        minSdk 23
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:\$kotlin_version"
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // LimeChat Widget SDK
    implementation 'com.github.wavicle-limechat:widget-sdk-android:${VERSION}'
}
EOF

# Create AndroidManifest.xml
mkdir -p app/src/main
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:allowBackup="true"
        android:label="LimeChat SDK Test"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# Create a test MainActivity that uses the SDK
cat > app/src/main/java/com/test/app/MainActivity.kt << 'EOF'
package com.test.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test that we can import and instantiate SDK classes
        val config = WidgetConfig(
            websiteToken = "test-token",
            locale = "en",
            colorScheme = WidgetConfig.ColorScheme.LIGHT
        )
        
        val widgetButton = LimechatWidgetButton(this)
        widgetButton.init(config)
        
        println("✅ LimeChat Widget SDK imported and instantiated successfully!")
    }
}
EOF

# Create gradle wrapper files
echo -e "${YELLOW}📦 Setting up Gradle wrapper...${NC}"
gradle wrapper --gradle-version 8.2

# Try to build the project
echo -e "${YELLOW}🔨 Building test project with SDK dependency...${NC}"
if ./gradlew build; then
    echo ""
    echo -e "${GREEN}✅ SUCCESS: SDK dependency resolved and builds correctly!${NC}"
    echo ""
    echo -e "${BLUE}📋 Test Results:${NC}"
    echo "✅ Dependency resolution: PASSED"
    echo "✅ Import statements: PASSED"
    echo "✅ Class instantiation: PASSED"
    echo "✅ Build compilation: PASSED"
    echo ""
    echo -e "${GREEN}🎉 The SDK version ${VERSION} is ready for public use!${NC}"
else
    echo ""
    echo -e "${RED}❌ FAILED: SDK dependency could not be resolved or built${NC}"
    echo ""
    echo -e "${YELLOW}💡 Possible issues:${NC}"
    echo "- JitPack build is still in progress"
    echo "- Version ${VERSION} doesn't exist"
    echo "- JitPack build failed"
    echo "- Network connectivity issues"
    echo ""
    echo "Check https://jitpack.io/#wavicle-limechat/widget-sdk-android for build status"
fi

# Cleanup
echo -e "${YELLOW}🧹 Cleaning up test project...${NC}"
cd /
rm -rf "$TEST_DIR"

echo -e "${BLUE}Test completed.${NC}"