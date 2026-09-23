pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://nexus-external.vkteam.ru/repository/vkid-sdk-android/")
        maven(url = "https://nexus-external.vkteam.ru/repository/maven/")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://nexus-external.vkteam.ru/repository/vkid-sdk-android/")
        maven(url = "https://nexus-external.vkteam.ru/repository/maven/")
    }
}

rootProject.name = "ReshalaAlfa0.1"
include(":app")
 