plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "ai.limechat.widget"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        targetSdk = 35

        consumerProguardFiles("consumer-rules.pro")
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

    testOptions {
        unitTests {
            // robolectric (test only): load sdk resources like ic_limechat_default in jvm tests
            isIncludeAndroidResources = true
        }
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
    testImplementation("org.robolectric:robolectric:4.14.1")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.github.wavicle-limechat"
                artifactId = "widget-sdk-android"
                version = "0.2.0-beta.1"
                
                pom {
                    name.set("LimeChat Widget Android SDK")
                    description.set("Official Android SDK for LimeChat widget integration with support for custom buttons and widget icons")
                    url.set("https://github.com/wavicle-limechat/widget-sdk-android")
                    
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("wavicle-limechat")
                            name.set("LimeChat Team")
                            email.set("support@limechat.ai")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://github.com/wavicle-limechat/widget-sdk-android.git")
                        developerConnection.set("scm:git:ssh://github.com/wavicle-limechat/widget-sdk-android.git")
                        url.set("https://github.com/wavicle-limechat/widget-sdk-android")
                    }
                }
            }
        }
    }
}