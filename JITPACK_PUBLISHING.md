# Publishing LimeChat Widget Android SDK to JitPack

This guide provides complete instructions for publishing the LimeChat Widget Android SDK to JitPack, making it easily available for developers to integrate into their Android projects.

## Prerequisites

1. **GitHub Repository**: Your code must be in a public GitHub repository
2. **Git Setup**: Ensure you have git configured with your credentials
3. **Java 17+**: Required for building the project
4. **Android SDK**: Required for Android development

## Quick Start

### 1. Automated Publishing (Recommended)

Use the provided script for easy publishing:

```bash
./publish-to-jitpack.sh
```

The script will:
- ✅ Check for uncommitted changes
- ✅ Update the version number
- ✅ Build and test the project
- ✅ Build the sample app to ensure integration works
- ✅ Create a git tag
- ✅ Push to GitHub
- ✅ Provide next steps and usage instructions

### 2. Manual Publishing

If you prefer to publish manually:

#### Step 1: Update Version

Edit `widget-sdk/build.gradle.kts` and update the version:

```kotlin
version = "1.0.1" // or your desired version
```

#### Step 2: Build and Test

```bash
./gradlew clean
./gradlew :widget-sdk:assembleRelease
./gradlew :widget-sdk:test
./gradlew :sample-app:assembleDebug
```

#### Step 3: Commit and Tag

```bash
git add widget-sdk/build.gradle.kts
git commit -m "Bump version to 1.0.1"
git tag -a "v1.0.1" -m "Release version 1.0.1"
git push origin main
git push origin "v1.0.1"
```

## Configuration Files

### jitpack.yml

This file tells JitPack how to build your project:

```yaml
jdk:
  - openjdk17

before_install:
  - ./gradlew clean

install:
  - ./gradlew :widget-sdk:assembleRelease
  - ./gradlew :widget-sdk:generatePomFileForReleasePublication
  - ./gradlew :widget-sdk:publishReleasePublicationToMavenLocal

script:
  - ./gradlew :widget-sdk:test
  - ./gradlew :widget-sdk:assembleRelease
  - ./gradlew :widget-sdk:generatePomFileForReleasePublication
  - ./gradlew :widget-sdk:publishReleasePublicationToMavenLocal
```

### Publishing Configuration

The publishing configuration in `widget-sdk/build.gradle.kts`:

```kotlin
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.github.wavicle-limechat"
                artifactId = "widget-sdk-android"
                version = "1.0.0"
                
                pom {
                    name.set("LimeChat Widget Android SDK")
                    description.set("Official Android SDK for LimeChat widget integration with support for custom buttons and widget icons")
                    url.set("https://github.com/limechat/limechat-widget-sdks")
                    
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("limechat")
                            name.set("LimeChat Team")
                            email.set("support@limechat.ai")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://github.com/limechat/limechat-widget-sdks.git")
                        developerConnection.set("scm:git:ssh://github.com/limechat/limechat-widget-sdks.git")
                        url.set("https://github.com/limechat/limechat-widget-sdks")
                    }
                }
            }
        }
    }
}
```

## Publishing Process

### 1. Create a Release

After pushing your tag, JitPack will automatically detect it:

1. Go to https://jitpack.io/#wavicle-limechat/widget-sdk-android
2. You'll see your tag listed
3. Click "Get it" to build the release
4. Wait for the build to complete (usually 2-5 minutes)

### 2. Verify the Build

Check the build logs for any errors. The build process includes:
- Compiling the SDK
- Running tests
- Generating documentation
- Creating the AAR artifact

### 3. Test the Dependency

Use the provided test script:

```bash
./test-jitpack-dependency.sh 1.0.0
```

This creates a temporary project and tests that the dependency can be resolved and used.

## Usage Instructions for Developers

Once published, developers can integrate your SDK:

### Step 1: Add JitPack Repository

Add to the project's `build.gradle` (Project level):

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Or in `settings.gradle` (for newer Gradle versions):

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add Dependency

Add to the app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.wavicle-limechat:widget-sdk-android:1.0.0'
}
```

### Step 3: Use the SDK

```kotlin
import ai.limechat.widget.LimechatWidgetButton
import ai.limechat.widget.models.WidgetConfig

// Create widget configuration
val config = WidgetConfig(
    websiteToken = "YOUR_WEBSITE_TOKEN",
    locale = "en",
    colorScheme = WidgetConfig.ColorScheme.LIGHT
)

// Create and initialize widget button
val widgetButton = LimechatWidgetButton(this)
widgetButton.init(config)

