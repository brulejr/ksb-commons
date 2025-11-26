dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    // spring-managed dependencies
    api("org.assertj:assertj-core")

    // additional dependencies
    api(libs.commonsLang3)
    api(libs.mockk)
}
