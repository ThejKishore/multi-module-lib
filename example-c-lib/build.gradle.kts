plugins{
    java
    id("spring-boot-library")
    id("sonar")
}

dependencies {
    implementation(project(":example-lib"))
}