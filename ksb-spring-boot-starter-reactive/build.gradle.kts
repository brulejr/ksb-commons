plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api("io.jrb.labs:ksb-commons-core")
    api("io.jrb.labs:ksb-commons-ms-client")
    api("io.jrb.labs:ksb-commons-ms-core")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-actuator")
}

// Optional but nice: mark it as a “starter” in the pom (purely descriptive)
tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = "ksb-spring-boot-starter-reactive"
        attributes["Implementation-Version"] = project.version
    }
}
