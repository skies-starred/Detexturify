pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter.create(rootProject) {
    version("1.21.11").buildscript = "build.obf.gradle.kts"
    versions("26.1", "26.2").buildscript = "build.gradle.kts"

    vcsVersion = "1.21.11"
}