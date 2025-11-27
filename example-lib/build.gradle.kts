plugins{
    id("spring-boot-library")
    id("sonar")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Lombok
    compileOnly(libs.spring.boot.webmvc)
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)
    // https://mvnrepository.com/artifact/am.ik.yavi/yavi
    implementation("am.ik.yavi:yavi:0.16.0")

    // Testing
    testImplementation("nl.jqno.equalsverifier:equalsverifier:4.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}