dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api(project(":ksb-commons-core"))
    api(project(":ksb-commons-ms-client"))

    api("org.springframework:spring-context")
    api("org.springframework:spring-web")

    testImplementation(project(":ksb-commons-core"))
    testImplementation(project(":ksb-commons-test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