// Set click listener
widgetButton.setOnClickListener {
    // Open your widget
}

// Add to layout
yourLayout.addView(widgetButton)
```

## Build Validation

Use the validation script to ensure everything builds correctly:

```bash
./validate-build.sh
```

This script:
- ✅ Cleans previous builds
- ✅ Builds SDK in release and debug modes
- ✅ Runs all tests
- ✅ Builds sample app
- ✅ Generates documentation
- ✅ Runs lint checks

## Troubleshooting

### Common Issues

#### 1. Build Fails on JitPack

**Symptoms**: Build fails with compilation errors

**Solutions**:
- Ensure your `jitpack.yml` is correct
- Check that all dependencies are available
- Verify the project builds locally with `./validate-build.sh`
- Check Java version compatibility

#### 2. Dependency Not Found

**Symptoms**: Gradle can't resolve the dependency

**Solutions**:
- Wait for the JitPack build to complete
- Check the exact artifact coordinates
- Ensure the repository is public
- Verify the tag exists on GitHub

#### 3. Version Not Available

**Symptoms**: Specific version not found

**Solutions**:
- Check that the git tag exists and follows format `v1.0.0`
- Wait for JitPack to process the tag
- Check build logs on JitPack for errors

#### 4. Runtime Issues

**Symptoms**: SDK crashes or doesn't work at runtime

**Solutions**:
- Check ProGuard/R8 rules
- Verify all required permissions are declared
- Test with the sample app first
- Check for dependency conflicts

### Debugging Steps

1. **Local Build Test**:
   ```bash
   ./validate-build.sh
   ```

2. **Check JitPack Status**:
   - Go to https://jitpack.io/#limechat/limechat-widget-sdks
   - Click on the build log for your version
   - Look for error messages

3. **Test Dependency**:
   ```bash
   ./test-jitpack-dependency.sh 1.0.0
   ```

4. **Verify Configuration**:
   - Check `jitpack.yml` syntax
   - Verify `build.gradle.kts` publishing config
   - Ensure all required files are committed

## Best Practices

### 1. Version Management

- Use semantic versioning (MAJOR.MINOR.PATCH)
- Create meaningful release notes
- Tag releases immediately after testing
- Update documentation with each release

### 2. Testing

- Always run `./validate-build.sh` before publishing
- Test the published dependency with `./test-jitpack-dependency.sh`
- Test integration in a real project
- Verify both Kotlin and Java compatibility

### 3. Documentation

- Keep README.md updated with latest version
- Include complete usage examples
- Document breaking changes clearly
- Provide migration guides for major versions

### 4. Quality Assurance

- Run tests before publishing
- Check for lint warnings
- Verify ProGuard/R8 compatibility
- Test on different Android versions

## Available Scripts

| Script | Purpose | Usage |
|--------|---------|-------|
| `publish-to-jitpack.sh` | Automated publishing to JitPack | `./publish-to-jitpack.sh` |
| `validate-build.sh` | Validate build and tests | `./validate-build.sh` |
| `test-jitpack-dependency.sh` | Test published dependency | `./test-jitpack-dependency.sh 1.0.0` |

## Example Workflow

Complete example of publishing version 1.0.1:

```bash
# 1. Validate current build
./validate-build.sh

# 2. Publish new version (interactive script)
./publish-to-jitpack.sh
# Enter version: 1.0.1

# 3. Wait for JitPack build (2-5 minutes)

# 4. Test the published dependency
./test-jitpack-dependency.sh 1.0.1

# 5. Update documentation and announce release
```

## Monitoring and Maintenance

### JitPack Status

Monitor your builds at: https://jitpack.io/#limechat/limechat-widget-sdks

### Build Metrics

JitPack provides:
- Build success/failure rates
- Download statistics
- Build duration tracking
- Error logs and debugging info

### Version Usage

Track which versions are being used:
- Check download statistics on JitPack
- Monitor GitHub dependency insights
- Review user feedback and issues

## Support

If you encounter issues:

1. Check this documentation
2. Review build logs on JitPack
3. Test locally with provided scripts
4. Check [JitPack documentation](https://jitpack.io/docs/)
5. Create an issue in the GitHub repository

## Next Steps

After successful publishing:

1. Update integration documentation
2. Announce the release
3. Monitor for issues
4. Plan the next release
5. Gather user feedback

Remember: JitPack builds are cached, so subsequent builds of the same version will be faster, but you cannot overwrite an existing version.