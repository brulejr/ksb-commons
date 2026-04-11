dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api(project(":ksb-commons-workflow-core"))

    // spring-managed dependencies
    api("org.springframework:spring-context")

    // additional dependencies
    api(libs.commonsLang3)

    // test dependencies
    testImplementation(project(":ksb-commons-test"))
    testImplementation(libs.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
