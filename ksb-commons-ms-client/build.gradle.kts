dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    implementation(project(":ksb-commons-core"))

    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework:spring-web")
}
