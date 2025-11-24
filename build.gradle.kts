plugins {
    kotlin("jvm") version "2.2.10" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "io.jrb.labs"
    version = "0.1.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    val isBom = (name == "ksb-dependency-bom")

    if (isBom) {
        apply(plugin = "java-platform")
    } else {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }

    if (!isBom) {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            withSourcesJar()
            withJavadocJar()
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}