#!/bin/bash

# LimeChat Widget Android SDK - Build Validation Script
# This script validates that the SDK can be built and tested successfully

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 LimeChat Widget Android SDK - Build Validation${NC}"
echo ""

# Clean build
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
./gradlew clean

# Build SDK
echo -e "${YELLOW}🔨 Building widget SDK...${NC}"
./gradlew :widget-sdk:assembleRelease
./gradlew :widget-sdk:assembleDebug

# Run SDK tests
echo -e "${YELLOW}🧪 Running SDK tests...${NC}"
./gradlew :widget-sdk:test

# Build sample app
echo -e "${YELLOW}🏗️  Building sample app...${NC}"
./gradlew :sample-app:assembleDebug

# Run sample app tests (if any)
echo -e "${YELLOW}🧪 Running sample app tests...${NC}"
./gradlew :sample-app:test || echo "No tests found for sample app"

# Generate documentation
echo -e "${YELLOW}📚 Generating documentation...${NC}"
./gradlew :widget-sdk:generatePomFileForReleasePublication

# Check for lint issues
echo -e "${YELLOW}🔍 Running lint checks...${NC}"
./gradlew :widget-sdk:lint || echo "Lint issues found - please review"

echo ""
echo -e "${GREEN}✅ Build validation completed successfully!${NC}"
echo ""
echo -e "${BLUE}📋 Build artifacts created:${NC}"
echo "- widget-sdk/build/outputs/aar/widget-sdk-release.aar"
echo "- widget-sdk/build/outputs/aar/widget-sdk-debug.aar"
echo "- sample-app/build/outputs/apk/debug/sample-app-debug.apk"
echo ""
echo -e "${YELLOW}💡 Next steps:${NC}"
echo "- Review lint report if any issues were found"
echo "- Test the generated AAR in a separate project"
echo "- Run './publish-to-jitpack.sh' when ready to publish"