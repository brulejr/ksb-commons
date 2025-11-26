dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    // spring-managed dependencies
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("io.projectreactor:reactor-core")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.slf4j:slf4j-api")

    // additional dependencies
    api(libs.commonsLang3)

    // test dependencies
    testImplementation(project(":ksb-commons-test"))
    testImplementation(libs.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
