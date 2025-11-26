plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Use your BOM inside this module to get aligned versions
    api(platform(project(":ksb-dependency-bom")))

    // If you want the starter to bring in your common libs as well:
    api(project(":ksb-commons-core"))
    api(project(":ksb-commons-ms-core"))
    api(project(":ksb-commons-ms-client"))

    // Spring Boot starters – versions come from the BOM above
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-actuator")
}

// Optional, just for a nicer manifest
tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = "ksb-spring-boot-starter-reactive"
        attributes["Implementation-Version"] = project.version
    }
}
