import gradle.kotlin.dsl.accessors._93b339c743cc5768c004a743c093f389.implementation
import gradle.kotlin.dsl.accessors._93b339c743cc5768c004a743c093f389.testImplementation

plugins{
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
    id("com.palantir.baseline-checkstyle")
}

extra["springCloudVersion"] = "2025.1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findBundle("core-spring-boot").get())
    runtimeOnly(libs.findLibrary("h2").get())
    testImplementation(libs.findBundle("core-testing").get())
    testRuntimeOnly(libs.findLibrary("h2").get())
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


//  Disable Spring Boot executable jar for library modules; keep plain jar
tasks.named("bootJar") {
    enabled = false
}
tasks.named("jar") {
    enabled = true
}