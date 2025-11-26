dependencies {
    implementation(platform(project(":ksb-dependency-bom")))

    implementation(project(":ksb-commons-core"))

    api("org.springframework:spring-web")
}
