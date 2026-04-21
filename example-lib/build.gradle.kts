import utility.*

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
    compileOnly(Libs.bootStarterWeb)
    annotationProcessor(Libs.lombok)
    compileOnly(Libs.lombok)
    // https://mvnrepository.com/artifact/am.ik.yavi/yavi
    implementation(Libs.yavi)

    // JWT - JJWT library for production-grade JWT validation
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Testing
    testImplementation(TestLibs.equalsVerifier)
    testImplementation(TestLibs.junitJupiterApi)
    testImplementation(TestLibs.junitJupiterEngine)
    testImplementation(TestLibs.junitPlatformLauncher)
    testImplementation(TestLibs.mockito)
    testImplementation(TestLibs.mockitoJunit)
    testImplementation(TestLibs.springTest)
}