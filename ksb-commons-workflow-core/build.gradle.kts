dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api(project(":ksb-commons-core"))

    testImplementation(project(":ksb-commons-test"))
    testImplementation(libs.mockk)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
