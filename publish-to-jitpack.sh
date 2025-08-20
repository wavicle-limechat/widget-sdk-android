#!/bin/bash

# LimeChat Widget Android SDK - JitPack Publishing Script
# This script helps you publish your SDK to JitPack

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 LimeChat Widget Android SDK - JitPack Publishing Script${NC}"
echo ""

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Not in a git repository${NC}"
    echo "Please run this script from the root of your git repository."
    exit 1
fi

# Check if there are uncommitted changes
if ! git diff-index --quiet HEAD --; then
    echo -e "${YELLOW}⚠️  Warning: You have uncommitted changes${NC}"
    echo "Please commit or stash your changes before publishing."
    echo ""
    read -p "Do you want to continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Publishing cancelled."
        exit 1
    fi
fi

# Get current version from build.gradle.kts
CURRENT_VERSION=$(grep -o 'version = "[^"]*"' widget-sdk/build.gradle.kts | cut -d'"' -f2)
echo -e "${BLUE}📦 Current version: ${CURRENT_VERSION}${NC}"

# Ask for new version
echo ""
read -p "Enter new version (or press Enter to keep current): " NEW_VERSION
if [ -z "$NEW_VERSION" ]; then
    NEW_VERSION=$CURRENT_VERSION
fi

echo -e "${BLUE}📦 Publishing version: ${NEW_VERSION}${NC}"

# Update version in build.gradle.kts
echo -e "${YELLOW}📝 Updating version in build.gradle.kts...${NC}"
sed -i.bak "s/version = \"[^\"]*\"/version = \"${NEW_VERSION}\"/g" widget-sdk/build.gradle.kts
rm widget-sdk/build.gradle.kts.bak

# Build the project
echo -e "${YELLOW}🔨 Building project...${NC}"
./gradlew clean
./gradlew :widget-sdk:assembleRelease

# Test the build
echo -e "${YELLOW}🧪 Running tests...${NC}"
./gradlew :widget-sdk:test

# Build sample app to ensure integration works
echo -e "${YELLOW}🏗️  Building sample app...${NC}"
./gradlew :sample-app:assembleDebug

# Commit changes
echo -e "${YELLOW}📝 Committing changes...${NC}"
if ! git diff-index --quiet HEAD --; then
    git add widget-sdk/build.gradle.kts
    git commit -m "Bump version to ${NEW_VERSION}"
else
    echo "No changes to commit."
fi

# Create and push tag
echo -e "${YELLOW}🏷️  Creating tag v${NEW_VERSION}...${NC}"
git tag -a "v${NEW_VERSION}" -m "Release version ${NEW_VERSION}"

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
echo -e "${YELLOW}📤 Pushing current branch (${CURRENT_BRANCH})...${NC}"
git push origin "${CURRENT_BRANCH}"

echo -e "${YELLOW}📤 Pushing tag...${NC}"
git push origin "v${NEW_VERSION}"

echo ""
echo -e "${GREEN}✅ Successfully published version ${NEW_VERSION} to JitPack!${NC}"
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo "1. Go to https://jitpack.io/#wavicle-limechat/widget-sdk-android"
echo "2. Wait for the build to complete (usually 2-5 minutes)"
echo "3. Click 'Get it' to see the dependency coordinates"
echo ""
echo -e "${BLUE}🔗 Users can now add your SDK using:${NC}"
echo ""
echo -e "${GREEN}repositories {${NC}"
echo -e "${GREEN}    maven { url 'https://jitpack.io' }${NC}"
echo -e "${GREEN}}${NC}"
echo ""
echo -e "${GREEN}dependencies {${NC}"
echo -e "${GREEN}    implementation 'com.github.wavicle-limechat:widget-sdk-android:${NEW_VERSION}'${NC}"
echo -e "${GREEN}}${NC}"
echo ""
echo -e "${YELLOW}💡 Tips:${NC}"
echo "- Make sure your repository is public"
echo "- Check that jitpack.yml is properly configured"
echo "- Monitor the build logs on JitPack for any issues"
echo "- Test the dependency in a new project before announcing"
echo "- Update documentation with the new version"
echo ""
echo -e "${BLUE}📖 For detailed integration guide, see:${NC}"
echo "https://github.com/wavicle-limechat/widget-sdk-android"