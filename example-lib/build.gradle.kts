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
    // Testing
    testImplementation(TestLibs.equalsVerifier)
    testImplementation(TestLibs.junitJupiterApi)
    testImplementation(TestLibs.junitJupiterEngine)
    testImplementation(TestLibs.junitPlatformLauncher)
}