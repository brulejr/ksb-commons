// buildSrc/build.gradle.kts
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // This makes org.jetbrains.kotlin.jvm available to precompiled script plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
}
