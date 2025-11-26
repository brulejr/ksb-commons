dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("io.projectreactor:reactor-core")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.slf4j:slf4j-api")

    testImplementation(project(":ksb-commons-test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
