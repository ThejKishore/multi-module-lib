plugins{
    java
    id("spring-boot-library")
    id("sonar")
}

dependencies {
    implementation(project(":example-lib"))
    implementation("am.ik.yavi:yavi:0.16.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}