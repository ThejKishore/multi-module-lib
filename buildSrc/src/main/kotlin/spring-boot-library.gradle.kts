import utility.*

plugins{
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
    id("com.palantir.baseline-checkstyle")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Add each dependency from our arrays individually; Gradle cannot accept Array<String> directly
    Bundle.core.forEach { implementation(it) }
    runtimeOnly(Libs.h2)
    Bundle.testing.forEach { testImplementation(it) }
    testRuntimeOnly(Libs.h2)
}

dependencyManagement {
    imports {
        mavenBom(Libs.cloudDependencies+Versions.springCloud)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JDK_VERSION))
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