plugins{
    id("spring-boot-application")
    id("docker")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":example-lib"))
    implementation(project(":example-b-lib"))
    implementation(project(":example-c-lib"))


}
