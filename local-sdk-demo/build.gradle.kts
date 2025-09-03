plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.localsdkdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.localsdkdemo"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // CRITICAL: This is the LOCAL SDK dependency - NOT published from JitPack
    // This proves the SDK works with local development
    implementation(project(":widget-sdk"))
    
    // Basic Android dependencies (minimal set)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // No other dependencies - keeping it minimal to prove local SDK works
}
