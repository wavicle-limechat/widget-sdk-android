plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.publishedsdkdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.publishedsdkdemo"
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
    // CRITICAL: This is the ONLY SDK dependency - published from JitPack
    // This proves the SDK works independently without local source code
    implementation("com.github.wavicle-limechat:widget-sdk-android:0.0.4")
    
    // Basic Android dependencies (minimal set)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // No other dependencies - keeping it minimal to prove SDK independence
}