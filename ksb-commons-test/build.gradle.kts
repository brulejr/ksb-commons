plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    `java-library`
    `maven-publish`
    signing
}

dependencies {
    implementation(platform(project(":dependency-bom")))

    api("io.mockk:mockk")
    api("org.apache.commons:commons-lang3")
    api("org.assertj:assertj-core")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21)
}
