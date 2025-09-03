pluginManagement {
    repositories {
        google() // Add this line
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "limechat-android-sdk"
include(":widget-sdk")
include(":local-sdk-demo")