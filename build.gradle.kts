import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

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
    val isPublishable = name in setOf("ksb-dependency-bom", "common-core")

    // Plugins per type
    if (isBom) {
        apply(plugin = "java-platform")
    } else {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "java-library")
    }

    if (isPublishable) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }

    // Common Java/Kotlin config for non-BOM projects
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

    // Publishing config for BOM + other modules
    if (isPublishable) {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    if (isBom) {
                        from(components["javaPlatform"])
                    } else {
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
                        username = findProperty("ossrhUsername") as String?
                            ?: System.getenv("OSSRH_USERNAME")
                        password = findProperty("ossrhPassword") as String?
                            ?: System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
        }

        configure<SigningExtension> {
            val signingKey = findProperty("signingKey") as String? ?: System.getenv("SIGNING_KEY")
            val signingPassword = findProperty("signingPassword") as String? ?: System.getenv("SIGNING_PASSWORD")

            if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
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
