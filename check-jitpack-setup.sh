#!/bin/bash

# LimeChat Widget Android SDK - JitPack Setup Verification Script
# This script checks that all required files for JitPack publishing are present and correctly configured

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 LimeChat Widget Android SDK - JitPack Setup Verification${NC}"
echo ""

# Check if required files exist
FILES_TO_CHECK=(
    "jitpack.yml"
    "widget-sdk/build.gradle.kts"
    "publish-to-jitpack.sh"
    "validate-build.sh"
    "test-jitpack-dependency.sh"
    "JITPACK_PUBLISHING.md"
)

echo -e "${YELLOW}📁 Checking required files...${NC}"
MISSING_FILES=()

for file in "${FILES_TO_CHECK[@]}"; do
    if [ -f "$file" ]; then
        echo -e "  ✅ $file"
    else
        echo -e "  ❌ $file"
        MISSING_FILES+=("$file")
    fi
done

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ Missing required files for JitPack publishing${NC}"
    exit 1
fi

# Check jitpack.yml content
echo ""
echo -e "${YELLOW}📝 Checking jitpack.yml configuration...${NC}"

if grep -q "openjdk17" jitpack.yml; then
    echo -e "  ✅ Java 17 configured"
else
    echo -e "  ❌ Java 17 not configured"
fi

if grep -q ":widget-sdk:assembleRelease" jitpack.yml; then
    echo -e "  ✅ Build command configured"
else
    echo -e "  ❌ Build command not configured"
fi

if grep -q ":widget-sdk:test" jitpack.yml; then
    echo -e "  ✅ Test command configured"
else
    echo -e "  ❌ Test command not configured"
fi

# Check build.gradle.kts publishing configuration
echo ""
echo -e "${YELLOW}📦 Checking publishing configuration...${NC}"

if grep -q "maven-publish" widget-sdk/build.gradle.kts; then
    echo -e "  ✅ Maven publish plugin applied"
else
    echo -e "  ❌ Maven publish plugin not applied"
fi

if grep -q "groupId.*com.github.wavicle-limechat" widget-sdk/build.gradle.kts; then
    echo -e "  ✅ Group ID configured"
else
    echo -e "  ❌ Group ID not configured"
fi

if grep -q "artifactId.*widget-sdk-android" widget-sdk/build.gradle.kts; then
    echo -e "  ✅ Artifact ID configured"
else
    echo -e "  ❌ Artifact ID not configured"
fi

if grep -q "version.*=" widget-sdk/build.gradle.kts; then
    VERSION=$(grep -o 'version = "[^"]*"' widget-sdk/build.gradle.kts | cut -d'"' -f2)
    echo -e "  ✅ Version configured: $VERSION"
else
    echo -e "  ❌ Version not configured"
fi

# Check script permissions
echo ""
echo -e "${YELLOW}🔧 Checking script permissions...${NC}"

SCRIPTS=(
    "publish-to-jitpack.sh"
    "validate-build.sh" 
    "test-jitpack-dependency.sh"
    "check-jitpack-setup.sh"
)

for script in "${SCRIPTS[@]}"; do
    if [ -x "$script" ]; then
        echo -e "  ✅ $script is executable"
    else
        echo -e "  ❌ $script is not executable"
        chmod +x "$script"
        echo -e "     ➡️  Made $script executable"
    fi
done

# Check git configuration
echo ""
echo -e "${YELLOW}📚 Checking git repository...${NC}"

if git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "  ✅ Git repository detected"
    
    # Check for remote origin
    if git remote get-url origin > /dev/null 2>&1; then
        ORIGIN_URL=$(git remote get-url origin)
        echo -e "  ✅ Git origin: $ORIGIN_URL"
        
        if [[ $ORIGIN_URL == *"github.com"* ]]; then
            echo -e "  ✅ GitHub repository detected"
        else
            echo -e "  ⚠️  Not a GitHub repository - JitPack requires GitHub"
        fi
    else
        echo -e "  ❌ No git origin configured"
    fi
else
    echo -e "  ❌ Not a git repository"
fi

# Check Gradle wrapper
echo ""
echo -e "${YELLOW}⚙️  Checking Gradle setup...${NC}"

if [ -f "gradlew" ]; then
    echo -e "  ✅ Gradle wrapper present"
    
    if [ -x "gradlew" ]; then
        echo -e "  ✅ Gradle wrapper is executable"
    else
        echo -e "  ❌ Gradle wrapper is not executable"
        chmod +x gradlew
        echo -e "     ➡️  Made gradlew executable"
    fi
else
    echo -e "  ❌ Gradle wrapper not found"
    echo -e "     ➡️  Run 'gradle wrapper' to create it"
fi

# Summary
echo ""
echo -e "${BLUE}📋 Setup Summary${NC}"
echo ""

if [ ${#MISSING_FILES[@]} -eq 0 ]; then
    echo -e "${GREEN}✅ All required files are present${NC}"
    echo -e "${GREEN}✅ Publishing configuration looks good${NC}"
    echo -e "${GREEN}✅ Scripts are properly configured${NC}"
    echo ""
    echo -e "${BLUE}🚀 Next steps:${NC}"
    echo "1. Test local build: ./validate-build.sh"
    echo "2. Publish to JitPack: ./publish-to-jitpack.sh"
    echo "3. Test dependency: ./test-jitpack-dependency.sh <version>"
    echo ""
    echo -e "${YELLOW}💡 Tips:${NC}"
    echo "- Make sure your repository is public on GitHub"
    echo "- Commit all changes before publishing"
    echo "- Test locally before publishing to JitPack"
else
    echo -e "${RED}❌ Setup incomplete - missing files detected${NC}"
    echo ""
    echo -e "${YELLOW}Required actions:${NC}"
    for file in "${MISSING_FILES[@]}"; do
        echo "- Create missing file: $file"
    done
fi

echo ""
echo -e "${BLUE}📖 For detailed instructions, see: JITPACK_PUBLISHING.md${NC}"