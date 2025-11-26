import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

val ksbVersion: String by project

// All modules that should be published to OSSRH/Maven Central
val publishableModules = listOf(
    "ksb-dependency-bom",
    "ksb-commons-core",
    "ksb-commons-ms-client",
    "ksb-commons-ms-core",
    "ksb-commons-test"
)

plugins {
    // Kotlin plugin is provided via buildSrc convention; no need to declare it here
    id("io.spring.dependency-management") version "1.1.7" apply false

    id("org.jetbrains.dokka") version "1.9.20"
}

allprojects {
    group = "io.jrb.labs"
    version = ksbVersion // X.Y.Z-SNAPSHOT for dev, X.Y.Z for release

    repositories {
        mavenCentral()
    }
}

subprojects {
    val isBom = (name == "ksb-dependency-bom")
    val isPublishable = name in publishableModules

    // --- Plugins per module type ---
    if (isBom) {
        apply(plugin = "java-platform")
    } else {
        // convention plugin for Kotlin/JVM libraries
        apply(plugin = "io.jrb.labs.kotlin-library")
        // plus Spring dependency management
        apply(plugin = "io.spring.dependency-management")
    }

    if (isPublishable) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }

    // --- Java toolchain for non-BOM modules ---
    if (!isBom) {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }

// --- Publishing config for BOM + other modules ---
    if (isPublishable) {

        if (!isBom) {
            // Only apply Dokka to JVM modules (skip BOM)
            plugins.withId("org.jetbrains.kotlin.jvm") {
                apply(plugin = "org.jetbrains.dokka")
            }
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    if (isBom) {
                        // Publish the BOM
                        from(components["javaPlatform"])
                    } else {
                        // Publish normal Java/Kotlin library
                        from(components["java"])
                    }

                    groupId = rootProject.group.toString()
                    artifactId = if (isBom) "ksb-dependency-bom" else project.name
                    version = rootProject.version.toString()

                    pom {
                        name.set(artifactId)
                        description.set("Part of the io.jrb.labs multi-module platform.")
                        url.set("https://github.com/brulejr/${rootProject.name}")

                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                id.set("brulejr")
                                name.set("Jon Brule")
                                email.set("brulejr@gmail.com")
                            }
                        }
                        scm {
                            url.set("https://github.com/brulejr/${rootProject.name}")
                            connection.set("scm:git:git://github.com/brulejr/${rootProject.name}.git")
                            developerConnection.set("scm:git:ssh://github.com:brulejr/${rootProject.name}.git")
                        }
                    }
                }
            }

            repositories {

                // --- Always available: GitHub Packages (works great in CI) ---
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/brulejr/${rootProject.name}")
                    credentials {
                        // GitHub Actions will provide these
                        username = System.getenv("GITHUB_ACTOR")
                            ?: (findProperty("gpr.user") as String?)  // optional local override
                        password = System.getenv("GITHUB_TOKEN")
                            ?: (findProperty("gpr.key") as String?)   // optional local override
                    }
                }

                // --- Conditionally available: OSSRH (only if creds are configured) ---
                val ossrhUser = (findProperty("ossrhUsername") as String?)
                    ?: System.getenv("OSSRH_USERNAME")
                val ossrhPass = (findProperty("ossrhPassword") as String?)
                    ?: System.getenv("OSSRH_PASSWORD")

                if (!ossrhUser.isNullOrBlank() && !ossrhPass.isNullOrBlank()) {
                    maven {
                        name = "OSSRH"
                        val isSnapshot = version.toString().endsWith("SNAPSHOT")
                        url = uri(
                            if (isSnapshot) {
                                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                            } else {
                                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                            }
                        )
                        credentials {
                            username = ossrhUser
                            password = ossrhPass
                        }
                    }
                } else {
                    logger.lifecycle("OSSRH credentials not configured; skipping OSSRH repository for ${project.path}")
                }
            }
        }

        // --- Signing: enabled only when keys are configured ---
        configure<SigningExtension> {
            val signingKey = findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
            val signingPassword = findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")

            if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
                // In-memory PGP key (good for CI)
                useInMemoryPgpKeys(signingKey, signingPassword)

                val publishing = extensions.getByType<PublishingExtension>()
                val publication = publishing.publications["maven"]
                sign(publication)
            } else {
                logger.warn("Signing key not configured; skipping signing for ${project.path}")
            }
        }
    }
}

// --- Release task: build + sign + publish for all publishable modules ---
tasks.register("release") {
    group = "release"
    description = "Builds, signs, and publishes all publishable modules to OSSRH."

    doFirst {
        // 1) Require non-SNAPSHOT version
        if (version.toString().endsWith("SNAPSHOT")) {
            throw GradleException("Release requires a non-SNAPSHOT version (current: $version)")
        }

        // 2) Require signing configured
        val signingKey = findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
        val signingPassword = findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")

        if (signingKey.isNullOrBlank() || signingPassword.isNullOrBlank()) {
            throw GradleException(
                "Release requires signingKey/signingPassword (or SIGNING_KEY/SIGNING_PASSWORD) to be configured."
            )
        }
    }

    // Clean everything first
    dependsOn("clean")

    // Publish each publishable subproject
    publishableModules.forEach { moduleName ->
        dependsOn(":$moduleName:publish")
    }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka/htmlMultiModule"))
}
