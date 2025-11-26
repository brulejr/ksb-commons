dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api(project(":ksb-commons-core"))
    api(project(":ksb-commons-ms-client"))

    api("org.springframework:spring-context")
    api("org.springframework:spring-web")

    api(libs.hivemqMqttClient)

    testImplementation(project(":ksb-commons-test"))
    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
