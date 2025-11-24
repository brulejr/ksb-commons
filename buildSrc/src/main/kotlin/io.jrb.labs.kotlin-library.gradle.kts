import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // IMPORTANT: no version here for precompiled script plugins
    kotlin("jvm")
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}
