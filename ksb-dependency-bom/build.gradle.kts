val ksbVersion: String by project

dependencies {
    constraints {
        // artifact dependencies
        api("io.jrb.labs:ksb-commons-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-client:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-test:$ksbVersion")
        api("io.jrb.labs:ksb-spring-boot-starter-reactive:${ksbVersion}")

        // development dependencies
        api(libs.commonsLang3)
        api(libs.hivemqMqttClient)
        api(libs.jacksonDatabind)
        api(libs.kotlinxCoroutinesReactor)
        api(libs.kotlinReflect)
        api(libs.kotlinStdlib)
        api(libs.reactorCore)
        api(libs.reactorKotlinExtensions)
        api(libs.slf4jApi)
        api(libs.springBoot)
        api(libs.springContext)
        api(libs.springWeb)
        api(libs.springWebflux)
        api(libs.springStarterActuator)
        api(libs.springStarterWebflux)

        // test dependencies
        api(libs.mockk)
        api(libs.assertjCore)
        api(libs.kotlinTestJunit5)
        api(libs.kotlinxCoroutinesTest)
        api(libs.junitJupiterParams)
        api(libs.springBootStarterTest)
        api(libs.reactorTest)
        api(libs.springCloudStreamTestBinder)
        api(libs.junitPlatformLauncher)
    }
}
