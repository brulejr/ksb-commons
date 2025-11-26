val ksbVersion: String by project

dependencies {
    constraints {
        // artifact dependencies
        api("io.jrb.labs:ksb-commons-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-client:$ksbVersion")
        api("io.jrb.labs:ksb-commons-ms-core:$ksbVersion")
        api("io.jrb.labs:ksb-commons-test:$ksbVersion")

        // development dependencies
        api(libs.jacksonDatabind)
        api(libs.hivemqMqttClient)
        api(libs.reactorCore)
        api(libs.commonsLang3)
        api(libs.kotlinStdlib)
        api(libs.kotlinReflect)
        api(libs.kotlinxCoroutinesReactor)
        api(libs.slf4jApi)
        api(libs.springContext)
        api(libs.springWeb)
        api(libs.springWebflux)
        api(libs.springBoot)

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
