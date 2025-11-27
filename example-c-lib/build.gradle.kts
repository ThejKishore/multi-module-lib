plugins{
    java
    id("spring-boot-library")
    id("sonar")
}

dependencies {
    implementation(project(":example-lib"))
    implementation("am.ik.yavi:yavi:0.16.0")
}