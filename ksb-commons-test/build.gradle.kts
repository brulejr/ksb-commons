dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api("io.mockk:mockk")
    api("org.apache.commons:commons-lang3")
    api("org.assertj:assertj-core")
}
