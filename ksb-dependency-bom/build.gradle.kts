val ksbVersion: String by project

// Needed if you want this platform to depend on another platform (Spring BOM)
javaPlatform {
    allowDependencies()
}

dependencies {

    // 1) Extend Spring Boot's BOM
    api(
        platform(
            "org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"
        )
    )

    // 2) Pin versions of your own modules in the BOM
    constraints {
        // artifact dependencies
        api("io.jrb.labs:ksb-commons-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-client:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-test:$ksbVersion")
        api("io.jrb.labs:ksb-spring-boot-starter-reactive:${ksbVersion}")
        api("io.jrb.labs:ksb-spring-boot-starter-reactive-test:${ksbVersion}")
        // development dependencies
        api(libs.commonsLang3)
        api(libs.hivemqMqttClient)

        // test dependencies
        api(libs.mockk)
    }
}
